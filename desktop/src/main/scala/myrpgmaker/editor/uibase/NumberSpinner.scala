package myrpgmaker.editor.uibase

import scala.swing._
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent

class NumberSpinner(
  min: Int,
  max: Int,
  initial: Int,
  onUpdate: ((Int) => Unit) = (v) => {},
  additionalAction: Option[() => Unit] = None,
  step: Int = 1)
  extends BoxPanel(Orientation.Horizontal) {
  val normalizedInitial = math.min(math.max(initial, min), max)

  val model = new SpinnerNumberModel(normalizedInitial, min, max, step)

  val spinner = new JSpinner(model)

  spinner.addChangeListener(new ChangeListener() {
    override def stateChanged(e: ChangeEvent) {
      onUpdate(getValue)
      additionalAction.foreach(_.apply())
    }
  })
  contents += Component.wrap(spinner)

  def getValue = model.getNumber().intValue()
  def setValue(v: Int) = model.setValue(v)

  override def enabled_=(b: Boolean) = {
    super.enabled_=(b)
    spinner.setEnabled(b)
  }
}

class FloatSpinner(
  min: Float,
  max: Float,
  step: Float,
  initial: Float,
  onUpdate: ((Float) => Unit) = (v) => {})
  extends BoxPanel(Orientation.Horizontal) {
  val normalizedInitial = math.min(math.max(initial, min), max)

  val model = new SpinnerNumberModel(normalizedInitial, min, max, step) {
    override def getPreviousValue() = {
      new java.lang.Float(math.max(getNumber().floatValue() - step, min))
    }

    override def getNextValue() = {
      new java.lang.Float(math.min(getNumber().floatValue() + step, max))
    }
  }

  val spinner = new JSpinner(model)

  spinner.addChangeListener(new ChangeListener() {
    override def stateChanged(e: ChangeEvent) {
      onUpdate(getValue)
    }
  })
  contents += Component.wrap(spinner)

  def getValue = model.getNumber().floatValue()
  def setValue(v: Float) = model.setValue(v)

  override def enabled_=(b: Boolean) = {
    super.enabled_=(b)
    spinner.setEnabled(b)
  }
}