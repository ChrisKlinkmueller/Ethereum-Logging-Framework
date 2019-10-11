package au.csiro.data61.aap.specification.types;

import java.math.BigInteger;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.util.MethodResult;
import au.csiro.data61.aap.util.StringUtil;

public class IntegerType extends SolidityType<BigInteger> {
    private static final Logger LOG = Logger.getLogger(IntegerType.class.getName());
    private static final String NAME = "int";
    private static final String UNSIGNED_PREFIX = "u";
    private static final int DEFAULT_LENGTH = 256;

    private final boolean signed;
    private final int bitLength;

    private static final IntegerType defaultInstance = new IntegerType(true, DEFAULT_LENGTH);
    public static IntegerType getDefaultInstance() {
        return defaultInstance;
    }
    
    IntegerType(boolean signed, int bitLength) {
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
                LOG.log(Level.SEVERE, errorMessage, ex);
                return MethodResult.ofError(errorMessage, ex);
            }
        }

        return this.castNotSupportedResult(obj);
    }

    @Override
    public String getTypeName() {
        final String unsignedPrefix = this.signed ? "" : "u";
        return String.format("%s%s%s", unsignedPrefix, NAME, this.bitLength);
    }

    @Override
    public boolean castSupportedFor(Class<?> cl) {
        return cl != null && (cl.equals(Integer.class) || cl.equals(Long.class) || cl.equals(BigInteger.class) || cl.equals(String.class));
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, this.signed, this.bitLength);
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

    static SolidityType<?> createIntegerType(String keyword) {
        final boolean unsigned = keyword.startsWith(UNSIGNED_PREFIX);
        if (unsigned) {
            keyword = keyword.replaceFirst(UNSIGNED_PREFIX, "");
        }

        if (!keyword.startsWith(NAME)) {
            return null;
        }

        keyword = keyword.replaceFirst(NAME, "");
        if (keyword.isEmpty()) {
            return new IntegerType(!unsigned, DEFAULT_LENGTH);
        }

        final MethodResult<Integer> lengthResult = StringUtil.parseInt(keyword);
        return lengthResult.isSuccessful() ? new IntegerType(!unsigned, lengthResult.getResult()) : null;
    }
    
}