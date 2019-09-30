package au.csiro.data61.aap.library.types;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.util.MethodResult;

public class IntegerType extends SolidityType<BigInteger> {
    private static final Logger LOG = Logger.getLogger(IntegerType.class.getName());
    private static final String NAME = "int";

    private final boolean signed;
    private final int bitLength;
    
    public IntegerType(boolean signed, int bitLength) {
        this.bitLength = bitLength;
        this.signed = signed;
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
        final int prime = 23;
        int hash = 19;
        hash += prime * hash + NAME.hashCode();
        hash += prime * hash + Boolean.hashCode(this.signed);
        hash += prime * hash + Integer.hashCode(this.bitLength);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FixedType) {
            final IntegerType type = (IntegerType)obj;
            return type.signed == this.signed && type.bitLength == this.bitLength;
        }
        
        return false;
    }
    
}