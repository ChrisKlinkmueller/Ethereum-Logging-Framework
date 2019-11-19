package au.csiro.data61.aap.etl.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.core.DataSink;

/**
 * LineExporter
 */
public class TextSink extends DataSink {
    private final List<String> lines;

    public TextSink() {
        this.lines = new LinkedList<String>();
    }

    public void addLine(SinkVariable... variables) {
        assert this.validVariables(variables);
        this.lines.add(
            Arrays.stream(variables)
                .map(variable -> variable.getValue() == null ? "" : variable.getValue().toString())
                .collect(Collectors.joining())
        );
    }

    @Override
    protected boolean validVariables(SinkVariable... variables) {
        return Arrays.stream(variables).allMatch(Objects::nonNull);
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