package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerInstructionHelper;
import blf.blockchains.hyperledger.helpers.UserContext;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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

        HyperledgerInstructionHelper.parseSmartContractFilterCtx(hyperledgerProgramState, smartContractFilterCtx);

        UserContext userContext = new UserContext();
        userContext.setName("User1");

        String certificate = null;
        try {
            certificate = Files.readString(Path.of("hyperledger/user1.crt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get private key from file.
        PrivateKey privateKey = null;
        try {
            Path clientKeyPath = Paths.get("hyperledger/user1.key");

            byte[] clientKeyBytes = Files.readAllBytes(clientKeyPath);

            String key = new String(clientKeyBytes, Charset.defaultCharset());

            String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

            byte[] base64EncodedPrivateKey = Base64.decodeBase64(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(base64EncodedPrivateKey);

            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchFileException e) {
            ExceptionHandler.getInstance().handleException(String.format("Private key file does not exist on path."), e);
        } catch (IOException e) {
            ExceptionHandler.getInstance().handleException(String.format("Input error when trying to read from private key file."), e);
        } catch (NoSuchAlgorithmException e) {
            ExceptionHandler.getInstance().handleException("Provided algorithm not found.", e);
        } catch (InvalidKeySpecException e) {
            ExceptionHandler.getInstance().handleException("Provided key spec is invalid.", e);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Unhandled exception has occurred.", e);
        }

        userContext.setMspId("Org1MSP");

        X509Enrollment x509Enrollment = new X509Enrollment(privateKey, certificate);
        userContext.setEnrollment(x509Enrollment);

        CryptoSuite cryptoSuite = null;
        try {
            cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CryptoException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        HFClient hfClient = HFClient.createNewInstance();
        try {
            hfClient.setCryptoSuite(cryptoSuite);
        } catch (CryptoException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        hfClient.setUserContext(userContext);

        Channel channel = ((HyperledgerProgramState) state).getNetwork().getChannel();

        ChaincodeID ccid = ChaincodeID.newBuilder().setName("kitties").build();

        QueryByChaincodeRequest queryRequest = hfClient.newQueryProposalRequest();
        queryRequest.setChaincodeID(ccid); // ChaincodeId object as created in Invoke block
        queryRequest.setFcn("OwnerOf"); // Chaincode function name for querying the blocks
        queryRequest.setUserContext(userContext);

        String[] arguments = { "6" }; // Arguments that the above functions take
        if (arguments != null) queryRequest.setArgs(arguments);
        // Query the chaincode
        Collection<ProposalResponse> queryResponse = null;
        try {
            queryResponse = channel.queryByChaincode(queryRequest);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
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
