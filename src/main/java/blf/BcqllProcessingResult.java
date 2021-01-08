package blf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blf.util.MethodResult;
import io.reactivex.annotations.NonNull;

/**
 * SpecificationParserResult
 */
public class BcqllProcessingResult<T> {
    private static final String ERROR_MESSAGE_JOIN = ", ";

    private final T result;
    private final BcqlProcessingError[] errors;

    private BcqllProcessingResult(T result, BcqlProcessingError[] errors) {
        this.result = result;
        this.errors = errors == null ? new BcqlProcessingError[0] : Arrays.copyOf(errors, errors.length);
    }

    public boolean isSuccessful() {
        return this.errors.length == 0;
    }

    public T getResult() {
        return this.result;
    }

    public int errorCount() {
        return this.errors.length;
    }

    public String getErrorMessage() {
        return this.errorStream().map(BcqlProcessingError::getErrorMessage).collect(Collectors.joining(ERROR_MESSAGE_JOIN));
    }

    public BcqlProcessingError getError(int index) {
        return this.errors[index];
    }

    public List<BcqlProcessingError> getErrors() {
        return this.errorStream().collect(Collectors.toList());
    }

    public Stream<BcqlProcessingError> errorStream() {
        return Arrays.stream(this.errors);
    }

    public static <T> BcqllProcessingResult<T> ofError(String message) {
        return ofError(message, null);
    }

    public static <T> BcqllProcessingResult<T> ofUnsuccessfulMethodResult(@NonNull MethodResult<?> result) {
        return ofError(result.getErrorMessage(), result.getErrorCause());
    }

    public static <T> BcqllProcessingResult<T> ofError(@NonNull String message, Throwable cause) {
        final BcqlProcessingError[] errors = new BcqlProcessingError[1];
        errors[0] = new BcqlProcessingError(0, 0, message, cause);
        return new BcqllProcessingResult<>(null, errors);
    }

    public static <T> BcqllProcessingResult<T> ofErrors(@NonNull Stream<BcqlProcessingError> errorStream) {

        final BcqlProcessingError[] errors = errorStream.toArray(BcqlProcessingError[]::new);
        assert 0 < errors.length;

        return new BcqllProcessingResult<>(null, errors);
    }

    public static <T> BcqllProcessingResult<T> ofResult(T result) {
        return new BcqllProcessingResult<T>(result, null);
    }
}
