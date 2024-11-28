package postsea.investment.stock.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockNode {
    double price;
    Date timestamp;
    List<Edge> edgeList;

    public StockNode(double price, Date timestamp) {
        this.price = price;
        this.timestamp = timestamp;
        this.edgeList = new ArrayList<>();
    }

    public void addEdge(StockNode targetNode, double weight) {
        this.edgeList.add(new Edge(targetNode, weight));
    }

    public double getPrice() {
        return price;
    }
}
