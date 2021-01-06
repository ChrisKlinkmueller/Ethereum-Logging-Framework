package blf.configuration;

import blf.core.filters.Parameter;
import io.reactivex.annotations.NonNull;

/**
 * LogEntryParameterSpecification
 */
public class ParameterSpecification {
    private final String paramName;
    private final String paramType;
    private final boolean isIndexed;

    private ParameterSpecification(String paramName, String paramType, boolean isIndexed) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.isIndexed = isIndexed;
    }

    Parameter getParameter() {
        return new Parameter(this.paramType, this.paramName, this.isIndexed);
    }

    public String getName() {
        return this.paramName;
    }

    public String getType() {
        return this.paramType;
    }

    public boolean isIndexed() {
        return this.isIndexed;
    }

    public static ParameterSpecification of(String paramName, String paramType) {
        return new ParameterSpecification(paramName, paramType, false);
    }

    public static ParameterSpecification of(@NonNull String paramName, @NonNull String paramType, boolean isIndexed) {
        return new ParameterSpecification(paramName, paramType, isIndexed);
    }

}
