package myrpgmaker.model.resource

import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.lib.Utils._
import myrpgmaker.lib.FileHelper._
import java.io._
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont

case class ScriptMetadata()

case class Script(proj: Project, name: String,
                  metadata: ScriptMetadata)
  extends Resource[Script, ScriptMetadata] {
  def meta = Script
}

object Script extends MetaResource[Script, ScriptMetadata] {
  def rcType = "script"
  def keyExts = Array("js")

  def defaultInstance(proj: Project, name: String) =
    Script(proj, name, ScriptMetadata())
}
