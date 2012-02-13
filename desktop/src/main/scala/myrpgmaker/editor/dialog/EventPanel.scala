package myrpgmaker.editor.dialog

import myrpgmaker.editor.StateMaster
import myrpgmaker.editor.uibase.SwingUtils._
import myrpgmaker.model.MapLoc
import scala.swing.Window
import scala.swing.BoxPanel
import scala.swing.TabbedPane
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.model.event.RpgEventState
import myrpgmaker.lib.Utils
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.TabbedPane.Page
import scala.swing.Action
import myrpgmaker.model.RpgMapData
import myrpgmaker.editor.Internationalized._

class EventPanel(
  owner: Window,
  sm: StateMaster,
  eventLoc: Option[MapLoc],
  initialName: String,
  updateNameF: String => Unit,
  initialStates: Array[RpgEventState],
  updateStatesF: Array[RpgEventState] => Unit) extends DesignGridPanel {

  private var statesModel = initialStates
  def updateStates(newStatesModel: Array[RpgEventState]) = {
    statesModel = newStatesModel
    updateStatesF(statesModel)
  }

  val nameField = textField(initialName, updateNameF)

  val tabPane = new TabbedPane() {
    def loadPanesFromModel(): Unit = {
      pages.clear()
      for (i <- 0 until statesModel.length) {
        val state = statesModel(i)
        val pane: EventStatePane = new EventStatePane(
            owner,
            sm,
            state,
            i,
            eventLoc,
            () => {
              if (state.runOnceThenIncrementState &&
                  i == statesModel.length - 1) {
                newState(false, false)
              }
            })
        pages += new Page(getMessage("State") + " %d".format(i), pane)
      }
    }

    def curPane = selection.page.content.asInstanceOf[EventStatePane]
  }

  tabPane.loadPanesFromModel()

  def newState(copyCurrent: Boolean, switchPane: Boolean): Unit = {
    // Add to list of states
    val newPaneIdx = tabPane.curPane.idx + 1
    val currentState = statesModel(tabPane.curPane.idx)
    val newState =
      if (copyCurrent)
        currentState.copyAll()
      else
        currentState.copyEssentials()

    updateStates(
      statesModel.take(newPaneIdx) ++ Array(newState) ++
        statesModel.takeRight(statesModel.size - newPaneIdx))

    tabPane.loadPanesFromModel()

    if (switchPane) {
      tabPane.selection.index = newPaneIdx
    }
  }

  def deleteState() = {
    if (statesModel.size == 1) {
      Dialog.showMessage(tabPane, getMessage("Cannot_Delete_The_Last_State"), getMessage("Error"),
        Dialog.Message.Error)
    } else {
      val deletedIdx = tabPane.curPane.idx

      updateStates(
        statesModel.take(deletedIdx) ++
          statesModel.takeRight(statesModel.size - deletedIdx - 1))

      tabPane.loadPanesFromModel()
      tabPane.selection.index = math.min(deletedIdx, statesModel.size - 1)
    }
  }

  row().grid()
    .add(leftLabel(getMessage("Name"))).add(nameField)
    .add(
      new Button(Action(getMessage("New_State")) {
        newState(false, true)
      }))
    .add(
      new Button(Action(getMessage("Copy_State")) {
        newState(true, true)
      }))
    .add(
      new Button(Action(getMessage("Delete_State")) {
        deleteState()
      }))

  row.grid().add(tabPane)
}