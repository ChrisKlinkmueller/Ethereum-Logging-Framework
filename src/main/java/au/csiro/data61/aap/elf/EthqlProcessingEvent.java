package au.csiro.data61.aap.elf;

import org.antlr.v4.runtime.Token;

/**
 * SpecificationParserErrors
 */
public class EthqlProcessingEvent {
    private final int line;
    private final int column;
    private final String errorMessage;
    private final Throwable errorCause;
    private final Type type;

    public EthqlProcessingEvent(Type type, Token token, String errorMessage) {
        this(type, token, errorMessage, null);
    }

    public EthqlProcessingEvent(Type type, Token token, String errorMessage, Throwable cause) {
        assert token != null;
        assert errorMessage != null && !errorMessage.trim().isEmpty();
        assert type != null;
        this.line = token.getLine();
        this.column = token.getCharPositionInLine();
        this.errorMessage = errorMessage;
        this.errorCause = cause;
        this.type = type;
    }

    public EthqlProcessingEvent(Type type, int line, int column, String errorMessage) {
        this(type, line, column, errorMessage, null);
    }

    public EthqlProcessingEvent(Type type, int line, int column, String errorMessage, Throwable errorCause) {
        assert 0 <= line;
        assert 0 <= column;
        assert errorMessage != null && !errorMessage.trim().isEmpty();
        assert type != null;
        this.line = line;
        this.column = column;
        this.errorMessage = errorMessage;
        this.errorCause = errorCause;
        this.type = type;
    }

    public Type getType() {
        return this.type;
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

    @Override
    public String toString() {
        return String.format("%s on Ln %s, Col %s: %s", this.type, this.line, this.column, this.errorMessage);
    }

    public enum Type {
        ERROR,
        WARNING,
        INFO;

        @Override
        public String toString() {
            switch (this) {
                case ERROR : return "Error";
                case WARNING : return "Warning";
                case INFO : return "Info";
                default : {
                    assert false : "Unknown message type.";
                    throw new IllegalArgumentException("Unknown message type.");
                }
            }
        }
    }
}
