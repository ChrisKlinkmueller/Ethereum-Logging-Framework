package blf.generation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import blf.util.RootListenerException;
import blf.util.RootListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * SolidityCodeGenerator
 */
public class SolidityCodeGeneration {
    public static final String CODE_GAP = IntStream.range(0, 3).mapToObj(i -> System.lineSeparator()).collect(Collectors.joining());

    private final RootListener listener;
    private final CodeCollector codeCollector;

    public SolidityCodeGeneration() {
        this.listener = new RootListener();
        this.codeCollector = new CodeCollector();
        this.init(Arrays.asList(new ItemGenerator(this.codeCollector)));
    }

    public SolidityCodeGeneration(List<Function<CodeCollector, BaseGenerator>> generatorConstructors) {
        assert generatorConstructors.stream().allMatch(Objects::nonNull);
        this.codeCollector = new CodeCollector();
        this.listener = new RootListener();
        this.init(generatorConstructors.stream().map(c -> c.apply(this.codeCollector)).collect(Collectors.toList()));
    }

    private void init(List<BaseGenerator> generators) {
        generators.forEach(gen -> {
            try {
                this.listener.addListener(gen);
            } catch (RootListenerException e) {
                e.printStackTrace();
            }
        });
    }

    public String generateLoggingFunctionality(ParseTree parseTree) {
        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this.listener, parseTree);

        final String code = this.codeCollector.getCode();
        this.codeCollector.clean();

        return code;
    }

}
