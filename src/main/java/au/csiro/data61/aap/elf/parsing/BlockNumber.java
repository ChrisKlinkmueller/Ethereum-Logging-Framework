package au.csiro.data61.aap.elf.parsing;

import java.math.BigInteger;

class BlockNumber {
    private final Type type;
    private final BigInteger value;

    private BlockNumber(Type type, BigInteger value) {
        this.value = value;
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public BigInteger getValue() {
        return this.value;
    }

    public boolean isDynamicValue() {
        return this.value == null;
    }

    public static BlockNumber ofContinuous() {
        return new BlockNumber(Type.CONTINUOUS, null);
    }

    public static BlockNumber ofEarliest() {
        return new BlockNumber(Type.EARLIEST, BigInteger.ZERO);
    }

    public static BlockNumber ofCurrent() {
        return new BlockNumber(Type.CURRENT, null);
    }

    public static BlockNumber ofInvalid() {
        return new BlockNumber(Type.INVALID, null);
    }

    public static BlockNumber ofVariable() {
        return new BlockNumber(Type.VARIABLE, null);
    }

    public static BlockNumber ofLiteral(BigInteger value) {
        assert value != null;
        return new BlockNumber(Type.LITERAL, value);
    }

    enum Type {
        INVALID,
        VARIABLE,
        LITERAL,
        CURRENT,
        EARLIEST,
        CONTINUOUS
    }
}
