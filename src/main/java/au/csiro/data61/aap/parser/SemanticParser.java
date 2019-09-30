package au.csiro.data61.aap.parser;

import static au.csiro.data61.aap.specification.ScopeType.BLOCK_RANGE_SCOPE;
import static au.csiro.data61.aap.specification.ScopeType.EMIT_SCOPE;
import static au.csiro.data61.aap.specification.ScopeType.GLOBAL_SCOPE;
import static au.csiro.data61.aap.specification.ScopeType.LOG_ENTRIES_SCOPE;
import static au.csiro.data61.aap.specification.ScopeType.SMART_CONTRACTS_SCOPE;
import static au.csiro.data61.aap.specification.ScopeType.TRANSCATIONS_SCOPE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.library.types.AddressType;
import au.csiro.data61.aap.library.types.ArrayType;
import au.csiro.data61.aap.library.types.BoolType;
import au.csiro.data61.aap.library.types.BytesType;
import au.csiro.data61.aap.library.types.FixedType;
import au.csiro.data61.aap.library.types.IntegerType;
import au.csiro.data61.aap.library.types.SolidityType;
import au.csiro.data61.aap.library.types.StringType;
import au.csiro.data61.aap.parser.XbelParser.BlockHeadContext;
import au.csiro.data61.aap.parser.XbelParser.BlockRangeHeadContext;
import au.csiro.data61.aap.parser.XbelParser.BlockRangeNumberContext;
import au.csiro.data61.aap.parser.XbelParser.LeftStatementSideContext;
import au.csiro.data61.aap.parser.XbelParser.MethodCallContext;
import au.csiro.data61.aap.parser.XbelParser.SolBytesTypeContext;
import au.csiro.data61.aap.parser.XbelParser.SolFixedTypesContext;
import au.csiro.data61.aap.parser.XbelParser.SolIntTypesContext;
import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;
import au.csiro.data61.aap.parser.XbelParser.StatementContext;
import au.csiro.data61.aap.specification.ScopeType;
import au.csiro.data61.aap.specification.Variable;
import au.csiro.data61.aap.util.CollectionUtil;

/**
 * ParseTreeTransformer
 */
public class SemanticParser extends XbelBaseListener {
    private static final Logger LOG = Logger.getLogger(SemanticParser.class.getName());
    private static final Map<ScopeType, Set<ScopeType>> SCOPE_NESTING_DICT;

    static {
        SCOPE_NESTING_DICT = new HashMap<>();
        addScopeNesting(GLOBAL_SCOPE, BLOCK_RANGE_SCOPE, TRANSCATIONS_SCOPE, SMART_CONTRACTS_SCOPE, EMIT_SCOPE);
        addScopeNesting(TRANSCATIONS_SCOPE, LOG_ENTRIES_SCOPE, EMIT_SCOPE);
        addScopeNesting(SMART_CONTRACTS_SCOPE, LOG_ENTRIES_SCOPE, EMIT_SCOPE);
        addScopeNesting(LOG_ENTRIES_SCOPE, EMIT_SCOPE);
        addScopeNesting(EMIT_SCOPE);
    }

    private static void addScopeNesting(ScopeType type, ScopeType... nestedTypes) {
        final Set<ScopeType> nestedTypesSet = CollectionUtil.arrayToSet(nestedTypes);
        SCOPE_NESTING_DICT.put(type, nestedTypesSet);
    }

    private final List<SpecificationParserError> errors;
    private final VisibleVariables variables;

    public SemanticParser() {
        this.errors = new ArrayList<>();
        this.variables = new VisibleVariables();
    }

    public void reset() {
        this.errors.clear();
        this.variables.clear();
    }

    public boolean isSuccessful() {
        return this.errors.isEmpty();
    }

    public Collection<SpecificationParserError> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    @Override
    public void enterBlockHead(BlockHeadContext ctx) {
        this.variables.addScope(ScopeType.EMIT_SCOPE);
    }

    @Override
    public void exitBlockHead(BlockHeadContext ctx) {
        this.variables.removeCurrentScope();
    }

    @Override
    public void enterStatement(StatementContext ctx) {
        final Variable variable = this.verifyLeftStatementSide(ctx.leftStatementSide());
        System.out.println(variable);
    }

    private Variable verifyLeftStatementSide(LeftStatementSideContext ctx) {
        if (ctx == null) {
            return null;
        }

        final String variableName = ctx.variableName().getText();
        final Variable variable = this.variables.findVariable(variableName);
        if (ctx.solType() == null) { // variable is reused
            if (variable == null) {
                final String errorMessage = String.format("Variable '%s' does not exist.", variableName);
                this.addError(errorMessage, ctx.variableName().getStart());
            }
            return variable;
        }
        else { // variable is defined
            if (variable != null) {
                final String errorMessage = String.format("Variable '%s' does not exist.", variableName);
                this.addError(errorMessage, ctx.variableName().getStart());
                return null;
            }
            final SolidityType<?> type = this.verifySolidityType(ctx.solType());
            return new Variable(type, variableName);
        }
    }

    private void verifyBlockRangeScopeDefinition(BlockRangeHeadContext ctx) {
        this.verifyBlockRangeNumber(ctx.from, true);
        this.verifyBlockRangeNumber(ctx.to, false);
    }

