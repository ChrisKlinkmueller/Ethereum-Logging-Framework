package au.csiro.data61.aap.elf;

import java.io.File;
import java.util.List;

public class ElfApp {
    private static final int INDEX_CMD = 0;
    private static final int INDEX_PATH = 1;
    private static final String CMD_GENERATE = "generate";
    private static final String CMD_EXTRACT = "extract";
    private static final String CMD_VALIDATE = "validate";

    public static void main(String[] args) {
        if (args.length < 2) {
            final String message = String.format(
                "Execution of ELF requires two arguments: [%s|%s|%s] <PATH_TO_SCRIPT>",
                CMD_GENERATE,
                CMD_EXTRACT,
                CMD_VALIDATE
            );
            System.out.println(message);
            return;
        }

        final String filepath = args[INDEX_PATH];
        final File file = new File(filepath);
        if (!file.exists()) {
            final String message = String.format("Invalid file path: %s", filepath);
            System.out.println(message);
            return;
        }

        final String command = args[INDEX_CMD].toLowerCase();
        if (command.equals(CMD_GENERATE)) {
            generate(filepath);
        } else if (command.equals(CMD_EXTRACT)) {
            extract(filepath);
        } else if (command.equals(CMD_VALIDATE)) {
            validate(filepath);
        } else {
            final String message = String.format(
                "Unsupported command. Must be %s, %s, or %s. But was: %s",
                CMD_GENERATE,
                CMD_EXTRACT,
                CMD_VALIDATE,
                command
            );
            System.out.println(message);
        }
    }

    private static void generate(String filepath) {
        final Generator generator = new Generator();
        try {
            final String generatedCode = generator.generateLoggingFunctionality(filepath);
            System.out.println(generatedCode);
        } catch (EthqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void extract(String filepath) {
        final Extractor extractor = new Extractor();

        try {
            extractor.extractData(filepath);
        } catch (EthqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void validate(String filepath) {
        final Validator validator = new Validator();

        try {
            final List<EthqlProcessingError> errors = validator.analyzeScript(filepath);
            printValidationResult(errors);
        } catch (EthqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void printValidationResult(List<EthqlProcessingError> errors) {
        if (errors.isEmpty()) {
            System.out.println("The validation didn't find errors.");
            return;
        }

        System.out.println("The validation detected the following errors:");
        errors.forEach(System.out::println);
    }

}
