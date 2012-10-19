package myrpgmaker.model.resource

import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.lib.FileHelper._
import org.json4s.native.Serialization
import scala.collection.JavaConversions._
import javax.imageio._
import java.io._
import java.awt.image._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas

case class SpritesetMetadata(boundsX: Int = Tileset.tilesize,
  boundsY: Int = Tileset.tilesize)

/**
 * *
 * This Spriteset class deals with image meeting the following criteria:
 *
 * It must either be a single sprite with a name beginning with a "$",
 * or it must contain 4 sprites in the x axis and 2 in the y axis
 *
 * Each spriteset follows the rpgmaker xp format.
 */
case class Spriteset(proj: Project,
  name: String,
  metadata: SpritesetMetadata)
  extends TiledImageResource[Spriteset, SpritesetMetadata] {
  def meta = Spriteset

  import Spriteset._
  /**
   * Gets the size of the sprites as well as the numerosity
   */
  val (tileH, tileW, xSprites, ySprites) = {
    val file = new File(name)
    val filename = file.getName()
    val oneSprite = filename.size > 2 &&
      (filename(0) == '$' || (filename(0) == '!' && filename(1) == '$'))

    if (oneSprite) {
      (img.getHeight() / spriteYTiles, img.getWidth() / spriteXTiles, 1, 1)
    } else {
      val tileH = img.getHeight() / (spriteYTiles * nSpritesInSetY)
      val tileW = img.getWidth() / (spriteXTiles * nSpritesInSetX)
      (tileH, tileW, nSpritesInSetX, nSpritesInSetY)
    }
  }

  val nSprites = xSprites * ySprites

  /**
   * Gets the offset for a given sprite in tiles.
   *
   * @param index   Given between 0-7. The sprite number in the page.
   * @param dir     One of Spriteset.DirectionOffsets. 0-3
   * @param step    One of Spriteset.Steps. 0-3
   */
  def srcTile(index: Int, dir: Int, step: Int) = {
    // In units of sprites. tileW, tileH
    val xOffset = (index % 4) * (3)
    val yOffset = (index / 4) * (4)

    // Account for the fact that
    val normalizedStep = if (step == 3) 1 else step

    val xTile = (xOffset + normalizedStep)
    val yTile = (yOffset + dir)

    (xTile, yTile)
  }

  def srcTile(spec: SpriteSpec): (Int, Int) =
    srcTile(spec.spriteIndex, spec.dir, spec.step)

  def srcTileImg(spec: SpriteSpec) = {
    val (xTile, yTile) = srcTile(spec)
    getTileImage(xTile, yTile)
  }

  def srcTexels(index: Int, dir: Int, step: Int): (Int, Int) = {
    val (xTile, yTile) = srcTile(index, dir, step)
    srcTexels(xTile, yTile)
  }

  /**
   * x and y are defined to the top-left of the sprite image.
   */
  def renderSprite(
    batch: SpriteBatch, atlasSprites: TextureAtlas,
    spriteIndex: Int, dir: Int, step: Int,
    dstX: Float, dstY: Float, dstW: Float, dstH: Float) = {
    val region = atlasSprites.findRegion(name)
    val texture = region.getTexture()
    val (xTile, yTile) = srcTile(spriteIndex, dir, step)
    val texelXOffset = region.getRegionX()
    val texelYOffset = region.getRegionY()

    drawTileAt(batch, texture, dstX, dstY, dstW, dstH, xTile, yTile,
      texelXOffset, texelYOffset)
  }
}

object Spriteset extends MetaResource[Spriteset, SpritesetMetadata] {
  def rcType = "spriteset"
  def keyExts = Array("png")

  def defaultInstance(proj: Project, name: String) =
    Spriteset(proj, name, SpritesetMetadata())

  // How many tiles are in X and Y
  def spriteXTiles = 3
  def spriteYTiles = 4
  def nSpritesInSetX = 4
  def nSpritesInSetY = 2
}

