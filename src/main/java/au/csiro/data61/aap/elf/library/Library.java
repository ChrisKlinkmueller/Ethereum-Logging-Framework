package au.csiro.data61.aap.elf.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import au.csiro.data61.aap.elf.core.Method;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.library.types.IntegerOperations;
import au.csiro.data61.aap.elf.library.types.ListOperations;

/**
 * Library
 */
public class Library {
    public static Library INSTANCE = new Library();

    private final Map<String, List<LibraryEntry>> registeredMethods;

    private Library() {
        this.registeredMethods = new HashMap<>();

        try {
            this.addMethod(new MethodSignature("connect", null, "string"), ProgramState::connectClient);
            this.addMethod(new MethodSignature("setOutputFolder", null, "string"), ProgramState::setOutputFolder);
            this.addMethod(new MethodSignature("add", "int", "int", "int"), IntegerOperations::add);
            this.addMethod(new MethodSignature("multiply", "int", "int", "int"), IntegerOperations::multiply);
            this.addMethod(new MethodSignature("subtract", "int", "int", "int"), IntegerOperations::subtract);
            this.addMethod(new MethodSignature("divide", "int", "int", "int"), IntegerOperations::divide);
            this.addMethod(new MethodSignature("contains", "bool", "address[]", "address"), ListOperations::contains);
            this.addMethod(new MethodSignature("contains", "bool", "int[]", "int"), ListOperations::contains);
            this.addMethod(new MethodSignature("add", null, "int[]", "int"), ListOperations::addElement);
            this.addMethod(new MethodSignature("add", null, "address[]", "address"), ListOperations::addElement);
            this.addMethod(new MethodSignature("remove", null, "address[]", "address"), ListOperations::removeElement);
            this.addMethod(new MethodSignature("clear", null, "address[]"), ListOperations::clear);

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

    

	public boolean isMethodNameKnown(String methodName) {
		return this.registeredMethods.containsKey(methodName);
    }

    public Method findMethod(String methodName, List<String> parameterTypes) {
        return this.findEntry(methodName, parameterTypes, LibraryEntry::getMethod);
    }

    public MethodSignature retrieveSignature(String methodName, List<String> parameterTypes) {
        return this.findEntry(methodName, parameterTypes, LibraryEntry::getSignature);
    }

    private <T> T findEntry(String methodName, List<String> parameterTypes, Function<LibraryEntry, T> mapper) {
        assert methodName != null;
        assert parameterTypes != null && parameterTypes.stream().allMatch(Objects::nonNull);
        final MethodSignature requestedSignature = new MethodSignature(methodName, "", parameterTypes);
        return this.registeredMethods.getOrDefault(methodName, Collections.emptyList())
            .stream()
            .filter(re -> re.isCompatibleWith(requestedSignature))
            .map(re -> mapper.apply(re))
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

        public MethodSignature getSignature() {
            return this.signature;
        }
    
        private boolean isCompatibleWith(MethodSignature signature) {
            return this.signature.isCompatibleWith(signature);
        }
    }
}