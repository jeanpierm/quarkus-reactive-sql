package com.jm;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import org.jboss.logging.Logger;

import com.sybase.jdbcx.SybDriver;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// @Path("/")
@ApplicationScoped
public class GreetingResource {

    @Inject
    Vertx vertx;

    @Inject
    Logger logger;

    private final static String SQL_GET_CUSTOMERS = "SELECT cus.customer_id, cus.surname FROM customers cus";

    @Route(methods = Route.HttpMethod.GET, path = "/test")
    public void test(RoutingExchange ex)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        logger.infof("SYBASE JCONN DRIVER: %s", Class.forName("com.sybase.jdbc4.jdbc.SybDriver"));
        logger.infof("SYBASE JTDS DRIVER: %s", Class.forName("net.sourceforge.jtds.jdbc.Driver"));

        // jConnect driver
        SybDriver sybDriver = (SybDriver) Class.forName("com.sybase.jdbc4.jdbc.SybDriver")
                .getDeclaredConstructor().newInstance();
        sybDriver.setVersion(com.sybase.jdbcx.SybDriver.VERSION_7);
        DriverManager.registerDriver(sybDriver);

        final JsonObject config = new JsonObject()
                .put("url",
                        "jdbc:sybase:Tds:127.0.0.1:5000/gizlo_test")
                .put("driver_class", "com.sybase.jdbc4.jdbc.SybDriver")
                .put("datasource_name", "sybase")
                .put("acquire_retry_attempts", 4)
                .put("max_pool_size", 5)
                .put("user", "sa")
                .put("password", "password");

        // jTDS driver
        // final JsonObject config = new JsonObject()
        // .put("url",
        // "jdbc:jtds:sybase://127.0.0.1:5000/gizlo_test")
        // .put("driver_class", "net.sourceforge.jtds.jdbc.Driver")
        // .put("datasource_name", "sybase")
        // .put("acquire_retry_attempts", 4)
        // .put("max_pool_size", 5)
        // .put("user", "sa")
        // .put("password", "password");

        // JDBCPool pool = JDBCPool.pool(vertx, config);
        JDBCClient client = JDBCClient.createShared(vertx, config);

        // Legacy JDBC Client API
        client.getConnection(res -> {
            if (res.succeeded()) {
                logger.info("Query ejecutado con éxito");
                JsonArray rows = new JsonArray();
                SQLConnection conn = res.result();

                conn.query(SQL_GET_CUSTOMERS, res2 -> {
                    if (res2.succeeded()) {
                        ResultSet rs = res2.result();
                        rs.getRows().forEach(row -> rows.add(row));
                        ex.ok(rows.toString());
                    } else {
                        logger.error("Ocurrió un error al ejecutar el query", res2.cause());
                        ex.serverError();
                    }
                });
            } else {
                logger.error("Ocurrió un error al ejecutar el query", res.cause());
                ex.serverError();
            }
        });

        // client.query(SQL_GET_CUSTOMERS, res -> {
        // if (res.succeeded()) {
        // logger.info("Query ejecutado con éxito");
        // JsonArray rows = new JsonArray();
        // ResultSet rs = res.result();
        // rs.getRows().forEach(row -> rows.add(row));
        // ex.ok(rows.toString());
        // } else {
        // logger.error("Ocurrió un error al ejecutar el query", res.cause());
        // ex.serverError();
        // }
        // });

        // Sql Client API
        // pool
        // .query(SQL_GET_CUSTOMERS)
        // .execute()
        // .onFailure(e -> {
        // // handle the failure
        // logger.error("Conexión falló", e);
        // ex.serverError();
        // })
        // .onSuccess(rows -> {
        // logger.info("Conexión exitosa");
        // JsonArray jsonArray = new JsonArray();
        // for (Row row : rows) {
        // JsonObject jsonObject = new JsonObject();
        // // String customerId = (String) row.getValue("customer_id");
        // // String name = (String) row.getValue("name");
        // String surname = (String) row.getValue("surname");
        // // jsonObject.put("customerId", customerId);
        // // jsonObject.put("name", name);
        // jsonObject.put("surname", surname);
        // jsonArray.add(jsonObject);
        // }
        // ex.ok(jsonArray.toString());
        // });
    }
}
