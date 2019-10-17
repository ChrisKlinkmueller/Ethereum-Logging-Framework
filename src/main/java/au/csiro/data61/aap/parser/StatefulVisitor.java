package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.specification.ProgramState;

/**
 * StatefulVisitor
 */
public class StatefulVisitor<T> extends XbelBaseVisitor<T> {
    private final ProgramState state;
    
    public StatefulVisitor(ProgramState state) {
        assert state != null;
        this.state = state;
    }

    protected ProgramState getState() {
        return this.state;
    }
}