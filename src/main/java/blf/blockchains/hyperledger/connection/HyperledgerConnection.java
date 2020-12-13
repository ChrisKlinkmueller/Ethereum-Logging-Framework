package blf.blockchains.hyperledger.connection;

import blf.core.exceptions.ExceptionHandler;
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

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;
import org.apache.commons.codec.binary.Base64;

/**
 * This class provides functionality to connect to a hyperledger blockchain node.
 * 
 */
public abstract class HyperledgerConnection {

    private static final Logger LOGGER = Logger.getLogger(HyperledgerConnection.class.getName());
    private static final ExceptionHandler exceptionHandler = new ExceptionHandler();

    private HyperledgerConnection() {}

    // TODO (by Mykola Digtiar): please have a look into Digtiar_Hyperledger_Test.bcql for more information
    /**
     * getGateway returns a new Gateway for the configuration provided. NetworkConfigFilePath  needs points to a 
     * hyperledger network config file, ServerKeyFilePath to a private key file and the serverCrtFilePath to 
     * a valid certificate related to the private key provided.
     * 
     * @param networkConfigFilePath
     * @param serverKeyFilePath
     * @param serverCrtFilePath
     * @return
     */
    public static @NonNull Gateway getGateway(
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

        LOGGER.info(infoMsg);

        Path networkConfigFile = Paths.get(networkConfigFilePath);

        // Get certificate from file.
        X509Certificate certificate = null;
        try {
            InputStream inStream = new FileInputStream(serverCrtFilePath);
            certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(inStream);
            inStream.close();
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
        Gateway gateway = builder.connect();

        return gateway;
    }

    /**
     * getNetwork will return a new Network object for the provided gateway object and channel name.
     * @param gateway
     * @param channel
     * @return
     */
    public static @NonNull Network getNetwork(@NonNull Gateway gateway, @NonNull String channel) {

        final String infoMsg = String.format("Hyperledger { gateway: %s,  channel: %s }", gateway, channel);

        LOGGER.info(infoMsg);

        return gateway.getNetwork(channel);
    }

    /**
     * getContract will return a new Contract object for the provided network object and contract name.
     * @param network
     * @param contract
     * @return
     */
    public static @NonNull Contract getContract(@NonNull Network network, @NonNull String contract) {
        final String infoMsg = String.format("Hyperledger { network: %s,  contract: %s }", network, contract);

        LOGGER.info(infoMsg);

        return network.getContract(contract);
    }
}
