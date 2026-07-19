package com.example.aegischest.commands;

import com.example.aegischest.AegisChestPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class FetchCommand extends Command {

    private final AegisChestPlugin plugin;

    public FetchCommand(AegisChestPlugin plugin) {
        super("aegischest");
        this.setAliases(List.of("ac"));
        this.setDescription("Fetch your AegisChest");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("fetch")) {
                handleFetch(player, args);
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                handleList(player);
                return true;
            }
        }
        
        // If they just typed /aegischest or something else
        player.sendMessage(plugin.getConfigMessage().getMessage("fetch-usage"));
        return true;
    }

    private void handleList(Player player) {
        java.util.List<com.example.aegischest.AegisChestData> chests = plugin.getActiveChests().get(player.getUniqueId());
        if (chests == null || chests.isEmpty()) {
            player.sendMessage(Component.text("You do not have any active AegisChests.", NamedTextColor.RED));
            return;
        }

        player.sendMessage(plugin.getConfigMessage().getMessage("list-header"));
        long now = System.currentTimeMillis();
        for (com.example.aegischest.AegisChestData chest : chests) {
            long remaining = chest.getExpireTime() - now;
            int secondsLeft = Math.max(0, (int) (remaining / 1000));
            int m = secondsLeft / 60;
            int s = secondsLeft % 60;
            String timeStr = String.format("%02d:%02d", m, s);
            
            Location loc = chest.getLocation();
            player.sendMessage(plugin.getConfigMessage().getMessage("list-item", 
                "id", String.valueOf(chest.getChestId()),
                "x", String.valueOf(loc.getBlockX()),
                "y", String.valueOf(loc.getBlockY()),
                "z", String.valueOf(loc.getBlockZ()),
                "time", timeStr
            ));
        }
    }

    private void handleFetch(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        java.util.List<com.example.aegischest.AegisChestData> chests = plugin.getActiveChests().get(uuid);

        if (chests == null || chests.isEmpty()) {
            player.sendMessage(Component.text("You do not have any active AegisChests.", NamedTextColor.RED));
            return;
        }

        com.example.aegischest.AegisChestData targetChest = null;
        if (chests.size() == 1) {
            targetChest = chests.get(0);
        } else {
            if (args.length < 2) {
                player.sendMessage(plugin.getConfigMessage().getMessage("fetch-multiple"));
                return;
            }
            try {
                int id = Integer.parseInt(args[1]);
                for (com.example.aegischest.AegisChestData chest : chests) {
                    if (chest.getChestId() == id) {
                        targetChest = chest;
                        break;
                    }
                }
            } catch (NumberFormatException ignored) {}
            
            if (targetChest == null) {
                player.sendMessage(plugin.getConfigMessage().getMessage("invalid-id"));
                return;
            }
        }

        Location oldLoc = targetChest.getLocation();
        Block oldBlock = oldLoc.getBlock();
        org.bukkit.Material type = oldBlock.getType();
        if (type != Material.CHEST && type != Material.PLAYER_HEAD) {
            player.sendMessage(plugin.getConfigMessage().getMessage("chest-broken"));
            chests.remove(targetChest);
            if (targetChest.getTimerHologram() != null) {
                targetChest.getTimerHologram().remove();
            }
            if (targetChest.getNameHologram() != null) {
                targetChest.getNameHologram().remove();
            }
            return;
        }

        // Set old chest block to AIR
        oldBlock.setType(Material.AIR);

        // Calculate new location (2 blocks in front, horizontal only)
        Location playerLoc = player.getLocation();
        org.bukkit.util.Vector dir = playerLoc.getDirection();
        dir.setY(0);
        if (dir.lengthSquared() > 0) {
            dir.normalize();
        } else {
            dir = new org.bukkit.util.Vector(1, 0, 0);
        }
        
        Location newLoc = playerLoc.clone().add(dir.multiply(2));
        newLoc.setY(playerLoc.getBlockY()); // Same Y as the player's feet
        newLoc.setX(newLoc.getBlockX());
        newLoc.setZ(newLoc.getBlockZ());

        Block newBlock = findSafeBlock(newLoc);
        Material markerType = plugin.getConfigMessage().getMarkerType();
        newBlock.setType(markerType);
        
        if (markerType == Material.PLAYER_HEAD) {
            if (newBlock.getState() instanceof org.bukkit.block.Skull skull) {
                skull.setOwningPlayer(player);
                skull.update();
            }
        }

        // Update tracking data
        targetChest.setLocation(newBlock.getLocation());
        
        // Teleport holograms
        if (targetChest.getNameHologram() != null && targetChest.getNameHologram().isValid()) {
            Location holoLoc = newBlock.getLocation().add(0.5, 1.2, 0.5);
            targetChest.getNameHologram().teleport(holoLoc);
        }
        if (targetChest.getTimerHologram() != null && targetChest.getTimerHologram().isValid()) {
            Location holoLoc = newBlock.getLocation().add(0.5, 1.5, 0.5);
            targetChest.getTimerHologram().teleport(holoLoc);
        }

        player.sendMessage(plugin.getConfigMessage().getMessage("teleported", 
            "x", String.valueOf(newBlock.getX()),
            "y", String.valueOf(newBlock.getY()),
            "z", String.valueOf(newBlock.getZ())
        ));
        
        plugin.getLogger().info("Player " + player.getName() + " fetched their AegisChest to X: " + newBlock.getX() + ", Y: " + newBlock.getY() + ", Z: " + newBlock.getZ());
    }

    private Block findSafeBlock(Location startLoc) {
        Location loc = startLoc.clone();
        // Search upwards up to 10 blocks to find an empty space
        for (int i = 0; i < 10; i++) {
            if (loc.getBlock().getType().isAir()) {
                return loc.getBlock();
            }
            loc.add(0, 1, 0);
        }
        // If no air found above, just return the start location (will replace whatever is there)
        return startLoc.getBlock();
    }
}
