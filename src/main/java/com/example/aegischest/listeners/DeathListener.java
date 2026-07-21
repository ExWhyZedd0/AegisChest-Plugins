package com.example.aegischest.listeners;

import com.example.aegischest.AegisChestPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class DeathListener implements Listener {

    private final AegisChestPlugin plugin;

    public DeathListener(AegisChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> drops = new java.util.ArrayList<>(event.getDrops());
        if (drops.isEmpty()) {
            return;
        }

        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        World world = player.getWorld();

        try {
            // Adjust Y coordinate if dying in the void
            if (deathLoc.getY() < world.getMinHeight()) {
                deathLoc.setY(world.getMinHeight() + 1);
            }

            // Place a dummy chest visually based on config
            Block block = findSafeBlock(deathLoc);
            Material markerType = plugin.getConfigMessage().getMarkerType();
            block.setType(markerType, false); // disable physics so it doesn't pop off mid-air
            
            if (markerType == Material.PLAYER_HEAD) {
                if (block.getState() instanceof org.bukkit.block.Skull skull) {
                    skull.setOwningPlayer(player);
                    skull.update();
                }
            }

            // Get or create the list of chests for this player
            List<com.example.aegischest.AegisChestData> playerChests = plugin.getActiveChests().computeIfAbsent(player.getUniqueId(), k -> new java.util.ArrayList<>());
            int newId = playerChests.size() + 1;

            // Spawn Hologram (name layer)
            Location nameHoloLoc = block.getLocation().add(0.5, 1.2, 0.5);
            org.bukkit.entity.ArmorStand nameHologram = null;
            try {
                nameHologram = world.spawn(nameHoloLoc, org.bukkit.entity.ArmorStand.class);
                nameHologram.setInvisible(true);
                nameHologram.setMarker(true);
                nameHologram.setCustomNameVisible(true);
                nameHologram.setGravity(false);
                nameHologram.setPersistent(false);
                nameHologram.customName(net.kyori.adventure.text.Component.text(player.getName() + "'s AegisChest", net.kyori.adventure.text.format.NamedTextColor.GOLD));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to spawn name hologram for " + player.getName() + " - " + e.getMessage());
            }

            // Spawn Hologram (timer layer)
            Location timerHoloLoc = block.getLocation().add(0.5, 1.5, 0.5);
            org.bukkit.entity.ArmorStand timerHologram = null;
            try {
                timerHologram = world.spawn(timerHoloLoc, org.bukkit.entity.ArmorStand.class);
                timerHologram.setInvisible(true);
                timerHologram.setMarker(true);
                timerHologram.setCustomNameVisible(true);
                timerHologram.setGravity(false);
                timerHologram.setPersistent(false);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to spawn timer hologram for " + player.getName() + " - " + e.getMessage());
            }

            long expireTime = System.currentTimeMillis() + plugin.getChestDurationMillis();
            
            com.example.aegischest.AegisChestData data = new com.example.aegischest.AegisChestData(
                    newId, player.getUniqueId(), player.getName(), block.getLocation(), expireTime, timerHologram, nameHologram, drops, 0);
            
            // Save TOTAL XP (only do this after chest data is created to ensure we can store it)
            int totalXp = calculateTotalExperience(player);
            if (totalXp > 0) {
                event.setDroppedExp(0);
                event.setNewExp(0);
                event.setNewLevel(0);
                event.setNewTotalExp(0);
            }
            
            // Update chest data with XP
            com.example.aegischest.AegisChestData finalData = new com.example.aegischest.AegisChestData(
                    newId, player.getUniqueId(), player.getName(), block.getLocation(), expireTime, timerHologram, nameHologram, drops, totalXp);

            playerChests.add(finalData);

            // ONLY clear drops now that the chest is fully registered and safe
            event.getDrops().clear();

            player.sendMessage(plugin.getConfigMessage().getMessage("death-message", "id", String.valueOf(newId)));
            plugin.getLogger().info("Successfully created AegisChest for " + player.getName() + " at " + block.getLocation() + " expiring at " + expireTime);
            
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "CRITICAL ERROR creating AegisChest for " + player.getName() + ". Items will drop naturally to prevent loss.", e);
            player.sendMessage(net.kyori.adventure.text.Component.text("A critical error occurred while creating your AegisChest! Your items have dropped on the ground normally.", net.kyori.adventure.text.format.NamedTextColor.RED));
        }
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

    private int calculateTotalExperience(Player player) {
        int level = player.getLevel();
        int exp = 0;
        if (level >= 32) {
            exp = (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 16) {
            exp = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            exp = level * level + 6 * level;
        }
        exp += Math.round(player.getExp() * player.getExpToLevel());
        return exp;
    }
}
