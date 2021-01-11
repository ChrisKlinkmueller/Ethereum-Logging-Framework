package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import org.antlr.v4.runtime.misc.Pair;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.json.*;

import java.math.BigInteger;
import java.util.List;

/**
 *
 */
public class HyperledgerLogEntryFilterInstruction implements Instruction {

    private final ExceptionHandler exceptionHandler;

    private final List<String> addressNames;
    private final String eventName;
    private final List<Pair<String, String>> entryParameters;

    public HyperledgerLogEntryFilterInstruction(
        final List<String> addressNames,
        String eventName,
        List<Pair<String, String>> entryParameters
    ) {
        this.addressNames = addressNames;
        this.eventName = eventName;
        this.entryParameters = entryParameters;

        this.exceptionHandler = new ExceptionHandler();
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

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

    private void parseNestedJsonPayload(HyperledgerProgramState hyperledgerProgramState, ChaincodeEvent ce, JSONObject obj) {
        try {
            // if json is nested, search for eventName as index in the nested object
            JSONObject eventObj = obj.getJSONObject(ce.getEventName());

            // search for every requested parameter and set it to the program state
            for (Pair<String, String> parameter : this.entryParameters) {
                String parameterType = parameter.a;
                String parameterName = parameter.b;

                setStateValue(hyperledgerProgramState, parameterName, parameterType, eventObj);
            }
        } catch (JSONException e) {
            // payload does not contain a nested json object with the given eventName
            parseFlatJsonPayload(hyperledgerProgramState, ce, obj);
        }
    }

    private void parseFlatJsonPayload(HyperledgerProgramState hyperledgerProgramState, ChaincodeEvent ce, JSONObject obj) {
        // if json is flat, check if the eventName is the event name of the Event
        if (this.eventName.equals(ce.getEventName())) {
            // search for every requested parameter and set it to the program state
            for (Pair<String, String> parameter : this.entryParameters) {
                String parameterType = parameter.a;
                String parameterName = parameter.b;

                setStateValue(hyperledgerProgramState, parameterName, parameterType, obj);
            }
        }
    }

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
            } else {
                this.exceptionHandler.handleExceptionAndDecideOnAbort(
                    "We expect exactly one parameter when extracting unstructured data",
                    jsonException
                );
            }
        }
    }

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
