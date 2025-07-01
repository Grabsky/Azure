package cloud.grabsky.azure.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;

@Deprecated(forRemoval = true)
public final class ResourcePackLoadEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter(AccessLevel.PUBLIC)
    private boolean isInitial;

    public ResourcePackLoadEvent(final @NotNull Player player, final boolean isInitial) {
        super(player);
        // Setting other values...
        this.isInitial = isInitial;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
