package postsea.investment.stock;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import postsea.investment.stock.model.StockGraph;
import postsea.investment.stock.model.StockNode;

import java.time.LocalDate;

public class StockForecastApplication extends Application {
    @Override
    public void start(Stage stage) {

        StockGraph stockGraph = new StockGraph();

        // Sample historical data
        stockGraph.addNode(100, LocalDate.of(2024, 1, 1));
        stockGraph.addNode(102, LocalDate.of(2024, 1, 2));
        stockGraph.addNode(101, LocalDate.of(2024, 1, 3));
        stockGraph.addNode(103, LocalDate.of(2024, 1, 4));
        stockGraph.addNode(105, LocalDate.of(2024, 1, 5));
        stockGraph.addNode(104, LocalDate.of(2024, 1, 6));

        double predictedPrice = stockGraph.predictNextPrice();
        double accuracy = stockGraph.calculateAccuracy();

        // Create line chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Price");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Stock Price Prediction");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Stock Prices");

        for (int i = 0; i < stockGraph.getNodes().size(); i++) {
            StockNode node = stockGraph.getNodes().get(i);
            series.getData().add(new XYChart.Data<>(i, node.getPrice()));
        }
        series.getData().add(new XYChart.Data<>(stockGraph.getNodes().size(), predictedPrice));

        lineChart.getData().add(series);

        Label stats = new Label("Predicted Next Price: $" + String.format("%.2f", predictedPrice) +
                "\nModel Accuracy: " + String.format("%.2f", accuracy) + "%");

        VBox vbox = new VBox(lineChart, stats);
        Scene scene = new Scene(vbox, 800, 600);

        stage.setScene(scene);
        stage.setTitle("Stock Prediction Visualization");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
