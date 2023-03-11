package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.AzureLocale;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.RootCommandInput;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static java.lang.String.valueOf;

public final class WorldCommand extends RootCommand {

    public WorldCommand() {
        super("world", null, "azure.command.world", "/world [args]", "Manage your worlds in-game.");
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        final RootCommandInput input = context.getInput();
        // ...
        if (index == 0)
            return CompletionsProvider.of("create", "delete", "gamerule", "info", "teleport");
        // ...
        return switch (input.at(1).toLowerCase()) {
            case "create" -> switch (index) {
                case 2 -> CompletionsProvider.of(World.Environment.class);
                case 3 -> CompletionsProvider.of(WorldType.class);
                default -> CompletionsProvider.EMPTY;
            };
            case "delete" -> (index == 1)
                    ? CompletionsProvider.of(World.class)
                    : CompletionsProvider.of("--confirm");
            case "gamerule" -> switch (index) {
                case 1 -> CompletionsProvider.of(World.class);
                case 2 -> CompletionsProvider.of(GameRule.class);
                case 3 -> {
                    final GameRule<?> gameRule = GameRule.getByName(context.getInput().at(3));
                    yield (gameRule != null)
                            ? CompletionsProvider.of(gameRule.getType())
                            : CompletionsProvider.EMPTY;
                }
                default -> CompletionsProvider.EMPTY;
            };
            case "info" -> (index == 1)
                    ? CompletionsProvider.of(World.class)
                    : CompletionsProvider.EMPTY;
            case "teleport" -> switch (index) {
                case 1 -> CompletionsProvider.of(Player.class);
                case 2 -> CompletionsProvider.of(World.class);
                default -> CompletionsProvider.EMPTY;
            };
            // ...
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final RootCommandContext context, final ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        System.out.println(arguments.getCurrentArgumentIndex());
        // ...
        switch (arguments.nextString()) {
            default -> sendMessage(sender, AzureLocale.COMMAND_WORLD_HELP);
            // syntax: /world create <name> <environment> <type>
            case "create" -> {
                System.out.println(arguments.getCurrentArgumentIndex());
                final NamespacedKey key = arguments.next(NamespacedKey.class).asRequired();
                System.out.println(arguments.getCurrentArgumentIndex());
                final World.Environment environment = arguments.next(World.Environment.class).asRequired();
                System.out.println(arguments.getCurrentArgumentIndex());
                final WorldType type = arguments.next(WorldType.class).asRequired();
                // ...
                if (Bukkit.getWorld(key) == null) {
                    final World world = Bukkit.createWorld(
                            new WorldCreator(key)
                                    .environment(environment)
                                    .type(type)
                    );
                    sendMessage(sender, AzureLocale.COMMAND_WORLD_CREATE,
                            Placeholder.unparsed("world", world.key().asString())
                    );
                    return;
                }
                sender.sendMessage("world already exist!");
            }
            // syntax: /world teleport <target> <world>
            case "teleport" -> {
                System.out.println(arguments.getCurrentArgumentIndex());
                final Player target = arguments.next(Player.class).asRequired();
                final World world = arguments.next(World.class).asRequired();
                // ...
                target.teleport(world.getSpawnLocation());
                sendMessage(sender, AzureLocale.COMMAND_WORLD_TELEPORT,
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("world", world.key().asString())
                );
            }
            // syntax: /world delete <world> [--confirm] [--backup]
            case "delete" -> {
                System.out.println(arguments.getCurrentArgumentIndex());
                final World world = arguments.next(World.class).asRequired();
                final String[] flags = arguments.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");
                // ...
                if (containsIgnoreCase(flags, "--confirm") == true) {
                    // teleporting players away from the world
                    for (final Player player : world.getPlayers()) {
                        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    }
                    // unloading the world (no save action is performed)
                    Bukkit.unloadWorld(world, false);
                    // deleting world directory
                    if (deleteDirectory(world.getWorldFolder()) == true) {
                        sendMessage(sender, AzureLocale.COMMAND_WORLD_DELETE,
                                Placeholder.unparsed("world", world.key().asString())
                        );
                        return;
                    }
                    sender.sendMessage("failed to delete");
                    return;
                }
                sendMessage(sender, AzureLocale.COMMAND_WORLD_DELETE_CONFIRM,
                        Placeholder.unparsed("input", context.getInput().toString()),
                        Placeholder.unparsed("world", world.key().asString())
                );
            }
            case "info" -> {
                final World world = arguments.next(World.class).asOptional(context.getExecutor().asPlayer().getWorld());
                // ...
                final Location spawnLoc = world.getSpawnLocation();
                // ...
                sendMessage(sender, AzureLocale.COMMAND_WORLD_INFO,
                        Placeholder.unparsed("world_name", world.getName()),
                        Placeholder.unparsed("world_key", world.getKey().asString()),
                        Placeholder.unparsed("world_environment", world.getEnvironment().name()),
                        Placeholder.unparsed("world_online", valueOf(world.getPlayerCount())),
                        Placeholder.unparsed("spawn_x", valueOf(spawnLoc.x())),
                        Placeholder.unparsed("spawn_y", valueOf(spawnLoc.y())),
                        Placeholder.unparsed("spawn_z", valueOf(spawnLoc.z()))
                );
            }
            case "boolean" -> {
                final boolean bool = arguments.next(Boolean.class).asRequired();
            }
        }
    }

    private static boolean containsIgnoreCase(final String[] arr, final String search) {
        for (final String element : arr) {
            if (search.equalsIgnoreCase(element) == true)
                return true;
        }
        return false;
    }

    private static boolean deleteDirectory(final File file) {
        try (final Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

}
