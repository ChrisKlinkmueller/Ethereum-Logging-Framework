package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.parser.XbelParser.BlocksScopeContext;
import au.csiro.data61.aap.parser.XbelParser.ConfigurationBlockContext;
import au.csiro.data61.aap.parser.XbelParser.DocumentContext;
import au.csiro.data61.aap.parser.XbelParser.GlobalVariableStmtsContext;
import au.csiro.data61.aap.parser.XbelParser.ScopesContext;
import au.csiro.data61.aap.parser.XbelParser.ValueAssignmentContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionStmtContext;
import au.csiro.data61.aap.specification.AbstractInstruction;
import au.csiro.data61.aap.specification.Configuration;
import au.csiro.data61.aap.specification.Scope;
import au.csiro.data61.aap.specification.ScopeDefinition;
import au.csiro.data61.aap.specification.ScopeType;
import au.csiro.data61.aap.specification.Specification;
import au.csiro.data61.aap.specification.Statement;
import au.csiro.data61.aap.specification.ValueSource;
import au.csiro.data61.aap.specification.Variable;

/**
 * ParseTreeTransformer
 */
public class ParseTreeTransformer extends XbelBaseVisitor<Specification> {
    
    @Override
    public Specification visitDocument(DocumentContext ctx) {
        final ConfigurationBlockContext configCtx = ctx.configurationBlock();
        final Configuration config = this.transformConfigurationContext(configCtx);
        final Scope globalScope = this.transformToScope(ctx);
        return new Specification(config, globalScope);
    }

	private Configuration transformConfigurationContext(ConfigurationBlockContext ctx) {
        // TODO: proper parsing of configuration
        return new Configuration();
    }

    private Scope transformToScope(DocumentContext ctx) {
        final ScopeDefinition definition = new ScopeDefinition(ScopeType.GLOBAL_SCOPE);

        final GlobalVariableStmtsContext varCtx = ctx.globalVariableStmts();
        final ScopesContext scopesCtx = ctx.scopes();

        final int instructionCount = varCtx.variableDefinitionStmt().size() + scopesCtx.blocksScope().size();
        final AbstractInstruction[] instructions = new AbstractInstruction[instructionCount];

        int i = 0;
        for (VariableDefinitionStmtContext varDefCtx : varCtx.variableDefinitionStmt()) {
            instructions[i++] = this.transformToStatement(varDefCtx);
        }

        for (BlocksScopeContext blocksCtx : scopesCtx.blocksScope()) {
            instructions[i++] = this.transformToScope(blocksCtx);
        }

        final Scope globalScope = new Scope(definition, instructions);
        return globalScope;
    }

    private Statement transformToStatement(VariableDefinitionStmtContext ctx) {
        final String type = ctx.solType().getText();
        final String name = ctx.variableName().getText();
        final Variable variable = new Variable(type, name);



        //Statement stmt = Statement.createVariableAssignment(variable, valueSource);

        return null;
    }   
    
    private ValueSource transformToValueSource(ValueAssignmentContext ctx) {
        return null;
    }

    private AbstractInstruction transformToScope(BlocksScopeContext ctx) {
        return null;
    }
}