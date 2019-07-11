/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.zarr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public final class ZarrUtils {

    private static Gson gson;

    static String toJson(Object header) {
        return getGson().toJson(header);
    }

    static void toJson(Object header, Appendable writer) throws IOException {
        writer.append(toJson(header));
    }

    public static int[][] computeChunkIndices(int[] shape, int[] chunks, int[] bufferShape, int[] to) {
        final int depth = shape.length;
        int[] start = new int[depth];
        int[] end = new int[depth];
        int numChunks = 1;
        for (int i = 0; i < depth; i++) {
            final int staIdx = to[i] / chunks[i];
            final int endIdx = (to[i] + bufferShape[i] - 1) / chunks[i];
            numChunks *= (endIdx - staIdx + 1);
            start[i] = staIdx;
            end[i] = endIdx;
        }

        final int[][] chunkIndices = new int[numChunks][];

        final int[] currentIdx = Arrays.copyOf(start, depth);
        for (int i = 0; i < chunkIndices.length; i++) {
            chunkIndices[i] = Arrays.copyOf(currentIdx, depth);
            int depthIdx = depth - 1;
            while (depthIdx >= 0) {
                if (currentIdx[depthIdx] >= end[depthIdx]) {
                    currentIdx[depthIdx] = start[depthIdx];
                    depthIdx--;
                } else {
                    currentIdx[depthIdx]++;
                    depthIdx = -1;
                }
            }
        }
        return chunkIndices;
    }

    public static String createChunkFilename(int[] currentIdx) {
        StringBuilder sb = new StringBuilder();
        for (int aCurrentIdx : currentIdx) {
            sb.append(aCurrentIdx);
            sb.append(".");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static <T> T  fromJson(Reader reader, final Class<T> classOfType) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(reader, classOfType);
    }

    public static long computeSizeLong(int[] ints) {
        long count = 1;
        for (int i : ints) {
            count *= i;
        }
        return count;
    }

    public static int computeSizeInteger(int[] ints) {
        int count = 1;
        for (int i : ints) {
            count *= i;
        }
        return count;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
        }
        return gson;
    }
}
