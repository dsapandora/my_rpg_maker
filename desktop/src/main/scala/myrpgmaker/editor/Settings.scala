package myrpgmaker.editor

import java.util.Properties

import java.io._
import myrpgmaker.lib.FileHelper._
import myrpgmaker.editor.Internationalized._

object Settings {
  val props = new Properties()

  def propsFile = new File(
    System.getProperty("user.home") + File.separator + ".myrpgmaker" +
      File.separator + "editor.props")

  if (propsFile.isFile && propsFile.canRead)
    props.load(new FileInputStream(propsFile))

  def get(k: String) = Option(props.getProperty(k))

  def set(k: String, v: String) = {
    props.setProperty(k, v)
    propsFile.getFos().map({ fos =>
      props.store(fos, getMessage("myrpgmaker_Editor_Settings"))
      true
    })
  }
}
