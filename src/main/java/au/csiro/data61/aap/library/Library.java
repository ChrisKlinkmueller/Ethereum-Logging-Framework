package au.csiro.data61.aap.library;

import java.util.HashMap;
import java.util.Map;

import au.csiro.data61.aap.spec.Method;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Library
 */
public class Library {
    private final Map<String, Method> methodDictionary;    

    private Library() {
        this.methodDictionary = new HashMap<>();
        this.init();
    }

    private void init() {
        
    }

    public MethodResult<Void> addMethod(Method method) {
        if (this.containsMethodWithSameName(method)) {
            return MethodResult.ofError("A method with name '%s' already exists.");
        }

        this.methodDictionary.put(method.getName(), method);
        return MethodResult.ofResult();
    } 

    public MethodResult<Void> removeMethod(Method method) {
        if (!this.containsMethodWithSameName(method)) {
            return MethodResult.ofError("A method with name '%s' does not exist.");
        }

        this.methodDictionary.remove(method.getName());
        return MethodResult.ofResult();
    }

    public boolean containsMethodWithSameName(Method method) {
        return this.containsMethod(method.getName());
    }

    public boolean containsMethod(String name) {
        return this.methodDictionary.containsKey(name);
    }
}