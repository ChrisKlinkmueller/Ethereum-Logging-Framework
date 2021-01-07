package blf.configuration;

import java.math.BigInteger;
import java.util.List;

import blf.core.values.ValueAccessor;
import blf.core.values.Variables;
import blf.util.TypeUtils;
import io.reactivex.annotations.NonNull;

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

    public static ValueAccessorSpecification addressLiteral(@NonNull String literal) {
        final String value = TypeUtils.parseBytesLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification addressArrayLiteral(@NonNull String literal) {
        final List<String> values = TypeUtils.parseBytesArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification booleanLiteral(@NonNull String literal) {
        final boolean value = TypeUtils.parseBoolLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification booleanArrayLiteral(@NonNull String literal) {
        final List<Boolean> values = TypeUtils.parseBoolArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification bytesLiteral(@NonNull String literal) {
        final String value = TypeUtils.parseBytesLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification bytesArrayLiteral(@NonNull String literal) {
        final List<String> values = TypeUtils.parseBytesArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification integerLiteral(@NonNull String literal) {
        final BigInteger number = TypeUtils.parseIntLiteral(literal);
        return new ValueAccessorSpecification(state -> number);
    }

    public static ValueAccessorSpecification integerArrayLiteral(@NonNull String literal) {
        final List<BigInteger> values = TypeUtils.parseIntArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification integerLiteral(long literal) {
        return new ValueAccessorSpecification(state -> BigInteger.valueOf(literal));
    }

    public static ValueAccessorSpecification integerLiteral(@NonNull BigInteger literal) {
        return new ValueAccessorSpecification(state -> literal);
    }

    public static ValueAccessorSpecification stringLiteral(@NonNull String literal) {
        final String value = TypeUtils.parseStringLiteral(literal);
        return new ValueAccessorSpecification(state -> value);
    }

    public static ValueAccessorSpecification stringArrayLiteral(@NonNull String literal) {
        final List<String> values = TypeUtils.parseStringArrayLiteral(literal);
        return new ValueAccessorSpecification(state -> values);
    }

    public static ValueAccessorSpecification ofVariable(@NonNull String varName) {
        return new ValueAccessorSpecification(Variables.createValueAccessor(varName));
    }
}
