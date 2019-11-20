package au.csiro.data61.aap.etl.core;

import java.nio.file.Path;

import au.csiro.data61.aap.etl.core.readers.DataReader;
import au.csiro.data61.aap.etl.core.writers.Writers;

/**
 * EtlState
 */
public class ProgramState {
    private final ValueStore valueStore;
    private final DataReader dataSource;
    private final Writers writers;    
    private final ExceptionHandler exceptionHandler;

    public ProgramState() {
        this.valueStore = new ValueStore();
        this.dataSource = new DataReader();
        this.exceptionHandler = new ExceptionHandler();
        this.writers = new Writers();
    }

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public DataReader getDataSource() {
        return this.dataSource;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public void setOutputFolder(String folderPath) throws EtlException {
        final Path outputFolder = Path.of(folderPath);
        if (!outputFolder.toFile().exists()) {
            throw new EtlException(String.format("Folder '%s' does not exist.", outputFolder.toString()));
        }

        try  {
            this.exceptionHandler.setOutputFolder(outputFolder);
        }
        catch (Throwable cause) {
            throw new EtlException("Error when setting the output folder.", cause);
        }
    }

    public Writers getWriters() {
        return this.writers;
    }

    public void close() {
        this.getDataSource().close();
    }
}