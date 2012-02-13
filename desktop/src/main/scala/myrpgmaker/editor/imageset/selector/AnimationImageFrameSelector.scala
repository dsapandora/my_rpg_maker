package myrpgmaker.editor.imageset.selector

import scala.swing._
import scala.swing.event._
import myrpgmaker.model.resource._
import java.awt.image.BufferedImage
import myrpgmaker.model._
import myrpgmaker.editor.uibase._
import myrpgmaker.editor.misc.TileUtils

/**
 * Chooses a frame of an AnimationImage
 */
class AnimationImageFrameSelector(
  animationImage: AnimationImage,
  initial: AnimationVisual,
  selectFunction: AnimationVisual => Any)
  extends BoxPanel(Orientation.Vertical) with DisposableComponent {

  import Spriteset._

  val xTiles = animationImage.xTiles

  val collageImage = TileUtils.getColumnCollageImg(animationImage)

  // Set up image selector contents
  contents += new ImageTileSelector(
    collageImage,
    tilesizeX = animationImage.tileW,
    tilesizeY = animationImage.tileH,
    xTilesVisible = 1,
    allowMultiselect = true,
    initialSelection =
      Some((0, initial.start.frameIndex), (0, initial.end.frameIndex)))
  {

    def selectTileF(button: Int, selectedTiles: Array[Array[(Int, Int)]]) = {
      val (x1, y1) = selectedTiles.head.head
      val (x2, y2) = selectedTiles.last.last
      assert(x1 == 0)
      assert(x2 == 0)

      val newStart = initial.start.copy(frameIndex = y1)
      val newEnd = initial.end.copy(frameIndex = y2)
      val newModel = initial.copy(start = newStart, end = newEnd)
      selectFunction(newModel)
    }
  }
}