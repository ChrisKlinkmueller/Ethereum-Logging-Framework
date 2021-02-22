/****************************************************** 
 *  Copyright 2018 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package blf.blockchains.hyperledger.helpers;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Set;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
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
     * @param hyperledgerProgramState   The current ProgramState of the BLF.
     */

    public UserContext(HyperledgerProgramState hyperledgerProgramState) {
        this.setName("User1");

        String certificate = null;
        try {
            certificate = Files.readString(Path.of("hyperledger/user1.crt"));
        } catch (IOException e) {
            ExceptionHandler.getInstance().handleException("Could not read user certificate", e);
        }

        // Get private key from file.
        PrivateKey privateKey = HyperledgerInstructionHelper.readPrivateKeyFromFile("hyperledger/user1.key");

        String mspName = hyperledgerProgramState.getMspName();
        this.setMspId(mspName);

        X509Enrollment x509Enrollment = new X509Enrollment(privateKey, certificate);
        this.setEnrollment(x509Enrollment);
    }

}
