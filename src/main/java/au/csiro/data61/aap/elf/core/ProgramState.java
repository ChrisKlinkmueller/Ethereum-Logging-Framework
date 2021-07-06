package au.csiro.data61.aap.elf.core;

import java.nio.file.Path;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.exceptions.ExceptionHandler;
import au.csiro.data61.aap.elf.core.readers.DataReader;
import au.csiro.data61.aap.elf.core.values.ValueStore;
import au.csiro.data61.aap.elf.core.writers.DataWriters;

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

    public static Object connectWebsocketClient(Object[] parameters, ProgramState state) throws ProgramException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String url = (String) parameters[0];
        state.getReader().connect(url);
        return null;
    }

    public static Object connectIpcClient(Object[] parameters, ProgramState state) throws ProgramException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String path = (String) parameters[0];
        state.getReader().connectIpc(path);
        return null;
    }

    public static Object setOutputFolder(Object[] parameters, ProgramState state) throws ProgramException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String outputFolder = (String) parameters[0];
        try {
            state.setOutputFolder(outputFolder);
        } catch (Throwable e) {
            throw new ProgramException("Error when setting the output folder.", e);
        }
        return null;
    }

    public static Object setXesGlobalEventAttribte(Object[] parameters, ProgramState state) throws ProgramException {
        assert parameters != null && parameters.length == 3;
        assert parameters[0] instanceof String;
        assert parameters[1] instanceof String;
        assert parameters[2] != null;
        try {
            state.writers.getXesWriter().addGlobalEventValue((String)parameters[0], (String)parameters[1], parameters[2]);
        } catch (Throwable e) {
            throw new ProgramException("Error when setting adding a global xes event attribute.", e);
        }
        return null;
    }
}
