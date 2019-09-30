package au.csiro.data61.aap.specification;

/**
 * VariableDefinitionStatement
 */
public class Statement extends Instruction {
    private final Variable variable;
    private final ValueSource valueSource;

    private Statement(Scope parentScope, Variable variable, ValueSource valueSource) {
        super(parentScope);
        this.variable = variable;
        this.valueSource = valueSource;
    }

    public Variable getVariable() {
        return this.variable;
    }

    public ValueSource getValueSource() {
        return this.valueSource;
    }

    public static Statement createMethodCall(Scope parentScope, MethodCall call) {
        assert call != null;
        return new Statement(parentScope, null, call);
    }

    public static Statement createVariableAssignment(Scope parentScope, Variable variable, ValueSource valueSource) {
        assert variable != null;
        assert valueSource != null;
        return new Statement(parentScope, variable, valueSource);
    }
}