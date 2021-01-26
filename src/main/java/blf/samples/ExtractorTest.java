package blf.samples;

import java.net.URL;

import blf.Extractor;

/**
 * ExtractorTest
 */
public class ExtractorTest {

    public static void main(String[] args) {
        Extractor extractor = new Extractor();
        SampleUtils.getExtractorResources().forEach(url -> test(extractor, url));
    }

    private static void test(Extractor extractor, URL url) {
        System.out.println(url.getFile());

        try {
            extractor.extractData(url.getFile(), true);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        System.out.println("\n");
    }

}
