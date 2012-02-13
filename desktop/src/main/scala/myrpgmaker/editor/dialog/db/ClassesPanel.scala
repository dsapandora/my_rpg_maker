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
import myrpgmaker.model.resource._
import net.java.dev.designgridlayout._
import scala.collection.mutable.ArrayBuffer
import myrpgmaker.editor.Internationalized._

class ClassesPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends RightPaneArrayDatabasePanel(
    owner,
    dbDiag.model.enums.classes)
  with DatabasePanel {
  def panelName = getMessage("Classes")
  def newDefaultInstance() = new CharClass()

  def editPaneForItem(idx: Int, model: CharClass) = {
    val fName = textField(
      model.name,
      model.name = _,
      Some(refreshModel))

    val fUnarmedAttackSkillId = indexedCombo(
      dbDiag.model.enums.skills,
      model.unarmedAttackSkillId,
      model.unarmedAttackSkillId = _)

    val fEffects =
      new EffectPanel(owner, dbDiag, model.effects, model.effects = _,
          EffectContext.CharacterClass)

    logger.info(getMessage("constructing_New_Array_Multiselect_Panel") + " %s".format(model.canUseItems) )

    val fCanEquip = new ArrayMultiselectPanel(
      owner,
      getMessage("Can_Equip"),
      dbDiag.model.enums.items,
      model.canUseItems,
      model.canUseItems = _)

    val fLearnedSkills = new LearnedSkillPanel(
      owner,
      dbDiag,
      model.learnedSkills,
      model.learnedSkills = _)

    val mainFields = new DesignGridPanel {
      row().grid(leftLabel(getMessageColon("Name"))).add(fName)
      row().grid(leftLabel(getMessageColon("Unarmed_Attack"))).add(fUnarmedAttackSkillId)
    }

    val rightFields = new GridPanel(2, 1) {
      contents += fCanEquip
      contents += fLearnedSkills
    }

    new BoxPanel(Orientation.Horizontal) with DisposableComponent {
      contents += new BoxPanel(Orientation.Vertical) {
        contents += mainFields
        contents += fEffects
      }
      contents += rightFields
    }
  }

  override def onListDataUpdate() = {
    logger.info(getMessage("Classes_Updated"))
    dbDiag.model.enums.classes = dataAsArray
  }
}