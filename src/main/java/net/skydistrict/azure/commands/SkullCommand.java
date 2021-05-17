package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkullCommand {

    public void register() {
        this.onSkullFromName().register();
        this.onSkullFromPlayer().register();
        this.onSkullFromUniqueId().register();
    }

    public CommandAPICommand onSkullFromName() {
        return new CommandAPICommand("skull")
                .withPermission("skydistrict.command.skull")
                .withArguments(new StringArgument("name"))
                .executesPlayer((sender, args) -> {
                    final String name = String.valueOf(args[0]);
                    if (UserCache.contains(name)) {
                        sender.getInventory().addItem(new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(UserCache.get(name).getTexture()).build());
                        Lang.send(sender, Lang.SKULL_RECEIVED
                                .replace("{player}", name)
                        );
                    }
                });
    }

    public CommandAPICommand onSkullFromPlayer() {
        return new CommandAPICommand("skull")
                .withPermission("skydistrict.command.skull")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .executesPlayer((sender, args) -> {
                    final String name = ((Player) args[0]).getName();
                    sender.getInventory().addItem(new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(UserCache.get(name).getTexture()).build());
                    Lang.send(sender, Lang.SKULL_RECEIVED
                            .replace("{player}", name)
                    );
                });
    }

    public CommandAPICommand onSkullFromUniqueId() {
        return new CommandAPICommand("skull")
                .withPermission("skydistrict.command.skull")
                .withArguments(new UUIDArgument("uuid"))
                .executesPlayer((sender, args) -> {
                    final UUID uuid = (UUID) args[0];
                    if (UserCache.contains(uuid)) {
                        sender.getInventory().addItem(new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(UserCache.get(uuid).getTexture()).build());
                        Lang.send(sender, Lang.SKULL_RECEIVED
                                .replace("{player}", uuid.toString())
                        );
                    }
                });
    }
}
