package postsea.database;

import java.io.Serializable;

public class FieldSchema implements Serializable {
    private static final long serialVersionUID = 1L;
    String type;
    boolean required;

    public FieldSchema(String type, boolean required) {
        this.type = type;
        this.required = required;
    }

    @Override
    public String toString() {
        return "{\"type\":\"" + type + "\",\"required\":" + required + "}";
    }
}