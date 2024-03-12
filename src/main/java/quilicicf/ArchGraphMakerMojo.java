package quilicicf;

import quilicicf.domain.data.DocType;
import quilicicf.domain.data.Graph;
import quilicicf.domain.data.Node;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.file.Files.exists;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;
import static quilicicf.domain.functions.MermaidGenerator.toMermaid;
import static quilicicf.ports.FileSystem.replaceInDocFile;

@Mojo(name = "arch-graph-maker", defaultPhase = COMPILE, aggregator = true)
public class ArchGraphMakerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    File baseDir;

    @Parameter(property = "shouldDisplayGraphInLogs", defaultValue = "false", required = false)
    boolean shouldDisplayGraphInLogs;

    @Parameter(property = "shouldColorLinks", defaultValue = "true", required = false)
    boolean shouldColorLinks;

    @Parameter(property = "relativePathToDocFile", defaultValue = "README.md", required = true)
    String relativePathToDocFile;

    @Parameter(property = "docType", defaultValue = "MARKDOWN", required = false)
    DocType docType;

    @Parameter(property = "startTag", defaultValue = "START: ARCHITECTURE SCHEMA", required = false)
    String startTag;

    @Parameter(property = "endTag", defaultValue = "END: ARCHITECTURE SCHEMA", required = false)
    String endTag;

    @Component
    DefaultModelBuilder modelBuilder;

    @Override
    public void execute() throws MojoFailureException {
        if (relativePathToDocFile.contains("..")) {
            throw new MojoFailureException("Cannot update doc files outside of the folder where the pom is.");
        }

        final Path docFilePath = baseDir.toPath().resolve(relativePathToDocFile);
        if (!exists(docFilePath)) {
            throw new MojoFailureException("Cannot find doc file: " + docFilePath);
        }

        final Model model = project.getOriginalModel();
        final Graph graph = buildGraph(model);
        final String mermaid = toMermaid(graph, shouldColorLinks);

        if (shouldDisplayGraphInLogs) {
            getLog().info(mermaid);
        }

        try {
            replaceInDocFile(docType, startTag, endTag, mermaid, docFilePath);
        } catch (final Exception e) {
            throw new MojoFailureException("Could not write in doc file", e);
        }
    }

    private Graph buildGraph(final Model model) {
        return Graph.builder()
                .setId(model.getArtifactId())
                .setIsSubgraph(false)
                .setNodes(buildNodes(model.getModules()))
                .setSubGraphs(
                        model.getProfiles()
                                .stream()
                                .filter((profile) -> !profile.getModules().isEmpty())
                                .map(this::buildSubGraph)
                                .collect(toMap(
                                        Graph::getId,
                                        identity()
                                ))
                )
                .build();
    }

    private Graph buildSubGraph(final Profile profile) {
        return Graph.builder()
                .setId(profile.getId())
                .setIsSubgraph(true)
                .setNodes(buildNodes(profile.getModules()))
                .build();
    }

    private Map<String, Node> buildNodes(final List<String> modules) {
        return modules
                .stream()
                .map(this::buildNode)
                .collect(toMap(
                        Node::getId,
                        identity()
                ));
    }

    private Node buildNode(final String module) {
        try {
            final File modulePomFile = baseDir.toPath()
                    .resolve(module + "/pom.xml")
                    .toFile();
            final Model moduleModel = modelBuilder
                    .build(new DefaultModelBuildingRequest().setPomFile(modulePomFile))
                    .getEffectiveModel();
            return Node.builder()
                    .setId(moduleModel.getArtifactId())
                    .setDescription(coalesce(moduleModel.getDescription(), ""))
                    .setLinkedModules(
                            moduleModel
                                    .getDependencies()
                                    .stream()
                                    .filter((dependency) -> Objects.equals(dependency.getGroupId(), moduleModel.getGroupId()))
                                    .map(Dependency::getArtifactId)
                                    .collect(toSet())
                    )
                    .build();
        } catch (ModelBuildingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T coalesce(final T a, final T b) {
        return a != null ? a : b;
    }
}
