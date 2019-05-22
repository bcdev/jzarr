package org.esa.snap.dataio.znap.zarr;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

public class ConstantsAndUtilsCFTest {

    @Test
    public void tryFindUnitString() {
        assertEquals("degree", ConstantsAndUtilsCF.tryFindUnitString("deg"));
        assertEquals("slftr", ConstantsAndUtilsCF.tryFindUnitString("slftr"));
    }
}