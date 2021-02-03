package blf.core.instructions;

import blf.core.interfaces.Method;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;
import io.reactivex.annotations.NonNull;

import java.util.List;

/**
 * MethodCall
 */
public class MethodCallInstruction extends Instruction {
    private final List<ValueAccessor> parameterAccessors;
    private final Method method;
    private final ValueMutator resultStorer;

    public MethodCallInstruction(@NonNull Method method, @NonNull List<ValueAccessor> parameterAccessors, ValueMutator resultStorer) {
        this.parameterAccessors = parameterAccessors;
        this.method = method;
        this.resultStorer = resultStorer;
    }

    @Override
    public void execute(ProgramState state) {
        Object[] parameterValues = new Object[parameterAccessors.size()];

        for (int i = 0; i < parameterAccessors.size(); i++) {
            parameterValues[i] = parameterAccessors.get(i).getValue(state);
        }

        final Object result = method.call(parameterValues, state);
        if (resultStorer != null) {
            resultStorer.setValue(result, state);
        }
    }

}
