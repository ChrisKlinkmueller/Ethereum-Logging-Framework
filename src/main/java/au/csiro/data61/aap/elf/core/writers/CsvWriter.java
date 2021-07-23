package au.csiro.data61.aap.elf.core.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CSVExporter
 */
public class CsvWriter extends DataWriter {
    private static final Logger LOGGER = Logger.getLogger(CsvWriter.class.getName());
    public static final String DEFAULT_DELIMITER = ",";

    private final Map<String, Map<String, ArrayList<Object>>> tables;
    private final Map<String, List<String>> columnNames;
    private final Map<String, Integer> rowCounts;
    private String delimiter;

    public CsvWriter() {
        this.delimiter = DEFAULT_DELIMITER;
        this.tables = new HashMap<>();
        this.rowCounts = new HashMap<>();
        this.columnNames = new HashMap<>();
    }

    public void setDelimiter(String delimiter) {
        assert delimiter != null;
        this.delimiter = delimiter;
    }

    public void beginRow(String tableName) {
        LOGGER.info("Csv row added.");
        this.tables.putIfAbsent(tableName, new HashMap<>());
        this.rowCounts.putIfAbsent(tableName, 0);
        this.columnNames.putIfAbsent(tableName, new ArrayList<>());
    }

    public void addCell(String tableName, String attribute, Object value) {
        assert tableName != null && this.tables.containsKey(tableName);
        assert attribute != null;
        final ArrayList<Object> column = this.createNewColumnIfAbsent(tableName, attribute);
        column.add(value);

        final List<String> columnNames = this.columnNames.get(tableName);
        if (!columnNames.contains(attribute)) {
            columnNames.add(attribute);
        }
    }

    private ArrayList<Object> createNewColumnIfAbsent(String tableName, String attribute) {
        final ArrayList<Object> column = this.tables.get(tableName).get(attribute);
        if (column != null) {
            return column;
        }

        final ArrayList<Object> newColumn = new ArrayList<>();
        IntStream.range(0, this.rowCounts.get(tableName)).forEach(i -> newColumn.add(null));
        this.tables.get(tableName).put(attribute, newColumn);
        return newColumn;
    }

    public void endRow(String tableName) {
        assert tableName != null && this.tables.containsKey(tableName);
        final int rowCount = this.rowCounts.compute(tableName, (k, v) -> v + 1);
        this.tables.get(tableName).values().stream().filter(column -> column.size() != rowCount).forEach(column -> column.add(null));
    }

    @Override
    protected void writeState(String namePrefix) throws Throwable {
        List<String> tableNames = new LinkedList<>(this.tables.keySet());
        for (String tableName : tableNames) {
            this.writeTable(namePrefix, tableName);
        }
    }

    protected void writeTable(String filenameSuffix, String tableName) throws Throwable {
        LOGGER.info(String.format("Export of CSV table %s started.", tableName));
        final String filename = filenameSuffix == null
            ? String.format("%s.csv", tableName)
            : String.format("%s_%s.csv", tableName, filenameSuffix);
        final Path path = Paths.get(this.getOutputFolder().toString(), filename);

        final Map<String, ArrayList<Object>> table = this.tables.get(tableName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            final List<String> columns = this.columnNames.get(tableName);

            final String header = columns.stream().collect(Collectors.joining(this.delimiter));
            writer.write(header);
            writer.newLine();

            for (int i = 0; i < this.rowCounts.get(tableName); i++) {
                final int index = i;
                final String row = columns.stream()
                    .map(column -> table.get(column).get(index))
                    .map(value -> this.asString(value))
                    .collect(Collectors.joining(this.delimiter));
                writer.write(row);
                writer.newLine();
            }
        } finally {
            LOGGER.info(String.format("Export of CSV table %s finished.", tableName));
            this.tables.remove(tableName);
            this.rowCounts.remove(tableName);
            this.columnNames.remove(tableName);
        }
    }

}
