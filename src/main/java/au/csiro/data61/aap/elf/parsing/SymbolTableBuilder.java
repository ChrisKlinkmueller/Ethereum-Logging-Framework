package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.grammar.EthqlBaseListener;

public class SymbolTableBuilder extends EthqlBaseListener {
    private final InterpretationEventCollector eventCollector;
    private SymbolTable currentTable;

    SymbolTableBuilder(InterpretationEventCollector eventCollector) {
        checkNotNull(eventCollector);
        this.currentTable = new SymbolTable();
        this.eventCollector = eventCollector;
    }

    SymbolTable getCurrentTable() {
        return this.currentTable;
    }
}
