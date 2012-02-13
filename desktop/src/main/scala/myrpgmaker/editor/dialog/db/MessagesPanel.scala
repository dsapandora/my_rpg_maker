package myrpgmaker.editor.dialog.db

import scala.swing.Window

import myrpgmaker.editor.Internationalized.getMessage
import myrpgmaker.editor.StateMaster
import myrpgmaker.editor.dialog.DatabaseDialog
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.editor.uibase.StringMapEditingPanel

class MessagesPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends DesignGridPanel
  with DatabasePanel {
  def panelName = getMessage("Messages")

  val fMessages = new StringMapEditingPanel(
    owner,
    getMessage("Messages"),
    dbDiag.model.messages,
    dbDiag.model.messages = _)

  row.grid().add(fMessages)
}