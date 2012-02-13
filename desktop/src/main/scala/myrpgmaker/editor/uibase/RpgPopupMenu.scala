package myrpgmaker.editor.uibase

import scala.swing._
import javax.swing.JPopupMenu
import scala.swing.Component
import scala.swing.SequentialContainer.Wrapper
import javax.swing.event._

object RpgPopupMenu {
  private[RpgPopupMenu] trait JPopupMenuMixin {
    def popupMenuWrapper: RpgPopupMenu
  }
}

class RpgPopupMenu extends Component with Wrapper {

  override lazy val peer: JPopupMenu =
    new JPopupMenu with RpgPopupMenu.JPopupMenuMixin with SuperMixin {
      def popupMenuWrapper = RpgPopupMenu.this
    }

  def show(invoker: Component, x: Int, y: Int): Unit =
    peer.show(invoker.peer, x, y)

  def showWithCallback(invoker: Component, x: Int, y: Int, onHide: () => Unit) = {
    val listener = new PopupMenuListener {
      def popupMenuWillBecomeVisible(e: PopupMenuEvent) = {}
      def popupMenuWillBecomeInvisible(e: PopupMenuEvent) = {
        onHide()
        peer.removePopupMenuListener(this)
      }
      def popupMenuCanceled(e: PopupMenuEvent) = {}
    }

    peer.addPopupMenuListener(listener)
    show(invoker, x, y)
  }

  /* Create any other peer methods here */
}

