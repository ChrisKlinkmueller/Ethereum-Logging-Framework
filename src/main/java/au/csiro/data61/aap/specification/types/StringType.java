package au.csiro.data61.aap.specification.types;

import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;

public class StringType extends BytesType {
    private static final String BASE_NAME = "string";
    
    public static final StringType DEFAULT_INSTANCE = new StringType();

    public StringType() {
        super(DYNAMIC_LENGTH);
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
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        return obj instanceof StringType;
    }
    
    @Override
    public boolean castSupportedFor(Class<?> cl) {
        return cl != null && cl.equals(String.class);
    }

    @Override
    public MethodResult<String> cast(Object obj) {
        if (obj == null) {
            return MethodResult.ofResult();
        }

        if (obj instanceof String) {
            final String string = (String)obj;
            if (string.length() < 2 || string.charAt(0) != '\"' || string.charAt(string.length() - 1) != '\"') {
                System.out.println("String cast error: " + obj);
                return MethodResult.ofError(String.format("'%' is not a valid string value.", string));
            }

            return MethodResult.ofResult(string.substring(1, string.length() - 1));
        }

        return this.castNotSupportedResult(obj);
    }
}