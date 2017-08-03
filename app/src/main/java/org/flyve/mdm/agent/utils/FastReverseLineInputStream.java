package org.flyve.mdm.agent.utils;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      3/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FastReverseLineInputStream extends InputStream {

    private static final int MAX_LINE_BYTES = 1024 * 1024;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    private RandomAccessFile in;

    private long currentFilePos;

    private int bufferSize;
    private byte[] buffer;
    private int currentBufferPos;

    private int maxLineBytes;
    private byte[] currentLine;
    private int currentLineWritePos = 0;
    private int currentLineReadPos = 0;
    private boolean lineBuffered = false;

    public FastReverseLineInputStream(File file) throws IOException {
        this(file, DEFAULT_BUFFER_SIZE, MAX_LINE_BYTES);
    }

    public FastReverseLineInputStream(File file, int bufferSize, int maxLineBytes) throws IOException {
        this.maxLineBytes = maxLineBytes;
        in = new RandomAccessFile(file, "r");
        currentFilePos = file.length() - 1;
        in.seek(currentFilePos);
        if (in.readByte() == 0xA) {
            currentFilePos--;
        }
        currentLine = new byte[maxLineBytes];
        currentLine[0] = 0xA;

        this.bufferSize = bufferSize;
        buffer = new byte[bufferSize];
        fillBuffer();
        fillLineBuffer();
    }

    @Override
    public int read() throws IOException {
        if (currentFilePos <= 0 && currentBufferPos < 0 && currentLineReadPos < 0) {
            return -1;
        }

        if (!lineBuffered) {
            fillLineBuffer();
        }


        if (lineBuffered) {
            if (currentLineReadPos == 0) {
                lineBuffered = false;
            }
            return currentLine[currentLineReadPos--];
        }
        return 0;
    }

    private void fillBuffer() throws IOException {
        if (currentFilePos < 0) {
            return;
        }

        if (currentFilePos < bufferSize) {
            in.seek(0);
            in.read(buffer);
            currentBufferPos = (int) currentFilePos;
            currentFilePos = -1;
        } else {
            in.seek(currentFilePos);
            in.read(buffer);
            currentBufferPos = bufferSize - 1;
            currentFilePos = currentFilePos - bufferSize;
        }
    }

    private void fillLineBuffer() throws IOException {
        currentLineWritePos = 1;
        while (true) {

            // we've read all the buffer - need to fill it again
            if (currentBufferPos < 0) {
                fillBuffer();

                // nothing was buffered - we reached the beginning of a file
                if (currentBufferPos < 0) {
                    currentLineReadPos = currentLineWritePos - 1;
                    lineBuffered = true;
                    return;
                }
            }

            byte b = buffer[currentBufferPos--];

            // \n is found - line fully buffered
            if (b == 0xA) {
                currentLineReadPos = currentLineWritePos - 1;
                lineBuffered = true;
                break;

                // just ignore \r for now
            } else if (b == 0xD) {
                continue;
            } else {
                if (currentLineWritePos == maxLineBytes) {
                    throw new IOException("file has a line exceeding " + maxLineBytes
                            + " bytes; use constructor to pickup bigger line buffer");
                }

                // write the current line bytes in reverse order - reading from
                // the end will produce the correct line
                currentLine[currentLineWritePos++] = b;
            }
        }
    }
}
