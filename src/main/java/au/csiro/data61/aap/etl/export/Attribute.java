package au.csiro.data61.aap.etl.export;

/**
 * Attribute
 */
public class Attribute {
    private String name;
    private Object value;

    public Attribute(String name, Object value) {
        assert name != null;
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