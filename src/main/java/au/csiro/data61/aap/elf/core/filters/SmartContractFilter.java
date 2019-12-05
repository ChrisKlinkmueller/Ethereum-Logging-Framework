package au.csiro.data61.aap.elf.core.filters;

import java.util.List;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * SmartContractFilter
 */
public class SmartContractFilter extends Filter {
    private final SmartContractQuery query;
    private final String contract;

    public SmartContractFilter(String contract, SmartContractQuery query, List<Instruction> instructions) {
        super(instructions);
        assert contract != null;
        assert query != null;
        this.query = query;
        this.contract = contract;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        this.query.query(this.contract, state);
        this.execute(state);
    }

    
}