package blf.core.writers;

import io.reactivex.annotations.NonNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LogWriter
 */
public class LogWriter extends DataWriter {
    private final List<String> lines;

    public LogWriter() {
        this.lines = new LinkedList<>();
    }

    public void addLogLine(@NonNull List<Object> itemParts) {

        this.lines.add(itemParts.stream().map(this::asString).collect(Collectors.joining()));
    }

    @Override
    protected void writeState(String filenameSuffix) throws Throwable {
        assert filenameSuffix != null;
        if (!this.lines.isEmpty()) {
            final Path outputPath = Paths.get(this.getOutputFolder().toString(), String.format("%s.log", filenameSuffix));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } finally {
                this.lines.clear();
            }
        }
    }
}
