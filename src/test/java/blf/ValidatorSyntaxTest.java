package blf;

import blf.parsing.BcqlInterpreter;
import blf.parsing.ErrorCollector;
import blf.parsing.SemanticAnalysis;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.util.List;

class ValidatorSyntaxTest {

    static private BcqlInterpreter interpreter;
    final static String p =
        "SET BLOCKCHAIN \"Ethereum\"; \nSET OUTPUT FOLDER \"./test_output\";\nSET CONNECTION \"ws://localhost:0000/\"; ";

    @BeforeAll
    static void setup() {
        SemanticAnalysis noOpAnalyser = Mockito.mock(SemanticAnalysis.class);
        Mockito.doNothing().when(noOpAnalyser).analyze(Mockito.mock(ParseTree.class));
        interpreter = new BcqlInterpreter(new ErrorCollector(), noOpAnalyser);
    }

    private List<BcqlProcessingError> validateSyntax(String script) {
        return interpreter.parseDocument(new ByteArrayInputStream(script.getBytes())).getErrors();
    }

    @ParameterizedTest
    @ValueSource(strings = { p + "/**/", p + "/*\n*/", p + "/* some comments\n*/", p + "//", p + "// some comments" })
    void testComment(String commentString) {
        Assertions.assertTrue(validateSyntax(commentString).isEmpty());
    }
}
