package au.csiro.data61.aap.etl.export;

import java.math.BigInteger;
import java.nio.file.Path;

/**
 * Exporter
 */
public abstract class Exporter {
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

    public void endBlock() throws Throwable {
        if (this.streaming) {
            this.writeState(String.format("block_", this.currentBlock));
        }
    } 

    public void endProgram() throws Throwable {
        if (!this.streaming) {
            this.writeState(String.format("all_blocks"));
        }
    }

    protected abstract void writeState(String namePrefix) throws Throwable;
}