package au.csiro.data61.aap.elf.parsing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;

import au.csiro.data61.aap.elf.core.writers.XesWriter;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementCsvContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesEventContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesTraceContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.XesEmitVariableContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * EmitAnalyzer
 */
public class EmitAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceAnalyzer varAnalyzer;

    public EmitAnalyzer(final ErrorCollector errorCollector, final VariableExistenceAnalyzer varAnalyzer) {
        super(errorCollector);
        assert varAnalyzer != null;
        this.varAnalyzer = varAnalyzer;
    }

    @Override
    public void clear() {}

    @Override
    public void exitEmitStatementCsv(final EmitStatementCsvContext ctx) {
        this.verifyTableName(ctx.valueExpression());

        this.verifyUniquenessOfNames(
            ctx.namedEmitVariable(),
            varCtx -> varCtx.variableName() != null ? varCtx.variableName() : varCtx.valueExpression().variableName()
        );
    }

    private void verifyTableName(ValueExpressionContext valueExpression) {
        String type = InterpreterUtils.determineType(valueExpression, this.varAnalyzer);
        if (type != null && !TypeUtils.isStringType(type)) {
            this.addError(valueExpression.start, "CSV table name must be a string.");
        }
    }

    @Override
    public void exitEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.verifyUniquenessOfNames(ctx.xesEmitVariable(), EmitAnalyzer::getXesVariableName);
        this.verifyXesTypeCompatibility(ctx.xesEmitVariable());
        this.verifyXesEventExtensions(ctx);
        this.verifyXesId(ctx.pid);
        this.verifyXesId(ctx.piid);
        this.verifyXesId(ctx.eid);
    }

    @Override
    public void exitEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.verifyUniquenessOfNames(ctx.xesEmitVariable(), EmitAnalyzer::getXesVariableName);
        this.verifyXesTypeCompatibility(ctx.xesEmitVariable());
        this.verifyXesId(ctx.pid);
        this.verifyXesId(ctx.piid);
    }

    private void verifyXesId(ValueExpressionContext ctx) {
        if (ctx != null) {
            final String type = InterpreterUtils.determineType(ctx, this.varAnalyzer);
            if (type != null && !XesWriter.areTypesCompatible(type, XesWriter.STRING_TYPE)) {
                this.addError(ctx.start, "An XES id must be a string value.");
            }
        }
    }

    private <T extends ParserRuleContext> void verifyUniquenessOfNames(
        final List<T> variables,
        final Function<T, VariableNameContext> nameAccessor
    ) {
        final Set<String> names = new HashSet<>();
        for (final T varCtx : variables) {
            final VariableNameContext nameCtx = nameAccessor.apply(varCtx);
            if (nameCtx == null) {
                this.addError(varCtx.start, "Attribute name must be specified for literals");
            }

            if (nameCtx != null) {
                this.verifyUniquenessOfName(nameCtx, names);
                names.add(nameCtx.getText());
            }
        }
    }

    private void verifyUniquenessOfName(final VariableNameContext ctx, final Set<String> varNames) {
        if (varNames.contains(ctx.getText())) {
            this.addError(ctx.start, String.format("Column name already specified."));
        }
    }

    private void verifyXesEventExtensions(EmitStatementXesEventContext eventCtx) {
        boolean containsConceptName = false;
        boolean containsTimestamp = false;
        boolean containsLifecycleTransition = false;
        boolean containsResource = false;

        for (XesEmitVariableContext variable : eventCtx.xesEmitVariable()) {
            final VariableNameContext nameCtx = getXesVariableName(variable);
            if (nameCtx == null) {
                continue;
            }

            final String name = nameCtx.getText();
            if (!name.contains(":")) { // name does not contain a prefix for an XES extension
                continue;
            }

            switch (name) {
                case "concept:name":
                    containsConceptName = true;
                    break;
                case "lifecycle:transition":
                    containsLifecycleTransition = true;
                    break;
                case "time:timestamp":
                    containsTimestamp = true;
                    break;
                case "org:resource":
                    containsResource = true;
                    break;
            }

            final String[] nameParts = name.split(":");
            final XExtension extension = XExtensionManager.instance().getByPrefix(nameParts[0]);
            if (extension == null) {
                final String warning = String.format("XES extension '%s' unknown.", nameParts[0]);
                this.errorCollector.addWarning(variable.start, warning);
                continue;
            }

            final XAttribute attribute = getXesExtensionAttribute(extension, name);
            if (attribute == null) {
                final String error = String.format(
                    "XES extension '%s' does not specify an event attribute '%s'.",
                    nameParts[0],
                    nameParts[1]
                );
                this.errorCollector.addSemanticError(variable.start, error);
                continue;
            }

            final Class<? extends XAttribute> cl = this.determineXesType(variable);
            if (cl == null) {
                continue;
            }

            if (!cl.isAssignableFrom(attribute.getClass())) {
                final String error = String.format(
                    "XES extension attribute '%s:%s' must be of type '%s', but was of type '%s'.",
                    nameParts[0],
                    nameParts[1],
                    getXesTypeName(attribute.getClass()),
                    getXesTypeName(cl)
                );
                this.errorCollector.addSemanticError(variable.start, error);
            }
        }

        if (!containsConceptName) {
            final String error = String.format("The XES event does not contain an attribute 'concept:name'.");
            this.errorCollector.addSemanticError(eventCtx.start, error);
        }

        if (!containsTimestamp) {
            final String warning = String.format(
                "The XES event does not contain an attribute 'time:timestamp'. "
                    + "If other events contain this attribute, a global default value is set "
                    + "that by default applies to all events without such this attribute."
            );
            this.errorCollector.addWarning(eventCtx.start, warning);
        }

        if (!containsLifecycleTransition) {
            final String warning = String.format(
                "The XES event does not contain an attribute 'lifecycle:transition'. "
                    + "If other events contain this attribute, a global default value is set "
                    + "that by default applies to all events without such this attribute."
            );
            this.errorCollector.addWarning(eventCtx.start, warning);
        }

        if (!containsResource) {
            final String warning = String.format(
                "The XES event does not contain an attribute 'org:resource'. "
                    + "If other events contain this attribute, a global default value is set "
                    + "that by default applies to all events without such this attribute."
            );
            this.errorCollector.addWarning(eventCtx.start, warning);
        }
    }

    private void verifyXesTypeCompatibility(List<XesEmitVariableContext> variables) {
        variables.forEach(ctx -> this.verifyXesTypeCompatibility(ctx));
    }

    private void verifyXesTypeCompatibility(XesEmitVariableContext ctx) {
        if (ctx.xesTypes() != null) {
            final String solType = InterpreterUtils.determineType(ctx.valueExpression(), varAnalyzer);
            if (solType == null) {
                return;
            }

            final String xesType = ctx.xesTypes().getText();
            if (!XesWriter.areTypesCompatible(solType, xesType)) {
                this.addError(
                    ctx.valueExpression().start,
                    String.format("Cannot export solidity type '%s' as xes type '%s'.", solType, xesType)
                );
            }
        }
    }

    private XAttribute getXesExtensionAttribute(XExtension extension, String attributeName) {
        return extension.getEventAttributes().stream().filter(attr -> attr.getKey().equals(attributeName)).findFirst().orElse(null);
    }

    private Class<? extends XAttribute> determineXesType(XesEmitVariableContext varCtx) {
        if (varCtx.xesTypes() != null) {
            switch (varCtx.xesTypes().getText()) {
                case "xs:string":
                    return XAttributeLiteral.class;
                case "xs:date":
                    return XAttributeTimestamp.class;
                case "xs:int":
                    return XAttributeDiscrete.class;
                case "xs:float":
                    return XAttributeContinuous.class;
                case "xs:boolean":
                    return XAttributeBoolean.class;
                default:
                    return null;
            }
        } else {
            final String solType = InterpreterUtils.determineType(varCtx.valueExpression(), varAnalyzer);
            switch (solType) {
                case TypeUtils.STRING_TYPE_KEYWORD:
                    return XAttributeLiteral.class;
                case TypeUtils.INT_TYPE_KEYWORD:
                    return XAttributeDiscrete.class;
                case TypeUtils.ADDRESS_TYPE_KEYWORD:
                    return XAttributeLiteral.class;
                case TypeUtils.BOOL_TYPE_KEYWORD:
                    return XAttributeBoolean.class;
                case TypeUtils.BYTES_TYPE_KEYWORD:
                    return XAttributeLiteral.class;
                default:
                    return null;
            }
        }
    }

    private String getXesTypeName(Class<? extends XAttribute> cl) {
        if (XAttributeBoolean.class.isAssignableFrom(cl)) {
            return "xs:boolean";
        } else if (XAttributeContinuous.class.isAssignableFrom(cl)) {
            return "xs:float";
        } else if (XAttributeDiscrete.class.isAssignableFrom(cl)) {
            return "xs:int";
        } else if (XAttributeTimestamp.class.isAssignableFrom(cl)) {
            return "xs:date";
        } else if (XAttributeLiteral.class.isAssignableFrom(cl)) {
            return "xs:string";
        } else {
            final String message = String.format("Unknown xes type class: %s", cl.getName());
            assert false : message;
            throw new IllegalArgumentException(message);
        }
    }

    private static VariableNameContext getXesVariableName(XesEmitVariableContext varCtx) {
        return varCtx.variableName() != null ? varCtx.variableName() : varCtx.valueExpression().variableName();
    }
}
