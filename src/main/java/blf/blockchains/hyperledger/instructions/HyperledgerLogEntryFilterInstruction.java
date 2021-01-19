package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerListenerHelper;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.grammar.BcqlParser;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.json.*;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

/**
 * HyperledgerLogEntryFilterInstruction is an Instruction for the Hyperledger log extraction mode of the Blockchain
 * Logging Framework. It extracts the requested event from the current Block and stores the extracted parameter values
 * in the ProgramState.
 */
public class HyperledgerLogEntryFilterInstruction extends FilterInstruction {

    private final BcqlParser.LogEntryFilterContext logEntryFilterCtx;

    private final ExceptionHandler exceptionHandler;
    @SuppressWarnings("FieldCanBeLocal")
    private final Logger logger;

    private String eventName;
    private List<Pair<String, String>> entryParameters;

    /**
     * Constructs a HyperledgerLogEntryFilterInstruction.
     *
     * @param logEntryFilterCtx  The context of logEntryFilter.
     * @param nestedInstructions The list of nested instruction.
     */
    public HyperledgerLogEntryFilterInstruction(BcqlParser.LogEntryFilterContext logEntryFilterCtx, List<Instruction> nestedInstructions) {
        super(nestedInstructions);

        this.logEntryFilterCtx = logEntryFilterCtx;
        this.logger = Logger.getLogger(HyperledgerBlockFilterInstruction.class.getName());
    }

