package au.csiro.data61.aap.etl;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;

import au.csiro.data61.aap.etl.export.CsvExporter;
import au.csiro.data61.aap.etl.export.Exporter;
import au.csiro.data61.aap.etl.export.LineExporter;
import au.csiro.data61.aap.etl.export.XesExporter;

/**
 * EtlState
 */
public class EtlState {
    private final ValueStore valueStore;
    private final EthereumSources ethereumSources;
    private final ExceptionHandler exceptionHandler;
    private final Exporter[] exporters;
    private final CsvExporter csvExporter;
    private final LineExporter lineExporter;
    private final XesExporter xesExporter;

    public EtlState() {
        this.valueStore = new ValueStore();
        this.ethereumSources = new EthereumSources();
        this.exceptionHandler = new ExceptionHandler();
        this.csvExporter = new CsvExporter();
        this.lineExporter = new LineExporter();
        this.xesExporter = new XesExporter();

        this.exporters = new Exporter[]{
            this.csvExporter,
            this.lineExporter,
            this.xesExporter
        };
    }

    public ValueStore getValueStore() {
        return this.valueStore;
    }

    public EthereumSources getEthereumSources() {
        return this.ethereumSources;
    }

    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public XesExporter getXesExporter() {
        return this.xesExporter;
    }

    public LineExporter getLineExporter() {
        return this.lineExporter;
    }

    public CsvExporter getCsvExporter() {
        return this.csvExporter;
    }

    public void startBlock(String filepath) throws Throwable {
        final Path outputFolder = Path.of(filepath);
        if (!outputFolder.toFile().exists()) {
            throw new EtlException(String.format("Folder '%s' does not exist.", outputFolder.toString()));
        }

        Arrays.stream(this.exporters).forEach(e -> e.setOutputFolder(outputFolder));
        exceptionHandler.setOutputFolder(outputFolder);
    }

    public void startBlock(BigInteger blockNumber) {
        Arrays.stream(this.exporters).forEach(e -> e.startBlock(blockNumber));
    }
}