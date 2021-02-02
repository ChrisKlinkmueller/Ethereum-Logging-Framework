package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumSmartContractQuery;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * SmartContractFilter
 */
public class EthereumSmartContractFilterInstruction extends Instruction {
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
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        final String address;
        try {
            address = (String) this.contractAddress.getValue(state);

            for (EthereumSmartContractQuery query : this.queries) {
                query.query(address, ethereumProgramState);
            }
        } catch (ProgramException e) {
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(e.getMessage(), e);
        }

        this.executeNestedInstructions(state);
    }

}
