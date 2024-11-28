module postsea.investment.stock {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens postsea.investment.stock to javafx.fxml;
    exports postsea.investment.stock;
}