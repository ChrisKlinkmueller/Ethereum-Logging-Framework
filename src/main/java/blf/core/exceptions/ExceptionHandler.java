package blf.core.exceptions;

import blf.core.state.ProgramState;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class is responsible for handling all of the exceptions that happen during runtime.
 * The exception handler is instantiated in the ProgramState and
 * its reference should be used to handle every possible exception.
 *
 * @see ProgramState
 */
public class ExceptionHandler {

    public static final String ERROR_LOG_FILENAME = "error.log";

    private final Logger errorLogger;
    private boolean abortOnException;

    public ExceptionHandler() {
        this.errorLogger = Logger.getLogger(ERROR_LOG_FILENAME);
        this.abortOnException = false;
    }

    public void setAbortOnException(boolean abortOnException) {
        this.abortOnException = abortOnException;
    }

    public void setErrorLogOutputFolder(Path outputFolder) {

        final Path errorLogFilePath = Paths.get(outputFolder.toFile().getAbsolutePath(), ERROR_LOG_FILENAME);
        final SimpleFormatter formatter = new SimpleFormatter();
        final FileHandler errorLogFileHandler;

        try {
            errorLogFileHandler = new FileHandler(errorLogFilePath.toString());
        } catch (IOException e) {
            final String errMsg = String.format("Setting the output folder for ExceptionHandler failed: %s", e.getMessage());
            this.handleException(errMsg, e);

            return;
        }

        this.errorLogger.addHandler(errorLogFileHandler);

        errorLogFileHandler.setFormatter(formatter);
    }

    public void handleException(String message) {
        this.handleException(message, null);
    }

    public void handleException(String message, Throwable cause) {
        if (cause == null) {
            this.errorLogger.severe(message);
        } else {
            this.errorLogger.log(Level.SEVERE, message, cause);
        }

        if (this.abortOnException) {
            final String errorMsg = String.format(
                "Program failed with the following message: %s. For more details please check the error log %s.%n",
                message,
                ERROR_LOG_FILENAME
            );

            this.errorLogger.severe(errorMsg);
            System.exit(1);
        }
    }
}
