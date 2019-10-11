package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.specification.types.SolidityType;

/**
 * ValueContainer
 */
public abstract class ValueContainer implements ValueSource {
    private final SolidityType<?> type;
    private final String name;
    protected Object value;

    public ValueContainer(SolidityType<?> type, String name) {
        assert type != null;
        assert name != null;
        this.type = type;
        this.name = name;
    }

    @Override
    public SolidityType<?> getType() {
        return this.type;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
}