package server.entities;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import server.Utils;

public class ItemDirty extends Item {

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
        super.setManufacturerName(manufacturerName);
    }

    @Override
    public void setManufactureCountry(String manufactureCountry) {
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
