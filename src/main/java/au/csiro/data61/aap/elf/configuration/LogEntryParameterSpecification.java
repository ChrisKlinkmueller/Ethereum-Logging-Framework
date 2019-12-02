package au.csiro.data61.aap.elf.configuration;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.elf.core.filters.LogEntryParameter;

/**
 * LogEntryParameterSpecification
 */
public class LogEntryParameterSpecification {
    private final LogEntryParameter parameter;
    
    private LogEntryParameterSpecification(LogEntryParameter parameter) {
        this.parameter = parameter;
    }

    public LogEntryParameter getParameter() {
        return this.parameter;
    }

    public static LogEntryParameterSpecification of(String paramName, String paramType, boolean isIndexed) throws BuildException {
        try {
            return new LogEntryParameterSpecification(
                new LogEntryParameter(
                    paramName, TypeReference.makeTypeReference(paramType, isIndexed, false)
                )
            );
        }
        catch (Throwable ex) {
            throw new BuildException("Error when creating the log parameter.", ex);
        }
    }
    
}