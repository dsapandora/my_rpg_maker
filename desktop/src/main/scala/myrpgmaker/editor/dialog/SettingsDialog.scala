package myrpgmaker.editor.dialog

import myrpgmaker.editor.uibase._
import myrpgmaker.editor.uibase.SwingUtils._
import myrpgmaker.editor.misc.Paths
import scala.swing._
import scala.swing.event._
import myrpgmaker.model._
import myrpgmaker.model.resource._
import net.java.dev.designgridlayout._
import java.io.File
import myrpgmaker.editor.Settings
import myrpgmaker.editor.uibase.StdDialog
import myrpgmaker.editor.Internationalized._
import javax.swing.BorderFactory

import myrpgmaker.editor.VisibleConnection

class SettingsDialog(owner: Window, onSuccess: Project => Any)
  extends StdDialog(owner, getMessage("Settings")) {

  centerDialog(new Dimension(400, 400))

  def okFunc() = {
    VisibleConnection.connection.restart()
    close()
  }

  if (Settings.get("assetserver.host") == "") {
    Settings.set("assetserver.host", "http://assets.myrpgmaker.com")
  }

  var assetserver_host = textField(
    Settings.get("assetserver.host").getOrElse(""),
    Settings.set("assetserver.host", _))
  var assetserver_username = textField(
    Settings.get("assetserver.username").getOrElse(""),
    Settings.set("assetserver.username", _))
  var assetserver_password = textField(
    Settings.get("assetserver.password").getOrElse(""),
    Settings.set("assetserver.password", _))

  contents = new DesignGridPanel {
    border = BorderFactory.createTitledBorder(getMessage("Asset_Server"))
    row().grid().add(leftLabel(getMessageColon("Asset_Server_Hostname")))
    row().grid().add(assetserver_host)
    row().grid().add(leftLabel(getMessageColon("Username")))
    row().grid().add(assetserver_username)
    row().grid().add(leftLabel(getMessageColon("Password")))
    row().grid().add(assetserver_password)

    addButtons(okBtn, cancelBtn)
  }

  //listenTo(projList.mouse.clicks)

  reactions += {
    case MouseClicked(`okBtn`, _, _, 2, _) => okBtn.doClick()
  }
}