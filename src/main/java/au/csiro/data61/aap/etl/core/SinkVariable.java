package au.csiro.data61.aap.etl.core;

/**
 * SinkVariable
 */
public class SinkVariable {
    private final String name;
    private final Object value;
    
    public SinkVariable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }
}