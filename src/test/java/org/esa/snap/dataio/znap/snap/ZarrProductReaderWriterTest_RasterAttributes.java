package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.ArrayUtils;
import org.esa.snap.core.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZarrProductReaderWriterTest_RasterAttributes {

    private Band source;
    private Band target;
    private HashMap<String, Object> attributes;

    @Before
    public void setUp() throws Exception {
        source = new Band("band", ProductData.TYPE_FLOAT64, 10, 10);
        target = new Band("band", ProductData.TYPE_FLOAT64, 10, 10);
        attributes = new HashMap<>();
    }

    @Test
    public void rasterDescription() {
        // preparation
        source.setDescription("some extended description");
        assertThat(target.getDescription(), is(nullValue()));
        // execution
        transferToTarget();
        // verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("long_name"));
        assertThat(target.getDescription(), is("some extended description"));
    }

    @Test
    public void unit() {
        //preparation
        source.setUnit("An example unit");
        assertThat(target.getUnit(), is(nullValue()));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("units"));
        assertThat(target.getUnit(), is("An example unit"));
    }

    @Test
    public void unsignedAttribute() {
        //preparation
        source = new Band("unsigned band", ProductData.TYPE_UINT8, 10, 10);
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("_Unsigned"));
    }

    @Test
    public void validPixelExpression() {
        //preparation
        source.setValidPixelExpression("example expression");
        assertThat(target.getValidPixelExpression(), is(nullValue()));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("valid_pixel_expression"));
        assertThat(target.getValidPixelExpression(), is("example expression"));
    }

    @Test
    public void scalingFactor() {
        //preparation
        source.setScalingFactor(321.3);
        assertThat(target.getScalingFactor(), is(1.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("scale_factor"));
        assertThat(target.getScalingFactor(), is(321.3));
    }

    @Test
    public void scalingOffset() {
        //preparation
        source.setScalingOffset(221.3);
        assertThat(target.getScalingOffset(), is(0.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("add_offset"));
        assertThat(target.getScalingOffset(), is(221.3));
    }

    @Test
    public void noDataValueIfLog10ScaledIsSet() {
        //preparation
        source.setLog10Scaled(true);
        source.setNoDataValue(5.0);
        assertThat(target.getNoDataValue(), is(0.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("_FillValue"));
        assertThat(target.getNoDataValue(), is(1.0E5));
    }

    @Test
    public void noDataValueInCaseOfUnsignedIntegerData() {
        // in this case standard source and target can not be used
        source = new Band("u int", ProductData.TYPE_UINT8, 10,10);
        target = new Band("u int", ProductData.TYPE_UINT8, 10,10);

        //preparation
        source.setNoDataValue(232);  // biger than Byte.MAX_VALUE (127) but not in case of unsigned Byte
        assertThat(target.getNoDataValue(), is(0.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(2));
        assertThat(attributes.keySet().toArray(), is(new String[]{"_FillValue", "_Unsigned"}));
        assertThat(target.getNoDataValue(), is(232.0));
    }

    @Test
    public void noDataValueInCaseOfSignedIntegerData() {
        // in this case standard source and target can not be used
        source = new Band("u int", ProductData.TYPE_INT8, 10,10);
        target = new Band("u int", ProductData.TYPE_INT8, 10,10);

        //preparation
        source.setNoDataValue(-24);
        assertThat(target.getNoDataValue(), is(0.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("_FillValue"));
        assertThat(target.getNoDataValue(), is(-24.0));
    }

    @Test
    public void noDataValueInCaseOfFloat32Data() {
        // in this case standard source and target can not be used
        source = new Band("u int", ProductData.TYPE_FLOAT32, 10,10);
        target = new Band("u int", ProductData.TYPE_FLOAT32, 10,10);

        //preparation
        source.setNoDataValue(1256.523);
        assertThat(target.getNoDataValue(), is(0.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("_FillValue"));
        assertThat(attributes.get("_FillValue"), is(1256.523F));
        assertThat(((Double)target.getNoDataValue()).floatValue(), is(1256.523F));
    }

    //

    //

    //


    @Test
    public void noDataValue() {
        //preparation
        source.setNoDataValue(893756.7899);
        assertThat(target.getNoDataValue(), is(0.0));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("_FillValue"));
        assertThat(target.getNoDataValue(), is(893756.7899));
    }

    @Test
    public void noDataValueUsed() {
        //preparation
        source.setNoDataValueUsed(true);
        assertThat(target.isNoDataValueUsed(), is(false));
        //execution
        transferToTarget();
        //verification
        assertThat(attributes.size(), is(1));
        assertThat(attributes.keySet().iterator().next(), is("no_data_value_used"));
        assertThat(target.isNoDataValueUsed(), is(true));
    }

    private void transferToTarget() {
        ZarrProductWriter.collectRasterAttributes(source, attributes);
        ZarrProductReader.apply(attributes, target);
    }
}