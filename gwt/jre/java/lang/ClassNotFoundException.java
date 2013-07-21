package java.lang;

public class ClassNotFoundException extends Exception {

	public ClassNotFoundException() 
	{
	}
	
	public ClassNotFoundException(String s) 
	{
		super(s);
	}

	public ClassNotFoundException(String s, Throwable ex) 
	{
		super(s,ex);
	}
}
