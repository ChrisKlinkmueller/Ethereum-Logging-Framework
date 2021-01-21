package blf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import blf.blockchains.ethereum.classes.EthereumLogEntrySignature;
import io.reactivex.annotations.NonNull;

/**
 * LogEntrySignatureSpecification
 */
public class LogEntrySignatureSpecification {
    private final EthereumLogEntrySignature signature;

    private LogEntrySignatureSpecification(EthereumLogEntrySignature signature) {
        this.signature = signature;
    }

    public EthereumLogEntrySignature getSignature() {
        return this.signature;
    }

    public static LogEntrySignatureSpecification of(String eventName, ParameterSpecification... parameters) {
        return of(eventName, Arrays.asList(parameters));
    }

    public static LogEntrySignatureSpecification of(@NonNull String eventName, @NonNull List<ParameterSpecification> parameters) {

        return new LogEntrySignatureSpecification(
            new EthereumLogEntrySignature(
                eventName,
                parameters.stream().map(ParameterSpecification::getParameter).collect(Collectors.toList())
            )
        );
    }

}
