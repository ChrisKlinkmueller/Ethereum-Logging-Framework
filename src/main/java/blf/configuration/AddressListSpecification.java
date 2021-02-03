package blf.configuration;

import blf.core.exceptions.ExceptionHandler;
import blf.core.interfaces.FilterPredicate;
import blf.core.values.ValueAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @SuppressWarnings("unused")
    public static AddressListSpecification ofAddress(String expectedAddress) {
        return new AddressListSpecification((state, address) -> expectedAddress.equalsIgnoreCase(address));
    }

    public static AddressListSpecification ofAddresses(List<String> expectedAddresses) {
        final List<String> lowerCaseAddresses = expectedAddresses.stream().map(String::toLowerCase).collect(Collectors.toList());
        return new AddressListSpecification((state, address) -> address != null && lowerCaseAddresses.contains(address.toLowerCase()));
    }

    @SuppressWarnings("unused")
    public static AddressListSpecification ofAddresses(String... expectedAddresses) {
        return ofAddresses(Arrays.asList(expectedAddresses));
    }

    public static AddressListSpecification ofAny() {
        return new AddressListSpecification((state, address) -> true);
    }

    public static AddressListSpecification ofEmpty() {
        return new AddressListSpecification((state, address) -> address == null);
    }

    public static AddressListSpecification ofVariableName(String name) {
        final ExceptionHandler exceptionHandler = new ExceptionHandler();

        final ValueAccessor accessor = ValueAccessor.createVariableAccessor(name);
        return new AddressListSpecification((state, address) -> {
            final Object value = accessor.getValue(state);

            if (value == null) {
                return address == null;
            }

            if (value instanceof String) {
                return address.equals(value);
            }

            try {
                // noinspection unchecked
                return ((List<String>) value).contains(address);
            } catch (Exception e) {
                exceptionHandler.handleException("Address list is not a string list.", e);
            }

            return false;
        });
    }

}
