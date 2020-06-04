package au.csiro.data61.aap.elf.core.filters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.readers.EthereumLogEntry;
import au.csiro.data61.aap.elf.core.readers.EthereumTransaction;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;

/**
 * LogEntryFilter To understand how to decode event data and topics, see:
 * https://www.programcreek.com/java-api-examples/?class=org.web3j.abi.FunctionReturnDecoder&method=decode
 */
public class LogEntryFilter extends Filter {
    private final FilterPredicate<String> contractCriterion;
    private final LogEntrySignature signature;

    public LogEntryFilter(FilterPredicate<String> contractCriterion, LogEntrySignature signature, Instruction... instructions) {
        this(contractCriterion, signature, Arrays.asList(instructions));
    }

    public LogEntryFilter(FilterPredicate<String> contractCriterion, LogEntrySignature signature, List<Instruction> instructions) {
        super(instructions);
        assert signature != null;
        assert instructions != null;
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
        try {
            if (this.isValidLogEntry(state, logEntry)) {
                state.getReader().setCurrentLogEntry(logEntry);
                this.signature.addLogEntryValues(state, logEntry);
                this.executeInstructions(state);
            }
        } catch (Throwable cause) {
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
            state.getReader().setCurrentTransaction(null);
        }
    }

    private boolean isValidLogEntry(ProgramState state, EthereumLogEntry logEntry) throws ProgramException {
        return this.contractCriterion.test(state, logEntry.getAddress()) && this.signature.hasSignature(logEntry);
    }

    private List<EthereumLogEntry> getEntries(ProgramState state) throws ProgramException {
        if (state.getReader().getCurrentTransaction() != null) {
            return state.getReader().getCurrentTransaction().logStream().collect(Collectors.toList());
        } else if (state.getReader().getCurrentBlock() != null) {
            return state.getReader()
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
