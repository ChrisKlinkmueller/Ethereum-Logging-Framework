package au.csiro.data61.aap.program;

import au.csiro.data61.aap.library.Library;
import au.csiro.data61.aap.program.types.SolidityString;

/**
 * BasicProgramTest
 */
public class BasicProgramTest {
    private static final String URL = "ws://localhost:8546/";

    public static void main(String[] args) {
        final GlobalScope scope = createProgram();

        try (ProgramState state = new ProgramState()) {
            scope.execute(state);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static GlobalScope createProgram() {
        final GlobalScope scope = new GlobalScope();
        addConnectionStatement(scope);
        return scope;
    }

    private static void addConnectionStatement(GlobalScope scope) {
        final Variable url = new Variable(SolidityString.DEFAULT_INSTANCE, "literal1", VariableCategory.LITERAL, URL);
        final Method method = Library.INSTANCE.getMethod("connect", SolidityString.DEFAULT_INSTANCE);
        final MethodCall call = new MethodCall(method, url);
        final Statement statement = new Statement(call);
        scope.addInstruction(statement);
        statement.setEnclosingScope(scope);
    }
}