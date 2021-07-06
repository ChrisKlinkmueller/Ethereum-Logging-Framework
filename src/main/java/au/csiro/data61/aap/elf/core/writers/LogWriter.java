package au.csiro.data61.aap.elf.core.writers;

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
        this.lines = new LinkedList<String>();
    }

    public void addLogLine(List<Object> itemParts) {
        assert itemParts != null;

        this.lines.add(itemParts.stream().map(obj -> this.asString(obj)).collect(Collectors.joining()));
    }

    @Override
    protected void writeState(String filenameSuffix) throws Throwable {
        assert filenameSuffix != null;
        if (!this.lines.isEmpty()) {
            final String filename = filenameSuffix == null 
                ? "log.txt"
                : String.format("log_%s.txt", filenameSuffix);
            final Path outputPath = Paths.get(this.getOutputFolder().toString(), filename);

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
