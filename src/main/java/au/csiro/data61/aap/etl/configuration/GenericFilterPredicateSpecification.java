package au.csiro.data61.aap.etl.configuration;

import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.filters.GenericFilterPredicate;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.library.types.ListOperations;

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

    /*public static GenericFilterPredicateSpecification equals(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return null;
    }

    public static GenericFilterPredicateSpecification smallerThan(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return null;
    }

    public static GenericFilterPredicateSpecification smallerThanAndEquals(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return null;
    }

    public static GenericFilterPredicateSpecification greaterThan(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return null;
    }

    public static GenericFilterPredicateSpecification greaterThanAndEquals(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return null;
    }*/

    public static GenericFilterPredicateSpecification ofBooleanVariable(ValueAccessorSpecification specification) {
        final ValueAccessor accessor = specification.getValueAccessor();
        return new GenericFilterPredicateSpecification(state -> (Boolean)accessor.getValue(state));
    }

    public static GenericFilterPredicateSpecification in(ValueAccessorSpecification accessor1, ValueAccessorSpecification accessor2) {
        return new GenericFilterPredicateSpecification(createFilter(accessor1, accessor2, ListOperations::contains));
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