package myrpgmaker.model.resource

import myrpgmaker._

class ResourceSpec extends UnitSpec {
  "Resource" should "resolve metadata paths correctly" in {
    Autotile.metadataPathRelative("sys/testname.png") should equal (
        "sys/testname.png.metadata.json")
  }
}