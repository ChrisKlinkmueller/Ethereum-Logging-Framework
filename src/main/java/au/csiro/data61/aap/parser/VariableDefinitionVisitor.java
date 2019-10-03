package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;
import au.csiro.data61.aap.specification.Variable;

/**
 * VariableDefinitionVisitor
 */
class VariableDefinitionVisitor extends XbelBaseVisitor<SpecificationParserResult<Variable>> {
    
    @Override
    public SpecificationParserResult<Variable> visitVariableDefinition(VariableDefinitionContext ctx) {
        final String name = ctx.variableName().getText();        
        if (name == null) {
            return SpecificationParserResult.ofError(ctx.start, "The variable definition doesn't contain a name.");
        }

        final SpecificationParserResult<String> typeResult = VisitorRepository.getSolidityTypeVisitor().visitSolType(ctx.solType());
        if (!typeResult.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulParserResult(typeResult);
        }

        System.out.println(typeResult.getResult());
        final Variable variable = new Variable(typeResult.getResult(), name);
        return SpecificationParserResult.ofResult(variable);
    }
}