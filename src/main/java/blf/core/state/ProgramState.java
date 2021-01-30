package blf.core.state;

import blf.configuration.EmissionSettings;
import blf.core.exceptions.ExceptionHandler;
import blf.core.values.BlockchainVariables;
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
    protected final BlockchainVariables blockchainVariables;
    protected EmissionSettings.EmissionMode emissionMode;

    protected ProgramState(BlockchainVariables blockchainVariables) {
        this.valueStore = new ValueStore();
        this.exceptionHandler = new ExceptionHandler();
        this.writers = new DataWriters();
        this.blockchainVariables = blockchainVariables;
        this.emissionMode = EmissionSettings.EmissionMode.DEFAULT_BATCHING;
    }

    public String outputFolderPath = "";

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public BlockchainVariables getBlockchainVariables() {
        return this.blockchainVariables;
    }

    public DataWriters getWriters() {
        return this.writers;
    }

    public EmissionSettings.EmissionMode getEmissionMode() {
        return emissionMode;
    }

    public void setEmissionMode(EmissionSettings.EmissionMode emissionMode) {
        this.emissionMode = emissionMode;
    }

    public void close() {}

}
