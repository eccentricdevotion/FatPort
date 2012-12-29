package me.eccentric_nz.plugins.FatPort;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class FatPortDatabase {

    private static FatPortDatabase instance = new FatPortDatabase();
    public Connection connection = null;
    public Statement statement;
    private FatPort plugin;

    public static synchronized FatPortDatabase getInstance() {
        return instance;
    }

    public void setConnection(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        try {
            statement = connection.createStatement();
            String queryPorts = "CREATE TABLE IF NOT EXISTS ports (p_id INTEGER PRIMARY KEY NOT NULL, name TEXT, world TEXT, x INTEGER, y INTEGER, z INTEGER)";
            statement.executeUpdate(queryPorts);
            String queryLinks = "CREATE TABLE IF NOT EXISTS links (l_id INTEGER PRIMARY KEY NOT NULL, p_id INTEGER, world TEXT, x INTEGER, y INTEGER, z INTEGER)";
            statement.executeUpdate(queryLinks);
            String queryCmds = "CREATE TABLE IF NOT EXISTS commands (c_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, p_id INTEGER, command TEXT, num_uses INTEGER, cooldown INTEGER)";
            statement.executeUpdate(queryCmds);
            String queryUses = "CREATE TABLE IF NOT EXISTS command_uses (u_id INTEGER PRIMARY KEY NOT NULL, c_id INTEGER, player TEXT, uses INTEGER, last_use INTEGER)";
            statement.executeUpdate(queryUses);
            statement.close();
        } catch (SQLException e) {
            System.err.println(FatPortConstants.MY_PLUGIN_NAME + " Create table error: " + e);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}