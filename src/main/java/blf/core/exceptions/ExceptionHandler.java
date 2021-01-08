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
    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());
    private final Logger errorLogger;
    private boolean abortOnException;

    public static final String ERROR_LOG_FILENAME = "error.log";

    public ExceptionHandler() {
        this.errorLogger = Logger.getLogger(ERROR_LOG_FILENAME);
    }

    public void setAbortOnException(boolean abortOnException) {
        this.abortOnException = abortOnException;
    }

    public void setOutputFolder(Path outputFolder) {
        final Path errorLogFilePath = Paths.get(outputFolder.toFile().getAbsolutePath(), ERROR_LOG_FILENAME);
        final SimpleFormatter formatter = new SimpleFormatter();
        final FileHandler errorLogFileHandler;

        try {
            errorLogFileHandler = new FileHandler(errorLogFilePath.toString());
        } catch (IOException e) {
            LOGGER.severe(String.format("Setting the output folder for ExceptionHandler failed: %s", e.getMessage()));
            System.exit(1);
            return;
        }

        this.errorLogger.addHandler(errorLogFileHandler);

        errorLogFileHandler.setFormatter(formatter);
    }

    public boolean handleExceptionAndDecideOnAbort(String message) {
        this.errorLogger.log(Level.SEVERE, message);
        return this.abortOnException;
    }

    public boolean handleExceptionAndDecideOnAbort(String message, Throwable cause) {
        // TODO (by Mykola Digtiar): the program abortion should be handled here
        this.errorLogger.log(Level.SEVERE, message, cause);
        return this.abortOnException;
    }

}
