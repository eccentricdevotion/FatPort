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
import org.apache.commons.lang.StringUtils;

public class FatPortCmdUtils {

    public Map<String, Integer> portCommand = new HashMap<String, Integer>();
    FatPortDatabase service = FatPortDatabase.getInstance();
    private FatPort plugin;

    public FatPortCmdUtils(FatPort plugin) {
        this.plugin = plugin;
    }

    public void insertCmd(int pid, String cmd, int num, int cooldown) {
        try {
            Connection connection = service.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO commands (p_id, command, num_uses, cooldown) VALUES (?,?,?,?)");
            statement.setInt(1, pid);
            statement.setString(2, cmd);
            statement.setInt(3, num);
            statement.setInt(4, cooldown);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.debug("Could not save command! " + e);
        }
    }

    public boolean hasCommand(int pid) {
        boolean bool = false;
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryCmd = "SELECT c_id FROM commands WHERE p_id = " + pid;
            ResultSet rsCmd = statement.executeQuery(queryCmd);
            if (rsCmd.isBeforeFirst()) {
                bool = true;
            }
            rsCmd.close();
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not check for command! " + e);
        }
        return bool;
    }

    public String getCommand(int pid, String name) {
        String command = "";
        try {
            Connection connection = service.getConnection();
            Statement statement = connection.createStatement();
            String queryCmd = "SELECT * FROM commands WHERE p_id = " + pid + " ORDER BY RANDOM()";
            ResultSet rsCmd = statement.executeQuery(queryCmd);
            String tmp = rsCmd.getString("command");
            portCommand.put(name, rsCmd.getInt("c_id"));
            command = StringUtils.replace(tmp, "@p", name);
            rsCmd.close();
            statement.close();
        } catch (SQLException e) {
            plugin.debug("Could not get command! " + e);
        }
        return command;
    }

    public boolean playerIsAllowed(String name) {
        boolean bool = false;
        if (portCommand.containsKey(name)) {
            int cid = portCommand.get(name);
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryCmd = "SELECT num_uses, cooldown FROM commands WHERE c_id = " + cid;
                ResultSet rsCmd = statement.executeQuery(queryCmd);
                if (rsCmd.next()) {
                    int cmd_num = (rsCmd.getInt("num_uses") < 0) ? Integer.MAX_VALUE : rsCmd.getInt("num_uses");
                    long cooldown = (rsCmd.getLong("cooldown") > 0) ? rsCmd.getLong("cooldown") : 0L;
                    String queryPlayer = "SELECT uses, last_use FROM command_uses WHERE c_id = " + cid + " AND player = '" + name + "'";
                    ResultSet rsPlayer = statement.executeQuery(queryPlayer);
                    int uses = 0;
                    long now = System.currentTimeMillis();
                    long last = 0;
                    if (rsPlayer.next()) {
                        uses = rsPlayer.getInt("uses");
                        last = rsPlayer.getLong("last_use") + (cooldown * 1000L);
                    }
                    if (uses < cmd_num && last < now) {
                        bool = true;
                    }
                }
                rsCmd.close();
                statement.close();
            } catch (SQLException e) {
                plugin.debug("Could not check for command! " + e);
            }
        }
        return bool;
    }

    public void setUse(String name) {
        if (portCommand.containsKey(name)) {
            int cid = portCommand.get(name);
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                String queryPlayer = "SELECT u_id FROM command_uses WHERE c_id = " + cid + " AND player = '" + name + "'";
                ResultSet rsPlayer = statement.executeQuery(queryPlayer);
                String queryUses;
                long time = System.currentTimeMillis();
                if (rsPlayer.next()) {
                    queryUses = "UPDATE command_uses SET uses = (uses+1), last_use = " + time + " WHERE u_id = " + rsPlayer.getInt("u_id");
                } else {
                    queryUses = "INSERT INTO command_uses (c_id, player, uses, last_use) VALUES (" + cid + ", '" + name + "', 1, " + time + ")";
                }
                statement.executeUpdate(queryUses);
                portCommand.remove(name);
            } catch (SQLException e) {
                plugin.debug("Could not save command! " + e);
            }
        }
    }
}