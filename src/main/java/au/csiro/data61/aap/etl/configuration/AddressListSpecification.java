package au.csiro.data61.aap.etl.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * AddressListSpecification
 */
public class AddressListSpecification {
    private BiPredicate<ProgramState, String> addressCheck;

    private AddressListSpecification(BiPredicate<ProgramState, String> addressCheck) {
        this.addressCheck = addressCheck;
    }

    BiPredicate<ProgramState, String> getAddressCheck() {
        return this.addressCheck;
    }

    public static AddressListSpecification ofAddress(String expectedAddress) {
        final String lowerCaseAddress = expectedAddress.toLowerCase();
        return new AddressListSpecification(
            (state, address) -> address != null && lowerCaseAddress.equals(address.toLowerCase())
        );
    }

    public static AddressListSpecification ofAddresses(List<String> expectedAddresses) {
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

}