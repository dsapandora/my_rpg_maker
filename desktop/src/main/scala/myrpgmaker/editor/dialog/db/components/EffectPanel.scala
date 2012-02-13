package myrpgmaker.editor.dialog.db.components

import scala.collection.mutable.ArrayBuffer
import scala.swing.BoxPanel
import scala.swing.Dimension
import scala.swing.Orientation
import scala.swing.Window

import javax.swing.BorderFactory
import myrpgmaker.editor.dialog.DatabaseDialog
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.editor.uibase.NumberSpinner
import myrpgmaker.editor.uibase.SwingUtils.lbl
import myrpgmaker.editor.uibase.TableEditor
import myrpgmaker.model.ArmAdd
import myrpgmaker.model.AtkAdd
import myrpgmaker.model.Effect
import myrpgmaker.model.EffectContext
import myrpgmaker.model.MagAdd
import myrpgmaker.model.MetaEffect
import myrpgmaker.model.MhpAdd
import myrpgmaker.model.MmpAdd
import myrpgmaker.model.MreAdd
import myrpgmaker.model.RecoverHpAdd
import myrpgmaker.model.SpdAdd
import myrpgmaker.editor.Internationalized._

class EffectPanel(
  owner: Window,
  dbDiag: DatabaseDialog,
  initial: Array[Effect],
  onUpdate: Array[Effect] => Unit,
  private var context: EffectContext.Value)
  extends BoxPanel(Orientation.Vertical) {

  def includeStatEffects =
    context != EffectContext.Skill && context != EffectContext.Enemy

  def updateContext(newContext: EffectContext.Value) = {
    context = newContext

    statEffectsPanel.enabled = includeStatEffects
  }

  if (includeStatEffects)
    preferredSize = new Dimension(300, 300)
  else
    preferredSize = new Dimension(250, 200)

  def isStatEffect(e: Effect) = {
    val statKeys =
      Set(MhpAdd, MmpAdd, AtkAdd, SpdAdd, MagAdd, ArmAdd, MreAdd).map(_.id)
    statKeys.contains(e.keyId)
  }

  def updateFromModel() = {
    onUpdate((statEffects ++ miscEffects).toArray)
  }

  val statEffects: collection.mutable.Set[Effect] =
    collection.mutable.Set(initial.filter(isStatEffect): _*)
  val statEffectsPanel = new DesignGridPanel {
    def statSpinner(metaEffect: MetaEffect) = {
      def spinFunc(newValue: Int) = {
        statEffects.retain(_.keyId != metaEffect.id)
        if (newValue != 0)
          statEffects.add(Effect(metaEffect.id, newValue))

        updateFromModel()
      }

      val initialValue =
        statEffects.find(metaEffect.matches _).map(_.v1).getOrElse(0)

      new NumberSpinner(-999, 999, initialValue, spinFunc)
    }

    row()
      .grid(lbl("+" + getMessage("Max_HP"))).add(statSpinner(MhpAdd))
      .grid(lbl("+" + getMessage("Attack"))).add(statSpinner(AtkAdd))
    row()
      .grid(lbl("+" + getMessage("Max_MP"))).add(statSpinner(MmpAdd))
      .grid(lbl("+" + getMessage("Speed"))).add(statSpinner(SpdAdd))

    row()
      .grid(lbl("+" + getMessage("Armor"))).add(statSpinner(ArmAdd))
      .grid(lbl("+" + getMessage("Magic"))).add(statSpinner(MagAdd))

    row()
      .grid(lbl("+" + getMessage("Mag_Res"))).add(statSpinner(MreAdd))
      .grid()
  }

  val miscEffects = ArrayBuffer(initial.filter(!isStatEffect(_)): _*)
  val miscEffectsTable = new TableEditor[Effect]() {
    def title = getMessage("Other_Effects")

    def modelArray = miscEffects
    def newInstance() = Effect(RecoverHpAdd.id)
    def onUpdate() = updateFromModel()

    def colHeaders = Array("Effect", "Value")
    def getRowStrings(effect: Effect) = {
      val meta = effect.meta
      Array(meta.name, meta.renderer(dbDiag.model, effect))
    }

    def showEditDialog(initial: Effect, okCallback: Effect => Unit) = {
      val diag = new EffectDialog(
        owner,
        dbDiag,
        initial,
        okCallback,
        context)
      diag.open()
    }
  }

  if (includeStatEffects) {
    contents += new BoxPanel(Orientation.Vertical) {
      border = BorderFactory.createTitledBorder(getMessage("Stat_Boosts"))
      contents += statEffectsPanel
    }
  }

  contents += miscEffectsTable
}