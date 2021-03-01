package blf.blockchains.template;

import blf.core.state.ProgramState;

import java.math.BigInteger;

/*
 * TemplateProgramState holds the state of your program here you can save variables that are
 * needed in instructions.
 */
public class TemplateProgramState extends ProgramState {

    public TemplateProgramState() {
        super(new TemplateVariables());
    }

    // For example the currentBlockNumber could be stored.
    private BigInteger currentBlockNumber;

    public BigInteger getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public void setCurrentBlockNumber(BigInteger currentBlockNumber) {
        this.currentBlockNumber = currentBlockNumber;
    }
}
