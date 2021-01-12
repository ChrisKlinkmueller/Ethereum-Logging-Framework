package blf.blockchains.hyperledger.reader;

import blf.core.exceptions.ProgramException;
import blf.core.readers.DataReader;
import io.reactivex.annotations.NonNull;

/**
 * HyperledgerDataReader
 */
public class HyperledgerDataReader extends DataReader<HyperledgerClient, HyperledgerBlock, HyperledgerTransaction, HyperledgerLogEntry> {

    @Override
    public void connect(@NonNull Object[] parameters) throws ProgramException {

    }

}
