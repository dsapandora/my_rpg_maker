package myrpgmaker.editor.dialog.db

import myrpgmaker.editor._
import myrpgmaker.editor.uibase._
import myrpgmaker.editor.dialog.db.components._
import myrpgmaker.editor.uibase.SwingUtils._
import scala.swing._
import scala.swing.event._
import myrpgmaker.editor.dialog._
import myrpgmaker.model._
import myrpgmaker.model.Constants._
import net.java.dev.designgridlayout._
import myrpgmaker.editor.resourceselector._
import scala.collection.mutable.ArrayBuffer
import myrpgmaker.lib.Utils
import myrpgmaker.editor.Internationalized._

class AnimationsPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends RightPaneArrayDatabasePanel(
    owner,
    dbDiag.model.enums.animations) {
  def panelName = getMessage("Animations")
  def newDefaultInstance() = new Animation()

  def editPaneForItem(idx: Int, model: Animation) = {
    logger.debug(getMessage("New_Edit_Pane_For_Animation") + "Id=%d".format(idx))
    val fName = textField(model.name, v => {
      model.name = v
      refreshModel()
    })

    val fVisuals = new TableEditor[AnimationVisual]() {
      val buffer = ArrayBuffer(model.visuals: _*)
      def title = getMessage("Visuals")
      def modelArray = buffer
      def newInstance() = AnimationVisual()
      def colHeaders = Array(getMessage("Start"), getMessage("End"), getMessage("Animation_File"))
      def getRowStrings(visual: AnimationVisual) = {
        Array(visual.start.time.toString, visual.end.time.toString,
          visual.animationImage)
      }
      def onUpdate() =
        model.visuals = modelArray.toArray
      def showEditDialog(initial: AnimationVisual,
        okCallback: AnimationVisual => Unit) = {
        val d = new AnimationVisualDialog(owner, sm, initial, okCallback)
        d.open()
      }
    }

    val fSounds = new TableEditor[AnimationSound]() {
      val buffer = ArrayBuffer(model.sounds: _*)
      def title = getMessage("Sounds")
      def modelArray = buffer
      def newInstance() = AnimationSound()
      def colHeaders = Array(getMessage("Start"), getMessage("Sound"))
      def getRowStrings(item: AnimationSound) = {
        Array(item.time.toString, item.sound.sound)
      }
      def onUpdate() =
        model.sounds = modelArray.toArray
      def showEditDialog(initial: AnimationSound,
        okCallback: AnimationSound => Unit) = {
        val d = new AnimationSoundDialog(owner, sm, initial, okCallback)
        d.open()
      }
    }

    val fFlashes = new TableEditor[AnimationFlash]() {
      val buffer = ArrayBuffer(model.flashes: _*)
      def title = getMessage("Flashes")
      def modelArray = buffer
      def newInstance() = AnimationFlash()
      def colHeaders =
        Array(getMessage("Start"), getMessage("End"), getMessage("Color"))
      def getRowStrings(item: AnimationFlash) = {
        Array(item.startTime.toString, item.endTime.toString,
            item.color.toString)
      }
      def onUpdate() =
        model.flashes = modelArray.toArray
      def showEditDialog(initial: AnimationFlash,
        okCallback: AnimationFlash => Unit) = {
        val d = new AnimationFlashDialog(owner, initial, okCallback)
        d.open()
      }
    }

    val animationPlayerPanel = new AnimationPlayerPanel(sm.getProj, model)

    new BoxPanel(Orientation.Vertical) with DisposableComponent {
      contents += new DesignGridPanel {
        row().grid(lbl(getMessageColon("Name"))).add(fName)
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += animationPlayerPanel
        contents += new BoxPanel(Orientation.Vertical) {
          contents += fVisuals
          contents += fSounds
          contents += fFlashes
        }
      }

      override def dispose() = {
        animationPlayerPanel.dispose()
        super.dispose()
      }
    }
  }

  override def onListDataUpdate() = {
    dbDiag.model.enums.animations = dataAsArray
  }
}

