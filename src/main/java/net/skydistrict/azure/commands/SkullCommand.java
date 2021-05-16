package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Material;

import java.util.UUID;

public class SkullCommand {

    public void register() {
        this.onSkullFromName().register();
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
