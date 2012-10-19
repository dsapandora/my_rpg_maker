package myrpgmaker.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import myrpgmaker.lib.ThreadChecked
import myrpgmaker.model.Project
import myrpgmaker.model.SoundSpec
import myrpgmaker.model.resource.Music
import myrpgmaker.model.resource.MusicPlayer
import myrpgmaker.model.resource.RpgAssetManager
import myrpgmaker.model.resource.Sound
import myrpgmaker.model.resource.SoundPlayer
import myrpgmaker.model.AnimationSound
import myrpgmaker.model.Animation
import myrpgmaker.player.entity.AnimationPlayer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import myrpgmaker.player.entity.FixedAnimationTarget
import myrpgmaker.player.entity.AnimationTarget
import myrpgmaker.player.entity.FixedAnimationTarget
import myrpgmaker.model.MusicSlots

trait RpgScreen extends Screen
  with ThreadChecked {
  def project: Project
  def assets: RpgAssetManager
  def screenW: Int
  def screenH: Int

  def scriptInterface: ScriptInterface

  def renderingOffForTesting: Boolean

  val inputs = new InputMultiplexer()

  val musics = Array.fill[Option[MusicPlayer]](MusicSlots.NUM_SLOTS)(None)

  val batch = if (renderingOffForTesting) null else new SpriteBatch()
  val shapeRenderer = if (renderingOffForTesting) null else new ShapeRenderer()

  /*
   * Music instances 'on their way out'.
   */
  val oldMusics = collection.mutable.Set[MusicPlayer]()

  val screenCamera: OrthographicCamera = new OrthographicCamera()
  screenCamera.setToOrtho(true, screenW, screenH) // y points down
  screenCamera.update()

  val windowManager =
    new WindowManager(assets, project, screenW, screenH, renderingOffForTesting)

  val shakeManager = new ShakeManager

  def playAnimation(animationId: Int, target: AnimationTarget,
      speedScale: Float, sizeScale: Float) = {
    val animation = project.data.enums.animations(animationId)
    val player =
      new AnimationPlayer(project, animation, assets,
          target, speedScale, sizeScale)
    player.play()
    windowManager.animationManager.addAnimation(player)
    player
  }

  def playMusic(slot: Int, specOpt: Option[SoundSpec],
    loop: Boolean, fadeDuration: Float): Unit = {
    assertOnBoundThread()

    if (slot < 0 || slot >= MusicSlots.NUM_SLOTS)
      return

    musics(slot).map({ oldMusic =>
      oldMusics.add(oldMusic)
      oldMusic.volumeTweener.tweenTo(0f, fadeDuration)
      oldMusic.volumeTweener.runAfterDone(() => {
        oldMusic.stop()
        oldMusic.dispose()
        oldMusics.remove(oldMusic)
      })
    })

    musics(slot) = specOpt.map { spec =>
      val resource = Music.readFromDisk(project, spec.sound)
      val newMusic = resource.newPlayer(assets)

      // Start at zero volume and fade to desired volume
      newMusic.stop()
      newMusic.setVolume(0f)
      newMusic.setLooping(loop)
      newMusic.play()
      newMusic.volumeTweener.tweenTo(spec.volume, fadeDuration)

      newMusic
    }
  }

  var soundPlayer:AnimationPlayer = null
  var animationSound:AnimationSound = null
  var animation: Animation = null

  def playSound(soundSpec: SoundSpec): Unit = {
    animationSound = AnimationSound(0.0f, soundSpec)
    animation = Animation(sounds = Array(animationSound))
    soundPlayer = new AnimationPlayer(project, animation, assets,
        new FixedAnimationTarget(0, 0))
    windowManager.animationManager.addAnimation(soundPlayer)
    soundPlayer.play()
  }

  def stopSound() = {
    if(soundPlayer.isInstanceOf[AnimationPlayer]) {
      soundPlayer.stop()
    }
  }

  def render()
  def update(delta: Float)

  def reset() = {
    windowManager.reset()
    for (i <- 0 until musics.length) {
      musics(i).map(_.stop())
      musics(i).map(_.dispose())
      musics(i) = None
    }

    oldMusics.foreach(_.dispose())
    oldMusics.clear()
  }

  override def dispose() = {
    reset()

    windowManager.dispose()

    if (shapeRenderer != null)
      shapeRenderer.dispose()

    if (batch != null)
      batch.dispose()
  }

  override def hide() = {
    assertOnBoundThread()
    inputs.releaseAllKeys()
    Gdx.input.setInputProcessor(null)

    // Sholud start all black again
    windowManager.transitionAlpha = 1.0f

    musics.foreach(_.map(_.pause()))
    oldMusics.foreach(_.pause())
  }

  override def pause() = {
    assertOnBoundThread()
  }

  override def render(delta: Float): Unit = {
    assertOnBoundThread()

    if (!assets.update())
      return

    musics.foreach(_.map(_.update(delta)))
    oldMusics.foreach(_.update(delta))

    // Update tweens
    windowManager.update(delta)

    if (!windowManager.inTransition) {
      shakeManager.update(delta)
      update(delta)
    }

    if (!renderingOffForTesting)
      render()
  }

  override def resize(width: Int, height: Int) = {
    assertOnBoundThread()
    // Do nothing for now
  }

  override def resume() = {
    assertOnBoundThread()
  }

  override def show() = {
    assertOnBoundThread()

    Gdx.input.setInputProcessor(inputs)
    musics.foreach(_.map(_.play()))
    oldMusics.foreach(_.play())
  }
}

trait RpgScreenWithGame extends RpgScreen {
  def game: RpgGame

  def project = game.project
  def screenW = project.data.startup.screenW
  def screenH = project.data.startup.screenH
  def assets = game.assets
  val scriptInterface = new ScriptInterface(game, this)
  val scriptFactory = new ScriptThreadFactory(scriptInterface)

  override def renderingOffForTesting = game.renderingOffForTesting

}
