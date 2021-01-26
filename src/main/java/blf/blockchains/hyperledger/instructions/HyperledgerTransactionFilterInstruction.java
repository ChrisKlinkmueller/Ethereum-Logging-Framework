package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerListenerHelper;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.core.values.ValueStore;
import blf.grammar.BcqlParser;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;

import java.math.BigInteger;
import java.util.List;

import static blf.blockchains.hyperledger.variables.HyperledgerTransactionVariables.*;

/**
 * HyperledgerTransactionFilterInstruction is an Instruction for the Hyperledger log extraction mode of the Blockchain
 * Logging Framework. It extracts the specified transactions (specified by transaction sender and/or recipient)
 * from the current Block and stores the extracted transaction parameters in the ValueStore.
 */
public class HyperledgerTransactionFilterInstruction extends FilterInstruction {

    private final BcqlParser.TransactionFilterContext transactionCtx;

    /**
     * Constructs a HyperledgerTransactionFilterInstruction.
     *
     * @param transactionCtx The context of transaction filter.
     * @param nestedInstructions The list of nested instructions.
     */
    public HyperledgerTransactionFilterInstruction(
        BcqlParser.TransactionFilterContext transactionCtx,
        List<Instruction> nestedInstructions
    ) {
        super(nestedInstructions);

        this.transactionCtx = transactionCtx;
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
        // init exception handler
        ExceptionHandler exceptionHandler = state.getExceptionHandler();

        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final List<String> sendersAddressList = HyperledgerListenerHelper.parseAddressListCtx(
            hyperledgerProgramState,
            transactionCtx.senders
        );
        final List<String> recipientsAddressList = HyperledgerListenerHelper.parseAddressListCtx(
            hyperledgerProgramState,
            transactionCtx.recipients
        );

        BlockEvent currentBlock = hyperledgerProgramState.getCurrentBlock();

        if (currentBlock == null) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Expected block, received null", new NullPointerException());

            return;
        }

        for (BlockEvent.TransactionEvent transactionEvent : currentBlock.getTransactionEvents()) {

            final BlockInfo.EnvelopeInfo.IdentitiesInfo transactionEventCreator = transactionEvent.getCreator();
            final String transactionSender = transactionEventCreator.getId().trim().replace("\n", "");

            if (sendersAddressList.isEmpty() || sendersAddressList.contains(transactionSender)) {

                for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo ti : transactionEvent.getTransactionActionInfos()) {

                    final String transactionRecipient = ti.getChaincodeIDName().trim().replace("\n", "");

                    if (recipientsAddressList.isEmpty() || recipientsAddressList.contains(transactionRecipient)) {
                        ValueStore valueStore = state.getValueStore();

                        valueStore.setValue(TRANSACTION_HASH, BigInteger.valueOf(transactionEvent.hashCode()));
                        valueStore.setValue(TRANSACTION_ID, transactionEvent.getTransactionID());
                        valueStore.setValue(TRANSACTION_CREATOR_ID, transactionEventCreator.getId());
                        valueStore.setValue(TRANSACTION_CREATOR_MSPID, transactionEventCreator.getMspid());
                        valueStore.setValue(TRANSACTION_PEER_NAME, transactionEvent.getPeer().getName());
                        valueStore.setValue(TRANSACTION_PEER_HASH, BigInteger.valueOf(transactionEvent.getPeer().hashCode()));
                        valueStore.setValue(TRANSACTION_PEER_URL, transactionEvent.getPeer().getUrl());
                        valueStore.setValue(TRANSACTION_CHAINCODE_ID, ti.getChaincodeIDName());
                        valueStore.setValue(TRANSACTION_RESPONSE_MESSAGE, ti.getResponseMessage());
                        valueStore.setValue(TRANSACTION_ENDORSEMENT_COUNT, BigInteger.valueOf(ti.getEndorsementsCount()));
                        valueStore.setValue(TRANSACTION_RESPONSE_STATUS, BigInteger.valueOf(ti.getResponseStatus()));

                        this.executeNestedInstructions(hyperledgerProgramState);
                    }
                }
            }
        }
    }
}
