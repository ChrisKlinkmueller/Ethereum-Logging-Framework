package au.csiro.data61.aap.etl.core.writers;

import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * XesParameter
 */
public class XesParameter {
    private final XesParameterExporter attributeExporter;

    private XesParameter(XesParameterExporter attributeExporter) {
        this.attributeExporter = attributeExporter;
    }

    void exportAttribute(ProgramState state, XesWriter writer) throws ProgramException {
        this.attributeExporter.exportValue(state, writer);
    }

    public static XesParameter stringParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringValue));
    }

    @SuppressWarnings("unchecked")
    public static <T> XesParameterExporter exportValue(String name, ValueAccessor accessor, XesWriterMethod<T> writerMethod) {
        return (state, writer) -> {
            try {
                final T value = (T)accessor.getValue(state);
                writerMethod.export(writer, name, value);
            } 
            catch (Throwable cause) {
                throw new ProgramException(String.format("Error exporting xes attribute '%s'.", name), cause);
            }
        };
    }
    
    @FunctionalInterface
    static interface XesParameterExporter {
        public void exportValue(ProgramState state, XesWriter writer) throws ProgramException;
    }

    @FunctionalInterface
    private static interface XesWriterMethod<T> {
        public void export(XesWriter writer, String name, T value);
    }

    
}