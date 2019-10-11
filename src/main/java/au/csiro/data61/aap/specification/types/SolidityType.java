package au.csiro.data61.aap.specification.types;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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

    public static SolidityType<?> createType(String keyword) {
        if (keyword == null) {
            return null;
        }

        for (Function<String, SolidityType<?>> factoryMethod : FACTORY_METHODS) {
            final SolidityType<?> type = factoryMethod.apply(keyword);
            if (type != null) {
                return type;
            }
        }

        return null;
    }
    
    private static final List<Function<String, SolidityType<?>>> FACTORY_METHODS = Arrays.asList(
        AddressType::createAddressType,
        ArrayType::createArrayType,
        BoolType::createBoolType,
        BytesType::createBytesType,
        FixedType::createFixedType,
        IntegerType::createIntegerType,
        StringType::createStringType
    );



    
}