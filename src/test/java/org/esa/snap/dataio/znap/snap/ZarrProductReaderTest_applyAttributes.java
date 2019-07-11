package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;
import static com.bc.zarr.CFConstantsAndUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZarrProductReaderTest_applyAttributes {

    private Band band;

    @Before
    public void setUp() throws Exception {
        final Product product = new Product("PName", "PType", 1234, 2345);
        band = product.addBand("BName", ProductData.TYPE_INT32);

    }

    @Test
    public void applySingelBitFlagCoding() {
        // preparation
        final HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(FLAG_MEANINGS, Arrays.asList("m1", "m2", "m3"));
        final double m1 = 0b000010; // should be int but gson library returns parsed doubles
        final double m2 = 0b001000; // should be int but gson library returns parsed doubles
        final double m3 = 0b010000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_MASKS, Arrays.asList(m1, m2, m3));

        // execution
        ZarrProductReader.apply(attributes, band);

        // verification
        assertThat(band.isFlagBand(), is(true));
        final FlagCoding flagCoding = band.getFlagCoding();
        assertThat(flagCoding.getName(), is("BName"));
        assertThat(band.getProduct().getFlagCodingGroup().contains(flagCoding), is(true));

        assertThat(flagCoding.getNumAttributes(), is(3));
        assertThat(flagCoding.getFlagNames(), is(new String[]{"m1", "m2", "m3"}));

        assertThat(flagCoding.getFlagMask("m1"), is(2));
        assertThat(flagCoding.getFlagMask("m2"), is(8));
        assertThat(flagCoding.getFlagMask("m3"), is(16));
        assertThat(flagCoding.getFlag("m1").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m2").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getAttribute("m1").getData().getNumElems(), is(1));
        assertThat(flagCoding.getAttribute("m2").getData().getNumElems(), is(1));
        assertThat(flagCoding.getAttribute("m3").getData().getNumElems(), is(1));
    }

    @Test
    public void applySingelBitFlagCoding_withDescriptions() {
        // preparation
        final HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(FLAG_MEANINGS, Arrays.asList("m1", "m2", "m3"));
        final double m1 = 0b000010; // should be int but gson library returns parsed doubles
        final double m2 = 0b001000; // should be int but gson library returns parsed doubles
        final double m3 = 0b010000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_MASKS, Arrays.asList(m1, m2, m3));
        attributes.put(FLAG_DESCRIPTIONS, Arrays.asList("d1", "d2", "d3"));

        // execution
        ZarrProductReader.apply(attributes, band);

        // verification
        assertThat(band.isFlagBand(), is(true));
        final FlagCoding flagCoding = band.getFlagCoding();
        assertThat(flagCoding.getName(), is("BName"));
        assertThat(band.getProduct().getFlagCodingGroup().contains(flagCoding), is(true));

        assertThat(flagCoding.getNumAttributes(), is(3));
        assertThat(flagCoding.getFlagNames(), is(new String[]{"m1", "m2", "m3"}));

        assertThat(flagCoding.getFlagMask("m1"), is(2));
        assertThat(flagCoding.getFlagMask("m2"), is(8));
        assertThat(flagCoding.getFlagMask("m3"), is(16));
        assertThat(flagCoding.getFlag("m1").getDescription(), is("d1"));
        assertThat(flagCoding.getFlag("m2").getDescription(), is("d2"));
        assertThat(flagCoding.getFlag("m3").getDescription(), is("d3"));
        assertThat(flagCoding.getAttribute("m1").getData().getNumElems(), is(1));
        assertThat(flagCoding.getAttribute("m2").getData().getNumElems(), is(1));
        assertThat(flagCoding.getAttribute("m3").getData().getNumElems(), is(1));
    }

    @Test
    public void applySingelBitFlagCoding_withDescriptionsAndSampleCodingName() {
        // preparation
        final HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(FLAG_MEANINGS, Arrays.asList("m1", "m2", "m3"));
        final double m1 = 0b000010; // should be int but gson library returns parsed doubles
        final double m2 = 0b001000; // should be int but gson library returns parsed doubles
        final double m3 = 0b010000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_MASKS, Arrays.asList(m1, m2, m3));
        attributes.put(FLAG_DESCRIPTIONS, Arrays.asList("d1", "d2", "d3"));
        attributes.put(NAME_SAMPLE_CODING, "SCName");

        // execution
        ZarrProductReader.apply(attributes, band);

        // verification
        assertThat(band.isFlagBand(), is(true));
        final FlagCoding flagCoding = band.getFlagCoding();
        assertThat(flagCoding.getName(), is("SCName"));
        assertThat(band.getProduct().getFlagCodingGroup().contains(flagCoding), is(true));

        assertThat(flagCoding.getNumAttributes(), is(3));
        assertThat(flagCoding.getFlagNames(), is(new String[]{"m1", "m2", "m3"}));

        assertThat(flagCoding.getFlagMask("m1"), is(2));
        assertThat(flagCoding.getFlagMask("m2"), is(8));
        assertThat(flagCoding.getFlagMask("m3"), is(16));
        assertThat(flagCoding.getFlag("m1").getDescription(), is("d1"));
        assertThat(flagCoding.getFlag("m2").getDescription(), is("d2"));
        assertThat(flagCoding.getFlag("m3").getDescription(), is("d3"));
        assertThat(flagCoding.getAttribute("m1").getData().getNumElems(), is(1));
        assertThat(flagCoding.getAttribute("m2").getData().getNumElems(), is(1));
        assertThat(flagCoding.getAttribute("m3").getData().getNumElems(), is(1));
    }

    @Test
    public void applyBitFieldFlagCoding() {
        // preparation
        final HashMap<String, Object> attributes = new HashMap<>();
        final double m1 = 0b000001; // should be int but gson library returns parsed doubles
        final double m2 = 0b000110; // should be int but gson library returns parsed doubles
        final double m3 = 0b111000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_MEANINGS, Arrays.asList("m1",
                                                    "m2_1", "m2_2", "m2_3",
                                                    "m3_1", "m3_2", "m3_3", "m3_4", "m3_5", "m3_6", "m3_7"));
        attributes.put(FLAG_MASKS, Arrays.asList(m1,
                                                 m2, m2, m2,
                                                 m3, m3, m3, m3, m3, m3, m3));
        final double v1 = 0b000001; // should be int but gson library returns parsed doubles
        final double v2_1 = 0b000010; // should be int but gson library returns parsed doubles
        final double v2_2 = 0b000100; // should be int but gson library returns parsed doubles
        final double v2_3 = 0b000110; // should be int but gson library returns parsed doubles
        final double v3_1 = 0b001000; // should be int but gson library returns parsed doubles
        final double v3_2 = 0b010000; // should be int but gson library returns parsed doubles
        final double v3_3 = 0b011000; // should be int but gson library returns parsed doubles
        final double v3_4 = 0b100000; // should be int but gson library returns parsed doubles
        final double v3_5 = 0b101000; // should be int but gson library returns parsed doubles
        final double v3_6 = 0b110000; // should be int but gson library returns parsed doubles
        final double v3_7 = 0b111000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_VALUES, Arrays.asList(v1,
                                                  v2_1, v2_2, v2_3,
                                                  v3_1, v3_2, v3_3, v3_4, v3_5, v3_6, v3_7));

        // execution
        ZarrProductReader.apply(attributes, band);

        // verification
        assertThat(band.isFlagBand(), is(true));
        final FlagCoding flagCoding = band.getFlagCoding();
        assertThat(flagCoding.getName(), is("BName"));
        assertThat(band.getProduct().getFlagCodingGroup().contains(flagCoding), is(true));

        assertThat(flagCoding.getNumAttributes(), is(11));
        assertThat(flagCoding.getFlagNames(), is(new String[]{"m1",
                                                              "m2_1", "m2_2", "m2_3",
                                                              "m3_1", "m3_2", "m3_3", "m3_4", "m3_5", "m3_6", "m3_7"}));

        assertThat(flagCoding.getFlagMask("m1"), is(1));
        assertThat(flagCoding.getFlagMask("m2_1"), is(6));
        assertThat(flagCoding.getFlagMask("m2_2"), is(6));
        assertThat(flagCoding.getFlagMask("m2_3"), is(6));
        assertThat(flagCoding.getFlagMask("m3_1"), is(56));
        assertThat(flagCoding.getFlagMask("m3_2"), is(56));
        assertThat(flagCoding.getFlagMask("m3_3"), is(56));
        assertThat(flagCoding.getFlagMask("m3_4"), is(56));
        assertThat(flagCoding.getFlagMask("m3_5"), is(56));
        assertThat(flagCoding.getFlagMask("m3_6"), is(56));
        assertThat(flagCoding.getFlagMask("m3_7"), is(56));
        assertThat(flagCoding.getFlag("m1").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m2_1").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m2_2").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m2_3").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_1").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_2").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_3").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_4").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_5").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_6").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getFlag("m3_7").getDescription(), isEmptyOrNullString());
        assertThat(flagCoding.getAttribute("m1").getData().getElems(), is(new int[]{1, 1}));
        assertThat(flagCoding.getAttribute("m2_1").getData().getElems(), is(new int[]{6, 2}));
        assertThat(flagCoding.getAttribute("m2_2").getData().getElems(), is(new int[]{6, 4}));
        assertThat(flagCoding.getAttribute("m2_3").getData().getElems(), is(new int[]{6, 6}));
        assertThat(flagCoding.getAttribute("m3_1").getData().getElems(), is(new int[]{56, 8}));
        assertThat(flagCoding.getAttribute("m3_2").getData().getElems(), is(new int[]{56, 16}));
        assertThat(flagCoding.getAttribute("m3_3").getData().getElems(), is(new int[]{56, 24}));
        assertThat(flagCoding.getAttribute("m3_4").getData().getElems(), is(new int[]{56, 32}));
        assertThat(flagCoding.getAttribute("m3_5").getData().getElems(), is(new int[]{56, 40}));
        assertThat(flagCoding.getAttribute("m3_6").getData().getElems(), is(new int[]{56, 48}));
        assertThat(flagCoding.getAttribute("m3_7").getData().getElems(), is(new int[]{56, 56}));
    }

    @Test
    public void applyBitFieldFlagCoding_withDescriptions() {
        // preparation
        final HashMap<String, Object> attributes = new HashMap<>();
        final double m1 = 0b000001; // should be int but gson library returns parsed doubles
        final double m2 = 0b000110; // should be int but gson library returns parsed doubles
        final double m3 = 0b111000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_MEANINGS, Arrays.asList("m1",
                                                    "m2_1", "m2_2", "m2_3",
                                                    "m3_1", "m3_2", "m3_3", "m3_4", "m3_5", "m3_6", "m3_7"));
        attributes.put(FLAG_MASKS, Arrays.asList(m1,
                                                 m2, m2, m2,
                                                 m3, m3, m3, m3, m3, m3, m3));
        final double v1 = 0b000001; // should be int but gson library returns parsed doubles
        final double v2_1 = 0b000010; // should be int but gson library returns parsed doubles
        final double v2_2 = 0b000100; // should be int but gson library returns parsed doubles
        final double v2_3 = 0b000110; // should be int but gson library returns parsed doubles
        final double v3_1 = 0b001000; // should be int but gson library returns parsed doubles
        final double v3_2 = 0b010000; // should be int but gson library returns parsed doubles
        final double v3_3 = 0b011000; // should be int but gson library returns parsed doubles
        final double v3_4 = 0b100000; // should be int but gson library returns parsed doubles
        final double v3_5 = 0b101000; // should be int but gson library returns parsed doubles
        final double v3_6 = 0b110000; // should be int but gson library returns parsed doubles
        final double v3_7 = 0b111000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_VALUES, Arrays.asList(v1,
                                                  v2_1, v2_2, v2_3,
                                                  v3_1, v3_2, v3_3, v3_4, v3_5, v3_6, v3_7));
        attributes.put(FLAG_DESCRIPTIONS, Arrays.asList("d1",
                                                        "d2_1", "d2_2", "d2_3",
                                                        "d3_1", "d3_2", "d3_3", "d3_4", "d3_5", "d3_6", "d3_7"));

        // execution
        ZarrProductReader.apply(attributes, band);

        // verification
        assertThat(band.isFlagBand(), is(true));
        final FlagCoding flagCoding = band.getFlagCoding();
        assertThat(flagCoding.getName(), is("BName"));
        assertThat(band.getProduct().getFlagCodingGroup().contains(flagCoding), is(true));

        assertThat(flagCoding.getNumAttributes(), is(11));
        assertThat(flagCoding.getFlagNames(), is(new String[]{"m1",
                                                              "m2_1", "m2_2", "m2_3",
                                                              "m3_1", "m3_2", "m3_3", "m3_4", "m3_5", "m3_6", "m3_7"}));

        assertThat(flagCoding.getFlagMask("m1"), is(1));
        assertThat(flagCoding.getFlagMask("m2_1"), is(6));
        assertThat(flagCoding.getFlagMask("m2_2"), is(6));
        assertThat(flagCoding.getFlagMask("m2_3"), is(6));
        assertThat(flagCoding.getFlagMask("m3_1"), is(56));
        assertThat(flagCoding.getFlagMask("m3_2"), is(56));
        assertThat(flagCoding.getFlagMask("m3_3"), is(56));
        assertThat(flagCoding.getFlagMask("m3_4"), is(56));
        assertThat(flagCoding.getFlagMask("m3_5"), is(56));
        assertThat(flagCoding.getFlagMask("m3_6"), is(56));
        assertThat(flagCoding.getFlagMask("m3_7"), is(56));
        assertThat(flagCoding.getFlag("m1").getDescription(), is("d1"));
        assertThat(flagCoding.getFlag("m2_1").getDescription(), is("d2_1"));
        assertThat(flagCoding.getFlag("m2_2").getDescription(), is("d2_2"));
        assertThat(flagCoding.getFlag("m2_3").getDescription(), is("d2_3"));
        assertThat(flagCoding.getFlag("m3_1").getDescription(), is("d3_1"));
        assertThat(flagCoding.getFlag("m3_2").getDescription(), is("d3_2"));
        assertThat(flagCoding.getFlag("m3_3").getDescription(), is("d3_3"));
        assertThat(flagCoding.getFlag("m3_4").getDescription(), is("d3_4"));
        assertThat(flagCoding.getFlag("m3_5").getDescription(), is("d3_5"));
        assertThat(flagCoding.getFlag("m3_6").getDescription(), is("d3_6"));
        assertThat(flagCoding.getFlag("m3_7").getDescription(), is("d3_7"));
        assertThat(flagCoding.getAttribute("m1").getData().getElems(), is(new int[]{1, 1}));
        assertThat(flagCoding.getAttribute("m2_1").getData().getElems(), is(new int[]{6, 2}));
        assertThat(flagCoding.getAttribute("m2_2").getData().getElems(), is(new int[]{6, 4}));
        assertThat(flagCoding.getAttribute("m2_3").getData().getElems(), is(new int[]{6, 6}));
        assertThat(flagCoding.getAttribute("m3_1").getData().getElems(), is(new int[]{56, 8}));
        assertThat(flagCoding.getAttribute("m3_2").getData().getElems(), is(new int[]{56, 16}));
        assertThat(flagCoding.getAttribute("m3_3").getData().getElems(), is(new int[]{56, 24}));
        assertThat(flagCoding.getAttribute("m3_4").getData().getElems(), is(new int[]{56, 32}));
        assertThat(flagCoding.getAttribute("m3_5").getData().getElems(), is(new int[]{56, 40}));
        assertThat(flagCoding.getAttribute("m3_6").getData().getElems(), is(new int[]{56, 48}));
        assertThat(flagCoding.getAttribute("m3_7").getData().getElems(), is(new int[]{56, 56}));
    }

    @Test
    public void applyBitFieldFlagCoding_withDescriptionsAndSampleCodingName() {
        // preparation
        final HashMap<String, Object> attributes = new HashMap<>();
        final double m1 = 0b000001; // should be int but gson library returns parsed doubles
        final double m2 = 0b000110; // should be int but gson library returns parsed doubles
        final double m3 = 0b111000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_MEANINGS, Arrays.asList("m1",
                                                    "m2_1", "m2_2", "m2_3",
                                                    "m3_1", "m3_2", "m3_3", "m3_4", "m3_5", "m3_6", "m3_7"));
        attributes.put(FLAG_MASKS, Arrays.asList(m1,
                                                 m2, m2, m2,
                                                 m3, m3, m3, m3, m3, m3, m3));
        final double v1 = 0b000001; // should be int but gson library returns parsed doubles
        final double v2_1 = 0b000010; // should be int but gson library returns parsed doubles
        final double v2_2 = 0b000100; // should be int but gson library returns parsed doubles
        final double v2_3 = 0b000110; // should be int but gson library returns parsed doubles
        final double v3_1 = 0b001000; // should be int but gson library returns parsed doubles
        final double v3_2 = 0b010000; // should be int but gson library returns parsed doubles
        final double v3_3 = 0b011000; // should be int but gson library returns parsed doubles
        final double v3_4 = 0b100000; // should be int but gson library returns parsed doubles
        final double v3_5 = 0b101000; // should be int but gson library returns parsed doubles
        final double v3_6 = 0b110000; // should be int but gson library returns parsed doubles
        final double v3_7 = 0b111000; // should be int but gson library returns parsed doubles
        attributes.put(FLAG_VALUES, Arrays.asList(v1,
                                                  v2_1, v2_2, v2_3,
                                                  v3_1, v3_2, v3_3, v3_4, v3_5, v3_6, v3_7));
        attributes.put(FLAG_DESCRIPTIONS, Arrays.asList("d1",
                                                        "d2_1", "d2_2", "d2_3",
                                                        "d3_1", "d3_2", "d3_3", "d3_4", "d3_5", "d3_6", "d3_7"));
        attributes.put(NAME_SAMPLE_CODING, "SCName");

        // execution
        ZarrProductReader.apply(attributes, band);

        // verification
        assertThat(band.isFlagBand(), is(true));
        final FlagCoding flagCoding = band.getFlagCoding();
        assertThat(flagCoding.getName(), is("SCName"));
        assertThat(band.getProduct().getFlagCodingGroup().contains(flagCoding), is(true));

        assertThat(flagCoding.getNumAttributes(), is(11));
        assertThat(flagCoding.getFlagNames(), is(new String[]{"m1",
                                                              "m2_1", "m2_2", "m2_3",
                                                              "m3_1", "m3_2", "m3_3", "m3_4", "m3_5", "m3_6", "m3_7"}));

        assertThat(flagCoding.getFlagMask("m1"), is(1));
        assertThat(flagCoding.getFlagMask("m2_1"), is(6));
        assertThat(flagCoding.getFlagMask("m2_2"), is(6));
        assertThat(flagCoding.getFlagMask("m2_3"), is(6));
        assertThat(flagCoding.getFlagMask("m3_1"), is(56));
        assertThat(flagCoding.getFlagMask("m3_2"), is(56));
        assertThat(flagCoding.getFlagMask("m3_3"), is(56));
        assertThat(flagCoding.getFlagMask("m3_4"), is(56));
        assertThat(flagCoding.getFlagMask("m3_5"), is(56));
        assertThat(flagCoding.getFlagMask("m3_6"), is(56));
        assertThat(flagCoding.getFlagMask("m3_7"), is(56));
        assertThat(flagCoding.getFlag("m1").getDescription(), is("d1"));
        assertThat(flagCoding.getFlag("m2_1").getDescription(), is("d2_1"));
        assertThat(flagCoding.getFlag("m2_2").getDescription(), is("d2_2"));
        assertThat(flagCoding.getFlag("m2_3").getDescription(), is("d2_3"));
        assertThat(flagCoding.getFlag("m3_1").getDescription(), is("d3_1"));
        assertThat(flagCoding.getFlag("m3_2").getDescription(), is("d3_2"));
        assertThat(flagCoding.getFlag("m3_3").getDescription(), is("d3_3"));
        assertThat(flagCoding.getFlag("m3_4").getDescription(), is("d3_4"));
        assertThat(flagCoding.getFlag("m3_5").getDescription(), is("d3_5"));
        assertThat(flagCoding.getFlag("m3_6").getDescription(), is("d3_6"));
        assertThat(flagCoding.getFlag("m3_7").getDescription(), is("d3_7"));
        assertThat(flagCoding.getAttribute("m1").getData().getElems(), is(new int[]{1, 1}));
        assertThat(flagCoding.getAttribute("m2_1").getData().getElems(), is(new int[]{6, 2}));
        assertThat(flagCoding.getAttribute("m2_2").getData().getElems(), is(new int[]{6, 4}));
        assertThat(flagCoding.getAttribute("m2_3").getData().getElems(), is(new int[]{6, 6}));
        assertThat(flagCoding.getAttribute("m3_1").getData().getElems(), is(new int[]{56, 8}));
        assertThat(flagCoding.getAttribute("m3_2").getData().getElems(), is(new int[]{56, 16}));
        assertThat(flagCoding.getAttribute("m3_3").getData().getElems(), is(new int[]{56, 24}));
        assertThat(flagCoding.getAttribute("m3_4").getData().getElems(), is(new int[]{56, 32}));
        assertThat(flagCoding.getAttribute("m3_5").getData().getElems(), is(new int[]{56, 40}));
        assertThat(flagCoding.getAttribute("m3_6").getData().getElems(), is(new int[]{56, 48}));
        assertThat(flagCoding.getAttribute("m3_7").getData().getElems(), is(new int[]{56, 56}));
    }
}