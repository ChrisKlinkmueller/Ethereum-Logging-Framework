package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.gateway.Network;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;
import java.lang.InterruptedException;

/**
 * This class handles the 'BLOCKS (fromBlock) (toBlock)' filter of the .bcql file.
 */
public class HyperledgerBlockFilterInstruction extends FilterInstruction {

    private final Logger logger;
    private ExceptionHandler exceptionHandler;

    private final BigInteger fromBlockNumber;
    private final BigInteger toBlockNumber;

    public HyperledgerBlockFilterInstruction(
        final BigInteger fromBlockNumber,
        final BigInteger toBlockNumber,
        List<Instruction> nestedInstructions
    ) {
        // here the list of nested instructions is created
        super(nestedInstructions);

        this.fromBlockNumber = fromBlockNumber;
        this.toBlockNumber = toBlockNumber;

        this.logger = Logger.getLogger(HyperledgerBlockFilterInstruction.class.getName());
    }

    @Override
    public void execute(final ProgramState state) throws ProgramException {
        // init exception handler
        this.exceptionHandler = state.getExceptionHandler();

        if (!executeParametersAreValid(this.fromBlockNumber, this.toBlockNumber, state)) {
            return;
        }

        // =========================================================================================================
        // At this point it is safe to assume that this.fromBlockNumber, this.toBlockNumber, state are non-null,
        // that state is an instance of HyperledgerProgramState and
        // that this.fromBlockNumber <= this.toBlockNumber
        // =========================================================================================================

        final HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        Network network = hyperledgerProgramState.getNetwork();

        network.addBlockListener(fromBlockNumber.longValue(), (BlockEvent blockEvent) -> {
            BigInteger currentBlockNumber = BigInteger.valueOf(blockEvent.getBlockNumber());
            // If the currentBlockNumber is greater than the toBlockNumber, we want to stop
            if (currentBlockNumber.compareTo(toBlockNumber) > 0) {
                synchronized (network) {
                    network.notifyAll();
                }
            } else {
                hyperledgerProgramState.setCurrentBlockNumber(currentBlockNumber);

                String infoMsg = currentBlockNumber.toString();
                this.logger.info("Extracting block number: " + infoMsg);
                hyperledgerProgramState.setCurrentBlock(blockEvent);

                try {
                    this.executeInstructions(hyperledgerProgramState);
                } catch (ProgramException err) {
                    String errorMsg = String.format("Unable to execute instructions");
                    this.exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, err);
                }

                if (currentBlockNumber.compareTo(toBlockNumber) == 0) {
                    synchronized (network) {
                        network.notifyAll();
                    }
                }
            }
        });
        synchronized (network) {
            try {
                network.wait();
            } catch (InterruptedException err) {
                String errorMsg = String.format("Failed when iterating over blocks.");
                this.exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, err);
            }
        }
    }

    private boolean executeParametersAreValid(BigInteger fromBlockNumber, BigInteger toBlockNumber, ProgramState state) {

        if (!(state instanceof HyperledgerProgramState)) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                "Variable 'state' is not an instance of 'HyperledgerProgramState'.",
                new ClassCastException()
            );

            return false;
        }

        if (fromBlockNumber == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'fromBlockNumber' is null.", new NullPointerException());

            return false;
        }

        if (toBlockNumber == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'toBlockNumber' is null.", new NullPointerException());

            return false;
        }

        // fromBlockNumber.compareTo(toBlockNumber) > 0 means fromBlockNumber > toBlockNumber
        if (fromBlockNumber.compareTo(toBlockNumber) > 0) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format(
                    "In BLOCKS statement the 'fromBlockNumber'(%s) parameter is bigger then 'toBlockNumber'(%s) parameter.",
                    fromBlockNumber.toString(),
                    toBlockNumber.toString()
                ),
                new Exception()
            );

            return false;
        }

        return true;
    }

}
