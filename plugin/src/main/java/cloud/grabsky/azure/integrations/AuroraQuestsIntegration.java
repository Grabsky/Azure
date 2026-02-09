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
package cloud.grabsky.azure.integrations;

import cloud.grabsky.azure.Azure;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import io.papermc.paper.event.block.VaultChangeStateEvent;
import org.bukkit.block.data.type.Vault;
import org.bukkit.event.EventPriority;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuroraQuestsIntegration {
    INSTANCE; // SINGLETON

    private static boolean IS_INITIALIZED = false;

    public static boolean initialize(final @NotNull Azure plugin) {
        if (IS_INITIALIZED == false) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("AuroraQuests") == true) {
                // Registering objectives to the AuroraQuests registry.
                ObjectiveFactory.registerObjective("PLAY_ONE_MINUTE", PlayOneMinuteObjective.class);
                ObjectiveFactory.registerObjective("UNLOCK_VAULT", UnlockVaultObjective.class);
                // Marking the integration as initialized.
                IS_INITIALIZED = true;
                // Returning true if integration was successfully initialized.
                return true;
            }
            // Logging warning and returning false if integration could not be initialized.
            plugin.getLogger().warning("AuroraQuests integration could not be initialized. (DEPENDENCY_NOT_ENABLED)");
            return false;
        }
        // Logging warning and returning false if integration could not be initialized.
        plugin.getLogger().warning("AuroraQuests integration could not be initialized. (ALREADY_INITIALIZED)");
        return false;
    }

    public static final class PlayOneMinuteObjective extends Objective {

        public PlayOneMinuteObjective(final Quest quest, final ObjectiveDefinition definition, final Profile.TaskDataWrapper data) {
            super(quest, definition, data);
        }

        @Override
        protected void activate() {
            // Scheduling an asynchronous repeating task which progresses for every minute spent on the server.
            asyncInterval(() -> progress(1, meta()), 1200, 1200);
        }

    }

    public static final class UnlockVaultObjective extends TypedObjective {

        public UnlockVaultObjective(final Quest quest, final ObjectiveDefinition definition, final Profile.TaskDataWrapper data) {
            super(quest, definition, data);
        }

        @Override
        protected void activate() {
            onEvent(VaultChangeStateEvent.class, this::onVaultStateChange, EventPriority.MONITOR);
        }

        public void onVaultStateChange(final VaultChangeStateEvent event) {
            if (event.getPlayer() != null && event.getNewState() == Vault.State.UNLOCKING) {
                progress(1, meta(event.getBlock().getLocation(), event.getBlock().getType()));
            }
        }

    }

}
