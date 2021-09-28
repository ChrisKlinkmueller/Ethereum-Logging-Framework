package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class SymbolTable {
    private final SymbolTable parent;
    private final Map<String, DataItem> items;

    SymbolTable() {
        this(null);
    }
    
    SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.items = new HashMap<>();
    }

    void addItem(DataItem item) {
        checkNotNull(item);
        this.items.put(item.getName(), item);
    }

    Optional<DataItem> findItem(String name) {
        checkNotNull(name);
        checkArgument(!name.isBlank());

        return this.searchItem(name);
    }

    Optional<DataItem> searchItem(String name) {
        final DataItem item = this.items.get(name);
        if (item != null) {
            return Optional.of(item);
        }
        else if (parent != null) {
            return this.parent.searchItem(name);
        }
        else {
            return Optional.empty();
        }
    }
    
}
