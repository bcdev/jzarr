package org.esa.snap.dataio.znap.snap;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.zarr.*;
import org.esa.snap.core.dataio.AbstractProductWriter;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.image.ImageManager;
import ucar.ma2.InvalidRangeException;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.bc.zarr.CFConstantsAndUtils.*;
import static org.esa.snap.core.util.StringUtils.isNotNullAndNotEmpty;
import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;

public class ZarrProductWriter extends AbstractProductWriter {

    private final HashMap<String, ArrayDataWriter> zarrWriters = new HashMap<>();
    private final ExecutorService executorService;
    private Compressor _compressor;
    private ZarrGroup zarrGroup;
    private int[] preferredChunks;

    public ZarrProductWriter(final ZarrProductWriterPlugIn productWriterPlugIn) {
        super(productWriterPlugIn);
        _compressor = CompressorFactory.create("zlib", 3);
        executorService = Executors.newFixedThreadPool(4);
    }

    @Override
    public void writeBandRasterData(Band sourceBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, ProductData sourceBuffer, ProgressMonitor pm) throws IOException {
        String name = sourceBand.getName();
        final ArrayDataWriter arrayDataWriter = zarrWriters.get(name);
        final int[] to = {sourceOffsetY, sourceOffsetX}; // common data model manner { y, x }
        final int[] shape = {sourceHeight, sourceWidth};  // common data model manner { y, x }
        Callable<Object> callable = () -> {
            try {
                final ProductData scaledBuffer;
                if (sourceBand.isLog10Scaled()) {
                    scaledBuffer = toScaledFloats(sourceBand, sourceBuffer);
                } else {
                    scaledBuffer = sourceBuffer;
                }
                arrayDataWriter.write(scaledBuffer.getElems(), shape, to);
                return null;
            } catch (InvalidRangeException e) {
                throw new IOException("Invalid range while writing raster '" + name + "'", e);
            }
        };
        executorService.submit(callable);
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

        zarrGroup = ZarrGroup.create(output, getProductAttributes(product));
        for (TiePointGrid tiePointGrid : product.getTiePointGrids()) {
            writeTiePointGrid(tiePointGrid);
        }
        for (Band band : product.getBands()) {
            initializeZarrBandWriter(band);
        }
    }

    static Map<String, Object> getProductAttributes(Product product) {
        final Map<String, Object> productAttributes = new HashMap<>();
        productAttributes.put(PRODUCT_NAME, product.getName());
        productAttributes.put(PRODUCT_TYPE, product.getProductType());
        productAttributes.put(PRODUCT_DESC, product.getDescription());
        productAttributes.put(TIME_START, ISO8601ConverterWithMlliseconds.format(product.getStartTime())); // "time_coverage_start"
        productAttributes.put(TIME_END, ISO8601ConverterWithMlliseconds.format(product.getEndTime())); // "time_coverage_end"

        productAttributes.put(PRODUCT_METADATA, product.getMetadataRoot().getElements());

        if (product.getAutoGrouping() != null) {
            productAttributes.put(DATASET_AUTO_GROUPING, product.getAutoGrouping().toString());
        }
        if (isNotNullAndNotEmpty(product.getQuicklookBandName())) {
            productAttributes.put(QUICKLOOK_BAND_NAME, product.getQuicklookBandName());
        }
        return productAttributes;
    }

    static void collectBandAttributes(Band band, Map<String, Object> attributes) {
        // TODO: 21.07.2019 SE -- units for bandwidth, wavelength, solarFlux
        if (band.getSpectralBandwidth() > 0) {
            attributes.put(BANDWIDTH, band.getSpectralBandwidth());
        }
        if (band.getSpectralWavelength() > 0) {
            attributes.put(WAVELENGTH, band.getSpectralWavelength());
        }
        if (band.getSolarFlux() > 0) {
            attributes.put(SOLAR_FLUX, band.getSolarFlux());
        }
        if ((float) band.getSpectralBandIndex() >= 0) {
            attributes.put(SPECTRAL_BAND_INDEX, band.getSpectralBandIndex());
        }

        collectSampleCodingAttributes(band, attributes);
    }

    static ProductData toScaledFloats(Band sourceBand, ProductData sourceBuffer) {
        ProductData scaledBuffer;
        scaledBuffer = ProductData.createInstance(ProductData.TYPE_FLOAT32, sourceBuffer.getNumElems());
        for (int i = 0; i < sourceBuffer.getNumElems(); i++) {
            double rawDouble = sourceBuffer.getElemDoubleAt(i);
            scaledBuffer.setElemDoubleAt(i, sourceBand.scale(rawDouble));
        }
        return scaledBuffer;
    }

