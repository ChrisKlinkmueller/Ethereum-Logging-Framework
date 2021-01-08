package blf.blockchains.hyperledger.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blf.blockchains.ethereum.reader.EthereumLogEntry;
import blf.core.parameters.Parameter;
import io.reactivex.annotations.NonNull;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Event;

import blf.core.state.ProgramState;
import org.web3j.abi.datatypes.Type;

/**
 * LogEntrySignature
 */
public class EthereumLogEntrySignature {
    private final String name;
    private final List<Parameter> parameters;
    private final Event event;
    private final String encodedSignature;

    public EthereumLogEntrySignature(final String name, final Parameter... parameters) {
        this(name, Arrays.asList(parameters));
    }

    public EthereumLogEntrySignature(@NonNull final String name, @NonNull final List<Parameter> parameters) {
        this.name = name;
        this.parameters = new ArrayList<>(parameters);
        this.event = new Event(this.name, parameters.stream().map(Parameter::getType).collect(Collectors.toList()));
        this.encodedSignature = EventEncoder.encode(this.event);
    }

    public String getName() {
        return this.name;
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public Parameter getParameter(final int index) {
        return this.parameters.get(index);
    }

    public Stream<Parameter> parameterStream() {
        return this.parameters.stream();
    }

    public boolean hasSignature(final EthereumLogEntry logEntry) {
        return !logEntry.getTopics().isEmpty() && logEntry.getTopics().get(0).equals(this.encodedSignature);
    }

    public void addLogEntryValues(ProgramState state, EthereumLogEntry logEntry) throws Exception {
        this.addTopics(state, logEntry);
        this.addData(state, logEntry);
    }

    private void addTopics(ProgramState state, EthereumLogEntry logEntry) throws Exception {
        final List<Parameter> topicParameters = this.getEntryParameters(true);
        assert logEntry.getTopics().size() == topicParameters.size() + 1;
        for (int i = 0; i < topicParameters.size(); i++) {
            final Parameter topic = topicParameters.get(i);
            Object value = TypeDecoder.instantiateType(topic.getType(), logEntry.getTopics().get(i + 1)).getValue();
            state.getValueStore().setValue(topic.getName(), value);
        }
    }

    private void addData(ProgramState state, EthereumLogEntry logEntry) {
        final List<Parameter> dataVariables = this.getEntryParameters(false);
        final List<Object> results = FunctionReturnDecoder.decode(logEntry.getData(), this.event.getNonIndexedParameters())
            .stream()
            .map(Type::getValue)
            .collect(Collectors.toList());
        assert dataVariables.size() == results.size();
        for (int i = 0; i < dataVariables.size(); i++) {
            String nameOfVariable = dataVariables.get(i).getName();
            Object value = results.get(i);
            state.getValueStore().setValue(nameOfVariable, value);
        }
    }

    private List<Parameter> getEntryParameters(boolean indexed) {
        return this.parameters.stream().filter(param -> param.isIndexed() == indexed).collect(Collectors.toList());
    }
}
