package au.csiro.data61.aap.elf.configuration;

import java.util.LinkedList;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.EthqlBaseListener;
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer;
import au.csiro.data61.aap.elf.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.DocumentContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntrySignatureContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * EthqlProgramBuilder
 */
public class EthqlProgramBuilder extends EthqlBaseListener {
    private final SpecificationComposer composer;
    private final VariableExistenceAnalyzer analyzer;
    private BuildException error;
    private Program program;

    public EthqlProgramBuilder(VariableExistenceAnalyzer analyzer) {
        this.composer = new SpecificationComposer();
        this.analyzer = analyzer;
    }

    public boolean containsError() {
        return this.error != null;
    }

    public BuildException getError() {
        return this.error;
    }

    public Program getProgram() {
        return this.program;
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.handleEthqlElement(ctx, this::prepareProgramBuild);
    }

    private void prepareProgramBuild(DocumentContext ctx) throws BuildException {
        this.error = null;
        this.composer.prepareProgramBuild();
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.handleEthqlElement(ctx, this::buildProgram);
    }

    private void buildProgram(DocumentContext ctx) throws BuildException {
        this.program = this.composer.buildProgram();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareBlockFilterBuild);
    }

    private void prepareBlockFilterBuild(BlockFilterContext ctx) throws BuildException {
        this.composer.prepareBlockRangeBuild();
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildBlockFilter);
    }

    private void buildBlockFilter(BlockFilterContext ctx) throws BuildException {
        BlockNumberSpecification from = this.getBlockNumberSpecification(ctx.from);
        BlockNumberSpecification to = this.getBlockNumberSpecification(ctx.to);
        this.composer.buildBlockRange(from, to);
    }

    private BlockNumberSpecification getBlockNumberSpecification(BlockNumberContext ctx) throws BuildException {
        if (ctx.valueExpression() != null) {
            ValueAccessorSpecification number = this.getValueAccessor(ctx.valueExpression());
            return BlockNumberSpecification.ofBlockNumber(number);
        } else if (ctx.KEY_CURRENT() != null) {
            return BlockNumberSpecification.ofCurrent();
        } else if (ctx.KEY_EARLIEST() != null) {
            return BlockNumberSpecification.ofEarliest();
        } else if (ctx.KEY_CONTINUOUS() != null) {
            return BlockNumberSpecification.ofContinuous();
        } else {
            throw new BuildException("Unsupported variable declaration.");
        }
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareTransactionFilterBuild);
    }

    private void prepareTransactionFilterBuild(TransactionFilterContext ctx) throws BuildException {
        this.composer.prepareTransactionFilterBuild();
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildTransactionFilter);
    }

    private void buildTransactionFilter(TransactionFilterContext ctx) throws BuildException {
        final AddressListSpecification senders = this.getAddressListSpecification(ctx.senders);
        final AddressListSpecification recipients = this.getAddressListSpecification(ctx.recipients);
        this.composer.buildTransactionFilter(senders, recipients);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.handleEthqlElement(ctx, this::prepareLogEntryFilterBuild);
    }

    private void prepareLogEntryFilterBuild(LogEntryFilterContext ctx) throws BuildException {
        this.composer.prepareLogEntryFilterBuild();
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.handleEthqlElement(ctx, this::buildLogEntryFilter);
    }

    private void buildLogEntryFilter(LogEntryFilterContext ctx) throws BuildException {
        final AddressListSpecification contracts = this.getAddressListSpecification(ctx.addressList());
        final LogEntrySignatureSpecification signature = this.getLogEntrySignature(ctx.logEntrySignature());
        this.composer.buildLogEntryFilter(contracts, signature);
    }

    private LogEntrySignatureSpecification getLogEntrySignature(LogEntrySignatureContext ctx) throws BuildException {
        final LinkedList<LogEntryParameterSpecification> parameters = new LinkedList<>();
        for (LogEntryParameterContext paramCtx : ctx.logEntryParameter()) {
            parameters.add(
                LogEntryParameterSpecification.of(
                    paramCtx.variableName().getText(), 
                    paramCtx.solType().getText(), 
                    paramCtx.KEY_INDEXED() != null
                )
            );
        }
        
        return LogEntrySignatureSpecification.of(ctx.methodName.getText(), parameters);
    }

    private AddressListSpecification getAddressListSpecification(AddressListContext ctx) throws BuildException {
        if (ctx.BYTES_LITERAL() != null) {
            return AddressListSpecification.ofAddresses(
                ctx.BYTES_LITERAL().stream()
                    .map(literal -> literal.getText())
                    .collect(Collectors.toList())
            );
        }
        else if (ctx.KEY_ANY() != null) {
            return AddressListSpecification.ofAny();
        }
        else if (ctx.variableName() != null) {
            return AddressListSpecification.ofAddress(ctx.variableName().getText());
        }
        else {
            return AddressListSpecification.ofEmpty();
        }
    }



    //#region Utils

    private <T extends ParserRuleContext> void handleEthqlElement(T ctx, BuilderMethod<T> builderMethod) {
        if (this.containsError()) {
            return;
        }

        try {
            builderMethod.build(ctx);
        } catch (BuildException e) {
            this.error = e;
        }
    }

    @FunctionalInterface
    private static interface BuilderMethod<T>  {
        public void build(T ctx) throws BuildException;
    }

    private ValueAccessorSpecification getValueAccessor(ValueExpressionContext ctx) throws BuildException {
        if (ctx.variableName() != null) {
            return ValueAccessorSpecification.ofVariable(ctx.getText());
        }
        else if (ctx.literal() != null) {
            return this.getLiteral(TypeUtils.INT_TYPE_KEYWORD, ctx.literal());
        }
        else {
            throw new UnsupportedOperationException("This value accessor specification is not supported.");
        }
    }

    private ValueAccessorSpecification getLiteral(String type, LiteralContext ctx) throws BuildException {
        return this.getLiteral(type, ctx.toString());
    }

    private ValueAccessorSpecification getLiteral(String type, String literal) throws BuildException {
        final boolean isArray = TypeUtils.isArrayType(type);
        if (TypeUtils.isArrayType(type, TypeUtils.ADDRESS_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.addressArrayLiteral(literal)
                : ValueAccessorSpecification.addressLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.BOOL_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.booleanArrayLiteral(literal)
                : ValueAccessorSpecification.booleanLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.BYTES_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.bytesArrayLiteral(literal)
                : ValueAccessorSpecification.bytesLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.INT_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.integerArrayLiteral(literal)
                : ValueAccessorSpecification.integerLiteral(literal);
        }
        else if (TypeUtils.isArrayType(type, TypeUtils.STRING_TYPE_KEYWORD)) {
            return isArray 
                ? ValueAccessorSpecification.stringArrayLiteral(literal)
                : ValueAccessorSpecification.stringLiteral(literal);
        }
        else {
            throw new BuildException(String.format("Unsupported type: '%s'.", type));
        }
    }

    //#endregion Utils
}    

    

    

    // @Override
    // public void exitStatement(StatementContext ctx) {
    //     this.handleEthqlElement(ctx, this::buildStatement);
    // }

    // private void buildStatement(StatementContext ctx) throws BuildException {
        // final String assignedVariable = this.getVariable(ctx.variable());
        // if (ctx.valueCreation().methodCall() != null) {
        //     this.buildMethodCallStatement(assignedVariable, ctx.valueCreation().methodCall());
        // } 
        // else if (ctx.valueCreation().variableReference() != null) {
        //     this.buildVariableAssignment(assignedVariable, ctx.valueCreation().variableReference());
        // } 
        // else if (ctx.valueCreation().literal() != null) {
        //     this.buildVariableAssignment(assignedVariable, ctx.valueCreation().literal());
        // }
        // else {
            // throw new BuildException("Unsupported statement declaration.");
        // }
    // }

    // private String getVariable(VariableContext variable) throws BuildException {
    //     if (variable == null) {
    //         return null;
    //     }
    //     else if (variable.variableDefinition() != null) {
    //         return variable.variableDefinition().variableName().getText();
    //     }
    //     else if (variable.variableReference() != null) {
    //         return variable.variableReference().variableName().getText();
    //     }
    //     else {
    //         throw new BuildException("Unsupported variable declaration.");
    //     }
    // }

    // private void buildMethodCallStatement(String assignedVariable, MethodCallContext ctx) throws BuildException {
    //     // TODO: implement
    // }

    // private void buildVariableAssignment(String assignedVariable, VariableReferenceContext variableReference) throws BuildException {
    //     if (assignedVariable == null) {
    //         throw new BuildException("No variable for assignement specified.");
    //     }
    //     final ValueMutatorSpecification variable = ValueMutatorSpecification.ofVariableName(assignedVariable);
    //     final ValueAccessorSpecification value = ValueAccessorSpecification.ofVariable(variableReference.variableName().getText());
    //     this.builder.addVariableAssignment(variable, value);
    // }

    // private void buildVariableAssignment(String assignedVariable, LiteralContext literal) throws BuildException {
    //     if (assignedVariable == null) {
    //         throw new BuildException("No variable for assignement specified.");
    //     }
    //     final String type = this.analyzer.getVariableType(assignedVariable);
    //     final ValueMutatorSpecification variable = ValueMutatorSpecification.ofVariableName(assignedVariable);
    //     final ValueAccessorSpecification value = this.getLiteral(type, literal);
    //     this.builder.addVariableAssignment(variable, value);
    // }    
// }