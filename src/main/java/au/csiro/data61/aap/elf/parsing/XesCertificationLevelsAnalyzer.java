package au.csiro.data61.aap.elf.parsing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;

import au.csiro.data61.aap.elf.core.writers.XesWriter;
import au.csiro.data61.aap.elf.library.Library;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.DocumentContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesEventContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodStatementContext;

public class XesCertificationLevelsAnalyzer extends SemanticAnalyzer {
    private static final String CONCEPT_NAME = "concept:name";
    private static final String TIME_TIMESTAMP = "time:timestamp";
    private static final String ORG_RESOURCE = "org:resource";
    private static final String LIFECYLE_TRANSITION = "lifecycle:transition";

    private static final String GLOBAL_ATTR_WARNING_TEMPLATE =
        "XES compliance problem: The XES standard extension attribute '%s' must be specified as a "
            + "global event attribute, in order to comply with XES certification level %s. "
            + "As this was not done explicitly in the script, a global event attribute definition with a default value will be added automatically during extraction.";

    private static class AttributeName {
        protected final String prefix;
        protected final String name;

        private AttributeName(String fullName) {
            if (fullName.contains(":")) {
                final String[] parts = fullName.split(":");
                this.prefix = parts[0];
                this.name = parts[1];
            } else {
                this.prefix = null;
                this.name = fullName;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AttributeName)) {
                return false;
            }

            final AttributeName name = (AttributeName) o;
            if (this.prefix == null ? name.prefix != null : !this.prefix.equals(name.prefix)) {
                return false;
            }

            return this.name.equals(name.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.prefix, this.name, this.getClass());
        }

