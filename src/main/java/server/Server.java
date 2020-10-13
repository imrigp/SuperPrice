package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import database.Database;
import database.DbQuery;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Xml.XmlFile;
import server.Xml.XmlParser;
import server.entities.Item;
import server.plans.CerberusPlan;
import server.plans.Plan;
import server.plans.ShufersalPlan;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final int DOWNLOAD_THREADS = 50;

    private final DbQuery db;
    private final XmlParser parser;
    private final EntityConsumer entityConsumer;
    private final PlanManager manager;
    private final State state;

    public Server() throws SchedulerException {
        db = DbQuery.getInstance();
        state = State.getInstance();
        entityConsumer = new EntityConsumer(state);
        parser = new XmlParser(entityConsumer);
        Consumer<XmlFile> xmlConsumer = parser::parseXmlFile;

        manager = new PlanManager(xmlConsumer, DOWNLOAD_THREADS);
        initDb();
        initPlans();

        Map<Long, Item> inc = state.getIncompleteDbItems();
        Map<Long, Item> all = state.getAllDbItems();
        log.info("Number of all items: {}", all.size());
        log.info("Number of incomplete items: {}", inc.size());
    }

    private void initDb() {
        try {
            Database.createTables(false);
            db.clearTables();
            state.clearState();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void initPlans() {
        List<Plan> plans = Arrays.asList(
                new CerberusPlan("tivtaam"),
                new CerberusPlan("yohananof"),
                new CerberusPlan("RamiLevi"),
                new CerberusPlan("freshmarket"),
                new ShufersalPlan("Shufersal")
        );

        try {
            for (Plan plan : plans) {
                manager.addPlan(plan);
            }

            manager.start();

            //manager.shutdown();
            log.info("Server started");
        } catch (SchedulerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void init() {
        try {
            Files.newDirectoryStream(Paths.get("resources\\"),
                    "Stores*.xml").forEach(path -> {
                XmlFile xmlFile = null;
                try {
                    xmlFile = new XmlFile(path.getFileName().toString(), new String(Files.readAllBytes(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert xmlFile != null;
                parser.parseXmlFile(xmlFile);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
