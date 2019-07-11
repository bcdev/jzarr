package com.bc.zarr;

import static org.junit.Assert.*;

import org.junit.*;

public class CFConstantsAndUtilsTest {

    @Test
    public void tryFindUnitString() {
        assertEquals("degree", CFConstantsAndUtils.tryFindUnitString("deg"));
        assertEquals("slftr", CFConstantsAndUtils.tryFindUnitString("slftr"));
    }
}