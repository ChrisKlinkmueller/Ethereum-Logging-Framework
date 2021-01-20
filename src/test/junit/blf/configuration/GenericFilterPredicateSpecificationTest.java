package blf.configuration;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericFilterPredicateSpecificationTest {
    @Test
    void ofBooleanAccessor() {
        // TODO think about parameterization?
        List<ValueAccessorSpecification> notBoolean = new ArrayList<ValueAccessorSpecification>();
        notBoolean.add(ValueAccessorSpecification.stringLiteral("\"string\""));
        notBoolean.add(ValueAccessorSpecification.integerLiteral("123"));

        for(ValueAccessorSpecification vas : notBoolean) {
            GenericFilterPredicateSpecification predicateSpec = GenericFilterPredicateSpecification.ofBooleanAccessor(vas);
            assertThrows(ClassCastException.class, () -> predicateSpec.getPredicate().test(new EthereumProgramState()));
        }
    }
}