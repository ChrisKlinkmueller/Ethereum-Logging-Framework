package blf;

import blf.ValidatorSemanticTest;
import org.junit.jupiter.params.provider.Arguments;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class YamlTestLoader {

    /**
     * Loads test data for a given ClassLoader and a given filename from a specfifically structured yaml file.
     * <p>
     * This is used as a workaround because data-driven jUnit 5 tests do not accept tuples a {@link org.junit.jupiter.params.provider.ValueSource}.
     *
     * @param filename The name of the file that contains the test data.
     * @param classLoader The ClassLoader for the class that wants to read the test data.
     * @return A stream of Arguments for jUnit to be used as a {@link org.junit.jupiter.params.provider.MethodSource}.
     */
    static Stream<Arguments> readTestData(String filename, ClassLoader classLoader) {
        InputStream inputStream = classLoader.getResourceAsStream(filename);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(inputStream);

        List<Arguments> output = new LinkedList<>();

        final String scriptPrefix = (String) yamlData.get("scriptPrefix");

        final ArrayList<Map<String, Object>> testData = (ArrayList<Map<String, Object>>) yamlData.get("testData");
        for (Map<String, Object> testDataPair : testData) {
            final String script = scriptPrefix + (String) testDataPair.get("script");
            final String expectedErr = (String) testDataPair.get("expectedErr");
            output.add(Arguments.of(script, expectedErr));
        }

        return output.stream();
    }

}
