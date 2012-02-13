package myrpgmaker.editor.dialog

import myrpgmaker.editor._
import myrpgmaker.editor.uibase._
import scala.swing._
import scala.swing.event._
import myrpgmaker.model._
import myrpgmaker.model.resource._
import net.java.dev.designgridlayout._
import myrpgmaker.editor.imageset.metadata._
import myrpgmaker.editor.uibase.StdDialog
import myrpgmaker.editor.Internationalized._ 

class ResourcesDialog(owner: Window, sm: StateMaster)
  extends StdDialog(owner, getMessage("Resources")) {
  def okFunc() = {
    tilesetsMetadataPanel.save()
    close()
  }

  centerDialog(new Dimension(1200, 730))

  val importResourcesPanel = new ImportResourcesPanel(sm)
  val tilesetsMetadataPanel = new TilesetsMetadataPanel(sm)

  val tabPane = new TabbedPane() {
    import TabbedPane._
    pages += new Page(getMessage("Import"), importResourcesPanel)
    pages += new Page(getMessage("Tilesets"), tilesetsMetadataPanel)
  }

  contents = new DesignGridPanel {
    row().grid().add(tabPane)
    addButtons(okBtn, cancelBtn)
  }
}
