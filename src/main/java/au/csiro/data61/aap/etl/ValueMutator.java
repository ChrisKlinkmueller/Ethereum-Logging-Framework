package au.csiro.data61.aap.etl;

/**
 * ValueMutator
 */
@FunctionalInterface
public interface ValueMutator {
    public void setValue(Object value, EtlState state) throws EtlException;
    
    public static ValueMutator createVariableMutator(String name) {
        assert name != null;
        return (value, state) -> state.getValueStore().setValue(name, value);
    }
}