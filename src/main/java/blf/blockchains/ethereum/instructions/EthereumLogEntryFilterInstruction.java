package blf.blockchains.ethereum.instructions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.blockchains.ethereum.reader.EthereumLogEntry;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.blockchains.hyperledger.classes.EthereumLogEntrySignature;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.FilterPredicate;
import io.reactivex.annotations.NonNull;

/**
 * LogEntryFilter To understand how to decode event data and topics, see:
 * https://www.programcreek.com/java-api-examples/?class=org.web3j.abi.FunctionReturnDecoder&amp;method=decode
 */
public class EthereumLogEntryFilterInstruction extends FilterInstruction {
    private final FilterPredicate<String> contractCriterion;
    private final EthereumLogEntrySignature signature;

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
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final List<EthereumLogEntry> logEntries = this.getEntries(state);
        for (EthereumLogEntry logEntry : logEntries) {
            processLogEntry(state, logEntry);
        }
    }

    private void processLogEntry(ProgramState state, EthereumLogEntry logEntry) throws ProgramException {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        try {
            if (this.isValidLogEntry(state, logEntry)) {
                ethereumProgramState.getReader().setCurrentLogEntry(logEntry);
                this.signature.addLogEntryValues(state, logEntry);
                this.executeInstructions(state);
            }
        } catch (Exception cause) {
            final String message = String.format(
                "Error mapping log entry '%s' in transaction '%s 'in block '%s'.",
                logEntry.getLogIndex(),
                logEntry.getTransactionIndex(),
                logEntry.getBlockNumber()
            );
            final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, cause);
            if (abort) {
                throw new ProgramException(message, cause);
            }
        } finally {
            ethereumProgramState.getReader().setCurrentTransaction(null);
        }
    }

    private boolean isValidLogEntry(ProgramState state, EthereumLogEntry logEntry) throws ProgramException {
        return this.contractCriterion.test(state, logEntry.getAddress()) && this.signature.hasSignature(logEntry);
    }

    private List<EthereumLogEntry> getEntries(ProgramState state) throws ProgramException {
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
            throw new ProgramException(
                "Log entries can only be extracted from blocks or transactions, but there is no open block or transaction."
            );
        }
    }

}
