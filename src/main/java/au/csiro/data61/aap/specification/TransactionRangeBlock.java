package au.csiro.data61.aap.specification;

/**
 * TransactionRangeBlock
 */
public class TransactionRangeBlock extends Block {
    private final ValueSource transactionSenders;
    private final ValueSource transactionRecipients;

    public TransactionRangeBlock(ValueSource transactionSenders, ValueSource transactionReciptients) {
        assert transactionSenders != null;
        assert transactionReciptients != null;
        this.transactionRecipients = transactionReciptients;
        this.transactionSenders = transactionSenders;
    }

    public ValueSource getTransactionSenders() {
        return this.transactionSenders;
    }

    public ValueSource getTransactionRecipients() {
        return this.transactionRecipients;
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}