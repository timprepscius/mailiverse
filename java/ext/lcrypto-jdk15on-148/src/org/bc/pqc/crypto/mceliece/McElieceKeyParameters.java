package org.bc.pqc.crypto.mceliece;

import org.bc.crypto.params.AsymmetricKeyParameter;


public class McElieceKeyParameters
    extends AsymmetricKeyParameter
{
    private McElieceParameters params;

    public McElieceKeyParameters(
        boolean isPrivate,
        McElieceParameters params)
    {
        super(isPrivate);
        this.params = params;
    }


    public McElieceParameters getParameters()
    {
        return params;
    }

}
