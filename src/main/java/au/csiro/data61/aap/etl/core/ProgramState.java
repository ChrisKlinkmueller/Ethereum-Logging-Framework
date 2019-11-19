package au.csiro.data61.aap.etl.core;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * EtlState
 */
public class ProgramState {
    private final ValueStore valueStore;
    private final DataSource dataSource;
    private final DataSink[] sinks;
    private final CsvSink csvSink;
    private final TextSink txtSink;
    private final XesSink xesSink;    
    private final ExceptionHandler exceptionHandler;

    public ProgramState() {
        this.valueStore = new ValueStore();
        this.dataSource = new DataSource();
        this.exceptionHandler = new ExceptionHandler();
        this.csvSink = new CsvSink();
        this.txtSink = new TextSink();
        this.xesSink = new XesSink();

        this.sinks = new DataSink[]{
            this.csvSink,
            this.txtSink,
            this.xesSink
        };
    }

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public XesSink getXesSink() {
        return this.xesSink;
    }

    public TextSink getTextSink() {
        return this.txtSink;
    }

    public CsvSink getCsvSink() {
        return this.csvSink;
    }

    public void setOutputFolder(String filepath) throws Throwable {
        final Path outputFolder = Path.of(filepath);
        if (!outputFolder.toFile().exists()) {
            throw new EtlException(String.format("Folder '%s' does not exist.", outputFolder.toString()));
        }

        Arrays.stream(this.sinks).forEach(e -> e.setOutputFolder(outputFolder));
        exceptionHandler.setOutputFolder(outputFolder);
    }

    public void startBlock(BigInteger blockNumber) {
        Arrays.stream(this.sinks).forEach(e -> e.startBlock(blockNumber));
    }

    public void endBlock() throws Throwable {
        for (DataSink ex : this.sinks) {
            ex.endBlock();
        }
    }

    public void endProgram() throws Throwable {
        for (DataSink ex : this.sinks) {
            ex.endProgram();
        }
    }

    public void close() {
        this.getDataSource().close();
    }
}