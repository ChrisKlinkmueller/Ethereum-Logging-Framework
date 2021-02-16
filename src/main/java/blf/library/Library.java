package blf.library;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import blf.core.interfaces.Method;
import blf.library.compression.BitMapping;
import blf.library.compression.ValueDictionary;
import blf.library.types.IntegerOperations;
import blf.library.types.ListOperations;
import blf.library.types.StringOperations;
import blf.library.util.ReaderOperations;

/**
 * Library of methods and operators, which can be used in the manifest file. These methods and operators serve mainly for
 * transformation purposes to prepare or process given data. They do not provide access to any blockchain related logging
 * data.
 * <p>
 * The type constants instantiated in the beginning of this file correspond to the types defined in the grammar files of
 * the parser generator ANTLR4. The Java type implication does not match in every case, e.g. TYPE_INT/"int" is a BigInteger
 * or TYPE_INTLIST/"int[]" is a List&lt;BigInteger&gt; as parameter or return value in the methods.
 * <p>
 * For more information look into the "Transformation Capabilities" segment in the wiki or check out and run the manifest
 * file "TransformationCapabilities.bcql".
 */

public class Library {
    private static final Logger LOGGER = Logger.getLogger(Library.class.getName());
    public static final Library INSTANCE = new Library();

    private static final String TYPE_BOOL = "bool";
    private static final String TYPE_INT = "int";
    private static final String TYPE_BYTE = "byte";
    private static final String TYPE_STRING = "string";
    private static final String TYPE_ADDRESS = "address";

    private static final String TYPE_BOOLLIST = "bool[]";
    private static final String TYPE_INTLIST = "int[]";
    private static final String TYPE_BYTELIST = "byte[]";
    private static final String TYPE_STRINGLIST = "string[]";
    private static final String TYPE_ADDRESSLIST = "address[]";

    private final Map<String, List<LibraryEntry>> registeredMethods;

