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


class LoadProjectDialog(owner: Window, onSuccess: Project => Any)
  extends StdDialog(owner, getMessage("Load_Project")) {
  val rootChooser = Paths.getRootChooserPanel(() => populateList())

  centerDialog(new Dimension(400, 400))

  val projList = new Table() {
    selection.intervalMode = Table.IntervalMode.Single
    selection.elementMode = Table.ElementMode.Row
  }

  def populateList(): Unit = {
    val projects: Array[Project] = {
      val rootPath = rootChooser.getRoot
      if (rootPath.isDirectory && rootPath.canRead) {
        val projs =
          rootPath.listFiles.map(child => {
            if (child.isDirectory && child.canRead) {
              Project.readFromDisk(child)
            } else None
          })

        projs.flatten
      } else Array.empty
    }

    val tableModel = new javax.swing.table.AbstractTableModel() {
      def getColumnCount = 2
      def getRowCount = projects.length

      val cols = Array(getMessage("Shortname"), getMessage("Title"))

      override def getColumnName(col: Int) = cols(col)

      def getValueAt(r: Int, c: Int): String = c match {
        case 0 => projects(r).dir.getName
        case _ => projects(r).data.title
      }

      override def isCellEditable(r: Int, c: Int) = false
    }

    projList.model = tableModel

    projList.peer.getColumnModel().getColumn(0).setPreferredWidth(50)
    projList.peer.getColumnModel().getColumn(1).setPreferredWidth(100)
  }

  populateList()

  def okFunc() = {
    if (!projList.selection.rows.isEmpty) {
      val shortName =
        projList.model.getValueAt(projList.selection.rows.head, 0).toString
      val projectFile = new File(rootChooser.getRoot, shortName)
      val pOpt = Project.readFromDisk(projectFile)
      Settings.set("project.last", projectFile.getAbsolutePath())
      pOpt.map(p => onSuccess(p))
      close()
    } else {
      Dialog.showMessage(okBtn, getMessage("No_Project_Selected"), getMessage("Error"),
        Dialog.Message.Error)
    }
  }

  contents = new DesignGridPanel {

    row().grid().add(leftLabel(getMessageColon("Directory_For_All_Projects")))
    row().grid().add(rootChooser)

    row().grid().add(leftLabel(getMessageColon("Projects")))
    row().grid().add(new ScrollPane {
      preferredSize = new Dimension(300, 150)
      contents = projList
    })

    addButtons(okBtn, cancelBtn)
  }

  listenTo(projList.mouse.clicks)

  reactions += {
    case MouseClicked(`projList`, _, _, 2, _) => okBtn.doClick()
  }
}