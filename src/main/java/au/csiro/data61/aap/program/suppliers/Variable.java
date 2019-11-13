package au.csiro.data61.aap.program.suppliers;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * Variable
 */
public class Variable implements ValueSupplier {
    private final SolidityType type;
    private final String name;
    private final VariableCategory category;
    protected Object value;

    public Variable(Variable variable) {
        assert variable != null;
        this.type = variable.type;
        this.name = variable.name;
        this.category = variable.category;
        this.value = variable.value;
    }

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
    public Object getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasSameName(Variable variable) {
        return variable == null ? false : variable.name.equals(this.name);
    }

    
}