final String EXPECTED_FLOW_CHART = "flowchart BT\n" +
  "  module-4(\"`**module-4**\n" +
  "  What module 4 does functionally`\")\n" +
  "  module-3(\"`**module-3**\n" +
  "  What module 3 does functionally`\")\n" +
  "  module-2(\"`**module-2**\n" +
  "  What module 2 does functionally`\")\n" +
  "  module-1(\"`**module-1**\n" +
  "  What module 1 does functionally\n" +
  "  Can be a multi-line string`\")\n" +
  "  mock(\"`**mock**\n" +
  "  A mock to run the service locally`\")\n" +
  "  subgraph \"`**Profile dev**`\"\n" +
  "    mock\n" +
  "  end\n" +
  "  module-4 --> module-3\n" +
  "  module-4 --> module-2\n" +
  "  module-4 --> module-1\n" +
  "  module-3 --> module-2\n" +
  "  module-3 --> module-1\n" +
  "  module-2 --> module-1\n" +
  "  mock --> module-4\n" +
  "  mock --> module-3\n" +
  "  mock --> module-2\n" +
  "  mock --> module-1\n" +
  "  linkStyle 0,7 stroke: #f27f96, stroke-width: 3px\n" +
  "  linkStyle 1,8 stroke: #dc89ba, stroke-width: 3px\n" +
  "  linkStyle 2,9 stroke: #ad8ab4, stroke-width: 3px\n" +
  "  linkStyle 3 stroke: #6e86b7, stroke-width: 3px\n" +
  "  linkStyle 4 stroke: #4dbaec, stroke-width: 3px\n" +
  "  linkStyle 5 stroke: #53c1be, stroke-width: 3px\n" +
  "  linkStyle 6 stroke: #98cd75, stroke-width: 3px"

def findTextInFile (final String filePath, final String textToSearchFor) {
  final File buildLog = new File((String) basedir, filePath)
  assert buildLog.exists()
  assert buildLog.text.contains(textToSearchFor)
}

findTextInFile('build.log', "[INFO] ${EXPECTED_FLOW_CHART}")
findTextInFile('README.md', EXPECTED_FLOW_CHART)
