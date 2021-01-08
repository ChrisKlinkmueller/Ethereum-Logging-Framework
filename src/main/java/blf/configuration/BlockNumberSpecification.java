package blf.configuration;

import java.math.BigInteger;

import blf.core.exceptions.ProgramException;
import blf.core.interfaces.FilterPredicate;
import blf.blockchains.ethereum.variables.EthereumVariables;
import blf.core.values.ValueAccessor;
import blf.core.values.Variables;
import io.reactivex.annotations.NonNull;

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

    public static BlockNumberSpecification ofVariableName(@NonNull String name) {
        final ValueAccessor accessor = Variables.createValueAccessor(name);
        return new BlockNumberSpecification(accessor, createStopCriterion(accessor), Type.VARIABLE);
    }

    public static BlockNumberSpecification ofBlockNumber(@NonNull ValueAccessorSpecification number) {
        final ValueAccessor accessor = number.getValueAccessor();
        return new BlockNumberSpecification(accessor, createStopCriterion(accessor), Type.NUMBER);
    }

    public static BlockNumberSpecification ofCurrent() {
        final ValueAccessor accessor = EthereumVariables.currentBlockNumberAccessor();
        return new BlockNumberSpecification(accessor, createStopCriterion(accessor), Type.CURRENT);
    }

    private static FilterPredicate<BigInteger> createStopCriterion(ValueAccessor accessor) {
        final Value endValue = new Value();
        return (state, blockNumber) -> {
            if (endValue.blockNumber == null) {
                try {
                    endValue.blockNumber = (BigInteger) accessor.getValue(state);
                } catch (ProgramException ex) {
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

    public static BlockNumberSpecification ofEarliest() throws BuildException {
        final ValueAccessor accessor = ValueAccessorSpecification.integerLiteral(BigInteger.ZERO).getValueAccessor();
        return new BlockNumberSpecification(accessor, null, Type.EARLIEST);
    }

    public static BlockNumberSpecification ofContinuous() {
        return new BlockNumberSpecification(null, (state, blockNumber) -> false, Type.CONTINUOUS);
    }

    enum Type {
        CONTINUOUS,
        CURRENT,
        EARLIEST,
        NUMBER,
        VARIABLE
    }

}
