package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.ArrayType;
import au.csiro.data61.aap.spec.types.BytesType;
import au.csiro.data61.aap.spec.types.StringType;
import au.csiro.data61.aap.state.ProgramState;

/**
 * TransactionScope
 */
public class TransactionScope extends Scope {
    public static final Variable ANY = new Variable(StringType.DEFAULT_INSTANCE, "any", true, "any");

    private final Variable senders;
    private final Variable recipients;

    public TransactionScope(Variable senders, Variable recipients) {
        assert isValidAddressListVariable(senders);
        assert isValidAddressListVariable(recipients);
        this.recipients = recipients;
        this.senders = senders;
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
        return variable != null && (
               variable == ANY
            || (variable.getType() instanceof ArrayType && BytesType.DEFAULT_INSTANCE.castableFrom((ArrayType)variable.getType()))
        );
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException();
    }

    
}