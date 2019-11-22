package au.csiro.data61.aap.etl.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.library.types.IntegerOperations;

/**
 * Library
 */
public class Library {
    public static Library INSTANCE = new Library();

    private final Map<String, List<LibraryEntry>> registeredMethods;

    private Library() {
        this.registeredMethods = new HashMap<>();

        try {
            this.addMethod(new MethodSignature("connect", "string"), ProgramState::connectClient);
            this.addMethod(new MethodSignature("setOutputFolder", "string"), ProgramState::setOutputFolder);
            this.addMethod(new MethodSignature("add", "int", "int"), IntegerOperations::add);
            this.addMethod(new MethodSignature("multiply", "int", "int"), IntegerOperations::multiply);
            this.addMethod(new MethodSignature("subtract", "int", "int"), IntegerOperations::subtract);
            this.addMethod(new MethodSignature("divide", "int", "int"), IntegerOperations::divide);
        } catch (LibraryException e) {
            e.printStackTrace();
        }
    }

    public void addMethod(MethodSignature signature, Method method) throws LibraryException {
        assert signature != null;
        assert method != null;
        this.registeredMethods.putIfAbsent(signature.getMethodName(), new LinkedList<>());
        final List<LibraryEntry> entries = this.registeredMethods.get(signature.getMethodName());
        if (this.containsEntry(entries, signature)) {
            throw new LibraryException(String.format("Entry with signature '%s' already exists.", signature.getSignature()));
        }
        entries.add(new LibraryEntry(method, signature));
    }

    public Method getMethod(MethodSignature signature) {
        return this.registeredMethods.getOrDefault(signature.getMethodName(), Collections.emptyList())
            .stream()
            .filter(entry -> entry.isCompatibleWith(signature))
            .map(LibraryEntry::getMethod)
            .findFirst().orElse(null);
    }

    private boolean containsEntry(List<LibraryEntry> entries, MethodSignature signature) {
        return entries.stream().anyMatch(re -> re.isCompatibleWith(signature));
    }
    
    private static class LibraryEntry {
        private final Method method;
        private final MethodSignature signature;
    
        private LibraryEntry(Method method, MethodSignature signature) {
            assert method != null;
            assert signature != null;
            this.method = method;
            this.signature = signature;
        }
    
        private Method getMethod() {
            return this.method;
        }
    
        private boolean isCompatibleWith(MethodSignature signature) {
            return this.signature.isCompatibleWith(signature);
        }
    }
}