package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Variable
 */
public class Variable implements ValueSource {
    private final SolidityType type;
    private final String name;
    private final boolean isScopeVariable;
    protected Object value;

    public Variable(SolidityType type, String name) {
        this(type, name, false, null);
    }

    public Variable(SolidityType type, String name, boolean isScopeVariable, Object value) {
        assert type != null;
        assert name != null;
        assert isScopeVariable ? value != null : true;
        this.type = type;
        this.name = name;
        this.isScopeVariable = isScopeVariable;
        this.value = value;
    }

    public boolean isScopeVariable() {
        return isScopeVariable;
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    public void setValue(Object object) {
        if (this.isScopeVariable) {
            throw new UnsupportedOperationException("The value of a constant cannot be updated!");
        }
    }

    @Override
    public MethodResult<Object> getValue() {
        return MethodResult.ofResult(this.value);
    }

    public String getName() {
        return this.name;
    }

    
}