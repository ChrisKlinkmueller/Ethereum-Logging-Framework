package blf.core;

import java.nio.file.Path;
import java.util.logging.Logger;

import blf.core.exceptions.ProgramException;
import blf.core.exceptions.ExceptionHandler;
import blf.core.readers.DataReader;
import blf.core.values.ValueStore;
import blf.core.writers.DataWriters;
import io.reactivex.annotations.NonNull;

/**
 * EtlState
 */
public class ProgramState {
    private static final Logger LOGGER = Logger.getLogger(ProgramState.class.getName());
    private final ValueStore valueStore;
    private final DataReader reader;
    private final DataWriters writers;
    private final ExceptionHandler exceptionHandler;

    public static final String ERROR_LOG_FILENAME = "error.log";

    public ProgramState() {
        this.valueStore = new ValueStore();
        this.reader = new DataReader();
        this.exceptionHandler = new ExceptionHandler();
        this.writers = new DataWriters();
    }

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public DataReader getReader() {
        return this.reader;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    private void setOutputFolder(String folderPath) throws ProgramException {
        final Path outputFolder = Path.of(folderPath);
        if (!outputFolder.toFile().exists()) {
            throw new ProgramException(String.format("Folder '%s' does not exist.", outputFolder.toString()));
        }

        try {
            this.exceptionHandler.setOutputFolder(outputFolder);
            this.writers.setOutputFolder(outputFolder);
        } catch (Throwable cause) {
            throw new ProgramException("Error when setting the output folder.", cause);
        }
    }

    public DataWriters getWriters() {
        return this.writers;
    }

    public void close() {
        this.getReader().close();
    }

    public static Object connectWebsocketClient(@NonNull Object[] parameters, ProgramState state) throws ProgramException {
        checkParameters(parameters);
        final String url = (String) parameters[0];
        state.getReader().connect(url);
        return null;
    }

    public static Object connectIpcClient(@NonNull Object[] parameters, ProgramState state) throws ProgramException {
        checkParameters(parameters);
        final String path = (String) parameters[0];
        state.getReader().connectIpc(path);
        return null;
    }

    public static Object setOutputFolder(@NonNull Object[] parameters, ProgramState state) throws ProgramException {
        checkParameters(parameters);
        final String outputFolder = (String) parameters[0];
        try {
            state.setOutputFolder(outputFolder);
        } catch (Exception e) {
            throw new ProgramException("Error when setting the output folder.", e);
        }
        return null;
    }

    private static void checkParameters(@NonNull Object[] parameters) {
        if (parameters.length != 1) {
            LOGGER.severe("Unexpected amount of parameters");
            System.exit(1);
        }
        if (!(parameters[0] instanceof String)) {
            LOGGER.severe("Parameters[0] is not instance of String");
            System.exit(1);
        }
    }
}
