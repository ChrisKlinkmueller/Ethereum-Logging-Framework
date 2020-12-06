package au.csiro.data61.aap.elf;

import au.csiro.data61.aap.elf.util.RootListenerException;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElfApp {
    private static final int INDEX_CMD = 0;
    private static final int INDEX_PATH = 1;
    private static final String CMD_GENERATE = "generate";
    private static final String CMD_EXTRACT = "extract";
    private static final String CMD_VALIDATE = "validate";
    private static final Logger LOGGER = Logger.getLogger(ElfApp.class.getName());

    public static void main(String[] args) {
        if (args.length < 2) {
            final String message = String.format(
                "Execution of ELF requires two arguments: [%s|%s|%s] <PATH_TO_SCRIPT>",
                CMD_GENERATE,
                CMD_EXTRACT,
                CMD_VALIDATE
            );
            LOGGER.log(Level.SEVERE, message);
            return;
        }

        final String filepath = args[INDEX_PATH];
        final File file = new File(filepath);
        if (!file.exists()) {
            final String message = String.format("Invalid file path: %s", filepath);
            LOGGER.log(Level.SEVERE, message);
            return;
        }

        final String command = args[INDEX_CMD].toLowerCase();
        switch (command) {
            case CMD_GENERATE:
                generate(filepath);
                break;
            case CMD_EXTRACT:
                extract(filepath);
                break;
            case CMD_VALIDATE:
                validate(filepath);
                break;
            default:
                final String message = String.format(
                    "Unsupported command. Must be %s, %s, or %s. But was: %s",
                    CMD_GENERATE,
                    CMD_EXTRACT,
                    CMD_VALIDATE,
                    command
                );
                LOGGER.log(Level.SEVERE, message);
                break;
        }
    }

    private static void generate(String filepath) {
        final Generator generator = new Generator();
        try {
            final String generatedCode = generator.generateLoggingFunctionality(filepath);
            LOGGER.log(Level.INFO, generatedCode);
        } catch (BcqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void extract(String filepath) {
        final Extractor extractor = new Extractor();

        try {
            extractor.extractData(filepath);
        } catch (BcqlProcessingException ex) {
            ex.printStackTrace(System.err);
        } catch (RootListenerException e) {
            e.printStackTrace();
        }
    }

    private static void validate(String filepath) {
        final Validator validator = new Validator();

        try {
            final List<EthqlProcessingError> errors = validator.analyzeScript(filepath);
            printValidationResult(errors);
        } catch (BcqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void printValidationResult(List<EthqlProcessingError> errors) {
        if (errors.isEmpty()) {
            LOGGER.log(Level.INFO, "The validation did not find errors.");
            return;
        }

        LOGGER.log(Level.WARNING, "The validation detected the following errors:");
        errors.forEach(System.out::println);
    }

}
