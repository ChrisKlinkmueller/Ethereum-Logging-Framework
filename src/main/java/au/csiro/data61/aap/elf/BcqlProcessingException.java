package au.csiro.data61.aap.elf;

/**
 * EthqlProcessingException
 */
public class BcqlProcessingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BcqlProcessingException(String message) {
        super(message);
    }

    public BcqlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
