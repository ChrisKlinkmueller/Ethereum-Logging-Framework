package blf.blockchains.hyperledger.connection;

import blf.core.exceptions.ExceptionHandler;
import io.reactivex.annotations.NonNull;

import java.util.logging.Logger;

public abstract class HyperledgerConnection {

    private static final Logger LOGGER = Logger.getLogger(HyperledgerConnection.class.getName());
    private static final ExceptionHandler exceptionHandler = new ExceptionHandler();

    private HyperledgerConnection() {}

    // TODO (by Mykola Digtiar): please have a look into Digtiar_Hyperledger_Test.bcql for more information
    public static @NonNull Object getGateway(
        @NonNull String networkConfigFilePath,
        @NonNull String serverKeyFilePath,
        @NonNull String serverCrtFilePath
    ) {

        final String infoMsg = String.format(
            "Hyperledger { networkConfigFilePath: %s,  serverKeyFilePath: %s, serverCrtFilePath: %s }",
            networkConfigFilePath,
            serverKeyFilePath,
            serverCrtFilePath
        );

        LOGGER.info(infoMsg);

        // TODO (by Mykola Digtiar): implement hyperledger network connection here
        // TODO (by Mykola Digtiar): config and server files might not exist -> pls check for existence
        // TODO (by Mykola Digtiar): do not throw any exceptions but use exceptionHandler
        // exceptionHandler.handleExceptionAndDecideOnAbort(msg, cause)

        return new Object();
    }

    public static @NonNull Object getNetwork(@NonNull Object gateway, @NonNull String channel) {

        final String infoMsg = String.format("Hyperledger { gateway: %s,  channel: %s }", gateway, channel);

        LOGGER.info(infoMsg);

        // TODO: (by Mykola Digtiar): implement hyperledger channel connection here
        // TODO (by Mykola Digtiar): config and server files might not exist -> pls check for existence
        // TODO (by Mykola Digtiar): do not throw any exceptions but use exceptionHandler
        // exceptionHandler.handleExceptionAndDecideOnAbort(msg, cause)

        return new Object();
    }

}
