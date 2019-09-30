package au.csiro.data61.aap.library.types;

import au.csiro.data61.aap.util.MethodResult;

public abstract class SolidityType<T> {
    public abstract String getTypeName();   
    public abstract MethodResult<T> cast(Object obj); 
    public abstract boolean castSupportedFor(Class<?> cl);

    protected final MethodResult<T> castNotSupportedResult(Object obj) {
        final String errorMessage = String.format("Cast for class '%s' not supported.", obj.getClass().getName());
        return MethodResult.ofError(errorMessage);
    }

    @Override
    public String toString() {
        return this.getTypeName();
    }
}