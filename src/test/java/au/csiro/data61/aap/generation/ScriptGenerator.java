package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import au.csiro.data61.aap.spec.types.SolidityInteger;

/**
 * ScriptGenerator
 */
public class ScriptGenerator {
    private static final String STRING_LITERAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz .:0123456789?*!@#$%^&()";
    private final Random random;

    public ScriptGenerator() {
        this.random = new Random();
    }

     

    private String toString(SolidityInteger type, boolean introduceError) {
        final String signedPrefix = type.isUnsigned() ? "u" : "";
        
        final boolean keywordError = introduceError && this.random.nextInt(100) < 30;
        final String keyword = keywordError ? modifyString("int", s -> !s.equals("int")) : "int";
        
        final boolean bitLengthError = introduceError && !keywordError;
        final int bitLength = bitLengthError ? this.modifyNumber(type.getLength(), i -> !SolidityInteger.isValidLength(i)) : type.getLength();

        return String.format("%s%s%s", signedPrefix, keyword, bitLength);
    }

    private int modifyNumber(int number, Predicate<Integer> isAcceptable) {
        int errorNumber = number;
        do {
            int m = this.random.nextInt(10);
            int n = (this.random.nextBoolean() ? 1: -1) * this.random.nextInt(100);
            errorNumber = m * errorNumber + n;
        } while(!isAcceptable.test(errorNumber));
        return errorNumber;
    }

    private String modifyString(String input, Predicate<String> isAcceptable) {
        String errorString = "";
        do {
            final int subStart = this.random.nextInt(input.length());
            final int subEnd = subStart + 1 + this.random.nextInt(input.length() - subStart);
            final String subString = input.substring(subStart, subEnd);
            final String prefix = generateString(STRING_LITERAL_ALPHABET, this.random.nextInt(3));
            final String suffix = generateString(STRING_LITERAL_ALPHABET, this.random.nextInt(3));
            errorString = String.format("%s%s%s", prefix, subString, suffix);
        } while (!isAcceptable.test(errorString));

        return errorString;
    }

    private String generateString(String alphabet, int length) {
        return IntStream.range(0, length)
            .mapToObj(i -> Character.toString(alphabet.charAt(this.random.nextInt(alphabet.length()))))
            .collect(Collectors.joining());
    }
}