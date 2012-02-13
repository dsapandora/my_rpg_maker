package myrpgmaker.editor.dialog

import scala.swing._
import myrpgmaker.editor.uibase.SwingUtils._
import scala.swing.event._
import myrpgmaker.model.event._
import myrpgmaker.editor.uibase._
import scala.collection.mutable.ArrayBuffer
import scala.swing.TabbedPane.Page
import myrpgmaker.model._
import myrpgmaker.editor.StateMaster
import java.awt.Dimension
import myrpgmaker.editor.uibase.StdDialog
import myrpgmaker.editor.resourceselector.SpriteField
import javax.swing.BorderFactory
import myrpgmaker.lib.Utils
import myrpgmaker.model.MapLoc
import myrpgmaker.model.event.RpgEvent
import myrpgmaker.editor.Internationalized._

class EventDialog(
  owner: Window,
  sm: StateMaster,
  val mapName: String,
  initialEvent: RpgEvent,
  onOk: RpgEvent => Any,
  onCancel: RpgEvent => Any)
  extends StdDialog(owner, getMessageColon("Event") + initialEvent.name + ":" + initialEvent.id) {

  centerDialog(new Dimension(600, 600))

  val event = Utils.deepCopy(initialEvent)

  override def cancelFunc() = onCancel(event)

  def okFunc() = {
    onOk(event)
    close()
  }

  val eventPanel = new EventPanel(
      owner,
      sm,
      Some(MapLoc(mapName, event.x, event.y)),
      event.name,
      event.name = _,
      event.states,
      event.states = _)

  contents = new BoxPanel(Orientation.Vertical) {
    contents += eventPanel
    contents += new DesignGridPanel {
      addButtons(okBtn, cancelBtn)
    }
  }
}