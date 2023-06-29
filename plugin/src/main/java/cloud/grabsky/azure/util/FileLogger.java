package cloud.grabsky.azure.util;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public final class FileLogger {
    private final Plugin plugin;
    private final File file;

    private static final SimpleDateFormat DATE_FORMAT =  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public FileLogger(final @NotNull Plugin plugin, final @NotNull File file) {
        this.plugin = plugin;
        this.file = file;
    }

    public void log(final @NotNull String text) {
        try {
            // Creating log file if does not exist.
            if (file.exists() == false && (file.getParentFile().mkdirs() == false || file.createNewFile() == false)) {
                plugin.getLogger().warning("Logging to '" + file.getPath() + "' failed. Using main logger:");
                plugin.getLogger().info(text);
            }
            // Creating new instance of PrintWriter.
            final PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            // Printing line to the file.
            writer.println("[" + DATE_FORMAT.format(System.currentTimeMillis()) + "]: " + text);
            // Closing PrintWriter.
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}