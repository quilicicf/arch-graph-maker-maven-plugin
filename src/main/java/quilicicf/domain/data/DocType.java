package quilicicf.domain.data;

import static java.lang.String.format;

public enum DocType {
	MARKDOWN() {
		@Override
		public String commentTag(final String tag) {
			return format("<!-- %s -->", tag);
		}

		@Override
		public String wrapGraph(final String graph) {
			return format("\n```mermaid\n%s\n```\n", graph);
		}
	},
	ASCIIDOC {
		@Override
		public String commentTag(final String tag) {
			return format("// %s", tag);
		}

		@Override
		public String wrapGraph(final String graph) {
			return format("\n[mermaid]\n....\n%s\n....\n", graph);
		}
	},
	ASCIIDOC_GITHUB {
		@Override
		public String commentTag(final String tag) {
			return format("// %s", tag);
		}

		@Override
		public String wrapGraph(final String graph) {
			return format("\n[source,mermaid]\n....\n%s\n....\n", graph); // Yeah, GitHub renders source blocks instead of custom blocks...
		}
	},
	;

	public abstract String commentTag(final String tag);

	public abstract String wrapGraph(final String graph);
}
