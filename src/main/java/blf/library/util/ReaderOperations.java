package blf.library.util;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ReaderOperations {

    private ReaderOperations() {}

    public static List<String> readIn(Object[] parameters, ProgramState state) {
        if (!(parameters != null && parameters.length == 1 && parameters[0] instanceof String)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return List.of();
        }

        final String fileName = (String) parameters[0];
        List<String> output = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }
            return output;
        } catch (IOException e) {
            ExceptionHandler.getInstance().handleException("Error when trying to read in a file.", e);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return List.of();
    }
}
