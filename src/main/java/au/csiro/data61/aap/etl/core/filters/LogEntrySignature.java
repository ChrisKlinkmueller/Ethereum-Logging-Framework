package au.csiro.data61.aap.etl.core.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Event;

import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.readers.EthereumLogEntry;

/**
 * LogEntrySignature
 */
public class LogEntrySignature {
    private final String name;
    private final List<LogEntryParameter> parameters;
    private final Event event;
    private final String encodedSignature;

    public LogEntrySignature(final String name, final LogEntryParameter... parameters) {
        this(name, Arrays.asList(parameters));
    }

    public LogEntrySignature(final String name, final List<LogEntryParameter> parameters) {
        assert name != null;
        assert parameters != null && parameters.stream().allMatch(Objects::nonNull);
        this.name = name;
        this.parameters = new ArrayList<>(parameters);
        this.event = new Event(this.name,
                parameters.stream().map(LogEntryParameter::getType).collect(Collectors.toList()));
        this.encodedSignature = EventEncoder.encode(this.event);
    }

    public String getName() {
        return this.name;
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public LogEntryParameter getParameter(final int index) {
        assert 0 <= index && index <= this.parameterCount();
        return this.parameters.get(index);
    }

    public Stream<LogEntryParameter> parameterStream() {
        return this.parameters.stream();
    }

    boolean hasSignature(final EthereumLogEntry logEntry) {
        return !logEntry.getTopics().isEmpty() && logEntry.getTopics().get(0).equals(this.encodedSignature);
    }

	public void addLogEntryValues(ProgramState state, EthereumLogEntry logEntry) throws Throwable {
        this.addTopics(state, logEntry);
        this.addData(state, logEntry);
	}

    private void addTopics(ProgramState state, EthereumLogEntry logEntry) throws Throwable {
        final List<LogEntryParameter> topicParameters = this.getEntryParameters(true);
        assert logEntry.getTopics().size() == topicParameters.size() + 1;
        for (int i = 0; i < topicParameters.size(); i++) {
            final LogEntryParameter topic = topicParameters.get(i);
            Object value = TypeDecoder.instantiateType(topic.getType(), logEntry.getTopics().get(i + 1));
            state.getValueStore().setValue(topic.getName(), value);
        }
    }

    private void addData(ProgramState state, EthereumLogEntry logEntry) throws Throwable {
        final List<LogEntryParameter> dataVariables = this.getEntryParameters(false);
        final List<Object> results = FunctionReturnDecoder.decode(logEntry.getData(), this.event.getNonIndexedParameters())
            .stream()
            .map(type -> type.getValue())
            .collect(Collectors.toList());
        assert dataVariables.size() == results.size();
        for (int i = 0; i < dataVariables.size(); i++) {
            String name = dataVariables.get(i).getName();
            Object value = results.get(i);
            state.getValueStore().setValue(name, value);
        }                
    }

    private List<LogEntryParameter> getEntryParameters(boolean indexed) {
        return this.parameters.stream()
            .filter(param -> param.isIndexed() == indexed)
            .collect(Collectors.toList());
    }
}