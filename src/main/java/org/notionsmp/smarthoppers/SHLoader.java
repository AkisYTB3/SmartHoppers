package org.notionsmp.smarthoppers;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.lang.reflect.Field;
import java.util.List;

public class SHLoader implements PluginLoader {
  private static final List<String> DEPENDS =
      List.of(
          "org.jetbrains.kotlin:kotlin-stdlib:2.1.20-Beta2",
          "org.jetbrains.kotlin:kotlin-reflect:2.1.20-Beta2"
      );

  @Override
  public void classloader(PluginClasspathBuilder classpathBuilder) {

    MavenLibraryResolver resolver = new MavenLibraryResolver();

    try {
      Field mirrorField = MavenLibraryResolver.class.getField("MAVEN_CENTRAL_DEFAULT_MIRROR");
      Object mirrorRepo = mirrorField.get(null);
      if (mirrorRepo instanceof RemoteRepository) {
        resolver.addRepository((RemoteRepository) mirrorRepo);
      }
      SmartHoppers.getInstance().getLogger().info("MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR found, using it to comply with ToS of the Maven Central");
    } catch (NoSuchFieldException | IllegalAccessException e) {
      resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
    }

    for (String dep : DEPENDS) {
      resolver.addDependency(new Dependency(new DefaultArtifact(dep), null));
    }

    classpathBuilder.addLibrary(resolver);
  }
}
