package au.csiro.data61.aap.specification.types;

import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;

/**
 * BytesType
 */
public class BytesType extends SolidityType<String> {
    private final static String BASE_NAME = "bytes";    
    protected final static int DYNAMIC_LENGTH = Integer.MAX_VALUE;
    public final static int MIN_STATIC_LENGTH = 1;
    public final static int MAX_STATIC_LENGTH = 32;

    public static final BytesType DEFAULT_INSTANCE = new BytesType();

    private final int length;
    public BytesType(int length) {
        this.length = length;
    }

    public BytesType() {
        this.length = DYNAMIC_LENGTH;
    }

    public boolean isDynamic() {
        return this.length == DYNAMIC_LENGTH;
    }

    public int getBytesLength() {
        return this.length;
    }

    @Override
    public String getTypeName() {
        final String lengthSuffix = this.length == DYNAMIC_LENGTH ? "" : Integer.toString(this.length);
        return String.format("%s%s", BASE_NAME, lengthSuffix);
    }

    @Override
    public MethodResult<String> cast(Object obj) {
        if (obj == null) {
            return MethodResult.ofResult();
        }

        if (obj instanceof String) {
            final String string = (String)obj;
            if (this.isDynamic() ? true : this.length == string.length()) {
                return MethodResult.ofResult(string);
            }
            else {
                final String errorMessage = String.format("Length of string '%s' is not %s.", string, this.length);
                return MethodResult.ofError(errorMessage);
            }
        }

        return this.castNotSupportedResult(obj);
    }

    @Override
    public boolean castSupportedFor(Class<?> cl) {
        return cl != null && cl.equals(String.class);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BASE_NAME, this.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof BytesType) {
            final BytesType type = (BytesType)obj;
            return type.length == this.length;
        }
        
        return false;
    }
}