package au.csiro.data61.aap.generation;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * ScriptGeneratorUtils
 */
public class GeneratorUtils {
    private static final int MAX_STRING_MUTATIONS = 5;

    private static final Random RANDOM = new Random(22061953);
    public static LiteralGenerator LITERAL_GENERATOR = new LiteralGenerator(RANDOM);
    public static ScopeGenerator SCOPE_GENERATOR = new ScopeGenerator(RANDOM);
    public static StatementGenerator STATEMENT_GENERATOR = new StatementGenerator(RANDOM);
    public static TypeGenerator TYPE_GENERATOR = new TypeGenerator(RANDOM);
    public static VariableGenerator VARIABLE_GENERATOR = new VariableGenerator(RANDOM);
    public static MethodCallGenerator METHOD_CALL_GENERATOR = new MethodCallGenerator(RANDOM);

    public static String generateString(String alphabet, int length) {
        return IntStream.range(0, length)
            .mapToObj(i -> Character.toString(alphabet.charAt(RANDOM.nextInt(alphabet.length()))))
            .collect(Collectors.joining());
    }
    
    public static String mutateString(String string, String alphabet) {
        String mutation = string;
        int mutations = 1 + RANDOM.nextInt(MAX_STRING_MUTATIONS);
        for (int i = 0; i < mutations; i++) {
            mutation = RANDOM.nextBoolean() || mutation.isEmpty() 
                ? insertCharacter(mutation, alphabet) 
                : deleteCharacter(mutation);
        }

        return mutation;
    }

    private static String insertCharacter(String mutation, String alphabet) {
        int insertion = RANDOM.nextInt(mutation.length());
        return String.format("%s%s%s",
            mutation.substring(0, insertion),
            alphabet.charAt(RANDOM.nextInt(alphabet.length())),
            mutation.substring(insertion, mutation.length())
        );
    }

    private static String deleteCharacter(String mutation) {
        final int deletedChar = RANDOM.nextInt(mutation.length());
        return IntStream.range(0, mutation.length())
            .filter(i -> i != deletedChar)
            .mapToObj(i -> Character.toString(mutation.charAt(i)))
            .collect(Collectors.joining());
    }

    public static <T> T randomElement(Stream<T> stream) {
        final List<T> list = stream.collect(Collectors.toList());
        return randomListElement(list);
    }

    public static <T> T randomListElement(List<T> list) {
        assert list != null;
        return list.isEmpty() ? null : list.get(RANDOM.nextInt(list.size()));
    }
}