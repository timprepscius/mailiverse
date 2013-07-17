package org.bc.crypto.modes.gcm;

import org.bc.util.Arrays;

public class BasicGCMMultiplier implements GCMMultiplier
{
    private byte[] H;

    public void init(byte[] H)
    {
        this.H = Arrays.clone(H);
    }

    public void multiplyH(byte[] x)
    {
        GCMUtil.multiply(x, H);
    }
}
