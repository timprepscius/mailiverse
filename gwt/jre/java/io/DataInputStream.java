package java.io;

public class DataInputStream extends FilterInputStream
{
	public DataInputStream(InputStream in) 
	{
		super(in);
	}

	
    /**
     * Reads bytes from the source stream into the byte array
     * <code>buffer</code>. The number of bytes actually read is returned.
     * 
     * @param buffer
     *            the buffer to read bytes into
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    @Override
    public final int read(byte[] buffer) throws IOException {
        return in.read(buffer, 0, buffer.length);
    }
	
	/**
     * Reads a 32-bit integer value from this stream.
     * 
     * @return the next <code>int</code> value from the source stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#writeInt(int)
     */
    public final int readInt() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        }
        
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
    }
    
    /**
     * Read at most <code>length</code> bytes from this DataInputStream and
     * stores them in byte array <code>buffer</code> starting at
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If a problem occurs reading from this DataInputStream.
     * 
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    @Override
    public final int read(byte[] buffer, int offset, int length)
            throws IOException {
        return in.read(buffer, offset, length);
    }
	
}
