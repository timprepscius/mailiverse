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

import core.util.Streams;

/**
 * InputStreamReader is class for turning a byte Stream into a character Stream.
 * Data read from the source input stream is converted into characters by either
 * a default or provided character converter. By default, the encoding is
 * assumed to ISO8859_1. The InputStreamReader contains a buffer of bytes read
 * from the source input stream and converts these into characters as needed.
 * The buffer size is 8K.
 * 
 * @see OutputStreamWriter
 */
public class InputStreamReader extends StringReader {

    /**
     * Constructs a new InputStreamReader on the InputStream <code>in</code>.
     * Now character reading can be filtered through this InputStreamReader.
     * This constructor assumes the default conversion of ISO8859_1
     * (ISO-Latin-1).
     * 
     * @param in
     *            the InputStream to convert to characters.
     * @throws IOException 
     */
    public InputStreamReader(InputStream in) throws IOException {
        super(Streams.readFullyString(in, "UTF-8"));
    }
    
    public InputStreamReader(InputStream in, String enc) throws IOException
    {
        super(Streams.readFullyString(in, enc));
    }

}