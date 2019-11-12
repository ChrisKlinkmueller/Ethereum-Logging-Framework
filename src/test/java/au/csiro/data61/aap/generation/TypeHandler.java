package au.csiro.data61.aap.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityString;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * TypeGenerator
 */
class TypeHandler {    
    private final static int MAX_TYPE_PROBABILITY = 125;
    private final static int MAX_VALUE_TYPE_PROBABILITY = 100;

    private final Random random;
    private final TypeGenerator[] generators = {
        new TypeGenerator(5, this::generateAddressType),
        new TypeGenerator(10, this::generateBoolType),
        new TypeGenerator(35, this::generateBytesType),
        new TypeGenerator(60, this::generateFixedType),
        new TypeGenerator(95, this::generateIntegerType),
        new TypeGenerator(MAX_VALUE_TYPE_PROBABILITY, this::generateStringType),
        new TypeGenerator(MAX_TYPE_PROBABILITY, this::generateArrayType),
    };
    private final Map<Class<? extends SolidityType>, String> baseKeywords;
    private final Map<Class<? extends SolidityType>, Function<SolidityType, String>> keywordCreators;

    public TypeHandler(Random random) {
        this.random = random;

        this.baseKeywords = new HashMap<>();
        this.baseKeywords.put(SolidityAddress.class, "address");
        this.baseKeywords.put(SolidityBool.class, "bool");
        this.baseKeywords.put(SolidityBytes.class, "bytes");
        this.baseKeywords.put(SolidityFixed.class, "fixed");
        this.baseKeywords.put(SolidityInteger.class, "int");
        this.baseKeywords.put(SolidityString.class, "string");

        this.keywordCreators = new HashMap<>();
        this.keywordCreators.put(SolidityAddress.class, this::serializeAddress);
        this.keywordCreators.put(SolidityBool.class, this::serializeBool);
        this.keywordCreators.put(SolidityBytes.class, this::serializeBytes);
        this.keywordCreators.put(SolidityFixed.class, this::serializeFixed);
        this.keywordCreators.put(SolidityInteger.class, this::serializeInteger);
        this.keywordCreators.put(SolidityString.class, this::serializeString);
    }    

    public String toBaseKeyword(SolidityType type) {
        assert type != null;
        if (type.getClass().equals(SolidityArray.class)) {
            return String.format("%s[]", this.toBaseKeyword(((SolidityArray)type).getBaseType()));
        }

        assert this.baseKeywords.containsKey(type.getClass());
        return this.baseKeywords.get(type.getClass());
    }

    public String toKeyword(SolidityType type) {
        assert type != null && this.keywordCreators.containsKey(type.getClass());
        return this.keywordCreators.get(type.getClass()).apply(type);
    }

    private String serializeAddress(SolidityType type) {
        return this.baseKeywords.get(SolidityAddress.class);
    }

    private String serializeBool(SolidityType type) {
        return this.baseKeywords.get(SolidityBool.class);
    }

    private String serializeBytes(SolidityType type) {
        final SolidityBytes bytes = (SolidityBytes)type;
        final String keyword = this.baseKeywords.get(SolidityBytes.class);
        
        if (bytes.isDynamic()) {
            return keyword;
        }
        
        if (bytes.getLength() == 1 && random.nextBoolean()) {
            return "byte";
        }

        return String.format("%s%s", keyword, bytes.getLength());
    }

    private String serializeFixed(SolidityType type) {
        final SolidityFixed fixed = (SolidityFixed)type;
        final String keyword = this.baseKeywords.get(SolidityFixed.class);
        final String signed = fixed.isSigned() ? "" : "u";

        if (fixed.getM() == 128 && fixed.getN() == 18 && random.nextBoolean()) {
            return String.format("%s%s", signed, keyword);
        }

        return String.format("%s%s%sx%s", signed, keyword, fixed.getM(), fixed.getN());
    }

    private String serializeInteger(SolidityType type) {
        final SolidityInteger integer = (SolidityInteger)type;
        final String keyword = this.baseKeywords.get(SolidityInteger.class);
        final String signed = integer.isSigned() ? "" : "u";

        if (integer.getLength() == 256 && random.nextBoolean()) {
            return String.format("%s%s", signed, keyword);
        }

        return String.format("%s%s%s", signed, keyword, integer.getLength());
    }

    private String serializeString(SolidityType type) {
        return this.baseKeywords.get(SolidityString.class);
    }

    public SolidityType generateType() {
        return this.generateType(MAX_TYPE_PROBABILITY);
    }

    public SolidityArray generateArrayType() {
        return new SolidityArray(this.generateType(MAX_VALUE_TYPE_PROBABILITY));
    }    

    private SolidityType generateType(int maxGeneratorThreshold) {
        final int number = this.random.nextInt(maxGeneratorThreshold);
        for (TypeGenerator gen : this.generators) {
            if (number < gen.threshold) {
                return gen.generator.get();
            }
        }
        return null;
    }

    public SolidityAddress generateAddressType() {
        return SolidityAddress.DEFAULT_INSTANCE;
    }

    public SolidityBool generateBoolType() {
        return SolidityBool.DEFAULT_INSTANCE;
    }

    public SolidityBytes generateBytesType() {
        final int bitLength = this.random.nextInt(34);
        if (bitLength == 0) {
            return new SolidityBytes(1);
        }
        else if (bitLength == 33) {
            return new SolidityBytes();
        }
        else {
            return new SolidityBytes(bitLength);
        }
    }

    public SolidityFixed generateFixedType() {
        final boolean signed = this.random.nextBoolean();
        final int bitLength = generateIntegerLength();

        if (bitLength == 0) {
            return new SolidityFixed(signed);
        }

        return new SolidityFixed(signed, bitLength, this.random.nextInt(81));
    }
    
    public SolidityInteger generateIntegerType() {
        final boolean signed = this.random.nextBoolean();
        final int bitLength = generateIntegerLength();
        return bitLength == 0 
            ? new SolidityInteger(signed)
            : new SolidityInteger(signed, bitLength);
    } 

    public SolidityString generateStringType() {
        return SolidityString.DEFAULT_INSTANCE;
    }

    private int generateIntegerLength() {
        return 8 * this.random.nextInt(33);
    } 

    private static class TypeGenerator {
        final int threshold;
        final Supplier<SolidityType> generator;
        
        TypeGenerator(int threshold, Supplier<SolidityType> generator) {
            this.threshold = threshold;
            this.generator = generator;
        }
    }
}