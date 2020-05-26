package au.csiro.data61.aap.elf.configuration

import au.csiro.data61.aap.elf.EthqlProcessingException
import au.csiro.data61.aap.elf.EthqlProcessingResult
import au.csiro.data61.aap.elf.Validator
import au.csiro.data61.aap.elf.core.ProgramState
import au.csiro.data61.aap.elf.core.filters.Program
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import spock.lang.Specification
import spock.lang.Unroll

class EthqlProgramComposerSpec extends Specification {
    EthqlProgramComposer composer = new EthqlProgramComposer(Mock(VariableExistenceAnalyzer))

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
            "(2 > 3) || (5 <= 10)",
            "true && true",
            "false || true",
            "true && (1 == 1)",
            "false || !(1 == 2)",
            "false == (1 > 2)"
        ]
    }

    static Program program(String script, EthqlProgramComposer composer) {
        Validator validator = new Validator()
        EthqlProcessingResult<ParseTree> result = validator.parseScript(new ByteArrayInputStream(script.getBytes()))
        assert result.isSuccessful()

        ParseTree tree = result.getResult()

        ParseTreeWalker walker = new ParseTreeWalker()
        walker.walk(composer, tree)

        if (composer.containsError()) {
            throw new EthqlProcessingException("Error when configuring the data extraction.", composer.getError())
        }

        composer.getProgram()
    }
}
