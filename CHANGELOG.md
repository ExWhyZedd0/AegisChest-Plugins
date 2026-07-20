# AegisChest Changelog

## Version 1.0.1

### 🐛 Bug Fixes
* **Hologram Ghosting:** Fixed a critical bug where AegisChest holograms (name and timer) would become permanently stuck floating in the world if the server restarted before the chest expired. Holograms are now properly flagged as non-persistent and will always clean themselves up.

### 🌟 New Features
* **Recovery Vault & Admin Restoration:** When a chest expires, its items no longer drop naturally on the ground where they might despawn. Instead, they are securely moved into a virtual "Recovery Vault".
* **`/ac recover <player>` Command:** Added a new admin command (`aegischest.admin` permission required). This command allows admins to instantly restore an expired chest by dropping its entire contents and XP perfectly at the admin's feet.

### ⚙️ Tweaks
* **Default Timer Updated:** The default chest expiration timer in `config.yml` has been increased from 10 minutes to **1 hour (3600 seconds)** to give players more time to retrieve their items safely.
