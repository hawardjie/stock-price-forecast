package postsea.investment.stock.model;

public class Edge {
    StockNode targetNode;
    double weight;

    public Edge(StockNode targetNode, double weight) {
        this.targetNode = targetNode;
        this.weight = weight;
    }
}
