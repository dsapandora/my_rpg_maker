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


class EventInstanceDialog(
  owner: Window,
  sm: StateMaster,
  initialEvent: RpgEvent,
  onOk: RpgEvent => Any,
  onCancel: RpgEvent => Any)
  extends StdDialog(owner, getMessageColon("Event_Instance") + initialEvent.name) {

  val model = Utils.deepCopy(initialEvent)

  override def cancelFunc() = onCancel(model)

  def okFunc() = {
    onOk(model)
    close()
  }

  val container = new BoxPanel(Orientation.Vertical) {
    def update() = {
      contents.clear()

      val eventClass =
        sm.getProjData.enums.eventClasses.apply(model.eventClassId)

      val freeVariableMap =
        collection.mutable.Map[String, EventParameter[_]]()
      val componentMap =
        collection.mutable.Map[String, Component]()

      for (state <- eventClass.states;
           cmd <- state.cmds;
           field <- EventParameterField.getParameterFields(owner, sm, cmd);
           if field.model.valueTypeId ==
             EventParameterValueType.LocalVariable.id) {
        val (paramCopy, component) = field.getModelCopyComponent()

        // TODO: Necessary to change the parameter type to constant here.
        paramCopy.valueTypeId = EventParameterValueType.Constant.id

        freeVariableMap.update(field.model.localVariable, paramCopy)
        componentMap.update(field.model.localVariable, component)
      }

      model.params = freeVariableMap.values.toArray[EventParameter[_]]

      contents += new DesignGridPanel {
        componentMap map {
          case (name, component) => row().grid(lbl(name)).add(component)
        }
      }

      revalidate()
      repaint()
      EventInstanceDialog.this.repaint()
    }

    update()
  }

  val fClassId = indexedCombo(
      sm.getProjData.enums.eventClasses,
      model.eventClassId,
      model.eventClassId = _,
      Some(() => {
        container.update()
      }))

  contents = new BoxPanel(Orientation.Vertical) {
    contents += new DesignGridPanel {
      row().grid(lbl(getMessageColon("Event_Class"))).add(fClassId)
    }

    contents += container

    contents += new DesignGridPanel {
      addButtons(okBtn, cancelBtn)
    }
  }
}