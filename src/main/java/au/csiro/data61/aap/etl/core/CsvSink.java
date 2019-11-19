package au.csiro.data61.aap.etl.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import au.csiro.data61.aap.etl.core.DataSink;

/**
 * CSVExporter
 */
public class CsvSink extends DataSink {
    public static final String DEFAULT_DELIMITER = ",";

    private Map<String, ArrayList<Object>> table;
    private String delimiter;

    public CsvSink() {
        this.delimiter = DEFAULT_DELIMITER;
        this.table = new HashMap<>();
    }

    public void setDelimiter(String delimiter) {
        assert delimiter != null;
        this.delimiter = delimiter;
    }

    public void addRow(SinkVariable... variables) {
        assert this.validVariables(variables);
        final int rowCount = this.rowCount();
        Arrays.stream(variables)
            .filter(variable -> !this.table.containsKey(variable.getName()))
            .forEach(variable -> {
                ArrayList<Object> values = new ArrayList<Object>();
                IntStream.range(0, rowCount).forEach(i -> values.add(null));
                this.table.put(variable.getName(), values);
            })
        ;
        
        table.entrySet().stream()
            .forEach(entry -> {
                    final Object value = Arrays.stream(variables)
                        .filter(v -> v.getName().equals(entry.getKey()))
                        .map(v -> v.getValue())
                        .findFirst().orElse(null);
                    entry.getValue().add(value);
                }
            )
        ;
    }

    private int rowCount() {
        return this.table.values().stream()
            .mapToInt(values -> values.size())
            .max()
            .orElse(0);
    }

    @Override
    protected void writeState(String namePrefix) throws Throwable {
        final Path path = Paths.get(
            this.getOutputFolder().toString(), 
            String.format("%s.csv", namePrefix)
        );
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            final String header = this.table.keySet().stream()
                .collect(Collectors.joining(this.delimiter));
            writer.write(header);
            writer.newLine();

            for (int i = 0; i < this.rowCount(); i++) {
                final int index = i;
                final String row = this.table.keySet().stream()
                    .map(key -> this.table.get(key).get(index))
                    .map(obj -> obj == null ? "" : obj.toString())
                    .collect(Collectors.joining(this.delimiter))
                ;
                writer.write(row);
                writer.newLine();
            }
        }
        finally {
            this.table.clear();
        }
    }

    
}