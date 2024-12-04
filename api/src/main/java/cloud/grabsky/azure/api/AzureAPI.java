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
package cloud.grabsky.azure.api;

import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.api.world.WorldManager;

public interface AzureAPI {

    /**
     * Returns the {@link UserCache} instance.
     */
    UserCache getUserCache();

    /**
     * Returns the {@link WorldManager} instance.
     */
    WorldManager getWorldManager();

}
