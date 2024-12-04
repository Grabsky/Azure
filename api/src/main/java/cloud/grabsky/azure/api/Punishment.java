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

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a punishment that can be applied to an {@link User}
 */
public interface Punishment {

    /**
     * Returns the unique identifier of the punishment.
     */
    @NotNull String getReason();

    /**
     * Returns the issuer of the punishment.
     */
    @NotNull String getIssuer();

    /**
     * Returns the start date {@link Interval} of the punishment.
     */
    @NotNull Interval getStartDate();

    /**
     * Returns the end date {@link Interval} of the punishment.
     */
    @NotNull Interval getDuration();

    /**
     * Returns the end data {@link Interval} of the punishment.
     */
    default @NotNull Interval getEndDate() {
        return this.getStartDate().add(this.getDuration());
    }

    /**
     * Returns the remaining {@link Interval} of the punishment.
     */
    default @NotNull Interval getDurationLeft() {
        return this.getEndDate().remove(Interval.now());
    }

    /**
     * Returns {@code true} if the punishment is permanent, {@code false} otherwise.
     */
    default boolean isPermanent() {
        return this.getDuration().as(Unit.MILLISECONDS) == Long.MAX_VALUE;
    }

    /**
     * Returns {@code true} if the punishment is still active, {@code false} otherwise.
     */
    default boolean isActive() {
        return this.isPermanent() == true || this.getEndDate().as(Unit.MILLISECONDS) > System.currentTimeMillis();
    }

}
