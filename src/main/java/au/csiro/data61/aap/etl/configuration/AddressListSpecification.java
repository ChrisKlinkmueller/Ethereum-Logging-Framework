package au.csiro.data61.aap.etl.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

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
        return new AddressListSpecification(
            (state, address) -> address != null && address.equals(expectedAddress)
        );
    }

    public static AddressListSpecification ofAddresses(List<String> expectedAddresses) {
        return new AddressListSpecification(
            (state, address) -> address != null && expectedAddresses.contains(address)
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