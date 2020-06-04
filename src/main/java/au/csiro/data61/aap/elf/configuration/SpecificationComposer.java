package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.configuration.BlockNumberSpecification.Type;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.filters.BlockFilter;
import au.csiro.data61.aap.elf.core.filters.GenericFilter;
import au.csiro.data61.aap.elf.core.filters.LogEntryFilter;
import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.core.filters.SmartContractFilter;
import au.csiro.data61.aap.elf.core.filters.TransactionFilter;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.values.ValueMutator;

/**
 * SpecificationComposer
 */
public class SpecificationComposer {
    private final Stack<FactoryState> states;
    private final Stack<List<Instruction>> instructions;

    public SpecificationComposer() {
        this.instructions = new Stack<>();
        this.states = new Stack<>();
    }

    public void prepareProgramBuild() throws BuildException {
        this.prepareBuild(FactoryState.PROGRAM);
    }

    public void prepareBlockRangeBuild() throws BuildException {
        this.prepareBuild(FactoryState.BLOCK_RANGE_FILTER, FactoryState.PROGRAM);
    }

    public void prepareTransactionFilterBuild() throws BuildException {
        this.prepareBuild(FactoryState.TRANSACTION_FILTER, FactoryState.BLOCK_RANGE_FILTER);
    }

    public void prepareLogEntryFilterBuild() throws BuildException {
        this.prepareBuild(FactoryState.LOG_ENTRY_FILTER, FactoryState.BLOCK_RANGE_FILTER, FactoryState.TRANSACTION_FILTER);
    }

    public void prepareSmartContractFilterBuild() throws BuildException {
        this.prepareBuild(FactoryState.SMART_CONTRACT_FILTER, FactoryState.BLOCK_RANGE_FILTER);
    }

    public void prepareGenericFilterBuild() throws BuildException {
        this.prepareBuild(
            FactoryState.GENERIC_FILTER,
            FactoryState.BLOCK_RANGE_FILTER,
            FactoryState.TRANSACTION_FILTER,
            FactoryState.LOG_ENTRY_FILTER,
            FactoryState.SMART_CONTRACT_FILTER,
            FactoryState.PROGRAM
        );
    }

    private void prepareBuild(FactoryState newState, FactoryState... possibleCurrentStates) throws BuildException {
        final boolean areStatesEmpty = this.states.isEmpty() && possibleCurrentStates.length == 0;
        final boolean currentStatesMatches = !this.states.isEmpty()
            && Arrays.stream(possibleCurrentStates).anyMatch(s -> this.states.peek() == s);

        if (!(areStatesEmpty || currentStatesMatches)) {
            throw new BuildException(
                possibleCurrentStates.length == 0
                    ? String.format("A %s can only be build when no other filter is being build.", newState)
                    : String.format(
                        "A %s cannot be added to %s, but only to: %s.",
                        newState,
                        this.states.peek(),
                        Arrays.stream(possibleCurrentStates).map(state -> state.toString()).collect(Collectors.joining(", "))
                    )
            );
        }

        this.states.push(newState);
        this.instructions.add(new LinkedList<>());
    }

    public Program buildProgram() throws BuildException {
        if (this.states.peek() != FactoryState.PROGRAM) {
            throw new BuildException(
                String.format("Cannot build a program, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final Program program = new Program(this.instructions.peek());
        this.closeScope(program);
        return program;
    }

    public void buildBlockRange(BlockNumberSpecification fromBlock, BlockNumberSpecification toBlock) throws BuildException {
        assert fromBlock != null && fromBlock.getType() != Type.CONTINUOUS;
        assert toBlock != null && toBlock.getType() != Type.EARLIEST;

        if (this.states.peek() != FactoryState.BLOCK_RANGE_FILTER) {
            throw new BuildException(
                String.format("Cannot build a block filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final BlockFilter blockRange = new BlockFilter(fromBlock.getValueAccessor(), toBlock.getStopCriterion(), this.instructions.peek());

        this.closeScope(blockRange);
    }

    public void buildTransactionFilter(AddressListSpecification senders, AddressListSpecification recipients) throws BuildException {
        assert senders != null;
        assert recipients != null;

        if (this.states.peek() != FactoryState.TRANSACTION_FILTER) {
            throw new BuildException(
                String.format("Cannot build a transaction filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final TransactionFilter filter = new TransactionFilter(
            senders.getAddressCheck(),
            recipients.getAddressCheck(),
            this.instructions.peek()
        );

        this.closeScope(filter);
    }

    public void buildLogEntryFilter(AddressListSpecification contracts, LogEntrySignatureSpecification signature) throws BuildException {
        assert contracts != null;
        assert signature != null;

        if (this.states.peek() != FactoryState.LOG_ENTRY_FILTER) {
            throw new BuildException(
                String.format("Cannot build a log entry filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final LogEntryFilter filter = new LogEntryFilter(contracts.getAddressCheck(), signature.getSignature(), this.instructions.peek());
        this.closeScope(filter);
    }

    public void buildGenericFilter(GenericFilterPredicateSpecification predicate) throws BuildException {
        assert predicate != null;

        if (this.states.peek() != FactoryState.GENERIC_FILTER) {
            throw new BuildException(
                String.format("Cannot build a generic filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final GenericFilter filter = new GenericFilter(predicate.getPredicate(), this.instructions.peek());
        this.closeScope(filter);
    }

    public void buildSmartContractFilter(SmartContractFilterSpecification specification) throws BuildException {
        assert specification != null;

        if (this.states.peek() != FactoryState.SMART_CONTRACT_FILTER) {
            throw new BuildException(
                String.format("Cannot build a smart contract filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final SmartContractFilter filter = new SmartContractFilter(
            specification.getContractAddress(),
            specification.getQueries(),
            this.instructions.peek()
        );
        this.closeScope(filter);
    }

    private void closeScope(Instruction instruction) {
        this.instructions.pop();
        if (!this.instructions.isEmpty()) {
            this.instructions.peek().add(instruction);
        }
        this.states.pop();
    }

    public void addInstruction(InstructionSpecification<?> instruction) throws BuildException {
        if (instruction == null) {
            throw new BuildException(String.format("Parameter instruction is null."));
        }
        this.instructions.peek().add(instruction.getInstruction());
    }

    public void addVariableAssignment(ValueMutatorSpecification variable, ValueAccessorSpecification value) {
        final Instruction variableAssignment = this.createVariableAssignment(variable.getMutator(), value.getValueAccessor());
        this.instructions.peek().add(variableAssignment);
    }

    private Instruction createVariableAssignment(ValueMutator variable, ValueAccessor valueAccessor) {
        return state -> {
            final Object value = valueAccessor.getValue(state);
            variable.setValue(value, state);
        };
    }

    private static enum FactoryState {
        PROGRAM,
        BLOCK_RANGE_FILTER,
        TRANSACTION_FILTER,
        LOG_ENTRY_FILTER,
        SMART_CONTRACT_FILTER,
        GENERIC_FILTER;

        @Override
        public String toString() {
            switch (this) {
                case PROGRAM:
                    return "program";
                case BLOCK_RANGE_FILTER:
                    return "block range filter";
                case TRANSACTION_FILTER:
                    return "transaction filter";
                case LOG_ENTRY_FILTER:
                    return "log entry filter";
                case SMART_CONTRACT_FILTER:
                    return "smart contract filter";
                case GENERIC_FILTER:
                    return "generic filter";
                default:
                    throw new IllegalArgumentException(String.format("FactoryState constant '%s' unknown.", this));
            }
        }
    }

}
