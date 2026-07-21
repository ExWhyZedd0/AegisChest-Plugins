# AegisChest Changelog

## Version 1.0.2

### ✨ New Features
* **Exact Inventory Slot Memory:** When you die, AegisChest now perfectly memorizes the exact slot location of every item in your inventory (hotbar, inventory, off-hand, and armor slots). When you retrieve your chest, the plugin precisely injects every item right back into its original slot! 
* **Safe Fallback System:** If you happen to pick up new items on your way back and they block your old slots, the plugin safely falls back to standard inventory insertion so no items are ever lost.
* **Auto-Equip Priority:** The new slot-memory system perfectly synergizes with our auto-equip logic. Armor, Elytras, Shields, and Totems still auto-equip before eating up inventory space!
* **Expanded Auto-Equip:** When a player collects their chest, the plugin will now automatically equip Elytras (in the chestplate slot) and Shields/Totems of Undying (in the off-hand slot), in addition to standard armor.
* **Collection Sound Effects:** Added satisfying "Level Up" and "Chest Open" sound effects that play when a player successfully opens and collects their AegisChest.

### 🐛 Bug Fixes
* **Cross-Version API Compatibility:** Fixed a `NoSuchMethodError` crash that occurred on PaperMC/Purpur 1.20+ servers when spawning holograms. The plugin now uses a version-agnostic spawning method, making it completely stable across 1.19, 1.20, and 1.21.
* **Cross-Dimension Fetching:** Fixed a bug where fetching an AegisChest across dimensions (e.g., from the Nether to the Overworld) would cause its holograms to permanently disappear due to unloaded chunks.
* **Floating Head Drops:** Disabled block physics when spawning an AegisChest via `/ac fetch`, preventing the `PLAYER_HEAD` from instantly popping off and dropping as an item if fetched into mid-air.

---

## Version 1.0.1

### 🐛 Bug Fixes
* **Death Registration Fallback (Hotfix):** Fixed a critical error where if Hologram generation was blocked by another plugin (like WorldGuard) or an API exception occurred during death, the player's items would be permanently swallowed. Items will now drop safely on the ground naturally if a chest fails to spawn.
* **Hologram Ghosting:** Fixed a critical bug where AegisChest holograms (name and timer) would become permanently stuck floating in the world if the server restarted before the chest expired. Holograms are now properly flagged as non-persistent and will always clean themselves up.

### ✨ New Features
* **Recovery Vault & Admin Restoration:** When a chest expires, its items no longer drop naturally on the ground where they might despawn. Instead, they are securely moved into a virtual "Recovery Vault".
* **`/ac recover <player>` Command:** Added a new admin command (`aegischest.admin` permission required). This command allows admins to instantly restore an expired chest by dropping its entire contents and XP perfectly at the admin's feet.

### ⚙️ Tweaks
* **Default Timer Updated:** The default chest expiration timer in `config.yml` has been increased from 10 minutes to **1 hour (3600 seconds)** to give players more time to retrieve their items safely.
