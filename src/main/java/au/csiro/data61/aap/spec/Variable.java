package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * Variable
 */
public class Variable implements ValueSource {
    private final SolidityType type;
    private final String name;
    private final boolean isConstant;
    protected Object value;

    public Variable(SolidityType type, String name) {
        this(type, name, false, null);
    }

    public Variable(SolidityType type, String name, boolean isConstant, Object value) {
        assert type != null;
        assert name != null;
        assert isConstant ? value != null : true;
        this.type = type;
        this.name = name;
        this.isConstant = isConstant;
        this.value = value;
    }

    public boolean isConstant() {
        return isConstant;
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    public void setValue(Object object) {
        if (this.isConstant) {
            throw new UnsupportedOperationException("The value of a constant cannot be updated!");
        }
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
}