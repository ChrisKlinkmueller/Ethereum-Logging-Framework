package blf.configuration;

import blf.blockchains.ethereum.instructions.EthereumLogEntryFilterInstruction;
import blf.blockchains.ethereum.instructions.EthereumSmartContractFilterInstruction;
import blf.blockchains.ethereum.instructions.EthereumTransactionFilterInstruction;
import blf.core.Program;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.GenericFilterInstruction;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * SpecificationComposer
 */
public class SpecificationComposer {

    public final Stack<FactoryState> states;
    public final Stack<List<Instruction>> instructionListsStack;

    private final ExceptionHandler exceptionHandler;

    public SpecificationComposer() {
        this.instructionListsStack = new Stack<>();
        this.states = new Stack<>();

        this.exceptionHandler = new ExceptionHandler();
    }

    public void prepareProgramBuild() {
        this.prepareBuild(FactoryState.PROGRAM);
    }

    public void prepareBlockRangeBuild() {
        this.prepareBuild(FactoryState.BLOCK_RANGE_FILTER, FactoryState.PROGRAM);
    }

    public void prepareTransactionFilterBuild() {
        this.prepareBuild(FactoryState.TRANSACTION_FILTER, FactoryState.BLOCK_RANGE_FILTER);
    }

    public void prepareLogEntryFilterBuild() {
        this.prepareBuild(FactoryState.LOG_ENTRY_FILTER, FactoryState.BLOCK_RANGE_FILTER, FactoryState.TRANSACTION_FILTER);
    }

    public void prepareSmartContractFilterBuild() {
        this.prepareBuild(FactoryState.SMART_CONTRACT_FILTER, FactoryState.BLOCK_RANGE_FILTER);
    }

    public void prepareGenericFilterBuild() {
        this.prepareBuild(
            FactoryState.GENERIC_FILTER,
            FactoryState.BLOCK_RANGE_FILTER,
            FactoryState.TRANSACTION_FILTER,
            FactoryState.LOG_ENTRY_FILTER,
            FactoryState.SMART_CONTRACT_FILTER,
            FactoryState.PROGRAM
        );
    }

    private void prepareBuild(FactoryState newState, FactoryState... possibleCurrentStates) {
        final boolean statesAreEmpty = this.states.isEmpty() && possibleCurrentStates.length == 0;
        final boolean currentStatesMatch = !this.states.isEmpty()
            && Arrays.stream(possibleCurrentStates).anyMatch(s -> this.states.peek() == s);

        if (statesAreEmpty || currentStatesMatch) {
            this.states.push(newState);
            this.instructionListsStack.add(new LinkedList<>());

            return;
        }

        final String errorMsg;
        if (possibleCurrentStates.length == 0) {
            errorMsg = String.format("A %s can only be build when no other filter is being build.", newState);
        } else {
            errorMsg = String.format(
                "A %s cannot be added to %s, but only to: %s.",
                newState,
                this.states.peek(),
                Arrays.stream(possibleCurrentStates).map(FactoryState::toString).collect(Collectors.joining(", "))
            );
        }

        this.exceptionHandler.handleException(errorMsg, new Exception());
    }

    public Program buildProgram() {
        final FactoryState statesPeek = this.states.peek();

        if (statesPeek != FactoryState.PROGRAM) {
            final String errorMsg = String.format("Cannot build a program, when construction of %s has not been finished.", statesPeek);
            this.exceptionHandler.handleException(errorMsg, new Exception());

            return null;
        }

        return new Program(this.instructionListsStack.peek());
    }

    public void buildTransactionFilter(AddressListSpecification senders, AddressListSpecification recipients) {
        final FactoryState statesPeek = this.states.peek();

        if (statesPeek != FactoryState.TRANSACTION_FILTER) {
            final String errorMsg = String.format(
                "Cannot build a transaction filter, when construction of %s has not been finished.",
                statesPeek
            );
            this.exceptionHandler.handleException(errorMsg, new Exception());

            return;
        }

        final EthereumTransactionFilterInstruction filter = new EthereumTransactionFilterInstruction(
            senders.getAddressCheck(),
            recipients.getAddressCheck(),
            this.instructionListsStack.peek()
        );

        this.closeScope(filter);
    }

