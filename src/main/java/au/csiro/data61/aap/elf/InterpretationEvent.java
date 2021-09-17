package au.csiro.data61.aap.elf;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.v4.runtime.Token;

public class InterpretationEvent {
    public static enum Type {
        ERROR,
        WARNING,
        INFO;

        @Override
        public String toString() {
            switch (this) {
                case ERROR : return "error";
                case WARNING : return "warning";
                case INFO : return "info";
                default : throw new IllegalArgumentException("Unknown type!");
            }
        }
    }

    private final int line;
    private final int column;
    private final String message;
    private final Throwable cause;
    private final Type type;

    public InterpretationEvent(Type type, Token token, String errorMessage) {
        this(type, token, errorMessage, null);
    }

    public InterpretationEvent(Type type, Token token, String message, Throwable cause) {
        checkNotNull(type);
        checkNotNull(token);
        checkNotNull(message);
        checkArgument(!message.isBlank());
        this.type = type;
        this.line = token.getLine();
        this.column = token.getCharPositionInLine();
        this.message = message;
        this.cause = cause;
    }

    public InterpretationEvent(Type type, String message) {
        this(type, message, null);
    }

    public InterpretationEvent(Type type, String message, Throwable cause) {
        checkNotNull(type);
        checkNotNull(message);
        checkArgument(!message.isBlank());
        this.type = type;
        this.line = 0;
        this.column = 0;
        this.message = message;
        this.cause = cause;
    }

    public InterpretationEvent(Type type, int line, int column, String message, Throwable cause) {
        checkNotNull(type);
        checkArgument(0 <= line);
        checkArgument(0 <= column);
        checkNotNull(message);
        checkArgument(!message.isBlank());
        this.type = type;
        this.line = line;
        this.column = column;
        this.message = message;
        this.cause = cause;
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

    public String getMessage() {
        return this.message;
    }

    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String toString() {
        final String description = String.format("%s on Ln %s, Col %s: %s", this.type, this.line, this.column, this.message);
        final StringBuilder builder = new StringBuilder(description);

        Throwable currentCause = cause;
        while (currentCause != null) {
            this.appendLine(builder, "\t", cause.getMessage());
            currentCause = cause.getCause();
        }

        return builder.toString();
    }

    private void appendLine(StringBuilder builder, String prefix, String text) {
        builder.append(prefix);
        builder.append(text);
        builder.append(System.lineSeparator());
    }
}
