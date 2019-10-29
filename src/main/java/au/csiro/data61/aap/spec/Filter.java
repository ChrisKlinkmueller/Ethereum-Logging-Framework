package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.ArrayType;
import au.csiro.data61.aap.spec.types.IntegerType;
import au.csiro.data61.aap.spec.types.StringType;

/**
 * Filter
 */
public interface Filter {
    public boolean isStateProcessable();

    public static class BlockFilter implements Filter {
        private static final Variable EARLIEST = new Variable(IntegerType.DEFAULT_INSTANCE, "earliest", true, 0);
        private static final Variable CURRENT = new Variable(StringType.DEFAULT_INSTANCE, "current", true, "current");
        private static final Variable PENDING = new Variable(StringType.DEFAULT_INSTANCE, "pending", true, "pending");

        private final Variable from;
        private final Variable to;

        public BlockFilter(Variable from, Variable to) {
            assert isValidVariable(from);
            assert isValidVariable(to);
            this.from = from;
            this.to = to;
        }

        public static boolean isValidVariable(Variable variable) {
            return variable != null && (
                variable == PENDING || 
                variable == EARLIEST || 
                variable == CURRENT || 
                IntegerType.DEFAULT_INSTANCE.castableFrom(variable.getType())
            );
        } 

        public Variable getFrom() {
            return this.from;
        }

        public Variable getTo() {
            return this.to;
        }

        @Override
        public boolean isStateProcessable() {
            return false;
        }
    }

    public static class TransactionFilter implements Filter {
        public static final Variable ANY = new Variable(StringType.DEFAULT_INSTANCE, "any", true, "any");

        private final Variable senders;
        private final Variable recipients;

        public TransactionFilter(Variable senders, Variable recipients) {
            assert isValidVariable(senders);
            assert isValidVariable(recipients);
            this.senders = senders;
            this.recipients = recipients;
        }

        public static boolean isValidVariable(Variable variable) {
            return variable != null && (
                variable == ANY
                || variable.getType() instanceof ArrayType && IntegerType.DEFAULT_INSTANCE.castableFrom((ArrayType)variable.getType())
            );
        }

        public Variable getSenders() {
            return this.senders;
        }

        public Variable getRecipients() {
            return this.recipients;
        }

        @Override
        public boolean isStateProcessable() {
            return false;
        }
    }

    public static class SmartContractFilter implements Filter {
        public static final Variable ANY = new Variable(StringType.DEFAULT_INSTANCE, "any", true, "any");
        private final Variable addressList;

        public SmartContractFilter(Variable addressList) {
            assert isValidVariable(addressList);
            this.addressList = addressList;
        }

        public static boolean isValidVariable(Variable variable) {
            return variable != null && (
                variable == ANY
                || variable.getType() instanceof ArrayType && IntegerType.DEFAULT_INSTANCE.castableFrom((ArrayType)variable.getType())
            );
        }

        public Variable getAddressList() {
            return this.addressList;
        }

        @Override
        public boolean isStateProcessable() {
            return false;
        }
    }

    public static class LogEntryFilter implements Filter {

        @Override
        public boolean isStateProcessable() {
            return false;
        }

    }
}