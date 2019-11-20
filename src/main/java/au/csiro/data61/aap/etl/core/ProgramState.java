package au.csiro.data61.aap.etl.core;

import java.nio.file.Path;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.exceptions.ExceptionHandler;
import au.csiro.data61.aap.etl.core.readers.DataReader;
import au.csiro.data61.aap.etl.core.writers.DataWriters;

/**
 * EtlState
 */
public class ProgramState {
    private final ValueStore valueStore;
    private final DataReader reader;
    private final DataWriters writers;    
    private final ExceptionHandler exceptionHandler;

    public ProgramState() {
        this.valueStore = new ValueStore();
        this.reader = new DataReader();
        this.exceptionHandler = new ExceptionHandler();
        this.writers = new DataWriters();
    }

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public DataReader getDataSource() {
        return this.reader;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public void setOutputFolder(String folderPath) throws ProgramException {
        final Path outputFolder = Path.of(folderPath);
        if (!outputFolder.toFile().exists()) {
            throw new ProgramException(String.format("Folder '%s' does not exist.", outputFolder.toString()));
        }

        try  {
            this.exceptionHandler.setOutputFolder(outputFolder);
        }
        catch (Throwable cause) {
            throw new ProgramException("Error when setting the output folder.", cause);
        }
    }

    public DataWriters getWriters() {
        return this.writers;
    }

    public void close() {
        this.getDataSource().close();
    }
}