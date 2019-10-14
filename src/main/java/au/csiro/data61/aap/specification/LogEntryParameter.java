package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.specification.types.SolidityType;

/**
 * LogEntryParameter
 */
public class LogEntryParameter {
    private final String name;
    private final SolidityType<?> type;
    private final boolean isIndexed;

    private LogEntryParameter(SolidityType<?> type, String name, boolean isIndexed) {
        this.name = name;
        this.type = type;
        this.isIndexed = isIndexed;
    }

    public String getName() {
        return this.name;
    }

    public SolidityType<?> getType() {
        return this.type;
    }

    public boolean isIndexed() {
        return this.isIndexed;
    }

    public boolean isSkip() {
        return this == SKIP_DATA_PARAMETER || this == SKIP_INDEXED_PARAMETER || this == VAR_END_PARAMETER;
    }

    public boolean isVarEnd() {
        return this == VAR_END_PARAMETER;
    }

    public static LogEntryParameter of(SolidityType<?> type, String name, boolean isIndexed) {
        assert type != null;
        assert name != null;
        return new LogEntryParameter(type, name, isIndexed);
    }

    private static final LogEntryParameter SKIP_DATA_PARAMETER = new LogEntryParameter(null, "skip data", false);
    public static LogEntryParameter skipDataParameter() {
        return SKIP_DATA_PARAMETER;
    }

    private static final LogEntryParameter SKIP_INDEXED_PARAMETER = new LogEntryParameter(null, "skip indexed", true);
    public static LogEntryParameter skipIndexedParameter() {
        return SKIP_INDEXED_PARAMETER;
    }

    private static final LogEntryParameter VAR_END_PARAMETER = new LogEntryParameter(null, "var end", false);
    public static LogEntryParameter varEndParameter() {
        return VAR_END_PARAMETER;
    }
}