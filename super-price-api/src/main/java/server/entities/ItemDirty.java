/*
 *
 *  * Copyright 2020 Imri
 *  *
 *  * This application is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package server.entities;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import server.Utils;

public class ItemDirty extends Item {

    private static final Pattern HAS_LETTERS_PATTERN = Pattern.compile("[\\p{IsHebrew}\\w]{2,}");
    private static final Pattern EXTRACT_MEASURE_PATTERN = Pattern.compile("([\\p{IsHebrew}\\p{P}]+)\\s*(\\d+)|(\\d+)\\s*([\\p{IsHebrew}\\p{P}]+)");
    private static final Pattern EXTRACT_NAME_PATTERN = Pattern.compile("(.*?)([^%\\p{IsHebrew}\\w]+?)?$");
    private static final Pattern EXTRACT_UNIT_PATTERN = Pattern.compile(
            "(.*?)(\\d+(?:\\.\\d+)?)\\s?(מ\"?'?`?ל|(מילי:?)?ליטר(ים:?)?|(קילו:?)?גר(מי:?)?ם|גר'?|ק\"?'?`?ג|ג\\b|ג'\\b|ל\\b|מ\\b)(.*?)");

    public ItemDirty() {
        super();
    }

    @Override
    public void setName(String name) {
        Matcher matcher = EXTRACT_UNIT_PATTERN.matcher(name);

        if (matcher.find()) {
            name = Stream.of(matcher.group(1), matcher.group(4))
                         .filter(Objects::nonNull).collect(Collectors.joining());
            setQuantity(matcher.group(2));
            setQuantityUnit(matcher.group(3));
        }

        name = name.trim();
        matcher = EXTRACT_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            name = matcher.group(1);
        }

        super.setName(name);
    }

    private QuantityUnit determineUnit(String unit) {
        Matcher matcher = EXTRACT_MEASURE_PATTERN.matcher(unit);

        // Sometimes they put the quantity in the unit field, so try to separate them
        if (matcher.find()) {
            String u = matcher.group(1) == null ? matcher.group(4) : matcher.group(1);
            String quantity = matcher.group(2) == null ? matcher.group(3) : matcher.group(2);
            if (u != null) {
                unit = u;
            }
            if (quantity != null) {
                if (getQuantity() == 0) {
                    setQuantity(quantity);
                }
            }
        }

        QuantityUnit quantityUnit = QuantityUnit.fromString(unit);
        if (quantityUnit == QuantityUnit.UNKNOWN) {
            // Add to unknown units list so we can maintain the supported units
            Utils.addMeasure(unit);
        }

        return quantityUnit;
    }

    @Override
    public void setPrice(String price) {
        super.setPrice(price);
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public void setManufacturerName(String manufacturerName) {
        if (manufacturerName == null || "לא ידוע".equals(manufacturerName) || "Unknown".equals(manufacturerName)
                || !HAS_LETTERS_PATTERN.matcher(manufacturerName).matches()) {
            manufacturerName = null;
        }
        super.setManufacturerName(manufacturerName);
    }

    @Override
    public void setManufactureCountry(String manufactureCountry) {
        if (manufactureCountry == null || "לא ידוע".equals(manufactureCountry) || "Unknown".equals(manufactureCountry)
                || !HAS_LETTERS_PATTERN.matcher(manufactureCountry).matches()) {
            manufactureCountry = null;
        }
        super.setManufactureCountry(manufactureCountry);
    }

    @Override
    public void setQuantityUnit(String quantityUnit) {
        if (getQuantityUnit() == QuantityUnit.UNKNOWN) {
            super.setQuantityUnit(determineUnit(quantityUnit));
        }
    }

    @Override
    public void setQuantity(String quantity) {
        if (getQuantity() == 0) {
            super.setQuantity(quantity);
        }
    }

    @Override
    public void setUnitOfMeasure(String unitOfMeasure) {
        super.setUnitOfMeasure(unitOfMeasure);
    }
}
