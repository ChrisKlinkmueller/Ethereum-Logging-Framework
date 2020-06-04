package au.csiro.data61.aap.elf.library.types;

import java.util.ArrayList;
import java.util.List;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import io.reactivex.functions.BiFunction;

/**
 * ListOperations
 */
public class ListOperations {

    public static Object addElement(Object[] parameters, ProgramState state) throws ProgramException {
        return operate(parameters, (list, value) -> {
            list.add(value);
            return null;
        });
    }

    public static Object removeElement(Object[] parameters, ProgramState state) throws ProgramException {
        return operate(parameters, (list, value) -> {
            list.remove(value);
            return null;
        });
    }

    public static Object clear(Object[] parameters, ProgramState state) throws ProgramException {
        return operate(parameters, (list, value) -> {
            list.clear();
            return null;
        });
    }

    public static Boolean contains(Object[] parameters, ProgramState state) throws ProgramException {
        return operate(parameters, (list, value) -> (Boolean) list.contains(value));
    }

    @SuppressWarnings("unchecked")
    private static <T> T operate(Object[] parameters, BiFunction<List<Object>, Object, T> operation) throws ProgramException {
        if (!areValidParameters(parameters)) {
            throw new ProgramException("Invalid parameters for method call.");
        }

        final List<Object> operand1 = (List<Object>) parameters[0];
        final Object operand2 = parameters[1];

        try {
            return operation.apply(operand1, operand2);
        } catch (Throwable cause) {
            throw new ProgramException("Error executing method call.", cause);
        }
    }

    private static boolean areValidParameters(Object[] parameters) {
        return parameters != null
            && parameters.length == 2
            && parameters[0] != null
            && parameters[1] != null
            && List.class.isAssignableFrom(parameters[0].getClass());

    }

    public static void main(String[] args) {
        System.out.println(List.class.isAssignableFrom(ArrayList.class));
    }

}
