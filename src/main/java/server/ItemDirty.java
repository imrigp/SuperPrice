package server;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemDirty extends Item {

    private static final Pattern pattern2 = Pattern.compile("(.*?)([^%\\p{IsHebrew}\\w]+?)?$");
    private static final Pattern pattern = Pattern.compile(
            "(.*?)(\\d+(?:\\.\\d+)?)\\s?(מ\"?'?`?ל|(מילי:?)?ליטר(ים:?)?|(קילו:?)?גר(מי:?)?ם|גר'?|ק\"?'?`?ג|ג\\b|ג'\\b|ל\\b|מ\\b)(.*?)");

    public ItemDirty() {
        super();
    }

    @Override
    public void setName(String name) {
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            name = Stream.of(matcher.group(1), matcher.group(4))
                    .filter(Objects::nonNull).collect(Collectors.joining());
            setQty(matcher.group(2));
            setUnitQty(matcher.group(3));
        }

        name = name.trim();
        matcher = pattern2.matcher(name);
        if (matcher.find()) {
            name = matcher.group(1);
        }

        super.setName(name);
    }

    private String determineUnit(String unit) {
        String newUnit;
        // Switch cases are implemented as hash maps under the hood, so this should be quite efficient
        // todo: Change to Enum!!
        switch (unit) {
            case "גרם":
            case "גרמים":
            case "גר":
            case "ג":
            case "ג'":
            case "ג`":
            case "גר'":
            case "גר`":
                newUnit = "gram";
                break;
            case "ליטר":
            case "ליטרים":
            case "ל":
            case "ל'":
            case "ל`":
                newUnit = "liter";
                break;
            case "קילו":
            case "קילוגרם":
            case "קילוגרמים":
            case "קג":
            case "ק":
            case "ק\"ג":
            case "ק'":
            case "ק'ג":
            case "ק`":
            case "ק`ג":
                newUnit = "kg";
                break;
            case "מיליליטר":
            case "מיליליטרים":
            case "מל":
            case "מ\"ל":
            case "מ":
            case "מ'ל":
            case "מ`ל":
                newUnit = "ml";
                break;
            case "Unknown":
            case "לא מוגדר":
            case "ק``ג\\גרם":
            case "ליטר\\מ``ל":
                newUnit = "unknown";
                break;
            case "מטר":
            case "מטרים":
                newUnit = "meter";
                break;
            case "יחידה":
            case "יח'":
            case "יח`":
            case "יח":
                newUnit = "unit";
                break;
            default:
                newUnit = unit;
                System.err.println("Unknown unit: " + unit);
                break;
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
