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
package cloud.grabsky.azure.user;

import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.bedrock.util.Interval;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class AzurePunishment implements Punishment {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String reason;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String issuer;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Interval startDate;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Interval duration;

}
