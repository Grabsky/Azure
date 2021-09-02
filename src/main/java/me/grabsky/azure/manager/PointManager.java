package me.grabsky.azure.manager;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.indigo.jackson.databind.ObjectMapper;
import me.grabsky.indigo.logger.ConsoleLogger;
import me.grabsky.indigo.utils.Resources;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointManager {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File pointsDirectory;
    private final ObjectMapper mapper;
    private final Map<String, JsonLocation> points;
    private final List<String> modified;
    private List<String> ids;

    public PointManager(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.pointsDirectory = new File(instance.getDataFolder() + File.separator + "points");
        this.mapper = new ObjectMapper();
        this.points = new HashMap<>();
        this.modified = new ArrayList<>();
        this.ids = new ArrayList<>();
    }

    public void loadAll() {
        // Create Azure/points directory if not existent
        if (!pointsDirectory.exists()) {
            pointsDirectory.getParentFile().mkdirs();
        }
        // List files if any
        if (pointsDirectory.list() != null) {
            final long s1 = System.nanoTime();
            for (final File file : pointsDirectory.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    final String name = file.getName().split("\\.")[0];
                    try {
                        final JsonLocation jsonLocation = mapper.readValue(file, JsonLocation.class);
                        if (jsonLocation != null) {
                            points.put(name, jsonLocation);
                        }
                    } catch (IOException e) {
                        consoleLogger.error("Error occurred while trying to load point with id '" + name + "'.");
                        e.printStackTrace();
                    }
                }
            }
            ids = new ArrayList<>(points.keySet().stream().sorted().toList());
            consoleLogger.success("Loaded " + ids.size() + " points in " + (System.nanoTime() - s1) / 1000000D + "ms");
        }
    }

    public void addPoint(String id, Location location) {
        points.put(id, new JsonLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
        if (!ids.contains(id)) ids.add(id);
        if (!modified.contains(id)) modified.add(id);
    }

    public void deletePoint(String id) {
        if (ids.contains(id)) {
            points.remove(id);
            Resources.deleteFile(new File(pointsDirectory + File.separator + id + ".json"));
        }
    }

    public boolean hasPoint(String id) {
        return points.containsKey(id);
    }

    public Location getPoint(String id) {
        return points.get(id).toLocation();
    }

    public List<String> getIds() {
        return ids;
    }

    public void saveAll() {
        final long s1 = System.nanoTime();
        final int size = modified.size();
        if (size > 0) {
            for (final String id : modified) {
                final File file = new File(pointsDirectory + File.separator + id + ".json");
                try {
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    mapper.writeValue(file, points.get(id));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ids = new ArrayList<>(points.keySet().stream().sorted().toList());
            modified.clear();
            consoleLogger.success("Saved " + size + " points in " + (System.nanoTime() - s1) / 1000000D + "ms");
        }
    }

    public void runSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, this::saveAll, AzureConfig.POINTS_SAVE_INTERVAL, AzureConfig.POINTS_SAVE_INTERVAL);
    }
}
