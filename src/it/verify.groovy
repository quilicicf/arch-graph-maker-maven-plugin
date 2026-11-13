// language=mermaid
final String EXPECTED_FLOW_CHART = """\
flowchart BT
  module-4("`**module-4**
  What module 4 does functionally`")
  module-3("`**module-3**
  What module 3 does functionally`")
  module-2("`**module-2**
  What module 2 does functionally`")
  module-1("`**module-1**
  What module 1 does functionally
  Can be a multi-line string`")
  bad-mock("`**bad-mock**
  A bad mock with dependencies only in this module, so it breaks maven resolution by default...`")
  mock("`**mock**
  A mock to run the service locally`")
  other-bad-mock("`**other-bad-mock**
  An other bad mock with dependencies only in this module, so it breaks maven resolution by default...`")
  subgraph "`**Profile dev**`"
    bad-mock
    mock
  end
  subgraph "`**Profile dev-completly-optional**`"
    other-bad-mock
  end
  module-4 --> module-3
  module-4 --> module-2
  module-4 --> module-1
  module-3 --> module-2
  module-3 --> module-1
  module-2 --> module-1
  bad-mock --> module-4
  bad-mock --> module-3
  bad-mock --> module-2
  bad-mock --> module-1
  mock --> module-4
  mock --> module-3
  mock --> module-2
  mock --> module-1
  other-bad-mock --> module-4
  other-bad-mock --> module-3
  other-bad-mock --> module-2
  other-bad-mock --> module-1
  linkStyle 0,7,14 stroke: #f27f96, stroke-width: 3px
  linkStyle 1,8,15 stroke: #dc89ba, stroke-width: 3px
  linkStyle 2,9,16 stroke: #ad8ab4, stroke-width: 3px
  linkStyle 3,10,17 stroke: #6e86b7, stroke-width: 3px
  linkStyle 4,11 stroke: #4dbaec, stroke-width: 3px
  linkStyle 5,12 stroke: #53c1be, stroke-width: 3px
  linkStyle 6,13 stroke: #98cd75, stroke-width: 3px
"""

def findTextInFile(final String filePath, final String textToSearchFor) {
    final File buildLog = new File((String) basedir, filePath)
    assert buildLog.exists()
    assert buildLog.text.replaceAll("\r\n", "\n").contains(textToSearchFor.replaceAll("\r\n", "\n"))
}

findTextInFile('build.log', "[INFO] ${EXPECTED_FLOW_CHART}")
findTextInFile('README.md', EXPECTED_FLOW_CHART)
