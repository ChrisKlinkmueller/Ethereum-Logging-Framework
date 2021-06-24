package au.csiro.data61.aap.elf.core.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeListImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * XesExporter
 */
public class XesWriter extends DataWriter {
    private static final Logger LOGGER = Logger.getLogger(XesWriter.class.getName());
    private static final String PID_ATTRIBUTE = "ident:pid";
    private static final String PIID_ATTRIBUTE = "ident:piid";
    private static final String EID_ATTRIBUTE = "ident:eid";
    private static final String DEFAULT_PID = "pid0";
    private static final String DEFAULT_PIID = "piid0";
    private static final String DEFAULT_EID = "eid";
    private static long EID = 0;

    private final Map<String, Map<String, XTrace>> traces;
    private final Map<String, Map<String, Map<String, XEvent>>> events;
    private final Map<String, List<XEventAttributeClassifier>> classifiers;
    private XAttributable element;

    public XesWriter() {
        this.traces = new LinkedHashMap<>();
        this.events = new LinkedHashMap<>();
        this.classifiers = new LinkedHashMap<>();
    }

    public void addEventClassifier(String pid, String name, String... attributes) {
        this.classifiers.computeIfAbsent(pid, n -> new LinkedList<>()).add(new XEventAttributeClassifier(name, attributes));
    }

    public void startTrace(String inputPid, String inputPiid) {
        final String pid = inputPid == null ? DEFAULT_PID : inputPid;
        final String piid = inputPiid == null ? DEFAULT_PIID : inputPiid;
        LOGGER.info(String.format("Trace %s in log %s started.", piid, pid));

        this.findOrCreateTrace(pid, piid);
    }

    private void findOrCreateTrace(String pid, String piid) {
        if (this.traces.containsKey(pid) && this.traces.get(pid).containsKey(piid)) {
            this.element = this.traces.get(pid).get(piid);
            return;
        }

        final XTrace trace = new XTraceImpl(new XAttributeMapImpl());
        this.element = trace;
        this.addStringValue(PIID_ATTRIBUTE, piid);

        this.traces.putIfAbsent(pid, new LinkedHashMap<>());
        this.traces.get(pid).put(piid, trace);
        return;
    }

    public void startEvent(String inputPid, String inputPiid, String inputEid) {
        final String pid = inputPid == null ? DEFAULT_PID : inputPid;
        final String piid = inputPiid == null ? DEFAULT_PIID : inputPiid;
        final String eid = inputEid == null ? String.format("%s%s", DEFAULT_EID, Long.toString(EID++)) : inputEid;
        LOGGER.info(String.format("Event %s in trace %s in log %s started.", eid, piid, pid));

        this.findOrCreateTrace(pid, piid);
        this.findOrCreateEvent(pid, piid, eid);
    }

    private void findOrCreateEvent(String pid, String piid, String eid) {
        if (this.events.containsKey(pid) && this.events.get(pid).containsKey(piid) && this.events.get(pid).get(piid).containsKey(eid)) {
            this.element = this.events.get(pid).get(piid).get(eid);
            return;
        }

        final XEvent event = new XEventImpl(new XAttributeMapImpl());
        this.element = event;
        this.addStringValue(EID_ATTRIBUTE, eid);

        this.events.putIfAbsent(pid, new LinkedHashMap<>());
        this.events.get(pid).putIfAbsent(piid, new LinkedHashMap<>());
        this.events.get(pid).get(piid).put(eid, event);
    }

    public void addBooleanValue(String key, Object value) {
        assert key != null;
        this.addAttribute(key, (boolean) value, XAttributeBooleanImpl::new);
        LOGGER.info(String.format("Boolean attribute %s added.", key));
    }

    @SuppressWarnings("unchecked")
    public void addBooleanList(String key, Object value) {
        assert key != null && value != null && value instanceof List && ((List<Boolean>) value).stream().allMatch(Objects::nonNull);
        this.addListAttribute(key, (List<Boolean>) value, XAttributeBooleanImpl::new);
        LOGGER.info(String.format("Boolean list attribute %s added.", key));
    }

    public void addFloatValue(String key, Object value) {
        assert key != null && value != null;
        this.addAttribute(key, ((BigInteger) value).doubleValue(), XAttributeContinuousImpl::new);
        LOGGER.info(String.format("Float attribute %s added.", key));
    }

    @SuppressWarnings("unchecked")
    public void addFloatList(String key, Object value) {
        assert key != null && value != null && value instanceof List && ((List<BigInteger>) value).stream().allMatch(Objects::nonNull);
        final List<Double> list = ((List<BigInteger>) value).stream().map(BigInteger::doubleValue).collect(Collectors.toList());
        this.addListAttribute(key, list, XAttributeContinuousImpl::new);
        LOGGER.info(String.format("Float list attribute %s added.", key));
    }

    public void addIntValue(String key, Object value) {
        assert key != null && value != null;
        this.addAttribute(key, ((BigInteger) value).longValue(), XAttributeDiscreteImpl::new);
        LOGGER.info(String.format("Int attribute %s added.", key));
    }

    @SuppressWarnings("unchecked")
    public void addIntList(String key, Object value) {
        assert key != null && value != null && value instanceof List && ((List<BigInteger>) value).stream().allMatch(Objects::nonNull);
        final List<Long> list = ((List<BigInteger>) value).stream().map(BigInteger::longValue).collect(Collectors.toList());
        this.addListAttribute(key, list, XAttributeContinuousImpl::new);
        LOGGER.info(String.format("Int list attribute %s added.", key));
    }

    private static final long MILLIS_MULTIPLIER = 1000l;

