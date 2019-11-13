package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.Set;

import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityArray;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.ValueCasts;
import au.csiro.data61.aap.util.MethodResult;

/**
 * TransactionScope
 */
public class TransactionScope extends Scope {
    public static final Variable ANY = new Variable(SolidityString.DEFAULT_INSTANCE, "any",
            VariableCategory.SCOPE_VARIABLE, "any");
    public static final Set<Variable> DEFAULT_VARIABLES;

    private final Variable senders;
    private final Variable recipients;

    public TransactionScope(Variable senders, Variable recipients) {
        super(DEFAULT_VARIABLES);
        
        assert isValidAddressListVariable(senders);
        assert isValidAddressListVariable(recipients);
        this.recipients = recipients;
        this.senders = senders;
    }

    static {
        DEFAULT_VARIABLES = new HashSet<>();
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "tx.blockHash");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.blockNumber");
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, "tx.from");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.gas");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.gasPrice");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "tx.hash");
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, "tx.input");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.nonce");
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, "tx.to");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.transactionIndex");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.value");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.v");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.r");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.s");
    }

    public void setEnclosingScope(BlockScope enclosingScope) {
        super.setEnclosingScope(enclosingScope);
    }

    public Variable getSenders() {
        return senders;
    }

    public Variable getRecipients() {
        return recipients;
    }

    public static boolean isValidAddressListVariable(Variable variable) {
        return variable != null && (variable == ANY || (variable.getType() instanceof SolidityArray
                && ValueCasts.isCastSupported(((SolidityArray)variable.getType()).getBaseType(), SolidityAddress.DEFAULT_INSTANCE)));
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        return MethodResult.ofError("Method not implemented.");
    }

    
}