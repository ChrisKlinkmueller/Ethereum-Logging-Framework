package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.grammar.EthqlBaseListener;

public abstract class AnalysisRule extends EthqlBaseListener {
    protected final SymbolTableBuilder symbolTableBuilder;
    protected final InterpretationEventCollector eventCollector;

    protected AnalysisRule(InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        checkNotNull(symbolTableBuilder);
        checkNotNull(eventCollector);
        this.eventCollector = eventCollector;
        this.symbolTableBuilder = symbolTableBuilder;
    }
}
