package au.csiro.data61.aap.elf

import au.csiro.data61.aap.samples.SampleUtils
import spock.lang.Specification
import spock.lang.Unroll

class ValidatorSemanticSpec extends Specification {
    Validator validator = new Validator()

    static List<EthqlProcessingError> validate(String script, Validator validator) {
        validator.analyzeScript(new ByteArrayInputStream(script.getBytes()))
    }

    /*
    Fails
    @Unroll
    def "type #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        "int[] a = newIntArray();"          | []
        "int a = 0x876;"                    | ["Cannot assign a byte value to a int variable."]
        "string a = 5;"                     | ["Cannot assign a int value to a string variable."]
        "uint a = -15;"                     | ["out of range"]
        "int8 a = 260"                      | ["out of range"]
    }
    */

    @Unroll
    def "transaction filter #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | BLOCKS (1) (5) {
        |   TRANSACTIONS (ANY) (
        |     0x931D387731bBbC988B312206c74F77D004D6B84b,
        |     0x931D387731bBbC988B312206c74F77D004D6B84c
        |   ) {}
        | }
        """.stripMargin()                   | []
        """
        | bytes20 addr = 0x931D387731bBbC988B312206c74F77D004D6B84b;
        | BLOCKS (1) (5) {
        |   TRANSACTIONS (ANY) (addr) {}
        | }
        """.stripMargin()                   | []
        "TRANSACTIONS (ANY) (ANY) {}"       | ["Invalid nesting of filters."]
        """
        | BLOCKS (1) (5) {
        |   TRANSACTIONS (ANY) (0x123) {}
        | }
        """.stripMargin()                   | ["'0x123' is not a valid address literal."]
    }

    @Unroll
    def "generic filter #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
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

    /*
    Fails
    @Unroll
    def "smartContractFilter #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | BLOCKS (6605100) (6615100) {
        |   SMART CONTRACT (0x931D387731bBbC988B312206c74F77D004D6B84c)
        |   (address addr, int i, string s = someMethod(int[] {5, 6})) {}
        | }
        """.stripMargin()                   | []
        """
        | BLOCKS (6605100) (6615100) {
        |   address addr = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        |   SMART CONTRACT (addr)
        |   (int i, string s = someMethod(int[] {5, 6})) {}
        | }
        """.stripMargin()                   | []
        """
        | BLOCKS (6605100) (6615100) {
        |   address addr = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        |   SMART CONTRACT (addr)
        |   (int i, string s = someMethod(int[] {"5", "6"})) {}
        | }
        """.stripMargin()                   | ["Cannot cast string[] literal to int[]."]
    }
    */

    def "log entries filter"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | address contract = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        | BLOCKS (6605100) (6615100) {
        |   LOG ENTRIES (contract) (someMethod(uint indexed authorId, bytes32 sha)) {}
        | }
        """.stripMargin()                   | []
        """
        | BLOCKS (6605100) (6615100) {
        |   LOG ENTRIES (contract) (someMethod(uint indexed authorId, bytes32 sha)) {}
        | }
        """.stripMargin()                   | ["Variable 'contract' not defined."]
        """
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
