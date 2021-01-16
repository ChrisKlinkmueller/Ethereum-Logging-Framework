package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;

import java.util.List;
import java.util.logging.Logger;

/**
 * HyperledgerTransactionFilterInstruction is an Instruction for the Hyperledger log extraction mode of the Blockchain
 * Logging Framework. It extracts the specified transactions (specified by transaction sender and/or recipient)
 * from the current Block and stores the extracted transaction parameters in the ValueStore.
 */
public class HyperledgerTransactionFilterInstruction implements Instruction {

    private final ExceptionHandler exceptionHandler;
    private final Logger logger;
    private final List<String> sendersAddressList;
    private final List<String> recipientsAddressList;

    /**
     * Constructs a HyperledgerTransactionFilterInstruction.
     *
     * @param sendersAddressList    The list of all sender addresses the user requested in the manifest (might be empty).
     * @param recipientsAddressList The list of all recipient addresses the user requested in the manifest (always non-empty).
     */
    public HyperledgerTransactionFilterInstruction(final List<String> sendersAddressList, final List<String> recipientsAddressList) {
        this.sendersAddressList = sendersAddressList;
        this.recipientsAddressList = recipientsAddressList;

        this.logger = Logger.getLogger(HyperledgerTransactionFilterInstruction.class.getName());
        this.exceptionHandler = new ExceptionHandler();
    }

    /**
     * execute is called once the program is constructed from the manifest. It contains the logic for extracting an
     * event from the Hyperledger block that the BLF is currently analyzing. It is called by the Program class.
     *
     * @param state The current ProgramState of the BLF, provided by the Program when called.
     * @throws ProgramException never explicitly
     */
    @Override
    public void execute(ProgramState state) throws ProgramException {
        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        // TODO: implement HyperledgerTransactionFilterInstruction logic here
        String infoMsg = String.format(
            "============== HyperledgerTransactionFilterInstruction ==============\n"
                + "sendersAddressList = %s \n"
                + "recipientsAddressList = %s \n",
            this.sendersAddressList,
            this.recipientsAddressList
        );

        logger.info(infoMsg);
    }

}
