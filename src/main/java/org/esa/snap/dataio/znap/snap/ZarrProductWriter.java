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
package org.esa.snap.dataio.znap.snap;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.snap.core.dataio.AbstractProductWriter;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.dataio.znap.zarr.ZarrDataType;
import org.esa.snap.dataio.znap.zarr.ZarrRoot;
import org.esa.snap.dataio.znap.zarr.ZarrWriter;
import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import ucar.ma2.InvalidRangeException;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ZarrProductWriter extends AbstractProductWriter {

    private final HashMap<String, ZarrWriter> zarrWriters = new HashMap<>();
    private Compressor _compressor;

    public ZarrProductWriter(final ZarrProductWriterPlugIn productWriterPlugIn) {
        super(productWriterPlugIn);
//        _compressor=Compressor.Null;
        _compressor = Compressor.Zip_L1;
    }

    @Override
    public void writeBandRasterData(Band sourceBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, ProductData sourceBuffer, ProgressMonitor pm) throws IOException {
        writeRasterData(sourceBand.getName(), sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceBuffer);
    }

    @Override
    public void flush() throws IOException {
//        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() throws IOException {
//        throw new RuntimeException("not implemented");
    }

    @Override
    public void deleteOutput() throws IOException {
        throw new RuntimeException("not implemented");
    }

    public void setCompressor(Compressor compressor) {
        _compressor = compressor;
    }

    @Override
    protected void writeProductNodesImpl() throws IOException {
        final Path output = convertToPath(getOutput());
        final ZarrRoot zarrRoot = new ZarrRoot(output);
        final Product sourceProduct = getSourceProduct();
        final Dimension preferredTileSize = ImageManager.getPreferredTileSize(sourceProduct);
        final int[] preferredChunks = {preferredTileSize.height, preferredTileSize.width}; // common data model manner { y, x }
        final List<RasterDataNode> rasterDataNodes = sourceProduct.getRasterDataNodes();
        for (RasterDataNode n : rasterDataNodes) {
            final int[] shape = {n.getRasterHeight(), n.getRasterWidth()}; // common data model manner { y, x }
            final String name = n.getName();
            int[] chunks;
            if (n.isSourceImageSet()) {
                final MultiLevelImage sourceImage = n.getSourceImage();
                chunks = new int[]{sourceImage.getTileHeight(), sourceImage.getTileWidth()}; // common data model manner { y, x }
            } else {
                chunks = Arrays.copyOf(preferredChunks, preferredChunks.length);
            }
            final ZarrWriter zarrWriter = zarrRoot.create(name, getZarrDataType(n), shape, chunks, getZarrFillValue(n), _compressor);
            zarrWriters.put(name, zarrWriter);
        }
        writeAllRasterDataWhichAreNotInstanceOfBand(sourceProduct);
    }

    /**
     * This implementation helper methods writes all raster data which are not of type {@link org.esa.snap.core.datamodel.Band Band}
     * of the given product. If a raster data is entirely loaded its data is written out immediately, if not, a raster's data raster is written out
     * line-by-line without producing any memory overhead.
     */
    private void writeAllRasterDataWhichAreNotInstanceOfBand(Product product) throws IOException {

        // for correct progress indication we need to collect
        // all bands which shall be written to the output

        final List<RasterDataNode> rasterDataNodes = product.getRasterDataNodes();
        for (RasterDataNode node : rasterDataNodes) {
            if (node instanceof Band) {
                continue;
            }
            if (shouldWrite(node)) {
                final String name = node.getName();
                if (node.hasRasterData()) {
                    final ProductData rasterData = node.getRasterData();
                    final int rasterWidth = node.getRasterWidth();
                    final int rasterHeight = node.getRasterHeight();
                    writeRasterData(name, 0, 0, rasterWidth, rasterHeight, rasterData);
                } else {
                    final MultiLevelImage sourceImage = node.getSourceImage();
                    final Point[] tileIndices = sourceImage.getTileIndices(
                            new Rectangle(0, 0, sourceImage.getWidth(), sourceImage.getHeight()));
                    for (final Point tileIndex : tileIndices) {
                        final Rectangle rect = sourceImage.getTileRect(tileIndex.x, tileIndex.y);
                        if (!rect.isEmpty()) {
                            final Raster data = sourceImage.getData(rect);
                            final ProductData rasterData = node.createCompatibleRasterData(rect.width, rect.height);
                            data.getDataElements(rect.x, rect.y, rect.width, rect.height, rasterData.getElems());
                            writeRasterData(name, rect.x, rect.y, rect.width, rect.height, rasterData);
                        }
                    }
                }
            }
        }
    }

    private void writeRasterData(String name, int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, ProductData sourceBuffer) throws IOException {
        final ZarrWriter zarrWriter = zarrWriters.get(name);
        final int[] to = {sourceOffsetY, sourceOffsetX}; // common data model manner { y, x }
        final int[] shape = {sourceHeight, sourceWidth};  // common data model manner { y, x }
        try {
            zarrWriter.write(sourceBuffer.getElems(), shape, to);
        } catch (InvalidRangeException e) {
            throw new IOException("Invalid range while writing raster '" + name + "'", e);
        }
    }

    private Number getZarrFillValue(RasterDataNode node) {
        final Double geophysicalNoDataValue = node.getGeophysicalNoDataValue();
        final ZarrDataType zarrDataType = getZarrDataType(node);
        if (zarrDataType == ZarrDataType.f8) {
            return geophysicalNoDataValue;
        }
        switch (zarrDataType) {
            case f4:
                return geophysicalNoDataValue.floatValue();
            case i1:
            case u1:
            case i2:
            case u2:
            case i4:
            case u4:
                return geophysicalNoDataValue.longValue();
            default:
                throw new IllegalStateException();
        }
    }

    private ZarrDataType getZarrDataType(RasterDataNode node) {
        final int geophysicalDataType = node.getDataType();
//        final int geophysicalDataType = node.getGeophysicalDataType();
        switch (geophysicalDataType) {
            case ProductData.TYPE_FLOAT64:
                return ZarrDataType.f8;
            case ProductData.TYPE_FLOAT32:
                return ZarrDataType.f4;
            case ProductData.TYPE_INT8:
                return ZarrDataType.i1;
            case ProductData.TYPE_INT16:
                return ZarrDataType.i2;
            case ProductData.TYPE_INT32:
                return ZarrDataType.i4;
            case ProductData.TYPE_UINT8:
                return ZarrDataType.u1;
            case ProductData.TYPE_UINT16:
                return ZarrDataType.u2;
            case ProductData.TYPE_UINT32:
                return ZarrDataType.u4;
            default:
                throw new IllegalStateException();
        }
    }

}
