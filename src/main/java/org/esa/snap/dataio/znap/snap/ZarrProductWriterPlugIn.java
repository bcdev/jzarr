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

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;

import org.esa.snap.core.dataio.EncodeQualification;
import org.esa.snap.core.dataio.ProductWriter;
import org.esa.snap.core.dataio.ProductWriterPlugIn;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.util.Locale;

public class ZarrProductWriterPlugIn implements ProductWriterPlugIn {

    public EncodeQualification getEncodeQualification(Product product) {
        return new EncodeQualification(EncodeQualification.Preservation.FULL);
    }

    public Class[] getOutputTypes() {
        return IO_TYPES;
    }

    public ProductWriter createWriterInstance() {
        return new ZarrProductWriter(this);
    }

    public String[] getFormatNames() {
        return new String[]{FORMAT_NAME};
    }

    public String[] getDefaultFileExtensions() {
        return new String[]{SNAP_ZARR_HEADER_FILE_EXTENSION};
    }

    public String getDescription(Locale locale) {
        return FORMAT_NAME + " product writer";
    }

    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null)) {
            @Override
            public FileSelectionMode getFileSelectionMode() {
                return FileSelectionMode.FILES_AND_DIRECTORIES;
            }
        };
    }
}
