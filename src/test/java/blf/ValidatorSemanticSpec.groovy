package blf

import spock.lang.Specification
import spock.lang.Unroll

class ValidatorSemanticSpec extends Specification {
    Validator validator = new Validator()

    static List<BcqlProcessingError> validate(String script, Validator validator) {
        validator.analyzeScript(new ByteArrayInputStream(script.getBytes()))
    }

    @Unroll
    def "transaction filter #script"() {
        expect:
        List<BcqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | SET BLOCKCHAIN "Ethereum"
        | BLOCKS (1) (5) {
        |   TRANSACTIONS (ANY) (
        |     0x931D387731bBbC988B312206c74F77D004D6B84b,
        |     0x931D387731bBbC988B312206c74F77D004D6B84c
        |   ) {}
        | }
        """.stripMargin()                   | []
        """
        | SET BLOCKCHAIN "Ethereum"
        | bytes20 addr = 0x931D387731bBbC988B312206c74F77D004D6B84b;
        | BLOCKS (1) (5) {
        |   TRANSACTIONS (ANY) (addr) {}
        | }
        """.stripMargin()                   | []
        "SET BLOCKCHAIN \"Ethereum\" \n" +
                "TRANSACTIONS (ANY) (ANY) {}"       | ["Invalid nesting of filters."]
        """
        | SET BLOCKCHAIN "Ethereum"
        | BLOCKS (1) (5) {
        |   TRANSACTIONS (ANY) (0x123) {}
        | }
        """.stripMargin()                   | ["'0x123' is not a valid address literal."]
    }

    @Unroll
    def "generic filter #script"() {
        expect:
        List<BcqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | if (true) {}
        """.stripMargin()                   | []
        """
        | int i = 5;
        | if (i == 0) {}
        """.stripMargin()                   | []
        """
        | if (5 > 0) {}
        """.stripMargin()                   | []
        """
        | if (true && false) {}
        """.stripMargin()                   | []
        """
        | if (true || false) {}
        """.stripMargin()                   | []
        """
        | if (5 in {4,5,6}) {}
        """.stripMargin()                   | []
        """
        | int i = 5;
        | if (i in {4,5,6}) {}
        """.stripMargin()                   | []
        "if (true == (5 == 4)) {}"          | []
        """
        | if (!false) {}
        """.stripMargin()                   | []
        "if (true && i in {5, 3}) {}"       | ["Variable 'i' not defined."]
        "if (false && \"true\") {}"         | ["Expression must return a boolean value."]
        """
        | int i = 4; 
        | if (i in \"[5,3]\") {}
        """.stripMargin()                   | ["Types are not compatible, cannot check containment of int in string."]
    }

    def "log entries filter"() {
        expect:
        List<BcqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | SET BLOCKCHAIN "Ethereum"
        | address contract = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        | BLOCKS (6605100) (6615100) {
        |   LOG ENTRIES (contract) (someMethod(uint indexed authorId, bytes32 sha)) {}
        | }
        """.stripMargin()                   | []
        """
        | SET BLOCKCHAIN "Ethereum"
        | BLOCKS (6605100) (6615100) {
        |   LOG ENTRIES (contract) (someMethod(uint indexed authorId, bytes32 sha)) {}
        | }
        """.stripMargin()                   | ["Variable 'contract' not defined."]
        """
        | SET BLOCKCHAIN "Ethereum"
        | address contract = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        | LOG ENTRIES
        | (contract)
        | (GitCommit(
        |   uint authorId,
        |   bytes32 sha
        | )) {}
        """.stripMargin()                   | ["Invalid nesting of filters."]
    }
}
