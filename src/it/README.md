<!-- Formatted by https://github.com/quilicicf/markdown-formatter -->

# My project

<!-- TOC START -->

* [Architecture](#architecture)

<!-- TOC END -->

## Architecture

<!-- START: ARCHITECTURE SCHEMA -->
```mermaid
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
  mock("`**mock**
  A mock to run the service locally`")
  subgraph "`**Profile dev**`"
    mock
  end
  module-4 --> module-3
  module-4 --> module-2
  module-4 --> module-1
  module-3 --> module-2
  module-3 --> module-1
  module-2 --> module-1
  mock --> module-4
  mock --> module-3
  mock --> module-2
  mock --> module-1
  linkStyle 0,7 stroke: #f27f96, stroke-width: 3px
  linkStyle 1,8 stroke: #dc89ba, stroke-width: 3px
  linkStyle 2,9 stroke: #ad8ab4, stroke-width: 3px
  linkStyle 3 stroke: #6e86b7, stroke-width: 3px
  linkStyle 4 stroke: #4dbaec, stroke-width: 3px
  linkStyle 5 stroke: #53c1be, stroke-width: 3px
  linkStyle 6 stroke: #98cd75, stroke-width: 3px
```
<!-- END: ARCHITECTURE SCHEMA --> 
