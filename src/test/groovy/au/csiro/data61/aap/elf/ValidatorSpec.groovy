package au.csiro.data61.aap.elf

import au.csiro.data61.aap.samples.SampleUtils
import spock.lang.Specification
import spock.lang.Unroll

class ValidatorSpec extends Specification {
    Validator validator = new Validator()

    @Unroll
    def "#url.getFile() should pass validation"() {
        when:
        List<EthqlProcessingError> errors = validator.analyzeScript(url.getFile())

        then:
        noExceptionThrown()
        errors.size() == 0

        where:
        url << SampleUtils.getAllResources()
    }

    def "Validator should throw exception when file not exists"() {
        when:
        validator.analyzeScript('notExist.ethql')

        then:
        EthqlProcessingException e = thrown()
        e.getMessage() == '''Invalid file path: 'notExist.ethql'.'''
    }

    def "comment"() {
        expect:
        validateSyntax('comment.ethql').size() == 0
    }

    def "identifier"() {
        expect:
        List<EthqlProcessingError> errors = validateSyntax('identifier.ethql')
        errors*.getErrorMessage() == [
                "token recognition error at: '^'",
                "extraneous input '8' expecting Identifier"
        ]
    }

    def "type"() {
        expect:
        List<EthqlProcessingError> errors = validateSyntax('type.ethql')
        errors*.getErrorMessage() == [
                "no viable alternative at input 'Stringd'"
        ]
    }

    def "literal"() {
        expect:
        List<EthqlProcessingError> errors = validateSyntax('literal.ethql')
        errors*.getLine() == [11, 11, 11, 12, 12, 12]
    }

    List<EthqlProcessingError> validateSyntax(String fileName) {
        validator.analyzeScript(getClass().getClassLoader().getResource("syntacticValidation/" + fileName).getFile())
    }

    List<EthqlProcessingError> validateSemantics(String fileName) {
        validator.analyzeScript(getClass().getClassLoader().getResource("semanticsValidation/" + fileName).getFile())
    }

    def "variable"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('variable.ethql')
        errors*.getErrorMessage() == [
                "Variable 'a' is already defined.",
                "Variable 'b' not defined."
        ]
    }

    def "block filter"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('blockfilter.ethql')
        errors*.getErrorMessage().size() != 0
    }

    def "type mismatch"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('type.ethql')
        errors*.getErrorMessage() == [
                "Cannot assign a byte value to a int variable.",
                "Cannot assign a int value to a string variable."

        ]
    }

    def "method"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('method.ethql')
        errors*.getErrorMessage() == [
                "Method 'connec' with parameters 'string' unknown.",
                "Method 'connect' with parameters 'string, int' unknown.",
                "Method 'connect' with parameters 'int' unknown."
        ]
    }

    def "transaction filter"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('transactionfilter.ethql')
        errors*.getErrorMessage() == []
    }

    def "generic filter"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('genericfilter.ethql')
        errors*.getErrorMessage() == [
                "Expression must return a boolean value.",
                "Expression must return a boolean value.",
                "Types are not compatible, cannot check containment of int in string."
        ]
    }

    def "smartContractFilter"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('smartcontractfilter.ethql')
        errors*.getErrorMessage() == [
                "Cannot cast string[] literal to int[]."
        ]
    }

    def "log entries filter"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('logentriesfilter.ethql')
        errors*.getErrorMessage() == [
                "Invalid nesting of filters."
        ]
    }

    def "emit"() {
        expect:
        List<EthqlProcessingError> errors = validateSemantics('emit.ethql')
        errors*.getErrorMessage() == [
                "Variable 'block.number' not defined."
        ]
    }
}
