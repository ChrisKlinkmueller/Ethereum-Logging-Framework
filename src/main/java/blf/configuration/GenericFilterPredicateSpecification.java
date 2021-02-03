package blf.configuration;

import blf.core.exceptions.ExceptionHandler;
import blf.core.interfaces.GenericFilterPredicate;
import blf.core.interfaces.Method;
import blf.core.values.ValueAccessor;
import blf.library.types.ListOperations;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

/**
 * GenericFilterSpecification
 */
public class GenericFilterPredicateSpecification {
    // TODO: this predicate parameter is not needed -> return GenericFilterPredicate directly from static methods
    private final GenericFilterPredicate predicate;

    private GenericFilterPredicateSpecification(GenericFilterPredicate predicate) {
        this.predicate = predicate;
    }

    GenericFilterPredicate getPredicate() {
        return this.predicate;
    }

    public static GenericFilterPredicateSpecification and(
        GenericFilterPredicateSpecification specification1,
        GenericFilterPredicateSpecification specification2
    ) {
        final GenericFilterPredicate predicate1 = specification1.getPredicate();
        final GenericFilterPredicate predicate2 = specification2.getPredicate();
        return new GenericFilterPredicateSpecification(state -> predicate1.test(state) && predicate2.test(state));
    }

    public static GenericFilterPredicateSpecification or(
        GenericFilterPredicateSpecification specification1,
        GenericFilterPredicateSpecification specification2
    ) {
        final GenericFilterPredicate predicate1 = specification1.getPredicate();
        final GenericFilterPredicate predicate2 = specification2.getPredicate();
        return new GenericFilterPredicateSpecification(state -> predicate1.test(state) || predicate2.test(state));
    }

    public static GenericFilterPredicateSpecification not(GenericFilterPredicateSpecification specification1) {
        final GenericFilterPredicate predicate1 = specification1.getPredicate();
        return new GenericFilterPredicateSpecification(state -> !predicate1.test(state));
    }

    public static GenericFilterPredicateSpecification ofBooleanAccessor(ValueAccessorSpecification valueSpecification) {
        final ValueAccessor accessor = valueSpecification.getValueAccessor();
        return new GenericFilterPredicateSpecification(state -> (Boolean) accessor.getValue(state));
    }

    public static GenericFilterPredicateSpecification equals(
        ValueAccessorSpecification valueSpecification1,
        ValueAccessorSpecification valueSpecification2
    ) {
        return ofEqualityCheck(valueSpecification1, valueSpecification2, (val1, val2) -> val1 != null && val1.equals(val2));
    }

    public static GenericFilterPredicateSpecification notEquals(
        ValueAccessorSpecification valueSpecification1,
        ValueAccessorSpecification valueSpecification2
    ) {
        return ofEqualityCheck(valueSpecification1, valueSpecification2, (val1, val2) -> !Objects.equals(val1, val2));
    }

    private static GenericFilterPredicateSpecification ofEqualityCheck(
        ValueAccessorSpecification valueSpecification1,
        ValueAccessorSpecification valueSpecification2,
        BiPredicate<Object, Object> predicate
    ) {
        assert valueSpecification1 != null;
        assert valueSpecification2 != null;
        final ValueAccessor accessor1 = valueSpecification1.getValueAccessor();
        final ValueAccessor accessor2 = valueSpecification2.getValueAccessor();
        return new GenericFilterPredicateSpecification(state -> {
            Object value1 = accessor1.getValue(state);
            Object value2 = accessor2.getValue(state);
            return predicate.test(value1, value2);
        });
    }

    public static GenericFilterPredicateSpecification smallerThan(
        ValueAccessorSpecification accessor1,
        ValueAccessorSpecification accessor2
    ) {
        return ofIntegerComparison(accessor1, accessor2, i -> i < 0);
    }

    public static GenericFilterPredicateSpecification smallerThanAndEquals(
        ValueAccessorSpecification accessor1,
        ValueAccessorSpecification accessor2
    ) {
        return ofIntegerComparison(accessor1, accessor2, i -> i <= 0);
    }

    public static GenericFilterPredicateSpecification greaterThan(
        ValueAccessorSpecification accessor1,
        ValueAccessorSpecification accessor2
    ) {
        return ofIntegerComparison(accessor1, accessor2, i -> i > 0);
    }

    public static GenericFilterPredicateSpecification greaterThanAndEquals(
        ValueAccessorSpecification accessor1,
        ValueAccessorSpecification accessor2
    ) {
        return ofIntegerComparison(accessor1, accessor2, i -> i >= 0);
    }

    private static GenericFilterPredicateSpecification ofIntegerComparison(
        ValueAccessorSpecification valueSpecification1,
        ValueAccessorSpecification valueSpecification2,
        IntPredicate comparator
    ) {
        final ExceptionHandler exceptionHandler = new ExceptionHandler();

        if (valueSpecification1 == null) {
            exceptionHandler.handleException("Value specification (first) is null.", new NullPointerException());
            return new GenericFilterPredicateSpecification(state -> false);
        }

        if (valueSpecification2 == null) {
            exceptionHandler.handleException("Value specification (second) is null.", new NullPointerException());
            return new GenericFilterPredicateSpecification(state -> false);
        }

        final ValueAccessor accessor1 = valueSpecification1.getValueAccessor();
        final ValueAccessor accessor2 = valueSpecification2.getValueAccessor();

        return new GenericFilterPredicateSpecification(state -> {
            final Object value1 = accessor1.getValue(state);
            if (value1 != null && !(value1 instanceof BigInteger)) {
                final String errorMsg = String.format("Value '%s' is not an BigInteger.", value1);
                exceptionHandler.handleException(errorMsg);

                return false;
            }

            final Object value2 = accessor2.getValue(state);
            if (value2 != null && !(value2 instanceof BigInteger)) {
                final String errorMsg = String.format("Value '%s' is not an BigInteger.", value2);
                exceptionHandler.handleException(errorMsg);

                return false;
            }

            return value1 != null && value2 != null && comparator.test(((BigInteger) value1).compareTo((BigInteger) value2));
        });

    }

    public static GenericFilterPredicateSpecification in(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return new GenericFilterPredicateSpecification(createFilter(accessor2, accessor1, ListOperations::contains));
    }

    private static GenericFilterPredicate createFilter(
        ValueAccessorSpecification specification1,
        ValueAccessorSpecification specification2,
        Method method
    ) {
        final ValueAccessor accessor1 = specification1.getValueAccessor();
        final ValueAccessor accessor2 = specification2.getValueAccessor();
        return state -> {
            final Object val1 = accessor1.getValue(state);
            final Object val2 = accessor2.getValue(state);
            return (Boolean) method.call(new Object[] { val1, val2 }, state);
        };
    }
}
