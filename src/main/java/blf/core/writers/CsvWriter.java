package blf.core.writers;

import blf.core.exceptions.ExceptionHandler;
import io.reactivex.annotations.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    public void setDelimiter(@NonNull String delimiter) {
        this.delimiter = delimiter;
    }

    public void beginRow(String tableName) {
        LOGGER.info("Csv row added.");
        this.tables.putIfAbsent(tableName, new HashMap<>());
        this.rowCounts.putIfAbsent(tableName, 0);
        this.columnNames.putIfAbsent(tableName, new ArrayList<>());
    }

    public void addCell(@NonNull String tableName, @NonNull String attribute, Object value) {
        final ArrayList<Object> column = this.createNewColumnIfAbsent(tableName, attribute);
        column.add(value);

        final List<String> colNames = this.columnNames.get(tableName);
        if (!colNames.contains(attribute)) {
            colNames.add(attribute);
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

    public void endRow(@NonNull String tableName) {
        final int rowCount = this.rowCounts.compute(tableName, (k, v) -> v + 1);
        this.tables.get(tableName).values().stream().filter(column -> column.size() != rowCount).forEach(column -> column.add(null));
    }

    @Override
    protected void writeState(String namePrefix) {
        if (this.tables.size() != 0) {
            LOGGER.info("Csv export started.");
            List<String> tableNames = new LinkedList<>(this.tables.keySet());
            for (String tableName : tableNames) {
                this.writeTable(namePrefix, tableName);
            }
            LOGGER.info("Csv export ended.");
        }
    }

    @Override
    protected void deleteState() {
        List<String> tableNames = new LinkedList<>(this.tables.keySet());
        for (String tableName : tableNames) {
            this.tables.remove(tableName);
            this.rowCounts.remove(tableName);
            this.columnNames.remove(tableName);
        }
    }

    protected void writeTable(String fileNameSuffix, String tableName) {
        final String fileNameSuffixNullErrorMsg = "The suffix of the CSV file is null.";
        final String tableNameNullErrorMsg = "The CSV table name is null.";

        final String exportStartInfoMsg = String.format("Export of CSV table %s started.", tableName);
        final String exportFinishInfoMsg = String.format("Export of CSV table %s finished.", tableName);
        final String exportErrorMsg = String.format("An error occurred while exporting of CSV table %s.", tableName);

        LOGGER.info(exportStartInfoMsg);

        if (fileNameSuffix == null) {
            ExceptionHandler.getInstance().handleException(fileNameSuffixNullErrorMsg, new NullPointerException());
        }

        if (tableName == null) {
            ExceptionHandler.getInstance().handleException(tableNameNullErrorMsg, new NullPointerException());
        }

        final Path path = Paths.get(this.getOutputFolder().toString(), String.format("%s_%s.csv", tableName, fileNameSuffix));

        final Map<String, ArrayList<Object>> table = this.tables.get(tableName);

        final File csvFile = path.toFile();

        try (
            final FileWriter csvFileWriter = new FileWriter(csvFile);
            final BufferedWriter csvBufferedWriter = new BufferedWriter(csvFileWriter)
        ) {

            final List<String> columns = this.columnNames.get(tableName);

            final String header = columns.stream().collect(Collectors.joining(this.delimiter));
            final int rowCount = this.rowCounts.get(tableName);

            csvBufferedWriter.write(header);
            csvBufferedWriter.newLine();

            for (int i = 0; i < rowCount; i++) {
                final int rowIndex = i;
                final String row = columns.stream()
                    .map(column -> table.get(column).get(rowIndex))
                    .map(this::asString)
                    .collect(Collectors.joining(this.delimiter));

                csvBufferedWriter.write(row);
                csvBufferedWriter.newLine();
            }

        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException(exportErrorMsg, e);
        }

        LOGGER.info(exportFinishInfoMsg);
    }

}
