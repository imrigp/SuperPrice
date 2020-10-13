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
            setQty(matcher.group(2));
            setUnitQty(matcher.group(3));
        }

        name = name.trim();
        matcher = EXTRACT_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            name = matcher.group(1);
        }

        super.setName(name);
    }

    private String determineUnit(String unit) {
        Matcher matcher = EXTRACT_MEASURE_PATTERN.matcher(unit);

        if (matcher.find()) {
            String u = matcher.group(1) == null ? matcher.group(4) : matcher.group(1);
            String quantity = matcher.group(2) == null ? matcher.group(3) : matcher.group(2);
            if (u != null) {
                unit = u;
            }
            if (quantity != null) {
                if (getQty() == 0) {
                    setUnitQty(quantity);
                }
            }
        }

        String newUnit;
        // Switch cases are implemented as hash maps under the hood, so this should be quite efficient
        // todo: Change to Enum!!
        switch (unit) {
            case "גרם", "גרמים", "גר", "ג", "ג'", "ג`", "גר'", "גר`" -> newUnit = "gram";
            case "ליטר", "ליטרים", "ל", "ל'", "ל`" -> newUnit = "liter";
            case "קילו", "קילוגרם", "קילוגרמים", "קג", "ק", "ק\"ג", "ק'", "ק'ג", "ק`", "ק`ג", "לקג" -> newUnit = "kg";
            case "מיליליטר", "מיליליטרים", "מל", "מ\"ל", "מ", "מ'ל", "מ`ל" -> newUnit = "ml";
            case "לא ידוע", "Unknown", "לא מוגדר", "ק``ג\\גרם", "ליטר\\מ``ל" -> newUnit = null;
            case "מטר", "מטרים" -> newUnit = "meter";
            case "סמ", "ס\"מ", "סנטימטר" -> newUnit = "cm";
            case "יחידה", "יח'", "יח`", "יח", "יחי", "יחידו" -> newUnit = "unit";
            default -> {
                newUnit = null;
                Utils.addMeasure(unit);
            }
        }
        return newUnit;
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
    public void setUnitQty(String unitQty) {
        if (getUnitQty().isEmpty()) {
            super.setUnitQty(determineUnit(unitQty));
        }
    }

    @Override
    public void setQty(String qty) {
        if (getQty() == 0) {
            super.setQty(qty);
        }
    }

    @Override
    public void setUnitOfMeasure(String unitOfMeasure) {
        super.setUnitOfMeasure(unitOfMeasure);
    }

    @Override
    public void setUnitOfMeasurePrice(String unitOfMeasurePrice) {
        super.setUnitOfMeasurePrice(unitOfMeasurePrice);
    }
}
