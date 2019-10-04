package au.csiro.data61.aap.library.types;

import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;
import au.csiro.data61.aap.util.StringUtil;

/**
 * BytesType
 */
public class BytesType extends SolidityType<String> {
    private final static String PREFIX = "byte";
    private final static String DYNAMIC_SUFFIX = "s";
    private final static int DYNAMIC = Integer.MAX_VALUE;
    private final static int MIN_STATIC_LENGTH = 1;
    private final static int MAX_STATIC_LENGTH = 32;
    private final static int DEFAULT_LENGTH = 1;
    
    private final int length;
    BytesType() {
        this.length = DYNAMIC;
    }

    BytesType(int length) {
        this.length = length;
    }

    public boolean isDynamic() {
        return this.length == DYNAMIC;
    }

    public int getBytesLength() {
        return this.length;
    }

    @Override
    public String getTypeName() {
        final String lengthSuffix = this.length == DYNAMIC ? "" : Integer.toString(this.length);
        return String.format("%S%s", PREFIX, lengthSuffix);
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
        return Objects.hash(PREFIX, this.length);
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


    static SolidityType<?> createBytesType(String keyword) {
        if (!keyword.startsWith(PREFIX)) {
            return null;
        }

        keyword = keyword.replaceFirst(PREFIX, "");
        if (keyword.isEmpty()) {
            return new BytesType(DEFAULT_LENGTH);
        }
        
        if (!keyword.startsWith(DYNAMIC_SUFFIX)) {
            return null;
        }

        keyword = keyword.replaceFirst(DYNAMIC_SUFFIX, "");

        if (keyword.isEmpty()) {
            return new BytesType();
        }

        final MethodResult<Integer> valueResult = StringUtil.parseInt(keyword);
        if (!valueResult.isSuccessful() || valueResult.getResult() < MIN_STATIC_LENGTH || MAX_STATIC_LENGTH < valueResult.getResult()) {
            return null;
        }              
        return new BytesType(valueResult.getResult());
    }
}