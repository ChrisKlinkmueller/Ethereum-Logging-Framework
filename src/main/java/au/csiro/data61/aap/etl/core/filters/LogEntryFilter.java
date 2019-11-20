package au.csiro.data61.aap.etl.core.filters;

import java.util.Arrays;
import java.util.List;

import org.web3j.abi.datatypes.Event;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * LogEntryFilter
 * To understand how to decode event data and topics, see: https://www.programcreek.com/java-api-examples/?class=org.web3j.abi.FunctionReturnDecoder&method=decode 
 */
public class LogEntryFilter extends Filter {
    private final ValueAccessor addresses;
    private final Event event;

    public LogEntryFilter(ValueAccessor addresses, Event event, Instruction... instructions) {
        this(addresses, event, Arrays.asList(instructions));
    }

    public LogEntryFilter(ValueAccessor addresses, Event event, List<Instruction> instructions) {
        super(instructions, EthereumVariables.getLogEntryValueCreators(), EthereumVariables.getLogEntryRemovers());
        assert event != null;
        assert instructions != null;
        this.addresses = addresses;
        this.event = event;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        throw new UnsupportedOperationException();
    }

    
}