package quilicicf;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;
import org.eclipse.aether.RepositorySystemSession;
import quilicicf.domain.data.DocType;
import quilicicf.domain.data.Graph;
import quilicicf.domain.data.Node;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import static java.nio.file.Files.exists;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;
import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;
import static quilicicf.domain.functions.MermaidGenerator.toMermaid;
import static quilicicf.ports.FileSystem.replaceInDocFile;

@Mojo(name = "arch-graph-maker", defaultPhase = COMPILE, aggregator = true)
public class ArchGraphMakerMojo extends AbstractMojo {

	private final ProjectBuilder projectBuilder;

	/**
	 * The current repository/network configuration of Maven.
	 */
	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession session;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

	/**
	 * The projects in the current build. The effective-POM for
	 * each of these projects will written.
	 */
	@Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
	private List<MavenProject> projects;

	@Parameter(defaultValue = "${project.modules}", required = true, readonly = true)
	List<String> modules;

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

	@Inject
	public ArchGraphMakerMojo(final ProjectBuilder projectBuilder) {
		super();
		this.projectBuilder = projectBuilder;
	}

	@Override
	public void execute() throws MojoFailureException {
		if (relativePathToDocFile.contains("..")) {
			throw new MojoFailureException("Cannot update doc files outside of the folder where the pom is.");
		}

		final Path docFilePath = baseDir.toPath().resolve(relativePathToDocFile);
		if (!exists(docFilePath)) {
			throw new MojoFailureException("Cannot find doc file: " + docFilePath);
		}

		final Map<String, List<MavenProject>> profileChildren = findProfileChildren();
		final Graph graph = buildGraph(project, projects, profileChildren);
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

	private Map<String, List<MavenProject>> findProfileChildren() {
		return project.getModel()
			.getProfiles()
			.stream()
			.collect(toMap(
				Profile::getId,
				this::findProfileChildren
			));
	}

	private List<MavenProject> findProfileChildren(final Profile profile) {
		return profile
			.getModules()
			.stream()
			.map(this::resolveProject)
			.collect(toList());
	}

	/**
	 * Used to resolve modules that are not in {@link ArchGraphMakerMojo#projects}
	 * because they are only activated by profiles.
	 */
	private MavenProject resolveProject(final String module) {
		final Path projectPomPath = project.getModel()
			.getPomFile()
			.toPath()
			.getParent()
			.resolve(module)
			.resolve("pom.xml");
		try {
			final DefaultProjectBuildingRequest request = new DefaultProjectBuildingRequest().setRepositorySession(session);
			return projectBuilder
				.build(projectPomPath.toFile(), request)
				.getProject();
		} catch (final ProjectBuildingException e) {
			throw new RuntimeException("Impossible to resolve module %s from parent pom".formatted(module), e);
		}
	}

	private Graph buildGraph(final MavenProject parent,
													 final List<MavenProject> standardProjects,
													 final Map<String, List<MavenProject>> profileChildren) {
		final Model model = parent.getModel();
		return Graph.builder()
			.setId(model.getArtifactId())
			.setIsSubgraph(false)
			.setNodes(buildNodes(
				standardProjects
					.stream()
					.filter(not(parent::equals))
					.toList()
			))
			.setSubGraphs(
				profileChildren
					.entrySet()
					.stream()
					.map(this::buildSubGraph)
					.collect(toMap(
						Graph::getId,
						identity()
					))
			)
			.build();
	}

	private Graph buildSubGraph(final Entry<String, List<MavenProject>> entry) {
		return Graph.builder()
			.setId(entry.getKey())
			.setIsSubgraph(true)
			.setNodes(buildNodes(entry.getValue()))
			.build();
	}

	private Map<String, Node> buildNodes(final List<MavenProject> children) {
		return children
			.stream()
			.map(this::buildNode)
			.collect(toMap(
				Node::getId,
				identity()
			));
	}

	private Node buildNode(final MavenProject project) {
		final Model model = project.getModel();
		return Node.builder()
			.setId(model.getArtifactId())
			.setDescription(coalesce(model.getDescription(), ""))
			.setLinkedModules(
				model
					.getDependencies()
					.stream()
					.filter((dependency) -> Objects.equals(dependency.getGroupId(), model.getGroupId()))
					.map(Dependency::getArtifactId)
					.collect(toSet())
			)
			.build();
	}

//	private Tree buildTree(final MavenProject project, final Map<Path, MavenProject> projectsByPomPath) {
//		final Tree tree = new Tree(createParentProject(project));
//		final Path pomFolderPath = project.getModel()
//			.getPomFile()
//			.toPath()
//			.getParent();
//		tree.addChildren(
//			project.getModules()
//				.stream()
//				.map(module -> new ModuleAndPomPath(module, pomFolderPath))
//				.filter(moduleAndPomPath -> projectsByPomPath.containsKey(moduleAndPomPath.getPomPath()))
//				.map(moduleAndPomPath -> createChildProject(
//					projectsByPomPath.get(moduleAndPomPath.getPomPath()),
//					moduleAndPomPath
//				))
//				.collect(toMap(
//					ProjectWithMetadata::getModule,
//					child -> recursiveBuildTree(child, projectsByPomPath)
//				))
//		);
//
//		return tree;
//	}
//
//	private Tree recursiveBuildTree(final ProjectWithMetadata project,
//																	final Map<Path, MavenProject> projectsByPomPath) {
//		final Tree tree = new Tree(project);
//		final Path pomFolderPath = project
//			.getPomPath()
//			.getParent();
//		tree.addChildren(
//			project
//				.getProject()
//				.getModules()
//				.stream()
//				.map(module -> new ModuleAndPomPath(module, pomFolderPath))
//				.filter(moduleAndPomPath -> projectsByPomPath.containsKey(moduleAndPomPath.getPomPath()))
//				.map(moduleAndPomPath -> createChildProject(
//					projectsByPomPath.get(moduleAndPomPath.getPomPath()),
//					moduleAndPomPath
//				))
//				.collect(toMap(
//					ProjectWithMetadata::getModule,
//					child -> recursiveBuildTree(child, projectsByPomPath)
//				))
//		);
//		return tree;
//	}
//
//	private Path addPomXmlFileName(final Path path) {
//		return path.resolve("pom.xml");
//	}

	public static <T> T coalesce(final T a, final T b) {
		return a != null ? a : b;
	}
}
