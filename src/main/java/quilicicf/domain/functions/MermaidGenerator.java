package quilicicf.domain.functions;

import quilicicf.domain.data.Graph;
import quilicicf.domain.data.Node;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;

public class MermaidGenerator {
    private MermaidGenerator() {}

    private static final List<String> LINK_COLORS = List.of(
            "#f27f96",
            "#dc89ba",
            "#ad8ab4",
            "#6e86b7",
            "#4dbaec",
            "#53c1be",
            "#98cd75"
    );

    public static String toMermaid(final Graph graph, final boolean shouldColorLinks) {
        final List<Node> allNodes = gatherAllNodes(graph);
        final List<String> allLinks = allNodes.stream()
                .flatMap(MermaidGenerator::generateLinks)
                .toList();
        final Stream<String> colorization = shouldColorLinks
                ? gatherColorization(allLinks.size())
                : Stream.of();
        return Stream.of(
                        Stream.of("flowchart BT"),
                        allNodes.stream().map(MermaidGenerator::generateNode),
                        graph.getSubGraphs()
                                .values()
                                .stream()
                                .flatMap(MermaidGenerator::generateSubGraph),
                        allLinks.stream(),
                        colorization
                )
                .flatMap(identity())
                .collect(joining("\n"));
    }

    private static List<Node> gatherAllNodes(final Graph graph) {
        return Stream.of(
                graph.getNodes().values().stream(),
                graph.getSubGraphs()
                        .values()
                        .stream()
                        .flatMap((subGraph) -> subGraph.getNodes().values().stream())
        ).flatMap(identity()).toList();
    }

    private static String generateNode(final Node node) {
        return format(
                "  %s(\"`**%s**\n%s`\")",
                node.getId().trim(),
                node.getId().trim(),
                processDescription(node.getDescription())
        );
    }

    private static String processDescription(final String description) {
        final String[] lines = description
                .trim()
                .split("\\n");
        return stream(lines)
                .map(String::trim)
                .map((line) -> format("  %s", line))
                .collect(joining("\n"));
    }

    private static Stream<String> generateSubGraph(final Graph subGraph) {
        return Stream.of(
                Stream.of(format("  subgraph \"`**Profile %s**`\"", subGraph.getId())),
                subGraph.getNodes()
                        .values()
                        .stream()
                        .map((node) -> format("    %s", node.getId())),
                Stream.of("  end")
        ).flatMap(identity());
    }

    private static Stream<String> generateLinks(final Node node) {
        return node.getLinkedModules()
                .stream()
                .map((linkedModule) -> format("  %s --> %s", node.getId().trim(), linkedModule));
    }

    private static Stream<String> gatherColorization(final int linksNumber) {
        final int colorsNumber = min(linksNumber, LINK_COLORS.size());
        return IntStream
                .range(0, colorsNumber)
                .boxed()
                .map((colorIndex) -> generateLinkStyle(colorIndex, colorsNumber, linksNumber));
    }

    private static String generateLinkStyle(final int colorIndex, final int colorsNumber, final int linksNumber) {
        final String linksToColor = IntStream
                .rangeClosed(0, linksNumber / colorsNumber)
                .boxed()
                .map((n) -> colorIndex + n * colorsNumber)
                .filter((n) -> n < linksNumber)
                .map(Object::toString)
                .collect(joining(","));
        return format("  linkStyle %s stroke: %s, stroke-width: 3px", linksToColor, LINK_COLORS.get(colorIndex));
    }
}
