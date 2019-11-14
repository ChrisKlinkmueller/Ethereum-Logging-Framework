package au.csiro.data61.aap.program.suppliers;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * UserVariableReference
 */
public class UserVariableReference implements ValueConsumer {
    private final UserVariable variable;

    public UserVariableReference(UserVariable variable) {
        assert variable != null;
        this.variable = variable;
    }

    @Override
    public Object getValue() throws Throwable {
        return this.variable.getValue();
    }

    @Override
    public void setValue(Object value) {
        this.variable.setValue(value);
    }

    @Override
    public SolidityType getType() {
        return this.variable.getType();
    }

    @Override
    public String getName() {
        return this.variable.getName();
    }

    
}