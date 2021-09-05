package me.grabsky.azure.manager;

import com.google.gson.Gson;
import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.indigo.logger.ConsoleLogger;
import me.grabsky.indigo.utils.Resources;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointManager {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final Gson gson;
    private final File pointsDirectory;
    private final Map<String, JsonLocation> points;
    private final List<String> modified;
    private List<String> ids;

    public PointManager(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.gson = instance.getGson();
        this.pointsDirectory = new File(instance.getDataFolder() + File.separator + "points");
        this.points = new HashMap<>();
        this.modified = new ArrayList<>();
        this.ids = new ArrayList<>();
    }

    public void loadAll() {
        // Create Azure/points directory if not existent
        if (!pointsDirectory.exists()) {
            pointsDirectory.mkdirs();
        }
        // List files if any
        if (pointsDirectory.list() != null) {
            final long s1 = System.nanoTime(); // PERF
            // Iterating over all files in Azure/points directory; Shouldn't be null
            for (final File file : pointsDirectory.listFiles()) {
                // Making sure to read only .json files
                if (file.getName().endsWith(".json")) {
                    // Getting file name without extension; equal to point id
                    final String id = file.getName().split("\\.")[0];
                    try {
                        // Creating BufferedReader to read the <point>.json file
                        final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
                        // Parsing file content to a JsonElement
                        final JsonLocation jsonLocation = gson.fromJson(reader, JsonLocation.class);
                        // Making sure file content was parsed successfully
                        if (jsonLocation != null) {
                            // Saving JsonLocation object to the HashMap
                            points.put(id, jsonLocation);
                        } else {
                            consoleLogger.error("Error occurred while trying to load point with id '" + id + "'.");
                        }
                        // Closing BufferedReader as we now have everything needed
                        reader.close();
                    } catch (IOException e) {
                        consoleLogger.error("Error occurred while trying to load point with id '" + id + "'.");
                        e.printStackTrace();
                    }
                }
            }
            // Sorting list as it's going to be used for command completions
            ids = new ArrayList<>(points.keySet().stream().sorted().toList());
            // Displaying console message
            consoleLogger.success("Loaded " + ids.size() + " points in " + (System.nanoTime() - s1) / 1000000D + "ms");
        }
    }

    // Adds new point (or overrides if existent); File will be created during next save
    public void addPoint(String id, Location location) {
        points.put(id, new JsonLocation(location.getWorld().getName(), (float) location.getX(), (float) location.getY(), (float) location.getZ(), location.getYaw(), location.getPitch()));
        if (!ids.contains(id)) ids.add(id);
        if (!modified.contains(id)) modified.add(id);
    }

    // Deletes point (and it's file) if existent
    public void deletePoint(String id) {
        points.remove(id);
        modified.remove(id);
        Resources.deleteFile(new File(pointsDirectory + File.separator + id + ".json"));
    }

    public boolean hasPoint(String id) {
        return points.containsKey(id);
    }

    // Returns Location of point with given id
    public Location getPoint(String id) {
        return points.get(id).toLocation();
    }

    // Returns sorted list of all point ids (for later use in command completions)
    public List<String> getIds() {
        return ids;
    }

    public void saveAll() {
        final long s1 = System.nanoTime();
        final int size = modified.size();
        if (size > 0) {
            // Creating Azure/points directory if not existent
            if (!pointsDirectory.exists()) {
                pointsDirectory.mkdirs();
            }
            // Iterating over list of modified points
            for (final String id : modified) {
                final File file = new File(pointsDirectory + File.separator + id + ".json");
                try {
                    // Creating <point>.json file if not existent
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // Creating BufferedWriter to save point location to the .json file
                    final BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
                    // Saving values into to .json file
                    final String json = gson.toJson(points.get(id), JsonLocation.class);
                    writer.write(json);
                    // Closing BufferedWriter as everything has been done
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    consoleLogger.error("Error occurred while trying to save point with id '" + id + "'.");
                    e.printStackTrace();
                }
            }
            // Sorting list as it's going to be used for command completions
            ids = new ArrayList<>(points.keySet().stream().sorted().toList());
            // Clearing list of modified points waiting for save
            modified.clear();
            // Displaying console message
            consoleLogger.success("Saved " + size + " points in " + (System.nanoTime() - s1) / 1000000D + "ms");
        }
    }

    public int runSaveTask() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(instance, this::saveAll, AzureConfig.POINTS_SAVE_INTERVAL, AzureConfig.POINTS_SAVE_INTERVAL).getTaskId();
    }
}
