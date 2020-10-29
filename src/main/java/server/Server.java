package server;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import api.Endpoints;
import api.JsonError;
import database.Database;
import database.DbQuery;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.util.RateLimit;
import org.jetbrains.annotations.NotNull;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.EntityConsumer;
import server.entities.Item;
import server.plans.CerberusPlan;
import server.plans.Plan;
import server.plans.PlanManager;
import server.plans.ShufersalPlan;
import server.plans.YinotBitanPlan;
import server.xml.XmlFile;
import server.xml.XmlParser;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final int DOWNLOAD_THREADS = 20;
    private static final int MAX_REQUESTS_PER_MINUTE = 50;
    public static final int PORT = 8081;

    private final DbQuery db;
    private final XmlParser parser;
    private final EntityConsumer entityConsumer;
    private final PlanManager manager;
    private final DatabaseState state;
    private static final List<String> CLASSES_LOAD_RUNTIME =
            List.of("api.Endpoints");

    public Server() throws SchedulerException {
        initDb();
        db = DbQuery.getInstance();

        state = DatabaseState.getInstance();
        //state.clearState();

        entityConsumer = new EntityConsumer(state);
        parser = new XmlParser(entityConsumer);
        Consumer<XmlFile> xmlConsumer = parser::parseXmlFile;

        manager = new PlanManager(xmlConsumer, DOWNLOAD_THREADS);
        initPlans();

        Map<Long, Item> inc = state.getIncompleteDbItems();
        Map<Long, Item> all = state.getAllDbItems();
        log.info("Number of all items: {}", all.size());
        log.info("Number of incomplete items: {}", inc.size());

        loadClasses();

        Javalin app =
                Javalin.create(config -> {
                    config.defaultContentType = "application/json";
                    config.showJavalinBanner = false;
                })
                       .exception(HttpResponseException.class, (e, ctx) -> {
                           ctx.json(new JsonError("Too many requests. Please wait a few minutes."));
                           log.error("ip: {} exceeded rate limit.", getRealIp(ctx));
                       })
                       .exception(Exception.class, (e, ctx) -> log.error("", e))
                       .error(404, ctx -> ctx.json(JsonError.build("Not found")))
                       .before(ctx -> {
                           new RateLimit(ctx).requestPerTimeUnit(MAX_REQUESTS_PER_MINUTE, TimeUnit.MINUTES);
                           log.debug("{} request: {}", getRealIp(ctx), ctx.fullUrl());
                       })
                       .routes(() -> {
                           get("chains", Endpoints::getAllChains);
                           get("chains/:chainId/stores", Endpoints::getChainStores);
                           get("items", Endpoints::searchItems);
                           get("items/:itemId", Endpoints::getItemInfo);
                           get("stores", Endpoints::searchStores);
                           post("price", Endpoints::getPrices);
                       }).start(PORT);
        log.info("Server started");
    }

    @NotNull
    public String getRealIp(Context ctx) {
        return Objects.requireNonNullElse(ctx.header("X-Forwarded-For"), ctx.ip());
    }

    private void initDb() {
        try {
            Database.createTables(false);
            //db.clearTables();
        } catch (SQLException e) {
            log.error("Error while creating tables: ", e);
            System.exit(1);
        }
    }

    private void initPlans() {
        List<Plan> plans = Arrays.asList(
                new CerberusPlan("tivtaam"),
                new CerberusPlan("yohananof"),
                new CerberusPlan("RamiLevi"),
                new CerberusPlan("freshmarket"),
                new ShufersalPlan("Shufersal"),
                new CerberusPlan("osherad"),
                new CerberusPlan("doralon"),
                new CerberusPlan("HaziHinam"),
                new CerberusPlan("Stop_Market"),
                new CerberusPlan("keshet"),
                new YinotBitanPlan("YinotBitan")
        );

        try {
            for (Plan plan : plans) {
                manager.addPlan(plan);
            }

            //manager.start();
            //manager.shutdown();
        } catch (SchedulerException e) {
            log.error("", e);
            System.exit(1);
        }
    }

    private void loadClasses() {
        try {
            for (String className : CLASSES_LOAD_RUNTIME) {
                Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            log.error("", e);
            System.exit(-1);
        }
    }
}
