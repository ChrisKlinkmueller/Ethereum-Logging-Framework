package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.types.Type;

class DataItem {
    
    static DataItem createConstant(Type type, String name) {
        return new DataItem(type, name, true);
    }

    static DataItem createVariable(Type type, String name) {
        return new DataItem(type, name, false);
    }

    private final Type type;
    private final boolean isConstant;
    private final String name;    

    private DataItem(Type type, String name, boolean isConstant) {
        checkNotNull(type);
        checkNotNull(name);
        checkArgument(!name.isBlank());
        this.type = type;
        this.isConstant = isConstant;
        this.name = name;
    }

    boolean isConstant() {
        return this.isConstant;
    }

    boolean isVariable() {
        return !this.isConstant;
    }

    String getName() {
        return this.name;
    }

    Type getType() {
        return this.type;
    }

}
