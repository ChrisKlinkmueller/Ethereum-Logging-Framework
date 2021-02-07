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

    public static final String DEFAULT_ERROR_LOG_FILENAME = "error.log";
    public static final String DEFAULT_ERROR_LOG_OUTPUT_FOLDER = ".";
    public static final boolean DEFAULT_ABORT_ON_EXCEPTION = false;
    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());

    private static ExceptionHandler instance;

    private boolean abortOnException;
    public boolean isInititalized;

    private ExceptionHandler() {
        this.isInititalized = false;
    }

    public static ExceptionHandler getInstance() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }

        return instance;
    }

    public void init(boolean abortOnException) {
        this.init(abortOnException, DEFAULT_ERROR_LOG_OUTPUT_FOLDER, DEFAULT_ERROR_LOG_FILENAME);
    }

    public void init(boolean abortOnException, String outputFolder) {
        this.init(abortOnException, outputFolder, DEFAULT_ERROR_LOG_FILENAME);
    }

    public void init(boolean abortOnException, String outputFolder, String errorLogFileName) {
        this.abortOnException = abortOnException;

        final Path outputFolderPath = Path.of(outputFolder);

        final Path errorLogFilePath = Paths.get(outputFolderPath.toFile().getAbsolutePath(), errorLogFileName);
        final SimpleFormatter formatter = new SimpleFormatter();
        final FileHandler errorLogFileHandler;

        try {
            errorLogFileHandler = new FileHandler(errorLogFilePath.toString());
            errorLogFileHandler.setFormatter(formatter);
        } catch (IOException e) {
            final String errMsg = String.format("Setting the output folder for ExceptionHandler failed: %s", e);
            LOGGER.severe(errMsg);

            System.exit(1);

            return;
        }

        LOGGER.addHandler(errorLogFileHandler);

        this.isInititalized = true;
    }

    public void handleException(String message) {
        this.handleException(message, null);
    }

    public void handleException(String message, Throwable cause) {

        if (!isInititalized) {
            this.init(DEFAULT_ABORT_ON_EXCEPTION, DEFAULT_ERROR_LOG_OUTPUT_FOLDER, DEFAULT_ERROR_LOG_FILENAME);
        }

        if (cause == null) {
            LOGGER.severe(message);
        } else {
            LOGGER.log(Level.SEVERE, message, cause);
        }

        if (this.abortOnException) {
            final String errorMsg = String.format(
                "Program failed with the following message: %s. For more details please check the error log %s.%n",
                message,
                DEFAULT_ERROR_LOG_FILENAME
            );

            LOGGER.severe(errorMsg);
            System.exit(1);
        }
    }
}
