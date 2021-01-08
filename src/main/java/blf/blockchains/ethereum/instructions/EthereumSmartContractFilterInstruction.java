package blf.blockchains.ethereum.instructions;

import java.util.ArrayList;
import java.util.List;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.blockchains.hyperledger.classes.EthereumSmartContractQuery;
import blf.core.instructions.FilterInstruction;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

/**
 * SmartContractFilter
 */
public class EthereumSmartContractFilterInstruction extends FilterInstruction {
    private final List<EthereumSmartContractQuery> queries;
    private final ValueAccessor contractAddress;

    public EthereumSmartContractFilterInstruction(
        @NonNull ValueAccessor contractAddress,
        @NonNull List<EthereumSmartContractQuery> queries,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.queries = new ArrayList<>(queries);
        this.contractAddress = contractAddress;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        final String address = (String) this.contractAddress.getValue(state);
        for (EthereumSmartContractQuery query : this.queries) {
            query.query(address, ethereumProgramState);
        }

        this.executeInstructions(state);
    }

}
