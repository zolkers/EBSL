/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.tools.pathfindersim.world.minecraft;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class NbtReader {
    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_FLOAT = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private static final int TAG_LONG_ARRAY = 12;

    private final DataInputStream input;

    private NbtReader(InputStream input) {
        this.input = new DataInputStream(input);
    }

    static Map<String, Object> readCompound(InputStream input) throws IOException {
        NbtReader reader = new NbtReader(input);
        int type = reader.input.readUnsignedByte();
        if (type != TAG_COMPOUND) {
            throw new IOException("NBT root is not a compound");
        }
        reader.input.readUTF();
        return reader.readCompoundPayload();
    }

    private Map<String, Object> readCompoundPayload() throws IOException {
        Map<String, Object> values = new HashMap<>();
        int type = input.readUnsignedByte();
        while (type != TAG_END) {
            String name = input.readUTF();
            values.put(name, readPayload(type));
            type = input.readUnsignedByte();
        }
        return values;
    }

    private Object readPayload(int type) throws IOException {
        return switch (type) {
            case TAG_BYTE -> input.readByte();
            case TAG_SHORT -> input.readShort();
            case TAG_INT -> input.readInt();
            case TAG_LONG -> input.readLong();
            case TAG_FLOAT -> input.readFloat();
            case TAG_DOUBLE -> input.readDouble();
            case TAG_BYTE_ARRAY -> readByteArray();
            case TAG_STRING -> input.readUTF();
            case TAG_LIST -> readList();
            case TAG_COMPOUND -> readCompoundPayload();
            case TAG_INT_ARRAY -> readIntArray();
            case TAG_LONG_ARRAY -> readLongArray();
            default -> throw new IOException("Unsupported NBT tag: " + type);
        };
    }

    private byte[] readByteArray() throws IOException {
        int length = input.readInt();
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return bytes;
    }

    private List<Object> readList() throws IOException {
        int elementType = input.readUnsignedByte();
        int length = input.readInt();
        List<Object> values = new ArrayList<>(Math.max(0, length));
        for (int index = 0; index < length; index++) {
            values.add(readPayload(elementType));
        }
        return values;
    }

    private int[] readIntArray() throws IOException {
        int length = input.readInt();
        int[] values = new int[length];
        for (int index = 0; index < length; index++) {
            values[index] = input.readInt();
        }
        return values;
    }

    private long[] readLongArray() throws IOException {
        int length = input.readInt();
        long[] values = new long[length];
        for (int index = 0; index < length; index++) {
            values[index] = input.readLong();
        }
        return values;
    }
}
