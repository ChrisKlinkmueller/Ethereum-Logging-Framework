package au.csiro.data61.aap.library;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.MethodSignature;
import au.csiro.data61.aap.program.types.SolidityType;

/**
 * Library
 */
public class Library {
    public static final Library INSTANCE = new Library();

    private final HashMap<String, List<Method>> methodDictionary;    

    private Library() {
        this.methodDictionary = new HashMap<>();
        this.init();
    }

    private void init() {
        this.add(Casts.castStream());
        this.add(Configurations.configurationMethods());
        // TODO: add default methods
    }

    private void add(Stream<Method> methods) {
        methods.forEach(method -> this.addMethod(method));
    }

    public boolean addMethod(Method method) {
        if (method == null) {
            return false;
        }
        
        final List<Method> existingMethodList = this.methodDictionary.get(method.getSignature().getName());
        if (existingMethodList == null) {
            final List<Method> methodList = new LinkedList<>();
            methodList.add(method);
            this.methodDictionary.put(method.getSignature().getName(), methodList);
            return true;
        }
        
        if (this.containsMethod(existingMethodList, method.getSignature())) {
            return false;
        }

        existingMethodList.add(method);
        return true;
    }

    public Method getMethod(String name, SolidityType... parameterTypes) {
        assert name != null;
        assert Arrays.stream(parameterTypes).allMatch(Objects::nonNull);
        return this.methodStream()
            .filter(method -> isCompatibleSignature(method.getSignature(), name, parameterTypes))
            .findFirst().orElse(null);
    }

	private boolean isCompatibleSignature(MethodSignature signature, String name, SolidityType[] parameterTypes) {
        if (!signature.getName().equals(name) || signature.parameterTypeCount() != parameterTypes.length) {
            return false;
        }
        
        return IntStream.range(0, parameterTypes.length)
            .allMatch(i -> parameterTypes[i].conceptuallyEquals(signature.getParameterType(i)));
    }

    public boolean isMethodNameKnown(String name) {
        return name != null && this.methodDictionary.containsKey(name);
    }
    
    public long methodCount() {
        return this.methodStream().count();
    }

    public Stream<Method> methodStream() {
        return this.methodDictionary
            .entrySet()
            .stream()
            .flatMap(e -> e.getValue().stream());
    }



    public Stream<Method> methodStream(String method) {
        return this.methodDictionary
            .getOrDefault(method, Collections.emptyList())
            .stream();
    }

    private boolean containsMethod(List<Method> methodList, MethodSignature signature) {
        return methodList.stream()
            .anyMatch(method -> this.areMethodSignaturesEqual(method, signature));
    }    

    private boolean areMethodSignaturesEqual(Method method, MethodSignature signature) {
        return this.compareMethodSignatures(method, signature, (t1, t2) -> t1.conceptuallyEquals(t2));
    }

    private boolean compareMethodSignatures(Method method, MethodSignature signature, BiPredicate<SolidityType, SolidityType> comparison) {
        if (!method.getSignature().getName().equals(signature.getName())) {
            return false;
        }

        if (method.getSignature().parameterTypeCount() != signature.parameterTypeCount()) {
            return false;
        }        

        return IntStream.range(0, signature.parameterTypeCount())
            .allMatch(i -> comparison.test(method.getSignature().getParameterType(i), signature.getParameterType(i)));
    }

    static boolean isValidParameterList(Object[] parameters, Class<?> cl) {
        return parameters != null && parameters.length == 1 && parameters[0].getClass().equals(cl);
    }

    // TODO: test library
}