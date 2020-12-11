package blf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blf.util.MethodResult;

/**
 * SpecificationParserResult
 */
public class EthqlProcessingResult<T> {
    private static final String ERROR_MESSAGE_JOIN = ", ";

    private final T result;
    private final EthqlProcessingError[] errors;

    private EthqlProcessingResult(T result, EthqlProcessingError[] errors) {
        assert errors == null ? true : 0 <= errors.length && Arrays.stream(errors).allMatch(error -> error != null);
        this.result = result;
        this.errors = errors == null ? new EthqlProcessingError[0] : Arrays.copyOf(errors, errors.length);
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
        return this.errorStream().map(error -> error.getErrorMessage()).collect(Collectors.joining(ERROR_MESSAGE_JOIN));
    }

    public EthqlProcessingError getError(int index) {
        assert 0 <= index && index < this.errorCount();
        return this.errors[index];
    }

    public List<EthqlProcessingError> getErrors() {
        return this.errorStream().collect(Collectors.toList());
    }

    public Stream<EthqlProcessingError> errorStream() {
        return Arrays.stream(this.errors);
    }

    public static <T> EthqlProcessingResult<T> ofError(String message) {
        return ofError(message, null);
    }

    public static <T> EthqlProcessingResult<T> ofUnsuccessfulMethodResult(MethodResult<?> result) {
        assert result != null && !result.isSuccessful();
        return ofError(result.getErrorMessage(), result.getErrorCause());
    }

    public static <T> EthqlProcessingResult<T> ofError(String message, Throwable cause) {
        assert message != null && !message.trim().isEmpty();
        final EthqlProcessingError[] errors = new EthqlProcessingError[1];
        errors[0] = new EthqlProcessingError(0, 0, message, cause);
        return new EthqlProcessingResult<T>(null, errors);
    }

    public static <T> EthqlProcessingResult<T> ofErrors(Stream<EthqlProcessingError> errorStream) {
        assert errorStream != null;

        final EthqlProcessingError[] errors = errorStream.toArray(EthqlProcessingError[]::new);
        assert 0 < errors.length;

        return new EthqlProcessingResult<T>(null, errors);
    }

    public static <T> EthqlProcessingResult<T> ofResult(T result) {
        return new EthqlProcessingResult<T>(result, null);
    }
}
