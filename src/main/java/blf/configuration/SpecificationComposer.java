package blf.configuration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import blf.core.interfaces.Instruction;
import blf.blockchains.ethereum.instructions.EthereumBlockFilterInstruction;
import blf.core.instructions.GenericFilterInstruction;
import blf.blockchains.ethereum.instructions.EthereumLogEntryFilterInstruction;
import blf.core.Program;
import blf.blockchains.ethereum.instructions.EthereumSmartContractFilterInstruction;
import blf.blockchains.ethereum.instructions.EthereumTransactionFilterInstruction;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;
import io.reactivex.annotations.NonNull;

/**
 * SpecificationComposer
 */
public class SpecificationComposer {
    public final Stack<FactoryState> states;
    public final Stack<List<Instruction>> instructionListsStack;

    public SpecificationComposer() {
        this.instructionListsStack = new Stack<>();
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
                        Arrays.stream(possibleCurrentStates).map(FactoryState::toString).collect(Collectors.joining(", "))
                    )
            );
        }

        this.states.push(newState);
        this.instructionListsStack.add(new LinkedList<>());
    }

    public Program buildProgram() throws BuildException {
        if (this.states.peek() != FactoryState.PROGRAM) {
            throw new BuildException(
                String.format("Cannot build a program, when construction of %s has not been finished.", this.states.peek())
            );
        }

        return new Program(this.instructionListsStack.peek());
    }

    public void buildTransactionFilter(@NonNull AddressListSpecification senders, @NonNull AddressListSpecification recipients)
        throws BuildException {

        if (this.states.peek() != FactoryState.TRANSACTION_FILTER) {
            throw new BuildException(
                String.format("Cannot build a transaction filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final EthereumTransactionFilterInstruction filter = new EthereumTransactionFilterInstruction(
            senders.getAddressCheck(),
            recipients.getAddressCheck(),
            this.instructionListsStack.peek()
        );

        this.closeScope(filter);
    }

    public void buildLogEntryFilter(@NonNull AddressListSpecification contracts, @NonNull LogEntrySignatureSpecification signature)
        throws BuildException {

        if (this.states.peek() != FactoryState.LOG_ENTRY_FILTER) {
            throw new BuildException(
                String.format("Cannot build a log entry filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final EthereumLogEntryFilterInstruction filter = new EthereumLogEntryFilterInstruction(
            contracts.getAddressCheck(),
            signature.getSignature(),
            this.instructionListsStack.peek()
        );
        this.closeScope(filter);
    }

    public void buildGenericFilter(@NonNull GenericFilterPredicateSpecification predicate) throws BuildException {

        if (this.states.peek() != FactoryState.GENERIC_FILTER) {
            throw new BuildException(
                String.format("Cannot build a generic filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final GenericFilterInstruction filter = new GenericFilterInstruction(predicate.getPredicate(), this.instructionListsStack.peek());
        this.closeScope(filter);
    }

    public void buildSmartContractFilter(@NonNull SmartContractFilterSpecification specification) throws BuildException {

        if (this.states.peek() != FactoryState.SMART_CONTRACT_FILTER) {
            throw new BuildException(
                String.format("Cannot build a smart contract filter, when construction of %s has not been finished.", this.states.peek())
            );
        }

        final EthereumSmartContractFilterInstruction filter = new EthereumSmartContractFilterInstruction(
            specification.getContractAddress(),
            specification.getQueries(),
            this.instructionListsStack.peek()
        );
        this.closeScope(filter);
    }

    private void closeScope(Instruction instruction) {
        this.instructionListsStack.pop();
        if (!this.instructionListsStack.isEmpty()) {
            this.instructionListsStack.peek().add(instruction);
        }
        this.states.pop();
    }

    public void addInstruction(@NonNull Instruction instruction) {
        if (this.instructionListsStack.isEmpty()) {
            return;
        }

        this.instructionListsStack.peek().add(instruction);
    }

    public void addInstruction(InstructionSpecification<?> instruction) throws BuildException {
        if (instruction == null) {
            throw new BuildException(String.format("Parameter instruction is null."));
        }
        this.instructionListsStack.peek().add(instruction.getInstruction());
    }

    public void addVariableAssignment(ValueMutatorSpecification variable, ValueAccessorSpecification value) {
        final Instruction variableAssignment = this.createVariableAssignment(variable.getMutator(), value.getValueAccessor());
        this.instructionListsStack.peek().add(variableAssignment);
    }

    private Instruction createVariableAssignment(ValueMutator variable, ValueAccessor valueAccessor) {
        return state -> {
            final Object value = valueAccessor.getValue(state);
            variable.setValue(value, state);
        };
    }

    public enum FactoryState {
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
