package au.csiro.data61.aap.elf.configuration

import au.csiro.data61.aap.elf.EthqlProcessingException
import au.csiro.data61.aap.elf.EthqlProcessingResult
import au.csiro.data61.aap.elf.Validator
import au.csiro.data61.aap.elf.core.ProgramState
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.web3j.abi.datatypes.Address
import spock.lang.Specification
import spock.lang.Unroll

class EthqlProgramComposerSpec extends Specification {
    SpecificationComposer specComposer = Mock(SpecificationComposer)
    EthqlProgramComposer composer = new EthqlProgramComposer(specComposer, new VariableExistenceAnalyzer())

    def "composer should delegate spec composer to build block filter"() {
        when:
        compose(composer, """
        | BLOCKS (0) (Current) {}
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.prepareBlockRangeBuild()
        then:
        1 * specComposer.buildBlockRange({ BlockNumberSpecification spec ->
            spec.getValueAccessor().getValue(new ProgramState()) == 0
        }, { BlockNumberSpecification spec ->
            spec.getType() == BlockNumberSpecification.Type.CURRENT
        })
        then:
        1 * specComposer.buildProgram()
    }

    def "composer should delegate spec composer to build transaction filter"() {
        when:
        compose(composer, """
        | BLOCKS (0) (6) {
        |  TRANSACTIONS (0x931D387731bBbC988B312206c74F77D004D6B84c) (ANY) {}
        | }
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.prepareBlockRangeBuild()
        then:
        1 * specComposer.prepareTransactionFilterBuild()
        then:
        1 * specComposer.buildTransactionFilter({ AddressListSpecification spec ->
            spec.addressCheck.test(new ProgramState(), "0x931D387731bBbC988B312206c74F77D004D6B84c")
        }, { AddressListSpecification spec ->
            spec.addressCheck.test(new ProgramState(), "any string")
        })
        then:
        1 * specComposer.buildBlockRange(_, _)
        then:
        1 * specComposer.buildProgram()
    }

    def "composer should delegate spec composer to build log entry filter"() {
        when:
        compose(composer, """
        | BLOCKS (0) (6) {
        |   LOG ENTRIES (0x931D387731bBbC988B312206c74F77D004D6B84c) (Transfer(address from)) {}
        | }
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.prepareBlockRangeBuild()
        then:
        1 * specComposer.prepareLogEntryFilterBuild()
        then:
        1 * specComposer.buildLogEntryFilter({ AddressListSpecification spec ->
            spec.addressCheck.test(new ProgramState(), "0x931D387731bBbC988B312206c74F77D004D6B84c")
        }, { LogEntrySignatureSpecification spec ->
            spec.getSignature().getName() == "Transfer"
            spec.getSignature().getParameter(0).name == "from"
            spec.getSignature().getParameter(0).type.type == Address
        })
        then:
        1 * specComposer.buildBlockRange(_, _)
        then:
        1 * specComposer.buildProgram()
    }

    @Unroll
    def "composer should delegate spec composer to build generic filter with condition: #condition"() {
        when:
        compose(composer, """
        | IF ($condition) {}
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.prepareGenericFilterBuild()
        then:
        1 * specComposer.buildGenericFilter({ GenericFilterPredicateSpecification spec ->
            spec.getPredicate().test(new ProgramState())
        })
        then:
        1 * specComposer.buildProgram()

        where:
        condition << [
                "true",
                "!false",
                "1 != 2",
                "true == true",
                "(1 < 2) && (3 >= 2)",
                "5 in [12,5] || 2 in [12,5]",
                "(2 > 3) || (5 <= 10)",
                "true && true",
                "false || true",
                "true && (1 == 1)",
                "false || !(1 == 2)",
                "false == (1 > 2)"
        ]
    }

    def "composer should delegate spec composer to build smart contract filter"() {
        when:
        compose(composer, """
        | BLOCKS (0) (6) {
        |   SMART CONTRACT (0x931D387731bBbC988B312206c74F77D004D6B84c)
        |                  (address johnAddr = lookup(string \"John\")) {}
        | }
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.prepareBlockRangeBuild()
        then:
        1 * specComposer.prepareSmartContractFilterBuild()
        then:
        1 * specComposer.buildSmartContractFilter({ SmartContractFilterSpecification spec ->
            spec.getContractAddress().getValue(new ProgramState()) == "0x931D387731bBbC988B312206c74F77D004D6B84c"
            spec.getQueries().head().memberName == "lookup"
        })
        then:
        1 * specComposer.buildBlockRange(_, _)
        then:
        1 * specComposer.buildProgram()
    }

    def "composer should delegate spec composer for value assignment"() {
        when:
        compose(composer, """
        | string s = "some string";
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.addInstruction(_ as ValueAssignmentSpecification)
        then:
        1 * specComposer.buildProgram()
    }

    def "composer should delegate spec composer for csv emit"() {
        when:
        compose(composer, """
        | EMIT CSV ROW ("commits") ("John" as author, 0x1234 as sha);
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.addInstruction({ CsvExportSpecification spec ->
            spec.getInstruction().tableName.getValue() == "commits"
            spec.getInstruction().columns*.getName() == ["author", "sha"]
            spec.getInstruction().columns*.getAccessor()*.getValue() == ["John", "0x1234"]
        })
        then:
        1 * specComposer.buildProgram()
    }

    def static compose(EthqlProgramComposer composer, String script) {
        Validator validator = new Validator()
        EthqlProcessingResult<ParseTree> result = validator.parseScript(new ByteArrayInputStream(script.getBytes()))
        assert result.isSuccessful()

        ParseTree tree = result.getResult()

        ParseTreeWalker walker = new ParseTreeWalker()
        walker.walk(composer, tree)

        if (composer.containsError()) {
            throw new EthqlProcessingException("Error when configuring the data extraction.", composer.getError())
        }
    }
}
