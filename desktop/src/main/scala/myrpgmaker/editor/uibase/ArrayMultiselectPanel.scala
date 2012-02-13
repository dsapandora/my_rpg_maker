package myrpgmaker.editor.uibase

import scala.swing._
import scala.swing.event._
import myrpgmaker.editor.dialog._
import myrpgmaker.model._
import javax.swing.BorderFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.swing.ListView.Renderer
import javax.swing.DefaultListSelectionModel

class ArrayMultiselectPanel[T <: HasName](
  owner: Window,
  label: String,
  choices: Array[T],
  initialSelections: Seq[Int],
  onUpdate: Array[Int] => Unit)
  extends ScrollPane
  with LazyLogging {
  val model = collection.mutable.Set[Int](initialSelections: _*)

  val gridPanel = new GridPanel(choices.size, 1) {
    for ((choice, i) <- choices.zipWithIndex) {
      contents += SwingUtils.boolField(
        choice.name,
        model.contains(i),
        v => if (v) model.add(i) else model.remove(i),
        Some(() => onUpdate(model.toArray)))
    }
  }

  border = BorderFactory.createTitledBorder(label)

  contents = gridPanel


  preferredSize = new Dimension(100, 100)
}