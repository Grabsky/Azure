package net.skydistrict.azure.storage;

import com.destroystokyo.paper.ClientOption;
import com.zaxxer.hikari.HikariDataSource;
import net.skydistrict.azure.Azure;
import net.skydistrict.azure.storage.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

// TO-DO: Implement data saving and save task.
// TO-DO: Json parsing for Locations.
public class DataManager {
    private final Azure instance;
    private final HikariDataSource hikari;

    public DataManager(Azure instance) {
        this.instance = instance;
        this.hikari = instance.getSQLManager().getHikari();
    }

    // Create data
    private PlayerData createPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String customName = player.getCustomName();
        String ip = player.getAddress().getAddress().toString();
        String country = "PL"; // TO-DO: Implement country fetching
        String language = player.getClientOption(ClientOption.LOCALE);
        try {
            Connection connection = hikari.getConnection();
            PreparedStatement first = connection.prepareStatement("INSERT INTO azure_playerdata VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            first.setString(1, uuid.toString());
            first.setString(2, name);
            first.setString(3, customName);
            first.setString(4, ip);
            first.setString(5, country);
            first.setString(6, language);
            first.setInt(7, (int) System.currentTimeMillis());
            first.setFloat(8, (float) player.getLocation().getX());
            first.setFloat(9, (float) player.getLocation().getY());
            first.setFloat(10, (float) player.getLocation().getZ());
            first.setString(11, player.getLocation().getWorld().getName());
            first.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerData(uuid, name, customName, ip, country, language, System.currentTimeMillis(), player.getLocation());
    }

    // Sends query to get player's data from database
    @Nullable
    public PlayerData query(UUID uuid) {
        try {
            Connection connection = hikari.getConnection();
            PreparedStatement first = connection.prepareStatement("SELECT * FROM azure_playerdata WHERE uuid=?");
            first.setString(1, uuid.toString());
            PreparedStatement second = connection.prepareStatement("SELECT * FROM azure_homes WHERE uuid=?");
            second.setString(1, uuid.toString());
            ResultSet dataResult = first.executeQuery();
            ResultSet homesResult = second.executeQuery();
            if(dataResult.first()) {
                PlayerData playerData = new PlayerData(
                        uuid,
                        dataResult.getString("name"),
                        dataResult.getString("customName"),
                        dataResult.getString("lastIpAddress"),
                        dataResult.getString("lastCountry"),
                        dataResult.getString("lastLanguage"),
                        dataResult.getInt("lastSeen"),
                        new Location(
                                Bukkit.getWorld(dataResult.getString("lastWorld")),
                                dataResult.getFloat("lastX"),
                                dataResult.getFloat("lastY"),
                                dataResult.getFloat("lastZ")
                        )
                );
                // Homes
            }
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                return createPlayerData(player);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PlayerData save(PlayerData data) {
        try {
            Connection connection = hikari.getConnection();
            PreparedStatement playerData = connection.prepareStatement("INSERT INTO azure_playerdata("
                    + "name, "
                    + "customName, "
                    + "lastIpAddress, "
                    + "lastCountry, "
                    + "lastLanguage, "
                    + "lastSeen, "
                    + "lastX, "
                    + "lastY, "
                    + "lastZ, "
                    + "lastWorld) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "WHERE uuid=?"
            );
            playerData.setString(1, data.getName());
            playerData.setString(2, data.getName());
            playerData.setString(3, data.getIpAddress());
            playerData.setString(4, data.getCountry());
            playerData.setString(5, data.getLanguage());
            playerData.setInt(6, (int) data.getLastSeen());
            playerData.setFloat(7, (float) data.getLastLocation().getX());
            playerData.setFloat(8, (float) data.getLastLocation().getY());
            playerData.setFloat(9, (float) data.getLastLocation().getZ());
            playerData.setString(10, data.getLastLocation().getWorld().getName());
            playerData.setString(11, data.getUUID().toString()); // UUID of player
            for(Map.Entry<String, Location> home : data.getHomes().entrySet()) {


            }
            PreparedStatement homes = connection.prepareStatement("INSERT INTO azure_homes("
                    + "uuid, "
                    + "homeName, "
                    + "homeX, "
                    + "homeY, "
                    + "homeZ, "
                    + "homeYaw, "
                    + "homePitch, "
                    + "homeWorld, "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "WHERE uuid=? "
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Sends query to get data of ONLINE players (for reloads)
    private void queryOnlineAll() {
        try {
            Connection connection = hikari.getConnection();
            PreparedStatement first = connection.prepareStatement("SELECT * FROM azure_playerdata WHERE uuid IN (?)");
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            String[] uuids = new String[players.size()];
            int i = 0;
            for(Player player : Bukkit.getOnlinePlayers()) {
                uuids[i] = player.getUniqueId().toString();
                i++;
            }
            first.setArray(1, connection.createArrayOf("varchar", uuids));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
