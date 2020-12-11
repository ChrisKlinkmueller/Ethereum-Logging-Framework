package blf.util;

/**
 * EtlException
 */
public class RootListenerException extends Exception {
    private static final long serialVersionUID = -2572372574030302250L;

    public RootListenerException(String message) {
        super(message);
    }

    public RootListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
