package au.csiro.data61.aap.elf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.elf.EthqlProcessingEvent.Type;
import au.csiro.data61.aap.elf.util.MethodResult;

/**
 * SpecificationParserResult
 */
public class EthqlProcessingResult<T> {
    private static final String ERROR_MESSAGE_JOIN = System.lineSeparator();

    private final T result;
    private final EthqlProcessingEvent[] events;

    private EthqlProcessingResult(T result, EthqlProcessingEvent[] events) {
        assert events == null ? true : 0 <= events.length && Arrays.stream(events).allMatch(error -> error != null);
        this.result = result;
        this.events = events == null ? new EthqlProcessingEvent[0] : Arrays.copyOf(events, events.length);
    }

    public boolean isSuccessful() {
        return this.events.length == 0;
    }

    public T getResult() {
        return this.result;
    }

    public int eventCount() {
        return this.events.length;
    }

    public String getEventMessage() {
        return this.eventStream().map(error -> error.getErrorMessage()).collect(Collectors.joining(ERROR_MESSAGE_JOIN));
    }

    public EthqlProcessingEvent getEvent(int index) {
        assert 0 <= index && index < this.eventCount();
        return this.events[index];
    }

    public List<EthqlProcessingEvent> getEvents() {
        return this.eventStream().collect(Collectors.toList());
    }

    public Stream<EthqlProcessingEvent> eventStream() {
        return Arrays.stream(this.events);
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
        final EthqlProcessingEvent[] errors = new EthqlProcessingEvent[1];
        errors[0] = new EthqlProcessingEvent(Type.ERROR, 0, 0, message, cause);
        return new EthqlProcessingResult<T>(null, errors);
    }

    public static <T> EthqlProcessingResult<T> ofErrors(Stream<EthqlProcessingEvent> errorStream) {
        assert errorStream != null;

        final EthqlProcessingEvent[] errors = errorStream.toArray(EthqlProcessingEvent[]::new);
        assert 0 < errors.length;

        return new EthqlProcessingResult<T>(null, errors);
    }

    public static <T> EthqlProcessingResult<T> ofResult(T result) {
        return new EthqlProcessingResult<T>(result, null);
    }
}
