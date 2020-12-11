package blf.core;

import java.util.List;
import java.util.Objects;

import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;

/**
 * MethodCall
 */
public class MethodCall implements Instruction {
    private final List<ValueAccessor> parameterAccessors;
    private final Method method;
    private final ValueMutator resultStorer;

    public MethodCall(Method method, List<ValueAccessor> parameterAccessors, ValueMutator resultStorer) {
        assert parameterAccessors != null && parameterAccessors.stream().allMatch(Objects::nonNull);
        assert method != null;
        this.parameterAccessors = parameterAccessors;
        this.method = method;
        this.resultStorer = resultStorer;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
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
