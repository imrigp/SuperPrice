package server;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import static javax.xml.stream.XMLStreamConstants.*;

public class XmlParser {
    private static final XMLInputFactory factory = XMLInputFactory.newInstance();


    public void parseXmlFile(XmlFile xmlFile) throws XMLStreamException {
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlFile.getXml()));
        switch (xmlFile.getType()) {
            case PRICE:
            case PRICEFULL:
                parsePrices(reader);
                break;
            case PROMO:
                break;
            case PROMOFULL:
                break;
            case STORES:
                parseChainStores(reader);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + xmlFile.getType());
        }
    }

    public Chain parseChainStores(Path path) throws FileNotFoundException, XMLStreamException {
        final XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(String.valueOf(path)));
        return parseChainStores(parser);
    }

    public Chain parseChainStores(Reader reader) throws XMLStreamException {
        final XMLStreamReader parser = factory.createXMLStreamReader(reader);
        return parseChainStores(parser);
    }

    public StoreItems parsePrices(Path path) throws FileNotFoundException, XMLStreamException {
        final XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(String.valueOf(path)));
        return parsePrices(parser);
    }

    public StoreItems parsePrices(Reader reader) throws XMLStreamException {
        final XMLStreamReader parser = factory.createXMLStreamReader(reader);
        return parsePrices(parser);
    }


    private Chain parseChainStores(XMLStreamReader parser) throws XMLStreamException {
        Chain chain = new Chain();

        while (parser.hasNext()) {
            int event = parser.next();
            if (event == START_ELEMENT) {
                switch (parser.getLocalName().toLowerCase()) {
                    case "store":
                        Store store = new Store();
                        // Get all store children
                        while (!(event == END_ELEMENT && parser.getLocalName().toLowerCase().equals("store"))) {
                            event = parser.next();
                            if (event != START_ELEMENT) {
                                continue;
                            }

                            switch (parser.getLocalName().toLowerCase()) {
                                case "storeid":
                                    store.setStoreId(getElementText(parser));
                                    break;
                                case "storename":
                                    store.setName(getElementText(parser));
                                    break;
                                case "address":
                                    store.setAddress(getElementText(parser));
                                    break;
                                case "city":
                                    store.setCity(getElementText(parser));
                                    break;
                                case "storetype":
                                    store.setType(getElementText(parser));
                                    break;
                                case "chainname": // Shufersal only puts this under each Store...
                                    chain.setName(getElementText(parser));
                                    break;
                            }
                        }
                        chain.addStore(store);
                        break;
                    case "chainid":
                        chain.setId(getElementText(parser));
                        break;
                    case "chainname":
                        chain.setName(getElementText(parser));
                        break;
                }
            }
        }
        return chain;
    }

    private StoreItems parsePrices(XMLStreamReader parser) throws XMLStreamException {
        StoreItems storeItems = new StoreItems();
        while (parser.hasNext()) {
            int event = parser.next();
            if (event == START_ELEMENT) {
                switch (parser.getLocalName().toLowerCase()) {
                    case "item":
                        Item item = new ItemDirty();
                        // Get all item children
                        while (!(event == END_ELEMENT && parser.getLocalName().toLowerCase().equals("item"))) {
                            event = parser.next();
                            if (event != START_ELEMENT) {
                                continue;
                            }

                            switch (parser.getLocalName().toLowerCase()) {
                                case "itemcode":
                                    item.setId(getElementText(parser));
                                    break;
                                case "itemname":
                                    item.setName(getElementText(parser));
                                    break;
                                case "itemprice":
                                    item.setPrice(getElementText(parser));
                                    break;
                                case "manufacturername":
                                    item.setManufacturerName(getElementText(parser));
                                    break;
                                case "manufacturecountry":
                                    item.setManufactureCountry(getElementText(parser));
                                    break;
                                case "unitqty":
                                    item.setUnitQty(getElementText(parser));
                                    break;
                                case "quantity":
                                    item.setQty(getElementText(parser));
                                    break;
                                case "unitOfmeasure":
                                    item.setUnitOfMeasure(getElementText(parser));
                                    break;
                                case "unitofmeasureprice":
                                    item.setUnitOfMeasurePrice(getElementText(parser));
                                    break;
                            }
                        }
                        storeItems.addItem(item);
                        break;
                    case "chainid":
                        storeItems.setChainId(getElementText(parser));
                        break;
                    case "storeid":
                        storeItems.setStoreId(getElementText(parser));
                        break;
                }
            }
        }

        return storeItems;
    }

    private String getElementText(XMLStreamReader parser) throws XMLStreamException {
        int event = parser.next();
        assert (event != CHARACTERS) : event;
        return (event == CHARACTERS) ? parser.getText().trim() : null;
    }
}
