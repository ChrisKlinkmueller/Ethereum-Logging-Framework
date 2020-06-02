package au.csiro.data61.aap.elf.configuration;

import java.math.BigInteger;
import java.util.List;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.values.Variables;
import au.csiro.data61.aap.elf.util.TypeUtils;

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

    public static ValueAccessorSpecification addressLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = TypeUtils.parseBytesLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification addressArrayLiteral(String literal)
            throws BuildException {
        assert literal != null;
        final List<String> values = TypeUtils.parseBytesArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification booleanLiteral(String literal) throws BuildException {
        assert literal != null;
        final boolean value = TypeUtils.parseBoolLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification booleanArrayLiteral(String literal)
            throws BuildException {
        assert literal != null;
        final List<Boolean> values = TypeUtils.parseBoolArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification bytesLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = TypeUtils.parseBytesLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification bytesArrayLiteral(String literal)
            throws BuildException {
        assert literal != null;
        final List<String> values = TypeUtils.parseBytesArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification integerLiteral(String literal) throws BuildException {
        assert literal != null;
        final BigInteger number = TypeUtils.parseIntLiteral(literal);
        return new ValueAccessorSpecification(state -> number);
    }

    public static ValueAccessorSpecification integerArrayLiteral(String literal)
            throws BuildException {
        assert literal != null;
        final List<BigInteger> values = TypeUtils.parseIntArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification integerLiteral(long literal) throws BuildException {
        return new ValueAccessorSpecification(state -> BigInteger.valueOf(literal));
    }

    public static ValueAccessorSpecification integerLiteral(BigInteger literal)
            throws BuildException {
        assert literal != null;
        return new ValueAccessorSpecification(state -> literal);
    }

    public static ValueAccessorSpecification stringLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = TypeUtils.parseStringLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification stringArrayLiteral(String literal)
            throws BuildException {
        assert literal != null;
        final List<String> values = TypeUtils.parseStringArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification ofVariable(String varName) {
        assert varName != null;
        return new ValueAccessorSpecification(Variables.createValueAccessor(varName));
    }
}
