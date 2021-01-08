package blf.samples;

import java.net.URL;
import java.util.List;

import blf.BcqlProcessingError;
import blf.BcqlProcessingException;
import blf.Validator;

/**
 * ValidatorTest
 */
public class ValidatorTest {

    public static void main(String[] args) {
        Validator validator = new Validator();
        SampleUtils.getAllResources().forEach(url -> test(validator, url));
    }

    private static void test(Validator validator, URL url) {
        System.out.println(url.getFile());
        try {
            List<BcqlProcessingError> errors = validator.analyzeScript(url.getFile());
            System.out.println(errors.size());
            errors.forEach(
                error -> System.out.println(
                    String.format("At line %s, col %s: %s", error.getLine(), error.getColumn(), error.getErrorMessage())
                )
            );
        } catch (BcqlProcessingException e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
