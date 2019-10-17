package au.csiro.data61.aap.parser;

import java.io.InputStream;
import java.util.function.Function;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.specification.types.SolidityType;
import au.csiro.data61.aap.specification.Block;
import au.csiro.data61.aap.specification.Constant;
import au.csiro.data61.aap.specification.Variable;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Parser
 */
public class SpecificationParser {
    private static final Logger LOG = Logger.getLogger(SpecificationParser.class.getName());

    public SpecificationParserResult<Variable> parseVariableDefinition(InputStream is) {
        return this.parseAndClean(is, XbelParser::variableDefinitionStartRule, VisitorRepository.getVariableDefinitionVisitor());
    }    

    public SpecificationParserResult<SolidityType<?>> parseSolidityType(InputStream is) {
        return this.parseAndClean(is, XbelParser::solTypeStartRule, VisitorRepository.getSolidityTypeVisitor());
    }

    public SpecificationParserResult<Block> parseBlock(InputStream is) {
        return this.parseAndClean(is, XbelParser::blockStartRule, VisitorRepository.getBlockVisitor());
    }

    public SpecificationParserResult<Constant> parseConstant(InputStream is) {
        return this.parseAndClean(is, XbelParser::staticValueStartRule, VisitorRepository.getConstantVisitor());
    }

    private <T> SpecificationParserResult<T> parseAndClean(InputStream is, Function<XbelParser, ParseTree> rule, XbelBaseVisitor<SpecificationParserResult<T>> visitor) {
        final SpecificationParserResult<T> result = this.parse(is, rule, visitor);
        VisitorRepository.clearProgramState();
        return result;
    }
    
    private <T> SpecificationParserResult<T> parse(InputStream is, Function<XbelParser, ParseTree> rule, XbelBaseVisitor<SpecificationParserResult<T>> visitor) {
        if (is == null) {
            LOG.severe("The 'is' parameter was null.");
            return SpecificationParserResult.ofError("The 'is' parameter was null.");
        }

        final MethodResult<CharStream> charStreamResult = SpecificationParserUtil.charStreamfromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            LOG.severe("Creation of CharStream failed.");
            return SpecificationParserResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final AntlrErrorReporter errorReporter = new AntlrErrorReporter();
        final XbelLexer lexer = new XbelLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorReporter);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);        
        final XbelParser syntacticParser = new XbelParser(tokens);
        syntacticParser.removeErrorListeners();
        syntacticParser.addErrorListener(errorReporter);

        final ParseTree tree = rule.apply(syntacticParser);      
        logParseTree(tree);       
        
        if (errorReporter.hasErrors()) {
            LOG.severe("Errors during syntactic parsing.");
            return SpecificationParserResult.ofErrorReporter(errorReporter);
        }

        return visitor.visit(tree);
    }

    private void logParseTree(ParseTree tree) {
        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new PrintParseTreeListener(), tree);
    }

    private static class PrintParseTreeListener extends XbelBaseListener {

        @Override
        public void visitErrorNode(ErrorNode node) {
            LOG.info(node.getText().replaceAll("\\s+", " "));
        }

        @Override
        public void visitTerminal(TerminalNode node) {
            LOG.info(node.getText().replaceAll("\\s+", " "));
        }
    }


}