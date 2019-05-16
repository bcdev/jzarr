package org.esa.snap.dataio.znap.snap;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;
import static org.esa.snap.dataio.znap.zarr.ConstantsAndUtilsCF.*;
import static org.esa.snap.dataio.znap.zarr.ZarrConstantsAndUtils.*;

import com.bc.ceres.core.ProgressMonitor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.util.Guardian;
import org.esa.snap.dataio.znap.zarr.ZarrDataType;
import org.esa.snap.dataio.znap.zarr.ZarrHeader;
import org.esa.snap.dataio.znap.zarr.ZarrReadRoot;
import org.esa.snap.dataio.znap.zarr.ZarrReader;
import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import ucar.ma2.InvalidRangeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ZarrProductReader extends AbstractProductReader {


    private final Map<String, ZarrReader> zarrReaders = new HashMap<>();

    protected ZarrProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    public void readBandRasterData(Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
        Guardian.assertNotNull("destBand", destBand);
        Guardian.assertNotNull("destBuffer", destBuffer);

        if (destBuffer.getNumElems() < destWidth * destHeight) {
            throw new IllegalArgumentException("destination buffer too small");
        }
        if (destBuffer.getNumElems() > destWidth * destHeight) {
            throw new IllegalArgumentException("destination buffer too big");
        }
        final ZarrReader zarrReaderWriter = zarrReaders.get(destBand.getName());
        final Object bufferElems = destBuffer.getElems();

        try {
            zarrReaderWriter.read(bufferElems, new int[]{destHeight, destWidth}, new int[]{destOffsetY, destOffsetX});
        } catch (InvalidRangeException e) {
            throw new IOException("InvalidRangeException while reading raster data for band '" + destBand.getName() + "'", e);
        }
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final Path rootPath = convertToPath(getInput());
        final List<Path> headerPaths = Files.find(rootPath, 10, (path, basicFileAttributes) ->
                path.getFileName().toString().equals(FILENAME_DOT_ZARRAY)
        ).collect(Collectors.toList());

        final Map<String, ZarrHeader> headerMap = new TreeMap<>();
        final Map<String, Map<String, Object>> attributesMap = new TreeMap<>();
        for (Path zarrHeaderPath : headerPaths) {
            final Path rasterDir = zarrHeaderPath.getParent();
            final String rasterName = rasterDir.getFileName().toString();
            try (BufferedReader reader = Files.newBufferedReader(zarrHeaderPath)) {
                final ZarrHeader zarrHeader = fromJson(reader, ZarrHeader.class);
                headerMap.put(rasterName, zarrHeader);
            }
            final Path zarrAttribsPath = rasterDir.resolve(FILENAME_DOT_ZATTRS);
            if (Files.isReadable(zarrAttribsPath)) {
                try (BufferedReader reader = Files.newBufferedReader(zarrAttribsPath)) {
                    final Map<String, Object> attributes = fromJson(reader, Map.class);
                    attributesMap.put(rasterName, attributes);
                }
            }
        }

        final ZarrReadRoot zarrReadRoot = new ZarrReadRoot(rootPath);

        final Map<String, Object> productAttributes;
        try (BufferedReader reader = Files.newBufferedReader(rootPath.resolve(FILENAME_DOT_ZATTRS))) {
            productAttributes = fromJson(reader, Map.class);
        }
        final String productName = (String) productAttributes.get(PRODUCT_NAME);
        final String productType = (String) productAttributes.get(PRODUCT_TYPE);
        final String productDesc = (String) productAttributes.get(PRODUCT_DESC);
        final ProductData.UTC sensingStart = getTime(productAttributes, TIME_START, "sensing start", rootPath);
        final ProductData.UTC sensingStop = getTime(productAttributes, TIME_END, "sinsing stop", rootPath);
        final List<Map<String, Object>> product_metadata = (List) productAttributes.get(PRODUCT_METADATA);
        final ArrayList<MetadataElement> metadataElements = toMetadataElements(product_metadata);
        final Product product = new Product(productName, productType, this);
        product.setDescription(productDesc);
        product.setStartTime(sensingStart);
        product.setEndTime(sensingStop);
        for (MetadataElement metadataElement : metadataElements) {
            product.getMetadataRoot().addElement(metadataElement);
        }

        for (Map.Entry<String, ZarrHeader> zarrHeaderEntry : headerMap.entrySet()) {
            final String rasterName = zarrHeaderEntry.getKey();
            final ZarrHeader headerBean = zarrHeaderEntry.getValue();
            final Map<String, Object> attributes = attributesMap.get(rasterName);
            final int[] shape = headerBean.getShape();
            final String dtype = headerBean.getDtype();
            final ZarrDataType zarrDataType = ZarrDataType.valueOf(removeLeadingChar(dtype));
            final SnapDataType snapDataType = getSnapDataType(zarrDataType);
            final ZarrHeader.CompressorBean compressor = headerBean.getCompressor();
            final String compressorId = compressor != null ? compressor.getId() : null;
            final int[] chunks = headerBean.getChunks();
            final ZarrReader zarrReader = zarrReadRoot.create(rasterName, zarrDataType, shape, chunks, headerBean.getFill_value(), Compressor.getInstance(compressorId), null);
            final int width = shape[1];
            final int height = shape[0];
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
                product.addTiePointGrid(tiePointGrid);
            } else {
                zarrReaders.put(rasterName, zarrReader);
                final Band band = new Band(rasterName, snapDataType.getValue(), width, height);
                band.setSourceImage(ImageManager.getInstance().getSourceImage(band, 0));
                product.addBand(band);
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
        throw new IOException("readBandRasterDataImpl is not implemented now");
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

    private ProductData.UTC getTime(Map<String, Object> productAttributes, String attributeName, String fieldName, Path rootPath) throws IOException {
        try {
            return ISO8601ConverterWithMlliseconds.parse((String) productAttributes.get(attributeName));
        } catch (ParseException e) {
            throw new IOException("Unparseable " + fieldName + " while reading product '" + rootPath.toString() + "'", e);
        }
    }

    private String removeLeadingChar(String dtype) {
        dtype = dtype.replace(">", "");
        dtype = dtype.replace("<", "");
        dtype = dtype.replace("!", "");
        return dtype;
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

}
