package blf.core.filters;

import java.util.ArrayList;
import java.util.List;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;

/**
 * SmartContractFilter
 */
public class SmartContractFilter extends Filter {
    private final List<SmartContractQuery> queries;
    private final ValueAccessor contractAddress;

    public SmartContractFilter(ValueAccessor contractAddress, List<SmartContractQuery> queries, List<Instruction> instructions) {
        super(instructions);
        assert contractAddress != null;
        assert queries != null;
        this.queries = new ArrayList<>(queries);
        this.contractAddress = contractAddress;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final String address = (String) this.contractAddress.getValue(state);
        for (SmartContractQuery query : this.queries) {
            query.query(address, state);
        }
        this.executeInstructions(state);
    }

}
