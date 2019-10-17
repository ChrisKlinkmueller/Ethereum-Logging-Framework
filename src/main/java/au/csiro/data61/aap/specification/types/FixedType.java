package au.csiro.data61.aap.specification.types;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.util.MethodResult;

public class FixedType extends SolidityType<BigDecimal> {
    private static final Logger LOG = Logger.getLogger(FixedType.class.getName());
    private static final String BASE_NAME = "fixed";
    public static final int MIN_M = 8;
    public static final int MAX_M = 256;
    public static final int MIN_N = 0;
    public static final int MAX_N = 80;
    public static final int FIXED_DEFAULT_M = 128;
    public static final int FIXED_DEFAULT_N = 18;
    
    private final int m;
    private final int n;
    private final boolean signed;

    public static final FixedType DEFAULT_INSTANCE = new FixedType(true);

    public FixedType(boolean signed) {
        this(signed, FIXED_DEFAULT_M, FIXED_DEFAULT_N);
    }

    public FixedType(boolean signed, int m, int n) {
        assert isValidMValue(m);
        assert isValidNValue(n);
        this.signed = signed;
        this.m = m;
        this.n = n;
    }

    public boolean isUnsigned() {
        return !this.signed;
    }

    public int getM() {
        return this.m;
    }

    public int getN() {
        return this.n;
    }

    @Override
    public MethodResult<BigDecimal> cast(Object obj) {
        if (obj == null) {
            return MethodResult.ofResult();
        }

        if (obj instanceof Double) {
            final BigDecimal value = BigDecimal.valueOf((double)obj);
            return MethodResult.ofResult(value);
        }

        if (obj instanceof BigDecimal) {
            final BigDecimal value = (BigDecimal)obj;
            return MethodResult.ofResult(value);
        }

        if (obj instanceof String) {
            final String string = (String)obj;
            try {
                final double value = Double.parseDouble(string);
                return MethodResult.ofResult(BigDecimal.valueOf(value));
            }
            catch (NumberFormatException ex) {
                final String errorMessage = String.format("'%s' is not a valid fixed value.", string);
                LOG.log(Level.SEVERE, errorMessage, ex);
                return MethodResult.ofError(errorMessage, ex);
            }
        }

        return this.castNotSupportedResult(obj);
    }

    @Override
    public boolean castSupportedFor(Class<?> cl) {
        assert cl != null;        
        return cl != null && (cl.equals(String.class) || cl.equals(Double.class) || cl.equals(BigDecimal.class));
    }

    @Override
    public String getTypeName() {
        final String unsignedPrefix = this.signed ? "" : "u";
        return String.format("%s%s%sx%s", unsignedPrefix, BASE_NAME, this.m, this.n);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BASE_NAME, this.signed, this.m, this.n);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof FixedType) {
            final FixedType type = (FixedType)obj;
            return type.signed == this.signed && type.m == this.m && type.n == this.n;
        }
        
        return false;
    }

    public static boolean isValidMValue(int value) {
        return value % 8 == 0 && MIN_M <= value && value <= MAX_M;
    }

    public static boolean isValidNValue(int value) {
        return MIN_N <= value && value <= MAX_N;
    }
    
}