package au.csiro.data61.aap.etl;

/**
 * EthqlProcessingException
 */
public class EthqlProcessingException extends Exception {
    private static final long serialVersionUID = 1L;

    public EthqlProcessingException(String message) {
        super(message);
    }

    public EthqlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}