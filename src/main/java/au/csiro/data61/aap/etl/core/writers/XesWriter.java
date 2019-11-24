package au.csiro.data61.aap.etl.core.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * XesExporter
 */
public class XesWriter extends DataWriter {
    private static final String PID_ATTRIBUTE = "ident:pid";
    private static final String PIID_ATTRIBUTE = "ident:piid";
    private static final String EID_ATTRIBUTE = "ident:eid";
    private static final String DEFAULT_PID = "___DEFAULT___PID___";
    private static final String DEFAULT_PIID = "___DEFAULT___PIID___";
    private static final String DEFAULT_EID = "___DEFAULT___EID___";
    
    private final Map<String, Map<String, XTrace>> traces;
    private final Map<String, Map<String, Map<String, XEvent>>> events;
    private XAttributable element;

    public XesWriter() {
        this.traces = new LinkedHashMap<>();
        this.events = new LinkedHashMap<>();
    }

    public void startTrace(String inputPid, String inputPiid) {
        final String pid = inputPid == null ? DEFAULT_PID : inputPid;
        final String piid = inputPiid == null ? DEFAULT_PIID : inputPiid;

        this.element = this.findOrCreateTrace(pid, piid);                
    }

    private XTrace findOrCreateTrace(String pid, String piid) {
        if (this.traces.containsKey(pid) && this.traces.get(pid).containsKey(piid)) {
            return this.traces.get(pid).get(piid);
        } 

        final XTrace trace = new XTraceImpl(new XAttributeMapImpl());
        this.addStringValue(PIID_ATTRIBUTE, piid);

        this.traces.putIfAbsent(pid, new LinkedHashMap<>());
        this.traces.get(pid).put(piid, trace);
        return trace;
    }

    public void startEvent(String inputPid, String inputPiid, String inputEid) {
        final String pid = inputPid == null ? DEFAULT_PID : inputPid;
        final String piid = inputPiid == null ? DEFAULT_PIID : inputPiid;
        final String eid = inputEid == null ? DEFAULT_EID : inputEid;

        this.element = this.findOrCreateEvent(pid, piid, eid);

        
    }    

    private XAttributable findOrCreateEvent(String pid, String piid, String eid) {
        if (   this.events.containsKey(pid)
            && this.events.get(pid).containsKey(piid)
            && this.events.get(pid).get(piid).containsKey(eid)
        ) {
            return this.events.get(pid).get(piid).get(eid);
        }
        
        final XEvent event = new XEventImpl(new XAttributeMapImpl());
        this.addStringValue(EID_ATTRIBUTE, eid);
        
        this.events.putIfAbsent(pid, new LinkedHashMap<>());
        this.events.get(pid).putIfAbsent(piid, new LinkedHashMap<>());
        this.events.get(pid).get(piid).put(eid, event);
        
        return event;

    }

    public void addBooleanValue(String key, boolean value) {
        assert key != null;
        this.addAttribute(key, value, XAttributeBooleanImpl::new);
    }

    public void addBooleanList(String key, List<Boolean> values) {
        assert key != null && values != null && values.stream().allMatch(Objects::nonNull);
        this.addListAttribute(key, values, XAttributeBooleanImpl::new);
    }

    public void addFloatValue(String key, BigInteger value) {
        assert key != null && value != null;
        this.addAttribute(key, value.doubleValue(), XAttributeContinuousImpl::new);
    }

    public void addFloatList(String key, List<BigInteger> values) {
        assert key != null && values != null && values.stream().allMatch(Objects::nonNull);
        final List<Double> list = values.stream().map(BigInteger::doubleValue).collect(Collectors.toList());
        this.addListAttribute(key, list, XAttributeContinuousImpl::new);
    }

    public void addIntValue(String key, BigInteger value) {
        assert key != null && value != null;
        this.addAttribute(key, value.longValue(), XAttributeDiscreteImpl::new);
    }

    public void addIntList(String key, List<BigInteger> values) {
        assert key != null && values != null && values.stream().allMatch(Objects::nonNull);
        final List<Long> list = values.stream().map(BigInteger::longValue).collect(Collectors.toList());
        this.addListAttribute(key, list, XAttributeContinuousImpl::new);
    }

    public void addDateValue(String key, BigInteger value) {
        assert key != null && value != null;
        final Date date = new Date(value.longValue());
        this.addAttribute(key, date, XAttributeTimestampImpl::new);
    }

    public void addDateList(String key, List<BigInteger> values) {
        assert key != null && values != null && values.stream().allMatch(Objects::nonNull);
        final List<Date> list = values.stream().map(BigInteger::longValue).map(Date::new).collect(Collectors.toList());
        this.addListAttribute(key, list, XAttributeTimestampImpl::new);
    }

    public void addStringValue(String key, String value) {
        assert key != null && value != null;
        this.addAttribute(key, value, XAttributeLiteralImpl::new);
    }

    public void addStringList(String key, List<String> values) {
        assert key != null && values != null && values.stream().allMatch(Objects::nonNull);
        this.addListAttribute(key, values, XAttributeLiteralImpl::new);
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
        final Map<String, XLog> logs = this.getLogs();
        try  {
            final File folder = this.getOutputFolder().toAbsolutePath().toFile();
            final XesXmlSerializer serializer = new XesXmlSerializer();
            for (Entry<String, XLog> entry : logs.entrySet()) {
                final String filename = String.format("log_%s_%s.xes", entry.getKey(), filenameSuffix);
                final File file = new File(folder, filename);
                serializer.serialize(entry.getValue(), new FileOutputStream(file));
            }
        }
        catch (Throwable t) {
            final String message = "Error exporting data to XES.";
            throw new ProgramException(message, t);
        }
        finally {
            this.events.clear();
            this.traces.clear();
            this.element = null;
        }
    }

    private Map<String, XLog> getLogs() {
        return Stream.concat(this.traces.keySet().stream(),this.events.keySet().stream())
            .distinct()
            .collect(Collectors.toMap(
                pid -> pid, pid -> createLog(pid)
            )
        );
    }

    private XLog createLog(String pid) {
        final XLog log = new XLogImpl(new XAttributeMapImpl());
        this.element = log;
        this.addStringValue(PID_ATTRIBUTE, pid);
        this.addTracesToLog(log, pid);
        return log;
    }

    private void addTracesToLog(XLog log, String pid) {
        this.traces.getOrDefault(pid, new LinkedHashMap<>())
            .entrySet().stream()
            .forEach(entry -> {
                log.add(entry.getValue());
                addEventsToLog(entry.getValue(), pid, entry.getKey());
            });
    }


    private void addEventsToLog(XTrace trace, String pid, String piid) {
        this.events.getOrDefault(pid, new LinkedHashMap<>())
            .getOrDefault(piid, new LinkedHashMap<>())
                .entrySet().stream()
                .forEach(entry -> trace.add(entry.getValue()))
        ;
    }
}