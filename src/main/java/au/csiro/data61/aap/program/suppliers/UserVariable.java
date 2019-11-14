package au.csiro.data61.aap.program.suppliers;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * Variable
 */
public class UserVariable implements ValueConsumer {
    private final SolidityType type;
    private final String name;
    protected Object value;

    public UserVariable(UserVariable variable) {
        assert variable != null;
        this.type = variable.type;
        this.name = variable.name;
        this.value = variable.value;
    }

    public UserVariable(SolidityType type, String name) {
        this(type, name, null);
    }

    public UserVariable(SolidityType type, String name, Object value) {
        assert type != null;
        assert name != null;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasSameName(UserVariable variable) {
        return variable == null ? false : variable.name.equals(this.name);
    }

    
}