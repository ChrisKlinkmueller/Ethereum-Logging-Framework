package au.csiro.data61.aap.samples;

import java.net.URL;
import java.util.List;

import au.csiro.data61.aap.elf.EthqlProcessingEvent;
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
            List<EthqlProcessingEvent> events = validator.analyzeScript(url.getFile(), false);
            System.out.printf("%s events during validation.%s", events.size(), System.lineSeparator());
            events.forEach(System.out::println);
        } catch (EthqlProcessingException e) {
            e.printStackTrace();
        }

        System.out.println();
    }
}
