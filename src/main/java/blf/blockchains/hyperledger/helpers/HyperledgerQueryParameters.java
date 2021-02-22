package blf.blockchains.hyperledger.helpers;

/**
 * A helper class to store the parsed parameters for a Hyperledger query as they are performed in the
 * HyperledgerSmartContractFilterInstruction.
 *
 */

public class HyperledgerQueryParameters {
    private final String[] outputParameters;
    private final String methodName;
    private final String[] inputParameters;

    /**
     * Constructs the HyperledgerQueryParameters Class for a PublicFunctionQuery.
     *
     * @param outputParameters      Output parameters as defined in the manifest.
     * @param methodName            Method name as defined in the manifest.
     * @param inputParameters       Input parameters as defined in the manifest.
     */
    public HyperledgerQueryParameters(String[] outputParameters, String methodName, String[] inputParameters) {
        this.outputParameters = outputParameters;
        this.methodName = methodName;
        this.inputParameters = inputParameters;
    }

    /**
     * Constructs the HyperledgerQueryParameters Class for a PublicVariableQuery.
     *
     * @param outputParameters      Output parameters as defined in the manifest.
     */
    public HyperledgerQueryParameters(String[] outputParameters) {
        this.outputParameters = outputParameters;
        this.methodName = null;
        this.inputParameters = null;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getOutputParameters() {
        return outputParameters;
    }

    public String[] getInputParameters() {
        return inputParameters;
    }
}
