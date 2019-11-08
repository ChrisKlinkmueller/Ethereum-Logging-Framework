package au.csiro.data61.aap.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import au.csiro.data61.aap.spec.Method;
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
        // TODO: add default methods
    }

    public boolean addMethod(Method method) {
        if (method == null) {
            return false;
        }
        
        final List<Method> existingMethodList = this.methodDictionary.get(method.getName());
        if (existingMethodList == null) {
            final List<Method> methodList = new LinkedList<>();
            methodList.add(method);
            this.methodDictionary.put(method.getName(), methodList);
            return true;
        }
        
        if (this.containsMethod(existingMethodList, method)) {
            return false;
        }

        existingMethodList.add(method);
        return true;
    }

    public List<Method> getCompatibleMethods(Method method) {
        final List<Method> existingMethodList = this.methodDictionary.get(method.getName());
        if (existingMethodList == null) {
            return Collections.emptyList();
        }

        return existingMethodList.stream()
            .filter(existingMethod -> this.areMethodsCompatible(existingMethod, method))
            .collect(Collectors.toList());
    }

    private boolean containsMethod(List<Method> methodList, Method method) {
        return methodList.stream()
            .anyMatch(existingMethod -> this.isMethodSignatureEqual(existingMethod, method));
    }    

    private boolean isMethodSignatureEqual(Method method1, Method method2) {
        return this.compareMethods(method1, method2, (t1, t2) -> t1.conceptuallyEquals(t2));
    }

    private boolean areMethodsCompatible(Method method1, Method method2) {
        return this.compareMethods(method1, method2, (t1, t2) -> t1.castableFrom(t2));
    }

    private boolean compareMethods(Method method1, Method method2, BiPredicate<SolidityType, SolidityType> comparison) {
        if (!method1.getName().equals(method2.getName())) {
            return false;
        }

        if (method1.parameterTypeCount() != method2.parameterTypeCount()) {
            return false;
        }        

        return IntStream.range(0, method1.parameterTypeCount())
            .allMatch(i -> comparison.test(method1.getParameterType(i), method2.getParameterType(i)));
    }

    // TODO: test library
}