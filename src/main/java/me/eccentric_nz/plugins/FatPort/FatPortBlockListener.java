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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class FatPortBlockListener implements Listener {

    private FatPort plugin;

    public FatPortBlockListener(FatPort plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        String pName = player.getName();
        if (plugin.portCheck.isPortBlock(loc, pName, true)) {
            if (player.hasPermission("fatport.remove")) {
                String data[] = plugin.portCheck.portData.get(pName);
                int portID = Integer.parseInt(data[0]);
                String portName = data[1];
                plugin.portCheck.deletePort(portID);
                plugin.portCheck.portData.remove(pName);
                player.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "Port Block" + ChatColor.AQUA + portName + ChatColor.RED + " has been removed.");
            } else {
                player.sendMessage(ChatColor.DARK_RED + "You do not have permission to remove MultiPorts.");
                event.setCancelled(true);
            }
        }
    }
}
