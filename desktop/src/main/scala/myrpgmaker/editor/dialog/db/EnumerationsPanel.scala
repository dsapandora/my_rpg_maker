package myrpgmaker.editor.dialog.db

import myrpgmaker.editor._
import myrpgmaker.editor.uibase._
import myrpgmaker.editor.uibase.SwingUtils._
import scala.swing._
import scala.swing.event._
import myrpgmaker.model._
import myrpgmaker.model.resource._
import net.java.dev.designgridlayout._
import myrpgmaker.editor.dialog.DatabaseDialog
import myrpgmaker.editor.Internationalized._

class EnumerationsPanel(
  owner: Window,
  sm: StateMaster,
  val dbDiag: DatabaseDialog)
  extends DesignGridPanel
  with DatabasePanel {
  def panelName = getMessage("Enumerations")
  layout.labelAlignment(LabelAlignment.RIGHT)

  val fElements =
    new StringArrayEditingPanel(
      owner,
      getMessage("Elements"),
      dbDiag.model.enums.elements,
      dbDiag.model.enums.elements = _)

  val fEquipTypes =
    new StringArrayEditingPanel(
      owner,
      getMessage("Equipment_Types"),
      dbDiag.model.enums.equipTypes,
      dbDiag.model.enums.equipTypes = _)

  row.grid().add(fElements).add(fEquipTypes)
}