package server.Xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.DatabaseState;
import server.entities.Chain;
import server.entities.Entity;
import server.entities.Item;
import server.entities.ItemDirty;
import server.entities.Store;
import server.entities.StoreItems;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class XmlParser {
    private static final Logger log = LoggerFactory.getLogger(XmlParser.class);
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final ExecutorService parseExecutor;
    private final Consumer<Entity> entityConsumer;

    private XmlFile xmlFile;

    public XmlParser(Consumer<Entity> entityConsumer) {
        this.entityConsumer = entityConsumer;
        parseExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("parser-thread").build());
    }

    public void parseXmlFile(final XmlFile xmlFile) {
        parseExecutor.execute(() -> parseDispatcher(xmlFile).ifPresent(entity -> {
            DatabaseState.getInstance().addParsedFile(xmlFile.toString());
            entityConsumer.accept(entity);
            log.info("Parsed: {}", xmlFile);
        }));
    }

    private Optional<Entity> parseDispatcher(final XmlFile xmlFile) {
        if (xmlFile.getInputStream() == null) {
            return Optional.empty();
        }

        // for debugging
        if (false) {
            try {
                String s = IOUtils.toString(xmlFile.getResponseInputStream().get(), StandardCharsets.UTF_8.name());
                System.out.println(s);
            } catch (IOException e) {
                log.error("Parsing error in {}:{}", xmlFile, e);
            }
            return Optional.empty();
        }

        this.xmlFile = xmlFile;
        Entity res = null;
        XMLStreamReader reader = null;
        try (InputStream is = xmlFile.getInputStream()) {
            reader = FACTORY.createXMLStreamReader(is);

            switch (xmlFile.getType()) {
                case PRICE:
                case PRICEFULL:
                    res = parsePrices(reader);
                case PROMO:
                    break;
                case PROMOFULL:
                    break;
                case STORES:
                    res = parseChainStores(reader);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + xmlFile.getType());
            }
        } catch (XMLStreamException | IOException e) {
            log.error("Parsing error in {}:", xmlFile, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    log.error("Parsing error in {}:", xmlFile, e);
                }
            }
        }

        return Optional.ofNullable(res);
    }

    private Chain parseChainStores(XMLStreamReader parser) throws XMLStreamException {
        Chain chain = new Chain();

        while (parser.hasNext()) {
            int event = parser.next();
            if (event == START_ELEMENT) {
                // Get all store children
                switch (parser.getLocalName().toLowerCase()) {
                    case "store" -> {
                        Store store = new Store();
                        while (!(event == END_ELEMENT && parser.getLocalName().toLowerCase().equals("store"))) {
                            event = parser.next();
                            if (event != START_ELEMENT) {
                                continue;
                            }
                            switch (parser.getLocalName().toLowerCase()) {
                                case "storeid" -> store.setStoreId(getElementText(parser));
                                case "storename" -> store.setName(getElementText(parser));
                                case "address" -> store.setAddress(getElementText(parser));
                                case "city" -> store.setCity(getElementText(parser));
                                case "storetype" -> store.setType(getElementText(parser));
                                // Shufersal only puts this under each Store...
                                case "chainname" -> chain.setName(getElementText(parser));
                            }
                        }
                        chain.addStore(store);
                    }
                    case "chainid" -> chain.setId(getElementText(parser));
                    case "chainname" -> chain.setName(getElementText(parser));
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
                // Get all item children
                switch (parser.getLocalName().toLowerCase()) {
                    case "item" -> {
                        Item item = new ItemDirty();
                        while (!(event == END_ELEMENT && parser.getLocalName().toLowerCase().equals("item"))) {
                            event = parser.next();
                            if (event != START_ELEMENT) {
                                continue;
                            }
                            switch (parser.getLocalName().toLowerCase()) {
                                case "itemcode" -> item.setId(getElementText(parser));
                                case "itemname" -> item.setName(getElementText(parser));
                                case "itemprice" -> item.setPrice(getElementText(parser));
                                case "manufacturername" -> item.setManufacturerName(getElementText(parser));
                                case "manufacturecountry" -> item.setManufactureCountry(getElementText(parser));
                                case "unitqty" -> item.setQuantityUnit(getElementText(parser));
                                case "quantity" -> item.setQuantity(getElementText(parser));
                                case "unitOfmeasure" -> item.setUnitOfMeasure(getElementText(parser));
                            }
                        }
                        storeItems.addItem(item);
                    }
                    case "chainid" -> storeItems.setChainId(getElementText(parser));
                    case "storeid" -> storeItems.setStoreId(getElementText(parser));
                }
            }
        }
        return storeItems;
    }

    private String getElementText(XMLStreamReader parser) {
        try {
            int event = parser.next();
            return (event == CHARACTERS) ? parser.getText().trim() : null;
        } catch (XMLStreamException | RuntimeException e) {
            log.error("Parsing error in {}:", xmlFile, e);
        }
        return null;
    }
}