    private static void collectSampleCodingAttributes(Band band, Map<String, Object> attributes) {
        final SampleCoding sampleCoding = band.getSampleCoding();
        if (sampleCoding == null) {
            return;
        }

        attributes.put(ZnapConstantsAndUtils.NAME_SAMPLE_CODING, sampleCoding.getName());

        final boolean indexBand = band.isIndexBand();
        final boolean flagBand = band.isFlagBand();
        if (!(indexBand || flagBand)) {
            Logger.getGlobal().warning("Band references a SampleCoding but this is neither an IndexCoding nor a FlagCoding.");
            return;
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

    private static Number getZarrFillValue(RasterDataNode node) {
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

    private static ZarrDataType getZarrDataType(RasterDataNode node) {
        if (node.isLog10Scaled()) {
            return ZarrDataType.f4;
        }
        final int dataType = node.getDataType();
//        final int dataType = node.getGeophysicalDataType();
        switch (dataType) {
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
        final Map<String, Object> attributes = new HashMap<>();
        collectRasterAttributes(tiePointGrid, attributes);
        attributes.put(OFFSET_X, tiePointGrid.getOffsetX());
        attributes.put(OFFSET_Y, tiePointGrid.getOffsetY());
        attributes.put(SUBSAMPLING_X, tiePointGrid.getSubSamplingX());
        attributes.put(SUBSAMPLING_Y, tiePointGrid.getSubSamplingY());
        final int discontinuity = tiePointGrid.getDiscontinuity();
        if (discontinuity != TiePointGrid.DISCONT_NONE) {
            attributes.put(DISCONTINUITY, discontinuity);
        }
        trimChunks(chunks, shape);
        final ArrayDataWriter arrayDataWriter = zarrGroup.createWriter(name, getZarrDataType(tiePointGrid), shape, chunks, getZarrFillValue(tiePointGrid), _compressor, attributes);
        try {
            arrayDataWriter.write(gridData.getElems(), shape, new int[]{0, 0});
        } catch (InvalidRangeException e) {
            throw new IOException("Invalid range while writing raster '" + name + "'", e);
        }
    }

    private void trimChunks(int[] chunks, int[] shape) {
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] < chunks[i]) {
                chunks[i] = shape[i];
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
        trimChunks(chunks, shape);
        final ArrayDataWriter arrayDataWriter = zarrGroup.createWriter(name, getZarrDataType(band), shape, chunks, getZarrFillValue(band), _compressor, getBandAttributes(band));
        zarrWriters.put(name, arrayDataWriter);
    }

    static Map<String, Object> getBandAttributes(Band band) {
        final Map<String, Object> bandAttributes = new HashMap<>();
        collectRasterAttributes(band, bandAttributes);
        collectBandAttributes(band, bandAttributes);
        return bandAttributes;
    }

    private ProductData readTiePointGridData(TiePointGrid tiePointGrid) throws IOException {
        final int gridWidth = tiePointGrid.getGridWidth();
        final int gridHeight = tiePointGrid.getGridHeight();
        ProductData productData = tiePointGrid.createCompatibleRasterData(gridWidth, gridHeight);
        getSourceProduct().getProductReader().readTiePointGridRasterData(tiePointGrid, 0, 0, gridWidth, gridHeight, productData,
                ProgressMonitor.NULL);
        return productData;
    }

    static void collectRasterAttributes(RasterDataNode rdNode, Map<String, Object> attributes) {

        final int nodeDataType = rdNode.getDataType();

        if (rdNode.getDescription() != null) {
            attributes.put(LONG_NAME, rdNode.getDescription());
        }
        if (rdNode.getUnit() != null) {
            attributes.put(UNITS, tryFindUnitString(rdNode.getUnit()));
        }
        if (ProductData.isUIntType(nodeDataType)) {
            attributes.put(UNSIGNED, String.valueOf(true));
        }
        collectNoDataValue(rdNode, attributes);

        if (isNotNullAndNotEmpty(rdNode.getValidPixelExpression())) {
            attributes.put(VALID_PIXEL_EXPRESSION, rdNode.getValidPixelExpression());
        }
    }

    private static void collectNoDataValue(RasterDataNode rdNode, Map<String, Object> attributes) {
        int nodeDataType = rdNode.getDataType();
        // TODO: 22.07.2019 SE -- shall log10 scaled really be prohibited
        final Number noDataValue;
        if (!rdNode.isLog10Scaled()) {

            if (rdNode.getScalingFactor() != 1.0) {
                attributes.put(SCALE_FACTOR, rdNode.getScalingFactor());
            }
            if (rdNode.getScalingOffset() != 0.0) {
                attributes.put(ADD_OFFSET, rdNode.getScalingOffset());
            }
            noDataValue = rdNode.getNoDataValue();
        } else {
            // scaling information is not written anymore for log10 scaled bands
            // instead we always write geophysical values
            // we do this because log scaling is not supported by NetCDF-CF conventions
            noDataValue = rdNode.getGeophysicalNoDataValue();
        }
        if (noDataValue.doubleValue() != 0.0) {
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
        if (rdNode.isNoDataValueUsed()) {
            attributes.put(NO_DATA_VALUE_USED, rdNode.isNoDataValueUsed());
        }
    }

    // TODO: 21.07.2019 SE implement geo coding
    // implement geocoding part for product
    // and for Band too

//    private void encodeGeoCoding(NFileWriteable ncFile, Band band, Product product, NVariable variable) throws IOException {
//        final GeoCoding geoCoding = band.getGeoCoding();
//        if (!geoCoding.equals(product.getSceneGeoCoding())) {
//            if (geoCoding instanceof TiePointGeoCoding) {
//                final TiePointGeoCoding tpGC = (TiePointGeoCoding) geoCoding;
//                final String[] names = new String[2];
//                names[LON_INDEX] = tpGC.getLonGrid().getName();
//                names[LAT_INDEX] = tpGC.getLatGrid().getName();
//                final String value = StringUtils.arrayToString(names, " ");
//                variable.addAttribute(GEOCODING, value);
//            } else {
//                if (geoCoding instanceof CrsGeoCoding) {
//                    final CoordinateReferenceSystem crs = geoCoding.getMapCRS();
//                    final double[] matrix = new double[6];
//                    final MathTransform transform = geoCoding.getImageToMapTransform();
//                    if (transform instanceof AffineTransform) {
//                        ((AffineTransform) transform).getMatrix(matrix);
//                    }
//                    final String crsName = "crs_" + band.getName();
//                    final NVariable crsVariable = ncFile.addScalarVariable(crsName, DataType.INT);
//                    crsVariable.addAttribute("wkt", crs.toWKT());
//                    crsVariable.addAttribute("i2m", StringUtils.arrayToCsv(matrix));
//                    variable.addAttribute(GEOCODING, crsName);
//                }
//            }
//        }
//    }
}
