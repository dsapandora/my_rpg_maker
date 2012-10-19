package myrpgmaker.model.resource

import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.lib.FileHelper._

import org.json4s.native.Serialization

import scala.collection.JavaConversions._

import javax.imageio._

import java.io._
import java.awt.image._

case class IconsetMetadata(tileW: Int = 24,
                           tileH: Int = 24)

case class Iconset(proj: Project,
                   name: String,
                   metadata: IconsetMetadata)
  extends TiledImageResource[Iconset, IconsetMetadata] {
  def meta = Iconset

  def tileW = metadata.tileW
  def tileH = metadata.tileH
}

object Iconset extends MetaResource[Iconset, IconsetMetadata] {
  def rcType = "iconset"
  def keyExts = Array("png")

  def defaultInstance(proj: Project, name: String) =
    Iconset(proj, name, IconsetMetadata())
}

