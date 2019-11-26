package au.csiro.data61.aap.etl.configuration;

import java.math.BigInteger;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.filters.FilterPredicate;
import au.csiro.data61.aap.etl.core.values.EthereumVariables;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.core.values.Variables;

/**
 * BlockNumberSpecification
 */
public class BlockNumberSpecification {
    private final ValueAccessor accessor;
    private final FilterPredicate<BigInteger> stopCriterion;
    private final Type type;

    private BlockNumberSpecification(ValueAccessor accessor, FilterPredicate<BigInteger> stopCriterion, Type type) {
        this.accessor = accessor;
        this.stopCriterion = stopCriterion;
        this.type = type;
    }

    ValueAccessor getValueAccessor() {
        return this.accessor;
    }
    
    FilterPredicate<BigInteger> getStopCriterion() {
        return this.stopCriterion;
    }


    Type getType() {
        return this.type;
    }

    public static BlockNumberSpecification ofVariableName(String name) {
        assert name != null;
        final ValueAccessor accessor = Variables.createValueAccessor(name);            
        return new BlockNumberSpecification(accessor, createStopCriterion(accessor), Type.VARIABLE);
    }

    public static BlockNumberSpecification ofBlockNumber(ValueAccessorSpecification number) {
        assert number != null;
        final ValueAccessor accessor = number.getValueAccessor();
        return new BlockNumberSpecification(accessor, createStopCriterion(accessor), Type.NUMBER);
    }

    public static BlockNumberSpecification ofCurrent() {
        final ValueAccessor accessor = EthereumVariables.currentBlockNumberAccessor();
        return new BlockNumberSpecification(accessor, createStopCriterion(accessor), Type.CURRENT);
    }

    private static final FilterPredicate<BigInteger> createStopCriterion(ValueAccessor accessor) {
        final Value endValue = new Value();
        return (state, blockNumber) -> {
            if (endValue.blockNumber == null) {
                try {
                    endValue.blockNumber = (BigInteger)accessor.getValue(state);
                }
                catch (ProgramException ex) {
                    final String message = "Error when retrieving the current block number.";
                    state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, ex);
                    endValue.blockNumber = BigInteger.ZERO.subtract(BigInteger.ONE);
                }
            }
            return endValue.blockNumber.compareTo(blockNumber) < 0;
        };
    }

    private static class Value {
        private BigInteger blockNumber;
    }

    public static BlockNumberSpecification ofEarliest() {
        final ValueAccessor accessor = ValueAccessorSpecification.integerLiteral(BigInteger.ZERO).getValueAccessor();
        return new BlockNumberSpecification(accessor, null, Type.EARLIEST);
    }

    public static BlockNumberSpecification ofContinuous() {
        return new BlockNumberSpecification(null, (state, blockNumber) -> false, Type.CONTINUOUS);
    }
    
    static enum Type {
        CONTINUOUS,
        CURRENT,
        EARLIEST,
        NUMBER,
        VARIABLE
    }

}