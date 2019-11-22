package au.csiro.data61.aap.etl.configuration;

import java.math.BigInteger;

import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.core.values.Variables;

/**
 * ValueAccessorSpecification
 */
public class ValueAccessorSpecification {
    private final ValueAccessor valueAccessor;
    
    public ValueAccessorSpecification(ValueAccessor valueAccessor) {
        this.valueAccessor = valueAccessor;
    }

    public ValueAccessor getValueAccessor() {
        return this.valueAccessor;
    }

    public static ValueAccessorSpecification integerLiteral(String literal) {
        assert literal != null;
        return new ValueAccessorSpecification(state -> new BigInteger(literal));
    }

    public static ValueAccessorSpecification integerLiteral(long literal) {
        return new ValueAccessorSpecification(state -> BigInteger.valueOf(literal));
    }

    public static ValueAccessorSpecification integerLiteral(BigInteger literal) {
        assert literal != null;
        return new ValueAccessorSpecification(state -> literal);
    }

	public static ValueAccessorSpecification stringLiteral(String literal) {
		return new ValueAccessorSpecification(state -> literal);
    }
    
    public static ValueAccessorSpecification ofVariable(String varName) {
        return new ValueAccessorSpecification(Variables.createValueAccessor(varName));
    }
}