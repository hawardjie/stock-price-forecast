package postsea.investment.stock.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockNode {
    double price;
    LocalDate timestamp;
    List<Edge> edgeList;

    public StockNode(double price, LocalDate timestamp) {
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
