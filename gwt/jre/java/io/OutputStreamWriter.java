/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import core.util.Strings;

/**
 * OutputStreamWriter is a class for turning a character output stream into a
 * byte output stream. The conversion of Unicode characters to their byte
 * equivalents is determined by the converter used. By default, the encoding is
 * ISO8859_1 (ISO-Latin-1) but can be changed by calling the constructor which
 * takes an encoding.
 * 
 * @see InputStreamReader
 */

public class OutputStreamWriter extends Writer {

    private OutputStream out;
    String enc;

    /**
     * Constructs a new OutputStreamWriter using <code>out</code> as the
     * OutputStream to write converted characters to. The default character
     * encoding is used (see class description).
     * 
     * @param out
     *            the non-null OutputStream to write converted bytes to.
     */
    public OutputStreamWriter(OutputStream out) {
        super(out);
        this.out = out;
    }

    /**
     * Constructs a new OutputStreamWriter using <code>out</code> as the
     * OutputStream to write converted characters to and <code>enc</code> as
     * the character encoding. If the encoding cannot be found, an
     * UnsupportedEncodingException error is thrown.
     * 
     * @param out
     *            the non-null OutputStream to write converted bytes to.
     * @param enc
     *            the non-null String describing the desired character encoding.
     * 
     * @throws UnsupportedEncodingException
     *             if the encoding cannot be found.
     */
    public OutputStreamWriter(OutputStream out, final String enc)
            throws UnsupportedEncodingException {
        super(out);
        if (enc == null) {
            throw new NullPointerException();
        }
        this.out = out;
        this.enc = enc;
    }

    /**
     * Close this OutputStreamWriter. This implementation first flushes the
     * buffer and the target OutputStream. The OutputStream is then closed and
     * the resources for the buffer and converter are freed.
     * <p>
     * Only the first invocation of this method has any effect. Subsequent calls
     * do no work.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this
     *             OutputStreamWriter.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            out.flush();
            out.close();
        }
    }

    /**
     * Flush this OutputStreamWriter. This implementation ensures all buffered
     * bytes are written to the target OutputStream. After writing the bytes,
     * the target OutputStream is then flushed.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this
     *             OutputStreamWriter.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            out.flush();
        }
    }

    private void checkStatus() throws IOException {
    }

    /**
     * Answer the String which identifies the encoding used to convert
     * characters to bytes. The value <code>null</code> is returned if this
     * Writer has been closed.
     * 
     * @return the String describing the converter or null if this Writer is
     *         closed.
     */

    public String getEncoding() {
    	return enc;
    }

    /**
     * Writes <code>count</code> characters starting at <code>offset</code>
     * in <code>buf</code> to this Writer. The characters are immediately
     * converted to bytes by the character converter and stored in a local
     * buffer. If the buffer becomes full as a result of this write, this Writer
     * is flushed.
     * 
     * @param buf
     *            the non-null array containing characters to write.
     * @param offset
     *            offset in buf to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws IOException
     *             If this OutputStreamWriter has already been closed or some
     *             other IOException occurs.
     * @throws IndexOutOfBoundsException
     *             If offset or count is outside of bounds.
     */
    @Override
    public void write(char[] buf, int offset, int count) throws IOException {
    	char[] sb = new char[count];
    	for (int i=0; i<count; ++i)
    		sb[i] = buf[i+offset];
    	
    	String s = Strings.toString(buf);
    	
    	out.write(s.getBytes(enc));
    }

    /**
     * Writes out the character <code>oneChar</code> to this Writer. The
     * low-order 2 bytes are immediately converted to bytes by the character
     * converter and stored in a local buffer. If the buffer becomes full as a
     * result of this write, this Writer is flushed.
     * 
     * @param oneChar
     *            the character to write
     * 
     * @throws IOException
     *             If this OutputStreamWriter has already been closed or some
     *             other IOException occurs.
     */
    @Override
    public void write(int oneChar) throws IOException {
    	char[] cs = new char[] { (char)oneChar };
    	write(cs, 0, 1);
    }

    /**
     * Writes <code>count</code> characters starting at <code>offset</code>
     * in <code>str</code> to this Writer. The characters are immediately
     * converted to bytes by the character converter and stored in a local
     * buffer. If the buffer becomes full as a result of this write, this Writer
     * is flushed.
     * 
     * @param str
     *            the non-null String containing characters to write.
     * @param offset
     *            offset in str to retrieve characters
     * @param count
     *            maximum number of characters to write
     * 
     * @throws IOException
     *             If this OutputStreamWriter has already been closed or some
     *             other IOException occurs.
     * @throws IndexOutOfBoundsException
     *             If count is negative
     * @throws StringIndexOutOfBoundsException
     *             If offset is negative or offset + count is outside of bounds
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
        	char[] chars = new char[count];
        	str.getChars(0, count, chars, 0);

        	write(chars);
        }
    }
}