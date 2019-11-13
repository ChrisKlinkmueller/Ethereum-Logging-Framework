package au.csiro.data61.aap.program.suppliers;

import java.util.function.Function;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.program.types.ValueCasts.ValueCast;

/**
 * BlockchainConstant
 */
public class BlockchainConstant<T> implements ValueContainer {
    private final SolidityType type;
    private final Function<T, Object> valueAccessor;
    private final ValueCast valueCast;
    private T valueProvider;
    
    public BlockchainConstant(SolidityType type, Function<T, Object> getter, ValueCast cast) {
        this.valueCast = cast;
        this.valueAccessor = getter;
        this.type = type;
    }

    public void setValueProvider(T object) {
        assert object != null;
        this.valueProvider = object; 
    }

    @Override
    public Object getValue() throws Throwable {
        assert this.valueProvider != null;
        final Object value = this.valueAccessor.apply(this.valueProvider);
        return this.valueCast.cast(value);
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }
}