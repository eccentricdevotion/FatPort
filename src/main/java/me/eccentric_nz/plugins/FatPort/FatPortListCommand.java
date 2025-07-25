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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FatPortListCommand implements CommandExecutor {

    private final FatPort plugin;
    FatPortDatabase service = FatPortDatabase.getInstance();

    public FatPortListCommand(FatPort plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("fatport.list")) {
            try {
                Connection connection = service.getConnection();
                Statement statement = connection.createStatement();
                // get ports
                sender.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "FatPORTS");
                sender.sendMessage("----------");
                String queryPort = "SELECT * FROM ports ORDER BY name";
                ResultSet rsPort = statement.executeQuery(queryPort);
                if (rsPort.isBeforeFirst()) {
                    while (rsPort.next()) {
                        String p = rsPort.getString("name");
                        String w = rsPort.getString("world");
                        int x = rsPort.getInt("x");
                        int y = rsPort.getInt("y");
                        int z = rsPort.getInt("z");
                        sender.sendMessage(ChatColor.AQUA + p + ChatColor.RESET + " - " + w + ":" + x + ":" + y + ":" + z);
                    }
                    sender.sendMessage("----------");
                    rsPort.close();
                    sender.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "FatLINKS");
                    sender.sendMessage("----------");
                    // get links
                    String queryLink = "SELECT links.*, ports.name FROM links, ports WHERE links.p_id = ports.p_id ORDER BY ports.name";
                    ResultSet rsLink = statement.executeQuery(queryLink);
                    while (rsLink.next()) {
                        String p = rsLink.getString("name");
                        String w = rsLink.getString("world");
                        int x = rsLink.getInt("x");
                        int y = rsLink.getInt("y");
                        int z = rsLink.getInt("z");
                        sender.sendMessage(ChatColor.AQUA + p + ChatColor.RESET + " - " + w + ":" + x + ":" + y + ":" + z);
                    }
                    rsLink.close();
                } else {
                    sender.sendMessage(ChatColor.AQUA + "No FatPorts were found!");
                }
                statement.close();
            } catch (SQLException e) {
                plugin.debug("Could not get link block! " + e);
            }
            return true;
        }
        return false;
    }
}