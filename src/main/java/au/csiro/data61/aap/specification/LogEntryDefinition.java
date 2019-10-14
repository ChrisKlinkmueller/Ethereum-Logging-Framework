package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * LogEntryDefinition
 */
public class LogEntryDefinition {
    private final String name;
    private final boolean isAnonymous;
    private final LogEntryParameter[] parameters;

    public LogEntryDefinition(LogEntryParameter... parameters) {
        this(null, true, parameters);
    }

    public LogEntryDefinition(String name, boolean isAnonymous, LogEntryParameter... parameters) {
        assert Arrays.stream(parameters).allMatch(p -> p != null);
        this.name = name;
        this.parameters = Arrays.copyOf(parameters, parameters.length);
        this.isAnonymous = isAnonymous;
    }

    public boolean isAnonymous() {
        return this.isAnonymous;
    }
    
    public String getName() {
        return this.name;
    }

    public int parameterCount() {
        return this.parameters.length;
    }

    public LogEntryParameter getParameter(int index) {
        assert 0 <= index && index < this.parameters.length;
        return this.parameters[index];
    }

    public Stream<LogEntryParameter> parametersStream() {
        return Arrays.stream(this.parameters);
    }

    public boolean hasVarargs() {
        return Arrays.stream(this.parameters).anyMatch(p -> p.isSkip());
    }
}