package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.library.types.SolidityType;

/**
 * StaticValue
 */
public class StaticValue implements ValueSource {
    private final SolidityType<?> type;
    private final String value;    

    public StaticValue(SolidityType<?> type, String value) {
        assert type != null;
        assert value != null && !value.trim().isEmpty();
        this.type = type;
        this.value = value;
    }

    @Override
    public SolidityType<?> getReturnType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }
}