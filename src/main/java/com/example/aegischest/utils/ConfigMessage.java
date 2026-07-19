package com.example.aegischest.utils;

import com.example.aegischest.AegisChestPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

public class ConfigMessage {

    private final AegisChestPlugin plugin;

    public ConfigMessage(AegisChestPlugin plugin) {
        this.plugin = plugin;
    }

    public Material getMarkerType() {
        String type = plugin.getConfig().getString("marker-type", "PLAYER_HEAD");
        if ("CHEST".equalsIgnoreCase(type)) {
            return Material.CHEST;
        }
        return Material.PLAYER_HEAD;
    }

    public Component getMessage(String key, String... placeholders) {
        String message = plugin.getConfig().getString("messages." + key);
        if (message == null) {
            return Component.text("Missing message: " + key);
        }

        // Apply placeholders (simple replacement, expected as key-value pairs)
        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
}
