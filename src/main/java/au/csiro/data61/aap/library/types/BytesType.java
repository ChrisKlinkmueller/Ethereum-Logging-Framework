package au.csiro.data61.aap.library.types;

import au.csiro.data61.aap.util.MethodResult;

/**
 * BytesType
 */
public class BytesType extends SolidityType<String> {
    private final static String NAME = "bytes";
    private final static int DYNAMIC = Integer.MAX_VALUE;
    private final int length;

    public BytesType() {
        this.length = DYNAMIC;
    }

    public BytesType(int length) {
        assert 1 <= length && length <= 32;
        this.length = length;
    }

    public boolean isDynamic() {
        return this.length == DYNAMIC;
    }

    @Override
    public String getTypeName() {
        final String lengthSuffix = this.length == DYNAMIC ? "" : Integer.toString(this.length);
        return String.format("%S%s", NAME, lengthSuffix);
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
        final int prime = 73;
        int hash = 71;
        hash += prime * hash + NAME.hashCode();
        hash += prime * hash + Integer.hashCode(this.length);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof BytesType) {
            final BytesType type = (BytesType)obj;
            return type.length == this.length;
        }
        
        return false;
    }
}