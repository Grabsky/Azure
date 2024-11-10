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
