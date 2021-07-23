package au.csiro.data61.aap.elf.parsing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
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
    private static final AttributeName CONCEPT_NAME = new AttributeName("concept:name");
    private static final AttributeName TIME_TIMESTAMP = new AttributeName("time:timestamp");
    private static final AttributeName ORG_RESOURCE = new AttributeName("org:resource");
    private static final AttributeName LIFECYLE_TRANSITION = new AttributeName("lifecycle:transition");
    private static final String LEVEL_A1 = "A1";
    // private static final String LEVEL_A2 = "A2";
    private static final String LEVEL_B1 = "B1";
    // private static final String LEVEL_B2 = "B2";
    private static final String LEVEL_C1 = "C1";
    // private static final String LEVEL_C2 = "C2";
    private static final String LEVEL_D1 = "D1";
    // private static final String LEVEL_D2 = "D2";
    private static final String FLAG_X1 = "X1";
    // private static final String FLAG_X2 = "X2";

    private static final String GLOBAL_ATTR_WARNING_TEMPLATE =
        "XES compliance problem: The XES standard extension attribute '%s' must be specified as a "
            + "global event attribute, in order to comply with XES certification level %s. "
            + "As this was not done explicitly in the script, a global event attribute definition "
            + "with a default value will be added automatically during extraction.";

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

        private AttributeDef(Token startToken, String type, String fullName) {
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
            super(startToken, type.substring(1, type.length() - 1), fullName.substring(1, fullName.length() - 1));
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
    // private final AtomicBoolean hasErrors;
    private final List<GlobalValueDef> globalValueDefs;
    private final List<ClassifierDef> classifierDefs;
    private final List<EventDef> eventDefs;
    private final AtomicReference<Token> documentStartToken;

    public XesCertificationLevelsAnalyzer(EventCollector errorCollector) {
        super(errorCollector);
        this.isPreambleFinished = new AtomicBoolean(false);
        this.globalValueDefs = new LinkedList<>();
        this.classifierDefs = new LinkedList<>();
        this.eventDefs = new LinkedList<>();
        this.documentStartToken = new AtomicReference<>();
        // this.hasErrors = new AtomicBoolean(false);
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
            case Library.ADD_XES_EVENT_CLASSIFIER:
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

        if (!this.areParametersLiterals(ctx, Library.ADD_XES_EVENT_CLASSIFIER)) {
            return;
        }

        final ClassifierDef def = new ClassifierDef(
            ctx.start,
            ctx.methodInvocation().valueExpression(0).literal().getText(),
            ctx.methodInvocation().valueExpression(1).literal().getText()
        );

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
            this.addError(ctx.start, errorMessage);
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
        this.verifyGlobalAttributes();
        this.verifyClassifiers();

        this.verifyCertLevelA();
        this.verifyCertLevelB();
        this.verifyCertLevelC();
        this.verifyCertLevelDAndFlagX();

        // if (!this.hasErrors.get()) {
        // this.determineCertificationLevels();
        // }

        this.cleanUp();
    }

    /*
    @Override
    protected void addError(Token token, String message) {
        this.hasErrors.set(true);
        super.addError(token, message);
    }
    */

    private void verifyGlobalAttributes() {
        final Map<String, AttributeDef> attributes = new HashMap<>();

        if (this.globalValueDefs.isEmpty()) {
            return;
        }

        if (this.eventDefs.isEmpty()) {
            final String error = "XES compliance problem: The script defines global event attributes "
                + "for XES logs but doesn't contain XES event emissions.";
            this.addError(this.documentStartToken.get(), error);
            return;
        }

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
                this.addError(globalValue.startToken, error);
                continue;
            }

            final AttributeDef attr = this.transformToExtensionCompliantAttribute(globalValue);
            if (attr == null) {
                continue;
            }
            attributes.put(attrName, attr);

            if (!this.isValueTypeCompatible(attr.type, globalValue.value)) {
                final String errorMessage =
                    "XES compliance problem: The value type and the specified type for the XES attribute are not compatible.";
                this.addError(globalValue.startToken, errorMessage);
            }
        }
    }

    private void verifyClassifiers() {
        for (ClassifierDef def : this.classifierDefs) {
            if (def.attributes.isEmpty()) {
                this.addError(def.startToken, "XES compliance problem: XES event classifiers must contain at least one attribute.");
                return;
            }

            for (AttributeName attrName : def.attributes) {
                if (this.isRequiredAsGlobal(attrName)) {
                    final String error = String.format(
                        "XES compliance problem: The attribute '%s' is part of an XES event classifier, but is not defined as a global event attribute.",
                        attrName
                    );
                    this.addError(def.startToken, error);
                }
            }
        }
    }

    private void verifyCertLevelA() {
        for (EventDef event : this.eventDefs) {
            final AttributeDef conceptName = this.getAttribute(event, CONCEPT_NAME);
            if (conceptName == null) {
                final String error = String.format(
                    "XES compliance problem: XES event emissions must contain the standard extension attribute"
                        + " '%s' to comply with XES certification level %s.",
                    CONCEPT_NAME,
                    LEVEL_A1
                );
                this.addError(event.startToken, error);
            } else {
                this.transformToExtensionCompliantAttribute(conceptName);
            }
        }

        this.addGlobalAttributeWarning(CONCEPT_NAME, LEVEL_A1);
    }

    private void verifyCertLevelB() {
        final boolean timestampUsed = this.existsEventAttribute(TIME_TIMESTAMP) || this.existsGlobalAttribute(TIME_TIMESTAMP);
        final boolean transitionUsed = this.existsEventAttribute(LIFECYLE_TRANSITION) || this.existsGlobalAttribute(LIFECYLE_TRANSITION);
        if (!timestampUsed && !transitionUsed) { // none of the attributes exist
            return;
        }

        final String explicitError = "XES compliance problem: The XES standard extension attribute '%2$s' is used in the script. "
            + "Thus, all XES event emissions must contain this attribute to comply with XES certification level %3$s.";
        final String implicitError = "XES compliance problem: The XES standard extension attribute '%1$s' is used in the script. "
            + "Thus, all XES event emissions must also contain the standard extension attribute '%2$s' "
            + "to comply with XES certification level %3$s.";
        final String timestampError = timestampUsed ? explicitError : implicitError;
        final String transitionError = transitionUsed ? explicitError : implicitError;
        for (EventDef event : this.eventDefs) {
            final AttributeDef timestamp = this.getAttribute(event, TIME_TIMESTAMP);
            if (timestamp == null) {
                final String error = String.format(timestampError, LIFECYLE_TRANSITION, TIME_TIMESTAMP, LEVEL_B1);
                this.addError(event.startToken, error);
            } else {
                this.transformToExtensionCompliantAttribute(timestamp);
            }

            final AttributeDef transition = this.getAttribute(event, LIFECYLE_TRANSITION);
            if (transition == null) {
                final String error = String.format(transitionError, TIME_TIMESTAMP, LIFECYLE_TRANSITION, LEVEL_B1);
                this.addError(event.startToken, error);
            } else {
                this.transformToExtensionCompliantAttribute(transition);
            }
        }

        this.addGlobalAttributeWarning(TIME_TIMESTAMP, LEVEL_B1);
        this.addGlobalAttributeWarning(LIFECYLE_TRANSITION, LEVEL_B1);
    }

    private void verifyCertLevelC() {
        if (!this.existsEventAttribute(ORG_RESOURCE) && !this.existsGlobalAttribute(ORG_RESOURCE)) {
            return;
        }

        this.addGlobalAttributeWarning(ORG_RESOURCE, LEVEL_C1);

        final String eventAttributeWarning = "XES compliance problem: The XES standard extension attribute '%1$s' is used in the script. "
            + "Thus, all XES event emissions must contain this attribute to comply with XES certification level %2$s.";
        for (EventDef event : this.eventDefs) {
            if (!this.containsAttribute(event, ORG_RESOURCE)) {
                final String timestampWarning = String.format(eventAttributeWarning, ORG_RESOURCE, LEVEL_C1);
                this.addError(event.startToken, timestampWarning);
            }
        }
    }

    private void verifyCertLevelDAndFlagX() {
        final Map<AttributeName, AttributeDef> attributes = new HashMap<>();
        this.eventDefs.stream().flatMap(e -> e.attributes.stream()).filter(a -> !this.isCertAttribute(a.name)).forEach(a -> {
            if (!attributes.containsKey(a.name)) {
                attributes.put(a.name, a);
            }
        });

        for (Entry<AttributeName, AttributeDef> entry : attributes.entrySet()) {
            final AttributeDef global = this.getGlobalAttribute(entry.getKey());

            for (EventDef event : this.eventDefs) {
                final AttributeDef attr = this.getAttribute(event, entry.getKey());

                if (attr == null) { // event doesn't have this attribute
                    if (global != null) { // that's only a problem, if the attribute was specified globally
                        final String error = String.format(
                            "XES compliance problem: The XES event attribute '%s' was defined globally. "
                                + "It must thus be added to all XES event emissions to comply with XES certification level %s.",
                            entry.getKey(),
                            entry.getKey().prefix == null ? FLAG_X1 : LEVEL_D1
                        );
                        this.addError(event.startToken, error);
                    }
                } else { // the event exists and must adhere to the type of the first usage or the global event attribute definition
                    if (this.transformToExtensionCompliantAttribute(attr) == null || attr.name.prefix != null) {
                        continue;
                    }

                    if (global != null) {
                        if (!attr.type.equals(global.type)) {
                            final String error = String.format(
                                "XES compliance problem: The XES event attribute '%s' was already defined with a different type at Ln %s, Col %s.",
                                attr.name,
                                global.startToken.getLine(),
                                global.startToken.getCharPositionInLine()
                            );
                            this.addError(attr.startToken, error);
                        }
                    } else if (attr != entry.getValue()) {
                        if (!attr.type.equals(entry.getValue().type)) {
                            final String error = String.format(
                                "XES compliance problem: The XES event attribute '%s' was already defined with a different type at Ln %s, Col %s.",
                                attr.name,
                                entry.getValue().startToken.getLine(),
                                entry.getValue().startToken.getCharPositionInLine()
                            );
                            this.addError(attr.startToken, error);
                        }
                    }
                }
            }
        }
    }

    private void cleanUp() {
        this.isPreambleFinished.set(false);
        this.globalValueDefs.clear();
        this.classifierDefs.clear();
        this.eventDefs.clear();
        this.documentStartToken.set(null);
        // this.hasErrors.set(false);
    }

    private AttributeDef transformToExtensionCompliantAttribute(AttributeDef attr) {
        if (attr.name.prefix == null) {
            return attr;
        }

        if (this.isExtensionAttribute(attr.name)) {
            final String type = this.getExtensionType(attr.name);
            if (type == null) {
                final String errorMessage = String.format(
                    "XES compliance problem: The XES standard extension attribute '%s' has the type '%s'. "
                        + "This type is currently not supported.",
                    attr.name,
                    type,
                    attr.type
                );
                this.addError(attr.startToken, errorMessage);
                return null;
            } else if (!type.equals(attr.type)) {
                final String errorMessage = String.format(
                    "XES compliance problem: The type '%s' was specified for attribute '%s'. "
                        + "This violates the definition from the standard extension, where the type '%s' was specified.",
                    attr.type,
                    attr.name,
                    type
                );
                this.addError(attr.startToken, errorMessage);
                return new AttributeDef(attr.startToken, type, attr.name.toString());
            } else {
                return attr;
            }
        } else {
            final String errorMessage = String.format(
                "XES compliance problem: The XES standard extension attribute '%s' does not exist.",
                attr.name
            );
            this.addError(attr.startToken, errorMessage);
            return null;
        }
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

    private boolean isRequiredAsGlobal(AttributeName attrName) {
        if (this.isCertAttribute(attrName)) {
            return false;
        }

        return !this.existsGlobalAttribute(attrName);
    }

    private boolean isCertAttribute(AttributeName attrName) {
        return attrName.equals(CONCEPT_NAME)
            || attrName.equals(TIME_TIMESTAMP)
            || attrName.equals(LIFECYLE_TRANSITION)
            || attrName.equals(ORG_RESOURCE);
    }

    private boolean existsGlobalAttribute(AttributeName name) {
        return this.globalValueDefs.stream().anyMatch(def -> def.name.equals(name));
    }

    private GlobalValueDef getGlobalAttribute(AttributeName name) {
        return this.globalValueDefs.stream().filter(a -> a.name.equals(name)).findFirst().orElse(null);
    }

    private boolean existsEventAttribute(AttributeName name) {
        return this.eventDefs.stream().anyMatch(e -> this.containsAttribute(e, name));
    }

    private boolean containsAttribute(EventDef event, AttributeName name) {
        return event.attributes.stream().anyMatch(a -> a.name.equals(name));
    }

    private AttributeDef getAttribute(EventDef event, AttributeName name) {
        return event.attributes.stream().filter(a -> a.name.equals(name)).findFirst().orElse(null);
    }

    private void addGlobalAttributeWarning(AttributeName attrName, String level) {
        if (!this.existsGlobalAttribute(attrName)) {
            final String warning = String.format(GLOBAL_ATTR_WARNING_TEMPLATE, attrName, level);
            this.errorCollector.addWarning(this.documentStartToken.get(), warning);
        }
    }
}
