package blf.core.parameters;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.writers.XesWriter;

/**
 * XesParameter
 */
public class XesParameter {
    private final XesParameterExporter attributeExporter;

    private XesParameter(XesParameterExporter attributeExporter) {
        this.attributeExporter = attributeExporter;
    }

    public static XesParameter boolParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addBooleanValue));
    }

    public static XesParameter boolListParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addBooleanList));
    }

    public static XesParameter dateParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addDateValue));
    }

    public static XesParameter dateListParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addDateList));
    }

    public static XesParameter floatParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addFloatValue));
    }

    public static XesParameter floatListParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addFloatList));
    }

    public static XesParameter integerParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addIntValue));
    }

    public static XesParameter integerListParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addIntList));
    }

    public static XesParameter stringParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringValue));
    }

    public static XesParameter stringListParameter(String name, ValueAccessor accessor) {
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringList));
    }

    @SuppressWarnings("unchecked")
    public static <T> XesParameterExporter exportValue(String name, ValueAccessor accessor, XesWriterMethod<T> writerMethod) {
        return (state, writer) -> {
            try {
                final T value = (T) accessor.getValue(state);
                writerMethod.export(writer, name, value);
            } catch (Exception e) {
                final String errorMsg = String.format("Error while exporting xes attribute '%s'.", name);
                ExceptionHandler.getInstance().handleException(errorMsg, e);
            }
        };
    }

    public void exportAttribute(ProgramState state, XesWriter writer) {
        this.attributeExporter.exportValue(state, writer);
    }

    @FunctionalInterface
    interface XesParameterExporter {
        void exportValue(ProgramState state, XesWriter writer);
    }

    @FunctionalInterface
    private interface XesWriterMethod<T> {
        void export(XesWriter writer, String name, T value);
    }

}
