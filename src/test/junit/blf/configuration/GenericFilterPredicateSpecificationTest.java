package blf.configuration;

import blf.core.interfaces.GenericFilterPredicate;
import blf.core.state.ProgramState;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericFilterPredicateSpecificationTest {
    @Test
    void ofBooleanAccessor() {
        List<ValueAccessorSpecification> notBoolean = new ArrayList<>();
        notBoolean.add(ValueAccessorSpecification.stringLiteral("\"string\""));
        notBoolean.add(ValueAccessorSpecification.integerLiteral("123"));

        for(ValueAccessorSpecification vas : notBoolean) {
            GenericFilterPredicate gfPredicate = GenericFilterPredicateSpecification.ofBooleanAccessor(vas).getPredicate();
            ProgramState psMock = Mockito.mock(ProgramState.class);
            assertThrows(ClassCastException.class, () -> gfPredicate.test(psMock));
        }
    }
}