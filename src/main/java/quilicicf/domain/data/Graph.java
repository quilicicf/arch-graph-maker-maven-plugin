package quilicicf.domain.data;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder(setterPrefix = "set", toBuilder = true)
public class Graph {
	private final String id;
	private final boolean isSubgraph;
	private final Map<String, Graph> subGraphs;
	private final Map<String, Node> nodes;
}
