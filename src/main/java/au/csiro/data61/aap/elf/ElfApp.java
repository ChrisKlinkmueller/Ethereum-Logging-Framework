package au.csiro.data61.aap.elf;

import java.io.File;
import java.util.List;

import au.csiro.data61.aap.elf.EthqlProcessingEvent.Type;

public class ElfApp {
    private static final int INDEX_CMD = 0;
    private static final int INDEX_PATH = 1;
    private static final int INDEX_MODE = 2;
    private static final String CMD_GENERATE = "generate";
    private static final String CMD_EXTRACT = "extract";
    private static final String CMD_VALIDATE = "validate";
    private static final String FULL_VALIDATION_MODE = "-full";
    private static final String ERROR_VALIDATION_MODE = "-errors";

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
            boolean errorsOnly = false;
            if (INDEX_MODE + 1 <= args.length) {
                if (args[INDEX_MODE].equals(ERROR_VALIDATION_MODE)) {
                    errorsOnly = true;
                } else if (!args[INDEX_MODE].equals(FULL_VALIDATION_MODE)) {
                    final String message = String.format(
                        "Invalid validation mode. Must be '%s' or '%s', but was %s",
                        ERROR_VALIDATION_MODE,
                        FULL_VALIDATION_MODE,
                        args[INDEX_MODE]
                    );
                    System.out.println(message);
                    return;
                }
            }
            validate(filepath, errorsOnly);
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

    private static void validate(String filepath, boolean errorsOnly) {
        final Validator validator = new Validator();

        try {
            final List<EthqlProcessingEvent> events = validator.analyzeScript(filepath, errorsOnly);
            printValidationResult(events);
        } catch (EthqlProcessingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static void printValidationResult(List<EthqlProcessingEvent> events) {
        if (events.stream().anyMatch(e -> e.getType() == Type.ERROR)) {
            System.out.println("The script is invalid.");
            printEvents(events, Type.ERROR);
        } else {
            System.out.println("The script is valid.");
        }

        printEvents(events, Type.WARNING);
        printEvents(events, Type.INFO);
    }

    private static void printEvents(List<EthqlProcessingEvent> events, Type type) {
        if (!events.stream().anyMatch(e -> e.getType() == type)) {
            return;
        }

        events.stream().filter(e -> e.getType() == type).forEach(e -> System.out.println("- " + e));
    }

}
