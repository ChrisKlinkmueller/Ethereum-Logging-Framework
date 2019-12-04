package au.csiro.data61.aap.elf.configuration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.values.Variables;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * ValueAccessorSpecification
 */
public class ValueAccessorSpecification {
    // TODO: replace use of TypeDecoder as it expects byte encoding, not string representations!

    private final ValueAccessor valueAccessor;

    public ValueAccessorSpecification(ValueAccessor valueAccessor) {
        this.valueAccessor = valueAccessor;
    }

    public ValueAccessor getValueAccessor() {
        return this.valueAccessor;
    }

    public static ValueAccessorSpecification addressLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = parseBytesLiteral(literal);
		return new ValueAccessorSpecification(state -> value);
	}

    public static ValueAccessorSpecification addressArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<String> values = ValueAccessorSpecification.parseArrayLiteral(literal, ValueAccessorSpecification::parseBytesLiteral);
		return new ValueAccessorSpecification(state -> values);
	}

    public static ValueAccessorSpecification booleanLiteral(String literal) throws BuildException  {
        assert literal != null;
		final boolean value = parseBoolLiteral(literal);
		return new ValueAccessorSpecification(state -> value);
	}

    public static ValueAccessorSpecification booleanArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<Boolean> values = ValueAccessorSpecification.parseArrayLiteral(literal, ValueAccessorSpecification::parseBoolLiteral);
		return new ValueAccessorSpecification(state -> values);
	}

    public static ValueAccessorSpecification bytesLiteral(String literal) throws BuildException  {
        assert literal != null;
		final String value = parseBytesLiteral(literal);
		return new ValueAccessorSpecification(state -> value);
	}

    public static ValueAccessorSpecification bytesArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<String> values = ValueAccessorSpecification.parseArrayLiteral(literal, ValueAccessorSpecification::parseBytesLiteral);
		return new ValueAccessorSpecification(state -> values);
	}

    public static ValueAccessorSpecification integerLiteral(String literal) throws BuildException  {
        assert literal != null;
        final BigInteger number = parseIntLiteral(literal);
        return new ValueAccessorSpecification(state -> number);
    }

    public static ValueAccessorSpecification integerArrayLiteral(String literal) throws BuildException  {
        assert literal != null;
        final List<BigInteger> values = ValueAccessorSpecification.parseArrayLiteral(literal, ValueAccessorSpecification::parseIntLiteral);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification integerLiteral(long literal) throws BuildException  {
        return new ValueAccessorSpecification(state -> BigInteger.valueOf(literal));
    }

    public static ValueAccessorSpecification integerLiteral(BigInteger literal) throws BuildException  {
        assert literal != null;
        return new ValueAccessorSpecification(state -> literal);
    }

    public static ValueAccessorSpecification stringLiteral(String literal) throws BuildException {
        assert literal != null;
        final String value = parseStringLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification stringArrayLiteral(String literal) throws BuildException {
        assert literal != null;
        final List<String> values = ValueAccessorSpecification.parseArrayLiteral(literal, ValueAccessorSpecification::parseStringLiteral);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification ofVariable(String varName) {
        assert varName != null;
        return new ValueAccessorSpecification(Variables.createValueAccessor(varName));
    }

    private static <T> List<T> parseArrayLiteral(String literal, Converter<T> converter) throws BuildException {
        if (!TypeUtils.isArrayLiteral(literal)) {
            throw new BuildException(String.format("Value '%s' is not an array literal.", literal));
        }
        List<T> list = new ArrayList<>();
        final String[] elements = literal.substring(1, literal.length() - 1).split(",");
        for (String element : elements) {
            list.add(converter.convert(element));
        }
        return list;
    }
    
    private static Boolean parseBoolLiteral(String literal) throws BuildException {
        if (!TypeUtils.isBooleanLiteral(literal)) {
            throw new BuildException(String.format("Value '%s' is not a string literal.", literal));
        }
        return Boolean.parseBoolean(literal);
    } 
    
    private static String parseBytesLiteral(String literal) throws BuildException {
        if (!TypeUtils.isBytesLiteral(literal)) {
            throw new BuildException(String.format("Value '%s' is not a bytes or address literal.", literal));
        }
        return literal;
    } 

    private static BigInteger parseIntLiteral(String literal) throws BuildException {
        if (!TypeUtils.isIntLiteral(literal)) {
            throw new BuildException(String.format("Value '%s' is not an int literal.", literal));
        }
        return new BigInteger(literal);
    }

    private static String parseStringLiteral(String literal) throws BuildException {
        if (!TypeUtils.isStringLiteral(literal)) {
            throw new BuildException(String.format("Value '%s' is not a string literal.", literal));
        }
        return literal.substring(1, literal.length() - 1);
    }

    @FunctionalInterface
    private static interface Converter<T> {
        public T convert(String literal) throws BuildException;
    }
}