package au.csiro.data61.aap.elf.core.values;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * ValueGetter
 */
public abstract class ValueAccessor {
    private Type type;

    protected ValueAccessor(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public abstract Object getValue(ProgramState state) throws ProgramException;

    public static ValueAccessor createLiteralAccessor(Object value) {
        return new LiteralAccessor(value);
    }

    public static ValueAccessor createVariableAccessor(String name) {
        return new VariableAccessor(name);
    }

    public static ValueAccessor createFunctionAccessor(ProgramFunction function) {
        return new FunctionAccessor(function);
    }

    public enum Type {
        LITERAL,
        VARIABLE,
        FUNCTION
    }

    private static class LiteralAccessor extends ValueAccessor {
        private final Object value;

        private LiteralAccessor(Object value) {
            super(Type.LITERAL);
            this.value = value;
        }

        @Override
        public Object getValue(ProgramState state) throws ProgramException {
            return this.value;
        }
    }

    private static class VariableAccessor extends ValueAccessor {
        private final String variableName;

        private VariableAccessor(String name) {
            super(Type.VARIABLE);
            this.variableName = name;
        }

        @Override
        public Object getValue(ProgramState state) throws ProgramException {
            return state.getValueStore().getValue(this.variableName);
        }
    }

    private static class FunctionAccessor extends ValueAccessor {
        private final ProgramFunction function;

        public FunctionAccessor(ProgramFunction function) {
            super(Type.FUNCTION);
            this.function = function;
        }

        @Override
        public Object getValue(ProgramState state) throws ProgramException {
            return this.function.getValue(state);
        }
    }

    public static interface ProgramFunction {
        Object getValue(ProgramState state) throws ProgramException;
    }
}
