/*
 * Copyright Rob Rate 2012.
 *
 * Licensed under The GNU General Public License v3.0, a copy of the licence is included in the JAR file.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package me.eccentric_nz.plugins.FatPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class FatPortUtils {

    public static Map<String, Block> PortBlock = new HashMap<String, Block>();
    public static Map<String, Block> PortLinks = new HashMap<String, Block>();
    public Map<String, String[]> portData = new HashMap<String, String[]>();
    public Map<String, Integer> portTravel = new HashMap<String, Integer>();
    public Map<String, Integer> linkData = new HashMap<String, Integer>();
    FatPortDatabase service = FatPortDatabase.getInstance();
    private FatPort plugin;

    public FatPortUtils(FatPort plugin) {
        this.plugin = plugin;
    }

    public Location getDest(int pid) {
        Location loc = null;
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryLink = "SELECT * FROM links WHERE p_id = " + pid + " ORDER BY RANDOM()";
            ResultSet rsLink = statement.executeQuery(queryLink);
            if (rsLink.next()) {
                World w = plugin.getServer().getWorld(rsLink.getString("world"));
                int x = rsLink.getInt("x");
                int y = rsLink.getInt("y");
                int z = rsLink.getInt("z");
                loc = new Location(w, x, y, z);
                loc.add(.5, 1, .5);
            }
        } catch (SQLException e) {
            plugin.debug("Could not get link block! " + e);
        }
        return loc;
    }

    public boolean containsPort(String name, Location loc) {
        boolean b = false;
        String w = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            // check block is not in use
            String queryPort = "SELECT name FROM ports WHERE world = '" + w + "' AND x = " + x + " AND y = " + y + " AND z = " + z;
            ResultSet rsPort = statement.executeQuery(queryPort);
            if (rsPort.isBeforeFirst()) {
                b = true;
            }
            rsPort.close();
            // check name is not in use
            String queryName = "SELECT name FROM ports WHERE name = '" + name + "'";
            ResultSet rsName = statement.executeQuery(queryName);
            if (rsName.isBeforeFirst()) {
                b = true;
            }
            rsName.close();
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not check port block and name! " + e);
        }
        return b;
    }

    public void insertPort(String name, Location loc) {
        String w = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        try {
            Connection connection = service.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO ports (name, world, x, y, z) VALUES (?,?,?,?,?)");
            // name 1, world 2, x 3, y 4, z 5
            statement.setString(1, name);
            statement.setString(2, w);
            statement.setInt(3, x);
            statement.setInt(4, y);
            statement.setInt(5, z);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.debug("Could not save port block! " + e);
        }
    }

    public int validPortName(String name) {
        int pid = 0;
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryName = "SELECT p_id FROM ports WHERE name = '" + name + "'";
            ResultSet rsName = statement.executeQuery(queryName);
            if (rsName.next()) {
                pid = rsName.getInt("p_id");
            }
            rsName.close();
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not check port name! " + e);
        }
        return pid;
    }

    public void insertLink(int pid, Location loc) {
        String w = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        try {
            Connection connection = service.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO links (p_id, world, x, y, z) VALUES (?,?,?,?,?)");
            // name 1, world 2, x 3, y 4, z 5
            statement.setInt(1, pid);
            statement.setString(2, w);
            statement.setInt(3, x);
            statement.setInt(4, y);
            statement.setInt(5, z);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.debug("Could not save port block! " + e);
        }
    }

    public boolean isPortBlock(Location loc, String p, boolean b) {
        boolean bool = false;
        String w = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryPort = "SELECT p_id, name FROM ports WHERE world = '" + w + "' AND x = " + x + " AND y = " + y + " AND z = " + z;
            ResultSet rsPort = statement.executeQuery(queryPort);
            if (rsPort.next()) {
                String[] data = new String[2];
                data[0] = rsPort.getString("p_id");
                data[1] = rsPort.getString("name");
                if (b == true) {
                    portData.put(p, data);
                } else {
                    portTravel.put(p, rsPort.getInt("p_id"));
                }
                bool = true;
            }
            rsPort.close();
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not check port block! " + e);
        }
        return bool;
    }

    public void deletePort(int pid) {
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryPort = "DELETE FROM ports WHERE p_id = " + pid;
            statement.executeUpdate(queryPort);
            String queryLinks = "DELETE FROM links WHERE p_id = " + pid;
            statement.executeUpdate(queryLinks);
        } catch (SQLException e) {
            plugin.debug("Could not delete port! " + e);
        }
    }

    public boolean isLinkBlock(Location loc, String p, boolean b) {
        boolean bool = false;
        String w = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryLink = "SELECT l_id FROM links WHERE world = '" + w + "' AND x = " + x + " AND y = " + y + " AND z = " + z;
            ResultSet rsLink = statement.executeQuery(queryLink);
            if (rsLink.next()) {
                linkData.put(p, rsLink.getInt("l_id"));
                bool = true;
            }
            rsLink.close();
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not check port block! " + e);
        }
        return bool;
    }

    public void deleteLink(int lid) {
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryLink = "DELETE FROM links WHERE l_id = " + lid;
            statement.executeUpdate(queryLink);
        } catch (SQLException e) {
            plugin.debug("Could not delete link! " + e);
        }
    }
}
