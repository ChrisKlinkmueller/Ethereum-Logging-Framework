package au.csiro.data61.aap.parser;

/**
 * SpecificationParserErrors
 */
public class SpecificationParserError {
    private final int line;
    private final int column;
    private final String errorMessage;
    private final Throwable errorCause;

    public SpecificationParserError(int line, int column, String errorMessage) {
        this(line, column, errorMessage, null);
    }

    public SpecificationParserError(int line, int column, String errorMessage, Throwable errorCause) {
        assert 0 <= line;
        assert 0 <= column;
        assert errorMessage != null && !errorMessage.trim().isEmpty();
        this.line = line;
        this.column = column;
        this.errorMessage = errorMessage;
        this.errorCause = errorCause;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public Throwable getErrorCause() {
        return this.errorCause;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}