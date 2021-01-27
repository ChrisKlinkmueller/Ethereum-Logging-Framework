package blf.blockchains.hyperledger.helpers;

import org.antlr.v4.runtime.misc.Pair;

import java.util.LinkedList;
import java.util.List;

public class HyperledgerQueryParameters {
    private final List<Pair<String, String>> outputParameters;
    private final String methodName;
    private final List<Pair<String, String>> inputParameters;

    public HyperledgerQueryParameters(
        List<Pair<String, String>> outputParameters,
        String methodName,
        List<Pair<String, String>> inputParameters
    ) {
        this.outputParameters = outputParameters;
        this.methodName = methodName;
        this.inputParameters = inputParameters;
    }

    public HyperledgerQueryParameters(Pair<String, String> outputVariable) {
        this.outputParameters = new LinkedList<>();
        outputParameters.add(outputVariable);
        this.methodName = null;
        this.inputParameters = null;
    }

    public String getMethodname() {
        return methodName;
    }

    public List<Pair<String, String>> getOutputParameters() {
        return outputParameters;
    }

    public List<Pair<String, String>> getInputParameters() {
        return inputParameters;
    }
}
