package myrpgmaker.editor

import java.awt.Toolkit
import java.io._
import java.util.Scanner
import java.awt.Desktop
import java.net.URL
import scala.collection.JavaConversions._
import scala.swing._
import javax.swing.ImageIcon
import myrpgmaker.editor.Internationalized._
import myrpgmaker.editor.dialog.ExportDialog
import myrpgmaker.editor.imageset.selector._
import myrpgmaker.editor.misc._
import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.model.resource._
import myrpgmaker.model.event.RpgEvent
import scalaj.http.Http

class ProjectPanel(val mainP: MainPanel, sm: StateMaster)
  extends BorderPanel
  with SelectsMap {
  val tileSelector = new TabbedTileSelector(sm)
  val mapSelector = new ProjectPanelMapSelector(sm, this)
  val mapView = new MapEditor(this, sm, tileSelector)

  val window = mainP.getWindow()
  window.resizable = true
  window.location = new Point(0,0)

  val screenSize = Toolkit.getDefaultToolkit().getScreenSize();

  window.minimumSize = new Dimension(screenSize.width/2,screenSize.height/2)
  window.maximize()

  /**
   * This is the project-wide clipboard for events.
   */
  var eventOnClipboard: Option[RpgEvent] = None

  val projMenu = new PopupMenu {
    contents += new MenuItem(mainP.actionNew)
    contents += new MenuItem(mainP.actionOpen)
    contents += new MenuItem(mainP.actionSave)
    contents += new MenuItem(mainP.actionSettings)
    contents += new MenuItem(mainP.actionClose)
  }

  def selectMap(mapOpt: Option[RpgMap]) = {
    List(tileSelector, mapView).map(_.selectMap(mapOpt))
  }

  val topBar = new BoxPanel(Orientation.Horizontal) {
    import myrpgmaker.editor.dialog._

    contents += new Button {
      val btn = this
      action = Action(getMessage("Project") + " \u25BC") {
        projMenu.show(btn, 0, btn.bounds.height)
      }
      icon = new ImageIcon(Utils.readClasspathImage(
        "crystal_project/16x16/devices/blockdevice.png"))
    }
    contents += new Button(Action(getMessage("Database") + "...") {
      val d = new DatabaseDialog(mainP.topWin, sm)
      d.open()
    }) {
      icon = new ImageIcon(Utils.readClasspathImage(
        "crystal_project/16x16/apps/database.png"))
    }
    contents += new Button(Action(getMessage("Resources") + "...") {
      val d = new ResourcesDialog(mainP.topWin, sm)
      d.open()
    }) {
      icon = new ImageIcon(Utils.readClasspathImage(
        "crystal_project/16x16/filesystems/folder_images.png"))
    }
    contents += new Button(Action(getMessage("Play") + "...") {
      if (sm.askSaveUnchanged(this)) {
        def inheritIO(src: InputStream, dest: PrintStream) = {
          new Thread(new Runnable() {
              def run() = {
                  val sc = new Scanner(src);
                  while (sc.hasNextLine()) {
                      dest.println(sc.nextLine());
                  }
              }
          }).start();
        }

        val projPath = sm.getProj.dir.getCanonicalPath()

        val processBuilder: ProcessBuilder = {
          val separator = System.getProperty("file.separator")
          val cpSeparator = System.getProperty("path.separator")
          val classpath =
            List("java.class.path", "java.boot.class.path",
                 "sun.boot.class.path")
              .map(s => System.getProperty(s, "")).mkString(cpSeparator)

          val javaPath =
            System.getProperty("java.home") +
              separator +
              "bin" +
              separator +
              "java";

          new ProcessBuilder(javaPath, "-cp",
            classpath,
            "myrpgmaker.editor.RpgDesktop",
            "--player",
            projPath)
        }

        println(processBuilder.command().mkString(" "))
        val process = processBuilder.start()
        inheritIO(process.getInputStream(), System.out)
        inheritIO(process.getErrorStream(), System.err)

        process.waitFor();
      }
    }) {
      icon = new ImageIcon(Utils.readClasspathImage(
        "crystal_project/16x16/actions/player_play.png"))
    }

    contents += new Button(Action(getMessage("Export") + "...") {
      val d = new ExportDialog(mainP.topWin, sm, mainP)
      d.open()
    }) {
      icon = new ImageIcon(Utils.readClasspathImage(
        "crystal_project/16x16/actions/fileexport.png"))
    }

    contents += new Button(Action(getMessage("Asset_Server") + "...") {
      if(Desktop.isDesktopSupported()) {
        var desktop =  Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
              var host = Settings.get("assetserver.host").get
              if(VisibleConnection.authenticated) {
                var username = Settings.get("assetserver.username").get
                var password = Settings.get("assetserver.password").get
                desktop.browse(new URL(host + "/api/v1/login/with/redirect/"+username+"/"+password+"?redirect=/").toURI());
              } else {
                desktop.browse(new URL(host).toURI());
              }

            } catch {
              case e: Exception => e.printStackTrace();
            }
        }
      }
    }) {
      icon = new ImageIcon(Utils.readClasspathImage(
        "hendrik-weiler-theme/asset-server.png"))
    }


    contents += new Button(Action(getMessage("Show_Community_Chat") + "...") {
      if(Desktop.isDesktopSupported()) {
        var desktop =  Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                var host = Settings.get("assetserver.host").get
                if(VisibleConnection.authenticated) {
                  var username = Settings.get("assetserver.username").get
                  var password = Settings.get("assetserver.password").get
                  desktop.browse(new URL(host + "/api/v1/login/with/redirect/"+username+"/"+password+"?redirect=/#showchat").toURI());
                } else {
                  desktop.browse(new URL(host).toURI());
                }
            } catch {
              case e: Exception => e.printStackTrace();
            }
        }
      }
    }) {
      icon = new ImageIcon(Utils.readClasspathImage(
        "hendrik-weiler-theme/community_chat.png"))
    }
  }

  val sidePane =
    new SplitPane(Orientation.Horizontal, tileSelector, mapSelector) {
      resizeWeight = 1.0
    }

  layout(mapView) = BorderPanel.Position.Center
  layout(sidePane) = BorderPanel.Position.West
  layout(topBar) = BorderPanel.Position.North

  // select most recent or first map if not empty
  val initialMap = {
    val mapStates = sm.getMapStates
    if (!mapStates.isEmpty) {
      val idToLoad =
        if (mapStates.contains(sm.getProj.data.recentMapName))
          sm.getProj.data.recentMapName
        else
          mapStates.keys.min

      mapStates.get(idToLoad).map(_.map)
    } else None
  }

  // This calls the selectMapFunction
  selectMap(initialMap)
  for (map <- initialMap;
       node <- mapSelector.getNode(map.name)) {
    mapSelector.highlightWithoutEvent(node)
  }

  mainP.revalidate()
}

