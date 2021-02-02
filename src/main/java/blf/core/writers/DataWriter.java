package blf.core.writers;

import blf.configuration.EmissionSettings;
import io.reactivex.annotations.NonNull;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Exporter
 */
public abstract class DataWriter {
    private Path outputFolder;
    private EmissionSettings.EmissionMode emissionMode = EmissionSettings.EmissionMode.DEFAULT_BATCHING;
    private BigInteger currentBlock;

    public void setOutputFolder(@NonNull Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    protected Path getOutputFolder() {
        return this.outputFolder;
    }

    public void setEmissionMode(EmissionSettings.EmissionMode emissionMode) {
        this.emissionMode = emissionMode;
    }

    public void startBlock(BigInteger blocknumber) {
        this.currentBlock = blocknumber;
    }

    public final void endBlock() {
        if (this.emissionMode == EmissionSettings.EmissionMode.STREAMING) {
            this.writeState(currentBlock.toString());
            this.deleteState();
        }
        if (this.emissionMode == EmissionSettings.EmissionMode.SAFE_BATCHING) {
            this.writeState("all");
        }
    }

    public final void endProgram() {
        this.writeState("all");
        this.deleteState();
    }

    protected abstract void writeState(String filenameSuffix);

    protected abstract void deleteState();

    @SuppressWarnings("unchecked")
    protected final String asString(Object object) {
        if (object == null) {
            return "";
        }

        if (Collection.class.isAssignableFrom(object.getClass())) {
            String value = ((Collection<Object>) object).stream().map(this::asString).collect(Collectors.joining(", "));
            return String.format("{%s}", value);
        }

        return object.toString();
    }
}
