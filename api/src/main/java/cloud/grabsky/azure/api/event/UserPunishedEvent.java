package cloud.grabsky.azure.api.event;

import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class UserPunishedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter(AccessLevel.PUBLIC)
    private @NotNull User user;

    @Getter(AccessLevel.PUBLIC)
    private @NotNull Punishment punishment;

    @Getter(AccessLevel.PUBLIC)
    private @NotNull PunishmentType type;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public enum PunishmentType {
        KICK, MUTE, BAN
    }

}