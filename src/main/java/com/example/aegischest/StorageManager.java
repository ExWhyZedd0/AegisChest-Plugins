package com.example.aegischest;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import java.util.logging.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StorageManager {
    private final AegisChestPlugin plugin;
    private final File chestsFile;
    private FileConfiguration chestsConfig;

    public StorageManager(AegisChestPlugin plugin) {
        this.plugin = plugin;
        this.chestsFile = new File(plugin.getDataFolder(), "chests.yml");
    }

    public void loadChests() {
        if (!chestsFile.exists()) {
            return;
        }

        chestsConfig = YamlConfiguration.loadConfiguration(chestsFile);
        Map<UUID, List<AegisChestData>> activeChests = plugin.getActiveChests();
        activeChests.clear();

        if (chestsConfig.getConfigurationSection("chests") != null) {
            for (String uuidStr : chestsConfig.getConfigurationSection("chests").getKeys(false)) {
                UUID ownerUuid = UUID.fromString(uuidStr);
                List<AegisChestData> playerChests = new ArrayList<>();
                
                for (String chestIdStr : chestsConfig.getConfigurationSection("chests." + uuidStr).getKeys(false)) {
                    int chestId = Integer.parseInt(chestIdStr);
                    String path = "chests." + uuidStr + "." + chestIdStr;

                    String ownerName = chestsConfig.getString(path + ".ownerName");
                    long expireTime = chestsConfig.getLong(path + ".expireTime");
                    int xp = chestsConfig.getInt(path + ".xp");
                    
                    World world = Bukkit.getWorld(chestsConfig.getString(path + ".location.world"));
                    if (world == null) continue;
                    
                    double x = chestsConfig.getDouble(path + ".location.x");
                    double y = chestsConfig.getDouble(path + ".location.y");
                    double z = chestsConfig.getDouble(path + ".location.z");
                    Location loc = new Location(world, x, y, z);
                    
                    List<ItemStack> items = (List<ItemStack>) chestsConfig.getList(path + ".items");

                    // Respawn hologram (name layer)
                    Location nameHoloLoc = loc.clone().add(0.5, 1.2, 0.5);
                    org.bukkit.entity.ArmorStand nameHologram = world.spawn(nameHoloLoc, org.bukkit.entity.ArmorStand.class);
                    nameHologram.setInvisible(true);
                    nameHologram.setMarker(true);
                    nameHologram.setCustomNameVisible(true);
                    nameHologram.setGravity(false);
                    nameHologram.setPersistent(false);
                    nameHologram.customName(net.kyori.adventure.text.Component.text(ownerName + "'s AegisChest", net.kyori.adventure.text.format.NamedTextColor.GOLD));

                    // Respawn hologram (timer layer)
                    Location timerHoloLoc = loc.clone().add(0.5, 1.5, 0.5);
                    org.bukkit.entity.ArmorStand timerHologram = world.spawn(timerHoloLoc, org.bukkit.entity.ArmorStand.class);
                    timerHologram.setInvisible(true);
                    timerHologram.setMarker(true);
                    timerHologram.setCustomNameVisible(true);
                    timerHologram.setGravity(false);
                    timerHologram.setPersistent(false);

                    AegisChestData chest = new AegisChestData(chestId, ownerUuid, ownerName, loc, expireTime, timerHologram, nameHologram, items, xp);
                    playerChests.add(chest);
                }
                if (!playerChests.isEmpty()) {
                    activeChests.put(ownerUuid, playerChests);
                }
            }
        }

        Map<UUID, List<AegisChestData>> expiredChests = plugin.getExpiredChests();
        expiredChests.clear();
        if (chestsConfig.getConfigurationSection("expired_chests") != null) {
            for (String uuidStr : chestsConfig.getConfigurationSection("expired_chests").getKeys(false)) {
                UUID ownerUuid = UUID.fromString(uuidStr);
                List<AegisChestData> playerChests = new ArrayList<>();
                for (String chestIdStr : chestsConfig.getConfigurationSection("expired_chests." + uuidStr).getKeys(false)) {
                    int chestId = Integer.parseInt(chestIdStr);
                    String path = "expired_chests." + uuidStr + "." + chestIdStr;

                    String ownerName = chestsConfig.getString(path + ".ownerName");
                    long expireTime = chestsConfig.getLong(path + ".expireTime");
                    int xp = chestsConfig.getInt(path + ".xp");
                    
                    World world = Bukkit.getWorld(chestsConfig.getString(path + ".location.world"));
                    if (world == null) continue;
                    
                    double x = chestsConfig.getDouble(path + ".location.x");
                    double y = chestsConfig.getDouble(path + ".location.y");
                    double z = chestsConfig.getDouble(path + ".location.z");
                    Location loc = new Location(world, x, y, z);
                    
                    List<ItemStack> items = (List<ItemStack>) chestsConfig.getList(path + ".items");

                    AegisChestData chest = new AegisChestData(chestId, ownerUuid, ownerName, loc, expireTime, null, null, items, xp);
                    playerChests.add(chest);
                }
                if (!playerChests.isEmpty()) {
                    expiredChests.put(ownerUuid, playerChests);
                }
            }
        }


        
        // Delete the file after loading to prevent duplicate loading if server crashes before next save
        chestsFile.delete();
    }

    public void saveChests() {
        chestsConfig = new YamlConfiguration();
        
        Map<UUID, List<AegisChestData>> activeChests = plugin.getActiveChests();
        Map<UUID, List<AegisChestData>> expiredChests = plugin.getExpiredChests();

        if (activeChests.isEmpty() && expiredChests.isEmpty()) {
            if (chestsFile.exists()) chestsFile.delete();
            return;
        }

        for (Map.Entry<UUID, List<AegisChestData>> entry : activeChests.entrySet()) {
            UUID ownerUuid = entry.getKey();
            for (AegisChestData chest : entry.getValue()) {
                String path = "chests." + ownerUuid.toString() + "." + chest.getChestId();
                
                chestsConfig.set(path + ".ownerName", chest.getOwnerName());
                chestsConfig.set(path + ".expireTime", chest.getExpireTime());
                chestsConfig.set(path + ".xp", chest.getXp());
                
                Location loc = chest.getLocation();
                chestsConfig.set(path + ".location.world", loc.getWorld().getName());
                chestsConfig.set(path + ".location.x", loc.getX());
                chestsConfig.set(path + ".location.y", loc.getY());
                chestsConfig.set(path + ".location.z", loc.getZ());
                
                chestsConfig.set(path + ".items", chest.getItems());
                
                // Cleanup hologram properly on shutdown
                if (chest.getTimerHologram() != null) {
                    chest.getTimerHologram().remove();
                }
                if (chest.getNameHologram() != null) {
                    chest.getNameHologram().remove();
                }
            }
        }

        for (Map.Entry<UUID, List<AegisChestData>> entry : expiredChests.entrySet()) {
            UUID ownerUuid = entry.getKey();
            for (AegisChestData chest : entry.getValue()) {
                String path = "expired_chests." + ownerUuid.toString() + "." + chest.getChestId();
                
                chestsConfig.set(path + ".ownerName", chest.getOwnerName());
                chestsConfig.set(path + ".expireTime", chest.getExpireTime());
                chestsConfig.set(path + ".xp", chest.getXp());
                
                Location loc = chest.getLocation();
                chestsConfig.set(path + ".location.world", loc.getWorld().getName());
                chestsConfig.set(path + ".location.x", loc.getX());
                chestsConfig.set(path + ".location.y", loc.getY());
                chestsConfig.set(path + ".location.z", loc.getZ());
                
                chestsConfig.set(path + ".items", chest.getItems());
            }
        }

        try {
            chestsConfig.save(chestsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save chests to chests.yml!", e);
        }
    }
}
