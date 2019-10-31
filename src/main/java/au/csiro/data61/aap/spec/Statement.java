package au.csiro.data61.aap.spec;

import java.util.Optional;

import au.csiro.data61.aap.state.ProgramState;

/**
 * Statement
 */
public class Statement extends Instruction {
    private Optional<Variable> variable;
    private ValueSource source;

    public Statement(ValueSource source) {
        this(null, source);
    }

    public Statement(Variable variable, ValueSource source) {
        this.variable = variable == null ? Optional.empty() : Optional.of(variable);
        this.source = source;
    } 

    @Override
    public void setEnclosingScope(Scope enclosingScope) {
        assert enclosingScope != null;
        super.setEnclosingScope(enclosingScope);
    }

    public Optional<Variable> getVariable() {
        return this.variable;
    }

    public ValueSource getSource() {
        return this.source;
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException();
    }
}