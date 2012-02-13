package myrpgmaker.editor.dialog.db

import scala.swing._

import net.java.dev.designgridlayout._
import myrpgmaker.editor._
import myrpgmaker.editor.dialog._
import myrpgmaker.editor.dialog.db.components._
import myrpgmaker.editor.uibase._
import myrpgmaker.editor.uibase.SwingUtils._
import myrpgmaker.model._
import myrpgmaker.model.Constants._
import myrpgmaker.model.event.EventClass
import myrpgmaker.editor.Internationalized._

class EventClassesPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends RightPaneArrayDatabasePanel(
    owner,
    dbDiag.model.enums.eventClasses)
  with DatabasePanel {
  def panelName = getMessage("Event_Classes")
  def newDefaultInstance() = new EventClass()

  def editPaneForItem(idx: Int, model: EventClass) = {
    new EventPanel(
        owner,
        sm,
        None,
        model.name,
        newName => {
          model.name = newName
          refreshModel()
        },
        model.states,
        model.states = _) with DisposableComponent
  }

  override def onListDataUpdate() = {
    logger.info(getMessage("Event_Classes_Updated"))
    dbDiag.model.enums.eventClasses = dataAsArray
  }
}