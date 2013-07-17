package org.bc.pqc.crypto.gmss;

import org.bc.crypto.params.AsymmetricKeyParameter;

public class GMSSKeyParameters
    extends AsymmetricKeyParameter
{
    private GMSSParameters params;

    public GMSSKeyParameters(
        boolean isPrivate,
        GMSSParameters params)
    {
        super(isPrivate);
        this.params = params;
    }

    public GMSSParameters getParameters()
    {
        return params;
    }
}