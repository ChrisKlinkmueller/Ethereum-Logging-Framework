package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.filters.LogEntrySignature;

/**
 * LogEntrySignatureSpecification
 */
public class LogEntrySignatureSpecification {
    private final LogEntrySignature signature;

    private LogEntrySignatureSpecification(LogEntrySignature signature) {
        this.signature = signature;
    }

    public LogEntrySignature getSignature() {
        return this.signature;
    }

    public static LogEntrySignatureSpecification of(String eventName, LogEntryParameterSpecification... parameters) throws BuildException {
        return of(eventName, Arrays.asList(parameters));
    }

    public static LogEntrySignatureSpecification of(String eventName, List<LogEntryParameterSpecification> parameters) throws BuildException {
        assert eventName != null;
        assert parameters != null && parameters.stream().allMatch(Objects::nonNull);

        return new LogEntrySignatureSpecification(
            new LogEntrySignature(
                eventName, 
                parameters.stream().map(p -> p.getParameter()).collect(Collectors.toList())
            )
        );
    }
    
}