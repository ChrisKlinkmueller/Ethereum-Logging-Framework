package blf

import blf.parsing.ErrorCollector
import blf.parsing.BcqlInterpreter
import blf.parsing.SemanticAnalysis
import org.antlr.v4.runtime.tree.ParseTree
import spock.lang.Specification

class ValidatorSyntaxSpec extends Specification {
    // we don't want to detect semantic errors while testing syntax validator
    // so here we mock a no-op semantic analyser
    SemanticAnalysis noOpAnalyser = Stub(SemanticAnalysis) {
        analyze(_ as ParseTree) >> {}
    }
    BcqlInterpreter interpreter = new BcqlInterpreter(new ErrorCollector(), noOpAnalyser)

    static List<BcqlProcessingError> validateSyntax(String script, BcqlInterpreter interpreter) {
        interpreter.parseDocument(new ByteArrayInputStream(script.getBytes())).getErrors()
    }

    def "comment"() {
        expect:
        validateSyntax(script, interpreter).size() == 0

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

    /*
    Fails
    def "identifier"() {
        expect:
        List<EthqlProcessingError> errors = validateSyntax(script, interpreter)
        errors*.getErrorMessage() == expectedErr

        where:
        script                      | expectedErr
        "int crypto_kitties = 0;"   | []
        // "int cryptoKitties8 = 0;"   | []
        """
        | int cryptoKitties8 = 0;
        | cryptoKitties8 = 10;
        """.stripMargin()           | []
        "int 变量 = 0;"              | []
        "int private^ = 0;"         | ["token recognition error at: '^'"]
        "int 8cryptoKitties = 0;"   | ["extraneous input '8' expecting Identifier"]
    }
    */

    /*
    Fails
    def "type"() {
        expect:
        List<EthqlProcessingError> errors = validateSyntax(script, interpreter)
        errors*.getErrorMessage() == expectedErr

        where:
        script                      | expectedErr
        "string a = \"\";"          | []
        "uint32 a = 5;"             | []
        "bytes a = 0x0;"            | []
        "int[] a = [0];"            | []
        "String d = \"\";"          | ["no viable alternative at input 'Stringd'"]
    }
    */

    /*
    Fails
    def "literal"() {
        expect:
        List<EthqlProcessingError> errors = validateSyntax(script, interpreter)
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
        "string[] a = newStringArray();" | []
        "int a = [5,-9,10];"        | []
        "string a = 'string\";"     | ["token recognition error at: '''",
                                       "token recognition error at: '\";'",
                                       "mismatched input 'string' expecting {'[', STRING_LITERAL, INT_LITERAL, BOOLEAN_LITERAL, BYTES_LITERAL, Identifier}",
                                       "mismatched input '<EOF>' expecting Identifier"]
        "int[] a = {-5,9,10};"      | ["extraneous input '{' expecting {'[', STRING_LITERAL, INT_LITERAL, BOOLEAN_LITERAL, BYTES_LITERAL, Identifier}",
                                       "mismatched input ',' expecting ';'"]
    }
    */
}
