package au.csiro.data61.aap.program;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Variable
 */
public class Variable implements ValueSource {
    private final SolidityType type;
    private final String name;
    private final VariableCategory category;
    protected Object value;

    public Variable(SolidityType type, String name) {
        this(type, name, VariableCategory.SCOPE_VARIABLE, null);
    }

    public Variable(SolidityType type, String name, VariableCategory category, Object value) {
        assert type != null;
        assert name != null;
        assert category != null;
        assert category == VariableCategory.LITERAL ? value != null : true;
        this.type = type;
        this.name = name;
        this.category = category;
        this.value = value;
    }

    public VariableCategory getCategory() {
        return this.category;
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    public void setValue(Object object) {
        if (this.category != VariableCategory.USER_DEFINED) {
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

    public boolean hasSameName(Variable variable) {
        return variable == null ? false : variable.name.equals(this.name);
    }

    
}