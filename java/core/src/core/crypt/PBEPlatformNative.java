/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import core.exceptions.CryptoException;

public class PBEPlatformNative
{
    public native static byte[] generate (String password, byte[] salt, int iterationCount, int keyLength);
}
