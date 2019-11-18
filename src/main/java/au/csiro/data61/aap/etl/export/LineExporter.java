package au.csiro.data61.aap.etl.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LineExporter
 */
public class LineExporter extends Exporter {
    private final List<String> lines;

    public LineExporter() {
        this.lines = new LinkedList<String>();
    }

    public void addLine(String... parts) {
        this.lines.add(Arrays.stream(parts).collect(Collectors.joining()));
    }

    @Override
    protected void writeState(String namePrefix) throws Throwable {
        assert namePrefix != null;
        if (!this.lines.isEmpty()) {
            final Path outputPath = Paths.get(this.getOutputFolder().toString(), String.format("%s.log", namePrefix));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            finally {            
                this.lines.clear();
            }
        }
    }
}