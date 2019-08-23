package au.csiro.data61.aap.specification;

/**
 * StaticValue
 */
public class StaticValue implements ValueSource {
    private final String type;
    private final String value;    

    public StaticValue(String type, String value) {
        assert type != null && !type.trim().isEmpty();
        assert value != null && !value.trim().isEmpty();
        this.type = type;
        this.value = value;
    }

    @Override
    public String getReturnType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }
}