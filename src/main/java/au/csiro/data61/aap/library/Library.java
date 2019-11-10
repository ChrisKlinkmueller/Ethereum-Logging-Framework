package au.csiro.data61.aap.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.Method;
import au.csiro.data61.aap.spec.MethodSignature;
import au.csiro.data61.aap.spec.types.SolidityType;

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

	public boolean isMethodNameKnown(String name) {
        return name != null && this.methodDictionary.containsKey(name);
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

    // TODO: test library
}