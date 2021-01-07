package blf.configuration;

import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

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

    public static TypedValueAccessorSpecification of(@NonNull String type, @NonNull ValueAccessorSpecification accessor) {
        return new TypedValueAccessorSpecification(type, accessor.getValueAccessor());
    }
}
