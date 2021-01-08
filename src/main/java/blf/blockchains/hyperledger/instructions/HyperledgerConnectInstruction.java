package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
 * This class provides functionality to connect to a hyperledger blockchain node.
 */
public class HyperledgerConnectInstruction implements Instruction {

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

    public HyperledgerConnectInstruction() {
        this.logger = Logger.getLogger(HyperledgerConnectInstruction.class.getName());
        this.exceptionHandler = new ExceptionHandler();
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final Gateway gateway = this.buildGateway(
            hyperledgerProgramState.getNetworkConfigFilePath(),
            hyperledgerProgramState.getServerKeyFilePath(),
            hyperledgerProgramState.getServerCrtFilePath(),
            hyperledgerProgramState.getMspName()
        );

        final Network network = this.buildNetwork(gateway, hyperledgerProgramState.getChannel());

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
    private Gateway buildGateway(
        @NonNull String networkConfigFilePath,
        @NonNull String serverKeyFilePath,
        @NonNull String serverCrtFilePath,
        @NonNull String mspName
    ) {

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
            exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Unable to read certificate from provided file %s", networkConfigFilePath),
                e
            );
        } catch (CertificateException e) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("No correct certificate provided at path: %s.", serverCrtFilePath),
                e
            );
        } catch (IOException e) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
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
        } catch (IOException e) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Input error when trying to read from private key file: %s", serverCrtFilePath),
                e
            );
        } catch (NoSuchAlgorithmException e) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Provided algorithm not found.", e);
        } catch (InvalidKeySpecException e) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Provided key spec is invalid.", e);
        }

        // Configure the gateway connection used to access the network.
        Gateway.Builder builder = null;
        try {
            builder = Gateway.createBuilder()
                .identity(Identities.newX509Identity(mspName, certificate, privateKey))
                .networkConfig(networkConfigFile);
        } catch (IOException e) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
                String.format("Unable to read network config from file %s", networkConfigFile),
                e
            );
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
    public Network buildNetwork(@NonNull Gateway gateway, @NonNull String channel) {

        final String infoMsg = String.format("Hyperledger { gateway: %s,  channel: %s }", gateway, channel);

        logger.info(infoMsg);

        return gateway.getNetwork(channel);
    }

}
