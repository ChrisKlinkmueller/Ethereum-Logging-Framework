package blf.configuration;

import java.util.HashMap;
import java.util.Map;

public class EmissionSettings {

    private static final Map<String, EmissionMode> emissionModeMap = new HashMap<>();

    public static Map<String, EmissionMode> getEmissionModeMap() {
        emissionModeMap.put("default batching", EmissionMode.DEFAULT_BATCHING);
        emissionModeMap.put("safe batching", EmissionMode.SAFE_BATCHING);
        emissionModeMap.put("streaming", EmissionMode.STREAMING);

        return emissionModeMap;
    }

    public enum EmissionMode {
        DEFAULT_BATCHING,
        SAFE_BATCHING,
        STREAMING
    }

}
