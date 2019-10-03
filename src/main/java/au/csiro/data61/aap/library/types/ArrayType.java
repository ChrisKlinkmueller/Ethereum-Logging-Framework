package au.csiro.data61.aap.library.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;

/**
 * ArrayType
 */
public class ArrayType<T> extends SolidityType<List<T>> {
    private final SolidityType<T> baseType;
    private static final String REGEX = "[a-zA-Z0-9]*\\[\\]"; 
    private static final String SUFFIX = "[]";

    ArrayType(SolidityType<T> baseType) {
        assert baseType != null;
        this.baseType = baseType;
    }

    @Override
    public String getTypeName() {
        return String.format("%s[]", this.baseType.getTypeName());
    }

    @Override
    public MethodResult<List<T>> cast(Object obj) {
        if (obj == null) {
            return MethodResult.ofResult();
        }

        if (obj.getClass().isArray()) {
            final ArrayList<T> values = new ArrayList<>();
            final Object[] array = (Object[])obj;
            for (Object arrObj : array) {
                final MethodResult<T> castResult = this.baseType.cast(arrObj);
                if (castResult.isSuccessful()) {
                    values.add(castResult.getResult());
                }
                else {
                    final String errorMessage = String.format("Not all elements of the array can be cast as '%s'", this.baseType.getTypeName());
                    return MethodResult.ofError(errorMessage);
                }
            }

            return MethodResult.ofResult(values);
        }

        return this.castNotSupportedResult(obj);
    }

    @Override
    public boolean castSupportedFor(Class<?> cl) {
        return cl != null && cl.isArray() && this.baseType.castSupportedFor(cl.getComponentType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.baseType, SUFFIX);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof ArrayType) {
            final ArrayType<?> arrayType = (ArrayType<?>)obj;
            return this.baseType.equals(arrayType.baseType);
        }

        return false;
    }

    static SolidityType<?> createArrayType(String keyword) {
        if (!keyword.matches(REGEX)) {
            return null;
        }

        final String baseKeyword = keyword.substring(0, keyword.length() - SUFFIX.length());
        final SolidityType<?> baseType = SolidityType.createType(baseKeyword);
        return baseType == null ? null : new ArrayType<>(baseType);
    }
}