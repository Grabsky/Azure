package net.skydistrict.azure.storage.cache;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

// TO-DO: Needs to be reworked or completely removed because as of now it makes no sense. We need to know if player changed his name.
public class UUIDCache {
    private static final HashMap<UUID, String> uniqueIdName = new HashMap<UUID, String>();
    private static final HashMap<String, UUID> nameUniqueId = new HashMap<String, UUID>();

    // Puts (name <-> uuid) of every player on the server and saves it two hash maps for easy access
    public static void cacheAllOffline() {
        for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            UUID uuid = offlinePlayer.getUniqueId();
            String name = offlinePlayer.getName();
            uniqueIdName.put(uuid, name);
            nameUniqueId.put(name, uuid);
        }
    }

    // Updates player values in UUID cache (in case they changed name or joined server for the first time)
    public static void updateCacheIfSomethingChanged(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        if(!contains(uuid)) {
            uniqueIdName.put(uuid, name);
            nameUniqueId.put(name, uuid);
        } else {
            if(uuid != getUniqueId(name) || !name.equals(getName(uuid))) {
                uniqueIdName.put(uuid, name);

            }
        }

        uniqueIdName.put(uuid, name);
        nameUniqueId.put(name, uuid);
    }



    // Returns true if player is already cached
    public static boolean contains(UUID uuid) {
        return uniqueIdName.containsKey(uuid);
    }

    // Returns name of player with given uuid
    public static String getName(UUID uuid) {
        return uniqueIdName.get(uuid);
    }

    // Returns uuid of player with given name
    public static UUID getUniqueId(String name) {
        return nameUniqueId.get(name);
    }

}
