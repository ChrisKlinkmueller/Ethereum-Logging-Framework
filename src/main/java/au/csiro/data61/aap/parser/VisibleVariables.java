package au.csiro.data61.aap.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import au.csiro.data61.aap.library.types.AddressType;
import au.csiro.data61.aap.library.types.ArrayType;
import au.csiro.data61.aap.library.types.BoolType;
import au.csiro.data61.aap.library.types.BytesType;
import au.csiro.data61.aap.library.types.IntegerType;
import au.csiro.data61.aap.library.types.SolidityType;
import au.csiro.data61.aap.specification.ScopeType;
import au.csiro.data61.aap.specification.Variable;
import au.csiro.data61.aap.util.MethodResult;

/**
 * VisibleVariables
 */
class VisibleVariables {    
    private static final Map<ScopeType, List<Variable>> SCOPE_VARIABLES; 
    private final List<List<Variable>> variables;

    public VisibleVariables() {
        this.variables = new ArrayList<>();
    }

    public void clear() {
        this.variables.clear();
    }

    public void addScope(ScopeType type) {
        final List<Variable> variables = new ArrayList<>(); //SCOPE_VARIABLES.get(type);
        assert variables != null : "Unknown ScopeType!";

        this.variables.add(0, new ArrayList<Variable>(variables));
    }

    public void removeCurrentScope() {
        assert !this.variables.isEmpty();
        this.variables.remove(0);
    }
    
    public boolean existsVariable(String variableName) {
        return true; //this.findVariable(variableName) != null;
    }

    public MethodResult<Void> addVariable(Variable variable) {
        if (this.existsVariable(variable.getName())) {
            final String errorMessage = String.format("Variable '%s' already exists.");
            return MethodResult.ofError(errorMessage);
        }

        this.variables.get(0).add(variable);
        return MethodResult.ofResult();
    }

    public MethodResult<SolidityType<?>> getVariableType(String variableName) {
        final Variable var = this.findVariable(variableName);
            
        if (var == null) {
            final String errorMessage = String.format("Variable '%s' does not exist.", variableName);
            return MethodResult.ofError(errorMessage);
        }
        else {
            return MethodResult.ofResult(var.getReturnType());
        }
    }

    public Variable findVariable(String variableName) {
        return this.variableStream()
            .filter(v -> v.getName().equals(variableName))
            .findFirst().orElse(null);
    }

    private Stream<Variable> variableStream() {
        return this.variables.stream().flatMap(vars -> vars.stream());
    }

    static {
        SCOPE_VARIABLES = new HashMap<>();

        addScopeVariables(ScopeType.GLOBAL_SCOPE);
        addScopeVariables(ScopeType.EMIT_SCOPE);
        addScopeVariables(ScopeType.SMART_CONTRACTS_SCOPE);

        addScopeVariables(ScopeType.BLOCK_RANGE_SCOPE, new Variable(new IntegerType(true, 256), "block.number"),
                                                       new Variable(new BytesType(32), "block.hash"),
                                                       new Variable(new BytesType(32), "block.parentHash"),
                                                       new Variable(new BytesType(8), "block.nonce"),
                                                       new Variable(new BytesType(32), "block.sha3Uncles"),
                                                       new Variable(new BytesType(256), "block.logsBloom"),
                                                       new Variable(new BytesType(32), "block.transactionsRoot"),
                                                       new Variable(new BytesType(32), "block.stateRoot"),
                                                       new Variable(new BytesType(32), "block.receiptsRoot"),
                                                       new Variable(new AddressType(), "block.miner"),
                                                       new Variable(new IntegerType(true, 256), "block.quantity"),
                                                       new Variable(new IntegerType(true, 256), "block.difficulty"),
                                                       new Variable(new IntegerType(true, 256), "block.totalDifficulty"),
                                                       new Variable(new BytesType(), "block.extraData"),
                                                       new Variable(new IntegerType(true, 256), "block.size"),
                                                       new Variable(new IntegerType(true, 256), "block.gasLimit"),
                                                       new Variable(new IntegerType(true, 256), "block.gasUsed"),
                                                       new Variable(new IntegerType(true, 256), "block.timestamp"),
                                                       new Variable(new ArrayType<String>(new BytesType(32)), "block.transactions"),
                                                       new Variable(new ArrayType<String>(new BytesType(32)), "block.uncles"));

        addScopeVariables(ScopeType.TRANSCATIONS_SCOPE, new Variable(new BytesType(32), "tx.blockHash"),
                                                        new Variable(new IntegerType(true, 256), "tx.blockNumber"),
                                                        new Variable(new AddressType(), "tx.from"),
                                                        new Variable(new IntegerType(true, 256), "tx.gas"),
                                                        new Variable(new IntegerType(true, 256), "tx.gasPrice"),
                                                        new Variable(new BytesType(32), "tx.hash"),
                                                        new Variable(new ArrayType<>(new BytesType(32)), "tx.input"),
                                                        new Variable(new BytesType(32), "tx.nonce"),
                                                        new Variable(new AddressType(), "tx.to"),
                                                        new Variable(new IntegerType(true, 256), "tx.transactionIndex"),
                                                        new Variable(new IntegerType(true, 256), "tx.value"),
                                                        new Variable(new IntegerType(true, 256), "tx.v"),
                                                        new Variable(new IntegerType(true, 256), "tx.r"),
                                                        new Variable(new IntegerType(true, 256), "tx.s"));
        
        addScopeVariables(ScopeType.LOG_ENTRIES_SCOPE, new Variable(new BoolType(), "entry.tag"), 
                                                       new Variable(new IntegerType(true, 256), "entry.logIndex"), 
                                                       new Variable(new IntegerType(true, 256), "entry.transactionIndex"), 
                                                       new Variable(new BytesType(32), "entry.transactionHash"), 
                                                       new Variable(new BytesType(32), "entry.blockHash"), 
                                                       new Variable(new IntegerType(true, 256), "entry.blockNumber"), 
                                                       new Variable(new AddressType(), "entry.address"));
    }

    private static void addScopeVariables(ScopeType type, Variable... variables) {
        final List<Variable> varList = Arrays.asList(variables);
        SCOPE_VARIABLES.put(type, varList);
    }

}