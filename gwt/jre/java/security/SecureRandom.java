package java.security;

import com.google.gwt.core.client.JsArrayInteger;

public class SecureRandom extends java.util.Random 
{
	  private static final double twoToThe24 = 16777216.0;
	  private static final double twoToThe31 = 2147483648.0;
	  private static final double twoToThe32 = 4294967296.0;
	  private static final double twoToTheMinus24 =
	    5.9604644775390625e-8;
	  private static final double twoToTheMinus26 =
	    1.490116119384765625e-8;
	  private static final double twoToTheMinus31 =
	    4.656612873077392578125e-10;
	  private static final double twoToTheMinus53 =
	    1.1102230246251565404236316680908203125e-16;
	
	protected native JsArrayInteger jsNextChunk() /*-{
		var words = $wnd.sjcl.random.randomWords(4);
		return words;
	}-*/;
	
	public int next(int numBits)
	{
		int numBytes = (numBits+7)/8;
		byte[] bytes = new byte[numBytes];
		nextBytes(bytes);
		
		int result=0;
		for (int i=0; i<numBytes; ++i)
		{
			result |= bytes[i] << (8 * i);
		}
		
		return result;
	}
	
	public void nextBytes(byte[] bytes)
	{
		final int chunkSize = 4;
		int chunks = (bytes.length+(chunkSize-1)) / chunkSize;
		
		int j=0;
		for (int i=0; i<chunks; ++i)
		{
			JsArrayInteger a = jsNextChunk();
			int k=0;
			while (j < bytes.length && k < chunkSize)
				bytes[j++] = (byte)(a.get(k++));
		}
	}
	
	public boolean nextBoolean ()
	{
		return next(1) != 0;
	}
	
	public int nextInt(int max) 
	{
		return (int) next(32) % max;
	}
	
	public long nextLong()
	{
		return ((long) next(32) << 32) + (long) next(32);		
	}
	
	public double nextDouble() {
		return 
			next(26) * twoToTheMinus26 +
			next(27) * twoToTheMinus53;
	}
	
	public float nextFloat() {
		return (float) (next(24) * twoToTheMinus24);
	}
	
	public static class Test {
	    public static double test() {  
	        // create random number generators  
	        SecureRandom rand = new SecureRandom();  
	  
	        // total number of sample points to take  
	        int numPoints = 10000;  
	  
	        int inRandCircle = 0;  
	        double xr, yr, zr;  
	        // xr and yr will be the random point  
	        // zr will be the calculated distance to the center  
	  
	        for(int i=0; i < numPoints; ++i)  
	        {  
	            xr = rand.nextDouble();  
	            yr = rand.nextDouble();  
	  
	            zr = (xr * xr) + (yr * yr);  
	            if(zr <= 1.0)  
	                inRandCircle++;  
	        }  
	  
	        // calculate the Pi approximations  
	        double randomPi = approxPi(inRandCircle, numPoints);  
	  
	        // calculate the % error  
	        double randomError = calcError(randomPi);  
	  
	        return randomError;
	    }  
	  
	    static double approxPi(int inCircle, int totalPoints)  
	    {  
	        return (double)inCircle / totalPoints * 4.0;  
	    }  
	  
	    static double calcError(double pi)  
	    {  
	        return (pi - Math.PI)/Math.PI * 100;  
	    }  
	}  	
}
