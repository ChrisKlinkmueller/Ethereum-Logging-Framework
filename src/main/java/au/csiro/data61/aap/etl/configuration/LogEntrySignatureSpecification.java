package au.csiro.data61.aap.etl.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.filters.LogEntryParameter;
import au.csiro.data61.aap.etl.core.filters.LogEntrySignature;

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

    public static LogEntrySignatureSpecification of(String eventName, String[] parameterTypes, String[] parameterNames, boolean[] indexed) throws BuildException {
        assert eventName != null;
        assert parameterTypes != null && Arrays.stream(parameterTypes).allMatch(Objects::nonNull);
        assert parameterNames != null && Arrays.stream(parameterNames).allMatch(Objects::nonNull);
        assert indexed != null;
        assert parameterTypes.length == parameterNames.length && parameterTypes.length == indexed.length;

        try {
            ArrayList<LogEntryParameter> parameters = new ArrayList<>();
            for (int i = 0; i < parameterNames.length; i++) {
                parameters.add(
                    new LogEntryParameter(
                        parameterNames[i], 
                        TypeReference.makeTypeReference(parameterTypes[i], indexed[i], false)
                    )
                );
            }
            return new LogEntrySignatureSpecification(new LogEntrySignature(eventName, parameters));
        }
        catch (Throwable ex) {
            throw new BuildException("Error when creating the log entry signature.", ex);
        }
    }
    
}