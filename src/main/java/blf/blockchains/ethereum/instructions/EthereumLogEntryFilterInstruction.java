package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumLogEntrySignature;
import blf.blockchains.ethereum.reader.EthereumLogEntry;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.Instruction;
import blf.core.interfaces.FilterPredicate;
import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LogEntryFilter To understand how to decode event data and topics, see:
 * https://www.programcreek.com/java-api-examples/?class=org.web3j.abi.FunctionReturnDecoder&amp;method=decode
 */
public class EthereumLogEntryFilterInstruction extends Instruction {
    private final FilterPredicate<String> contractCriterion;
    private final EthereumLogEntrySignature signature;

    private final ExceptionHandler exceptionHandler;

    public EthereumLogEntryFilterInstruction(
        FilterPredicate<String> contractCriterion,
        EthereumLogEntrySignature signature,
        Instruction... instructions
    ) {
        this(contractCriterion, signature, Arrays.asList(instructions));
    }

    public EthereumLogEntryFilterInstruction(
        FilterPredicate<String> contractCriterion,
        @NonNull EthereumLogEntrySignature signature,
        @NonNull List<Instruction> instructions
    ) {
        super(instructions);
        this.contractCriterion = contractCriterion;
        this.signature = signature;

        this.exceptionHandler = new ExceptionHandler();
    }

    @Override
    public void execute(ProgramState state) {
        final List<EthereumLogEntry> logEntries = this.getEntries(state);

        for (EthereumLogEntry logEntry : logEntries) {
            processLogEntry(state, logEntry);
        }
    }

    private void processLogEntry(ProgramState state, EthereumLogEntry logEntry) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        try {
            if (this.isValidLogEntry(state, logEntry)) {
                ethereumProgramState.getReader().setCurrentLogEntry(logEntry);
                this.signature.addLogEntryValues(state, logEntry);
                this.executeNestedInstructions(state);
            }
        } catch (Exception cause) {
            final String message = String.format(
                "Error mapping log entry '%s' in transaction '%s 'in block '%s'.",
                logEntry.getLogIndex(),
                logEntry.getTransactionIndex(),
                logEntry.getBlockNumber()
            );
            state.getExceptionHandler().handleException(message, cause);
        } finally {
            ethereumProgramState.getReader().setCurrentTransaction(null);
        }
    }

    private boolean isValidLogEntry(ProgramState state, EthereumLogEntry logEntry) throws ProgramException {
        return this.contractCriterion.test(state, logEntry.getAddress()) && this.signature.hasSignature(logEntry);
    }

    private List<EthereumLogEntry> getEntries(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        if (ethereumProgramState.getReader().getCurrentTransaction() != null) {
            return ethereumProgramState.getReader().getCurrentTransaction().logStream().collect(Collectors.toList());
        } else if (ethereumProgramState.getReader().getCurrentBlock() != null) {
            return ethereumProgramState.getReader()
                .getCurrentBlock()
                .transactionStream()
                .flatMap(EthereumTransaction::logStream)
                .collect(Collectors.toList());
        } else {
            this.exceptionHandler.handleException(
                "Log entries can only be extracted from blocks or transactions, but there is no open block or transaction.",
                new Exception()
            );
        }

        return new LinkedList<>();
    }

}
