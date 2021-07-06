package au.csiro.data61.aap.elf.core.writers;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

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
            this.writeState();
        }
    }

    protected void writeState() throws Throwable {
        this.writeState(null);
    }

    protected abstract void writeState(String filenameSuffix) throws Throwable;

    @SuppressWarnings("unchecked")
    protected final String asString(Object object) {
        if (object == null) {
            return "";
        }

        if (Collection.class.isAssignableFrom(object.getClass())) {
            String value = ((Collection<Object>) object).stream().map(obj -> asString(obj)).collect(Collectors.joining(", "));
            return String.format("{%s}", value);
        }

        return object.toString();
    }
}
