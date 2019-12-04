package au.csiro.data61.aap.elf.configuration;

import java.math.BigInteger;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

import au.csiro.data61.aap.elf.core.Method;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.filters.GenericFilterPredicate;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.library.types.ListOperations;

/**
 * GenericFilterSpecification
 */
public class GenericFilterPredicateSpecification {
    private final GenericFilterPredicate predicate;
    
    private GenericFilterPredicateSpecification(GenericFilterPredicate predicate) {
        this.predicate = predicate;
    }

    GenericFilterPredicate getPredicate() {
        return this.predicate;
    }

    public static GenericFilterPredicateSpecification and(GenericFilterPredicateSpecification specification1, GenericFilterPredicateSpecification specification2) {
        final GenericFilterPredicate predicate1 = specification1.getPredicate();
        final GenericFilterPredicate predicate2 = specification2.getPredicate();    
        return new GenericFilterPredicateSpecification(state -> predicate1.test(state) && predicate2.test(state));
    } 

    public static GenericFilterPredicateSpecification or(GenericFilterPredicateSpecification specification1, GenericFilterPredicateSpecification specification2) {
        final GenericFilterPredicate predicate1 = specification1.getPredicate();
        final GenericFilterPredicate predicate2 = specification2.getPredicate();    
        return new GenericFilterPredicateSpecification(state -> predicate1.test(state) || predicate2.test(state));
    }    

    public static GenericFilterPredicateSpecification not(GenericFilterPredicateSpecification specification1) {
        final GenericFilterPredicate predicate1 = specification1.getPredicate();
        return new GenericFilterPredicateSpecification(state -> !predicate1.test(state));
    } 

    public static GenericFilterPredicateSpecification ofBooleanValue(ValueAccessorSpecification valueSpecification) {
        assert valueSpecification != null;
        final ValueAccessor accessor = valueSpecification.getValueAccessor();
        return new GenericFilterPredicateSpecification(state -> (Boolean)accessor.getValue(state));
    }

    public static GenericFilterPredicateSpecification equals(ValueAccessorSpecification valueSpecidication1, ValueAccessorSpecification valueSpecidication2) {
        return ofEqualityCheck(valueSpecidication1, valueSpecidication2, (val1, val2) -> val1 != null && val1.equals(val2));
    }

    public static GenericFilterPredicateSpecification notEquals(ValueAccessorSpecification valueSpecidication1, ValueAccessorSpecification valueSpecidication2) {
        return ofEqualityCheck(valueSpecidication1, valueSpecidication2, (val1, val2) -> val1 == null ? val2 != null : !val1.equals(val2));
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
        return new GenericFilterPredicateSpecification(
            state -> {
                Object value1 = accessor1.getValue(state);
                Object value2 = accessor2.getValue(state);
                return predicate.test(value1, value2);
            }
        );
    }

    public static GenericFilterPredicateSpecification smallerThan(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return ofIntegerComparison(accessor1, accessor2, i -> i < 0);
    }

    public static GenericFilterPredicateSpecification smallerThanAndEquals(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return ofIntegerComparison(accessor1, accessor2, i -> i <= 0);
    }

    public static GenericFilterPredicateSpecification greaterThan(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return ofIntegerComparison(accessor1, accessor2, i -> i > 0);
    }

    public static GenericFilterPredicateSpecification greaterThanAndEquals(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return ofIntegerComparison(accessor1, accessor2, i -> i >= 0);
    }

    private static GenericFilterPredicateSpecification ofIntegerComparison(
        ValueAccessorSpecification valueSpecification1, 
        ValueAccessorSpecification valueSpecification2,
        IntPredicate comparator
    ) {
        assert valueSpecification1 != null;
        assert valueSpecification2 != null;
        final ValueAccessor accessor1 = valueSpecification1.getValueAccessor();
        final ValueAccessor accessor2 = valueSpecification2.getValueAccessor();
        return new GenericFilterPredicateSpecification(
            state -> {
                final Object value1 = accessor1.getValue(state);
                if (value1 != null && !(value1 instanceof BigInteger)) {
                    throw new ProgramException(String.format("Value '%s' is not an BigInteger.", value1));
                } 
                
                final Object value2 = accessor2.getValue(state);
                if (value2 != null && !(value2 instanceof BigInteger)) {
                    throw new ProgramException(String.format("Value '%s' is not an BigInteger.", value2));
                } 

                return value1 != null && value2 != null && comparator.test(((BigInteger)value1).compareTo((BigInteger)value2));
            }
        );
        
    }

    public static GenericFilterPredicateSpecification ofBooleanVariable(ValueAccessorSpecification specification) {
        final ValueAccessor accessor = specification.getValueAccessor();
        return new GenericFilterPredicateSpecification(state -> (Boolean)accessor.getValue(state));
    }

    public static GenericFilterPredicateSpecification in(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return new GenericFilterPredicateSpecification(createFilter(accessor2, accessor1, ListOperations::contains));
    }

    private static final GenericFilterPredicate createFilter(ValueAccessorSpecification specification1, ValueAccessorSpecification specification2, Method method) {
        final ValueAccessor accessor1 = specification1.getValueAccessor();
        final ValueAccessor accessor2 = specification2.getValueAccessor();
        return state -> {
            final Object val1 = accessor1.getValue(state);
            final Object val2 = accessor2.getValue(state);
            return (Boolean)method.call(new Object[]{val1, val2}, state);
        };
    }
}