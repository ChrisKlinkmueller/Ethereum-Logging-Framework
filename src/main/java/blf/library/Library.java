package blf.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import blf.core.interfaces.Method;
import blf.library.compression.BitMapping;
import blf.library.compression.ValueDictionary;
import blf.library.types.IntegerOperations;
import blf.library.types.ListOperations;

/**
 * Library of methods and operators, which can be used in the manifest file.
 *
 * TODO: remove int from int[] method
 * TODO: clear int from int[] method
 * TODO: capabilities of int[] and address[] for string[]
 */
public class Library {
    private static final Logger LOGGER = Logger.getLogger(Library.class.getName());
    public static final Library INSTANCE = new Library();

    private static final String TYPE_STRING = "string";
    private static final String TYPE_ADDRESS = "address";
    private static final String TYPE_BOOLLIST = "bool[]";
    private static final String TYPE_STRINGLIST = "string[]";
    private static final String TYPE_INTLIST = "int[]";
    private static final String TYPE_BYTELIST = "byte[]";
    private static final String TYPE_ADDRESSLIST = "address[]";

    private final Map<String, List<LibraryEntry>> registeredMethods;

    private Library() {
        this.registeredMethods = new HashMap<>();

        try {
            this.addMethod(new MethodSignature("add", "int", "int", "int"), IntegerOperations::add);
            this.addMethod(new MethodSignature("multiply", "int", "int", "int"), IntegerOperations::multiply);
            this.addMethod(new MethodSignature("subtract", "int", "int", "int"), IntegerOperations::subtract);
            this.addMethod(new MethodSignature("divide", "int", "int", "int"), IntegerOperations::divide);

            this.addMethod(new MethodSignature("contains", "bool", TYPE_ADDRESSLIST, TYPE_ADDRESS), ListOperations::contains);
            this.addMethod(new MethodSignature("contains", "bool", TYPE_INTLIST, "int"), ListOperations::contains);
            this.addMethod(new MethodSignature("add", null, TYPE_INTLIST, "int"), ListOperations::addElement);
            this.addMethod(new MethodSignature("add", null, TYPE_ADDRESSLIST, TYPE_ADDRESS), ListOperations::addElement);
            this.addMethod(new MethodSignature("remove", null, TYPE_ADDRESSLIST, TYPE_ADDRESS), ListOperations::removeElement);
            this.addMethod(new MethodSignature("clear", null, TYPE_ADDRESSLIST), ListOperations::clear);

            this.addMethod(ValueDictionary::boolToBool, "bool", ValueDictionary.METHOD_NAME, "bool", "bool", TYPE_BOOLLIST, TYPE_BOOLLIST);
            this.addMethod(ValueDictionary::stringToBool, "bool", ValueDictionary.METHOD_NAME, "byte", "bool", TYPE_BYTELIST, TYPE_BOOLLIST);
            this.addMethod(ValueDictionary::intToBool, "bool", ValueDictionary.METHOD_NAME, "int", "bool", TYPE_INTLIST, TYPE_BOOLLIST);
            this.addMethod(ValueDictionary::stringToBool, "bool", ValueDictionary.METHOD_NAME, TYPE_STRING, "bool", TYPE_STRINGLIST, TYPE_BOOLLIST);
            this.addMethod(ValueDictionary::boolToString, "byte", ValueDictionary.METHOD_NAME, "bool", "byte", TYPE_BOOLLIST, TYPE_BYTELIST);
            this.addMethod(ValueDictionary::stringToString, "byte", ValueDictionary.METHOD_NAME, "byte", "byte", TYPE_BYTELIST, TYPE_BYTELIST);
            this.addMethod(ValueDictionary::intToString, "byte", ValueDictionary.METHOD_NAME, "int", "byte", TYPE_INTLIST, TYPE_BYTELIST);
            this.addMethod(ValueDictionary::stringToString, "byte", ValueDictionary.METHOD_NAME, TYPE_STRING, "byte", TYPE_STRINGLIST, TYPE_BYTELIST);
            this.addMethod(ValueDictionary::boolToInt, "int", ValueDictionary.METHOD_NAME, "bool", "int", TYPE_BOOLLIST, TYPE_INTLIST);
            this.addMethod(ValueDictionary::stringToInt, "int", ValueDictionary.METHOD_NAME, "byte", "int", TYPE_BYTELIST, TYPE_INTLIST);
            this.addMethod(ValueDictionary::intToInt, "int", ValueDictionary.METHOD_NAME, "int", "int", TYPE_INTLIST, TYPE_INTLIST);
            this.addMethod(ValueDictionary::stringToInt, "int", ValueDictionary.METHOD_NAME, TYPE_STRING, "int", TYPE_STRINGLIST, TYPE_INTLIST);
            this.addMethod(ValueDictionary::boolToString, TYPE_STRING, ValueDictionary.METHOD_NAME, "bool", TYPE_STRING, TYPE_BOOLLIST, TYPE_STRINGLIST);
            this.addMethod(ValueDictionary::stringToString, TYPE_STRING, ValueDictionary.METHOD_NAME, "byte", TYPE_STRING, TYPE_BYTELIST, TYPE_STRINGLIST);
            this.addMethod(ValueDictionary::intToString, TYPE_STRING, ValueDictionary.METHOD_NAME, "int", TYPE_STRING, TYPE_INTLIST, TYPE_STRINGLIST);
            this.addMethod(ValueDictionary::stringToString, TYPE_STRING, ValueDictionary.METHOD_NAME, TYPE_STRING, TYPE_STRING, TYPE_STRINGLIST, TYPE_STRINGLIST);
            this.addMethod(BitMapping::mapBitsToString, TYPE_STRING, BitMapping.METHOD_NAME, "int", "int", "int", TYPE_STRINGLIST);
            this.addMethod(BitMapping::mapBitsToString, "byte", BitMapping.METHOD_NAME, "int", "int", "int", TYPE_BYTELIST);
            this.addMethod(BitMapping::mapBitsToInt, "int", BitMapping.METHOD_NAME, "int", "int", "int", TYPE_INTLIST);
            this.addMethod(BitMapping::mapBitsToBool, "bool", BitMapping.METHOD_NAME, "int", "int", "int", TYPE_BOOLLIST);

            this.addMethod(ListOperations::newAddressArray, TYPE_ADDRESSLIST, "newAddressArray");
            this.addMethod(ListOperations::newBoolArray, TYPE_BOOLLIST, "newBoolArray");
            this.addMethod(ListOperations::newByteArray, TYPE_BYTELIST, "newByteArray");
            this.addMethod(ListOperations::newIntArray, TYPE_INTLIST, "newIntArray");
            this.addMethod(ListOperations::newStringArray, TYPE_STRINGLIST, "newStringArray");
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
        if (signature == null || method == null) {
            throw new LibraryException("Signature or method is null");
        }
        this.registeredMethods.putIfAbsent(signature.getMethodName(), new LinkedList<>());
        final List<LibraryEntry> entries = this.registeredMethods.get(signature.getMethodName());
        if (this.containsEntry(entries, signature)) {
            throw new LibraryException(String.format("Entry with signature '%s' already exists.", signature.getSignature()));
        }
        entries.add(new LibraryEntry(method, signature));
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
            .map(mapper::apply)
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
