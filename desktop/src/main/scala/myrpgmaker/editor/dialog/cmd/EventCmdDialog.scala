package myrpgmaker.editor.dialog.cmd

import scala.swing.Dialog
import scala.swing.Window

import myrpgmaker.editor.StateMaster
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.editor.uibase.EventParameterField
import myrpgmaker.editor.uibase.ParameterFullComponent
import myrpgmaker.editor.uibase.StdDialog
import myrpgmaker.editor.uibase.SwingUtils.lbl
import myrpgmaker.lib.Utils
import myrpgmaker.model.event.EventCmd

abstract class EventCmdDialog[T <: EventCmd](
  owner: Window,
  sm: StateMaster,
  title: String,
  initial: T,
  successF: T => Any)(implicit m: reflect.Manifest[T])
  extends StdDialog(owner, title) {

  def normalFields: Seq[EventField] = Nil
  def parameterFields: Seq[EventParameterField[_]] = Nil

  val model: T = Utils.deepCopy(initial)

  def okFunc() = {
    successF(model)
    close()
  }

  contents = new DesignGridPanel {
    for (EventField(fieldName, fieldComponent) <- normalFields) {
      row().grid(lbl(fieldName)).add(fieldComponent)
    }
    ParameterFullComponent.addParameterFullComponentsToPanel(
        owner, this, parameterFields)

    addButtons(okBtn, cancelBtn)
  }
}

object EventCmdDialog {
  /**
   * This function gets a dialog for the given EventCmd
   */
  def dialogFor(
    owner: Window,
    sm: StateMaster,
    mapName: Option[String],
    evtCmd: EventCmd,
    successF: EventCmd => Any): Option[Dialog] = {
    val ui = EventCmdUI.uiFor(evtCmd)
    ui.getDialog(owner, sm, mapName,
        evtCmd.asInstanceOf[ui.EventCmdType],
        successF.asInstanceOf[ui.EventCmdType => Any])
  }
}