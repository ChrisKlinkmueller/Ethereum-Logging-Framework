package au.csiro.data61.aap.library.types;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.util.MethodResult;

public class FixedType extends SolidityType<BigDecimal> {
    private static final Logger LOG = Logger.getLogger(FixedType.class.getName());
    private static final String NAME = "fixed";

    private final int m;
    private final int n;
    private final boolean signed;

    public FixedType(boolean signed, int m, int n) {
        this.signed = signed;
        this.m = m;
        this.n = n;
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
        final int prime = 157;
        int hash = 151;
        hash += prime * hash + NAME.hashCode();
        hash += prime * hash + Boolean.hashCode(this.signed);
        hash += prime * hash + Integer.hashCode(this.m);
        hash += prime * hash + Integer.hashCode(this.n);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FixedType) {
            final FixedType type = (FixedType)obj;
            return type.signed == this.signed && type.m == this.m && type.n == this.n;
        }
        
        return false;
    }

    
}