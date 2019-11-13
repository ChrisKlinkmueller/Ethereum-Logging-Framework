package au.csiro.data61.aap.program;

import java.util.Optional;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.suppliers.MethodCall;
import au.csiro.data61.aap.program.suppliers.ValueSupplier;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Statement
 */
public class Statement extends Instruction {
    private Optional<Variable> variable;
    private ValueSupplier source;

    public Statement(ValueSupplier source) {
        this(null, source);
    }

    public Statement(Variable variable, ValueSupplier source) {
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

    public ValueSupplier getSource() {
        return this.source;
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        if (this.source instanceof MethodCall) {
            MethodResult<Void> callResult = ((MethodCall) this.source).execute(state);
            if (!callResult.isSuccessful()) {
                return callResult;
            }
        }

        try {
            if (this.variable.isPresent()) {
                final Object value = this.source.getValue();
                this.variable.get().setValue(value);
            }
        }
        catch (Throwable cause) {
            final String message = String.format("Error getting a value.");
            state.reportException(message, cause);
        }

        return MethodResult.ofResult();
    }

    @Override
    public Stream<Variable> variableStream() {
        return this.variable.isPresent()
            ? Stream.of(this.variable.get())
            : Stream.empty();
    }
}