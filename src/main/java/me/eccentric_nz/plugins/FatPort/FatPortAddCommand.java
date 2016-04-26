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
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FatPortAddCommand implements CommandExecutor {

    private final FatPort plugin;

    public FatPortAddCommand(FatPort plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arg) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (player == null) {
            sender.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "This command requires player interaction and cannot be run from the console!");
            return true;
        }
        if ((arg.length == 1) && player.hasPermission("fatport.add")) {
            String name = arg[0];
            // check name is valid

            Block block = FatPortPlayerListener.SelectBlock.get(player.getUniqueId());
            if (block == null) {
                player.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "Block has not been selected.");
                return false;
            }
            Location loc = block.getLocation();
            // check ports make sure block is not in use
            boolean blockInUse = plugin.portCheck.containsPort(name, loc);
            if (!blockInUse) {
                plugin.portCheck.insertPort(name, loc);
                player.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "Port Block " + ChatColor.AQUA + name + ChatColor.RESET + " has been added.");
                FatPortPlayerListener.SelectBlock.remove(player.getUniqueId());

            } else {
                player.sendMessage(FatPortConstants.MY_PLUGIN_NAME + name + ChatColor.RED + " already exists, or block is in use.");
            }
            return true;
        }
        return false;
    }
}
