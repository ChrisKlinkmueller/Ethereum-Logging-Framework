package au.csiro.data61.aap.program;

import au.csiro.data61.aap.library.Library;
import au.csiro.data61.aap.program.suppliers.Literal;
import au.csiro.data61.aap.program.suppliers.MethodCall;
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
        final Literal url = new Literal(SolidityString.DEFAULT_INSTANCE, URL);
        final Method method = Library.INSTANCE.getMethod("connect", SolidityString.DEFAULT_INSTANCE);
        final MethodCall call = new MethodCall(method, url);
        final Statement statement = new Statement(call);
        scope.addInstruction(statement);
        statement.setEnclosingScope(scope);
    }
}