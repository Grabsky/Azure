package cloud.grabsky.azure.api;

import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.api.world.WorldManager;

public interface AzureAPI  {

    UserCache getUserCache();

    WorldManager getWorldManager();

}
