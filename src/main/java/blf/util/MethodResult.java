package blf.util;

/**
 * MethodResult
 */
public class MethodResult<T> {
    private final String errorMessage;
    private final Throwable errorCause;
    private final T result;

    private MethodResult(T result, String errorMessage, Throwable errorCause) {
        this.result = result;
        this.errorMessage = errorMessage;
        this.errorCause = errorCause;
    }

    public Throwable getErrorCause() {
        return this.errorCause;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public T getResult() {
        return this.result;
    }

    public boolean isSuccessful() {
        return this.errorCause == null && this.errorMessage == null;
    }

    public static <T> MethodResult<T> ofResult(T result) {
        return new MethodResult<T>(result, null, null);
    }

    public static <T> MethodResult<T> ofResult() {
        return new MethodResult<T>(null, null, null);
    }

    public static <T> MethodResult<T> ofError(String errorMessage) {
        assert errorMessage != null && !errorMessage.trim().isEmpty();
        return new MethodResult<T>(null, errorMessage, null);
    }

    public static <T> MethodResult<T> ofError(String errorMessage, Throwable errorCause) {
        assert errorMessage != null && !errorMessage.trim().isEmpty() && errorCause != null;
        return new MethodResult<T>(null, errorMessage, errorCause);
    }

    public static <T> MethodResult<T> ofError(Throwable errorCause) {
        assert errorCause != null;
        return new MethodResult<T>(null, errorCause.getMessage(), errorCause);
    }

    public static <S, T> MethodResult<T> ofError(MethodResult<S> errorResult) {
        assert errorResult != null && !errorResult.isSuccessful();
        return new MethodResult<T>(null, errorResult.getErrorMessage(), errorResult.getErrorCause());
    }
}
