package au.csiro.data61.aap.samples;

import java.net.URL;
import java.util.List;

import au.csiro.data61.aap.elf.EthqlProcessingError;
import au.csiro.data61.aap.elf.EthqlProcessingException;
import au.csiro.data61.aap.elf.Validator;

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
            List<EthqlProcessingError> errors = validator.analyzeScript(url.getFile());
            System.out.println(errors.size());
            errors.forEach(
                error -> System.out.println(
                    String.format("At line %s, col %s: %s", error.getLine(), error.getColumn(), error.getErrorMessage())
                )
            );
        } catch (EthqlProcessingException e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
