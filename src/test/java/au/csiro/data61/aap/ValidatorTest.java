package au.csiro.data61.aap;

import java.net.URL;
import java.util.List;

import au.csiro.data61.aap.elf.EthqlProcessingError;
import au.csiro.data61.aap.elf.EthqlProcessingException;
import au.csiro.data61.aap.elf.Validator;

/**
 * ValidatorTest
 */
public class ValidatorTest {
    private static final String CRYPTO_KITTIES = "CryptoKitties.ethql";
    private static final String NETWORK_STATISTICS = "NetworkStatistics.ethql";

    public static void main(String[] args) {
        Validator validator = new Validator();
        test(validator, CRYPTO_KITTIES);
        test(validator, NETWORK_STATISTICS);
    }

    private static void test(Validator validator, String resource) {
        System.out.println("Test case: " + resource);
        final URL url = ValidatorTest.class.getClassLoader().getResource(resource);
        

        try {
            List<EthqlProcessingError> errors = validator.analyzeScript(url.getFile());
            System.out.println(errors.size());
            errors.forEach(error -> System.out.println(
                String.format(
                    "At line %s, col %s: %s",
                    error.getLine(),
                    error.getColumn(),
                    error.getErrorMessage())
                )
            );
        } catch (EthqlProcessingException e) {           
            e.printStackTrace();
        }

        System.out.println();
    }
}