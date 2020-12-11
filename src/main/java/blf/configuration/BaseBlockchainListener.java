package blf.configuration;

import blf.core.filters.Program;
import blf.grammar.BcqlBaseListener;

/**
 * BaseBlockchainListener
 */
public abstract class BaseBlockchainListener extends BcqlBaseListener {

    protected Program program;
    protected BuildException error;

    public Program getProgram() {
        return this.program;
    }

    public boolean containsError() {
        return this.error != null;
    }

    public BuildException getError() {
        return this.error;
    }

}
