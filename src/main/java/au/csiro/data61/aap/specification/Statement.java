package au.csiro.data61.aap.specification;

/**
 * VariableDefinitionStatement
 */
public class Statement extends AbstractInstruction {
    private final Variable variable;
    private final ValueSource valueSource;

    private Statement(Variable variable, ValueSource valueSource) {
        this.variable = variable;
        this.valueSource = valueSource;
    }

    public Variable getVariable() {
        return this.variable;
    }

    public ValueSource getValueSource() {
        return this.valueSource;
    }

    public static Statement createMethodCall(MethodCall call) {
        assert call != null;
        return new Statement(null, call);
    }

    public static Statement createVariableAssignment(Variable variable, ValueSource valueSource) {
        assert variable != null;
        assert valueSource != null;
        return new Statement(variable, valueSource);
    }
}