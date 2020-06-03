package au.csiro.data61.aap.samples;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SampleUtils
 */
public class SampleUtils {
    public static final String CRYPTO_KITTIES = "CryptoKitties.ethql";
    public static final String NETWORK_STATISTICS = "NetworkStatistics.ethql";
    public static final String AUGUR_REGISTRY = "AugurContractRegistry.ethql";
    public static final String GIT_GENERATOR = "GeneratorGitExample.ethql";
    public static final String SHIRT_GENERATOR = "GeneratorShirtExample.ethql";


    public static List<URL> getExtractorResources() {
        return getResources(CRYPTO_KITTIES, NETWORK_STATISTICS, AUGUR_REGISTRY);
    }

    public static List<URL> getGeneratorResources() {
        return getResources(GIT_GENERATOR, SHIRT_GENERATOR);
    }

    public static List<URL> getAllResources() {
        return getResources(CRYPTO_KITTIES, NETWORK_STATISTICS, AUGUR_REGISTRY, GIT_GENERATOR,
                SHIRT_GENERATOR);
    }

    public static List<URL> getResources(String... resources) {
        return Arrays.stream(resources)
                .map(resource -> SampleUtils.class.getClassLoader().getResource(resource))
                .collect(Collectors.toList());
    }
}
