package cloud.grabsky.azure.api;

import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.commands.RootCommandManager;

import java.util.function.Consumer;

public interface AzureAPI  {

    UserCache getUserCache();

    Consumer<RootCommandManager> getCommandManagerTemplate();

}
