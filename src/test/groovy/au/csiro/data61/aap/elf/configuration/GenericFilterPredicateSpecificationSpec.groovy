package au.csiro.data61.aap.elf.configuration

import au.csiro.data61.aap.elf.core.ProgramState
import spock.lang.Specification

class GenericFilterPredicateSpecificationSpec extends Specification {
    def "ofBooleanValue should throw when condition is not boolean"() {
        given:
        GenericFilterPredicateSpecification predicateSpec =
                GenericFilterPredicateSpecification.ofBooleanValue(notBoolean)

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
