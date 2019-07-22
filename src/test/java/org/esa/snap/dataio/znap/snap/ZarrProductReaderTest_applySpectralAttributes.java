package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


public class ZarrProductReaderTest_applySpectralAttributes {

    private Band sourceBand;
    private Map<String, Object> attributes;
    private Band targetBand;

    @Before
    public void setUp() throws Exception {
        sourceBand = new Band("source", ProductData.TYPE_FLOAT32, 10, 10);
        targetBand = new Band("target", ProductData.TYPE_UINT8, 20, 20);
        attributes = new HashMap<>();
    }

    @Test
    public void applySpectralBandwidth() {
        //preparation
        sourceBand.setSpectralBandwidth(123.4f);
        ZarrProductWriter.collectBandAttributes(sourceBand, attributes);
        assertThat(attributes.size(), is(1));
        //execution
        ZarrProductReader.apply(attributes, targetBand);
        //verification
        assertThat(targetBand.getSpectralBandwidth(), is(123.4f));
    }

    @Test
    public void applySpectralWavelength() {
        //preparation
        sourceBand.setSpectralWavelength(234.5f);
        ZarrProductWriter.collectBandAttributes(sourceBand, attributes);
        assertThat(attributes.size(), is(1));
        //execution
        ZarrProductReader.apply(attributes, targetBand);
        //verification
        assertThat(targetBand.getSpectralWavelength(), is(234.5f));
    }

    @Test
    public void applySolarFlux() {
        //preparation
        sourceBand.setSolarFlux(24.3f);
        ZarrProductWriter.collectBandAttributes(sourceBand, attributes);
        assertThat(attributes.size(), is(1));
        //execution
        ZarrProductReader.apply(attributes, targetBand);
        //verification
        assertThat(targetBand.getSolarFlux(), is(24.3f));
    }

    @Test
    public void applySpectralBandIndex() {
        //preparation
        sourceBand.setSpectralBandIndex(24);
        ZarrProductWriter.collectBandAttributes(sourceBand, attributes);
        assertThat(attributes.size(), is(1));
        //execution
        ZarrProductReader.apply(attributes, targetBand);
        //verification
        assertThat(targetBand.getSpectralBandIndex(), is(24));
    }
}