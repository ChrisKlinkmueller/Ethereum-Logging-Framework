package au.csiro.data61.aap.etl.core.writers;

import java.math.BigInteger;
import java.nio.file.Path;

/**
 * Exporter
 */
public abstract class DataWriter {
    private Path outputFolder;
    private boolean streaming;
    private BigInteger currentBlock;

    public void setOutputFolder(Path outputFolder) {
        assert outputFolder != null;
        this.outputFolder = outputFolder;
    }

    protected Path getOutputFolder() {
        return this.outputFolder;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public void startBlock(BigInteger blocknumber) {
        this.currentBlock = blocknumber;
    }

    public final void endBlock() throws Throwable {
        if (this.streaming) {
            this.writeState(this.currentBlock.toString());
        }
    } 

    public final void endProgram() throws Throwable {
        if (!this.streaming) {
            this.writeState("all");
        }
    }

    protected abstract void writeState(String namePrefix) throws Throwable;
}