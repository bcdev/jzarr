/*
 * $
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
package com.bc.zarr;

import ucar.nc2.constants.CDM;
import ucar.nc2.constants.CF;

import java.util.HashMap;
import java.util.Map;

public final class ConstantsAndUtilsCF implements CF, CDM {

    // CF sample coding attributes
    public static final String FLAG_VALUES = "flag_values";
    public static final String FLAG_MASKS = "flag_masks";
    public static final String FLAG_MEANINGS = "flag_meanings";

    private static final Map<String, String> unitMap = new HashMap<String, String>();

    static {
        unitMap.put("deg", "degree");
    }

    public static String tryFindUnitString(String unit) {
        if (unitMap.containsKey(unit)) {
            return unitMap.get(unit);
        }
        return unit;
    }
}
