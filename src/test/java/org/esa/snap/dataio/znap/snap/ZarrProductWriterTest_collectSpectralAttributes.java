package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


public class ZarrProductWriterTest_collectSpectralAttributes {

    private Band band;

    @Before
    public void setUp() throws Exception {
        band = new Band("band", ProductData.TYPE_FLOAT32, 10, 10);
    }

    @Test
    public void collectBandwidth() {
        final HashMap<String, Object> attributes = new HashMap<>();

        band.setSpectralBandwidth(234.5f);
        ZarrProductWriter.collectBandAttributes(band, attributes);

        assertThat(attributes.size(), is(1));
        assertThat(attributes.containsKey(BANDWIDTH), is(true));
        assertThat(attributes.get(BANDWIDTH), is(234.5f));
    }

    @Test
    public void collectWavelength() {
        final HashMap<String, Object> attributes = new HashMap<>();

        band.setSpectralWavelength(123.4f);
        ZarrProductWriter.collectBandAttributes(band, attributes);

        assertThat(attributes.size(), is(1));
        assertThat(attributes.containsKey(WAVELENGTH), is(true));
        assertThat(attributes.get(WAVELENGTH), is(123.4f));
    }

    @Test
    public void collectSolarFlux() {
        final HashMap<String, Object> attributes = new HashMap<>();

        band.setSolarFlux(23.4f);
        ZarrProductWriter.collectBandAttributes(band, attributes);

        assertThat(attributes.size(), is(1));
        assertThat(attributes.containsKey(SOLAR_FLUX), is(true));
        assertThat(attributes.get(SOLAR_FLUX), is(23.4f));
    }

    @Test
    public void collectSpectralBandIndex() {
        final HashMap<String, Object> attributes = new HashMap<>();

        band.setSpectralBandIndex(23);
        ZarrProductWriter.collectBandAttributes(band, attributes);

        assertThat(attributes.size(), is(1));
        assertThat(attributes.containsKey(SPECTRAL_BAND_INDEX), is(true));
        assertThat(attributes.get(SPECTRAL_BAND_INDEX), is(23));
    }


}