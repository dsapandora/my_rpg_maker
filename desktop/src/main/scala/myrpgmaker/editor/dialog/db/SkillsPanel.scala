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
import myrpgmaker.editor.Internationalized._

class SkillsPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends RightPaneArrayDatabasePanel(
    owner,
    dbDiag.model.enums.skills) {
  def panelName = getMessage("Skills")
  def newDefaultInstance() = Skill()

  def editPaneForItem(idx: Int, model: Skill) = {
    new BoxPanel(Orientation.Horizontal) with DisposableComponent {
      val normalFields = new DesignGridPanel {
        val fName = textField(
          model.name,
          v => {
            model.name = v
            refreshModel()
          })

        val fScope = enumIdCombo(Scope)(model.scopeId, model.scopeId = _)

        val fCost = new NumberSpinner(0, 100, model.cost, model.cost = _)

        val fAnimationId = indexedCombo(
          dbDiag.model.enums.animations,
          model.animationId,
          model.animationId = _)

        row().grid(lbl(getMessageColon("Name"))).add(fName)
        row().grid(lbl(getMessageColon("Scope"))).add(fScope)
        row().grid(lbl(getMessageColon("Skill_Point_Cost"))).add(fCost)
        row().grid(lbl(getMessageColon("Animation"))).add(fAnimationId)

      }

      contents += new BoxPanel(Orientation.Vertical) {
        contents += normalFields

        val damageHelp = new TextArea {
          text =
            "Damage expressions are JavaScript.\n" +
            "'a' is the attacker and 'b' is the target.\n\n" +
            "Valid expresions are:\n" +
            "a.atk, a.spd, a.mag, " +
            "a.arm, a.mre, a.hp, a.mhp, a.mp, and a.mmp. \n\n" +
            "Same with b.atk, b.spd, etc.\n\n" +
            "Examples:\n" +
            "a.atk*1.2\n" +
            "Math.max(a.atk - b.def, 0)\n" +
            "100 + a.mag"

          maximumSize = new Dimension(300, 300)
          lineWrap = true
          wordWrap = true
          editable = false
        }
        contents += damageHelp
      }

      contents += new BoxPanel(Orientation.Vertical) {
        val effectPanel = new EffectPanel(owner, dbDiag, model.effects,
                                          model.effects = _,
                                          EffectContext.Skill)
        val damagePanel =
          new DamageFormulaArrayPanel(dbDiag, model.damages, model.damages = _)


        contents += effectPanel
        contents += damagePanel
      }
    }

  }

  override def onListDataUpdate() = {
    dbDiag.model.enums.skills = dataAsArray
  }
}