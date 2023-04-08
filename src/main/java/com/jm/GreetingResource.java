package com.jm;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class GreetingResource {

    @Inject
    Vertx vertx;

    @Inject
    Logger logger;

    @GET()
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> test() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("hello", "world");
        // JDBCPool pool = JDBCPool.pool(
        // vertx,
        // // configure the connection
        // new JDBCConnectOptions()
        // // H2 connection string
        // .setJdbcUrl("com.sybase.jdbc4.jdbc.SybDriver")
        // // username
        // .setUser("sa")
        // // password
        // .setPassword("password"),
        // // configure the pool
        // new PoolOptions()
        // .setMaxSize(16)
        // .setName("pool-name"));
        final JsonObject config = new JsonObject()
                // .put("url", "com.sybase.jdbc4.jdbc.SybDriver")
                .put("url", "com.sybase.jdbc4.jdbc.SybDriver")
                .put("datasourceName", "pool-name")
                .put("username", "sa")
                .put("password", "password")
                .put("max_pool_size", 16);
        JDBCPool pool = JDBCPool.pool(vertx, config);
        pool
                .query("SELECT * FROM customers")
                .execute()
                .onFailure(e -> {
                    // handle the failure
                    logger.error("Conexión falló", e);
                })
                .onSuccess(rows -> {
                    logger.error("Conexión exitosa");
                    for (Row row : rows) {
                        logger.infof("Customer name: %s", row.getString("name"));
                    }
                });
        return Uni.createFrom().item(jsonObject.toString());
    }
}
