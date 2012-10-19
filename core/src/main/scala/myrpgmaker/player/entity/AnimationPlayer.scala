package myrpgmaker.player.entity

import com.badlogic.gdx.utils.Disposable
import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.model.resource._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import myrpgmaker.player.GdxGraphicsUtils
import myrpgmaker.player.MapScreen
import myrpgmaker.player.EntityInfo
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Color
import myrpgmaker.player.PictureLike

trait AnimationTarget {
  def getScreenCoords(): Option[(Float, Float)]
  def setTint(color: Color): Unit = Unit
}

case class FixedAnimationTarget(x: Float, y: Float) extends AnimationTarget {
  def getScreenCoords() = Some((x, y))
}

class MapEntityAnimationTarget(mapScreen: MapScreen, entity: Entity)
  extends AnimationTarget {
  def getScreenCoords() = {
    val info = EntityInfo.apply(entity, mapScreen)
    Some((info.screenX, info.screenY))
  }
  override def setTint(color: Color) = entity.setTintColor(color)
}

class BoxAnimationTarget(box: BoxLike) extends AnimationTarget {
  def getScreenCoords() = Some((box.x, box.y))
}

class PictureLikeAnimationTarget(picture: PictureLike) extends AnimationTarget {
  override def getScreenCoords() = {
    val rect = picture.getRect()
    Some((rect.x, rect.y))
  }
  override def setTint(color: Color) = picture.setAnimationTint(color)
}

/**
 * Can only be used from the Gdx thread.
 */
class AnimationPlayer(
  proj: Project, animation: Animation, assets: RpgAssetManager,
  target: AnimationTarget, speedScale: Float = 1.0f, sizeScale: Float = 1.0f)
  extends Disposable {

  object States {
    val Idle = 0

    val Playing = 1

    /**
     * When all the visuals are done displaying and we can move onto the next
     * animation.
     */
    val VisualsDone = 2

    /**
     * When we can assume all the sounds are done playing and the resources can
     * be disposed.
     */
    val Expired = 3
  }

  import States._

  case class SoundState(
    animationSound: AnimationSound, resource: Sound, var played: Boolean)

  // Load all the assets used in this animation.
  val animationImages: Array[AnimationImage] = animation.visuals.map(
    v => AnimationImage.readFromDisk(proj, v.animationImage))
  val animationSounds = animation.sounds.map(s => {
    val sound = Sound.readFromDisk(proj, s.sound.sound)
    sound.loadAsset(assets)

    SoundState(s, sound, false)
  })

  animationImages.map(_.loadAsset(assets))

  /**
   *  Time of the previous update call.
   */

  private var _time = 0.0f
  private var _state = Idle

  def playing = _state == Playing
  def visualsDone = _state == VisualsDone || _state == Expired
  def expired = _state == Expired

  def allResourcesLoaded = {
    animationImages.forall(_.isLoaded(assets)) &&
      animationSounds.forall(_.resource.isLoaded(assets))
  }

  def anyFailed = {
    animationImages.exists(_.failed) ||
      animationSounds.exists(_.resource.failed)
  }

  def time = _time

  def reset() = {
    _time = 0
    _state = Idle
    animationSounds.map(_.played = false)
  }

  def play() = {
    reset()
    _state = Playing
  }

  def stop() = {
    reset()
    _state = Expired
  }

  def update(delta: Float): Unit = {
    if (anyFailed) {
      _state = Expired
      return
    }

    if (!allResourcesLoaded) {
      return
    }

    if (_state != Idle) {
      _time += delta * speedScale

      _state match {
        case Playing if _time >= animation.totalTime =>
          _state = VisualsDone
        case VisualsDone if _time >= animation.totalTime + 30 =>
          _state = Expired
        case _ =>
          Unit
      }
    }

    for (soundState <- animationSounds) {
      if (!soundState.played && time >= soundState.animationSound.time &&
        soundState.resource.isLoaded(assets)) {
        val soundSpec = soundState.animationSound.sound
        soundState.resource.getAsset(assets).play(
          soundSpec.volume, soundSpec.pitch, 0f)
        soundState.played = true
      }
    }

    val currentTargetFlashes = animation.flashes
      .filter(_.flashTypeId == AnimationFlashType.Target.id)
      .filter(_.within(time))

    if (!currentTargetFlashes.isEmpty) {
      val tintColor = currentTargetFlashes.map(_.currentColor(time)).maxBy(_.a)
      target.setTint(tintColor)
    } else {
      target.setTint(Color.WHITE)
    }
  }

  /**
   * Assumes |batch| is already centered on the animation origin.
   */
  def render(batch: SpriteBatch, shapeRenderer: ShapeRenderer,
             screenW: Int, screenH: Int): Unit = {
    if (_state != Playing)
      return

    import TweenUtils._
    for ((visual, image) <- animation.visuals zip animationImages) {
      if (visual.within(time) && image.isLoaded(assets)) {
        val alpha = tweenAlpha(visual.start.time, visual.end.time, time)
        val frameIndex = tweenIntInclusive(
          alpha, visual.start.frameIndex, visual.end.frameIndex)

        val screenCoordsOpt = target.getScreenCoords()
        if (screenCoordsOpt.isDefined) {
          val (targetX, targetY) = screenCoordsOpt.get
          val dstX =
            targetX +
              tweenFloat(alpha, visual.start.x, visual.end.x) * sizeScale
          val dstY =
            targetY +
              tweenFloat(alpha, visual.start.y, visual.end.y) * sizeScale

          val xTile = frameIndex % image.xTiles
          val yTile = frameIndex / image.xTiles

          image.drawTileCentered(
            batch, assets, dstX, dstY, xTile, yTile, sizeScale)
        }
      }
    }

    val currentScreenFlashes = animation.flashes
      .filter(_.flashTypeId == AnimationFlashType.Screen.id)
      .filter(_.within(time))

    if (!currentScreenFlashes.isEmpty) {
      // Spritebatch seems to turn off blending after it's done. Turn it on.
      Gdx.gl.glEnable(GL20.GL_BLEND)
      shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

      for (flash <- currentScreenFlashes) {
        shapeRenderer.setColor(flash.currentColor(time))
        shapeRenderer.rect(0, 0, screenW, screenH)
      }

      shapeRenderer.end()
    }

  }

  def dispose() = {
    animationImages.map(_.dispose(assets))
    animationSounds.map(_.resource.dispose(assets))
  }
}