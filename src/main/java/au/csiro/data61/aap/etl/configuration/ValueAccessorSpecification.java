package au.csiro.data61.aap.etl.configuration;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Type;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
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

    public static ValueAccessorSpecification addressLiteral(String literal) {
		return new ValueAccessorSpecification(state -> instantiateType("address", literal).getValue());
	}

    public static ValueAccessorSpecification addressArrayLiteral(String literal) {
		return new ValueAccessorSpecification(state -> instantiateType("address[]", toList(literal)).getValue());
	}

    public static ValueAccessorSpecification booleanLiteral(String literal) {
		return new ValueAccessorSpecification(state -> instantiateType("bool", literal).getValue());
	}

    public static ValueAccessorSpecification booleanArrayLiteral(String literal) {
		return new ValueAccessorSpecification(state -> instantiateType("bool[]", toList(literal)).getValue());
	}

    public static ValueAccessorSpecification bytesLiteral(String literal) {
		return new ValueAccessorSpecification(state -> instantiateType("bytes", literal).getValue());
	}

    public static ValueAccessorSpecification bytesArrayLiteral(String literal) {
		return new ValueAccessorSpecification(state -> instantiateType("bytes[]", toList(literal)).getValue());
	}

    public static ValueAccessorSpecification fixedLiteral(String literal) {
        return new ValueAccessorSpecification(state -> instantiateType("fixed", literal).getValue());
    }

    public static ValueAccessorSpecification fixedArrayLiteral(String literal) {
        return new ValueAccessorSpecification(state -> instantiateType("fixed[]", toList(literal)).getValue());
    }

    public static ValueAccessorSpecification integerLiteral(String literal) {
        return new ValueAccessorSpecification(state -> instantiateType("int", literal).getValue());
    }

    public static ValueAccessorSpecification integerArrayLiteral(String literal) {
        return new ValueAccessorSpecification(state -> instantiateType("int[]", toList(literal)).getValue());
    }

    public static ValueAccessorSpecification integerLiteral(long literal) {
        return new ValueAccessorSpecification(state -> BigInteger.valueOf(literal));
    }

    public static ValueAccessorSpecification integerLiteral(BigInteger literal) {
        assert literal != null;
        return new ValueAccessorSpecification(state -> literal);
    }

    public static ValueAccessorSpecification stringLiteral(String literal) {
        assert literal != null;
        return new ValueAccessorSpecification(state -> instantiateType("string", literal).getValue());
    }

    public static ValueAccessorSpecification stringArrayLiteral(String literal) {
        assert literal != null;
        return new ValueAccessorSpecification(state -> instantiateType("string[]", literal).getValue());
    }

    private static List<String> toList(String literal) {
        String values = literal.trim();
        values = values.substring(1, values.length() - 1);
        return Arrays.asList(values.split("."))
            .stream()
            .map(v -> v.trim())
            .collect(Collectors.toList());
    }
    

    private static Type<?> instantiateType(String type, Object value) throws ProgramException {
        try {
            return TypeDecoder.instantiateType(type, value);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ProgramException(String.format("Error decoding value '%s' as %s.", value, type), e);
        }
    }

    public static ValueAccessorSpecification ofVariable(String varName) {
        return new ValueAccessorSpecification(Variables.createValueAccessor(varName));
    }
}