package myrpgmaker.editor.resourceselector

import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import scala.swing.Panel
import scala.swing.Window
import myrpgmaker.editor.StateMaster
import myrpgmaker.editor.imageset.selector.SpriteSelector
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.editor.uibase.DisposableComponent
import myrpgmaker.editor.uibase.FloatSpinner
import myrpgmaker.editor.uibase.SwingUtils.leftLabel
import myrpgmaker.model.BattlerSpec
import myrpgmaker.model.FaceSpec
import myrpgmaker.model.Project
import myrpgmaker.model.SpriteSpec
import myrpgmaker.model.resource.Battler
import myrpgmaker.model.resource.Faceset
import myrpgmaker.model.resource.Spriteset
import myrpgmaker.editor.imageset.selector.ImageTileSelector

abstract class FaceSelectDialog(
  owner: Window,
  sm: StateMaster,
  initial: Option[FaceSpec])
  extends ResourceSelectDialog(owner, sm, initial, allowNone = true, Faceset) {

  def specToResourceName(spec: FaceSpec): String = spec.faceset
  def newRcNameToSpec(name: String, prevSpec: Option[FaceSpec]) = {
    prevSpec
      .map(_.copy(faceset = name))
      .getOrElse(FaceSpec(name, 0, 0))
  }

  def rightPaneDim = new Dimension(384, 384)

  override def rightPaneFor(faceSelection: FaceSpec,
                            updateSelectionF: FaceSpec => Unit) = {
    assume(!faceSelection.faceset.isEmpty)

    val faceset = Faceset.readFromDisk(sm.getProj, faceSelection.faceset)

    new ImageTileSelector(faceset.img, faceset.tileW, faceset.tileH,
      xTilesVisible = 4, allowMultiselect = false,
      drawSelectionSq = true,
      initialSelection = Some(((faceSelection.faceX, faceSelection.faceY),
                               (faceSelection.faceX, faceSelection.faceY)))) {
      def selectTileF(button: Int, selectedTiles: Array[Array[(Int, Int)]]) = {
        updateSelectionF(faceSelection.copy(
            faceX = selectedTiles.head.head._1,
            faceY = selectedTiles.head.head._2))
      }
    }
  }
}

abstract class SpriteSelectDialog(
  owner: Window,
  sm: StateMaster,
  initial: Option[SpriteSpec])
  extends ResourceSelectDialog(owner, sm, initial, true, Spriteset) {
  import Spriteset._

  def specToResourceName(spec: SpriteSpec): String = spec.name
  def newRcNameToSpec(name: String, prevSpec: Option[SpriteSpec]) = {
    val (dir, step) = prevSpec.map { spec =>
      (spec.dir, spec.step)
    } getOrElse ((SpriteSpec.Directions.SOUTH, SpriteSpec.Steps.STILL))

    val idx = prevSpec.map { spec =>
      val newSpriteset = sm.assetCache.getSpriteset(name)
      val prevIdx = spec.spriteIndex

      // ensure that the prevIdx is applicable to the new spriteset
      if (prevIdx < newSpriteset.nSprites) prevIdx else 0
    } getOrElse (0)

    SpriteSpec(name, idx, dir, step)
  }

  def rightPaneDim = new Dimension(384, 384)

  override def rightPaneFor(
    selection: SpriteSpec,
    updateSelectionF: SpriteSpec => Unit) = {
    val spriteset = sm.assetCache.getSpriteset(selection.name)

    new SpriteSelector(spriteset, selection, updateSelectionF)
  }
}

abstract class BattlerSelectDialog(
  owner: Window,
  sm: StateMaster,
  initial: Option[BattlerSpec])
  extends ResourceSelectDialog(owner, sm, initial, true, Battler) {

  def specToResourceName(spec: BattlerSpec): String = spec.name
  def newRcNameToSpec(name: String, prevSpec: Option[BattlerSpec]) =
    prevSpec.map(_.copy(name = name)).getOrElse(BattlerSpec(name))

  override def rightPaneFor(
    selection: BattlerSpec,
    updateSelectionF: BattlerSpec => Unit) = {
    new BattlerSelector(sm.getProj, selection, updateSelectionF)
  }
}

class BattlerSelector(
  proj: Project,
  initial: BattlerSpec,
  selectFunction: BattlerSpec => Any)
  extends DesignGridPanel with DisposableComponent {

  def updateModel(spec: BattlerSpec) = {
    fPreview.setImage(BattlerField.getImage(spec, proj))
    selectFunction(spec)
  }

  val fPreview = new Panel {
    private var _img: BufferedImage = null

    def setImage(img: BufferedImage) = {
      _img = img
      repaint()
    }

    preferredSize = new Dimension(240, 240)

    override def paintComponent(g: Graphics2D) = {
      super.paintComponent(g)
      if (_img != null) g.drawImage(_img, 0, 0, null)
    }
  }

  val fScale = new FloatSpinner(
    0.1f,
    10.0f,
    0.1f,
    initial.scale,
    v => updateModel(initial.copy(scale = v)))

  updateModel(initial)

  row().grid(leftLabel("Preview:")).add(fPreview)
  row().grid(leftLabel("Scale:")).add(fScale)
}