package blf.configuration


import blf.Validator
import blf.BcqlProcessingException
import blf.EthqlProcessingResult
import blf.core.filters.Program
import blf.parsing.VariableExistenceListener
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import spock.lang.Specification

class EthereumListenerSpec extends Specification {
    EthereumListener composer = new EthereumListener(Mock(VariableExistenceListener))

    /*
    Fails
    @Unroll
    def "#condition should return true"() {
        given:
        ProgramState state = new ProgramState()
        Program p = program("""
            | IF ($condition) {
            |   string s = "";
            | }""".stripMargin(), composer)

        when:
        p.executeInstructions(state)
        then:
        state.getValueStore().containsName("s")

        where:
        condition << [
            "true",
            "!false",
            "1 != 2",
            "true == true",
            "(1 < 2) && (3 >= 2)",
            "5 in {12,5}",
            "5 in {12,5} || 2 in {12,5}",
            "(2 > 3) || (5 <= 10)",
            "true && true",
            "false || true",
            "true && (1 == 1)",
            "false || !(1 == 2)",
            "false == (1 > 2)"
        ]
    }
    */

    static Program program(String script, EthereumListener composer) {
        Validator validator = new Validator()
        EthqlProcessingResult<ParseTree> result = validator.parseScript(new ByteArrayInputStream(script.getBytes()))
        assert result.isSuccessful()

        ParseTree tree = result.getResult()

        ParseTreeWalker walker = new ParseTreeWalker()
        walker.walk(composer, tree)

        if (composer.containsError()) {
            throw new BcqlProcessingException("Error when configuring the data extraction.", composer.getError())
        }

        composer.getProgram()
    }
}
