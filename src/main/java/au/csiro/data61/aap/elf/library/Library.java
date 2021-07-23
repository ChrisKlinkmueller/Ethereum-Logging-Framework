package au.csiro.data61.aap.elf.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.elf.core.Method;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.library.compression.BitMapping;
import au.csiro.data61.aap.elf.library.compression.ValueDictionary;
import au.csiro.data61.aap.elf.library.types.IntegerOperations;
import au.csiro.data61.aap.elf.library.types.ListOperations;

/**
 * Library
 */
public class Library {
    public static final String ADD_XES_EVENT_CLASSIFIER = "addXesEventClassifier";
    public static final String ADD_XES_GLOBAL_EVENT_ATTRIBUTE = "addGlobalXesEventAttribute";

    private static final Logger LOGGER = Logger.getLogger(Library.class.getName());
    public static Library INSTANCE = new Library();

    private final Map<String, List<LibraryEntry>> registeredMethods;

    private Library() {
        this.registeredMethods = new HashMap<>();

        try {
            this.addMethod(new MethodSignature("connect", null, "string"), ProgramState::connectWebsocketClient);
            this.addMethod(new MethodSignature("connectIpc", null, "string"), ProgramState::connectIpcClient);
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

            this.addMethod(ValueDictionary::boolToBool, "bool", ValueDictionary.METHOD_NAME, "bool", "bool", "bool[]", "bool[]");
            this.addMethod(ValueDictionary::stringToBool, "bool", ValueDictionary.METHOD_NAME, "byte", "bool", "byte[]", "bool[]");
            this.addMethod(ValueDictionary::intToBool, "bool", ValueDictionary.METHOD_NAME, "int", "bool", "int[]", "bool[]");
            this.addMethod(ValueDictionary::stringToBool, "bool", ValueDictionary.METHOD_NAME, "string", "bool", "string[]", "bool[]");
            this.addMethod(ValueDictionary::boolToString, "byte", ValueDictionary.METHOD_NAME, "bool", "byte", "bool[]", "byte[]");
            this.addMethod(ValueDictionary::stringToString, "byte", ValueDictionary.METHOD_NAME, "byte", "byte", "byte[]", "byte[]");
            this.addMethod(ValueDictionary::intToString, "byte", ValueDictionary.METHOD_NAME, "int", "byte", "int[]", "byte[]");
            this.addMethod(ValueDictionary::stringToString, "byte", ValueDictionary.METHOD_NAME, "string", "byte", "string[]", "byte[]");
            this.addMethod(ValueDictionary::boolToInt, "int", ValueDictionary.METHOD_NAME, "bool", "int", "bool[]", "int[]");
            this.addMethod(ValueDictionary::stringToInt, "int", ValueDictionary.METHOD_NAME, "byte", "int", "byte[]", "int[]");
            this.addMethod(ValueDictionary::intToInt, "int", ValueDictionary.METHOD_NAME, "int", "int", "int[]", "int[]");
            this.addMethod(ValueDictionary::stringToInt, "int", ValueDictionary.METHOD_NAME, "string", "int", "string[]", "int[]");
            this.addMethod(ValueDictionary::boolToString, "string", ValueDictionary.METHOD_NAME, "bool", "string", "bool[]", "string[]");
            this.addMethod(ValueDictionary::stringToString, "string", ValueDictionary.METHOD_NAME, "byte", "string", "byte[]", "string[]");
            this.addMethod(ValueDictionary::intToString, "string", ValueDictionary.METHOD_NAME, "int", "string", "int[]", "string[]");
            this.addMethod(
                ValueDictionary::stringToString,
                "string",
                ValueDictionary.METHOD_NAME,
                "string",
                "string",
                "string[]",
                "string[]"
            );
            this.addMethod(BitMapping::mapBitsToString, "string", BitMapping.METHOD_NAME, "int", "int", "int", "string[]");
            this.addMethod(BitMapping::mapBitsToString, "byte", BitMapping.METHOD_NAME, "int", "int", "int", "byte[]");
            this.addMethod(BitMapping::mapBitsToInt, "int", BitMapping.METHOD_NAME, "int", "int", "int", "int[]");
            this.addMethod(BitMapping::mapBitsToBool, "bool", BitMapping.METHOD_NAME, "int", "int", "int", "bool[]");

            this.addMethod(ListOperations::newAddressArray, "address[]", "newAddressArray");
            this.addMethod(ListOperations::newBoolArray, "bool[]", "newBoolArray");
            this.addMethod(ListOperations::newByteArray, "byte[]", "newByteArray");
            this.addMethod(ListOperations::newIntArray, "int[]", "newIntArray");
            this.addMethod(ListOperations::newStringArray, "string[]", "newStringArray");

            this.addMethod(ProgramState::setXesGlobalEventAttribte, null, ADD_XES_GLOBAL_EVENT_ATTRIBUTE, "string", "string", "bool");
            this.addMethod(ProgramState::setXesGlobalEventAttribte, null, ADD_XES_GLOBAL_EVENT_ATTRIBUTE, "string", "string", "byte");
            this.addMethod(ProgramState::setXesGlobalEventAttribte, null, ADD_XES_GLOBAL_EVENT_ATTRIBUTE, "string", "string", "int");
            this.addMethod(ProgramState::setXesGlobalEventAttribte, null, ADD_XES_GLOBAL_EVENT_ATTRIBUTE, "string", "string", "string");
            this.addMethod(ProgramState::addXesClassifier, null, ADD_XES_EVENT_CLASSIFIER, "string", "string[]");
        } catch (LibraryException e) {
            e.printStackTrace();
        }
    }

    private void addMethod(Method method, String returnType, String methodName, String... parameterTypes) {
        final MethodSignature signature = new MethodSignature(methodName, returnType, parameterTypes);
        try {
            this.addMethod(signature, method);
        } catch (LibraryException ex) {
            LOGGER.log(Level.SEVERE, "Error during initialization of Library!", ex);
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
            .findFirst()
            .orElse(null);
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
