// package au.csiro.data61.aap.generation;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Random;
// import java.util.function.Function;

// import au.csiro.data61.aap.program.types.SolidityAddress;
// import au.csiro.data61.aap.program.types.SolidityArray;
// import au.csiro.data61.aap.program.types.SolidityBool;
// import au.csiro.data61.aap.program.types.SolidityBytes;
// import au.csiro.data61.aap.program.types.SolidityFixed;
// import au.csiro.data61.aap.program.types.SolidityInteger;
// import au.csiro.data61.aap.program.types.SolidityString;
// import au.csiro.data61.aap.program.types.SolidityType;

// /**
//  * TypeGenerator
//  */
// class TypeGenerator {
//     private static final int MAX_TYPE_PROBABILITY = 125;
//     private static final int MAX_VALUE_TYPE_PROBABILITY = 100;

//     private final TypeCreator[] typeCreators;
//     private final Map<Class<? extends SolidityType>, String> baseKeywords;
//     private final Map<Class<? extends SolidityType>, Function<SolidityType, String>> keywordCreators;
//     private final Random random;
    
//     public TypeGenerator(Random random) {
//         this.random = random;

//         this.typeCreators = new TypeCreator[]{
//             new TypeCreator(5, this::generateAddressType),
//             new TypeCreator(10, this::generateBoolType),
//             new TypeCreator(35, this::generateBytesType),
//             new TypeCreator(60, this::generateFixedType),
//             new TypeCreator(95, this::generateIntegerType),
//             new TypeCreator(MAX_VALUE_TYPE_PROBABILITY, this::generateStringType),
//             new TypeCreator(MAX_TYPE_PROBABILITY, this::generateArrayType),
//         };

//         this.baseKeywords = new HashMap<>();
//         this.baseKeywords.put(SolidityAddress.class, "address");
//         this.baseKeywords.put(SolidityBool.class, "bool");
//         this.baseKeywords.put(SolidityBytes.class, "bytes");
//         this.baseKeywords.put(SolidityFixed.class, "fixed");
//         this.baseKeywords.put(SolidityInteger.class, "int");
//         this.baseKeywords.put(SolidityString.class, "string");

//         this.keywordCreators = new HashMap<>();
//         this.keywordCreators.put(SolidityAddress.class, this::serializeAddress);
//         this.keywordCreators.put(SolidityBool.class, this::serializeBool);
//         this.keywordCreators.put(SolidityBytes.class, this::serializeBytes);
//         this.keywordCreators.put(SolidityFixed.class, this::serializeFixed);
//         this.keywordCreators.put(SolidityInteger.class, this::serializeInteger);
//         this.keywordCreators.put(SolidityString.class, this::serializeString);
//     }    

//     public String toBaseKeyword(SolidityType type) {
//         assert type != null;
//         if (type.getClass().equals(SolidityArray.class)) {
//             return String.format("%s[]", this.toBaseKeyword(((SolidityArray)type).getBaseType()));
//         }

//         assert this.baseKeywords.containsKey(type.getClass());
//         return this.baseKeywords.get(type.getClass());
//     }

//     public String toKeyword(SolidityType type) {
//         assert type != null && this.keywordCreators.containsKey(type.getClass());
//         return this.keywordCreators.get(type.getClass()).apply(type);
//     }

//     private String serializeAddress(SolidityType type) {
//         return this.baseKeywords.get(SolidityAddress.class);
//     }

//     private String serializeBool(SolidityType type) {
//         return this.baseKeywords.get(SolidityBool.class);
//     }

//     private String serializeBytes(SolidityType type) {
//         final SolidityBytes bytes = (SolidityBytes)type;
//         final String keyword = this.baseKeywords.get(SolidityBytes.class);
        
//         if (bytes.isDynamic()) {
//             return keyword;
//         }
        
//         if (bytes.getLength() == 1 && random.nextBoolean()) {
//             return "byte";
//         }

