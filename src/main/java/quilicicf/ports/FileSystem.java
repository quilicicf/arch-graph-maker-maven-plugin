package quilicicf.ports;

import quilicicf.domain.data.DocType;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class FileSystem {

	private FileSystem() {
	}

	public static void replaceInDocFile(final DocType docType, final String startTag, final String endTag,
																			final String graph, final Path filePath) {

		try {
			final byte[] bytes = readAllBytes(filePath);
			final String oldDocumentation = new String(bytes, UTF_8);

			final String fullStartTag = docType.commentTag(startTag);
			final String fullEndTag = docType.commentTag(endTag);

			if (!oldDocumentation.contains(fullStartTag)) {
				throw new RuntimeException(format("Cannot find start tag %s in doc file %s", fullStartTag, filePath));
			}

			if (!oldDocumentation.contains(fullEndTag)) {
				throw new RuntimeException(format("Cannot find end tag %s in doc file %s", fullEndTag, filePath));
			}

			final int mermaidStartIndex = oldDocumentation.indexOf(fullStartTag) + fullStartTag.length();
			final int mermaidEndIndex = oldDocumentation.indexOf(fullEndTag);

			final String blockBeforeStartTag = oldDocumentation.substring(0, mermaidStartIndex);
			final String blockAfterEndTag = oldDocumentation.substring(mermaidEndIndex);

			final String newDocumentation = blockBeforeStartTag +
				"\n" +
				docType.wrapGraph(graph) +
				"\n" +
				blockAfterEndTag;

			writeString(filePath, newDocumentation, TRUNCATE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException("Cannot read doc file", e);
		}
	}
}
