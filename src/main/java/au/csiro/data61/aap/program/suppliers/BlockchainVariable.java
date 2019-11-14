package au.csiro.data61.aap.program.suppliers;

import java.util.function.Function;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.program.types.ValueCasts.ValueCast;

/**
 * BlockchainConstant
 */
public class BlockchainVariable<T> implements Variable {
    private final SolidityType type;
    private final Function<T, Object> valueAccessor;
    private final ValueCast valueCast;
    private final String name;
    private T valueProvider;

    public BlockchainVariable(BlockchainVariable<T> original) {
        assert original != null;
        this.type = original.getType();
        this.valueAccessor = original.valueAccessor;
        this.name = original.name;
        this.valueCast = original.valueCast;
    }

    public BlockchainVariable(SolidityType type, String name, Function<T, Object> valueAccessor) {
        this(type, name, valueAccessor, null);
    }
    
    public BlockchainVariable(SolidityType type, String name, Function<T, Object> valueAccessor, ValueCast valueCast) {
        assert type != null;
        assert name != null;
        assert valueAccessor != null;
        this.valueCast = valueCast;
        this.valueAccessor = valueAccessor;
        this.type = type;
        this.name = name;
    }

    public void setValueProvider(T object) {
        assert object != null;
        this.valueProvider = object; 
    }

    @Override
    public Object getValue() throws Throwable {
        assert this.valueProvider != null;
        final Object value = this.valueAccessor.apply(this.valueProvider);
        return this.valueCast == null ? value : this.valueCast.cast(value);
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }
}