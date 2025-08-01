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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FatPortPlayerListener implements Listener {

    private final FatPort plugin;
    public static Map<UUID, Block> SelectBlock = new HashMap<>();

    public FatPortPlayerListener(FatPort plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            //Allow block / Item in hand here - May make config for any block
            if (player.getInventory().getItemInMainHand().getType() == Material.valueOf(plugin.getConfig().getString("wand"))) {
                if (player.hasPermission("fatport.add")) {
                    SelectBlock.put(player.getUniqueId(), block);
                    player.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "Port Block selected.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        World world = event.getTo().getWorld();
        Player player = event.getPlayer();
        String pName = player.getName();
        UUID uuid = player.getUniqueId();
        Location destLoc;
        //getY()-.2 = at what point below the player to scan
        Location loc = new Location(world, event.getTo().getX(), event.getTo().getY() - .2, event.getTo().getZ());
        boolean isFatPort = plugin.portCheck.isPortBlock(loc, uuid, false);
        if (isFatPort && player.hasPermission("fatport.use")) {
            int pid = plugin.portCheck.portTravel.get(uuid);
            // check if this is a TP block or a Command block
            if (plugin.cmdCheck.hasCommand(pid)) {
                // run command
                String cmd = plugin.cmdCheck.getCommand(pid, pName);
                if (plugin.cmdCheck.playerIsAllowed(pName)) {
                    //CommandSender sender = plugin.getServer().getConsoleSender();
                    plugin.getServer().dispatchCommand(player, cmd);
                    plugin.cmdCheck.setUse(pName);
                }
            } else {
                // send to random teleport location
                destLoc = (plugin.getConfig().getBoolean("use_radius")) ? plugin.portCheck.getRadialDest(pid) : plugin.portCheck.getDest(pid);
                if (destLoc != null) {
                    destLoc.setPitch(player.getLocation().getPitch());
                    destLoc.setYaw(player.getLocation().getYaw());
                    player.teleport(destLoc);
                } else {
                    player.sendMessage(FatPortConstants.MY_PLUGIN_NAME + "You fell down a hole!");
                }
            }
        }
    }
}
