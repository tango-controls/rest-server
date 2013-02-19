/*
 * The main contributor to this project is Institute of Materials Research,
 * Helmholtz-Zentrum Geesthacht,
 * Germany.
 *
 * This project is a contribution of the Helmholtz Association Centres and
 * Technische Universitaet Muenchen to the ESS Design Update Phase.
 *
 * The project's funding reference is FKZ05E11CG1.
 *
 * Copyright (c) 2012. Institute of Materials Research,
 * Helmholtz-Zentrum Geesthacht,
 * Germany.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package hzg.wpn.util.base64;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 20.01.2012
 */
public final class Base64OutputStream extends OutputStream {
    static final String codeToChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-/";

    private final Writer writer;
    private int blockCount = 0;
    private final int[] block = new int[3];

    /**
     * Convert a 6-bit sequennce to a base64 char
     */
    public static int convertBitsToChar(int sixBit) {
        return codeToChar.charAt(sixBit);
    }

    public static String encode(byte[] data) {
        try {
            StringWriter writer = new StringWriter();
            OutputStream out = new Base64OutputStream(writer);
            out.write(data);
            out.close();
            return writer.toString();
        } catch (IOException ex) {
            // never happens with StringWriter
            throw new IllegalStateException();
        }
    }


    public Base64OutputStream(Writer writer) {
        this.writer = writer;
    }


    /**
     * (overrides OutputStream) Write one byte to stream.
     */
    public void write(int bytes) throws IOException {
        block[blockCount++] = bytes & 255;
        if (blockCount >= 3) dumpBlock();
    }

    /**
     * (overrides OutputStream) Dump the uncomplete byte triplet.
     */
    public void close() throws IOException {
        if (blockCount != 0) dumpBlock();
        super.close();
    }


    /**
     * Write 3 bytes as 4 characters in Base64 format
     */
    private void dumpBlock() throws IOException {
        int block = (this.block[0] << 16) | (this.block[1] << 8) | (this.block[2]);
        writer.write(convertBitsToChar((block >> 18) & 63));
        writer.write(convertBitsToChar((block >> 12) & 63));
        // if we are writing uncomplete block, fill trailer chars with '='s
        // that makes item IDs stand out and is rather useful during debugging
        writer.write(blockCount < 2 ? '=' : convertBitsToChar((block >> 6) & 63));
        writer.write(blockCount < 3 ? '=' : convertBitsToChar((block) & 63));
        blockCount = 0;
    }

}