class AnimationVisualDialog(
  owner: Window,
  sm: StateMaster,
  initial: AnimationVisual,
  onOk: (AnimationVisual) => Unit)
  extends StdDialog(owner, getMessage("Animation_Visual")) {
  import SwingUtils._

  val model = Utils.deepCopy(initial)

  // These are declared first because fAnimationImage refers to these
  val fStartFrame = new AnimationKeyframePanel(model.start)
  val fEndFrame = new AnimationKeyframePanel(model.end)

  val fAnimationImage = new AnimationImageField(
    owner,
    sm,
    if (initial.animationImage.isEmpty) None else Some(initial),
    newSelectionOpt => {
      newSelectionOpt.map { newSelection =>
        fStartFrame.fFrameIndex.setValue(newSelection.start.frameIndex)
        fEndFrame.fFrameIndex.setValue(newSelection.end.frameIndex)

        model.animationImage = newSelection.animationImage
      }
    })

  contents = new DesignGridPanel {
    row().grid(leftLabel(getMessageColon("Image"))).add(fAnimationImage)

    row().grid(leftLabel(getMessageColon("Start"))).add(fStartFrame)
    row().grid(leftLabel(getMessageColon("End"))).add(fEndFrame)

    addButtons(okBtn, cancelBtn)
  }

  def okFunc() = {
    val start = model.start.time
    val end = model.end.time
    model.start.time = math.min(start, end)
    model.end.time = math.max(start, end)

    onOk(model)
    close()
  }
}

/**
 * Modifies |model| in-place.
 */
class AnimationKeyframePanel(model: AnimationKeyframe) extends DesignGridPanel {
  val fStartTime =
    new FloatSpinner(0f, 30f, 0.1f, model.time, model.time = _)
  val fFrameIndex =
    new NumberSpinner(0, 100, model.frameIndex, model.frameIndex = _)
  val fX = new NumberSpinner(-999, 999, model.x, model.x = _)
  val fY = new NumberSpinner(-999, 999, model.y, model.y = _)

  row().grid(leftLabel(getMessageColon("Time"))).add(fStartTime)
  row().grid(leftLabel(getMessageColon("Frame_Index"))).add(fFrameIndex, 2)
  row().grid(leftLabel(getMessageColon("X"))).add(fX).grid(leftLabel("y:")).add(fY)
}

class AnimationSoundDialog(
  owner: Window,
  sm: StateMaster,
  initial: AnimationSound,
  onOk: (AnimationSound) => Unit)
  extends StdDialog(owner, getMessage("Animation_Sound")) {
  import SwingUtils._

  val model = Utils.deepCopy(initial)
  val fTime = new FloatSpinner(0f, 30f, 0.1f, model.time, model.time = _)
  val fSound = {
    val initialModel =
      if (model.sound.sound.isEmpty()) None else Some(model.sound)
    new SoundField(
      owner, sm, initialModel, _.map(model.sound = _), allowNone = false)
  }

  contents = new DesignGridPanel {
    row().grid(leftLabel(getMessageColon("Start_Time"))).add(fTime)
    row().grid(leftLabel(getMessageColon("Sound"))).add(fSound)

    addButtons(okBtn, cancelBtn)
  }

  def okFunc() = {
    onOk(model)
    close()
  }
}

class AnimationFlashDialog(
  owner: Window,
  initial: AnimationFlash,
  onOk: (AnimationFlash) => Unit)
  extends StdDialog(owner, getMessage("Animation_Flash")) {
  import SwingUtils._

  val model = Utils.deepCopy(initial)

  val fStartTime =
    new FloatSpinner(0f, 30f, 0.1f, model.startTime, model.startTime = _)
  val fEndTime =
    new FloatSpinner(0f, 30f, 0.1f, model.endTime, model.endTime = _)

  val fColor = colorField(model.color, model.color = _)
  val fFlashType =
    enumIdCombo(AnimationFlashType)(model.flashTypeId, model.flashTypeId = _)

  contents = new DesignGridPanel {
    row().grid(leftLabel(getMessageColon("Color"))).add(fColor)
    row().grid(leftLabel(getMessageColon("Flash_Type"))).add(fFlashType)
    row().grid(leftLabel(getMessageColon("Start_Time"))).add(fStartTime)
    row().grid(leftLabel(getMessageColon("End_Time"))).add(fEndTime)

    addButtons(okBtn, cancelBtn)
  }

  def okFunc() = {
    val start = model.startTime
    val end = model.endTime
    model.startTime = math.min(start, end)
    model.endTime = math.max(start, end)
    onOk(model)
    close()
  }


}