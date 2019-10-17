package au.csiro.data61.aap.specification.types;

import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;

public class BoolType extends SolidityType<Boolean> {
    private static final String BASE_NAME = "bool";
    
    public static final BoolType DEFAULT_INSTANCE = new BoolType();

    public BoolType() {}

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
        return BASE_NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(BASE_NAME);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj == this || obj instanceof BoolType);
    }
}