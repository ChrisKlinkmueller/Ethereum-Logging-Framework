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
    public static final String BOCKRATH_FORSAGE = "Bockrath_Forsage.ethql";
    public static final String BOCKRATH_RAIDEN = "Bockrath_Raiden.ethql";
    public static final String REBESKY_AUGUR = "Rebesky_Augur.ethql";
    public static final String REBESKY_CHICKEN_HUNT = "Rebesky_ChickenHunt.ethql";
    public static final String REBESKY_IDEX_1 = "Rebesky_Idex1.ethql";
    public static final String REBESKY_IDEX_2 = "Rebesky_Idex2.ethql";
    public static final String REBESKY_IDEX_3 = "Rebesky_Idex3.ethql";

    public static List<URL> getExtractorResources() {
        return getResources(CRYPTO_KITTIES, NETWORK_STATISTICS, AUGUR_REGISTRY);
    }

    public static List<URL> getGeneratorResources() {
        return getResources(GIT_GENERATOR, SHIRT_GENERATOR);
    }

    public static List<URL> getAllResources() {
        return getResources(
            CRYPTO_KITTIES,
            NETWORK_STATISTICS,
            AUGUR_REGISTRY,
            GIT_GENERATOR,
            SHIRT_GENERATOR,
            BOCKRATH_FORSAGE,
            BOCKRATH_RAIDEN,
            REBESKY_AUGUR,
            REBESKY_CHICKEN_HUNT,
            REBESKY_IDEX_1,
            REBESKY_IDEX_2,
            REBESKY_IDEX_3
        );
    }

    public static List<URL> getResources(String... resources) {
        return Arrays.stream(resources)
            .map(resource -> SampleUtils.class.getClassLoader().getResource(resource))
            .collect(Collectors.toList());
    }
}
