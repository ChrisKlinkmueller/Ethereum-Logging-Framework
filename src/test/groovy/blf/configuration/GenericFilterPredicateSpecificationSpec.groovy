package blf.configuration

import blf.core.ProgramState
import spock.lang.Specification

class GenericFilterPredicateSpecificationSpec extends Specification {
    def "ofBooleanValue should throw when condition is not boolean"() {
        given:
        GenericFilterPredicateSpecification predicateSpec =
                GenericFilterPredicateSpecification.ofBooleanAccessor(notBoolean)

        when:
        predicateSpec.getPredicate().test(Mock(ProgramState))

        then:
        thrown(ClassCastException)

        where:
        notBoolean << [
                ValueAccessorSpecification.stringLiteral("\"string\""),
                ValueAccessorSpecification.integerLiteral("123")
        ]
    }
}
