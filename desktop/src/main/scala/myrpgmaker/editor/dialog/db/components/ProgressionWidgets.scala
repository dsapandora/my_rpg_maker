package myrpgmaker.editor.dialog.db.components

import scala.swing._
import scala.swing.event._
import myrpgmaker.editor.uibase.SwingUtils._
import myrpgmaker.editor.dialog._
import javax.swing.BorderFactory
import myrpgmaker.model._
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.editor.uibase.FloatSpinner
import myrpgmaker.editor.Internationalized._

/**
 * Edits the model in-place.
 */
class StatProgressionPanel(model: StatProgressions)
  extends DesignGridPanel {

  def progressionEditor(label: String, model: Curve) = {
    val lvl50Val = new TextField {
      editable = false
      text = model(50).toString
    }

    def numSpinner(initial: Float, mutateF: (Float) => Unit) = {
      new FloatSpinner(0, 1000, 0.1f, initial, onUpdate = { v =>
        mutateF(v)
        lvl50Val.text = model(50).toString
      })
    }

    new DesignGridPanel {
      border = BorderFactory.createTitledBorder(label)

      val mSpin = numSpinner(model.perLevel, model.perLevel = _)
      val bSpin = numSpinner(model.base, model.base = _)

      row().grid(leftLabel(getMessage("L1_Base") + ":")).add(bSpin)
      row().grid(leftLabel(getMessage("Per_level") + ":")).add(mSpin)
      row().grid(leftLabel(getMessage("At_L50") + ":")).add(lvl50Val)
    }
  }

  val fExp = progressionEditor(getMessage("EXP_To_Level_Up"), model.exp)
  val fMhp = progressionEditor(getMessage("Max_HP"), model.mhp)
  val fMmp = progressionEditor(getMessage("Max_MP"), model.mmp)
  val fAtk = progressionEditor(getMessage("Attack"), model.atk)
  val fSpd = progressionEditor(getMessage("Speed"), model.spd)
  val fMag = progressionEditor(getMessage("Magic"), model.mag)
  val fArm = progressionEditor(getMessage("Base_Armor"), model.arm)
  val fMre = progressionEditor(getMessage("Magic_Resist"), model.mre)

  row().grid().add(fMhp, fExp)
  row().grid().add(fMmp, fSpd)
  row().grid().add(fAtk, fArm)
  row().grid().add(fMag, fMre)
}