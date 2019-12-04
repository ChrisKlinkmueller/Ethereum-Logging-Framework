package au.csiro.data61.aap.elf.generation;

/**
 * BitMappingGenerator
 */
public class BitMappingGenerator {

    public static int encode(int[] values, int[] powers) {
        int code = values[0];
        for (int i = 1; i < values.length; i++) {
            code = code << powers[i];
            code = code ^ values[i];
        }
        return code;
    }
}