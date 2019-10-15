package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.specification.types.SolidityType;
import au.csiro.data61.aap.state.ProgramState;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionStartRuleContext;
import au.csiro.data61.aap.specification.Variable;

/**
 * VariableDefinitionVisitor
 */
class VariableDefinitionVisitor extends StatefulVisitor<SpecificationParserResult<Variable>> {
    
    public VariableDefinitionVisitor(ProgramState state) {
        super(state);
    }

    @Override
    public SpecificationParserResult<Variable> visitVariableDefinitionStartRule(VariableDefinitionStartRuleContext ctx) {
        return this.visitVariableDefinition(ctx.variableDefinition());
    }

    @Override
    public SpecificationParserResult<Variable> visitVariableDefinition(VariableDefinitionContext ctx) {
        final String name = ctx.variableName().getText();        
        if (name == null) {
            return SpecificationParserResult.ofError(ctx.start, "The variable definition doesn't contain a name.");
        }

        final SpecificationParserResult<SolidityType<?>> typeResult = VisitorRepository.getSolidityTypeVisitor().visitSolType(ctx.solType());
        if (!typeResult.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulParserResult(typeResult);
        }

        final Variable variable = new Variable(typeResult.getResult(), name);
        return SpecificationParserResult.ofResult(variable);
    }
}