package au.csiro.data61.aap.etl.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.filters.FilterPredicate;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * AddressListSpecification
 */
public class AddressListSpecification {
    private FilterPredicate<String> addressCheck;

    private AddressListSpecification(FilterPredicate<String> addressCheck) {
        this.addressCheck = addressCheck;
    }

    FilterPredicate<String> getAddressCheck() {
        return this.addressCheck;
    }

    public static AddressListSpecification ofAddress(String expectedAddress) {
        assert expectedAddress != null;
        final String lowerCaseAddress = expectedAddress.toLowerCase();
        return new AddressListSpecification(
            (state, address) -> address != null && lowerCaseAddress.equals(address.toLowerCase())
        );
    }

    public static AddressListSpecification ofAddresses(List<String> expectedAddresses) {
        assert expectedAddresses != null && expectedAddresses.stream().allMatch(Objects::nonNull);
        final List<String> lowerCaseAddresses = expectedAddresses.stream().map(ad -> ad.toLowerCase()).collect(Collectors.toList());
        return new AddressListSpecification(
            (state, address) -> address != null && lowerCaseAddresses.contains(address.toLowerCase())
        );
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
    public static AddressListSpecification ofVariableName(String name) {
        assert name != null;
        final ValueAccessor accessor = ValueAccessor.createVariableAccessor(name);
        return new AddressListSpecification(
            (state, address) -> {
                final Object value = accessor.getValue(state);
                if (value == null) {
                    return address == null;
                }
                else if (value instanceof String) {
                    return address.equals((String)value);
                }
                else if (List.class.isAssignableFrom(value.getClass())) {
                    try {
                        return ((List<String>)value).contains(address);
                    }
                    catch (Throwable cause) {
                        throw new ProgramException("Address list is not a string list.", cause);
                    } 
                }
                else {
                    return false;
                }
            }
        );
    }

}