package au.csiro.data61.aap.etl.core;

/**
 * EtlException
 */
public class EtlException extends Exception {
    private static final long serialVersionUID = -2572372574030302250L;

    public EtlException(String message) {
        super(message);
    }

    public EtlException(String message, Throwable cause) {
        super(message, cause);
    }
}