    private boolean verifyBlockRangeNumber(BlockRangeNumberContext ctx, boolean isFrom) {
        if (isFrom && ctx.KEY_PENDING() != null) {
            final String errorMessage = String.format("'%s' is not a valid value for from.", ctx.KEY_PENDING().getText());
            this.addError(errorMessage, ctx.KEY_PENDING().getSymbol());
            return false;
        }

        if (!isFrom && ctx.KEY_EARLIEST() != null) {
            final String errorMessage = String.format("'%s' is not a valid value for to.", ctx.KEY_EARLIEST().getText());
            this.addError(errorMessage, ctx.KEY_EARLIEST().getSymbol());
            return false;
        }

        if (ctx.methodCall() != null) {
            if (this.verifyMethodCall(ctx.methodCall())) {
                // check if return type == int
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean verifyMethodCall(MethodCallContext ctx) {
        final String methodName = ctx.methodName.getText();
        System.out.println(methodName);

        final String message = "";
        this.addError(message, ctx.methodName);

        return true;
    }

    private void addError(String errorMessage, Token token) {
        final int line = token.getLine();
        final int column = token.getStartIndex();
        final SpecificationParserError error = new SpecificationParserError(line, column, errorMessage);
        this.errors.add(error);
    }

    public SolidityType<?> verifySolidityType(SolTypeContext ctx) {
        if (ctx.SOL_ADDRESS_ARRAY_TYPE() != null) {
            return new ArrayType<String>(new AddressType()); 
        } 
        else if (ctx.SOL_ADDRESS_TYPE() != null) {
            return new AddressType();
        } 
        else if (ctx.SOL_BOOL_ARRAY_TYPE() != null) {
            return new ArrayType<Boolean>(new BoolType()); 
        } 
        else if (ctx.SOL_BOOL_TYPE() != null) {
            return new BoolType();
        } 
        else if (ctx.solBytesArrayTypes() != null) {
            return new BytesType();
        }
        else if (ctx.solBytesType() != null) {
            return this.getBytesType(ctx.solBytesType());
        }
        else if (ctx.solFixedArrayTypes() != null) {
            final SolidityType<BigDecimal> fixedType = this.getFixedType(ctx.solFixedArrayTypes().solFixedTypes());
            return new ArrayType<BigDecimal>(fixedType);
        }
        else if (ctx.solFixedTypes() != null) {
            return this.getFixedType(ctx.solFixedTypes());
        }
        else if (ctx.solIntArrayTypes() != null) {
            final SolidityType<BigInteger> intType = this.getIntTypes(ctx.solIntArrayTypes().solIntTypes());
            return new ArrayType<BigInteger>(intType);
        } 
        else if (ctx.solIntTypes() != null) {
            return this.getIntTypes(ctx.solIntTypes());
        }
        else if (ctx.SOL_STRING_TYPE() != null) {
            return new StringType();
        }
        else {
            return this.typeKeywordUnknown(ctx.getText(), null);
        }
    }

    private static final int DEFAULT_FIXED_M = 128;
    private static final int DEFAULT_FIXED_N = 18;
    private SolidityType<BigDecimal> getFixedType(SolFixedTypesContext ctx) {
        final boolean signed = ctx.SOL_UNSIGNED() == null;
        if (ctx.SOL_FIXED_N() != null && ctx.SOL_NUMBER_LENGTH() != null) {
            final int m = Integer.parseInt(ctx.SOL_NUMBER_LENGTH().getText());
            final int n = Integer.parseInt(ctx.SOL_FIXED_N().getText());
            return new FixedType(signed, m, n);
        }
        else {
            return new FixedType(signed, DEFAULT_FIXED_M, DEFAULT_FIXED_N);
        }
    }

    private static final int DEFAULT_INT_LENGTH = 256;
    private SolidityType<BigInteger> getIntTypes(SolIntTypesContext ctx) {
        final boolean signed = ctx.SOL_UNSIGNED() == null;
        
        if (ctx.SOL_NUMBER_LENGTH() == null) {
            return new IntegerType(signed, DEFAULT_INT_LENGTH);
        }
        else {
            final int length = Integer.parseInt(ctx.SOL_NUMBER_LENGTH().getText());
            return new IntegerType(signed, length);
        }
    }

    private static final int DEFAULT_BYTE_LENGTH = 1;
    private SolidityType<String> getBytesType(SolBytesTypeContext ctx) {
        if (ctx.SOL_BYTES_SUFFIX() == null) {
            return new BytesType(DEFAULT_BYTE_LENGTH);
        }
        else {
            final int length = Integer.parseInt(ctx.SOL_BYTES_LENGTH().getText());
            return new BytesType(length);
        }
    }

    private SolidityType<?> typeKeywordUnknown(String keyword, Exception ex) {
        final String message = String.format("Keyword '%s' is not a valid solidity type.", keyword);
        assert false : message;

        if (ex == null) {
            LOG.log(Level.SEVERE, message);
        }
        else {
            LOG.log(Level.SEVERE, message, ex);
        }

        return null;
    }
    
    // CodeInstructionTree - a class member that stores the different blocks and their statements    
    // method dictionary - a list that contains all defined methods identified by their signature and the scope in which they can be used
        // methods might also require that certain variables exist or certain methods were called before
    // constants dictionary - a dictionary containing the default constants that are visiable in a certain scope

    
    /* 
    verification rules for a block
        block nesting : 
            global { 
                blocks { transactions { log entries { emits } } }  
                transactions { log entries { emits } } 
                smart contracts { log entries { emits } }
                emits
            }
        block header verification (is parameter valid incl. does method return type match, does variable type match)
        left hand side : is variable defined already?
        righthandside: 
            does variable type match
            is methodcall valid
            event/trace calls only in emit block
            is method / variable visible or existing
        configuration assignments only at beginning
    */
    
}