    private Library() {
        this.registeredMethods = new HashMap<>();

        try {
            // Integer Operations
            this.addMethod(new MethodSignature("add", TYPE_INT, TYPE_INT, TYPE_INT), IntegerOperations::add);
            this.addMethod(new MethodSignature("multiply", TYPE_INT, TYPE_INT, TYPE_INT), IntegerOperations::multiply);
            this.addMethod(new MethodSignature("subtract", TYPE_INT, TYPE_INT, TYPE_INT), IntegerOperations::subtract);
            this.addMethod(new MethodSignature("divide", TYPE_INT, TYPE_INT, TYPE_INT), IntegerOperations::divide);

            // String Operations
            this.addMethod(new MethodSignature("split", TYPE_STRINGLIST, TYPE_STRING, TYPE_STRING), StringOperations::split);
            this.addMethod(new MethodSignature("match", TYPE_BOOL, TYPE_STRING, TYPE_STRING), StringOperations::matches);
            this.addMethod(
                new MethodSignature("replaceFirst", TYPE_STRING, TYPE_STRING, TYPE_STRING, TYPE_STRING),
                StringOperations::replaceFirst
            );
            this.addMethod(
                new MethodSignature("replaceAll", TYPE_STRING, TYPE_STRING, TYPE_STRING, TYPE_STRING),
                StringOperations::replaceAll
            );
            this.addMethod(new MethodSignature("length", TYPE_INT, TYPE_STRING), StringOperations::length);

            // Boolean List Operations
            this.addMethod(ListOperations::newBoolArray, TYPE_BOOLLIST, "newBoolArray");
            this.addMethod(new MethodSignature("contains", TYPE_BOOL, TYPE_BOOLLIST, TYPE_BOOL), ListOperations::contains);
            this.addMethod(new MethodSignature("add", null, TYPE_BOOLLIST, TYPE_BOOL), ListOperations::addElement);
            this.addMethod(new MethodSignature("remove", null, TYPE_BOOLLIST, TYPE_BOOL), ListOperations::removeElement);
            this.addMethod(new MethodSignature("clear", null, TYPE_BOOLLIST), ListOperations::clear);
            this.addMethod(new MethodSignature("get", TYPE_BOOL, TYPE_BOOLLIST, TYPE_INT), ListOperations::get);

            // Integer List Operations
            this.addMethod(ListOperations::newIntArray, TYPE_INTLIST, "newIntArray");
            this.addMethod(new MethodSignature("contains", TYPE_BOOL, TYPE_INTLIST, TYPE_INT), ListOperations::contains);
            this.addMethod(new MethodSignature("add", null, TYPE_INTLIST, TYPE_INT), ListOperations::addElement);
            this.addMethod(new MethodSignature("remove", null, TYPE_INTLIST, TYPE_INT), ListOperations::removeElement);
            this.addMethod(new MethodSignature("clear", null, TYPE_INTLIST), ListOperations::clear);
            this.addMethod(new MethodSignature("get", TYPE_INT, TYPE_INTLIST, TYPE_INT), ListOperations::get);
            this.addMethod(new MethodSignature("reduceToSum", TYPE_INT, TYPE_INTLIST), ListOperations::reduceToSum);
            this.addMethod(new MethodSignature("reduceToProduct", TYPE_INT, TYPE_INTLIST), ListOperations::reduceToProduct);

            // String List Operations
            this.addMethod(ListOperations::newStringArray, TYPE_STRINGLIST, "newStringArray");
            this.addMethod(new MethodSignature("contains", TYPE_BOOL, TYPE_STRINGLIST, TYPE_STRING), ListOperations::contains);
            this.addMethod(new MethodSignature("add", null, TYPE_STRINGLIST, TYPE_STRING), ListOperations::addElement);
            this.addMethod(new MethodSignature("remove", null, TYPE_STRINGLIST, TYPE_STRING), ListOperations::removeElement);
            this.addMethod(new MethodSignature("clear", null, TYPE_STRINGLIST), ListOperations::clear);
            this.addMethod(new MethodSignature("get", TYPE_STRING, TYPE_STRINGLIST, TYPE_INT), ListOperations::get);
            this.addMethod(new MethodSignature("reduceToString", TYPE_STRING, TYPE_STRINGLIST), ListOperations::reduceToString);

            // Address List Operations
            this.addMethod(ListOperations::newAddressArray, TYPE_ADDRESSLIST, "newAddressArray");
            this.addMethod(new MethodSignature("contains", TYPE_BOOL, TYPE_ADDRESSLIST, TYPE_ADDRESS), ListOperations::contains);
            this.addMethod(new MethodSignature("add", null, TYPE_ADDRESSLIST, TYPE_ADDRESS), ListOperations::addElement);
            this.addMethod(new MethodSignature("remove", null, TYPE_ADDRESSLIST, TYPE_ADDRESS), ListOperations::removeElement);
            this.addMethod(new MethodSignature("clear", null, TYPE_ADDRESSLIST), ListOperations::clear);
            this.addMethod(new MethodSignature("get", TYPE_ADDRESS, TYPE_ADDRESSLIST, TYPE_INT), ListOperations::get);

            // Byte List Operations
            this.addMethod(ListOperations::newByteArray, TYPE_BYTELIST, "newByteArray");

            // Reader Operations
            this.addMethod(new MethodSignature("readIn", TYPE_STRINGLIST, TYPE_STRING), ReaderOperations::readIn);

            // Compression Operations (Only used in the Ethereum Generator)
            this.addMethod(
                ValueDictionary::boolToBool,
                TYPE_BOOL,
                ValueDictionary.METHOD_NAME,
                TYPE_BOOL,
                TYPE_BOOL,
                TYPE_BOOLLIST,
                TYPE_BOOLLIST
            );
            this.addMethod(
                ValueDictionary::stringToBool,
                TYPE_BOOL,
                ValueDictionary.METHOD_NAME,
                TYPE_BYTE,
                TYPE_BOOL,
                TYPE_BYTELIST,
                TYPE_BOOLLIST
            );
            this.addMethod(
                ValueDictionary::intToBool,
                TYPE_BOOL,
                ValueDictionary.METHOD_NAME,
                TYPE_INT,
                TYPE_BOOL,
                TYPE_INTLIST,
                TYPE_BOOLLIST
            );
            this.addMethod(
                ValueDictionary::stringToBool,
                TYPE_BOOL,
                ValueDictionary.METHOD_NAME,
                TYPE_STRING,
                TYPE_BOOL,
                TYPE_STRINGLIST,
                TYPE_BOOLLIST
            );
            this.addMethod(
                ValueDictionary::boolToString,
                TYPE_BYTE,
                ValueDictionary.METHOD_NAME,
                TYPE_BOOL,
                TYPE_BYTE,
                TYPE_BOOLLIST,
                TYPE_BYTELIST
            );
            this.addMethod(
                ValueDictionary::stringToString,
                TYPE_BYTE,
                ValueDictionary.METHOD_NAME,
                TYPE_BYTE,
                TYPE_BYTE,
                TYPE_BYTELIST,
                TYPE_BYTELIST
            );
            this.addMethod(
                ValueDictionary::intToString,
                TYPE_BYTE,
                ValueDictionary.METHOD_NAME,
                TYPE_INT,
                TYPE_BYTE,
                TYPE_INTLIST,
                TYPE_BYTELIST
            );
            this.addMethod(
                ValueDictionary::stringToString,
                TYPE_BYTE,
                ValueDictionary.METHOD_NAME,
                TYPE_STRING,
                TYPE_BYTE,
                TYPE_STRINGLIST,
                TYPE_BYTELIST
            );
            this.addMethod(
                ValueDictionary::boolToInt,
                TYPE_INT,
                ValueDictionary.METHOD_NAME,
                TYPE_BOOL,
                TYPE_INT,
                TYPE_BOOLLIST,
                TYPE_INTLIST
            );
            this.addMethod(
                ValueDictionary::stringToInt,
                TYPE_INT,
                ValueDictionary.METHOD_NAME,
                TYPE_BYTE,
                TYPE_INT,
                TYPE_BYTELIST,
                TYPE_INTLIST
            );
            this.addMethod(
                ValueDictionary::intToInt,
                TYPE_INT,
                ValueDictionary.METHOD_NAME,
                TYPE_INT,
                TYPE_INT,
                TYPE_INTLIST,
                TYPE_INTLIST
            );
            this.addMethod(
                ValueDictionary::stringToInt,
                TYPE_INT,
                ValueDictionary.METHOD_NAME,
                TYPE_STRING,
                TYPE_INT,
                TYPE_STRINGLIST,
                TYPE_INTLIST
            );
            this.addMethod(
                ValueDictionary::boolToString,
                TYPE_STRING,
                ValueDictionary.METHOD_NAME,
                TYPE_BOOL,
                TYPE_STRING,
                TYPE_BOOLLIST,
                TYPE_STRINGLIST
            );
            this.addMethod(
                ValueDictionary::stringToString,
                TYPE_STRING,
                ValueDictionary.METHOD_NAME,
                TYPE_BYTE,
                TYPE_STRING,
                TYPE_BYTELIST,
                TYPE_STRINGLIST
            );
            this.addMethod(
                ValueDictionary::intToString,
                TYPE_STRING,
                ValueDictionary.METHOD_NAME,
                TYPE_INT,
                TYPE_STRING,
                TYPE_INTLIST,
                TYPE_STRINGLIST
            );
            this.addMethod(
                ValueDictionary::stringToString,
                TYPE_STRING,
                ValueDictionary.METHOD_NAME,
                TYPE_STRING,
                TYPE_STRING,
                TYPE_STRINGLIST,
                TYPE_STRINGLIST
            );
            this.addMethod(BitMapping::mapBitsToString, TYPE_STRING, BitMapping.METHOD_NAME, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_STRINGLIST);
            this.addMethod(BitMapping::mapBitsToString, TYPE_BYTE, BitMapping.METHOD_NAME, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_BYTELIST);
            this.addMethod(BitMapping::mapBitsToInt, TYPE_INT, BitMapping.METHOD_NAME, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INTLIST);
            this.addMethod(BitMapping::mapBitsToBool, TYPE_BOOL, BitMapping.METHOD_NAME, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_BOOLLIST);

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
