package myrpgmaker.editor.dialog

import scala.swing._
import myrpgmaker.model.event.RpgEventState
import myrpgmaker.editor.uibase.SwingUtils._
import myrpgmaker.model.event.EventHeight
import myrpgmaker.editor.resourceselector.SpriteField
import myrpgmaker.editor.StateMaster
import myrpgmaker.model.SpriteSpec
import myrpgmaker.editor.uibase.DesignGridPanel
import myrpgmaker.model.event.EventTrigger
import myrpgmaker.model.event.AnimationType
import javax.swing.BorderFactory
import myrpgmaker.model.MapLoc
import myrpgmaker.editor.Internationalized._

/**
 * @param   model   Mutated in-place.
 */
class EventStatePane(
  owner: Window,
  sm: StateMaster,
  model: RpgEventState,
  val idx: Int,
  eventLoc: Option[MapLoc],
  runOnceChangeCallback: () => Unit)
  extends BoxPanel(Orientation.Horizontal) {

  val heightBox =
    enumIdCombo(EventHeight)(model.height, model.height = _)
  val fAffixDirection =
    boolField(getMessage("Affix_Direction"), model.affixDirection, model.affixDirection = _)

  val spriteBox = new SpriteField(
    owner,
    sm,
    model.sprite,
    (spriteSpec: Option[SpriteSpec]) => {
      // If the sprite's "existence" has changed...
      if (model.sprite.isDefined != spriteSpec.isDefined) {
        heightBox.selection.index =
          if (spriteSpec.isDefined)
            EventHeight.SAME.id
          else
            EventHeight.UNDER.id
      }

      model.sprite = spriteSpec
    })

  val triggerBox =
    enumIdCombo(EventTrigger)(model.trigger, model.trigger = _)

  val animationTypeBox =
    enumIdCombo(AnimationType)(model.animationType, model.animationType = _)

  val fRunOnce =
    boolField(getMessage("Run_Once_Then_Increment_State"),
        model.runOnceThenIncrementState,
        model.runOnceThenIncrementState = _,
        Some(runOnceChangeCallback))

  contents += new BoxPanel(Orientation.Vertical) {
    contents += new DesignGridPanel {
      border = BorderFactory.createTitledBorder(getMessage("Appearance"))

      row()
        .grid().add(leftLabel(getMessageColon("Height")))
      row()
        .grid().add(heightBox)
      row()
        .grid().add(leftLabel(getMessageColon("Sprite")))
      row()
        .grid().add(spriteBox)
      row().grid().add(fAffixDirection)
    }

    contents += new DesignGridPanel {
      border = BorderFactory.createTitledBorder(getMessage("Behavior"))
      row().grid().add(leftLabel(getMessageColon("Trigger")))
      row().grid().add(triggerBox)
      row().grid().add(leftLabel(getMessageColon("AnimationType")))
      row().grid().add(animationTypeBox)
      row().grid().add(fRunOnce)
    }

    contents += new ConditionsPanel(
        owner, sm.getProjData, model.conditions, model.conditions = _)
  }

  val commandBox = new CommandBox(
    owner,
    sm,
    eventLoc,
    model.cmds,
    newCmds => {
      model.cmds = newCmds
      messageField.updateMessages()
    },
    inner = false)

  val messageField = new TextField() {
    enabled = true
    editable = false

    def updateMessages() = {
      val freeVars = model.getFreeVariables()
      if (!freeVars.isEmpty) {
        text = getMessage("Free_Variables_Normal_For_Event_Classes") + ": %s.".format(
            freeVars.map(_.localVariable).mkString(", "))
      } else {
        text = getMessage("No_Errors")
      }
    }
    updateMessages()
  }

  contents += new DesignGridPanel {
    row.grid.add(leftLabel(getMessageColon("Commands")))
    row.grid.add(new ScrollPane {
      preferredSize = new Dimension(400, 400)
      contents = commandBox
    })

    row.grid.add(messageField)
  }
}