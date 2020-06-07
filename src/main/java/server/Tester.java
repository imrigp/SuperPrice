package server;

import database.*;
import org.quartz.SchedulerException;
import server.plans.Plan;
import server.plans.ShufersalPlan;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// todo: add update date for each price in db and check before inserting... It's either that or updating sequentially

public class Tester {

    private static void updatePrices(StoreItems storeItems) {
        ItemDaoPostgres itemDao = new ItemDaoPostgres(PostgreSql.getDataSource());
        StoreItemsDaoPostgres storeItemsDao = new StoreItemsDaoPostgres(PostgreSql.getDataSource());

        // get db items snapshot
        Map<Long, Item> incompleteDbItems = itemDao.getIncompleteItems()
                .stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
        Map<Long, Item> allDbItems = itemDao.getAllItems()
                .stream()
                .filter(item -> !incompleteDbItems.containsKey(item.getId()))
                .collect(Collectors.toMap(Item::getId, item -> item));

        System.out.println(incompleteDbItems.size());
        itemDao.addItems(
                storeItems.getItems()
                        .stream()
                        .filter(item -> !allDbItems.containsKey(item.getId())
                                || incompleteDbItems.containsKey(item.getId()))
                        .collect(Collectors.toCollection(ArrayList::new)));

        storeItemsDao.addStoreItems(storeItems);
    }

    private static void updateStores(Chain chain) {
        ChainDaoPostgres chainDao = new ChainDaoPostgres(PostgreSql.getDataSource());
        chainDao.addChain(chain);
        StoreDaoPostgres storeDao = new StoreDaoPostgres(PostgreSql.getDataSource());
        storeDao.addStores(chain.getId(), chain.getStores());
    }

    public static void main(String[] args) throws SQLException, IOException, XMLStreamException, InterruptedException {
        {

            long startTime = System.nanoTime();

            //Plan plan1 = new CerberusPlan("tivtaam");
            //Plan plan2 = new CerberusPlan("yohananof");
            Plan plan1 = new ShufersalPlan();


            if (true) {
                PlanManager manager;

                try {
                    manager = new PlanManager(50);
                    manager.addPlan(plan1);
                    //manager.addPlan(plan2);
                    manager.start();

                    //manager.shutdown();
                    System.out.println("fin");
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                return;
            }


            long estimatedTime = System.nanoTime() - startTime;
            System.out.println("Time Elapsed in seconds: " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime));
            System.out.println("Time Elapsed in milliseconds: " + TimeUnit.NANOSECONDS.toMillis(estimatedTime));
            System.exit(6);


            //parser.parseChainStores(new StringReader(res));

            HttpClientPool.shutdown();
            System.exit(1);
        }


        PostgreSql.createTables();
        StoreItemsDaoPostgres storeItemsDao = new StoreItemsDaoPostgres(PostgreSql.getDataSource());
        ChainDaoPostgres chainDao = new ChainDaoPostgres(PostgreSql.getDataSource());
        StoreDaoPostgres storeDao = new StoreDaoPostgres(PostgreSql.getDataSource());


        XmlParser parser = new XmlParser();
        Files.newDirectoryStream(Paths.get("resources\\"),
                "Stores*.xml").forEach(path -> {
            try {
                updateStores(parser.parseChainStores(path));
            } catch (FileNotFoundException | XMLStreamException e) {
                e.printStackTrace();
            }
        });

        Files.newDirectoryStream(Paths.get("resources\\"),
                "PriceFull*.xml").forEach(path -> {
            try {
                updatePrices(parser.parsePrices(path));
            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
            }
        });


        StoreItems storeItems = storeItemsDao.getStoreItems(7290873255550L, 21);

        //ArrayList<Store> stores = storeDao.getChainStores(7290873255550L);
        //System.out.println(Arrays.toString(storeItems.getItems().toArray()));
        //items.forEach(item -> System.out.println(item.getName()));

    }
}
