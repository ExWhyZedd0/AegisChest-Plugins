# AegisChest 🛡️

AegisChest is a lightweight, reliable, and user-friendly PaperMC plugin that saves your players' items and experience points upon death. Instead of scattering your items and XP across the floor and risking despawn, they are safely secured in an **AegisChest**. 

## ✨ Features

- **Item & XP Protection**: All items and experience points are safely stored inside a marker (defaults to the dying player's head) at the exact death location.
- **Holograms**: Spawns a two-layer hologram above the chest displaying the owner's name and a live, blue countdown timer.
- **Timed Expiry**: AegisChests automatically expire after a configurable duration (default: 1 hour). If they expire, they are securely stored in an Admin Recovery Vault instead of despawning on the ground.
- **Secure**: Other players cannot open, break, or interact with an AegisChest that doesn't belong to them (bypassed by server operators).
- **Auto-Equip**: When an owner collects their AegisChest, armor is automatically equipped if the slots are empty, and XP is instantly restored.
- **Fetch Command**: Players can safely fetch and teleport their AegisChests to themselves using a command.
- **Multiple Chests**: Supports multiple active chests per player.
- **Highly Configurable**: Customize messages, chest duration, and the marker type (e.g., `PLAYER_HEAD` or `CHEST`) directly from the configuration file.
- **Admin Recovery**: Recover expired chests with ease using `/ac recover <player>` so items are never truly lost to time.

## 📦 Installation

1. Download the latest `AegisChest-1.0.1.jar` release.
2. Drop the `.jar` file into your server's `plugins/` directory.
3. Restart your server.
4. Customize the generated `config.yml` inside the `plugins/AegisChest/` folder to your liking.

## ⚙️ Configuration (`config.yml`)

The plugin generates a straightforward configuration file:

```yaml
# aegischest-duration: The time in seconds before an AegisChest expires. Default is 3600 (1 hour).
aegischest-duration: 3600

# marker-type: The block type to use as the chest marker. 
# Defaults to PLAYER_HEAD (drops dying player's head). Can also be CHEST.
marker-type: PLAYER_HEAD

# Customizable messages (using color codes '&')
messages:
  chest-collected: "&aAegisChest successfully collected!"
  not-owner: "&cYou cannot interact with another player's AegisChest!"
  ...
```

## 💻 Commands

- `/aegischest list` - Displays a list of all your active AegisChests, including their IDs, coordinates, and remaining time.
- `/aegischest fetch` - Teleports your active AegisChest to you (if you only have one).
- `/aegischest fetch <id>` - Teleports a specific AegisChest to you by its ID.
- `/aegischest recover <player>` - Allows admins to instantly restore an expired chest directly at their feet.

*Alias:* `/ac` can be used instead of `/aegischest`.

## 📜 Permissions

*Currently, AegisChest works out-of-the-box for all players. Server operators (OPs) have the bypass ability to open or break any AegisChest. Admins require `aegischest.admin` to use the `/ac recover` command.*

## 🛠️ Building from Source

To build this project from source, you need JDK 17+ and Gradle.

```bash
git clone https://github.com/YourUsername/AegisChest.git
cd AegisChest
./gradlew build
```
The compiled JAR will be located in `build/libs/`.

## 📄 License
This project is open-source and available for customization.
