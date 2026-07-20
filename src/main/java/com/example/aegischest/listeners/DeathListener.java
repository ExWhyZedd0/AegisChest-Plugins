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

        // Adjust Y coordinate if dying in the void
        if (deathLoc.getY() < world.getMinHeight()) {
            deathLoc.setY(world.getMinHeight() + 1);
        }

        // Place a dummy chest visually based on config
        Block block = findSafeBlock(deathLoc);
        Material markerType = plugin.getConfigMessage().getMarkerType();
        block.setType(markerType);
        
        if (markerType == Material.PLAYER_HEAD) {
            if (block.getState() instanceof org.bukkit.block.Skull skull) {
                skull.setOwningPlayer(player);
                skull.update();
            }
        }

        // Cancel natural drops
        event.getDrops().clear();

        // Save TOTAL XP
        int totalXp = calculateTotalExperience(player);
        if (totalXp > 0) {
            event.setDroppedExp(0);
            event.setNewExp(0);
            event.setNewLevel(0);
            event.setNewTotalExp(0);
        }

        // Get or create the list of chests for this player
        List<com.example.aegischest.AegisChestData> playerChests = plugin.getActiveChests().computeIfAbsent(player.getUniqueId(), k -> new java.util.ArrayList<>());
        int newId = playerChests.size() + 1;

        // Spawn Hologram (name layer)
        Location nameHoloLoc = block.getLocation().add(0.5, 1.2, 0.5);
        org.bukkit.entity.ArmorStand nameHologram = world.spawn(nameHoloLoc, org.bukkit.entity.ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setMarker(true);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setPersistent(false);
            as.customName(net.kyori.adventure.text.Component.text(player.getName() + "'s AegisChest", net.kyori.adventure.text.format.NamedTextColor.GOLD));
        });

        // Spawn Hologram (timer layer)
        Location timerHoloLoc = block.getLocation().add(0.5, 1.5, 0.5);
        org.bukkit.entity.ArmorStand timerHologram = world.spawn(timerHoloLoc, org.bukkit.entity.ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setMarker(true);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setPersistent(false);
        });

        long expireTime = System.currentTimeMillis() + plugin.getChestDurationMillis();
        
        com.example.aegischest.AegisChestData data = new com.example.aegischest.AegisChestData(
                newId, player.getUniqueId(), player.getName(), block.getLocation(), expireTime, timerHologram, nameHologram, drops, totalXp);
        playerChests.add(data);

        player.sendMessage(plugin.getConfigMessage().getMessage("death-message", "id", String.valueOf(newId)));
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
