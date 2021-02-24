package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerInstructionHelper;
import blf.blockchains.hyperledger.helpers.HyperledgerQueryParameters;
import blf.blockchains.hyperledger.helpers.UserContext;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;
import org.antlr.v4.runtime.misc.Pair;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * HyperledgerSmartContractFilterInstruction is an Instruction for the Hyperledger smart contract mode of the Blockchain
 * Logging Framework. It queries for the requested output parameters with a respective function from the current Block and
 * stores the extracted parameter values in the ProgramState.
 */

public class HyperledgerSmartContractFilterInstruction extends Instruction {

    private final BcqlParser.SmartContractFilterContext smartContractFilterCtx;

    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final Logger logger;

    /**
     * Constructs a HyperledgerSmartContractFilterInstruction.
     *
     * @param smartContractFilterCtx    The context of smartContractFilter.
     * @param nestedInstructions        The list of nested instruction.
     */
    public HyperledgerSmartContractFilterInstruction(
        BcqlParser.SmartContractFilterContext smartContractFilterCtx,
        List<Instruction> nestedInstructions
    ) {
        super(nestedInstructions);
        this.smartContractFilterCtx = smartContractFilterCtx;
        this.logger = Logger.getLogger(HyperledgerSmartContractFilterInstruction.class.getName());
    }

    /**
     * execute is called once the program is constructed from the manifest. It contains the logic for queries
     * from Hyperledger blocks that the BLF is currently analyzing. It is called by the Program class.
     *
     * @param state The current ProgramState of the BLF, provided by the Program when called.
     */
    @Override
    public void execute(final ProgramState state) {

        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        Pair<String, List<HyperledgerQueryParameters>> hyperledgerContractQueries = HyperledgerInstructionHelper
            .parseSmartContractFilterCtx(hyperledgerProgramState, smartContractFilterCtx);

        // preparation to fulfill the hyperledger sdk requirements for queries

        UserContext userContext = hyperledgerProgramState.getUserContext();

        if (userContext == null) {
            ExceptionHandler.getInstance()
                .handleException("UserContext has to be specified in SET CONNECTION for queries to work", new NullPointerException());

            return;
        }

        HFClient hfClient = createHFClient();
        hfClient.setUserContext(userContext);
        Channel channel = hyperledgerProgramState.getNetwork().getChannel();

        QueryByChaincodeRequest queryRequest = hfClient.newQueryProposalRequest();
        queryRequest.setUserContext(userContext);

        // here begins the usage of parsed values from the smart contract signature

        String contractName = hyperledgerContractQueries.a;
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(contractName).build();
        queryRequest.setChaincodeID(chaincodeID);

        for (HyperledgerQueryParameters hyperledgerQueryParameters : hyperledgerContractQueries.b) {

            List<ProposalResponse> responses = processQuery(queryRequest, hyperledgerQueryParameters, channel);

            setValues(responses, hyperledgerQueryParameters.getOutputParameters(), hyperledgerProgramState);
        }

        this.executeNestedInstructions(hyperledgerProgramState);
    }

    /**
     * Creates the Hyperledger Fabric CLient and prepares a CryptoSuite for it. The client is needed to call a query.
     *
     * @return      returns a Hyperledger Fabric Client, including a set CryptoSuite.
     */
    private HFClient createHFClient() {
        CryptoSuite cryptoSuite = null;
        try {
            cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Cryptosuite couldn't be initialized", e);
        }

        HFClient hfClient = HFClient.createNewInstance();

        try {
            hfClient.setCryptoSuite(cryptoSuite);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Cryptosuite couldn't be set", e);
        }
        return hfClient;
    }

    /**
     * Processes the previously instantiated queryRequest, through updating it with a function name and input parameters
     * from the parsed Smart Contract Query of the manifest and querying the channel.
     *
     * @param queryRequest                  The request object needed for the query.
     * @param hyperledgerQueryParameters    The parsed parameters of the manifest to perform a query.
     * @param channel                       The hyperledger fabric channel on which the query is performed.
     * @return                              A List of Proposal Responses, which can be written into the ProgramState.
     */
    private List<ProposalResponse> processQuery(
        QueryByChaincodeRequest queryRequest,
        HyperledgerQueryParameters hyperledgerQueryParameters,
        Channel channel
    ) {
        // Chaincode function name for querying the blocks
        queryRequest.setFcn(hyperledgerQueryParameters.getMethodName());

        // Arguments that the above function takes
        if (hyperledgerQueryParameters.getInputParameters() != null) queryRequest.setArgs(hyperledgerQueryParameters.getInputParameters());

        // Query the chaincode
        Collection<ProposalResponse> queryResponse = null;
        try {
            queryResponse = channel.queryByChaincode(queryRequest);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("SmartContract query could not be sent", e);
        }

        if (queryResponse != null) {
            return new LinkedList<>(queryResponse);
        } else {
            ExceptionHandler.getInstance()
                    .handleException("The queries response is null", new NullPointerException());

            return new LinkedList<>();
        }
    }

    /**
     * Tries to parse the contents of the Proposal Responses to Strings. Afterwards sets it to the
     * ValueStore of the ProgramState together with the output parameter name defined in the manifest.
     *
     * @param responses                 List of Proposal Responses, which include the returned values of the query.
     * @param outputParameters          The output parameters defined in the manifest and which serve as variableNames.
     * @param hyperledgerProgramState   The current ProgramState of the BLF.
     */
    private void setValues(List<ProposalResponse> responses, String[] outputParameters, HyperledgerProgramState hyperledgerProgramState) {
        if (responses.size() != outputParameters.length) {
            throw new IllegalArgumentException("Expected output parameters amount does not match with actually returned values.");
        }

        IntStream.range(0, responses.size()).forEach(i -> {
            final String value;
            try {
                value = new String(responses.get(i).getChaincodeActionResponsePayload());
                final String name = outputParameters[i];
                hyperledgerProgramState.getValueStore().setValue(name, value);
            } catch (InvalidArgumentException e) {
                ExceptionHandler.getInstance().handleException("Chaincode response includes an invalid argument", e);
            }
        });
    }

}
