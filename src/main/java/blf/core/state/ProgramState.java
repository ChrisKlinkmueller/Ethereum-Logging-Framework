package blf.core.state;

import blf.core.exceptions.ExceptionHandler;
import blf.core.values.ValueStore;
import blf.core.writers.DataWriters;

import java.util.logging.Logger;

/**
 * ProgramState
 */
public abstract class ProgramState {
    private final ExceptionHandler exceptionHandler;

    protected static final Logger LOGGER = Logger.getLogger(ProgramState.class.getName());
    protected final ValueStore valueStore;
    protected final DataWriters writers;

    protected ProgramState() {
        this.valueStore = new ValueStore();
        this.exceptionHandler = new ExceptionHandler();
        this.writers = new DataWriters();
    }

    public String outputFolderPath = "";

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public DataWriters getWriters() {
        return this.writers;
    }

    public void close() {}

}
