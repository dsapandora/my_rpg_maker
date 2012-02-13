package myrpgmaker.editor.imageset.selector

import scala.swing._
import scala.swing.event._
import myrpgmaker.editor.misc.TileUtils
import myrpgmaker.model._
import myrpgmaker.model.resource._
import java.awt.image.BufferedImage
import myrpgmaker.editor.StateMaster

class AutotileSelector(
  sm: StateMaster,
  map: RpgMap,
  selectBytesF: Array[Array[Array[Byte]]] => Unit)
  extends BoxPanel(Orientation.Vertical) with TileBytesSelector {

  val autotiles =
    map.metadata.autotiles.map(sm.assetCache.getAutotile(_))
  val collageImage = TileUtils.getAutotileCollageImg(autotiles)

  val imgTileSelector: ImageTileSelector = new ImageTileSelector(collageImage) {
    def selectTileF(button: Int, selectedTiles: Array[Array[(Int, Int)]]) =
      selectBytesF(selectionBytes)
  }

  def selectionBytes = imgTileSelector.selection.map(_.map({
    case (xTile, yTile) =>
      Array(RpgMap.autotileByte, xTile.toByte, 0.toByte)
  }))

  // x coordiate corresponds to tileset number,
  // other two bytes we leave blank. 
  contents += imgTileSelector

  preferredSize = imgTileSelector.preferredSize

}
