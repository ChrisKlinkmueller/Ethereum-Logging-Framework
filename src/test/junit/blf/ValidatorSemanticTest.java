package blf;

import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ValidatorSemanticTest {
    static final private Validator validator = new Validator();

    static List<BcqlProcessingError> validate(String script)  {
        try {
            return validator.analyzeScript(new ByteArrayInputStream(script.getBytes()));
        } catch (BcqlProcessingException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @ParameterizedTest(name = "script: {0} expected error msg: {1}")
    @MethodSource("transactionProvider")
    void transactionFilter(String script, String expectedErrorMsg) {
        List<BcqlProcessingError> errors = validate(script);

        errors.forEach(err -> Assertions.assertEquals(err.getErrorMessage(), expectedErrorMsg));
    }

    static Stream<Arguments> transactionProvider() {
        return readTestData("./ValidatorSemanticTestData.txt");
    }

    static Stream<Arguments> readTestData(String filename) {
        try {
            URL url = ValidatorSemanticTest.class.getResource(filename);
            Path path = Paths.get(url.toURI());
            Stream<String> streamlines = Files.lines(path);

            List<String> lines = streamlines.collect(Collectors.toList());
            List<Arguments> output = new LinkedList<>();

            String script = "";
            String expectedErr = "";
            boolean operateOnScript = true;
            for (String line : lines) {
                if (line.equals("|")) {
                    operateOnScript = false;
                    continue;
                }
                if (line.equals("||")) {
                    output.add(Arguments.of(script, expectedErr));
                    script = "";
                    expectedErr = "";
                    operateOnScript = true;
                    continue;
                }
                if (operateOnScript) {
                    script += line + "\n";
                } else {
                    expectedErr += line;
                }
            }

            return output.stream();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

//    @ParameterizedTest
//    @ValueSource
//    void genericFilter() {
//
//    }

//    @ParameterizedTest
//    @ValueSource
//    void logEntriesFilter() {
//
//    }
}
