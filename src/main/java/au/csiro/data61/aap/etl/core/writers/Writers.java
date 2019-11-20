package au.csiro.data61.aap.etl.core.writers;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Sinks
 */
public class Writers {
    private final DataWriter[] writers;
    private final CsvWriter csvWriter;
    private final TxtWriter txtWriter;
    private final XesWriter xesWriter;
    
    public Writers() {
        this.csvWriter = new CsvWriter();
        this.txtWriter = new TxtWriter();
        this.xesWriter = new XesWriter();

        this.writers = new DataWriter[]{
            this.csvWriter,
            this.txtWriter,
            this.xesWriter
        };
    }

    public XesWriter getXesWriter() {
        return this.xesWriter;
    }

    public TxtWriter getTextWriter() {
        return this.txtWriter;
    }

    public CsvWriter getCsvWriter() {
        return this.csvWriter;
    }

    public void setOutputFolder(Path folderPath) throws Throwable {
        assert folderPath != null;
        Arrays.stream(this.writers).forEach(e -> e.setOutputFolder(folderPath));
    }

    public void startNewBlock(BigInteger blockNumber) {
        Arrays.stream(this.writers).forEach(e -> e.startBlock(blockNumber));
    }

    public void writeBlock() throws Throwable {
        for (DataWriter ex : this.writers) {
            ex.endBlock();
        }
    }

    public void writeAllData() throws Throwable {
        for (DataWriter ex : this.writers) {
            ex.endProgram();
        }
    }
}