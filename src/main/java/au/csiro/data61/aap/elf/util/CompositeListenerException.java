package au.csiro.data61.aap.elf.util;

/**
 * EtlException
 */
public class CompositeListenerException extends Exception {
    private static final long serialVersionUID = -2572372574030302250L;

    public CompositeListenerException(String message) {
        super(message);
    }

    public CompositeListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
