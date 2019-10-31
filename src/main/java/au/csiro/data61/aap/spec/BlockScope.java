package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.IntegerType;
import au.csiro.data61.aap.spec.types.StringType;
import au.csiro.data61.aap.state.ProgramState;

/**
 * BlockScope
 */
public class BlockScope extends Scope {
    public static final Variable EARLIEST = new Variable(IntegerType.DEFAULT_INSTANCE, "earliest", true, 0);
    public static final Variable CURRENT = new Variable(StringType.DEFAULT_INSTANCE, "current", true, "current");
    public static final Variable PENDING = new Variable(StringType.DEFAULT_INSTANCE, "pending", true, "pending");

    private final Variable fromBlockNumber;
    private final Variable toBlockNumber;

    public BlockScope(GlobalScope enclosingScope, Variable fromBlockNumber, Variable toBlockNumber) {
        super(enclosingScope);
        assert enclosingScope != null;
        assert isValidBlockNumberVariable(fromBlockNumber);
        assert isValidBlockNumberVariable(toBlockNumber);
        this.fromBlockNumber = fromBlockNumber;
        this.toBlockNumber = toBlockNumber;
    }

    public static boolean isValidBlockNumberVariable(Variable variable) {
        return variable != null && (
               variable == PENDING
            || variable == EARLIEST 
            || variable == CURRENT
            || IntegerType.DEFAULT_INSTANCE.castableFrom(variable.getType())
        );
    } 

    public Variable getFromBlockNumber() {
        return this.fromBlockNumber;
    }

    public Variable getToBlockNumber() {
        return this.toBlockNumber;
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException();
    }
    
}