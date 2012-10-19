package myrpgmaker.player.entity

import scala.concurrent.Channel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.badlogic.gdx.graphics.g2d.SpriteBatch

import myrpgmaker.lib.GdxUtils.syncRun
import myrpgmaker.lib.Layout
import myrpgmaker.lib.Rect
import myrpgmaker.lib.Utils
import myrpgmaker.model.SoundSpec
import myrpgmaker.model.resource.Sound
import myrpgmaker.player.ChoiceInputHandler
import myrpgmaker.player.InputMultiplexer
import myrpgmaker.player.MyKeys
import myrpgmaker.player.MyKeys.Cancel
import myrpgmaker.player.MyKeys.Down
import myrpgmaker.player.MyKeys.Left
import myrpgmaker.player.MyKeys.OK
import myrpgmaker.player.MyKeys.Right
import myrpgmaker.player.MyKeys.Up
import myrpgmaker.player.PersistentState
import myrpgmaker.player.WindowManager

trait HasIntCallback {
  def intCallback(value: Int): Unit
}

abstract class ChoiceWindow(
  persistent: PersistentState,
  manager: WindowManager,
  inputs: InputMultiplexer,
  layout: Layout,
  invisible: Boolean = false,
  defaultChoice: Int = 0,
  allowCancel: Boolean = true)
  extends Window(manager, inputs, layout, invisible)
  with ChoiceInputHandler {

  private var choiceChangeCallback: HasIntCallback = null
  private var _curChoice = defaultChoice

  def curChoice = _curChoice
  def setCurChoice(choiceId: Int) = {
    assertOnBoundThread()
    _curChoice = choiceId
    if (choiceChangeCallback != null) {
      Future {
        choiceChangeCallback.intCallback(choiceId)
      }
    }
  }

  override def capturedKeys =
    Set(MyKeys.Left, MyKeys.Right, MyKeys.Up, MyKeys.Down,
        MyKeys.OK, MyKeys.Cancel)

  val choiceChannel = new Channel[Int]()

  def project = manager.project
  def assets = manager.assets

  override def startClosing() = {
    super.startClosing()
    choiceChannel.write(-1)
  }

  def optionallyReadAndLoad(spec: Option[SoundSpec]) = {
    val snd = spec.map(s => Sound.readFromDisk(project, s.sound))
    snd.map(_.loadAsset(assets))
    snd
  }

  val soundSelect = optionallyReadAndLoad(project.data.startup.soundSelect)
  val soundCursor = optionallyReadAndLoad(project.data.startup.soundCursor)
  val soundCancel = optionallyReadAndLoad(project.data.startup.soundCancel)
  val soundCannot = optionallyReadAndLoad(project.data.startup.soundCannot)

  class ChoiceWindowScriptInterface extends WindowScriptInterface {
    def getChoice() = choiceChannel.read

    def setChoiceChangeCallback(callback: HasIntCallback) = {
      choiceChangeCallback = callback
      syncRun {
        setCurChoice(curChoice)
      }
    }

    def takeFocus(): Unit = syncRun {
      inputs.remove(ChoiceWindow.this)
      inputs.prepend(ChoiceWindow.this)
      manager.focusWindow(ChoiceWindow.this)
    }
  }

  override lazy val scriptInterface = new ChoiceWindowScriptInterface
}

/**
 * @param   choices     Is an Array[Set[Rect]] to support some choices being
 *                      defined by multiple rectangles on screen. For instance,
 *                      selecting all the members of your party during a battle.
 */
class SpatialChoiceWindow(
  persistent: PersistentState,
  manager: WindowManager,
  inputs: InputMultiplexer,
  choices: Array[Set[Rect]] = Array(),
  defaultChoice: Int = 0)
  extends ChoiceWindow(persistent, manager, inputs, Layout.empty,
                       invisible = true, defaultChoice, allowCancel = true) {
  def keyActivate(key: Int): Unit = {
    import MyKeys._

    if (state != Window.Open)
      return

    // TODO: Remove hack
    // Need to finish loading all assets before accepting key input
    assets.finishLoading()

    import MyKeys._
    if (key == Up || key == Left) {
      setCurChoice(Utils.pmod(curChoice - 1, choices.length))
      soundCursor.map(_.getAsset(assets).play())
    } else if (key == Down || key == Right) {
      setCurChoice(Utils.pmod(curChoice + 1, choices.length))
      soundCursor.map(_.getAsset(assets).play())
    }

    if (key == OK) {
      soundSelect.map(_.getAsset(assets).play())
      choiceChannel.write(curChoice)
    }

    if (key == Cancel) {
      soundCancel.map(_.getAsset(assets).play())
      choiceChannel.write(-1)
    }
  }

  override def render(b: SpriteBatch): Unit = {
    // Draw the window and text
    super.render(b)

    if (curChoice >= choices.length || curChoice < 0)
      return

    for (choiceRect <- choices(curChoice)) {
      skin.draw(b, skinTexture,
                choiceRect.left, choiceRect.top, choiceRect.w, choiceRect.h,
                bordersOnly = true)
    }
  }
}