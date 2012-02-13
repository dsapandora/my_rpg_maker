package myrpgmaker.editor.dialog.db

import myrpgmaker.editor._
import myrpgmaker.editor.Internationalized._
import myrpgmaker.editor.uibase._
import myrpgmaker.editor.dialog.db.components._
import myrpgmaker.editor.uibase.SwingUtils._
import scala.swing._
import scala.swing.event._
import myrpgmaker.editor.dialog._
import myrpgmaker.model._
import myrpgmaker.model.Constants._
import myrpgmaker.model.resource._
import net.java.dev.designgridlayout._
import myrpgmaker.editor.resourceselector.SpriteField
import myrpgmaker.editor.resourceselector.FaceField

class CharactersPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends RightPaneArrayDatabasePanel(
    owner,
    dbDiag.model.enums.characters) {
  def panelName = getMessage("Characters")
  def newDefaultInstance() = new Character()

  def editPaneForItem(idx: Int, model: Character) = {
    val bioFields = new DesignGridPanel {
      val fName = textField(
        model.name,
        v => {
          model.name = v
          refreshModel()
        })
      val fSubtitle = textField(model.subtitle, model.subtitle = _)
      val fDescription = textField(model.description, model.description = _)

      val fSprite = new SpriteField(owner, sm, model.sprite, model.sprite = _)
      val fFace = new FaceField(owner, sm, model.face, model.face = _)

      val fClass = indexedCombo(
        dbDiag.model.enums.classes,
        model.charClass,
        model.charClass = _)

      val fInitLevel = new NumberSpinner(
        MINLEVEL,
        MAXLEVEL,
        model.initLevel,
        model.initLevel = _)

      val fMaxLevel = new NumberSpinner(
        MINLEVEL,
        MAXLEVEL,
        model.maxLevel,
        model.maxLevel = _)

      row().grid(leftLabel(getMessageColon("Default_name"))).add(fName)

      row().grid(leftLabel(getMessageColon("Subtitle"))).add(fSubtitle)

      row()
        .grid(leftLabel(getMessageColon("Description")))
        .add(fDescription)

      row().grid(leftLabel(getMessageColon("Sprite"))).add(fSprite)
      row().grid(leftLabel(getMessage("Face"))).add(fFace)


      row().grid(leftLabel(getMessageColon("Class"))).add(fClass)

      row()
        .grid(leftLabel(getMessageColon("Initial_level")))
        .add(fInitLevel)
      row()
        .grid(leftLabel(getMessageColon("Max_level")))
        .add(fMaxLevel)
    }

    val progressionFields = new StatProgressionPanel(model.progressions)

    new BoxPanel(Orientation.Horizontal) with DisposableComponent {
      contents += bioFields
      contents += progressionFields
    }
  }

  override def onListDataUpdate() = {
    logger.info(getMessage("Characters_Data_Updated"))
    dbDiag.model.enums.characters = dataAsArray
  }
}