package au.csiro.data61.aap.specification.types;

import java.math.BigInteger;
import java.util.Objects;

import au.csiro.data61.aap.util.MethodResult;

public class IntegerType extends SolidityType<BigInteger> {
    private static final String BASE_NAME = "int";
    private static final int DEFAULT_LENGTH = 256;
    
    private final boolean signed;
    private final int bitLength;

    public static final IntegerType DEFAULT_INSTANCE = new IntegerType(true);

    public IntegerType(boolean signed) {
        this(signed, DEFAULT_LENGTH);
    }

    public IntegerType(boolean signed, int bitLength) {
        this.bitLength = bitLength;
        this.signed = signed;
    }

    public boolean isUnsigned() {
        return !this.signed;
    }

    public int getLength() {
        return this.bitLength;
    }

    @Override
    public MethodResult<BigInteger> cast(Object obj) {
        if (obj == null) {
            return MethodResult.ofResult();
        }

        if (obj instanceof Integer) {
            final BigInteger value = BigInteger.valueOf((int)obj);
            return MethodResult.ofResult(value);
        }

        if (obj instanceof Long) {
            final BigInteger value = BigInteger.valueOf((long)obj);
            return MethodResult.ofResult(value);
        }

        if (obj instanceof BigInteger) {
            return MethodResult.ofResult((BigInteger)obj);
        }

        if (obj instanceof String) {
            try {
                final BigInteger value = new BigInteger((String)obj);
                return MethodResult.ofResult(value);
            }
            catch (NumberFormatException ex) {
                final String errorMessage = String.format("'%s' is not a valid integer.", obj);
                return MethodResult.ofError(errorMessage, ex);
            }
        }

        return this.castNotSupportedResult(obj);
    }

    @Override
    public String getTypeName() {
        final String unsignedPrefix = this.signed ? "" : "u";
        return String.format("%s%s%s", unsignedPrefix, BASE_NAME, this.bitLength);
    }

    @Override
    public boolean castSupportedFor(Class<?> cl) {
        return cl != null && (cl.equals(Integer.class) || cl.equals(Long.class) || cl.equals(BigInteger.class) || cl.equals(String.class));
    }

    @Override
    public int hashCode() {
        return Objects.hash(BASE_NAME, this.signed, this.bitLength);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof IntegerType) {
            final IntegerType type = (IntegerType)obj;
            return type.signed == this.signed && type.bitLength == this.bitLength;
        }
        
        return false;
    }
    
}