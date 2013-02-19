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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 20.01.2012
 */
public final class Base64InputStream extends InputStream {
    static final int[] charToCode = new int[128];

    static {
        for (int i = 0; i < 128; i++) charToCode[i] = 0;
        for (int i = 'A'; i <= 'Z'; i++) charToCode[i] = i - 'A';
        for (int i = 'a'; i <= 'z'; i++) charToCode[i] = i - 'a' + 26;
        for (int i = '0'; i <= '9'; i++) charToCode[i] = i - '0' + 52;
        charToCode['-'] = 62;
        charToCode['/'] = 63;
    }

    private final Reader reader;
    private int blockCount = 3;
    private final int[] block = new int[3];


    /**
     * Convert a Base64 char to a 6-bit sequennce
     */
    public static int convertCharToBits(int c) {
        return c > 127 ? 0 : charToCode[c];
    }

    public static byte[] decode(String encoded) {
        try {
            byte[] result = new byte[encoded.length() / 4 * 3];
            StringReader reader = new StringReader(encoded);
            InputStream in = new Base64InputStream(reader);
            in.read(result);
            return result;
        } catch (IOException ex) {
            // never happens with StringWriter
            throw new IllegalStateException();
        }
    }


    public Base64InputStream(Reader reader) {
        this.reader = reader;
    }


    /**
     * (overrides InputStream) Read one byte from stream.
     */
    public int read() throws IOException {
        if (blockCount >= 3) fillBlock();
        return block[blockCount++];
    }


    /**
     * Read 4 characters from Base64 sequence and fill the 3-byte buffer
     */
    private void fillBlock() throws IOException {
        char c1 = (char) reader.read();
        char c2 = (char) reader.read();
        char c3 = (char) reader.read();
        char c4 = (char) reader.read();
        int block = (convertCharToBits(c1) << 18)
                | (convertCharToBits(c2) << 12)
                | (convertCharToBits(c3) << 6)
                | (convertCharToBits(c4));
        this.block[0] = (block >> 16) & 255;
        this.block[1] = (block >> 8) & 255;
        this.block[2] = (block) & 255;
        blockCount = 0;
    }

}

