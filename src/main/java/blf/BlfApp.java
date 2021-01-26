package blf;

import blf.util.RootListenerException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlfApp {
    private static final int INDEX_CMD = 0;
    private static final int INDEX_PATH = 1;
    private static final String CMD_GENERATE = "generate";
    private static final String CMD_EXTRACT = "extract";
    private static final String CMD_VALIDATE = "validate";
    private static final String ON_EXCEPTION_ABORT = "-abortOnException";
    private static final Logger LOGGER = Logger.getLogger(BlfApp.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.SEVERE, String.join(", ", args));
        boolean onExceptionAbort = Arrays.asList(args).contains(ON_EXCEPTION_ABORT);
        if (onExceptionAbort) {
            List<String> listArgs = new LinkedList<String>(Arrays.asList(args));
            listArgs.remove(ON_EXCEPTION_ABORT);
            args = listArgs.toArray(new String[0]);
        }
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
                extract(filepath, onExceptionAbort);
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

    private static void extract(String filepath, boolean onExceptionAbort) {
        final Extractor extractor = new Extractor();

        try {
            extractor.extractData(filepath, onExceptionAbort);
        } catch (BcqlProcessingException ex) {
            ex.printStackTrace(System.err);
        } catch (RootListenerException e) {
            e.printStackTrace();
        }
    }

    private static void validate(String filepath) {
        final Validator validator = new Validator();

        try {
            final List<BcqlProcessingError> errors = validator.analyzeScript(filepath);
            printValidationResult(errors);
        } catch (BcqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void printValidationResult(List<BcqlProcessingError> errors) {
        if (errors.isEmpty()) {
            LOGGER.log(Level.INFO, "The validation did not find errors.");
            return;
        }

        LOGGER.log(Level.WARNING, "The validation detected the following errors:");
        errors.forEach(System.out::println);
    }

}
