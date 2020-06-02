package au.csiro.data61.aap.elf.library;

/**
 * LibraryException
 */
public class LibraryException extends Exception {
    private static final long serialVersionUID = 1L;

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }

}
