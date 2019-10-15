package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.specification.types.SolidityType;
import au.csiro.data61.aap.state.ProgramState;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionStartRuleContext;
import au.csiro.data61.aap.parser.XbelParser.VariableNameContext;
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
        if (name == null || name.trim().isEmpty()) {
            return SpecificationParserResult.ofError(ctx.start, "The variable name is empty.");
        }

        final SpecificationParserResult<SolidityType<?>> typeResult = VisitorRepository.getSolidityTypeVisitor().visitSolType(ctx.solType());
        if (!typeResult.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulParserResult(typeResult);
        }

        if (this.getState().exitsVariable(name)) {
            return SpecificationParserResult.ofError(ctx.start, String.format("A variable with the name '%s' already exists.", name));
        }

        final Variable variable = new Variable(typeResult.getResult(), name);
        this.getState().addVariable(variable);
        return SpecificationParserResult.ofResult(variable);
    }

    @Override
    public SpecificationParserResult<Variable> visitVariableName(VariableNameContext ctx) {
        final String name = ctx.getText();
        if (name == null || name.trim().isEmpty()) {
            return SpecificationParserResult.ofError(ctx.start, "The variable name is empty.");
        }

        final Variable variable = this.getState().getVariable(name);
        if (variable == null) {
            return SpecificationParserResult.ofError(ctx.start, String.format("The variable '%s' has not been defined.", name));
        }

        return SpecificationParserResult.ofResult(variable);
    }
}