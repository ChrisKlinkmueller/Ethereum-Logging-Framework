package blf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import blf.core.exceptions.ProgramException;
import blf.core.interfaces.FilterPredicate;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

/**
 * AddressListSpecification
 */
public class AddressListSpecification {
    private final FilterPredicate<String> addressCheck;

    private AddressListSpecification(FilterPredicate<String> addressCheck) {
        this.addressCheck = addressCheck;
    }

    FilterPredicate<String> getAddressCheck() {
        return this.addressCheck;
    }

    public static AddressListSpecification ofAddress(@NonNull String expectedAddress) {
        return new AddressListSpecification((state, address) -> expectedAddress.equalsIgnoreCase(address));
    }

    public static AddressListSpecification ofAddresses(@NonNull List<String> expectedAddresses) {
        final List<String> lowerCaseAddresses = expectedAddresses.stream().map(String::toLowerCase).collect(Collectors.toList());
        return new AddressListSpecification((state, address) -> address != null && lowerCaseAddresses.contains(address.toLowerCase()));
    }

    public static AddressListSpecification ofAddresses(String... expectedAddresses) {
        return ofAddresses(Arrays.asList(expectedAddresses));
    }

    public static AddressListSpecification ofAny() {
        return new AddressListSpecification((state, address) -> true);
    }

    public static AddressListSpecification ofEmpty() {
        return new AddressListSpecification((state, address) -> address == null);
    }

    @SuppressWarnings("unchecked")
    public static AddressListSpecification ofVariableName(@NonNull String name) {
        final ValueAccessor accessor = ValueAccessor.createVariableAccessor(name);
        return new AddressListSpecification((state, address) -> {
            final Object value = accessor.getValue(state);
            if (value == null) {
                return address == null;
            } else if (value instanceof String) {
                return address.equals(value);
            } else if (List.class.isAssignableFrom(value.getClass())) {
                try {
                    return ((List<String>) value).contains(address);
                } catch (Exception cause) {
                    throw new ProgramException("Address list is not a string list.", cause);
                }
            } else {
                return false;
            }
        });
    }

}
