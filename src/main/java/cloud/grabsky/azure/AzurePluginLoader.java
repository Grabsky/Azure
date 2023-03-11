package cloud.grabsky.azure;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class AzurePluginLoader implements PluginLoader {

    private static final RemoteRepository[] REPOSITORIES = {
            repository("https://repo.papermc.io/repository/maven-public/"),
            repository("https://repo.grabsky.cloud/snapshots/")
    };

    private static final Dependency[] DEPENDENCIES = {
            dependency("cloud.grabsky", "bedrock", "9ae7165"),
            dependency("cloud.grabsky", "commands", "c370583"),
            dependency("cloud.grabsky", "configuration-paper", "7abe6c1"),
    };

    @Override
    public void classloader(final @NotNull PluginClasspathBuilder builder) {
        final MavenLibraryResolver maven = new MavenLibraryResolver();
        // ...
        for (final RemoteRepository repository : REPOSITORIES)
            maven.addRepository(repository);
        // ...
        for (final Dependency dependency : DEPENDENCIES)
            maven.addDependency(dependency);
        // ...
        // builder.addLibrary(maven);
    }

    private static RemoteRepository repository(final @NotNull String url) {
        return new RemoteRepository.Builder(null, "default", url).build();
    }

    private static Dependency dependency(final @NotNull String groupId, final @NotNull String artifactId, final @NotNull String version) {
        return new Dependency(new DefaultArtifact(groupId + ":" + artifactId + ":" + version), null);
    }

}
