/*
 * Copyright 2010 Vrije Universiteit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ibis.smartsockets.util;

public class TransferUtils {

    public static void storeShort(short v, byte [] target, int off) {
        target[off++] = (byte)(0xff & (v >> 8));
        target[off]   = (byte)(0xff & v);
    }

    public static short readShort(byte [] source, int off) {
        return (short) (((source[off] & 0xff) << 8)  | (source[off+1] & 0xff));
    }

    public static void storeInt(int v, byte [] target, int off) {
        target[off++] = (byte)(0xff & (v >> 24));
        target[off++] = (byte)(0xff & (v >> 16));
        target[off++] = (byte)(0xff & (v >> 8));
        target[off]   = (byte)(0xff & v);
    }


    public static int readInt(byte [] source, int off) {
        return (((source[off]   & 0xff) << 24) |
                ((source[off+1] & 0xff) << 16) |
                ((source[off+2] & 0xff) << 8)  |
                 (source[off+3] & 0xff));
    }
}
