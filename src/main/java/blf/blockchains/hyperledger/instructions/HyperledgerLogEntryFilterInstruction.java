package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;

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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class HyperledgerLogEntryFilterInstruction implements Instruction {

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

    private final List<String> addressNames;
    private final String eventName;
    private final List<Pair<String, String>> entryParameters;

    public HyperledgerLogEntryFilterInstruction(
            final List<String> addressNames,
            String eventName,
            List<Pair<String, String>> entryParameters
    ) {
        this.addressNames = addressNames;
        this.eventName = eventName;
        this.entryParameters = entryParameters;

        this.logger = Logger.getLogger(HyperledgerLogEntryFilterInstruction.class.getName());
        this.exceptionHandler = new ExceptionHandler();
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final String infoMsg = String.format(
                "Executing HyperledgerLogEntryFilterInstruction(addressNames -> %s | eventName -> %s | entryParameters -> %s) for the block %s",
                this.addressNames.toString(),
                this.eventName,
                this.entryParameters.toString(),
                hyperledgerProgramState.getCurrentBlockNumber().toString()
        );

        logger.info(infoMsg);
    }

}
