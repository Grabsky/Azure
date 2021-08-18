package me.grabsky.azure.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.grabsky.azure.Azure;
import me.grabsky.indigo.logger.ConsoleLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLManager {
    private final Azure azure = Azure.getInstance();
    private final ConsoleLogger consoleLogger = Azure.getInstance().getConsoleLogger();
    private static HikariDataSource hikari;
    private Credentials credentials;


    // Updates credentials used to connect to database
    public void setCredentials(String type, String address, String port, String username, String password, String database) {
        this.credentials = new Credentials(type, address, port, username, password, database);
    }

    // Initializes SQL data "pipeline"
    public boolean initialize() {
        if(!credentials.isEmpty()) {
            prepareDatabase();
            createTables();
            return true;
        }
        return false;
    }

    // Closes hikari connection
    public void close() {
        if(!hikari.isClosed()) {
            hikari.close();
        }
    }

    // Prepares HikariDataSource before connecting
    private void prepareDatabase() {
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com." + credentials.getType() + ".jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", credentials.getAddress());
        hikari.addDataSourceProperty("port", credentials.getPort());
        hikari.addDataSourceProperty("databaseName", credentials.getDatabase());
        hikari.addDataSourceProperty("user", credentials.getUsername());
        hikari.addDataSourceProperty("password", credentials.getPassword());
    }

    // Create tables (if not existent)
    private void createTables() {
        try {
            consoleLogger.log("Creating tables...");
            Connection connection = hikari.getConnection();
            PreparedStatement playerData = connection.prepareStatement("CREATE TABLE IF NOT EXISTS azure_playerdata("
                    + "uuid VARCHAR(36) UNIQUE PRIMARY KEY, "
                    + "name VARCHAR(16), "
                    + "customName VARCHAR(48), "
                    + "lastIpAddress VARCHAR(15), "
                    + "lastCountry VARCHAR(32), "
                    + "lastLanguage VARCHAR(8), "
                    + "lastSeen INT, "
                    + "lastLocation JSON"
                    + ")"
            );
            playerData.executeUpdate();
            PreparedStatement homes = connection.prepareStatement("CREATE TABLE IF NOT EXISTS azure_homes("
                    + "uuid VARCHAR(36) PRIMARY KEY, "
                    + "homeName VARCHAR(16), "
                    + "homeLocation JSON"
                    + "FOREIGN KEY (uuid) REFERENCES azure_playerdata(uuid)"
                    + ")"
            );
            homes.executeUpdate();
//            PreparedStatement actionCooldowns = connection.prepareStatement("CREATE TABLE IF NOT EXISTS azure_actions("
//                    + "uuid VARCHAR(36) UNIQUE PRIMARY KEY, "
//                    + "action VARCHAR(16), "
//                    + "lastUsage FLOAT(2), "
//                    + "FOREIGN KEY (uuid) REFERENCES azure_playerdata(uuid)"
//                    + ")"
//            );
//            actionCooldowns.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Returns HikariDataSource so we can get connection in other classes
    protected HikariDataSource getHikari() {
        return hikari;
    }

}