//         return String.format("%s%s", keyword, bytes.getLength());
//     }

//     private String serializeFixed(SolidityType type) {
//         final SolidityFixed fixed = (SolidityFixed)type;
//         final String keyword = this.baseKeywords.get(SolidityFixed.class);
//         final String signed = fixed.isSigned() ? "" : "u";

//         if (fixed.getM() == 128 && fixed.getN() == 18 && random.nextBoolean()) {
//             return String.format("%s%s", signed, keyword);
//         }

//         return String.format("%s%s%sx%s", signed, keyword, fixed.getM(), fixed.getN());
//     }

//     private String serializeInteger(SolidityType type) {
//         final SolidityInteger integer = (SolidityInteger)type;
//         final String keyword = this.baseKeywords.get(SolidityInteger.class);
//         final String signed = integer.isSigned() ? "" : "u";

//         if (integer.getLength() == 256 && random.nextBoolean()) {
//             return String.format("%s%s", signed, keyword);
//         }

//         return String.format("%s%s%s", signed, keyword, integer.getLength());
//     }

//     private String serializeString(SolidityType type) {
//         return this.baseKeywords.get(SolidityString.class);
//     }

//     public SolidityType generateTypeOtherThan(SolidityType excludedType) {
//         SolidityType type = null;
//         do {
//             type = this.generateType();
//         } while (type.conceptuallyEquals(excludedType));
//         return type;
//     }

//     public SolidityType generateType() {
//         return generateType(this.random, MAX_TYPE_PROBABILITY);
//     }

//     private SolidityArray generateArrayType(Random random) {
//         return new SolidityArray(generateType(random, MAX_VALUE_TYPE_PROBABILITY));
//     }    

//     private SolidityType generateType(Random random, int maxGeneratorThreshold) {
//         final int number = random.nextInt(maxGeneratorThreshold);
//         for (TypeCreator gen : this.typeCreators) {
//             if (number < gen.threshold) {
//                 return gen.generator.apply(random);
//             }
//         }

//         assert false : "A type should have been generated! Probabilities don't seem to match!";
//         return null;
//     }

//     private SolidityAddress generateAddressType(Random random) {
//         return SolidityAddress.DEFAULT_INSTANCE;
//     }

//     private SolidityBool generateBoolType(Random random) {
//         return SolidityBool.DEFAULT_INSTANCE;
//     }

//     private SolidityBytes generateBytesType(Random random) {
//         final int bitLength = random.nextInt(34);
//         if (bitLength == 0) {
//             return new SolidityBytes(1);
//         }
//         else if (bitLength == 33) {
//             return new SolidityBytes();
//         }
//         else {
//             return new SolidityBytes(bitLength);
//         }
//     }

//     private SolidityFixed generateFixedType(Random random) {
//         final boolean signed = random.nextBoolean();
//         final int bitLength = generateIntegerLength(random);

//         if (bitLength == 0) {
//             return new SolidityFixed(signed);
//         }

//         return new SolidityFixed(signed, bitLength, random.nextInt(81));
//     }
    
//     private SolidityInteger generateIntegerType(Random random) {
//         final boolean signed = random.nextBoolean();
//         final int bitLength = generateIntegerLength(random);
//         return bitLength == 0 
//             ? new SolidityInteger(signed)
//             : new SolidityInteger(signed, bitLength);
//     } 

//     private SolidityString generateStringType(Random random) {
//         return SolidityString.DEFAULT_INSTANCE;
//     }

//     private int generateIntegerLength(Random random) {
//         return 8 * random.nextInt(33);
//     } 

//     private static class TypeCreator {
//         final int threshold;
//         final Function<Random, SolidityType> generator;
        
//         TypeCreator(int threshold, Function<Random, SolidityType> generator) {
//             this.threshold = threshold;
//             this.generator = generator;
//         }
//     }
// }