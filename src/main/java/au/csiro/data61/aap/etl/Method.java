package au.csiro.data61.aap.etl;

/**
 * Method
 */
@FunctionalInterface
public interface Method {
    public Object call(Object[] parameters, EtlState state) throws EtlException;    
}