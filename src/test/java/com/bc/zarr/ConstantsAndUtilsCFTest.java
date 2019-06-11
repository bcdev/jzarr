package com.bc.zarr;

import static org.junit.Assert.*;

import org.junit.*;

public class ConstantsAndUtilsCFTest {

    @Test
    public void tryFindUnitString() {
        assertEquals("degree", ConstantsAndUtilsCF.tryFindUnitString("deg"));
        assertEquals("slftr", ConstantsAndUtilsCF.tryFindUnitString("slftr"));
    }
}