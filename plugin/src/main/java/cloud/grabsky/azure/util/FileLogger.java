/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.util;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

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