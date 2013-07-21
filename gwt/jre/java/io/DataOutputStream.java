package java.io;

public class DataOutputStream extends FilterOutputStream {

	protected int written;

	public DataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void close() throws IOException
	{
		super.close();
	}
	
   public void write(byte buffer[], int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(); //$NON-NLS-1$
        }
        out.write(buffer, offset, count);
        written += count;
    }
   
   public void write(byte buffer[]) throws IOException
   {
	   write(buffer, 0, buffer.length);
   }
   
   /**
    * Writes a 32-bit int to this output stream. The resulting output is the 4
    * bytes, highest order first, of val.
    * 
    * @param val
    *            the int to be written.
    * 
    * @throws IOException
    *             If an error occurs attempting to write to this
    *             DataOutputStream.
    * 
    * @see DataInput#readInt()
    */
   public final void writeInt(int val) throws IOException {
       out.write((byte)(val >> 24));
       out.write((byte)(val >> 16));
       out.write((byte)(val >> 8));
       out.write((byte)(val));
       written += 4;
   }   
}
