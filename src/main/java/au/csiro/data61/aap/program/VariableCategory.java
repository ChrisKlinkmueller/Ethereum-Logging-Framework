package au.csiro.data61.aap.program;

/**
 * VariableCategory
 */
public enum VariableCategory {
    USER_DEFINED,
    SCOPE_VARIABLE,
    LOG_ENTRY_TOPIC,
    LOG_ENTRY_DATA,
    LOG_ENTRY_SKIP,
    LOG_ENTRY_VARARGS,
    SMART_CONTRACT_STATE,
    SMART_CONTRACT_SKIP,
    SMART_CONTRACT_VARARGS,
    LITERAL
}