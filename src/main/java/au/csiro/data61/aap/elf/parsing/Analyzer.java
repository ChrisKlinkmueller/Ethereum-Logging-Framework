package au.csiro.data61.aap.elf.parsing;

import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

class Analyzer {
    
    InterpretationResult<ParseTree> analyze(ParseTree parseTree) {
        return InterpretationResult.failure(new InterpretationEvent(Type.ERROR, "Method not implemented!"));
    }

}
