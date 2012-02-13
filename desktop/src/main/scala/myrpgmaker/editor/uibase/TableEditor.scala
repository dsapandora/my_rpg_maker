package myrpgmaker.editor.uibase

import javax.swing.table.AbstractTableModel
import scala.swing._
import scala.swing.event.MouseClicked
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import scala.collection.mutable.ArrayBuffer
import myrpgmaker.editor.Internationalized._
import myrpgmaker.editor.util.MouseUtil

/**
 * A table that can be edited by context menu. Can add a new element by double
 * clicking the last empty row. Edits modelArray "in-place" and calls
 * onUpdate whenever its contents are updated.
 */
abstract class TableEditor[T] extends ScrollPane {
  def title: String

  def modelArray: ArrayBuffer[T]
  def newInstance(): T

  minimumSize = new Dimension(100, 100)
  preferredSize = new Dimension(300, 100)

  /**
   * Called whenever modelArray changes.
   */
  def onUpdate()

  def colHeaders: Array[String]
  def getRowStrings(element: T): Array[String]

  def showEditDialog(initial: T, okCallback: T => Unit)

  border = BorderFactory.createTitledBorder(title)

  def modelRowCount: Int = modelArray.length

  val tableModel = new AbstractTableModel() {
    def getRowCount() = modelRowCount + 1 // last element blank for adding
    def getColumnCount() = colHeaders.length
    override def getColumnName(col: Int) = colHeaders(col)

    def getValueAt(row: Int, col: Int) = {
      // There should be just one extra blank row at the end of the table.
      assume(row < modelRowCount + 1)
      if (row < modelRowCount) {
        val element = modelArray(row)
        getRowStrings(element)(col)
      } else {
        "" // blank for new row
      }
    }

    override def isCellEditable(r: Int, c: Int) = false

  }

  val table = new Table {
    model = tableModel

    selection.elementMode = Table.ElementMode.Row
    selection.intervalMode = Table.IntervalMode.Single

    def editDialog(row: Int) = {
      val element = modelArray(row)
      showEditDialog(element, v => {
        modelArray.update(row, v)
        tableModel.fireTableRowsUpdated(row, row)
        onUpdate()
      })
    }

    def newDialog() = {
      val element = newInstance()
      showEditDialog(element, v => {
        modelArray += v
        tableModel.fireTableRowsUpdated(modelRowCount - 1, modelRowCount - 1)
        tableModel.fireTableRowsInserted(modelRowCount, modelRowCount)
        onUpdate()
      })
    }

    // TODO: Refactor the duplicate tableModel.fire ... calls
    listenTo(mouse.clicks)
    reactions += {
      case MouseClicked(_, _, _, 2, _) => {
        val row = selection.rows.head
        if (row < modelRowCount)
          editDialog(row)
        else
          newDialog()
      }
      case e: MouseClicked if MouseUtil.isRightClick(e) => {
        val (x0, y0) = (e.point.getX().toInt, e.point.getY().toInt)

        val row = peer.rowAtPoint(e.point)

        if (row != -1) {
          selection.rows.clear()
          selection.rows += row
          val menu = new RpgPopupMenu {
            contents += new MenuItem(Action(getMessage("New")) {
              newDialog()
            })

            if (row != rowCount - 1) {
              contents += new MenuItem(Action(getMessage("Edit")) {
                editDialog(row)
              })
              contents += new MenuItem(Action(getMessage("Delete")) {
                modelArray.remove(row)
                tableModel.fireTableRowsDeleted(row, row)
                onUpdate()
              })
            }
          }
          menu.show(this, x0, y0)
        }
      }
    }
  }

  contents = table
}