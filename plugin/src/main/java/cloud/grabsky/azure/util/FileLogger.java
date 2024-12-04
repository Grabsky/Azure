/*
 * Azure (https://github.com/Grabsky/Azure)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.azure.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class FileLogger {

    private final @NotNull Plugin plugin;
    private final @NotNull File file;

    private static final SimpleDateFormat DATE_FORMAT =  new SimpleDateFormat("dd MMM yy, HH:mm");

    /**
     * Logs message to the file associated with this {@link FileLogger} instance.
     */
    public void log(final @NotNull String text) {
        try {
            // Creating log file if does not exist.
            if (file.exists() == false) {
                // Trying to create parent directories.
                file.getParentFile().mkdirs();
                // Trying to create a new file... In case operation was unsuccessful, regular Bukkit logger is used.
                if (file.createNewFile() == false) {
                    plugin.getLogger().warning("Logging to '" + file.getPath() + "' failed. Using default Plugin#getLogger now:");
                    plugin.getLogger().info(text);
                    return;
                }
            }
            // Creating new instance of PrintWriter.
            final PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            // Printing line to the file.
            writer.println(DATE_FORMAT.format(System.currentTimeMillis()) + " | " + text);
            // Closing PrintWriter.
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}