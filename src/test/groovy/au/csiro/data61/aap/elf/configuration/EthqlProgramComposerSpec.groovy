package au.csiro.data61.aap.elf.configuration

import au.csiro.data61.aap.elf.EthqlProcessingException
import au.csiro.data61.aap.elf.EthqlProcessingResult
import au.csiro.data61.aap.elf.Validator
import au.csiro.data61.aap.elf.core.ProgramState
import au.csiro.data61.aap.elf.core.filters.PublicMemberQuery
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer
import au.csiro.data61.aap.elf.configuration.BlockNumberSpecification.Type
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.web3j.abi.datatypes.Address
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.IntStream

class EthqlProgramComposerSpec extends Specification {
    SpecificationComposer specComposer = Mock(SpecificationComposer)
    EthqlProgramComposer composer = new EthqlProgramComposer(specComposer, new VariableExistenceAnalyzer())

    @Unroll
    def "composer should delegate spec composer to build block filter from #from to #to"() {
        when:
        compose(composer, """
        | BLOCKS ($from) ($to) {}
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.prepareBlockRangeBuild()
        then:
        1 * specComposer.buildBlockRange({ BlockNumberSpecification spec ->
            if (fromValue != null) spec.getValueAccessor().getValue(new ProgramState()) == fromValue
            spec.getType() == fromType
        }, { BlockNumberSpecification spec ->
            if (toValue != null) spec.getValueAccessor().getValue(new ProgramState()) == toValue
            spec.getType() == toType
        })
        then:
        1 * specComposer.buildProgram()

        where:
        from       | to        | fromType      | fromValue | toType       | toValue
        0          | 6         | Type.NUMBER   | 0         | Type.NUMBER  | 6
        // use null to skip value check, because values are not yet available during program build
        "EARLIEST" | "CURRENT" | Type.EARLIEST | null      | Type.CURRENT | null
    }

    @Unroll
    def "composer should delegate spec composer to build transaction filter from #from to #to"() {
        when:
        compose(composer, """
        | BLOCKS (0) (6) {
        |  TRANSACTIONS (${String.join(",", from)}) (${String.join(",", to)}) {}
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
            for (address in from) spec.addressCheck.test(new ProgramState(), address)
        }, { AddressListSpecification spec ->
            for (address in to) spec.addressCheck.test(new ProgramState(), address)
        })
        then:
        1 * specComposer.buildBlockRange(_, _)
        then:
        1 * specComposer.buildProgram()

        where:
        from                                           | to
        ["0x931D387731bBbC988B312206c74F77D004D6B84c"] | ["ANY"]
        ["0x931D387731bBbC988B312206c74F77D004D6B84a"] | ["0x931D387731bBbC988B312206c74F77D004D6B84c",
                                                          "0x931D387731bBbC988B312206c74F77D004D6B84b"]
    }

    @Unroll
    def "composer should delegate spec composer to build log entry filter at #addrs"() {
        when:
        compose(composer, """
        | BLOCKS (0) (6) {
        |   LOG ENTRIES (${String.join(",", addrs)}) ($signature) {}
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
            for (addr in addrs) spec.addressCheck.test(new ProgramState(), addr)
        }, { LogEntrySignatureSpecification spec ->
            spec.getSignature().getName() == name
            IntStream.range(0, paramTypes.size()).forEach(idx -> {
                spec.getSignature().getParameter(idx).name == paramNames[idx]
                spec.getSignature().getParameter(idx).type.type == paramTypes[idx]
            })
        })
        then:
        1 * specComposer.buildBlockRange(_, _)
        then:
        1 * specComposer.buildProgram()

        where:
        addrs                                          | signature                | name       | paramTypes | paramNames
        ["0x931D387731bBbC988B312206c74F77D004D6B84c"] | "Transfer(address from)" | "Transfer" | [Address]  | ["from"]
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
        |   SMART CONTRACT ($addr) ($query) {}
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
            spec.getContractAddress().getValue(new ProgramState()) == addr
            PublicMemberQuery query = spec.getQueries().head()
            query.memberName == memberName
        })
        then:
        1 * specComposer.buildBlockRange(_, _)
        then:
        1 * specComposer.buildProgram()

        where:
        addr                                         | query                                        | memberName
        "0x931D387731bBbC988B312206c74F77D004D6B84c" | "address johnAddr = lookup(string \"John\")" | "lookup"
    }

    def "composer should delegate spec composer for value assignment"() {
        when:
        compose(composer, """
        | string s = "some string";
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.addInstruction({ ValueAssignmentSpecification spec ->
            spec.getInstruction().valueAccessor.getValue(new ProgramState()) == "some string"
        })
        then:
        1 * specComposer.buildProgram()
    }

    def "composer should delegate spec composer for csv emit"() {
        when:
        compose(composer, """
        | EMIT CSV ROW ("$tableName") ($emitVars);
        """.stripMargin())
        then:
        1 * specComposer.prepareProgramBuild()
        then:
        1 * specComposer.addInstruction({ CsvExportSpecification spec ->
            spec.getInstruction().tableName.getValue() == tableName
            spec.getInstruction().columns*.getName() == columnNames
            spec.getInstruction().columns*.getAccessor()*.getValue() == columnValues
        })
        then:
        1 * specComposer.buildProgram()

        where:
        tableName | emitVars                              | columnNames       | columnValues
        "commits" | """"John" as author, 0x1234 as sha""" | ["author", "sha"] | ["John", "0x1234"]
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
