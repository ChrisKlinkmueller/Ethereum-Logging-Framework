
// KEYWORDS

grammar Keywords;

KEY_BLOCK_RANGE : B L O C K S;                          // initiates a blockFilter
KEY_TRANSACTIONS : T R A N S A C T I O N S;             // initiates a transactionFilter
KEY_SMART_CONTRACT : S M A R T ' ' C O N T R A C T;     // initiates a smartContractFilter
KEY_LOG_ENTRIES : L O G ' ' E N T R I E S ;             // initiates a logEntryFilter
KEY_IF : I F;                                           // initiates a smartContractFilter

KEY_EARLIEST : E A R L I E S T;                         // pick the earliest block the program can access in the source file
KEY_CURRENT : C U R R E N T;                            // pick the latest block the program can access in the source file
KEY_CONTINUOUS : C O N T I N U O U S;                   // set the program to a continous mode of extraction instead of an ending block
KEY_ANY : A N Y;                                        // set no address restriction in an addressList in the transactionFilter
KEY_INDEXED : 'indexed';                                // corresponds to the 'indexed' addition for indexed parameters in log entries

KEY_NOT: '!';
KEY_AND: '||';
KEY_OR: '&&';
KEY_IN: I N;
KEY_AS: A S;
KEY_EMIT: E M I T;
KEY_CSV_ROW: C S V ' ' R O W;
KEY_LOG_LINE: L O G ' ' L I N E;
KEY_XES_EVENT: X E S ' ' E V E N T;
KEY_XES_TRACE: X E S ' ' T R A C E;
KEY_SET: S E T;
KEY_BLOCKCHAIN: B L O C K C H A I N;
KEY_OUTPUT_FOLDER: O U T P U T ' ' F O L D E R;
KEY_CONNECTION: C O N N E C T I O N;
KEY_IPC: I P C;

KEY_SKIP_INDEXED : '_indexed_';                         // unused
KEY_SKIP_DATA : '_';                                    // unused
