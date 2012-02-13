package myrpgmaker.editor

import myrpgmaker.lib._

import myrpgmaker.editor.dialog._
import myrpgmaker.editor.uibase._

import scala.swing._
import scala.swing.event._

import javax.imageio._
import javax.swing.ImageIcon

import myrpgmaker.model._
import myrpgmaker.model.resource._

class StartPanel(val mainP: MainPanel)
  extends BoxPanel(Orientation.Vertical) {

  val img = new Label { 
  	icon = new ImageIcon(myrpgmaker.lib.Utils.readClasspathImage("hendrik-weiler-theme/splash.jpg")) 
  }
  contents += new BorderPanel {
  	add(img, BorderPanel.Position.East)
  }

  contents += new BoxPanel(Orientation.Horizontal) {
	contents += new Button(mainP.actionNew)
    contents += Swing.HStrut(32)
	contents += new Button(mainP.actionOpen)
  }
}

