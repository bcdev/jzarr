package org.esa.snap.dataio.znap.snap;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;
import static com.bc.zarr.CFConstantsAndUtils.*;
import static com.bc.zarr.ZarrUtils.*;
import static com.bc.zarr.ZarrConstants.*;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.zarr.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.IndexCoding;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.image.ResolutionLevel;
import ucar.ma2.InvalidRangeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ZarrProductReader extends AbstractProductReader {

    protected ZarrProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    public void readBandRasterData(Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
        throw new IllegalStateException("Data is provided by images");
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final Path rootPath = convertToPath(getInput());
        final ZarrGroup rootGroup = ZarrGroup.open(rootPath);
        final List<Path> headerPaths = Files.find(rootPath, 10, (path, basicFileAttributes) ->
                path.getFileName().toString().equals(FILENAME_DOT_ZARRAY)
        ).collect(Collectors.toList());

        final List<String> rasterNames = new ArrayList<>();
        final Map<String, Map<String, Object>> attributesMap = new TreeMap<>();
        for (Path zarrHeaderPath : headerPaths) {
            final Path rasterDir = zarrHeaderPath.getParent();
            final String rasterName = rasterDir.getFileName().toString();
            rasterNames.add(rasterName);
            final Path zarrAttribsPath = rasterDir.resolve(FILENAME_DOT_ZATTRS);
            if (Files.isReadable(zarrAttribsPath)) {
                try (BufferedReader reader = Files.newBufferedReader(zarrAttribsPath)) {
                    final Map<String, Object> attributes = fromJson(reader, Map.class);
                    attributesMap.put(rasterName, attributes);
                }
            }
        }

        final Map<String, Object> productAttributes;
        try (BufferedReader reader = Files.newBufferedReader(rootPath.resolve(FILENAME_DOT_ZATTRS))) {
            productAttributes = fromJson(reader, Map.class);
        }
        final String productName = (String) productAttributes.get(PRODUCT_NAME);
        final String productType = (String) productAttributes.get(PRODUCT_TYPE);
        final String productDesc = (String) productAttributes.get(PRODUCT_DESC);
        final ProductData.UTC sensingStart = getTime(productAttributes, TIME_START, rootPath); // "time_coverage_start"
        final ProductData.UTC sensingStop = getTime(productAttributes, TIME_END, rootPath); // "time_coverage_end"
        final List<Map<String, Object>> product_metadata = (List) productAttributes.get(PRODUCT_METADATA);
        final ArrayList<MetadataElement> metadataElements = toMetadataElements(product_metadata);
        final Product product = new Product(productName, productType, this);
        product.setDescription(productDesc);
        product.setStartTime(sensingStart);
        product.setEndTime(sensingStop);
        if (productAttributes.get(DATASET_AUTO_GROUPING) != null) {
            product.setAutoGrouping((String) productAttributes.get(DATASET_AUTO_GROUPING));
        }
        if (productAttributes.get(QUICKLOOK_BAND_NAME) != null) {
            product.setQuicklookBandName(((String) productAttributes.get(QUICKLOOK_BAND_NAME)).trim());
        }
        for (MetadataElement metadataElement : metadataElements) {
            product.getMetadataRoot().addElement(metadataElement);
        }

        for (String rasterName : rasterNames) {
            final ZarrReader zarrReader = rootGroup.createReader(rasterName);

            final int[] shape = zarrReader.getShape();
            final int[] chunks = zarrReader.getChunks();
            final ZarrDataType zarrDataType = zarrReader.getDataType();

            final SnapDataType snapDataType = getSnapDataType(zarrDataType);
            final int width = shape[1];
            final int height = shape[0];

            final Map<String, Object> attributes = attributesMap.get(rasterName);

            if (attributes != null && attributes.containsKey(OFFSET_X)) {
                final double offsetX = (double) attributes.get(OFFSET_X);
                final double offsetY = (double) attributes.get(OFFSET_Y);
                final double subSamplingX = (double) attributes.get(SUBSAMPLING_X);
                final double subSamplingY = (double) attributes.get(SUBSAMPLING_Y);
                final float[] dataBuffer = new float[width * height];
                try {
                    zarrReader.read(dataBuffer, shape, new int[]{0, 0});
                } catch (InvalidRangeException e) {
                    throw new IOException("InvalidRangeException while reading tie point raster '" + rasterName + "'", e);
                }
                final TiePointGrid tiePointGrid = new TiePointGrid(rasterName, width, height, offsetX, offsetY, subSamplingX, subSamplingY, dataBuffer);
                if (attributes.containsKey(DISCONTINUITY)) {
                    tiePointGrid.setDiscontinuity(((Number) attributes.get(DISCONTINUITY)).intValue());
                }
                product.addTiePointGrid(tiePointGrid);
            } else {
                final Band band = new Band(rasterName, snapDataType.getValue(), width, height);
                product.addBand(band);
                apply(attributes, band);
                final ZarrOpImage zarrOpImage = new ZarrOpImage(band, shape, chunks, zarrReader, ResolutionLevel.MAXRES);
                band.setSourceImage(zarrOpImage);
            }
        }
        product.setFileLocation(rootPath.toFile());
        product.setProductReader(this);
        product.getSceneRasterSize();
        product.setModified(false);
        return product;
    }

    @Override
    protected void readBandRasterDataImpl(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight,
            ProductData destBuffer, ProgressMonitor pm) throws IOException {
        throw new IllegalStateException("Data is provided by images");
    }

    static void apply(Map<String, Object> attributes, Band band) {
        applySampleCodings(attributes, band);
        if (attributes.get(LONG_NAME) != null) {
            band.setDescription((String) attributes.get(LONG_NAME));
        }
        if (attributes.get(UNITS) != null) {
            band.setUnit((String) attributes.get(UNITS));
        }
        if (attributes.get(SCALE_FACTOR) != null) {
            band.setScalingFactor(((Number) attributes.get(SCALE_FACTOR)).doubleValue());
        }
        if (attributes.get(ADD_OFFSET) != null) {
            band.setScalingOffset(((Number) attributes.get(ADD_OFFSET)).doubleValue());
        }
        if (getNoDataValue(attributes) != null) {
            band.setNoDataValue(getNoDataValue(attributes).doubleValue());
        }
        if (attributes.get(NO_DATA_VALUE_USED) != null) {
            band.setNoDataValueUsed((Boolean) attributes.get(NO_DATA_VALUE_USED));
        }
        if (attributes.get(VALID_PIXEL_EXPRESSION) != null) {
            band.setValidPixelExpression((String) attributes.get(VALID_PIXEL_EXPRESSION));
        }
        // TODO: 21.07.2019 SE -- units for bandwidth, wavelength, solarFlux
        if (attributes.get(BANDWIDTH) != null) {
            band.setSpectralBandwidth(((Number)attributes.get(BANDWIDTH)).floatValue());
        }
        if (attributes.get(WAVELENGTH)!= null) {
             band.setSpectralWavelength(((Number) attributes.get(WAVELENGTH)).floatValue());
        }
        if (attributes.get(SOLAR_FLUX) != null) {
            band.setSolarFlux(((Number) attributes.get(SOLAR_FLUX)).floatValue());
        }
        if (attributes.get(SPECTRAL_BAND_INDEX) != null) {
            band.setSpectralBandIndex(((Number) attributes.get(SPECTRAL_BAND_INDEX)).intValue());
        }
    }

    private static void applySampleCodings(Map<String, Object> attributes, Band band) {
        final String rasterName = band.getName();
        final List<String> flagMeanings = (List) attributes.get(FLAG_MEANINGS);
        if (flagMeanings != null) {

            final List<Double> flagMasks = (List<Double>) attributes.get(FLAG_MASKS);
            final List<Double> flagValues = (List<Double>) attributes.get(FLAG_VALUES);

            FlagCoding flagCoding = null;
            IndexCoding indexCoding = null;
            final Product product = band.getProduct();
            if (flagMasks != null) {
                flagCoding = new FlagCoding(getSampleCodingName(attributes, rasterName));
                band.setSampleCoding(flagCoding);
                product.getFlagCodingGroup().add(flagCoding);
            } else if (flagValues != null) {
                indexCoding = new IndexCoding(getSampleCodingName(attributes, rasterName));
                band.setSampleCoding(indexCoding);
                product.getIndexCodingGroup().add(indexCoding);
            } else {
                Logger.getGlobal().warning(
                        "Raster attributes for '" + rasterName
                                + "' contains the attribute '" + FLAG_MEANINGS
                                + "' but neither an attribute '" + FLAG_MASKS
                                + "' nor an attribute '" + FLAG_VALUES + "'."
                );
                return;
            }
            for (int i = 0; i < flagMeanings.size(); i++) {
                final String meaningName = flagMeanings.get(i);
                final String description = getFlagDescription(attributes, i);
                if (flagMasks != null) {
                    final int flagMask = flagMasks.get(i).intValue();
                    if (flagValues != null) {
                        flagCoding.addFlag(meaningName, flagMask, flagValues.get(i).intValue(), description);
                    } else {
                        flagCoding.addFlag(meaningName, flagMask, description);
                    }
                } else {
                    indexCoding.addIndex(meaningName, flagValues.get(i).intValue(), description);
                }
            }
        }
    }

    private static String getFlagDescription(Map<String, Object> attributes, int pos) {
        if (attributes.containsKey(FLAG_DESCRIPTIONS)) {
            return (String) ((List) attributes.get(FLAG_DESCRIPTIONS)).get(pos);
        }
        return null;
    }

    private static String getSampleCodingName(Map<String, Object> attributes, String rasterName) {
        final String sampleCodingName;
        if (attributes.containsKey(NAME_SAMPLE_CODING)) {
            sampleCodingName = (String) attributes.get(NAME_SAMPLE_CODING);
        } else {
            sampleCodingName = rasterName;
        }
        return sampleCodingName;
    }

    private static ArrayList<MetadataElement> toMetadataElements(List<Map<String, Object>> product_metadata) {
        final ArrayList<MetadataElement> snapElements = new ArrayList<>();
        for (Map<String, Object> jsonElement : product_metadata) {
            final MetadataElementGson element = toGsonMetadataElement(jsonElement);
            final MetadataElement snapElement = new MetadataElement(element.name);
            snapElement.setDescription(element.description);
            addAttributes(snapElement, element.attributes);
            addElements(snapElement, element.elements);
            snapElements.add(snapElement);
        }
        return snapElements;
    }

    private static void addElements(MetadataElement parentElement, ProductNodeGroupGson<MetadataElementGson> elements) {
        if (elements != null) {
            for (MetadataElementGson node : elements.nodeList.nodes) {
                final MetadataElement childElement = new MetadataElement(node.name);
                childElement.setDescription(node.description);
                addAttributes(childElement, node.attributes);
                addElements(childElement, node.elements);
                parentElement.addElement(childElement);
            }
        }
    }

    private static void addAttributes(MetadataElement snapElement, ProductNodeGroupGson<MetadataAttributeGson> attributes) {
        if (attributes != null) {
            for (MetadataAttributeGson node : attributes.nodeList.nodes) {
                final MetadataAttribute attribute;
                if (node.dataType == ProductData.TYPE_ASCII) {
                    attribute = new MetadataAttribute(node.name, node.dataType);
                } else {
                    attribute = new MetadataAttribute(node.name, node.dataType, node.numElems);
                }
                attribute.setDescription(node.description);
                attribute.setReadOnly(node.readOnly);
                attribute.setSynthetic(node.synthetic);
                attribute.setUnit(node.unit);
                final List<Double> data = (List<Double>) node.data._array;
                if (ProductData.TYPE_ASCII == node.dataType) {
                    if (data.size() > 0) {
                        final byte[] bytes = new byte[data.size()];
                        for (int i = 0; i < data.size(); i++) {
                            Double c = data.get(i);
                            bytes[i] = c.byteValue();
                        }
                        attribute.getData().setElems(bytes);
                    }
                } else {
                    for (int i = 0; i < data.size(); i++) {
                        Double v = data.get(i);
                        attribute.getData().setElemDoubleAt(i, v);
                    }
                }
                if (node.dataType == ProductData.TYPE_UINT32 && node.numElems == 3 && "utc".equalsIgnoreCase(node.unit)) {
                    final ProductData pd = attribute.getData();
                    attribute.setData(new ProductData.UTC(pd.getElemIntAt(0), pd.getElemIntAt(1), pd.getElemIntAt(2)));
                }
                snapElement.addAttribute(attribute);
            }
        }
    }

    private static MetadataElementGson toGsonMetadataElement(Map<String, Object> jsonElement) {
        final Gson gson = new GsonBuilder().create();
        final String str = gson.toJson(jsonElement);
        final StringReader reader = new StringReader(str);
        return gson.fromJson(reader, MetadataElementGson.class);
    }

    private ProductData.UTC getTime(Map<String, Object> productAttributes, String attributeName, Path rootPath) throws IOException {
        try {
            return ISO8601ConverterWithMlliseconds.parse((String) productAttributes.get(attributeName));
        } catch (ParseException e) {
            throw new IOException("Unparseable " + attributeName + " while reading product '" + rootPath.toString() + "'", e);
        }
    }

    private static class ProductDataGson {

        protected Object _array;
        protected int _type;
    }

    private static class ProductNodeGson {

        protected String name;
        protected String description;
    }

    private static class MetadataAttributeGson extends ProductNodeGson {

        protected int dataType;
        protected int numElems;
        protected ProductDataGson data;
        protected boolean readOnly;
        protected String unit;
        protected boolean synthetic;
    }

    private static class ProductNodeListGson<T extends ProductNodeGson> {

        protected List<T> nodes;
        protected List<T> removedNodes;
    }

    private static class ProductNodeGroupGson<T extends ProductNodeGson> extends ProductNodeGson {

        protected ProductNodeListGson<T> nodeList;
        protected boolean takingOverNodeOwnership;
    }

    private static class MetadataElementGson extends ProductNodeGson {

        protected ProductNodeGroupGson<MetadataElementGson> elements;
        protected ProductNodeGroupGson<MetadataAttributeGson> attributes;
    }

    private static Number getNoDataValue(Map<String, Object> attributes) {
        Object attribute = attributes.get(FILL_VALUE);
        if (attribute == null) {
            attribute = attributes.get(MISSING_VALUE);
        }
        if (attribute != null) {
            return (Number) attribute;
        }
        return null;
    }
}
