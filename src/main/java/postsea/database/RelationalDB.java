package postsea.database;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class FieldSchema implements Serializable {
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

class Table implements Serializable {
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

public class RelationalDB {
    private final String dbName;
    private final Map<String, Table> tables;
    private final Path dbPath;

    public RelationalDB(String dbName) {
        this.dbName = dbName;
        this.tables = new HashMap<>();
        this.dbPath = Paths.get(System.getProperty("user.dir"), dbName + ".dat");
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            if (Files.exists(dbPath)) {
                try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(dbPath.toFile()))) {
                    Map<String, Table> loadedTables = (Map<String, Table>) inputStream.readObject();
                    tables.putAll(loadedTables);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("Failed to initialize database", e);
                }
            } else {
                save();
            }
        });
    }

    private CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(() -> {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dbPath.toFile()))) {
                outputStream.writeObject(tables);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save database", e);
            }
        });
    }

    public CompletableFuture<Void> createTable(String tableName, Map<String, FieldSchema> schema) {
        return CompletableFuture.runAsync(() -> {
            if (!tables.containsKey(tableName)) {
                tables.put(tableName, new Table(schema));
                save();
            }
        });
    }

    public CompletableFuture<Map<String, Object>> insert(String tableName, Map<String, Object> record) {
        return CompletableFuture.supplyAsync(() -> {
            Table table = tables.get(tableName);
            if (table == null) {
                throw new RuntimeException("Table '" + tableName + "' not found");
            }

            // Validate schema
            for (Map.Entry<String, FieldSchema> entry : table.schema.entrySet()) {
                String field = entry.getKey();
                FieldSchema schema = entry.getValue();
                if (schema.required && !record.containsKey(field)) {
                    throw new RuntimeException("Required field " + field + " not found");
                }
                if (record.containsKey(field)) {
                    validateType(record.get(field), schema.type, field);
                }
            }

            Map<String, Object> newRecord = new HashMap<String, Object>(record);
            newRecord.put("id", table.autoIncrement++);
            table.data.add(newRecord);
            save();
            return newRecord;
        });
    }

    public List<Map<String, Object>> select(String tableName, Map<String, Object> conditions) {
        Table table = tables.get(tableName);
        if (table == null) {
            throw new RuntimeException("Table " + tableName + " not found");
        }

        return table.data.stream()
                .filter(record -> conditions.entrySet().stream()
                        .allMatch(entry -> Objects.equals(record.get(entry.getKey()), entry.getValue())))
                .collect(Collectors.toList());
    }

    public CompletableFuture<Map<String, Object>> update(String tableName, int id, Map<String, Object> updates) {
        return CompletableFuture.supplyAsync(() -> {
            Table table = tables.get(tableName);
            if (table == null) {
                throw new RuntimeException("Table " + tableName + " not found");
            }

            int index = findRecordIndex(table.data, id);
            if (index == -1) {
                throw new RuntimeException("Record not found for table " + tableName);
            }

            // Validate schema for updates
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                if (!table.schema.containsKey(field)) {
                    throw new RuntimeException("Invalid field " + field);
                }
                validateType(entry.getValue(), table.schema.get(field).type, field);
            }

            Map<String, Object> updatedRecord = new HashMap<>(table.data.get(index));
            updatedRecord.putAll(updates);
            table.data.set(index, updatedRecord);
            save();
            return updatedRecord;
        });
    }

    public CompletableFuture<Void> delete(String tableName, int id) {
        return CompletableFuture.runAsync(() -> {
            Table table = tables.get(tableName);
            if (table == null) {
                throw new RuntimeException("Table " + tableName + " does not exist");
            }

            int index = findRecordIndex(table.data, id);
            if (index == -1) {
                throw new RuntimeException("Record not found for table " + tableName);
            }

            table.data.remove(index);
            save();
        });
    }

    public List<Map<String, Object>> join(String table1Name, String table2Name, String foreignKey) {
        Table table1 = tables.get(table1Name);
        Table table2 = tables.get(table2Name);
        if (table1 == null || table2 == null) {
            throw new RuntimeException("Table '" + table1Name + "'' or '" + table2Name + "' does not exist");
        }

        return table1.data.stream()
                .map(record1 -> {
                    Map<String, Object> joined = new HashMap<>(record1);
                    joined.put(table2Name, table2.data.stream()
                            .filter(record2 -> Objects.equals(record2.get("id"), record1.get(foreignKey)))
                            .findFirst()
                            .orElse(null));
                    return joined;
                })
                .collect(Collectors.toList());
    }

    private int findRecordIndex(List<Map<String, Object>> data, int id) {
        for (int i = 0; i < data.size(); i++) {
            if (Objects.equals(data.get(i).get("id"), id)) {
                return i;
            }
        }
        return -1;
    }

    private void validateType(Object value, String expectedType, String field) {
        boolean valid = switch (expectedType) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            default -> false;
        };

        if (!valid) {
            throw new RuntimeException("Invalid type for " + field);
        }
    }

    // Helper method to print database state (for debugging)
    public void printState() {
        System.out.println("Database State:");
        tables.forEach((tableName, table) -> {
            System.out.println("Table: " + tableName);
            System.out.println("Schema: " + table.schema);
            System.out.println("Data: " + table.data);
        });
    }
}