/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.dataio.znap.zarr.ZarrDataType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ZnapConstantsAndUtils {

    static final String FORMAT_NAME = "SNAP-Zarr";
    static final String SNAP_ZARR_CONTAINER_EXTENSION = ".znap";

    // Tie point grid attributes
    public static final String DISCONTINUITY = "discontinuity";

    // Sample coding attributes
    public static final String FLAG_DESCRIPTIONS = "flag_descriptions";

    // Product header keys
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_TYPE = "product_type";
    public static final String PRODUCT_DESC = "product_description";
    public static final String PRODUCT_METADATA = "product_metadata";

    static final Class[] IO_TYPES = new Class[]{
            Path.class,
            File.class,
            String.class
    };

    private static final OutputConverter[] IO_CONVERTERS = new OutputConverter[]{
            output -> (Path) output,
            output -> ((File) output).toPath(),
            output -> Paths.get((String) output)
    };

    static Path convertToPath(final Object object) {
        for (int i = 0; i < IO_TYPES.length; i++) {
            if (IO_TYPES[i].isInstance(object)) {
                return IO_CONVERTERS[i].convertOutput(object);
            }
        }
        return null;
    }

    private interface OutputConverter {

        Path convertOutput(Object output);
    }

    static SnapDataType getSnapDataType(ZarrDataType zarrDataType) {
        if (zarrDataType == ZarrDataType.f8) {
            return SnapDataType.TYPE_FLOAT64;
        } else if (zarrDataType == ZarrDataType.f4) {
            return SnapDataType.TYPE_FLOAT32;
        } else if (zarrDataType == ZarrDataType.i1) {
            return SnapDataType.TYPE_INT8;
        } else if (zarrDataType == ZarrDataType.u1) {
            return SnapDataType.TYPE_UINT8;
        } else if (zarrDataType == ZarrDataType.i2) {
            return SnapDataType.TYPE_INT16;
        } else if (zarrDataType == ZarrDataType.u2) {
            return SnapDataType.TYPE_UINT16;
        } else if (zarrDataType == ZarrDataType.i4) {
            return SnapDataType.TYPE_INT32;
        } else if (zarrDataType == ZarrDataType.u4) {
            return SnapDataType.TYPE_UINT32;
        } else {
            throw new IllegalStateException();
        }
    }

}
