package au.csiro.data61.aap.etl.library;

import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.EtlException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.etl.core.ValueMutator;

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

    public void execute(ProgramState state) throws EtlException {
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