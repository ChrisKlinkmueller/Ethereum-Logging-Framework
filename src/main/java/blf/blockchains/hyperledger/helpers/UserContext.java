package blf.blockchains.hyperledger.helpers;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Set;

import blf.core.exceptions.ExceptionHandler;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

/**
 * This helper class is the implementation for a Hyperledger Fabric User. The purpose of the UserContext in regards to
 * BLF is to fulfill the requirements for queries in the Java Hyperledger Fabric SDK. The UserContext must be instantiated
 * with a valid user name, a valid MspID, a valid user certificate and a valid user key to be accepted by a query, as
 * they are performed in the HyperledgerSmartContractFilterInstruction.
 *
 */

public class UserContext implements User, Serializable {

    private static final long serialVersionUID = 1L;
    protected String name;
    protected Set<String> roles;
    protected String account;
    protected String affiliation;
    protected Enrollment enrollment;
    protected String mspId;

    public void setName(String name) {
        this.name = name;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    /**
     * Constructs a UserContext by setting the name, mspID and enrollment.
     *
     * @param userName              The User name specification.
     * @param mspId                 The Msp ID specification.
     * @param privateKeyPath        The private key for the user enrollment.
     * @param certificatePath       The certificate for the user enrollment.
     */

    public UserContext(String userName, String mspId, String privateKeyPath, String certificatePath) {
        this.setName(userName);

        String certificate = null;
        try {
            certificate = Files.readString(Path.of(certificatePath));
        } catch (IOException e) {
            ExceptionHandler.getInstance().handleException("Could not read user certificate", e);
        }

        // Get private key from file.
        PrivateKey privateKey = HyperledgerInstructionHelper.readPrivateKeyFromFile(privateKeyPath);

        this.setMspId(mspId);

        X509Enrollment x509Enrollment = new X509Enrollment(privateKey, certificate);
        this.setEnrollment(x509Enrollment);
    }

}
