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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class ZarrHeader {

    private final int[] chunks;
    private final Compressor compressor;
    private final String dtype;
    private final Number fill_value;
    private final String filters = null;
    private final String order = "C";
    private final int[] shape;
    private final int zarr_format = 2;

    public ZarrHeader(int[] shape, int[] chunks, String dtype, ByteOrder byteOrder, Number fill_value, Compressor compressor) {
        this.chunks = chunks;
        if (compressor == null || CompressorFactory.nullCompressor.equals(compressor)) {
            this.compressor = null;
        } else {
            this.compressor = compressor;
        }

        this.dtype = translateByteOrder(byteOrder) + dtype;
        this.fill_value = fill_value;
        this.shape = shape;
    }

    public int[] getChunks() {
        return chunks;
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public String getDtype() {
        return dtype;
    }

    public DataType getRawDataType() {
        return getRawDataType(dtype);
    }

    private static DataType getRawDataType(String dtype) {
        dtype = dtype.replace(">", "");
        dtype = dtype.replace("<", "");
        dtype = dtype.replace("|", "");
        return DataType.valueOf(dtype);
    }

    public ByteOrder getByteOrder() {
        return getByteOrder(this.dtype);
    }

    private static ByteOrder getByteOrder(String dtype) {
        if (dtype.startsWith(">")) {
            return ByteOrder.BIG_ENDIAN;
        } else if (dtype.startsWith("<")) {
            return ByteOrder.LITTLE_ENDIAN;
        } else if (dtype.startsWith("|")) {
            return ByteOrder.nativeOrder();
        }
        return ByteOrder.BIG_ENDIAN;
    }

    private String translateByteOrder(ByteOrder order) {
        if (order == null) {
            order = ByteOrder.nativeOrder();
        }
        if (ByteOrder.BIG_ENDIAN.equals(order)) {
            return ">";
        }
        return "<";
    }

    public Number getFill_value() {
        return fill_value;
    }

    public int[] getShape() {
        return shape;
    }

    static class ZarrHeaderSerializer extends StdSerializer<ZarrHeader> {

        protected ZarrHeaderSerializer() {
            super(ZarrHeader.class);
        }

        @Override
        public void serialize(ZarrHeader value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("chunks", value.getChunks());
            gen.writeFieldName("compressor");
            gen.writeObject(value.getCompressor());
            gen.writeStringField("dtype", value.getDtype());
            gen.writeObjectField("fill_value", value.getFill_value());
            gen.writeObjectField("filters", value.filters);
            gen.writeObjectField("order", value.order);
            gen.writeObjectField("shape", value.getShape());
            gen.writeNumberField("zarr_format", value.zarr_format);
            gen.writeEndObject();
        }
    }

    static class ZarrHeaderDeSerializer extends StdDeserializer<ZarrHeader> {

        protected ZarrHeaderDeSerializer() {
            super(ZarrHeader.class);
        }

        @Override
        public ZarrHeader deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            TreeNode root = codec.readTree(p);
            int[] shape = StreamSupport.stream(((ArrayNode) root.path("shape")).spliterator(), false).mapToInt(JsonNode::asInt).toArray();
            int[] chunks = StreamSupport.stream(((ArrayNode) root.path("chunks")).spliterator(), false).mapToInt(JsonNode::asInt).toArray();
            String dtype = ((JsonNode) root.path("dtype")).asText();
            JsonNode fillValueNode = (JsonNode) root.path("fill_value");
            final Number fill;
            if (fillValueNode.isLong()){
                fill = fillValueNode.longValue();
            } else if(fillValueNode.isFloat()) {
                fill = fillValueNode.floatValue();
            } else {
                fill = fillValueNode.asDouble();
            }

            Map<String, Object> compBean = codec.readValue(root.path("compressor").traverse(codec), HashMap.class);
            Compressor compressor;
            if (compBean == null) {
                compressor = CompressorFactory.nullCompressor;
            } else {
                compressor = CompressorFactory.create(compBean);
            }
            return new ZarrHeader(shape, chunks, getRawDataType(dtype).toString(), getByteOrder(dtype), fill, compressor);
        }

    }

    static void register(ObjectMapper objectMapper) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(ZarrHeader.class, new ZarrHeaderSerializer());
        simpleModule.addDeserializer(ZarrHeader.class, new ZarrHeaderDeSerializer());

        objectMapper.registerModules(simpleModule);
    }
}
