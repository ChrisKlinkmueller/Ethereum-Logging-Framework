package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class handles the 'BLOCKS (fromBlock) (toBlock)' filter of the .bcql file.
 */
public class HyperledgerBlockFilterInstruction extends FilterInstruction {

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

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
        this.exceptionHandler = new ExceptionHandler();
    }

    public void execute(final ProgramState state) throws ProgramException {
        if (!executeParametersAreValid(this.fromBlockNumber, this.toBlockNumber, state)) {
            return;
        }

        // =========================================================================================================
        // At this point it is safe to assume that this.fromBlockNumber, this.toBlockNumber, state are non-null,
        // that state is an instance of HyperledgerProgramState and
        // that this.fromBlockNumber <= this.toBlockNumber
        // =========================================================================================================

        final HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        BigInteger currentBlockNumber = fromBlockNumber;

        // currentBlockNumber.compareTo(toBlock) < 1 means currentBlockNumber <= toBlock
        while (currentBlockNumber.compareTo(toBlockNumber) < 1) {

            hyperledgerProgramState.setCurrentBlockNumber(currentBlockNumber);

            // TODO: Retrieve Hyperledger block based on the currentBlockNumber parameter.
            // TODO: Please handle all exception via exceptionHandler.handleExceptionAndDecideOnAbort() method.

            String infoMsg = currentBlockNumber.toString();
            this.logger.info(infoMsg);

            // TODO: Change the type of currentBlock from Object to the type of the retrieved currentBlock.

            // TODO: It might be the case that currentBlockNumber refer to the block which does not exist still
            // TODO: in this case execution should wait until the currentBlockNumber is available on the chain.
            hyperledgerProgramState.setCurrentBlock(null);

            this.executeInstructions(hyperledgerProgramState);

            currentBlockNumber = currentBlockNumber.add(BigInteger.ONE);
        }
    }

    private boolean executeParametersAreValid(BigInteger fromBlockNumber, BigInteger toBlockNumber, ProgramState state) {
        if (state == null) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'state' is null.", new NullPointerException());

            return false;
        }

        if (!(state instanceof HyperledgerProgramState)) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
                "Variable 'state' is not an instance of 'HyperledgerProgramState'.",
                new ClassCastException()
            );

            return false;
        }

        if (fromBlockNumber == null) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'fromBlockNumber' is null.", new NullPointerException());

            return false;
        }

        if (toBlockNumber == null) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'toBlockNumber' is null.", new NullPointerException());

            return false;
        }

        // fromBlockNumber.compareTo(toBlockNumber) > 0 means fromBlockNumber > toBlockNumber
        if (fromBlockNumber.compareTo(toBlockNumber) > 0) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
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
