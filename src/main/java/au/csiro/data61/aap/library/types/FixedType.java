package au.csiro.data61.aap.library.types;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.util.MethodResult;
import au.csiro.data61.aap.util.StringUtil;

public class FixedType extends SolidityType<BigDecimal> {
    private static final Logger LOG = Logger.getLogger(FixedType.class.getName());
    private static final String NAME = "fixed";
    private static final String UNSIGNED_PREFIX = "u";
    private static final String M_N_DIVIDER = "x";
    private static final int DEFAULT_M = 128;
    private static final int DEFAULT_N = 18;
    
    private final int m;
    private final int n;
    private final boolean signed;

    FixedType(boolean signed, int m, int n) {
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
        return String.format("%s%s%sx%s", unsignedPrefix, NAME, this.m, this.n);
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, this.signed, this.m, this.n);
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

    /**
     * Creates a FixedType instance based on the keyword. A valid keyword matches the regex u?fixed<M>x<N> where
     * M must be divisible by 8 and on the interval [8,256] and N must be on the interval [0,80], more information 
     * <a href="https://solidity.readthedocs.io/en/v0.5.11/types.html#fixed-point-numbers">here</a>. The method
     * returns null, if the keyword is invalid.
     * @param keyword the keyword
     * @return  a FixedType instance
     */
    static SolidityType<?> createFixedType(String keyword) {
        final boolean unsigned = keyword.startsWith(UNSIGNED_PREFIX);
        if (unsigned) {
            keyword = keyword.replaceFirst(UNSIGNED_PREFIX, "");
        }

        if (!keyword.startsWith(NAME)) {
            return null;
        }

        keyword = keyword.replaceFirst(NAME, "");
        if (keyword.isEmpty()) {
            return new FixedType(!unsigned, DEFAULT_M, DEFAULT_N);
        }

        final String[] precisionConfig = keyword.split(M_N_DIVIDER);
        if (precisionConfig.length != 2) {
            return null;
        }

        final MethodResult<Integer> mResult = StringUtil.parseInt(precisionConfig[0]);
        if (!mResult.isSuccessful() || !isValidMValue(mResult.getResult())) {
            return null;
        }

        final MethodResult<Integer> nResult = StringUtil.parseInt(precisionConfig[1]);
        if (!nResult.isSuccessful() || !isValidNValue(nResult.getResult())) {
            return null;
        }

        return new FixedType(!unsigned, mResult.getResult(), nResult.getResult());
    }

    private static boolean isValidMValue(int value) {
        return value % 8 == 0 && 8 <= value && value <= 256;
    }

    private static boolean isValidNValue(int value) {
        return 0 <= value && value <= 80;
    }
    
}