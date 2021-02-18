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
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class HyperledgerSmartContractFilterInstruction extends Instruction {

    private final BcqlParser.SmartContractFilterContext smartContractFilterCtx;

    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final Logger logger;

    public HyperledgerSmartContractFilterInstruction(
        BcqlParser.SmartContractFilterContext smartContractFilterCtx,
        List<Instruction> nestedInstructions
    ) {
        super(nestedInstructions);
        this.smartContractFilterCtx = smartContractFilterCtx;
        this.logger = Logger.getLogger(HyperledgerSmartContractFilterInstruction.class.getName());
    }

    @Override
    public void execute(final ProgramState state) {

        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        Pair<String, List<HyperledgerQueryParameters>> hyperledgerContractQueries = HyperledgerInstructionHelper
            .parseSmartContractFilterCtx(hyperledgerProgramState, smartContractFilterCtx);

        UserContext userContext = new UserContext();
        userContext.setName("User1");

        String certificate = null;
        try {
            certificate = Files.readString(Path.of("hyperledger/user1.crt"));
        } catch (IOException e) {
            ExceptionHandler.getInstance().handleException("Could not read user certificate", e);
        }

        // Get private key from file.
        PrivateKey privateKey = HyperledgerInstructionHelper.readPrivateKeyFromFile("hyperledger/user1.key");

        String mspName = hyperledgerProgramState.getMspName();
        userContext.setMspId(mspName);

        X509Enrollment x509Enrollment = new X509Enrollment(privateKey, certificate);
        userContext.setEnrollment(x509Enrollment);

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

        hfClient.setUserContext(userContext);

        Channel channel = hyperledgerProgramState.getNetwork().getChannel();

        QueryByChaincodeRequest queryRequest = hfClient.newQueryProposalRequest();
        queryRequest.setUserContext(userContext);

        // here begins using parsed values from the smart contract signature

        String contractName = hyperledgerContractQueries.a;
        ChaincodeID ccid = ChaincodeID.newBuilder().setName(contractName).build();
        queryRequest.setChaincodeID(ccid); // ChaincodeId object as created in Invoke block

        queryRequest.setFcn("OwnerOf"); // Chaincode function name for querying the blocks
        String[] arguments = { "6" }; // Arguments that the above functions take
        if (arguments != null) queryRequest.setArgs(arguments);
        // Query the chaincode
        Collection<ProposalResponse> queryResponse = null;
        try {
            queryResponse = channel.queryByChaincode(queryRequest);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Smartcontract query could not be sent", e);
        }

        for (ProposalResponse pres : queryResponse) {
            try {
                System.out.println(new String(pres.getChaincodeActionResponsePayload()));
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

}
