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
            return format("```mermaid\n%s\n```", graph);
        }
    },
    ASCIIDOC {
        @Override
        public String commentTag(final String tag) {
            return format("// %s", tag);
        }

        @Override
        public String wrapGraph(final String graph) {
            return format("[mermaid]\n....\n%s\n....", graph);
        }
    },
    ASCIIDOC_GITHUB {
        @Override
        public String commentTag(final String tag) {
            return format("// %s", tag);
        }

        @Override
        public String wrapGraph(final String graph) {
            return format("[source,mermaid]\n....\n%s\n....", graph); // Yeah, GitHub renders source blocks instead of custom blocks...
        }
    },
    ;

    public abstract String commentTag(final String tag);
    public abstract String wrapGraph(final String graph);
}
