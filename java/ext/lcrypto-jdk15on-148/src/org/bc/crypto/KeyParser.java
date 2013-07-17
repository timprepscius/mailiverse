package org.bc.crypto;

import java.io.IOException;
import java.io.InputStream;

import org.bc.crypto.params.AsymmetricKeyParameter;

public interface KeyParser
{
    AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException;
}
