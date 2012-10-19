package myrpgmaker.model.resource

import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.lib.FileHelper._

import org.json4s.native.Serialization

import scala.collection.JavaConversions._

import javax.imageio._

import java.io._
import java.awt.image._

/**
 * @param blockedDirsAry  Array of blocked directions. Array should be accessed
 *                        as blockedDirsAry[yTile][xTile]. We do this so that
 *                        the array layout is the same as the image layout --
 *                        and it so it matches the layer arrays in RpgMapData.
 *
 */
case class TilesetMetadata(
  var blockedDirsAry: Array[Array[Byte]],
  var heightAry: Array[Array[Byte]])

case class Tileset(proj: Project,
                   name: String,
                   metadata: TilesetMetadata)
  extends TiledImageResource[Tileset, TilesetMetadata] {
  import Tileset.tilesize
  def meta = Tileset

  def tileH = tilesize
  def tileW = tilesize
}

object Tileset extends MetaResource[Tileset, TilesetMetadata] {
  def rcType = "tileset"
  def keyExts = Array("png")

  def tilesize = 32
  def halftile = tilesize / 2

  def defaultInstance(proj: Project, name: String) = {
    val tilesetWOMetadata = Tileset(proj, name,
      new TilesetMetadata(Array.empty, Array.empty))

    import DirectionMasks._

    // Generate blockedDirs array
    val x = tilesetWOMetadata.xTiles
    val y = tilesetWOMetadata.yTiles
    val blockedDirsAry = Array.fill(y, x)(NONE.toByte)
    val heightAry = Array.fill(y, x)(0.toByte)

    tilesetWOMetadata.copy(metadata =
      TilesetMetadata(blockedDirsAry, heightAry))
  }
}

