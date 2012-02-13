package myrpgmaker.editor.misc

import myrpgmaker.editor._
import scala.swing._
import scala.swing.event._
import scalaswingcontrib.tree._
import scalaswingcontrib.event._
import scalaswingcontrib.tree.Tree._
import myrpgmaker.model._
import myrpgmaker.model.resource._

class MapSelector(sm: StateMaster)
  extends ScrollPane {
  protected val allNodes = collection.mutable.Map[String, Node]()

  /*
   * TODO: Currently assume no map will be "orphaned". Add code to
   * handle orphans later
   */
  class Node(
    val displayName: String,
    val mapName: String) {
    def children: Seq[Node] =
      sm.getMapMetas
        .filter(_.metadata.parent == mapName)
        .map(Node.apply(_))

    // XXX: Should the allNodes addition really be in the constructor? Shady.
    allNodes.put(mapName, this)

    def getPath(): Path[Node] = {
      if (mapName.isEmpty()) {
        Path(this)
      } else {
        val parentMapName = sm.getMap(mapName).get.metadata.parent
        val parentNode = allNodes(parentMapName)
        parentNode.getPath() ++ Path(this)
      }
    }
  }

  object Node {
    def apply(m: RpgMap) = new Node(m.displayName, m.name)
  }

  def removeNode(n: Node) = {
    allNodes.remove(n.mapName)
    tree.model.remove(n.getPath())
  }

  val projectRoot = new Node(sm.getProj.data.title, "")

  minimumSize = new Dimension(8 * 32, 200)
  preferredSize = new Dimension(8 * 32, 200)

  val tree = new Tree[Node] {
    model = InternalTreeModel(projectRoot)(_.children)

    renderer = Renderer({
      case n: Node => n.displayName
    })
  }

  // Necessary to get the reaction event working :(
  val treeAny = tree.asInstanceOf[Tree[Any]]
  tree.expandAll()
  tree.collapseAll()

  tree.expandPath(Path(projectRoot))

  def getNode(mapName: String) = {
    allNodes.get(mapName)
  }

  def highlightWithoutEvent(node: Node) = {
    deafTo(tree.selection)

    val nodePath = node.getPath()
    val wasCollapsed = tree.isCollapsed(nodePath)

    selectNode(node)

    if (wasCollapsed)
      tree.collapsePath(nodePath)

    listenTo(tree.selection)
  }

  def selectNode(node: Node) = {
    val path = node.getPath()
    tree.expandPath(path)
    tree.selectPaths(path)
  }

  override def enabled_=(b: Boolean) = {
    super.enabled_=(b)
    tree.enabled_=(b)
  }

  contents = tree

  listenTo(tree.selection)

  def onSelectMap(map: Option[RpgMap]) = {}

  reactions += {
    case TreePathSelected(`treeAny`, newPathListAny, _, _, _) => {
      if (!newPathListAny.isEmpty) {
        val newPath = newPathListAny.last.asInstanceOf[Path[Node]]

        val mapName = newPath.last.mapName

        if (mapName.isEmpty()) {
          onSelectMap(None)
        } else {
          val map = sm.getMap(mapName).get

          onSelectMap(Some(map))
        }
      }
    }
  }
}
