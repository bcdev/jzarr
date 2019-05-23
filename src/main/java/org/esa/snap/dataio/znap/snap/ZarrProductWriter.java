package org.esa.snap.dataio.znap.snap;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.snap.core.dataio.AbstractProductWriter;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.SampleCoding;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.dataio.znap.zarr.ZarrDataType;
import org.esa.snap.dataio.znap.zarr.ZarrWriteRoot;
import org.esa.snap.dataio.znap.zarr.ZarrWriter;
import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import ucar.ma2.InvalidRangeException;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;
import static org.esa.snap.dataio.znap.zarr.ConstantsAndUtilsCF.*;

public class ZarrProductWriter extends AbstractProductWriter {

    private final HashMap<String, ZarrWriter> zarrWriters = new HashMap<>();
    private Compressor _compressor;
    private ZarrWriteRoot zarrWriteRoot;
    private int[] preferredChunks;

    public ZarrProductWriter(final ZarrProductWriterPlugIn productWriterPlugIn) {
        super(productWriterPlugIn);
//        _compressor = Compressor.Null;
        _compressor = Compressor.Zip_L1;
    }

    @Override
    public void writeBandRasterData(Band sourceBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, ProductData sourceBuffer, ProgressMonitor pm) throws IOException {
        String name = sourceBand.getName();
        final ZarrWriter zarrReaderWriter = zarrWriters.get(name);
        final int[] to = {sourceOffsetY, sourceOffsetX}; // common data model manner { y, x }
        final int[] shape = {sourceHeight, sourceWidth};  // common data model manner { y, x }
        try {
            zarrReaderWriter.write(sourceBuffer.getElems(), shape, to);
        } catch (InvalidRangeException e) {
            throw new IOException("Invalid range while writing raster '" + name + "'", e);
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
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
        final Product product = getSourceProduct();
        final Dimension preferredTileSize = ImageManager.getPreferredTileSize(product);

        // common data model manner { y, x }
        preferredChunks = new int[]{preferredTileSize.height, preferredTileSize.width};

        final HashMap<String, Object> productAttributes = new HashMap<>();
        productAttributes.put(PRODUCT_NAME, product.getName());
        productAttributes.put(PRODUCT_TYPE, product.getProductType());
        productAttributes.put(PRODUCT_DESC, product.getDescription());
        productAttributes.put(TIME_START, ISO8601ConverterWithMlliseconds.format(product.getStartTime()));
        productAttributes.put(TIME_END, ISO8601ConverterWithMlliseconds.format(product.getEndTime()));

        productAttributes.put(PRODUCT_METADATA, product.getMetadataRoot().getElements());

        zarrWriteRoot = new ZarrWriteRoot(output, productAttributes);
        for (TiePointGrid tiePointGrid : product.getTiePointGrids()) {
            writeTiePointGrid(tiePointGrid);
        }
        for (Band band : product.getBands()) {
            initializeZarrBandWriter(band);
        }
    }

    static Map<? extends String, ?> createCfConformSampleCodingAttributes(Band band) {
        final HashMap<String, Object> attributes = new HashMap<>();
        final SampleCoding sampleCoding = band.getSampleCoding();
        if (sampleCoding == null) {
            return attributes;
        }

        final boolean indexBand = band.isIndexBand();
        final boolean flagBand = band.isFlagBand();
        if (!(indexBand || flagBand)) {
            Logger.getGlobal().warning("Band references a SampleCoding but this is neither an IndexCoding nor an FlagCoding.");
            return attributes;
        }


        final int numCodings = sampleCoding.getNumAttributes();
        final String[] names = new String[numCodings];
        final String[] descriptions = new String[numCodings];
        final int[] masks = new int[numCodings];
        final int[] values = new int[numCodings];
        final MetadataAttribute[] codingAtts = sampleCoding.getAttributes();
        boolean alsoFlagValues = false;
        for (int i = 0; i < codingAtts.length; i++) {
            MetadataAttribute attribute = codingAtts[i];
            names[i] = attribute.getName();
            descriptions[i] = attribute.getDescription();
            final ProductData data = attribute.getData();
            if (indexBand) {
                values[i] = data.getElemInt();
            } else {
                masks[i] = data.getElemInt();
                final boolean twoElements = data.getNumElems() == 2;
                values[i] = twoElements ? data.getElemIntAt(1) : data.getElemInt();
                alsoFlagValues = alsoFlagValues || twoElements;
            }
        }
        if (indexBand) {
            attributes.put(FLAG_MEANINGS, names);
            attributes.put(FLAG_VALUES, values);
        } else {
            attributes.put(FLAG_MEANINGS, names);
            attributes.put(FLAG_MASKS, masks);
            if (alsoFlagValues) {
                attributes.put(FLAG_VALUES, values);
            }
        }
        if (containsNotEmptyStrings(descriptions, true)) {
            attributes.put(FLAG_DESCRIPTIONS, descriptions);
        }
        return attributes;
    }

    private static boolean containsNotEmptyStrings(final String[] strings, final boolean trim) {
        if (strings != null || strings.length > 0) {
            if (trim) {
                for (int i = 0; i < strings.length; i++) {
                    final String string = strings[i];
                    strings[i] = string != null ? string.trim() : string;
                }
            }
            for (final String string : strings) {
                if (string != null && !string.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void writeTiePointGrid(TiePointGrid tiePointGrid) throws IOException {
        final int[] shape = {tiePointGrid.getGridHeight(), tiePointGrid.getGridWidth()}; // common data model manner { y, x }
        final String name = tiePointGrid.getName();
        final ProductData gridData;
        final boolean hasData = tiePointGrid.getData() != null;
        if (hasData) {
            gridData = tiePointGrid.getData();
        } else {
            gridData = readTiePointGridData(tiePointGrid);
        }
        int[] chunks = Arrays.copyOf(preferredChunks, preferredChunks.length);
        final Map<String, Object> attributes = createCfConformRasterAttributes(tiePointGrid);
        attributes.put(OFFSET_X, tiePointGrid.getOffsetX());
        attributes.put(OFFSET_Y, tiePointGrid.getOffsetY());
        attributes.put(SUBSAMPLING_X, tiePointGrid.getSubSamplingX());
        attributes.put(SUBSAMPLING_Y, tiePointGrid.getSubSamplingY());
        final int discontinuity = tiePointGrid.getDiscontinuity();
        if (discontinuity != TiePointGrid.DISCONT_NONE) {
            attributes.put(DISCONTINUITY, discontinuity);
        }
        trimChunks(chunks, shape);
        final ZarrWriter zarrReaderWriter = zarrWriteRoot.create(name, getZarrDataType(tiePointGrid), shape, chunks, getZarrFillValue(tiePointGrid), _compressor, attributes);
        try {
            zarrReaderWriter.write(gridData.getElems(), shape, new int[]{0, 0});
        } catch (InvalidRangeException e) {
            throw new IOException("Invalid range while writing raster '" + name + "'", e);
        }
    }

    private void trimChunks(int[] chunks, int[] shape) {
        for (int i = 0; i < shape.length; i++) {
            int shape_i = shape[i];
            final int chunk_i = chunks[i];
            if (shape_i < chunk_i) {
                chunks[i] = shape_i;
            }
        }
    }

    private void initializeZarrBandWriter(Band band) throws IOException {
        final int[] shape = {band.getRasterHeight(), band.getRasterWidth()}; // common data model manner { y, x }
        final String name = band.getName();
        int[] chunks;
        if (band.isSourceImageSet()) {
            final MultiLevelImage sourceImage = band.getSourceImage();
            chunks = new int[]{sourceImage.getTileHeight(), sourceImage.getTileWidth()}; // common data model manner { y, x }
        } else {
            chunks = Arrays.copyOf(preferredChunks, preferredChunks.length);
        }
        final Map<String, Object> attributes = createCfConformRasterAttributes(band);
        attributes.putAll(createCfConformSampleCodingAttributes(band));
        trimChunks(chunks, shape);
        final ZarrWriter zarrReaderWriter = zarrWriteRoot.create(name, getZarrDataType(band), shape, chunks, getZarrFillValue(band), _compressor, attributes);
        zarrWriters.put(name, zarrReaderWriter);
    }

    private ProductData readTiePointGridData(TiePointGrid tiePointGrid) throws IOException {
        final int gridWidth = tiePointGrid.getGridWidth();
        final int gridHeight = tiePointGrid.getGridHeight();
        ProductData productData = tiePointGrid.createCompatibleRasterData(gridWidth, gridHeight);
        getSourceProduct().getProductReader().readTiePointGridRasterData(tiePointGrid, 0, 0, gridWidth, gridHeight, productData,
                                                                         ProgressMonitor.NULL);
        return productData;
    }

    private Map<String, Object> createCfConformRasterAttributes(RasterDataNode node) {
        final HashMap<String, Object> attributes = new HashMap<>();

        final String description = node.getDescription();
        if (description != null) {
            attributes.put(LONG_NAME, description);
        }
        String unit = node.getUnit();
        if (unit != null) {
            unit = tryFindUnitString(unit);
            attributes.put(UNITS, unit);
        }
        final int nodeDataType = node.getDataType();

        if (ProductData.isUIntType(nodeDataType)) {
            attributes.put(UNSIGNED, String.valueOf(true));
        }

        Number noDataValue;
        if (!node.isLog10Scaled()) {
            final double scalingFactor = node.getScalingFactor();
            if (scalingFactor != 1.0) {
                attributes.put(SCALE_FACTOR, scalingFactor);
            }
            final double scalingOffset = node.getScalingOffset();
            if (scalingOffset != 0.0) {
                attributes.put(ADD_OFFSET, scalingOffset);
            }
            noDataValue = node.getNoDataValue();
        } else {
            // scaling information is not written anymore for log10 scaled bands
            // instead we always write geophysical values
            // we do this because log scaling is not supported by NetCDF-CF conventions
            noDataValue = node.getGeophysicalNoDataValue();
        }
        if (node.isNoDataValueUsed()) {
            if (ProductData.isIntType(nodeDataType)) {
                final long longValue = noDataValue.longValue();
                if (ProductData.isUIntType(nodeDataType)) {
                    attributes.put(FILL_VALUE, longValue & 0xffffffffL);
                } else {
                    attributes.put(FILL_VALUE, longValue);
                }
            } else if (ProductData.TYPE_FLOAT64 == nodeDataType) {
                attributes.put(FILL_VALUE, noDataValue.doubleValue());
            } else {
                attributes.put(FILL_VALUE, noDataValue.floatValue());
            }
        }


        return attributes;
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
