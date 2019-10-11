package au.csiro.data61.aap.specification.types;

import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;

public class BoolType extends SolidityType<Boolean> {
    private static final String NAME = "bool";
    private static final BoolType INSTANCE = new BoolType();

    public static final BoolType defaultInstance() {
        return INSTANCE;
    }

    private BoolType() {}

    @Override
    public MethodResult<Boolean> cast(Object obj) {
        if (obj == null) {
            return MethodResult.ofResult();
        }

        if (obj instanceof Boolean) {
            return MethodResult.ofResult((Boolean)obj);
        }

        if (obj instanceof String) {
            final String string = (String)obj;
            final Boolean value = Boolean.parseBoolean(string);
            return MethodResult.ofResult(value);
        }

       return this.castNotSupportedResult(obj);
    }

    @Override
    public boolean castSupportedFor(Class<?> cl) {
        assert cl != null;
        return cl != null && (cl.equals(Boolean.class) || cl.equals(String.class));
    }

    @Override
    public String getTypeName() {
        return NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj == this || obj instanceof BoolType);
    }

    static SolidityType<?> createBoolType(String keyword) {
        return keyword.equals(NAME) ? INSTANCE : null;
    }
}