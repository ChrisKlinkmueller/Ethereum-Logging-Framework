package au.csiro.data61.aap.etl.configuration;

import au.csiro.data61.aap.etl.core.writers.XesParameter;

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
    
    public static XesParameterSpecification ofBooleanParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.boolParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofBooleanListParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.boolListParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofDateParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.dateParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofDateListParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.dateListParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofIntegerParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.integerParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofIntegerListParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.integerListParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofFloatParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.floatParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofFloatListParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.floatListParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofStringParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.stringParameter(name, accessor.getValueAccessor()));
    }
    
    public static XesParameterSpecification ofStringListParameter(String name, ValueAccessorSpecification accessor) {
        assert name != null && accessor != null;
        return new XesParameterSpecification(XesParameter.stringListParameter(name, accessor.getValueAccessor()));
    }

    public static XesParameterSpecification ofStringLiteral(String name, ValueAccessorSpecification spec) {
        assert name != null && spec != null;
        return new XesParameterSpecification(XesParameter.stringListParameter(name, spec.getValueAccessor()));
    }
}