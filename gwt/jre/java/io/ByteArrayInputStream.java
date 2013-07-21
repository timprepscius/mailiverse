/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.io;

public class ByteArrayInputStream extends InputStream {

	protected byte[] buf;
	protected int pos = 0;
	protected int start = 0;
	protected int mark = 0;
	protected int count;

	public ByteArrayInputStream(byte[] data) {
		this.buf = data;
		this.count = data.length;
	}

	public ByteArrayInputStream(byte[] data, int offset, int len) {
		this.buf = data;
		count = offset + len;
		this.mark = this.start = this.pos = offset;
	}

	@Override
	public int read() {
		return pos < count ? (buf[pos++] & 0xFF) : -1;
	}

	@Override
	public int available() {
		return count - pos;
	}

	@Override
	public long skip(long n) throws IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		pos += n;
		return pos;
	}

	@Override
	public void reset() throws IOException {
		pos = mark;
	}

	@Override
	public void mark(int readAheadLimit) {
		mark = pos;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

}
