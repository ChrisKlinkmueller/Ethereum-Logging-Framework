package blf.library.types;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;
import io.reactivex.functions.BiFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * ListOperations
 */
public class ListOperations {

    private ListOperations() {}

    public static Object newAddressArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newBoolArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newByteArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newIntArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newStringArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object addElement(Object[] parameters, ProgramState state) {
        return operate(state, parameters, (list, value) -> {
            list.add(value);
            return list;
        });
    }

    public static Object removeElement(Object[] parameters, ProgramState state) {
        return operate(state, parameters, (list, value) -> {
            list.remove(value);
            return list;
        });
    }

    public static Object clear(Object[] parameters, ProgramState state) {
        return operate(state, parameters, (list, value) -> {
            list.clear();
            return list;
        });
    }

    public static Boolean contains(Object[] parameters, ProgramState state) {
        return operate(state, parameters, List::contains);
    }

    @SuppressWarnings("unchecked")
    private static <T> T operate(ProgramState state, Object[] parameters, BiFunction<List<Object>, Object, T> operation) {
        if (!areValidParameters(parameters)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return null;
        }

        final List<Object> operand1 = (List<Object>) parameters[0];
        final Object operand2 = parameters[1];

        try {
            return operation.apply(operand1, operand2);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return null;
    }

    private static boolean areValidParameters(Object[] parameters) {
        return parameters != null
            && parameters.length == 2
            && parameters[0] != null
            && parameters[1] != null
            && List.class.isAssignableFrom(parameters[0].getClass());

    }

}
