package au.csiro.data61.aap.etl.configuration;

/**
 * BuildException
 */
public class BuildException extends Exception {
    private static final long serialVersionUID = -2906028448408996582L;

    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}