    /**
     * execute is called once the program is constructed from the manifest. It contains the logic for extracting an
     * event from the Hyperledger block that the BLF is currently analyzing. It is called by the Program class.
     *
     * @param state The current ProgramState of the BLF, provided by the Program when called.
     * @throws ProgramException never explicitly
     */
    @Override
    public void execute(ProgramState state) throws ProgramException {
        // init exception handler
        this.exceptionHandler = state.getExceptionHandler();

        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        final Triple<String, List<Pair<String, String>>, List<String>> logEntryFilterParams = HyperledgerListenerHelper
            .parseLogEntryFilterCtx(hyperledgerProgramState, logEntryFilterCtx);

        this.eventName = logEntryFilterParams.a;
        this.entryParameters = logEntryFilterParams.b;
        List<String> addressNames = logEntryFilterParams.c;

        // get current block
        BlockEvent be = hyperledgerProgramState.getCurrentBlock();
        if (be == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Expected block, received null", new NullPointerException());

            return;
        }

        for (BlockEvent.TransactionEvent te : be.getTransactionEvents()) {
            for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo ti : te.getTransactionActionInfos()) {
                // if current transaction does not belong to requested chaincode addresses
                if (!addressNames.contains(ti.getChaincodeIDName())) {
                    continue;
                }

                ChaincodeEvent ce = ti.getEvent();
                if (ce != null) {
                    parseChaincodeEvent(hyperledgerProgramState, ce);
                }
            }
        }
    }

    /**
     * Tries to parse the ChaincodeEvent in the manner the manifest requested.
     * <p>
     * Firstly, it tries to parse the payload as a JSON string. If that fails, it tries to extract a single parameter
     * from the unstructured payload.
     *
     * @param hyperledgerProgramState The current ProgramState of the BLF.
     * @param ce                      The current ChaincodeEvent to analyze.
     */
    private void parseChaincodeEvent(HyperledgerProgramState hyperledgerProgramState, ChaincodeEvent ce) {
        // first try parse json
        String payloadString = new String(ce.getPayload());
        try {
            JSONObject obj = new JSONObject(payloadString);

            // if string could be parsed as JSON object handle it as such
            parseNestedJsonPayload(hyperledgerProgramState, ce, obj);
        } catch (JSONException e) {
            // there is no valid json in the payload
            parseNonStructuredPayload(hyperledgerProgramState, ce, payloadString, e);
        }
    }

    /**
     * Tries to parse an event from a nested JSON object, if another JSON object is contained in the top-level
     * JSON object of the payload and its key is the requested event name.
     * If such object is not found, the top-level JSON object is tried to be handles as a flat JSON object containing
     * the event parameters.
     *
     * @param hyperledgerProgramState The current ProgramState of the BLF.
     * @param ce                      The ChaincodeEvent to analyze.
     * @param obj                     The top-level JSON object that was successfully parsed from the event payload.
     */
    private void parseNestedJsonPayload(HyperledgerProgramState hyperledgerProgramState, ChaincodeEvent ce, JSONObject obj) {
        try {
            // if json is nested, search for eventName as index in the nested object
            JSONObject eventObj = obj.getJSONObject(this.eventName);

            // search for every requested parameter and set it to the program state
            for (Pair<String, String> parameter : this.entryParameters) {
                String parameterType = parameter.a;
                String parameterName = parameter.b;

                setStateValue(hyperledgerProgramState, parameterName, parameterType, eventObj);
            }

            // emit what we extracted
            this.executeNestedInstructions(hyperledgerProgramState);
        } catch (JSONException e) {
            // payload does not contain a nested json object with the given eventName
            parseFlatJsonPayload(hyperledgerProgramState, ce, obj);
        }
    }

    /**
     * Tries to parse the requested event from a flat JSON object, if the event name fits the requested event name.
     *
     * @param hyperledgerProgramState The current ProgramState of the BLF.
     * @param ce                      The ChaincodeEvent to analyze.
     * @param obj                     The top-level JSON object that was successfully parsed from the event payload.
     */
    private void parseFlatJsonPayload(HyperledgerProgramState hyperledgerProgramState, ChaincodeEvent ce, JSONObject obj) {
        // if json is flat, check if the eventName is the event name of the Event
        if (this.eventName.equals(ce.getEventName())) {
            // search for every requested parameter and set it to the program state
            for (Pair<String, String> parameter : this.entryParameters) {
                String parameterType = parameter.a;
                String parameterName = parameter.b;

                setStateValue(hyperledgerProgramState, parameterName, parameterType, obj);
            }

            // emit what we extracted
            this.executeNestedInstructions(hyperledgerProgramState);
        }
    }

    /**
     * Tries to parse a single parameter from an unstructured payload.
     * <p>
     * If the event payload does not contain structured JSON data, we can not now if it holds any other structure of
     * multiple parameters internally, as all we see is an array of bytes. We can try to parse one single parameter out
     * of these bytes.
     *
     * @param hyperledgerProgramState The current ProgramState of the BLF.
     * @param ce                      The ChaincodeEvent to analyze.
     * @param payloadString           The payload of the ce, already converted to a string.
     * @param jsonException           The exception thrown by the JSON parser, the reason why the JSON could not be recognized.
     */
    private void parseNonStructuredPayload(
        HyperledgerProgramState hyperledgerProgramState,
        ChaincodeEvent ce,
        String payloadString,
        JSONException jsonException
    ) {
        // if json parsing fails completely, also check the eventName and try to parse the byte array as single value
        if (this.eventName.equals(ce.getEventName())) {
            if (this.entryParameters.size() == 1) {
                // parse the one unstructured parameter
                String parameterType = this.entryParameters.get(0).a;
                String parameterName = this.entryParameters.get(0).b;
                setStateValue(hyperledgerProgramState, parameterName, parameterType, payloadString, ce.getPayload());

                // emit what we extracted
                this.executeNestedInstructions(hyperledgerProgramState);
            } else {
                this.exceptionHandler.handleExceptionAndDecideOnAbort(
                    "We expect exactly one parameter when extracting unstructured data",
                    jsonException
                );
            }
        }
    }

    /**
     * Tries to parse the payload into a type hinted by parameterType. Afterwards sets it to the ValueStore of the
     * ProgramState.
     *
     * @param hyperledgerProgramState The current ProgramState of the BLF.
     * @param parameterName           The name of the parameter that is to be extracted and set to the state.
     * @param parameterType           The type of the parameter that is to be extracted and set to the state.
     * @param payloadString           The payload of the ce, already converted to a string.
     * @param payload                 The payload of the ce, as the original array of bytes.
     */
    private void setStateValue(
        HyperledgerProgramState hyperledgerProgramState,
        String parameterName,
        String parameterType,
        String payloadString,
        byte[] payload
    ) {
        if (parameterType.contains("int")) {
            try {
                BigInteger data = new BigInteger(payloadString);
                hyperledgerProgramState.getValueStore().setValue(parameterName, data);
            } catch (NumberFormatException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Could not parse payload to BigInteger", e);
            }
        } else if (parameterType.contains("string")) {
            hyperledgerProgramState.getValueStore().setValue(parameterName, payloadString);
        } else if (parameterType.contains("bool")) {
            try {
                boolean data = payload[0] != 0;
                hyperledgerProgramState.getValueStore().setValue(parameterName, data);
            } catch (ArrayIndexOutOfBoundsException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Could not convert empty payload to bool", e);
            }
        } else if (parameterType.equals("byte")) {
            try {
                byte data = payload[0];
                hyperledgerProgramState.getValueStore().setValue(parameterName, data);
            } catch (ArrayIndexOutOfBoundsException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Could not access byte in empty payload", e);
            }
        } else if (parameterType.contains("bytes")) {
            hyperledgerProgramState.getValueStore().setValue(parameterName, payload);
        }
    }

    /**
     * Tries to parse the contents of a JSON object into a type hinted by parameterType. Afterwards sets it to the
     * ValueStore of the ProgramState.
     *
     * @param hyperledgerProgramState The current ProgramState of the BLF.
     * @param parameterName           The name of the parameter that is to be extracted and set to the state.
     * @param parameterType           The type of the parameter that is to be extracted and set to the state.
     * @param obj                     The JSON object containing the requested event.
     */
    private void setStateValue(
        HyperledgerProgramState hyperledgerProgramState,
        String parameterName,
        String parameterType,
        JSONObject obj
    ) {
        if (obj.has(parameterName)) {
            try {
                if (parameterType.contains("int")) {
                    BigInteger data = obj.getBigInteger(parameterName);
                    hyperledgerProgramState.getValueStore().setValue(parameterName, data);
                } else if (parameterType.contains("bool")) {
                    boolean data = obj.getBoolean(parameterName);
                    hyperledgerProgramState.getValueStore().setValue(parameterName, data);
                } else if (parameterType.contains("string") || parameterType.equals("byte") || parameterType.contains("bytes")) {
                    // TODO maybe something else is better suited here for byte and bytes?
                    String data = obj.getString(parameterName);
                    hyperledgerProgramState.getValueStore().setValue(parameterName, data);
                }
            } catch (JSONException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Wrong type: " + parameterType, e);
            }
        } else {
            String message = "JSON object does not contain key: " + parameterName;
            this.exceptionHandler.handleExceptionAndDecideOnAbort(message, new JSONException(message));
        }
    }
}
