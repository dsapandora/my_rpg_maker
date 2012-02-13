package myrpgmaker.editor.dialog.db
import myrpgmaker.model.ProjectData
import myrpgmaker.editor.dialog.DatabaseDialog
import myrpgmaker.editor.uibase.RightPaneArrayEditingPanel
import scala.swing._
import myrpgmaker.model.HasName
import myrpgmaker.editor.uibase.DisposableComponent

trait DatabasePanel extends DisposableComponent {
  def panelName: String
  def dbDiag: DatabaseDialog

  def activate(): Unit = {}
}

abstract class RightPaneArrayDatabasePanel[T <: HasName](
  owner: Window,
  initialAry: Array[T])(implicit m: Manifest[T])
  extends RightPaneArrayEditingPanel[T](owner, initialAry)(m)
  with DatabasePanel {

  def label(a: T) = a.name
  def arrayLabel = panelName

  override def activate(): Unit = {
    if (!listView.selection.indices.isEmpty)
      listView.selectIndices(listView.selection.indices.head)
  }

  override def dispose() = {
    super[RightPaneArrayEditingPanel].dispose()
  }
}