package au.csiro.data61.aap.elf.core.exceptions;

/**
 * EtlException
 */
public class ProgramException extends Exception {
    private static final long serialVersionUID = -2572372574030302250L;

    public ProgramException(String message) {
        super(message);
    }

    public ProgramException(String message, Throwable cause) {
        super(message, cause);
    }
}
