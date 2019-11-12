package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ScriptGeneratorUtils
 */
public class GeneratorUtils {
    private static final int MAX_STRING_MUTATIONS = 5;

    public static String generateString(Random random, String alphabet, int length) {
        return IntStream.range(0, length)
            .mapToObj(i -> Character.toString(alphabet.charAt(random.nextInt(alphabet.length()))))
            .collect(Collectors.joining());

    }
    
    public static String mutateString(String string, Random random, String alphabet) {
        String mutation = string;
        int mutations = 1 + random.nextInt(MAX_STRING_MUTATIONS);
        for (int i = 0; i < mutations; i++) {
            mutation = random.nextBoolean() || mutation.isEmpty() 
                ? insertCharacter(mutation, random, alphabet) 
                : deleteCharacter(mutation, random);
        }

        return mutation;
    }

    private static String insertCharacter(String mutation, Random random, String alphabet) {
        int insertion = random.nextInt(mutation.length());
        return String.format("%s%s%s",
            mutation.substring(0, insertion),
            alphabet.charAt(random.nextInt(alphabet.length())),
            mutation.substring(insertion, mutation.length())
        );
    }

    private static String deleteCharacter(String mutation, Random random) {
        final int deletedChar = random.nextInt(mutation.length());
        return IntStream.range(0, mutation.length())
            .filter(i -> i != deletedChar)
            .mapToObj(i -> Character.toString(mutation.charAt(i)))
            .collect(Collectors.joining());
    }
}