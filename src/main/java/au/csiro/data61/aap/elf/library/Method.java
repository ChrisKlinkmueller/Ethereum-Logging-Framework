package au.csiro.data61.aap.elf.library;

import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.types.Type;

public class Method {
    private final MethodSignature signature;
    private final Type returnType;

    public Method(MethodSignature signature, Type returnType) {
        checkNotNull(signature);
        checkNotNull(returnType);
        this.signature = signature;
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return this.returnType;
    }

    public MethodSignature getSignature() {
        return this.signature;
    }
}