        @Override
        public String toString() {
            return this.prefix == null ? this.name : String.format("%s:%s", this.prefix, this.name);
        }
    }

    private static class EventDef {
        protected final Token startToken;
        protected final List<AttributeDef> attributes;

        private EventDef(Token startToken) {
            this.startToken = startToken;
            this.attributes = new LinkedList<>();
        }

        @Override
        public String toString() {
            return String.format(
                "Ln %s, Col %s: Event with attributes: %s",
                this.startToken.getLine(),
                this.startToken.getCharPositionInLine(),
                this.attributes.stream().map(AttributeDef::toString).collect(Collectors.joining(", "))
            );
        }
    }

    private static class AttributeDef {
        protected final Token startToken;
        protected final String type;
        protected final AttributeName name;
        protected final boolean isGlobal;

        private AttributeDef(Token startToken, String type, String fullName) {
            this(startToken, type, fullName, false);
        }

        private AttributeDef(Token startToken, String type, String fullName, boolean isGlobal) {
            this.isGlobal = isGlobal;
            this.startToken = startToken;
            this.type = type;
            this.name = new AttributeName(fullName);
        }

        @Override
        public String toString() {
            return String.format("%s %s", this.type, this.name);
        }
    }

    private static class GlobalValueDef extends AttributeDef {
        private final LiteralContext value;

        private GlobalValueDef(Token startToken, String fullName, String type, LiteralContext value) {
            super(startToken, type.substring(1, type.length() - 1), fullName.substring(1, fullName.length() - 1), true);
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format(
                "Ln %s, Col %s: Global attribute: %s %s = %s",
                this.startToken.getLine(),
                this.startToken.getCharPositionInLine(),
                this.type,
                this.name,
                this.value
            );
        }
    }

    private static class ClassifierDef {
        private final Token startToken;
        private final String name;
        private final List<AttributeName> attributes;

        private ClassifierDef(Token startToken, String name, String attributes) {
            this.startToken = startToken;
            this.name = name.substring(1, name.length() - 1);

            this.attributes = new LinkedList<>();
            for (String rawAttribute : attributes.replaceAll("[{|}]", "").trim().split(",")) {
                final String attribute = rawAttribute.trim();
                this.attributes.add(new AttributeName(attribute.substring(1, attribute.length() - 1)));
            }
        }

        @Override
        public String toString() {
            return String.format(
                "Ln %s, Col %s: Classifier '%s' with attributes: %s",
                this.startToken.getLine(),
                this.startToken.getCharPositionInLine(),
                this.name,
                this.attributes.stream().map(AttributeName::toString).collect(Collectors.joining(", "))
            );
        }
    }

    private final AtomicBoolean isPreambleFinished;
    private final AtomicBoolean hasErrors;
    private final List<GlobalValueDef> globalValueDefs;
    private final List<ClassifierDef> classifierDefs;
    private final List<EventDef> eventDefs;
    private final AtomicReference<Token> documentStartToken;
    private final List<String> certLevels;

    public XesCertificationLevelsAnalyzer(EventCollector errorCollector) {
        super(errorCollector);
        this.isPreambleFinished = new AtomicBoolean(false);
        this.globalValueDefs = new LinkedList<>();
        this.classifierDefs = new LinkedList<>();
        this.eventDefs = new LinkedList<>();
        this.documentStartToken = new AtomicReference<>();
        this.certLevels = new LinkedList<>();
        this.hasErrors = new AtomicBoolean(false);
    }

    @Override
    public void clear() {}

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.documentStartToken.set(ctx.start);
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.isPreambleFinished.set(true);
    }

    @Override
    public void enterMethodStatement(MethodStatementContext ctx) {
        if (this.isPreambleFinished.get()) {
            return;
        }

        final String methodName = ctx.methodInvocation().methodName.getText();
        switch (methodName) {
            case Library.ADD_XES_CLASSIFIER:
                this.collectClassifier(ctx);
                break;
            case Library.ADD_XES_GLOBAL_EVENT_ATTRIBUTE:
                this.collectGlobalEventAttribute(ctx);
                break;
        }
    }

    private void collectClassifier(MethodStatementContext ctx) {
        if (ctx.methodInvocation().valueExpression().size() != 2) {
            return;
        }

        if (!this.areParametersLiterals(ctx, Library.ADD_XES_CLASSIFIER)) {
            return;
        }

        final ClassifierDef def = new ClassifierDef(
            ctx.start,
            ctx.methodInvocation().valueExpression(0).literal().getText(),
            ctx.methodInvocation().valueExpression(1).literal().getText()
        );

        if (def.attributes.isEmpty()) {
            this.errorCollector.addSemanticError(ctx.start, "An XES event classifier must contain at least one attribute.");
            return;
        }

        this.classifierDefs.add(def);
    }

    private void collectGlobalEventAttribute(MethodStatementContext ctx) {
        if (ctx.methodInvocation().valueExpression().size() != 3) {
            return;
        }

        if (!this.areParametersLiterals(ctx, Library.ADD_XES_GLOBAL_EVENT_ATTRIBUTE)) {
            return;
        }

        final GlobalValueDef def = new GlobalValueDef(
            ctx.start,
            ctx.methodInvocation().valueExpression(0).literal().getText(),
            ctx.methodInvocation().valueExpression(1).literal().getText(),
            ctx.methodInvocation().valueExpression(2).literal()
        );
        this.globalValueDefs.add(def);
    }

    private boolean areParametersLiterals(MethodStatementContext ctx, String methodName) {
        final boolean areParamsLiterals = ctx.methodInvocation().valueExpression().stream().allMatch(v -> v.literal() != null);

        if (!areParamsLiterals) {
            final String errorMessage = String.format(
                "The method '%s' configures the XES emission process and "
                    + "for validation purposes can only take literals as parameters.",
                methodName
            );
            this.errorCollector.addSemanticError(ctx.start, errorMessage);
        }

        return areParamsLiterals;
    }

    @Override
    public void enterEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        final EventDef def = new EventDef(ctx.start);
        ctx.xesEmitVariable()
            .stream()
            .map(varCtx -> new AttributeDef(varCtx.variableName().start, varCtx.xesTypes().getText(), varCtx.variableName().getText()))
            .forEach(def.attributes::add);
        this.eventDefs.add(def);
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        final Map<String, AttributeDef> attributes = this.verifyGlobalAttributes();
        this.verifyClassifiers(attributes);

        if (this.eventDefs.isEmpty()) {
            return;
        }

        this.verifyEventAttributes(attributes);
        this.verifyCertLevelA(attributes);
        this.verifyCertLevelB(attributes);
        this.verifyCertLevelC(attributes);

        if (!this.hasErrors.get()) {
            // TODO: add certification levels
        }

        this.cleanUp();
    }

    private void cleanUp() {
        this.isPreambleFinished.set(false);
        this.globalValueDefs.clear();
        this.classifierDefs.clear();
        this.eventDefs.clear();
        this.documentStartToken.set(null);
        this.hasErrors.set(false);
        this.certLevels.clear();
    }

    private Map<String, AttributeDef> verifyGlobalAttributes() {
        final Map<String, AttributeDef> attributes = new HashMap<>();

        for (GlobalValueDef globalValue : this.globalValueDefs) {
            final String attrName = globalValue.name.toString();
            final AttributeDef existingAttr = attributes.get(attrName);
            if (existingAttr != null) {
                final String error = String.format(
                    "XES compliance problem: A global value for the XES event attribute '%s' was already specified at Ln %s, Col %s.",
                    attrName,
                    existingAttr.startToken.getLine(),
                    existingAttr.startToken.getCharPositionInLine()
                );
                this.errorCollector.addSemanticError(globalValue.startToken, error);
                continue;
            }

            if (!this.satisfiesExtension(globalValue)) {
                continue;
            }

            if (!this.isValueTypeCompatible(globalValue.type, globalValue.value)) {
                final String errorMessage = String.format(
                    "XES compliance problem: The value type and the specified type for the XES attribute are not compatible."
                );
                this.errorCollector.addSemanticError(globalValue.startToken, errorMessage);
            }

            final AttributeDef attr = new AttributeDef(globalValue.startToken, globalValue.type, attrName, true);
            attributes.put(attrName, attr);
        }

        return attributes;
    }

    private void verifyEventAttributes(Map<String, AttributeDef> attributes) {
        for (EventDef event : this.eventDefs) {
            for (AttributeDef attr : event.attributes) {
                final String attrName = attr.name.toString();
                if (attr.name.prefix == null) {
                    if (attributes.containsKey(attrName)) {
                        final AttributeDef definedAttribute = attributes.get(attrName);
                        if (!definedAttribute.type.equals(attr.type)) {
                            final String error = String.format(
                                "XES compliance problem: The attribute '%s' was already used with a different type in the XES event emission at Ln %s, Col %s.",
                                attrName,
                                definedAttribute.startToken.getLine(),
                                definedAttribute.startToken.getCharPositionInLine()
                            );
                            this.errorCollector.addSemanticError(attr.startToken, error);
                        }
                    }
                } else if (!this.satisfiesExtension(attr)) {
                    continue;
                }

                if (!attributes.containsKey(attrName)) {
                    attributes.put(attrName, attr);
                }
            }
        }
    }

    private void verifyClassifiers(Map<String, AttributeDef> attributes) {
        for (ClassifierDef def : this.classifierDefs) {
            for (AttributeName attrName : def.attributes) {
                if (this.isRequiredAsGlobal(attrName, attributes)) {
                    final String error = String.format(
                        "XES compliance problem: The attribute '%s' is part of an XES event classifier, but is not defined as a global event attribute.",
                        attrName
                    );
                    this.errorCollector.addSemanticError(def.startToken, error);
                }
            }
        }
    }

    private void verifyCertLevelA(Map<String, AttributeDef> attributes) {
        for (EventDef event : this.eventDefs) {
            final boolean containsConceptName = event.attributes.stream().anyMatch(a -> a.name.toString().equals(CONCEPT_NAME));
            if (!containsConceptName) {
                final String error = String.format(
                    "XES compliance problem: XES event emissions must contain the '%s' attribute to comply with XES certification level A1.",
                    CONCEPT_NAME
                );
                this.errorCollector.addSemanticError(event.startToken, error);
            }
        }

        final AttributeDef def = attributes.get(CONCEPT_NAME);
        if (def == null || !def.isGlobal) {
            final String warning = String.format(GLOBAL_ATTR_WARNING_TEMPLATE, CONCEPT_NAME, "A1");
            this.errorCollector.addWarning(this.documentStartToken.get(), warning);
        }
    }

    private void verifyCertLevelB(Map<String, AttributeDef> attributes) {
        final AttributeDef timestamp = attributes.get(TIME_TIMESTAMP);
        final AttributeDef transition = attributes.get(LIFECYLE_TRANSITION);

        final String missingAttributeWarning = "XES compliance problem: The XES standard extension attribute '%1$s' "
            + "must be used in combination with the XES standard extension attribute '%2$s', which is not explicitly used in the script. "
            + "To ensure compliance with XES certification level B1, a global event attribute for '%2$s' will automatically be added during extraction.";
        if (timestamp == null && transition == null) { // none of the attributes exist
            return;
        } else if (transition == null) { // only timestamp is set
            final String transitionWarning = String.format(missingAttributeWarning, TIME_TIMESTAMP, LIFECYLE_TRANSITION);
            this.errorCollector.addWarning(this.documentStartToken.get(), transitionWarning);
        } else if (timestamp == null) { // only transition is set
            final String timestampWarning = String.format(missingAttributeWarning, LIFECYLE_TRANSITION, TIME_TIMESTAMP);
            this.errorCollector.addWarning(this.documentStartToken.get(), timestampWarning);
        }

        if (timestamp != null && !timestamp.isGlobal) { // timestamp is used, but not set globally
            this.errorCollector.addWarning(
                this.documentStartToken.get(),
                String.format(GLOBAL_ATTR_WARNING_TEMPLATE, TIME_TIMESTAMP, "B1")
            );
        }
        if (transition != null && !transition.isGlobal) { // transition is used, but not set globally
            this.errorCollector.addWarning(
                this.documentStartToken.get(),
                String.format(GLOBAL_ATTR_WARNING_TEMPLATE, LIFECYLE_TRANSITION, "B1")
            );
        }

        final String eventAttributeWarning =
            "XES compliance problem: The XES standard extension attribute '%1$s' is %2$s used in the script, "
                + "but it was not added in this XES event emission. When used, it is recommended to define meaningful values for '%1$s' in all XES event emissions.";
        for (EventDef event : this.eventDefs) {
            if (!this.containsAttribute(event, TIME_TIMESTAMP)) {
                final String timestampWarning = String.format(
                    eventAttributeWarning,
                    TIME_TIMESTAMP,
                    timestamp == null ? "implicitly" : "explicitly",
                    timestamp == null || !timestamp.isGlobal ? "automatically" : "manually"
                );
                this.errorCollector.addWarning(event.startToken, timestampWarning);
            }
            if (!this.containsAttribute(event, LIFECYLE_TRANSITION)) {
                final String timestampWarning = String.format(
                    eventAttributeWarning,
                    LIFECYLE_TRANSITION,
                    transition == null ? "implicitly" : "explicitly",
                    transition == null || !transition.isGlobal ? "automatically" : "manually"
                );
                this.errorCollector.addWarning(event.startToken, timestampWarning);
            }
        }
    }

    private void verifyCertLevelC(Map<String, AttributeDef> attributes) {
        final AttributeDef resource = attributes.get(ORG_RESOURCE);
        if (resource == null) {
            return;
        }

        if (!resource.isGlobal) { // timestamp is used, but not set globally
            this.errorCollector.addWarning(this.documentStartToken.get(), String.format(GLOBAL_ATTR_WARNING_TEMPLATE, ORG_RESOURCE, "C1"));
        }

        final String eventAttributeWarning = "XES compliance problem: The XES standard extension attribute '%1$s' is used in the script, "
            + "but was not added in this XES event emission. When used, it is recommended to define meaningful values for '%1$s' in all XES event emissions.";
        for (EventDef event : this.eventDefs) {
            if (!this.containsAttribute(event, ORG_RESOURCE)) {
                final String timestampWarning = String.format(
                    eventAttributeWarning,
                    ORG_RESOURCE,
                    resource.isGlobal ? "manually" : "automatically"
                );
                this.errorCollector.addWarning(event.startToken, timestampWarning);
            }
        }
    }

    private boolean satisfiesExtension(AttributeDef attr) {
        if (attr.name.prefix == null) {
            return true;
        }

        if (this.isExtensionAttribute(attr.name)) {
            final String type = this.getExtensionType(attr.name);
            if (type == null) {
                final String errorMessage = String.format(
                    "XES compliance problem: The XES standard extension attribute '%s' has the type '%s'. "
                        + "This type is currently not supported.",
                    attr.toString(),
                    type,
                    attr.type
                );
                this.errorCollector.addSemanticError(attr.startToken, errorMessage);
                return false;
            } else if (!type.equals(attr.type)) {
                final String errorMessage = String.format(
                    "XES compliance problem: The type '%s' was specified for attribute '%s'. "
                        + "This violates the definition from the standard extension, where the type '%s' was specified.",
                    attr.type,
                    attr.name.toString(),
                    type
                );
                this.errorCollector.addSemanticError(attr.startToken, errorMessage);
                return false;
            }
        } else {
            final String errorMessage = String.format(
                "XES compliance problem: The XES standard extension attribute '%s' does not exist.",
                attr.name
            );
            this.errorCollector.addSemanticError(attr.startToken, errorMessage);
            return false;
        }

        return true;
    }

    private boolean isExtensionAttribute(AttributeName name) {
        final XExtension extension = XExtensionManager.instance().getByPrefix(name.prefix);
        if (extension == null) {
            return false;
        }

        return extension.getEventAttributes().stream().anyMatch(attr -> attr.getKey().equals(name.toString()));
    }

    private String getExtensionType(AttributeName name) {
        final XExtension extension = XExtensionManager.instance().getByPrefix(name.prefix);
        final XAttribute attribute = extension.getEventAttributes()
            .stream()
            .filter(a -> a.getKey().equals(name.toString()))
            .findFirst()
            .orElse(null);

        if (attribute == null) {
            final String errorMessage = String.format("The XES standard extension attribute '%s' does not exist!", name);
            throw new IllegalArgumentException(errorMessage);
        }

        if (XAttributeBoolean.class.isAssignableFrom(attribute.getClass())) {
            return XesWriter.BOOLEAN_TYPE;
        } else if (XAttributeTimestamp.class.isAssignableFrom(attribute.getClass())) {
            return XesWriter.DATE_TYPE;
        } else if (XAttributeContinuous.class.isAssignableFrom(attribute.getClass())) {
            return XesWriter.FLOAT_TYPE;
        } else if (XAttributeDiscrete.class.isAssignableFrom(attribute.getClass())) {
            return XesWriter.INT_TYPE;
        } else if (XAttributeLiteral.class.isAssignableFrom(attribute.getClass())) {
            return XesWriter.STRING_TYPE;
        } else {
            return null;
        }
    }

    private boolean isValueTypeCompatible(String xesType, LiteralContext value) {
        final String solType = InterpreterUtils.literalType(value);
        return XesWriter.areTypesCompatible(solType, xesType);
    }

    private boolean isRequiredAsGlobal(AttributeName attrName, Map<String, AttributeDef> attributes) {
        final AttributeDef attr = attributes.get(attrName.toString());
        if (attr == null) {
            return true;
        }

        if (attr.isGlobal) {
            return false;
        }

        return !this.isCertAttribute(attrName);
    }

    private boolean isCertAttribute(AttributeName attrName) {
        final String plainName = attrName.toString();
        return plainName.equals(CONCEPT_NAME)
            || plainName.equals(TIME_TIMESTAMP)
            || plainName.equals(LIFECYLE_TRANSITION)
            || plainName.equals(ORG_RESOURCE);
    }

    private boolean containsAttribute(EventDef event, String fullName) {
        return event.attributes.stream().anyMatch(a -> a.name.toString().equals(fullName));
    }
}