    public void buildLogEntryFilter(AddressListSpecification contracts, LogEntrySignatureSpecification signature) {

        final FactoryState statesPeek = this.states.peek();

        if (statesPeek != FactoryState.LOG_ENTRY_FILTER) {
            final String errorMsg = String.format(
                "Cannot build a log entry filter, when construction of %s has not been finished.",
                statesPeek
            );
            this.exceptionHandler.handleException(errorMsg, new Exception());

            return;
        }

        final EthereumLogEntryFilterInstruction filter = new EthereumLogEntryFilterInstruction(
            contracts.getAddressCheck(),
            signature.getSignature(),
            this.instructionListsStack.peek()
        );

        this.closeScope(filter);
    }

    public void buildGenericFilter(GenericFilterPredicateSpecification predicate) {

        final FactoryState statesPeek = this.states.peek();

        if (statesPeek != FactoryState.GENERIC_FILTER) {
            final String errorMsg = String.format(
                "Cannot build a generic filter, when construction of %s has not been finished.",
                statesPeek
            );
            this.exceptionHandler.handleException(errorMsg, new Exception());

            return;
        }

        final GenericFilterInstruction filter = new GenericFilterInstruction(predicate.getPredicate(), this.instructionListsStack.peek());

        this.closeScope(filter);
    }

    public void buildSmartContractFilter(SmartContractFilterSpecification specification) {
        final FactoryState statesPeek = this.states.peek();

        if (statesPeek != FactoryState.SMART_CONTRACT_FILTER) {
            final String errorMsg = String.format(
                "Cannot build a smart contract filter, when construction of %s has not been finished.",
                statesPeek
            );
            this.exceptionHandler.handleException(errorMsg, new Exception());

            return;
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

    public void addInstruction(Instruction instruction) {
        if (this.instructionListsStack.isEmpty()) {
            this.exceptionHandler.handleException("Stack of instructions is empty.", new NullPointerException());

            return;
        }

        this.instructionListsStack.peek().add(instruction);
    }

    public void addInstruction(InstructionSpecification<?> instruction) {
        if (instruction == null) {
            this.exceptionHandler.handleException("Parameter instruction is null.", new NullPointerException());

            return;
        }

        if (this.instructionListsStack.isEmpty()) {
            this.exceptionHandler.handleException("Stack of instructions is empty.", new NullPointerException());

            return;
        }

        this.instructionListsStack.peek().add(instruction.getInstruction());
    }

    public void addVariableAssignment(ValueMutatorSpecification variable, ValueAccessorSpecification value) {
        final Instruction variableAssignment = this.createVariableAssignment(variable.getMutator(), value.getValueAccessor());
        this.instructionListsStack.peek().add(variableAssignment);
    }

    private Instruction createVariableAssignment(ValueMutator variable, ValueAccessor valueAccessor) {
        // TODO: replace TmpInstruction declaration with something meaningful
        class TmpInstruction extends Instruction {
            @Override
            public void execute(ProgramState programState) {
                final Object value = valueAccessor.getValue(programState);
                if (variable != null) {
                    variable.setValue(value, programState);
                }
            }
        }

        return new TmpInstruction();
    }

    public enum FactoryState {
        PROGRAM,
        BLOCK_RANGE_FILTER,
        TRANSACTION_FILTER,
        LOG_ENTRY_FILTER,
        SMART_CONTRACT_FILTER,
        GENERIC_FILTER;

        private final ExceptionHandler exceptionHandler = new ExceptionHandler();

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
                    final String errorMsg = String.format("FactoryState constant '%s' unknown.", this);
                    exceptionHandler.handleException(errorMsg, new Exception());

                    return "";
            }
        }
    }

}
