package myrpgmaker.editor.dialog.db.components

import javax.swing.BorderFactory
import myrpgmaker.editor.uibase._
import myrpgmaker.editor.dialog._
import myrpgmaker.lib._
import myrpgmaker.model._
import scala.collection.mutable.ArrayBuffer
import scala.swing._
import myrpgmaker.editor.Internationalized._

class LearnedSkillPanel(
  owner: Window,
  dbDiag: DatabaseDialog,
  initial: Array[LearnedSkill],
  onUpdate: Array[LearnedSkill] => Unit)
  extends BoxPanel(Orientation.Vertical) {

  preferredSize = new Dimension(150, 200)

  val learnedSkills = ArrayBuffer(initial : _*)

  val tableEditor = new TableEditor[LearnedSkill]() {
    def title = getMessage("Learned_Skills")
    
    def modelArray = learnedSkills
    def newInstance() = LearnedSkill(1, 0)
    def onUpdate() = LearnedSkillPanel.this.onUpdate(learnedSkills.toArray)
      
    def colHeaders = Array(getMessage("Level"), getMessage("Skill"))
    def getRowStrings(learnedSkill: LearnedSkill) = {
      val skill = dbDiag.model.enums.skills(learnedSkill.skillId)
      Array("Level %d".format(learnedSkill.level),
            StringUtils.standardIdxFormat(learnedSkill.skillId, skill.name))
    }

    def showEditDialog(initial: LearnedSkill, 
                       okCallback: LearnedSkill => Unit) = {
      val diag = new LearnedSkillDialog(
        owner,
        dbDiag,
        initial,
        okCallback)
      diag.open()
    }
  }

  contents += tableEditor
}

class LearnedSkillDialog(
  owner: Window,
  dbDiag: DatabaseDialog,
  initial: LearnedSkill,
  onOk: LearnedSkill => Unit)
  extends StdDialog(owner, getMessage("Learned_Skill")) {

  import SwingUtils._

  val model = initial

  val fLevel = new NumberSpinner(1, 100, model.level, model.level = _)
  val fSkill =
    indexedCombo(dbDiag.model.enums.skills, model.skillId, model.skillId = _)

  contents = new DesignGridPanel {
    row().grid(lbl("Level:")).add(fLevel)
    row().grid(lbl("Skill:")).add(fSkill)

    addButtons(okBtn, cancelBtn)
  }

  def okFunc() = {
    onOk(model)
    close()
  }
}