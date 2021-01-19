package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Logger;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;
import org.apache.commons.codec.binary.Base64;

/**
 * This class provides functionality to connect to a Hyperledger blockchain node.
 */
public class HyperledgerConnectInstruction implements Instruction {

    private final Logger logger;
    private ExceptionHandler exceptionHandler;

    private final String networkConfigFilePath;
    private final String serverKeyFilePath;
    private final String serverCrtFilePath;
    private final String mspName;
    private final String channel;

    public HyperledgerConnectInstruction(
        final String networkConfigFilePath,
        final String serverKeyFilePath,
        final String serverCrtFilePath,
        final String mspName,
        final String channel
    ) {
        this.networkConfigFilePath = networkConfigFilePath;
        this.serverKeyFilePath = serverKeyFilePath;
        this.serverCrtFilePath = serverCrtFilePath;
        this.mspName = mspName;
        this.channel = channel;

        this.logger = Logger.getLogger(HyperledgerConnectInstruction.class.getName());
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        // init exception handler
        this.exceptionHandler = state.getExceptionHandler();

        final HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final Gateway gateway = this.buildGateway(this.networkConfigFilePath, this.serverKeyFilePath, this.serverCrtFilePath, this.mspName);

        final Network network = this.buildNetwork(gateway, channel);

        hyperledgerProgramState.setGateway(gateway);
        hyperledgerProgramState.setNetwork(network);
    }

    /**
     * This method returns a new {@link Gateway Gateway} for the configuration provided.
     * NetworkConfigFilePath needs to point to a hyperledger network config file,
     * ServerKeyFilePath to a private key file and
     * serverCrtFilePath to a valid certificate related to the private key provided.
     *
     * @param networkConfigFilePath - path to a hyperledger network config file
     * @param serverKeyFilePath     - path to a private key file and
     * @param serverCrtFilePath     - path to a valid certificate related to the private key provided
     * @param mspName               - mspName parameter
     * @return - new {@link Gateway Gateway} object for the configuration provided
     */
    private Gateway buildGateway(String networkConfigFilePath, String serverKeyFilePath, String serverCrtFilePath, String mspName) {

        final String infoMsg = String.format(
            "Hyperledger { networkConfigFilePath: %s,  serverKeyFilePath: %s, serverCrtFilePath: %s }",
            networkConfigFilePath,
            serverKeyFilePath,
            serverCrtFilePath
        );

        logger.info(infoMsg);

        Path networkConfigFile = Paths.get(networkConfigFilePath);

        // Get certificate from file.
        X509Certificate certificate = null;

        // try-with-resources closes the inStream automatically
        try (InputStream inStream = new FileInputStream(serverCrtFilePath)) {
            certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(inStream);
        } catch (FileNotFoundException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Unable to read certificate from provided file %s", networkConfigFilePath),
                e
            );
        } catch (CertificateException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("No correct certificate provided at path: %s.", serverCrtFilePath),
                e
            );
        } catch (IOException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Input error when trying to read from certificate file %s", serverCrtFilePath),
                e
            );
        }

        // Get private key from file.
        PrivateKey privateKey = null;
        try {
            Path serverKeyPath = Paths.get(serverKeyFilePath);

            byte[] serverKeyBytes = Files.readAllBytes(serverKeyPath);

            String key = new String(serverKeyBytes, Charset.defaultCharset());

            String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

            byte[] base64EncodedPrivateKey = Base64.decodeBase64(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(base64EncodedPrivateKey);

            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchFileException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Private key file does not exist on path '%s'", serverKeyFilePath),
                e
            );
        } catch (IOException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Input error when trying to read from private key file: %s.", serverKeyFilePath),
                e
            );
        } catch (NoSuchAlgorithmException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Provided algorithm not found.", e);
        } catch (InvalidKeySpecException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Provided key spec is invalid.", e);
        } catch (Exception e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Unhandled exception has occurred.", e);
        }

        if (certificate == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'certificate' is null.", new NullPointerException());

            return null;
        }

        if (privateKey == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'privateKey' is null.", new NullPointerException());

            return null;
        }

        // Configure the gateway connection used to access the network.
        Gateway.Builder builder = null;
        try {
            builder = Gateway.createBuilder()
                .identity(Identities.newX509Identity(mspName, certificate, privateKey))
                .networkConfig(networkConfigFile);
        } catch (IOException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Unable to read network config from file %s", networkConfigFile),
                e
            );
        }

        if (builder == null) {
            return null;
        }

        // connect to the network.

        return builder.connect();
    }

    /**
     * This method will return a new Network object for the provided gateway object and channel name.
     *
     * @param gateway - provided {@link Gateway Gateway} object
     * @param channel - provided channel name
     * @return - new {@link Network Network} object
     */
    public Network buildNetwork(Gateway gateway, String channel) {

        final String infoMsg = String.format("Hyperledger { gateway: %s,  channel: %s }", gateway, channel);

        logger.info(infoMsg);

        if (gateway == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'gateway' is null.", new NullPointerException());

            return null;
        }

        if (channel == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'channel' is null.", new NullPointerException());

            return null;
        }

        return gateway.getNetwork(channel);
    }

}
