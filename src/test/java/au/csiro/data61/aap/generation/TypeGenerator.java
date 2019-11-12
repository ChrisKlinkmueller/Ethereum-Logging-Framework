package au.csiro.data61.aap.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

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
class TypeGenerator {
    private static final int MAX_TYPE_PROBABILITY = 125;
    private static final int MAX_VALUE_TYPE_PROBABILITY = 100;
    private static final TypeCreator[] TYPE_CREATORS;
    private static final Map<Class<? extends SolidityType>, String> BASE_KEYWORDS;
    private static final Map<Class<? extends SolidityType>, BiFunction<Random, SolidityType, String>> KEYWORD_CREATORS;

    static {
        TYPE_CREATORS = new TypeCreator[]{
            new TypeCreator(5, TypeGenerator::generateAddressType),
            new TypeCreator(10, TypeGenerator::generateBoolType),
            new TypeCreator(35, TypeGenerator::generateBytesType),
            new TypeCreator(60, TypeGenerator::generateFixedType),
            new TypeCreator(95, TypeGenerator::generateIntegerType),
            new TypeCreator(MAX_VALUE_TYPE_PROBABILITY, TypeGenerator::generateStringType),
            new TypeCreator(MAX_TYPE_PROBABILITY, TypeGenerator::generateArrayType),
        };

        BASE_KEYWORDS = new HashMap<>();
        BASE_KEYWORDS.put(SolidityAddress.class, "address");
        BASE_KEYWORDS.put(SolidityBool.class, "bool");
        BASE_KEYWORDS.put(SolidityBytes.class, "bytes");
        BASE_KEYWORDS.put(SolidityFixed.class, "fixed");
        BASE_KEYWORDS.put(SolidityInteger.class, "int");
        BASE_KEYWORDS.put(SolidityString.class, "string");

        KEYWORD_CREATORS = new HashMap<>();
        KEYWORD_CREATORS.put(SolidityAddress.class, TypeGenerator::serializeAddress);
        KEYWORD_CREATORS.put(SolidityBool.class, TypeGenerator::serializeBool);
        KEYWORD_CREATORS.put(SolidityBytes.class, TypeGenerator::serializeBytes);
        KEYWORD_CREATORS.put(SolidityFixed.class, TypeGenerator::serializeFixed);
        KEYWORD_CREATORS.put(SolidityInteger.class, TypeGenerator::serializeInteger);
        KEYWORD_CREATORS.put(SolidityString.class, TypeGenerator::serializeString);
    }



    private final Random random;
    public TypeGenerator(Random random) {
        this.random = random;

        
    }    

    public String toBaseKeyword(SolidityType type) {
        assert type != null;
        if (type.getClass().equals(SolidityArray.class)) {
            return String.format("%s[]", this.toBaseKeyword(((SolidityArray)type).getBaseType()));
        }

        assert BASE_KEYWORDS.containsKey(type.getClass());
        return BASE_KEYWORDS.get(type.getClass());
    }

    public String toKeyword(SolidityType type) {
        assert type != null && KEYWORD_CREATORS.containsKey(type.getClass());
        return KEYWORD_CREATORS.get(type.getClass()).apply(this.random, type);
    }

    private static String serializeAddress(Random random, SolidityType type) {
        return BASE_KEYWORDS.get(SolidityAddress.class);
    }

    private static String serializeBool(Random random, SolidityType type) {
        return BASE_KEYWORDS.get(SolidityBool.class);
    }

    private static String serializeBytes(Random random, SolidityType type) {
        final SolidityBytes bytes = (SolidityBytes)type;
        final String keyword = BASE_KEYWORDS.get(SolidityBytes.class);
        
        if (bytes.isDynamic()) {
            return keyword;
        }
        
        if (bytes.getLength() == 1 && random.nextBoolean()) {
            return "byte";
        }

        return String.format("%s%s", keyword, bytes.getLength());
    }

    private static String serializeFixed(Random random, SolidityType type) {
        final SolidityFixed fixed = (SolidityFixed)type;
        final String keyword = BASE_KEYWORDS.get(SolidityFixed.class);
        final String signed = fixed.isSigned() ? "" : "u";

        if (fixed.getM() == 128 && fixed.getN() == 18 && random.nextBoolean()) {
            return String.format("%s%s", signed, keyword);
        }

        return String.format("%s%s%sx%s", signed, keyword, fixed.getM(), fixed.getN());
    }

    private static String serializeInteger(Random random, SolidityType type) {
        final SolidityInteger integer = (SolidityInteger)type;
        final String keyword = BASE_KEYWORDS.get(SolidityInteger.class);
        final String signed = integer.isSigned() ? "" : "u";

        if (integer.getLength() == 256 && random.nextBoolean()) {
            return String.format("%s%s", signed, keyword);
        }

        return String.format("%s%s%s", signed, keyword, integer.getLength());
    }

    private static String serializeString(Random random, SolidityType type) {
        return BASE_KEYWORDS.get(SolidityString.class);
    }

    public SolidityType generateTypeOtherThan(SolidityType excludedType) {
        SolidityType type = null;
        do {
            type = this.generateType();
        } while (type.conceptuallyEquals(excludedType));
        return type;
    }

    public SolidityType generateType() {
        return generateType(this.random, MAX_TYPE_PROBABILITY);
    }

    private static SolidityArray generateArrayType(Random random) {
        return new SolidityArray(generateType(random, MAX_VALUE_TYPE_PROBABILITY));
    }    

    private static SolidityType generateType(Random random, int maxGeneratorThreshold) {
        final int number = random.nextInt(maxGeneratorThreshold);
        for (TypeCreator gen : TYPE_CREATORS) {
            if (number < gen.threshold) {
                return gen.generator.apply(random);
            }
        }

        assert false : "A type should have been generated! Probabilities don't seem to match!";
        return null;
    }

    private static SolidityAddress generateAddressType(Random random) {
        return SolidityAddress.DEFAULT_INSTANCE;
    }

    private static SolidityBool generateBoolType(Random random) {
        return SolidityBool.DEFAULT_INSTANCE;
    }

    private static SolidityBytes generateBytesType(Random random) {
        final int bitLength = random.nextInt(34);
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

    private static SolidityFixed generateFixedType(Random random) {
        final boolean signed = random.nextBoolean();
        final int bitLength = generateIntegerLength(random);

        if (bitLength == 0) {
            return new SolidityFixed(signed);
        }

        return new SolidityFixed(signed, bitLength, random.nextInt(81));
    }
    
    private static SolidityInteger generateIntegerType(Random random) {
        final boolean signed = random.nextBoolean();
        final int bitLength = generateIntegerLength(random);
        return bitLength == 0 
            ? new SolidityInteger(signed)
            : new SolidityInteger(signed, bitLength);
    } 

    private static SolidityString generateStringType(Random random) {
        return SolidityString.DEFAULT_INSTANCE;
    }

    private static int generateIntegerLength(Random random) {
        return 8 * random.nextInt(33);
    } 

    private static class TypeCreator {
        final int threshold;
        final Function<Random, SolidityType> generator;
        
        TypeCreator(int threshold, Function<Random, SolidityType> generator) {
            this.threshold = threshold;
            this.generator = generator;
        }
    }
}