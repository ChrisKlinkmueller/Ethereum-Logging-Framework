package au.csiro.data61.aap;

import java.net.URISyntaxException;

/**
 * SpecificationParserTestApp
 */
public class SpecificationParserTestApp {
    //private static Logger LOG = Logger.getLogger(SpecificationParserTestApp.class.getName());
    public static void main(String[] args) throws URISyntaxException {
        /*final String resourceName = "test.xbel";
        final Path path = getPathToResource(resourceName);
        if (path == null) {
            final String message = String.format("Ayoh lah. Path for resource '%s' not valid.", resourceName);
            System.err.println(message);
        }

        final SpecificationParser parser = new SpecificationParser();
        final SpecificationParserResult result = parser.parse(path);

        if (result != null && !result.isSuccessful()) {
            result.errorStream().forEach(error -> System.out.println(error));
        }
        else {
            System.out.println("So far, so good.");
        }
    }

    private static Path getPathToResource(String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = classLoader.getResource("test.xbel");
        try {
            return Paths.get(resourceUrl.toURI());
        }
        catch (URISyntaxException ex) {
            final String msg = String.format("Error accessing resource '%s'", name);
            LOG.log(Level.SEVERE, msg, ex);
        }
        return null;*/
    }
}