package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerInstructionHelper;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * This class provides functionality to connect to a Hyperledger blockchain node.
 */
public class HyperledgerConnectInstruction extends Instruction {

    private final Logger logger;

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
    public void execute(ProgramState state) {

        final HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final Gateway gateway = this.buildGateway(this.networkConfigFilePath, this.serverKeyFilePath, this.serverCrtFilePath, this.mspName);

        final Network network = this.buildNetwork(gateway, this.channel);

        hyperledgerProgramState.setGateway(gateway);
        hyperledgerProgramState.setNetwork(network);
        hyperledgerProgramState.setMspName(this.mspName);
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
            ExceptionHandler.getInstance()
                .handleException(String.format("Unable to read certificate from provided file %s", networkConfigFilePath), e);
        } catch (CertificateException e) {
            ExceptionHandler.getInstance()
                .handleException(String.format("No correct certificate provided at path: %s.", serverCrtFilePath), e);
        } catch (IOException e) {
            ExceptionHandler.getInstance()
                .handleException(String.format("Input error when trying to read from certificate file %s", serverCrtFilePath), e);
        }

        // Get private key from file.
        PrivateKey privateKey = HyperledgerInstructionHelper.readPrivateKeyFromFile(serverKeyFilePath);

        if (certificate == null) {
            ExceptionHandler.getInstance().handleException("Variable 'certificate' is null.", new NullPointerException());

            return null;
        }

        if (privateKey == null) {
            ExceptionHandler.getInstance().handleException("Variable 'privateKey' is null.", new NullPointerException());

            return null;
        }

        // Configure the gateway connection used to access the network.
        Gateway.Builder builder = null;
        try {
            builder = Gateway.createBuilder()
                .identity(Identities.newX509Identity(mspName, certificate, privateKey))
                .networkConfig(networkConfigFile);
        } catch (IOException e) {
            ExceptionHandler.getInstance()
                .handleException(String.format("Unable to read network config from file %s", networkConfigFile), e);
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
            ExceptionHandler.getInstance().handleException("Variable 'gateway' is null.", new NullPointerException());

            return null;
        }

        if (channel == null) {
            ExceptionHandler.getInstance().handleException("Variable 'channel' is null.", new NullPointerException());

            return null;
        }

        return gateway.getNetwork(channel);
    }

}
