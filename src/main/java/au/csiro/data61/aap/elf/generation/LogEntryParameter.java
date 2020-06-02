package au.csiro.data61.aap.elf.generation;

/**
 * LogEntryParameter
 */
public class LogEntryParameter {
    private final String name;
    private final String type;
    private final boolean indexed;

    public LogEntryParameter(String type, String name) {
        this(type, name, false);
    }

    public LogEntryParameter(String type, String name, boolean indexed) {
        assert name != null;
        assert type != null;
        this.name = name;
        this.type = type;
        this.indexed = indexed;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public boolean isIndexed() {
        return this.indexed;
    }

}
