package com.example.aegischest;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public class AegisChestData {
    private final int chestId;
    private final UUID owner;
    private final String ownerName;
    private Location location;
    private final long expireTime;
    private ArmorStand timerHologram;
    private ArmorStand nameHologram;
    private final org.bukkit.inventory.ItemStack[] items;
    private final int xp;

    public AegisChestData(int chestId, UUID owner, String ownerName, Location location, long expireTime, ArmorStand timerHologram, ArmorStand nameHologram, org.bukkit.inventory.ItemStack[] items, int xp) {
        this.chestId = chestId;
        this.owner = owner;
        this.ownerName = ownerName;
        this.location = location;
        this.expireTime = expireTime;
        this.timerHologram = timerHologram;
        this.nameHologram = nameHologram;
        this.items = items;
        this.xp = xp;
    }

    public int getChestId() {
        return chestId;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public ArmorStand getTimerHologram() {
        return timerHologram;
    }

    public void setTimerHologram(ArmorStand timerHologram) {
        this.timerHologram = timerHologram;
    }

    public ArmorStand getNameHologram() {
        return nameHologram;
    }

    public void setNameHologram(ArmorStand nameHologram) {
        this.nameHologram = nameHologram;
    }

    public org.bukkit.inventory.ItemStack[] getItems() {
        return items;
    }

    public int getXp() {
        return xp;
    }
}
