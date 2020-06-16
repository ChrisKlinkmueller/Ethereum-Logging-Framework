package au.csiro.data61.aap.elf.configuration

import au.csiro.data61.aap.elf.core.Instruction
import au.csiro.data61.aap.elf.core.VariableAssignment
import au.csiro.data61.aap.elf.core.filters.TransactionFilter
import au.csiro.data61.aap.elf.configuration.SpecificationComposer.FactoryState
import spock.lang.Specification

class SpecificationComposerSpec extends Specification {
    SpecificationComposer composer = new SpecificationComposer()

    def "prepareProgramBuild should throw exception when states not empty"() {
        when: 'build a program with empty states'
        composer.prepareProgramBuild()
        then:
        notThrown(BuildException)
        checkStates(composer, [FactoryState.PROGRAM])
        checkInstructions(composer, [[]])

        when: 'build a program when another program is being build'
        composer.prepareProgramBuild()
        then:
        BuildException e = thrown()
        e.getMessage() == 'A program can only be build when no other filter is being build.'
    }

    def "prepareTransactionFilterBuild should throw exception when current states not match"() {
        given:
        composer.prepareProgramBuild()

        when: 'build a transaction filter when current state is PROGRAM'
        composer.prepareTransactionFilterBuild()
        then:
        BuildException e = thrown()
        e.getMessage() == 'A transaction filter cannot be added to program, but only to: block range filter.'

        when: 'build a transaction filter when current state is BLOCK_RANGE_FILTER'
        composer.prepareBlockRangeBuild()
        composer.prepareTransactionFilterBuild()
        then:
        notThrown(BuildException)
        checkStates(composer, [FactoryState.PROGRAM, FactoryState.BLOCK_RANGE_FILTER, FactoryState.TRANSACTION_FILTER])
        checkInstructions(composer, [[], [], []])
    }

    def "buildTransactionFilter should build a transaction filter"() {
        given:
        composer.prepareProgramBuild()
        composer.prepareBlockRangeBuild()
        composer.prepareTransactionFilterBuild()

        when:
        composer.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny())

        then:
        checkStates(composer, [FactoryState.PROGRAM, FactoryState.BLOCK_RANGE_FILTER])
        checkInstructions(composer, [[], [TransactionFilter]])
    }

    def "addInstruction should add an instruction"() {
        when:
        composer.prepareProgramBuild()
        composer.prepareGenericFilterBuild()
        then:
        checkStates(composer, [FactoryState.PROGRAM, FactoryState.GENERIC_FILTER])
        checkInstructions(composer, [[],[]])

        when:
        composer.addInstruction(ValueAssignmentSpecification.of(
                ValueMutatorSpecification.ofVariableName("var"),
                ValueAccessorSpecification.ofVariable("var")
        ))
        then:
        checkStates(composer, [FactoryState.PROGRAM, FactoryState.GENERIC_FILTER])
        checkInstructions(composer, [[],[VariableAssignment]])
    }

    static def checkStates(SpecificationComposer composer, List<FactoryState> states) {
        Stack<FactoryState> stack = new Stack()
        for (state in states) stack.push(state)
        composer.states == stack
    }

    static def checkInstructions(SpecificationComposer composer, List<List<Class>> instructionTypes) {
        def instructions = composer.instructions
        assert instructions.size() == instructionTypes.size()
        instructions.eachWithIndex { List<Instruction> entry, int i ->
            assert entry.size() == instructionTypes[i].size()
            entry.eachWithIndex { Instruction instruction, int j ->
                assert instruction.getClass() == instructionTypes[i][j]
            }
        }
    }
}
