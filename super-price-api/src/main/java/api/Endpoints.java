package api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DbQuery;
import io.javalin.core.util.OptionalDependency;
import io.javalin.core.util.Util;
import io.javalin.http.Context;
import org.intellij.lang.annotations.Language;
import server.DatabaseState;
import server.entities.Chain;
import server.entities.Item;
import server.entities.ItemPrice;
import server.search.ItemSearcher;

public class Endpoints {

    private static final ItemSearcher searcher = new ItemSearcher();
    private static final DatabaseState state = DatabaseState.getInstance();
    private static final DbQuery db = DbQuery.getInstance();
    private static final Chain DUMMY_CHAIN = new Chain();

    private Endpoints() {
    }

    public static void getPrices(Context ctx) {
        final long chainId;
        final int storeId;
        final List<Long> itemIds;

        try {
            JsonNode node = new ObjectMapper().readTree(ctx.body());
            chainId = node.get("chain_id").asLong();
            storeId = node.get("store_id").asInt();
            itemIds = StreamSupport
                    .stream(node.get("item_list").spliterator(), false)
                    .map(JsonNode::asLong)
                    .collect(Collectors.toList());
            if (chainId == 0 || storeId == 0) {
                throw new RuntimeException();
            }
        } catch (JsonProcessingException | RuntimeException e) {
            ctx.json(JsonError.build("bad request body."));
            return;
        }

        final List<ItemPrice> prices = db.getItemsPrice(chainId, storeId, itemIds);
        ctx.json(prices);
    }

    public static void searchStores(Context ctx) {
        final String groupBy = ctx.queryParam("groupBy");
        if (groupBy == null || (!groupBy.equals("city") && !groupBy.equals("chain"))) {
            ctx.json(db.searchStores(ctx.queryParam("city")));
        } else {
            ctx.json(db.searchStores(ctx.queryParam("city"), groupBy));
        }
    }

    public static void searchItems(Context ctx) {
        final String query = ctx.queryParam("name");
        if (query == null) {
            ctx.json(JsonError.build("parameter 'name' missing."));
        } else {
            final List<Item> itemList = searcher.search(query);
            ctx.json(itemList);
        }
    }


    public static void getChainStores(Context ctx) {
        try {
            Long chainId = Long.parseLong(ctx.pathParam("chainId"));
            ctx.json(state.getChains().getOrDefault(chainId, DUMMY_CHAIN).getStores().values());
        } catch (NumberFormatException e) {
            ctx.json(Collections.emptyList());
        }
    }

    public static void getAllChains(Context ctx) {
        ctx.json(state.getChains().values());
    }

    public static void getItemInfo(Context ctx) {
        try {
            long itemId = Long.parseLong(ctx.pathParam("itemId"));
            final Item item = state.getAllDbItems().get(itemId);
            ctx.json(Objects.requireNonNullElseGet(item, () -> new JsonError("Item not found.")));
        } catch (NumberFormatException e) {
            ctx.json(Collections.emptyList());
        }
    }

    public static void getSwaggerUi(Context ctx) {
        String path = Util.getWebjarPublicPath(ctx, OptionalDependency.SWAGGERUI);

        @Language("html")
        String html = """
                    <!-- HTML for static distribution bundle build -->
                    <!DOCTYPE html>
                    <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <title>Swagger UI</title>
                            <link rel="stylesheet" type="text/css" href="%s/swagger-ui.css" >
                            <link rel="icon" type="image/png" href="%s/favicon-32x32.png" sizes="32x32" />
                            <link rel="icon" type="image/png" href="%s/favicon-16x16.png" sizes="16x16" />
                            <style>
                                html {
                                    box-sizing: border-box;
                                    overflow: -moz-scrollbars-vertical;
                                    overflow-y: scroll;
                                }
                                *, *:before, *:after {
                                    box-sizing: inherit;
                                }
                                body {
                                    margin:0;
                                    background: #fafafa;
                                }
                            </style>
                        </head>
                        <body>
                            <div id="swagger-ui"></div>
                            <script src="%s/swagger-ui-bundle.js"> </script>
                            <script src="%s/swagger-ui-standalone-preset.js"> </script>
                            <script>
                            window.onload = function() {
                                window.ui = SwaggerUIBundle({
                                    url: "%s",
                                    dom_id: "#swagger-ui",
                                    deepLinking: true,
                                    presets: [
                                      SwaggerUIBundle.presets.apis,
                                      SwaggerUIStandalonePreset
                                    ],
                                    plugins: [
                                      SwaggerUIBundle.plugins.DownloadUrl
                                    ],
                                    layout: "StandaloneLayout"
                                  })
                            }
                            </script>
                        </body>
                    </html>
                """.formatted(path, path, path, path, path, "swagger-docs.yaml");
        ctx.html(html);
    }
}
