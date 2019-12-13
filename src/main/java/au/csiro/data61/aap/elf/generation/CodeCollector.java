package au.csiro.data61.aap.elf.generation;

/**
 * CodeCollector
 */
public class CodeCollector {
    private final StringBuilder builder;

    public CodeCollector() {
        this.builder = new StringBuilder();
    }

    String getCode() {
        return this.builder.toString();
    }

    protected final void addCodeLine(String line) {
        assert line != null;
        this.appendLine(line);
    }

    protected final void addCommentLine(String line) {
        assert line != null;
        this.appendLine(String.format("// %s", line));
    }

    protected final void addEmptyLine() {
        this.builder.append(System.lineSeparator());
    }

    private void appendLine(String line) {
        this.builder.append(line);
        this.builder.append(System.lineSeparator());
    }

    public void clean() {
        this.builder.delete(0, this.builder.length());
    }
}