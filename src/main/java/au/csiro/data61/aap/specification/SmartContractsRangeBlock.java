package au.csiro.data61.aap.specification;

/**
 * SmartContractsRangeBlock
 */
public class SmartContractsRangeBlock extends Block {
    private final ValueSource addresses;

    public SmartContractsRangeBlock(ValueSource addresses) {
        assert addresses != null;
        this.addresses = addresses;
    }

    public ValueSource getAddresses() {
        return this.addresses;
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}