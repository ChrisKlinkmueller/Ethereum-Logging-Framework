package au.csiro.data61.aap.specification;

/**
 * RpcVariable
 */
public class RpcVariable implements ValueSource {
    private final String rpcVariableName;
    private final String type;

    public RpcVariable(String type, String rpcVariableName) {
        assert type != null && !type.trim().isEmpty();
        assert rpcVariableName != null && !rpcVariableName.trim().isEmpty();
        this.rpcVariableName = rpcVariableName;
        this.type = type;
    }

    public String getRpcVariableName() {
        return this.rpcVariableName;
    }

    @Override
    public String getReturnType() {
        return this.type;
    }

}