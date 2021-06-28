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
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(value);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification addressArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<String> values = TypeUtils.parseBytesArrayLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(values);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification booleanLiteral(String literal) throws BuildException {
        assert literal != null;
        final boolean value = TypeUtils.parseBoolLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(value);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification booleanArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<Boolean> values = TypeUtils.parseBoolArrayLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(values);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification bytesLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = TypeUtils.parseBytesLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(value);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification bytesArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<String> values = TypeUtils.parseBytesArrayLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(values);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification integerLiteral(String literal) throws BuildException {
        assert literal != null;
        final BigInteger number = TypeUtils.parseIntLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(number);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification integerArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<BigInteger> values = TypeUtils.parseIntArrayLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(values);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification integerLiteral(long literal) throws BuildException {
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(BigInteger.valueOf(literal));
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification integerLiteral(BigInteger literal) throws BuildException {
        assert literal != null;
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(literal);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification stringLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = TypeUtils.parseStringLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(value);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification stringArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<String> values = TypeUtils.parseStringArrayLiteral(literal);
        final ValueAccessor accessor = ValueAccessor.createLiteralAccessor(values);
        return new ValueAccessorSpecification(accessor);
    }

    public static ValueAccessorSpecification ofVariable(String varName) {
        assert varName != null;
        return new ValueAccessorSpecification(Variables.createValueAccessor(varName));
    }
}
