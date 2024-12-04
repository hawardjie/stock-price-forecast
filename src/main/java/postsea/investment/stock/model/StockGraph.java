package postsea.investment.stock.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockGraph {
    List<StockNode> nodes;
    int windowSize;

    public StockGraph() {
        this.nodes = new ArrayList<>();
        this.windowSize = 5;
    }

    public void addNode(double price, LocalDate timestamp) {
        StockNode node = new StockNode(price, timestamp);
        this.nodes.add(node);

        if (this.nodes.size() > 1) {
            StockNode previousNode = this.nodes.get(this.nodes.size() - 2);
            double priceChange = price - previousNode.price;
            double weight = Math.exp(-Math.abs(priceChange));
            previousNode.addEdge(node, weight);
        }
    }

    public double predictNextPrice() {
        if (this.nodes.size() < this.windowSize) {
            throw new IllegalStateException("Not enough data points for prediction");
        }

        List<StockNode> recentNodes = this.nodes.subList(this.nodes.size() - this.windowSize, this.nodes.size());

        double weightedSum = 0;
        double weightSum = 0;

        for (int i = 0; i < recentNodes.size(); i++) {
            double weight = Math.exp(i / (double) recentNodes.size());
            weightedSum += recentNodes.get(i).price * weight;
            weightSum += weight;
        }

        double[] priceChanges = new double[recentNodes.size() - 1];
        for (int i = 1; i < recentNodes.size(); i++) {
            priceChanges[i - 1] = recentNodes.get(i).price - recentNodes.get(i - 1).price;
        }

        double trend = mean(priceChanges);

        double basePredict = weightedSum / weightSum;
        double prediction = basePredict + trend;

        return Math.max(0, prediction);
    }

    private double mean(double[] values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    public double calculateAccuracy() {
        if (this.nodes.size() < 2) {
            return 0;
        }

        double totalError = 0;
        int predictions = 0;

        for (int i = this.windowSize; i < this.nodes.size(); i++) {
            List<StockNode> historicalNodes = this.nodes.subList(0, i);
            double actualPrice = this.nodes.get(i).price;

            StockGraph tempGraph = new StockGraph();
            tempGraph.nodes = new ArrayList<>(historicalNodes);
            tempGraph.windowSize = this.windowSize;

            double predictedPrice = tempGraph.predictNextPrice();
            double error = Math.abs((predictedPrice - actualPrice) / actualPrice);

            totalError += error;
            predictions++;
        }

        return predictions > 0 ? (1 - totalError / predictions) * 100 : 0;
    }

    public List<StockNode> getNodes() {
        return this.nodes;
    }
}
