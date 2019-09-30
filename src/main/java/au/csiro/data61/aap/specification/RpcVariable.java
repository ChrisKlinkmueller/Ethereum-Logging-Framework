package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.library.types.SolidityType;

/**
 * RpcVariable
 */
public class RpcVariable implements ValueSource {
    private final String rpcVariableName;
    private final SolidityType<?> type;

    public RpcVariable(SolidityType<?> type, String rpcVariableName) {
        assert type != null;
        assert rpcVariableName != null && !rpcVariableName.trim().isEmpty();
        this.rpcVariableName = rpcVariableName;
        this.type = type;
    }

    public String getRpcVariableName() {
        return this.rpcVariableName;
    }

    @Override
    public SolidityType<?> getReturnType() {
        return this.type;
    }

}