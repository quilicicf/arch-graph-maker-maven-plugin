package quilicicf.domain.data;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder(setterPrefix = "set", toBuilder = true)
public class Node {
	final String id; // Also used as title
	final String description;
	final Set<String> linkedModules;
}
