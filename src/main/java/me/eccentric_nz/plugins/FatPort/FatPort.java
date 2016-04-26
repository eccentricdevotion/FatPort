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

import java.io.File;
import java.io.IOException;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FatPort extends JavaPlugin {

    protected static FatPort plugin;
    static File blocks = null;
    static File links = null;
    FatPortDatabase service = FatPortDatabase.getInstance();
    private final FatPortPlayerListener playerListener = new FatPortPlayerListener(this);
    private final FatPortBlockListener blockListener = new FatPortBlockListener(this);
    public static Server server;
    public FatPortUtils portCheck;
    public FatPortCmdUtils cmdCheck;
    private FileConfiguration config = null;

    @Override
    public void onEnable() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
        } catch (Exception e) {
            System.err.println("FatPort could not create directory!");
        }

        getDataFolder().setWritable(true);
        getDataFolder().setExecutable(true);

        plugin = this;
        this.saveDefaultConfig();

        try {
            String path = getDataFolder() + File.separator + "FatPort.db";
            service.setConnection(path);
            service.createTables();
        } catch (Exception e) {
            System.err.println(FatPortConstants.MY_PLUGIN_NAME + " Connection and Tables Error: " + e);
        }

        server = this.getServer();
        PluginManager pm = server.getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockListener, this);

        getCommand("addport").setExecutor(new FatPortAddCommand(this));
        getCommand("linkport").setExecutor(new FatPortLinkCommand(this));
        getCommand("listport").setExecutor(new FatPortListCommand(this));
        getCommand("addcmd").setExecutor(new FatPortCmdCommand(this));

        portCheck = new FatPortUtils(this);
        cmdCheck = new FatPortCmdUtils(this);

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        File myconfigfile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(myconfigfile);
        if (!config.contains("use_radius")) {
            getConfig().set("use_radius", false);
            getConfig().set("min", 0);
            getConfig().set("max", 10);
            saveConfig();
            System.out.println("[FatPort] Added new config options");
        }
    }

    @Override
    public void onDisable() {
        this.saveConfig();
        try {
            service.connection.close();
        } catch (Exception e) {
            debug("Could not close database connection! " + e);
        }
    }

    public boolean anonymousCheck(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute that command, I don't know who you are!");
            return true;
        } else {
            return false;
        }
    }

    public void debug(Object o) {
        if (getConfig().getBoolean("debug") == true) {
            System.out.println("[FatPort Debug] " + o);
        }
    }
}
