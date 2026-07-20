package com.example.aegischest;

import com.example.aegischest.commands.FetchCommand;
import com.example.aegischest.listeners.ChestProtectionListener;
import com.example.aegischest.listeners.DeathListener;
import com.example.aegischest.utils.ConfigMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AegisChestPlugin extends JavaPlugin {

    private final Map<UUID, List<AegisChestData>> activeChests = new HashMap<>();
    private final Map<UUID, List<AegisChestData>> expiredChests = new HashMap<>();
    private NamespacedKey ownerKey;
    private NamespacedKey xpKey;
    private NamespacedKey idKey;
    private long chestDurationMillis;
    private StorageManager storageManager;
    private ConfigMessage configMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        configMessage = new ConfigMessage(this);
        
        storageManager = new StorageManager(this);
        storageManager.loadChests();

        // Load duration in seconds from config, default 3600 (1 hour)
        int durationSeconds = getConfig().getInt("aegischest-duration", 3600);
        this.chestDurationMillis = durationSeconds * 1000L;

        this.ownerKey = new NamespacedKey(this, "aegischest_owner");
        this.xpKey = new NamespacedKey(this, "aegischest_xp");
        this.idKey = new NamespacedKey(this, "aegischest_id");

        // Register events
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestProtectionListener(this), this);
        
        // Register command
        getServer().getCommandMap().register(getName().toLowerCase(), new FetchCommand(this));

        // Start Hologram / Expiration ticker
        getServer().getScheduler().runTaskTimer(this, () -> {
            long now = System.currentTimeMillis();
            for (List<AegisChestData> playerChests : activeChests.values()) {
                // Use a standard iterator or removeIf to handle removals safely
                playerChests.removeIf(chestData -> {
                    long remaining = chestData.getExpireTime() - now;
                    
                    // Expired
                    if (remaining <= 0) {
                        if (chestData.getTimerHologram() != null) {
                            chestData.getTimerHologram().remove();
                        }
                        if (chestData.getNameHologram() != null) {
                            chestData.getNameHologram().remove();
                        }
                        
                        Block block = chestData.getLocation().getBlock();
                        if (block.getType() == Material.CHEST || block.getType() == Material.PLAYER_HEAD) {
                            block.setType(Material.AIR);
                        }
                        
                        // Add to expired chests map
                        List<AegisChestData> expList = expiredChests.computeIfAbsent(chestData.getOwner(), k -> new ArrayList<>());
                        expList.add(chestData);
                        return true;
                    }
                    
                    // Update timer hologram
                    if (chestData.getTimerHologram() != null && chestData.getTimerHologram().isValid()) {
                        int secondsLeft = (int) (remaining / 1000);
                        int m = secondsLeft / 60;
                        int s = secondsLeft % 60;
                        String timeStr = String.format("%02d:%02d", m, s);
                        
                        Component timeComp = Component.text(timeStr, NamedTextColor.BLUE);
                        chestData.getTimerHologram().customName(timeComp);
                    }
                    return false;
                });
            }
        }, 20L, 20L); // run every 1 second (20 ticks)

        getLogger().info("AegisChest enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.saveChests();
        }
        getLogger().info("AegisChest disabled successfully!");
    }

    public Map<UUID, List<AegisChestData>> getActiveChests() {
        return activeChests;
    }

    public Map<UUID, List<AegisChestData>> getExpiredChests() {
        return expiredChests;
    }

    public NamespacedKey getOwnerKey() {
        return ownerKey;
    }

    public NamespacedKey getXpKey() {
        return xpKey;
    }

    public NamespacedKey getIdKey() {
        return idKey;
    }

    public long getChestDurationMillis() {
        return chestDurationMillis;
    }

    public ConfigMessage getConfigMessage() {
        return configMessage;
    }
}