    public void addDateValue(String key, Object value) {
        assert key != null && value instanceof BigInteger;
        final long timestamp = ((BigInteger) value).longValue() * MILLIS_MULTIPLIER;
        final Date date = new Date(timestamp);
        this.addAttribute(key, date, XAttributeTimestampImpl::new);
        LOGGER.info(String.format("Date attribute %s added.", key));
    }

    @SuppressWarnings("unchecked")
    public void addDateList(String key, Object value) {
        assert key != null && value instanceof List && ((List<BigInteger>) value).stream().allMatch(Objects::nonNull);
        final List<Date> list = ((List<BigInteger>) value).stream().map(BigInteger::longValue).map(Date::new).collect(Collectors.toList());
        this.addListAttribute(key, list, XAttributeTimestampImpl::new);
        LOGGER.info(String.format("Date list attribute %s added.", key));
    }

    public void addStringValue(String key, Object value) {
        assert key != null && value != null;
        this.addAttribute(key, value.toString(), XAttributeLiteralImpl::new);
        LOGGER.info(String.format("String attribute %s = %s added.", key, value));
    }

    @SuppressWarnings("unchecked")
    public void addStringList(String key, Object value) {
        assert key != null && value instanceof List && ((List<? extends Object>) value).stream().allMatch(Objects::nonNull);
        final List<String> stringValues = ((List<Object>) value).stream().map(Object::toString).collect(Collectors.toList());
        this.addListAttribute(key, stringValues, XAttributeLiteralImpl::new);
    }

    private <T> void addListAttribute(String key, List<T> list, BiFunction<String, T, XAttribute> attributeCreator) {
        final XAttributeListImpl attrList = new XAttributeListImpl(key);
        for (int i = 0; i < list.size(); i++) {
            T value = list.get(i);
            XAttribute attr = attributeCreator.apply(String.format("%s%s", key, i), value);
            attrList.addToCollection(attr);
        }
        this.element.getAttributes().put(key, attrList);
    }

    private <T> void addAttribute(String key, T value, BiFunction<String, T, XAttribute> attributeCreator) {
        final XAttribute attr = attributeCreator.apply(key, value);
        this.element.getAttributes().put(key, attr);
    }

    @Override
    protected void writeState(String filenameSuffix) throws Throwable {
        LOGGER.info("Xes export started.");
        final Map<String, XLog> logs = this.getLogs();
        try {
            final File folder = this.getOutputFolder().toAbsolutePath().toFile();
            final XesXmlSerializer serializer = new XesXmlSerializer();
            for (Entry<String, XLog> entry : logs.entrySet()) {
                final String filename = String.format("log_%s_%s.xes", entry.getKey(), filenameSuffix);
                final File file = new File(folder, filename);
                serializer.serialize(entry.getValue(), new FileOutputStream(file));
            }
        } catch (Throwable t) {
            LOGGER.info("Xes export finished unsuccessfully.");
            final String message = "Error exporting data to XES.";
            throw new ProgramException(message, t);
        } finally {
            this.events.clear();
            this.traces.clear();
            this.element = null;
        }
        LOGGER.info("Xes export finished.");
    }

    private Map<String, XLog> getLogs() {
        return Stream.concat(this.traces.keySet().stream(), this.events.keySet().stream())
            .distinct()
            .collect(Collectors.toMap(pid -> pid, pid -> createLog(pid)));
    }

    private XLog createLog(String pid) {
        final XLog log = new XLogImpl(new XAttributeMapImpl());
        this.element = log;
        this.addClassifiers(log, pid);
        this.addStringValue(PID_ATTRIBUTE, pid);
        this.addTracesToLog(log, pid);
        return log;
    }

    private void addClassifiers(XLog log, String pid) {
        for (XEventAttributeClassifier classifier : this.classifiers.getOrDefault(pid, Collections.emptyList())) {
            log.getClassifiers().add(new XEventAttributeClassifier(classifier.name(), classifier.getDefiningAttributeKeys()));
        }

    }

    private void addTracesToLog(XLog log, String pid) {
        this.traces.getOrDefault(pid, new LinkedHashMap<>()).entrySet().stream().forEach(entry -> {
            log.add(entry.getValue());
            addEventsToLog(entry.getValue(), pid, entry.getKey());
        });
    }

    private void addEventsToLog(XTrace trace, String pid, String piid) {
        this.events.getOrDefault(pid, new LinkedHashMap<>())
            .getOrDefault(piid, new LinkedHashMap<>())
            .entrySet()
            .stream()
            .forEach(entry -> trace.add(entry.getValue()));
    }

    public static final String BOOLEAN_TYPE = "xs:boolean";
    public static final String DATE_TYPE = "xs:date";
    public static final String FLOAT_TYPE = "xs:float";
    public static final String INT_TYPE = "xs:int";
    public static final String STRING_TYPE = "xs:string";
    private static Map<String, Set<String>> SUPPORTED_SOL_TO_XES_CASTS = Map.of(
        TypeUtils.ADDRESS_TYPE_KEYWORD,
        Set.of(STRING_TYPE),
        TypeUtils.BYTES_TYPE_KEYWORD,
        Set.of(STRING_TYPE),
        TypeUtils.BOOL_TYPE_KEYWORD,
        Set.of(BOOLEAN_TYPE, STRING_TYPE),
        TypeUtils.INT_TYPE_KEYWORD,
        Set.of(STRING_TYPE, FLOAT_TYPE, INT_TYPE, DATE_TYPE),
        TypeUtils.STRING_TYPE_KEYWORD,
        Set.of(STRING_TYPE)
    );

    public static boolean areTypesCompatible(String solType, String xesType) {
        final String rootType = TypeUtils.getRootType(solType);
        return SUPPORTED_SOL_TO_XES_CASTS.getOrDefault(rootType, Collections.emptySet()).contains(xesType);
    }
}
