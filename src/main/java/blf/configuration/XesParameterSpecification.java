package blf.configuration;

import blf.core.writers.XesParameter;
import io.reactivex.annotations.NonNull;

/**
 * XesParameterSpecification
 */
public class XesParameterSpecification {
    private final XesParameter parameter;

    private XesParameterSpecification(XesParameter parameter) {
        this.parameter = parameter;
    }

    XesParameter getParameter() {
        return this.parameter;
    }

    public static XesParameterSpecification ofBooleanParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.boolParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofBooleanListParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.boolListParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofDateParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.dateParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofDateListParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.dateListParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofIntegerParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.integerParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofIntegerListParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.integerListParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofFloatParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.floatParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofFloatListParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.floatListParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofStringParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.stringParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofStringListParameter(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new XesParameterSpecification(XesParameter.stringListParameter(name, accessor.getValueAccessor()));
    }
}
