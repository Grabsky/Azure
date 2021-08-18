package me.grabsky.azure.storage;

public class Credentials {
    private String type;
    private String address;
    private String port;
    private String username;
    private String password;
    private String database;

    // Constructor used to create Credentials bundle using Config entries
    public Credentials(String type, String address, String port, String username, String password, String database) {
        this.type = type;
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    protected String getType() {
        return type;
    }

    protected String getAddress() {
        return address;
    }

    protected String getPort() {
        return port;
    }

    protected String getUsername() {
        return username;
    }

    protected String getPassword() {
        return password;
    }

    protected String getDatabase() {
        return database;
    }

    // Returns true when all values are existent
    public boolean isEmpty() {
        return type == null || address == null || port == null || username == null || password == null || database == null;
    }

}
