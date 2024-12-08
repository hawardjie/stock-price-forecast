package postsea.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table implements Serializable {
    private static final long serialVersionUID = 2L;
    Map<String, FieldSchema> schema;
    List<Map<String, Object>> data;
    int autoIncrement;

    public Table(Map<String, FieldSchema> schema) {
        this.schema = schema;
        this.data = new ArrayList<>();
        this.autoIncrement = 1;
    }
}