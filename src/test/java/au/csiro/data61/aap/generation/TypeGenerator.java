package au.csiro.data61.aap.generation;

import static au.csiro.data61.aap.generation.ScriptGeneratorUtils.RANDOM;

import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;

/**
 * TypeGenerator
 */
public class TypeGenerator {    

    public static SolidityAddress generateAddressType() {
        return SolidityAddress.DEFAULT_INSTANCE;
    }

    public static SolidityBool generateBoolType() {
        return SolidityBool.DEFAULT_INSTANCE;
    }

    public static SolidityBytes generateBytesType() {
        final int bitLength = RANDOM.nextInt(33);
        if (bitLength == 0) {
            return new SolidityBytes(1);
        }
        else {
            return new SolidityBytes(bitLength);
        }
    }

    public static SolidityFixed generateFixedType() {
        final boolean signed = RANDOM.nextBoolean();
        final int bitLength = generateIntegerLength();

        if (bitLength == 0) {
            return new SolidityFixed(signed);
        }

        return new SolidityFixed(signed, bitLength, RANDOM.nextInt(81));
    }
    
    public static SolidityInteger generateIntegerType() {
        final boolean signed = RANDOM.nextBoolean();
        final int bitLength = generateIntegerLength();
        return bitLength == 0 
            ? new SolidityInteger(signed)
            : new SolidityInteger(signed, bitLength);
    } 

    private static int generateIntegerLength() {
        return 8 * RANDOM.nextInt(33);
    } 
}