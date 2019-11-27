package au.csiro.data61.aap.elf.core.filters;

import org.web3j.abi.TypeReference;

/**
 * LogEntryParameter
 */
public class LogEntryParameter {
    private final TypeReference<?> type;
    private final String name;
    
    public LogEntryParameter(String name, TypeReference<?> type) {
        assert name != null;
        assert type != null;
        this.name = name;
        this.type = type;
    }

    public boolean isIndexed() {
        return this.type.isIndexed();
    }

    public String getName() {
        return this.name;
    }

    public TypeReference<?> getType() {
        return this.type;
    }
}