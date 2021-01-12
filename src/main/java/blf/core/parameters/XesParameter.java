package blf.core.parameters;

import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.core.writers.XesWriter;
import io.reactivex.annotations.NonNull;

/**
 * XesParameter
 */
public class XesParameter {
    private final XesParameterExporter attributeExporter;

    private XesParameter(XesParameterExporter attributeExporter) {
        this.attributeExporter = attributeExporter;
    }

    public void exportAttribute(ProgramState state, XesWriter writer) throws ProgramException {
        this.attributeExporter.exportValue(state, writer);
    }

    public static XesParameter boolParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addBooleanValue));
    }

    public static XesParameter boolListParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addBooleanList));
    }

    public static XesParameter dateParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addDateValue));
    }

    public static XesParameter dateListParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addDateList));
    }

    public static XesParameter floatParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addFloatValue));
    }

    public static XesParameter floatListParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addFloatList));
    }

    public static XesParameter integerParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addIntValue));
    }

    public static XesParameter integerListParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addIntList));
    }

    public static XesParameter stringParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringValue));
    }

    public static XesParameter stringListParameter(@NonNull String name, @NonNull ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringList));
    }

    @SuppressWarnings("unchecked")
    public static <T> XesParameterExporter exportValue(String name, ValueAccessor accessor, XesWriterMethod<T> writerMethod) {
        return (state, writer) -> {
            try {
                final T value = (T) accessor.getValue(state);
                writerMethod.export(writer, name, value);
            } catch (Exception cause) {
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
