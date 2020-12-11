package blf.core.exceptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * ExceptionHandler
 */
public class ExceptionHandler {
    private static final String FILENAME = "error.log";
    private final Logger logger;
    private boolean abortOnException;

    public ExceptionHandler() {
        this.logger = Logger.getLogger(FILENAME);
    }

    public void setAbortOnException(boolean abortOnException) {
        this.abortOnException = abortOnException;
    }

    public void setOutputFolder(Path outputFolder) throws Throwable {
        final Path filepath = Paths.get(outputFolder.toFile().getAbsolutePath(), FILENAME);
        final FileHandler fileHandler = new FileHandler(filepath.toString());
        this.logger.addHandler(fileHandler);

        final SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    public boolean handleExceptionAndDecideOnAbort(String message) {
        this.logger.log(Level.SEVERE, message);
        return this.abortOnException;
    }

    public boolean handleExceptionAndDecideOnAbort(String message, Throwable cause) {
        this.logger.log(Level.SEVERE, message, cause);
        return this.abortOnException;
    }

}
