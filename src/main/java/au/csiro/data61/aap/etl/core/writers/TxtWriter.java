package au.csiro.data61.aap.etl.core.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * LineExporter
 */
public class TxtWriter extends DataWriter {
    private final List<String> lines;

    public TxtWriter() {
        this.lines = new LinkedList<String>();
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