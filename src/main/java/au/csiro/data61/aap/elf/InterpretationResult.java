package au.csiro.data61.aap.elf;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.InterpretationEvent.Type;

public class InterpretationResult<T> {

    /**
     * Creates a successful {@link InterpretationResult} that contains the {@code result} but no {@link InterpretationEvent}s.
     * @param <T> the type of the {@code result}
     * @param result the result
     * @return a successful {@link InterpretationResult}
     */
    public static <T> InterpretationResult<T> of(T result) {
        return new InterpretationResult<>(result, Collections.emptyList());
    }

    /**
     * Creates a successful {@link InterpretationResult} that contains the {@code result} and the {@code events}
     * all of which must not represent errors.
     * @param <T> the type of the {@code result}
     * @param result the result
     * @param events the events representing warnings and infos
     * @return a successful {@link InterpretationResult}
     * @throws IllegalArgumentException if exists event in events: event.getType() == Type.ERROR.
     */
    public static <T> InterpretationResult<T> of(T result, List<InterpretationEvent> events) {
        checkNotNull(events);
        checkArgument(events.stream().allMatch(Objects::nonNull));
        checkArgument(events.stream().allMatch(e -> e.getType() != Type.ERROR));
        final List<InterpretationEvent> eventsCopy = events.stream().collect(Collectors.toList());
        return new InterpretationResult<>(result, eventsCopy);
    }

    /**
     * Creates a failed {@link InterpretationResult} that contains the {@code events} where
     * at least one event must represent an error.
     * @param <T> the type of the result
     * @param events the events
     * @return a failed {@link InterpretationResult}
     * @throws IllegalArgumentException if not exists event in events: event.getType() == Type.ERROR.
     */
    public static <T> InterpretationResult<T> failure(List<InterpretationEvent> events) {
        checkNotNull(events);
        checkArgument(events.stream().allMatch(Objects::nonNull));
        checkArgument(events.stream().anyMatch(e -> e.getType() == Type.ERROR));
        final List<InterpretationEvent> eventsCopy = events.stream().collect(Collectors.toList());
        return new InterpretationResult<>(null, eventsCopy);
    }

    /**
     * Creates a failed {@link InterpretationResult} that contains the {@code events} where
     * at least one event must represent an error.
     * @param <T> the type of the result
     * @param events the events
     * @return a failed {@link InterpretationResult}
     * @throws IllegalArgumentException if not exists event in events: event.getType() == Type.ERROR.
     */
    public static <T> InterpretationResult<T> failure(InterpretationEvent... events) {
        return failure(List.of(events));
    }

    private final T result;
    private final List<InterpretationEvent> events;

    private InterpretationResult(T result, List<InterpretationEvent> events) {
        this.result = result;
        this.events = events;
    }

    public boolean isSuccess() {
        return this.events.stream().allMatch(e -> e.getType() != Type.ERROR);
    }

    public boolean isFailure() {
        return !this.isSuccess();
    }

    public T getResult() {
        checkState(this.isSuccess());
        return this.result;
    }

    public List<InterpretationEvent> getEvents() {
        checkState(this.isFailure());
        return this.events;
    }

    public <S> InterpretationResult<S> convertFailure() {
        checkState(this.isFailure());
        return new InterpretationResult<>(null, this.events);
    }

}
