package au.csiro.data61.aap.elf.core.writers;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;

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

    public static XesParameter boolParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addBooleanValue));
    }

    public static XesParameter boolListParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addBooleanList));
    }

    public static XesParameter dateParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addDateValue));
    }

    public static XesParameter dateListParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addDateList));
    }

    public static XesParameter floatParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addFloatValue));
    }

    public static XesParameter floatListParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addFloatList));
    }

    public static XesParameter integerParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addIntValue));
    }

    public static XesParameter integerListParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addIntList));
    }

    public static XesParameter stringParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringValue));
    }

    public static XesParameter stringListParameter(String name, ValueAccessor accessor) {
        assert name != null && accessor != null;
        return new XesParameter(exportValue(name, accessor, XesWriter::addStringList));
    }

    public static <T> XesParameterExporter exportValue(String name, ValueAccessor accessor, XesWriterMethod writerMethod) {
        return (state, writer) -> {
            try {
                final Object value = accessor.getValue(state);
                writerMethod.export(writer, name, value);
            } catch (Throwable cause) {
                throw new ProgramException(String.format("Error exporting xes attribute '%s'.", name), cause);
            }
        };
    }

    @FunctionalInterface
    static interface XesParameterExporter {
        public void exportValue(ProgramState state, XesWriter writer) throws ProgramException;
    }

    @FunctionalInterface
    private static interface XesWriterMethod {
        public void export(XesWriter writer, String name, Object value);
    }

}
