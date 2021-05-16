package net.skydistrict.azure.manager;

import net.skydistrict.azure.commands.*;

public class CommandManager {

    public void registerCommands() {
        new AzureCommand().register();
        new TeleportCommandBundle().register();
        new MessageCommandBundle().register();
        new ChatCommandBundle().register();
        new SkullCommand().register();
        new NameCommand().register();
        new LoreCommand().register();
        new EnchantCommand().register();
    }
}
