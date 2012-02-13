package myrpgmaker.editor.dialog

import myrpgmaker.editor.uibase._
import myrpgmaker.editor.uibase.SwingUtils._
import myrpgmaker.editor.misc._
import scala.swing._
import scala.swing.event._
import myrpgmaker.model._
import myrpgmaker.model.resource._
import net.java.dev.designgridlayout._
import java.io._
import myrpgmaker.lib.FileHelper._
import myrpgmaker.editor.uibase.StdDialog
import myrpgmaker.model.resource.Resource
import myrpgmaker.editor.Internationalized._

class NewProjectDialog(owner: Window, onSuccess: Project => Any)
  extends StdDialog(owner, getMessage("New_Project")) {
  val rootChooser = Paths.getRootChooserPanel(() => Unit)

  centerDialog(new Dimension(400, 200))

  val shortnameField = new TextField() {
    columns = 12
  }

  def okFunc() = {
    if (shortnameField.text.isEmpty)
      Dialog.showMessage(shortnameField, getMessage("Need_Short_Name"))
    else {
      val shortname = shortnameField.text
      val projectDirectory = new File(rootChooser.getRoot, shortname)

      val projectOption =
        myrpgmaker.util.ProjectCreator.create(shortname, projectDirectory)
      
      if (projectOption.isDefined) {
        onSuccess(projectOption.get)
        close()
      } else
        Dialog.showMessage(okBtn, getMessage("File_Write_Error"), getMessage("Error"),
          Dialog.Message.Error)
    }
  }

  contents = new DesignGridPanel {

    row().grid().add(leftLabel(getMessageColon("Directory_Project")))
    row().grid().add(rootChooser)

    row().grid().add(leftLabel(getMessageColon("Project_Shortname")))
    row().grid().add(shortnameField)

    addButtons(okBtn, cancelBtn)

    shortnameField.requestFocus()
  }
}