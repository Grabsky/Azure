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
package cloud.grabsky.azure.api.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface WorldManager {

    NamespacedKey OVERWORLD = NamespacedKey.minecraft("overworld");

    /**
     * Returns default / primary {@link World} specified in {@code server.properties} configuration file.
     */
    default @UnknownNullability World getPrimaryWorld() {
        return Bukkit.getWorld(OVERWORLD); // This should never be null.
    }

    /**
     * Returns spawn {@link Location} of specified world.
     */
    default @NotNull Location getSpawnPoint(final World world) {
        return world.getSpawnLocation();
    }

}
