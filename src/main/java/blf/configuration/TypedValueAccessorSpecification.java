package blf.configuration;

import blf.core.values.ValueAccessor;

/**
 * TypedValueAccessorSpecification
 */
public class TypedValueAccessorSpecification {
    private final String type;
    private final ValueAccessor accessor;

    private TypedValueAccessorSpecification(String type, ValueAccessor accessor) {
        this.type = type;
        this.accessor = accessor;
    }

    ValueAccessor getAccessor() {
        return accessor;
    }

    String getType() {
        return type;
    }

    public static TypedValueAccessorSpecification of(String type, ValueAccessorSpecification accessor) throws BuildException {
        assert type != null;
        assert accessor != null;
        return new TypedValueAccessorSpecification(type, accessor.getValueAccessor());
    }
}
