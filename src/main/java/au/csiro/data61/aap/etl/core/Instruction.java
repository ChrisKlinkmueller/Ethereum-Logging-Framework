package au.csiro.data61.aap.etl.core;

import java.util.List;
import java.util.Objects;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    public void execute(ProgramState state) throws EtlException;

    //#region MethodCall

    public static Instruction createMethodCall(List<ValueAccessor> parameterAccessors, Method method, ValueMutator resultStorer) {
        assert parameterAccessors != null && parameterAccessors.stream().allMatch(Objects::nonNull);
        assert method != null;
        return state -> callMethod(state, parameterAccessors, method, resultStorer);
    }

    private static Object callMethod(ProgramState state, List<ValueAccessor> parameterAccessors, Method method, ValueMutator resultStorer) throws EtlException {
        Object[] parameterValues = new Object[parameterAccessors.size()];
        for (int i = 0; i < parameterAccessors.size(); i++) {
            parameterValues[i] = parameterAccessors.get(i).getValue(state);
        }

        final Object result = method.call(parameterValues, state);
        
        if (resultStorer != null) {
            resultStorer.setValue(result, state);
        }

        return result;
    }

    //#endregion MethodCall
}