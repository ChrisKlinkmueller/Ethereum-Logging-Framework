package blf.core.exceptions;

import blf.core.state.ProgramState;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

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
    private String outputFolder;
    private String errorLogFileName;
    private FileHandler errorLogFileHandler;
    private boolean isInititalized;

    private ExceptionHandler() {
        this.isInititalized = false;
        this.abortOnException = DEFAULT_ABORT_ON_EXCEPTION;
        this.outputFolder = DEFAULT_ERROR_LOG_OUTPUT_FOLDER;
        this.errorLogFileName = DEFAULT_ERROR_LOG_FILENAME;
        this.errorLogFileHandler = null;
    }

    public static ExceptionHandler getInstance() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }

        return instance;
    }

    public void setAbortOnException(boolean abortOnException) {
        this.abortOnException = abortOnException;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder.replaceAll("(?:^\")|(?:\"$)", "");
    }

    public void setOutputFilename(String errorLogFileName) {
        this.errorLogFileName = errorLogFileName.replaceAll("(?:^\")|(?:\"$)", "");
    }

    public void initializeLoggerHandler() {
        // do not set up the logger handler multiple times
        // as a result of this, initializeLoggerHandler should be called when all parameters are definitely set
        // right now, this point in time is RootListener.enterConnection
        if (this.isInititalized) {
            return;
        }

        final Path outputFolderPath = Path.of(this.outputFolder);

        final Path errorLogFilePath = Paths.get(outputFolderPath.toFile().getPath(), this.errorLogFileName);
        final SimpleFormatter formatter = new SimpleFormatter();

        try {
            this.errorLogFileHandler = new FileHandler(errorLogFilePath.toString());
            this.errorLogFileHandler.setFormatter(formatter);
        } catch (IOException e) {
            final String errMsg = String.format("Setting the output folder for ExceptionHandler failed: %s", e);
            LOGGER.severe(errMsg);

            System.exit(1);
        }

        LOGGER.addHandler(this.errorLogFileHandler);

        this.isInititalized = true;
    }

    public void handleException(String message) {
        this.handleException(message, null);
    }

    public void handleException(String message, Throwable cause) {

        if (!isInititalized) {
            // setting the handler with the default values from the constructor
            this.initializeLoggerHandler();
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
