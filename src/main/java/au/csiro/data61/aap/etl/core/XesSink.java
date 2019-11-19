package au.csiro.data61.aap.etl.core;

import au.csiro.data61.aap.etl.core.DataSink;

/**
 * XesExporter
 */
public class XesSink extends DataSink {

    @Override
    protected void writeState(String namePrefix) throws Throwable {
        throw new UnsupportedOperationException();
    }

    public void addEvent(SinkVariable... variables) {
        assert this.validVariables(variables);
        throw new UnsupportedOperationException();
    }

    public void addTrace(SinkVariable... variables) {
        assert this.validVariables(variables);
        throw new UnsupportedOperationException();
    }
}