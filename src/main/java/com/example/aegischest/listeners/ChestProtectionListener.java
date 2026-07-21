package com.example.aegischest.listeners;

import com.example.aegischest.AegisChestPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ChestProtectionListener implements Listener {

    private final AegisChestPlugin plugin;

    public ChestProtectionListener(AegisChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        org.bukkit.Material type = block.getType();
        if (type != org.bukkit.Material.CHEST && type != org.bukkit.Material.PLAYER_HEAD) {
            return;
        }

        com.example.aegischest.AegisChestData chestData = getChestAt(block.getLocation());
        if (chestData != null) {
            Player player = event.getPlayer();
            
            boolean isOwner = chestData.getOwner().equals(player.getUniqueId());
            boolean isOp = player.isOp();

            if (!isOwner && !isOp) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigMessage().getMessage("not-owner"));
                return;
            }

            // Auto-loot and auto-equip logic
            event.setCancelled(true); // Cancel vanilla opening of the chest

            // Give XP
            player.giveExp(chestData.getXp());

            // Loot items
            if (chestData.getItems() != null) {
                org.bukkit.inventory.ItemStack[] savedContents = chestData.getItems();
                org.bukkit.inventory.PlayerInventory inv = player.getInventory();
                
                for (int i = 0; i < savedContents.length; i++) {
                    org.bukkit.inventory.ItemStack item = savedContents[i];
                    if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
                    
                    // Try auto equip armor
                    if (isArmor(item.getType())) {
                        if (autoEquipArmor(player, item)) {
                            continue; // Successfully equipped
                        }
                    }

                    // Try to put it exactly in its original slot
                    org.bukkit.inventory.ItemStack existing = null;
                    if (i < 41) { // Normal inventory bounds
                        existing = inv.getItem(i);
                    }

                    if (i < 41 && (existing == null || existing.getType() == org.bukkit.Material.AIR)) {
                        inv.setItem(i, item);
                    } else {
                        // Slot is taken, or it's an overflow item (i >= 41)
                        java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftovers = inv.addItem(item);
                        // Drop leftovers on the ground if inventory is full
                        for (org.bukkit.inventory.ItemStack leftover : leftovers.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                        }
                    }
                }
            }

            // Destroy chest
            block.setType(org.bukkit.Material.AIR);
            
            // Remove from active chests and clear hologram
            java.util.List<com.example.aegischest.AegisChestData> chests = plugin.getActiveChests().get(chestData.getOwner());
            if (chests != null) {
                chests.remove(chestData);
                if (chestData.getTimerHologram() != null) {
                    chestData.getTimerHologram().remove();
                }
                if (chestData.getNameHologram() != null) {
                    chestData.getNameHologram().remove();
                }
            }

            player.sendMessage(plugin.getConfigMessage().getMessage("chest-collected"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
        }
    }

    private com.example.aegischest.AegisChestData getChestAt(org.bukkit.Location loc) {
        for (java.util.List<com.example.aegischest.AegisChestData> list : plugin.getActiveChests().values()) {
            for (com.example.aegischest.AegisChestData data : list) {
                if (data.getLocation().equals(loc)) {
                    return data;
                }
            }
        }
        return null;
    }

    private boolean isArmor(org.bukkit.Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || 
               name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
               name.equals("ELYTRA") || name.equals("SHIELD") || name.equals("TOTEM_OF_UNDYING");
    }

    private boolean autoEquipArmor(Player player, org.bukkit.inventory.ItemStack armor) {
        String name = armor.getType().name();
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        
        if (name.endsWith("_HELMET") && inv.getHelmet() == null) {
            inv.setHelmet(armor);
            return true;
        }
        if ((name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) && inv.getChestplate() == null) {
            inv.setChestplate(armor);
            return true;
        }
        if (name.endsWith("_LEGGINGS") && inv.getLeggings() == null) {
            inv.setLeggings(armor);
            return true;
        }
        if (name.endsWith("_BOOTS") && inv.getBoots() == null) {
            inv.setBoots(armor);
            return true;
        }
        if (name.equals("SHIELD") || name.equals("TOTEM_OF_UNDYING")) {
            if (inv.getItemInOffHand().getType() == org.bukkit.Material.AIR) {
                inv.setItemInOffHand(armor);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        org.bukkit.Material type = block.getType();
        if (type == org.bukkit.Material.CHEST || type == org.bukkit.Material.PLAYER_HEAD) {
            com.example.aegischest.AegisChestData chestData = getChestAt(block.getLocation());
            if (chestData != null) {
                Player player = event.getPlayer();

                if (!chestData.getOwner().equals(player.getUniqueId()) && !player.isOp()) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigMessage().getMessage("not-owner"));
                } else {
                    // The owner (or OP) broke it, drop virtual contents naturally
                    if (chestData.getItems() != null) {
                        for (int i = 0; i < chestData.getItems().length; i++) {
                            org.bukkit.inventory.ItemStack item = chestData.getItems()[i];
                            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                                block.getWorld().dropItemNaturally(block.getLocation(), item);
                            }
                        }
                    }
                    
                    // Remove from tracking map and clear hologram
                    java.util.List<com.example.aegischest.AegisChestData> chests = plugin.getActiveChests().get(chestData.getOwner());
                    if (chests != null) {
                        chests.remove(chestData);
                        if (chestData.getTimerHologram() != null) {
                            chestData.getTimerHologram().remove();
                        }
                        if (chestData.getNameHologram() != null) {
                            chestData.getNameHologram().remove();
                        }
                    }
                }
            }
        }
    }
}
