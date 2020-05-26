package au.csiro.data61.aap.elf

class ValidatorSyntaxSpec extends ValidatorBaseSpec {
    def "comment"() {
        expect:
        validate(script, validator).size() == 0

        where:
        script << [
            "/**/",
            """
            | /*
            | */
            """.stripMargin(),
            "//",
            """
            | /* some comments
            | */
            """.stripMargin(),
            "//  some comments"
        ]
    }

    def "identifier"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                      | expectedErr
        "int crypto_kitties8 = 0;"  | []
        "int 变量 = 0;"              | []
        "int _private^ = 0;"        | ["token recognition error at: '^'"]
        "int 8crypto_kitties = 0;"  | ["extraneous input '8' expecting Identifier"]
    }

    def "type"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                      | expectedErr
        "string a = \"\";"          | []
        "uint32 a = 5;"             | []
        "bytes a = 0x0;"            | []
        "int[] a = [0];"            | []
        "String d = \"\";"          | ["no viable alternative at input 'Stringd'"]
    }

    def "literal"() {
        expect:
        List<EthqlProcessingError> errors = validate(script, validator)
        errors*.getErrorMessage() == expectedErr

        where:
        script                      | expectedErr
        "string a = \"string\";"    | []
        "string a = \"\\\\\";"      | []
        "string a = \"\";"          | []
        "int a = -5;"               | []
        "bool a = TrUe;"            | []
        "bool a = fAlSe;"           | []
        "bytes a = 0x2d8f6c;"       | []
        "string[] a = [];"          | []
        "int a = [5,-9,10];"        | []
        "string a = 'string\";"     | ["token recognition error at: '''",
                                       "token recognition error at: '\";'",
                                       "mismatched input 'string' expecting {'[', STRING_LITERAL, INT_LITERAL, BOOLEAN_LITERAL, BYTES_LITERAL, Identifier}",
                                       "mismatched input '<EOF>' expecting Identifier"]
        "int[] a = {-5,9,10};"      | ["extraneous input '{' expecting {'[', STRING_LITERAL, INT_LITERAL, BOOLEAN_LITERAL, BYTES_LITERAL, Identifier}",
                                       "mismatched input ',' expecting ';'"]
    }
}
