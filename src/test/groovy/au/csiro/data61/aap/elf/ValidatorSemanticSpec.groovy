package au.csiro.data61.aap.elf

import au.csiro.data61.aap.samples.SampleUtils
import spock.lang.Unroll

class ValidatorSemanticSpec extends ValidatorBaseSpec {
    @Unroll
    private def "#url.getFile() should pass validation"() {
        when:
        List<EthqlProcessingError> errors = validator.analyzeScript(url.getFile())

        then:
        noExceptionThrown()
        errors.size() == 0

        where:
        url << SampleUtils.getAllResources()
    }

    private def "Validator should throw exception when file not exists"() {
        when:
        validator.analyzeScript('notExist.ethql')

        then:
        EthqlProcessingException e = thrown()
        e.getMessage() == '''Invalid file path: 'notExist.ethql'.'''
    }

    def "variable"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | string a = "";
        | a = "new string";
        |
        | string a = "";
        | b = "";
        """.stripMargin()                   | ["Variable 'a' is already defined.",
                                               "Variable 'b' not defined."]
    }

    @Unroll
    def "type #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        "int[] a = [];"                     | []
        "int a = 0x876;"                    | ["Cannot assign a byte value to a int variable."]
        "string a = 5;"                     | ["Cannot assign a int value to a string variable."]
        "uint a = -15;"                     | ["out of range"]
        "int8 a = 260"                      | ["out of range"]
    }

    def "method"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        "connect(\"localhost:8465\");"      | []
        "int result = add(10, -5);"         | []
        "connec(\"localhost:8465\");"       | ["Method 'connec' with parameters 'string' unknown."]
        "connect(\"localhost:8465\", 5);"   | ["Method 'connect' with parameters 'string, int' unknown."]
        "connect(5);"                       | ["Method 'connect' with parameters 'int' unknown."]
        """
        | string result = contains(
        |   [0x931D387731bBbC988B312206c74F77D004D6B84c],
        |   0x931D387731bBbC988B312206c74F77D004D6B84c
        | );
        """.stripMargin()                   | ["Cannot assign a bool value to a string variable."]
        """
        | int result = mapValue(
        |   4,
        |   "unknown",
        |   [0,1,2,3],
        |   ["first", "second", "third", "fourth"]
        | );
        """.stripMargin()                   | ["Cannot assign a string value to a int variable."]
    }

    @Unroll
    def "emit #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | EMIT LOG LINE (
        |   "Block ",
        |   5,
        |   ": New FeeToken registered with address '",
        |   0x123,
        |   "'."
        | );
        """.stripMargin()                   | []
        """
        | EMIT CSV ROW ("table")
        |   (5 AS blockNumber, 0x123 AS addr);
        """.stripMargin()                   | []
        """
        | int kittyId = 15;
        | EMIT XES EVENT
        | ()(kittyId)()("birth" AS xs:string concept:name);
        """.stripMargin()                   | []
        """
        | EMIT LOG LINE (
        |   "Block ",
        |   block.number,
        |   ": New FeeToken registered with address '",
        |   0x123,
        |   "'."
        | );
        """.stripMargin()                   | ["Variable 'block.number' not defined."]
        "EMIT CSV ROW (tableName) (5);"     | ["Variable 'tableName' not defined.",
                                               "Attribute name must be specified for literals"]
        """
        | EMIT XES EVENT
        | ()(catId)()(birth AS xs:string concept:name);
        """.stripMargin()                   | ["catId not defined", "birth not defined"]
    }

    @Unroll
    def "block filter #script"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                              | expectedErr
        """
        | BLOCKS (6605100) (6615100) {
        |   TRANSACTIONS (ANY) (ANY) {}
        | }
        """.stripMargin()                   | []
        """
        | BLOCKS (Earliest) (cuRRent) {
        |   TRANSACTIONS (ANY) (ANY) {}
        | }
        """.stripMargin()                   | []
        "BLOCKS (var1) (8) {}"              | ["Variable 'var1' not defined."]
        "BLOCKS (10) (8) {}"                | ["10 greater than 8"]
        "BLOCKS (-10) (-8) {}"              | ["negative block number"]
        "BLOCKS (\"123\") (0x123) {}"       | ["type not int"]
        """
        | TRANSACTIONS (ANY) (ANY) {
        |   BLOCKS (0) (1) {}
        | }
        """.stripMargin()                   | ["Invalid nesting of filters.",
                                               "Invalid nesting of filters."]
    }

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
        | int i = 5;
        | bool b = true;
        | if (
        |   (i == 5) &&
        |   (true == (5 == 4)) &&
        |   !b ||
        |   (true && i in [5, 3])
        | ) {}
        """.stripMargin()                   | []
        """
        | int i = 5;
        | if (i > 0) {}
        """.stripMargin()                   | []
        "if (true == (5 == 4)) {}"          | []
        "if (true && i in [5, 3]) {}"       | ["variable i not defined."]
        "if (5 && \"true\") {}"             | ["Expression must return a boolean value.",
                                               "Expression must return a boolean value."]
        "if (i in \"[5,3]\") {}"            | ["type mismatch"]
    }

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
        |   (address addr, int i, string s = someMethod(int[] [5, 6])) {}
        |   address addr = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        |   SMART CONTRACT (addr)
        |   (int i, string s = someMethod(int[] [5, 6])) {}
        | }
        """.stripMargin()                   | []
        """
        | BLOCKS (6605100) (6615100) {
        |   address addr = 0x931D387731bBbC988B312206c74F77D004D6B84c;
        |   SMART CONTRACT (addr)
        |   (int i, string s = someMethod(int[] ["5", "6"])) {}
        | }
        """.stripMargin()                   | ["Cannot cast string[] literal to int[]."]
    }

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
        | uint authorId = 15;
        """.stripMargin()                   | []
        """
        | LOG ENTRIES
        | (contract)
        | (GitCommit(
        |   uint authorId,
        |   bytes32 sha
        | )) {}
        """.stripMargin()                   | ["Invalid nesting of filters.",
                                               "Variable 'contract' not defined."]
    }
}
