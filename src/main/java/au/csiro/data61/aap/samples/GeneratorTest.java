package au.csiro.data61.aap.samples;

import java.net.URL;

import au.csiro.data61.aap.elf.Generator;

/**
 * GeneratorTest
 */
public class GeneratorTest {

    public static void main(String[] args) {
        Generator generator = new Generator();
        SampleUtils.getGeneratorResources().forEach(url -> test(generator, url));
    }

    private static void test(Generator generator, URL url) {
        System.out.println(url.getFile());

        try {
            String code = generator.generateLoggingFunctionality(url.getFile());
            System.out.println(code);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        System.out.println("\n");
    }
}
