package myrpgmaker.player

import myrpgmaker.lib.ThreadChecked
import myrpgmaker.player.entity.AnimationPlayer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class AnimationManager(screenW: Int, screenH: Int)
  extends ThreadChecked with Disposable {
  val animations = new collection.mutable.HashSet[AnimationPlayer]

  def addAnimation(animation: AnimationPlayer) = {
    animations += animation
  }

  def dispose() = {
    animations.foreach(_.dispose())
  }

  def update(delta: Float) = {
    animations.foreach(_.update(delta))

    val toRemove = new collection.mutable.HashSet[AnimationPlayer]

    for (animation <- animations) {
      animation.update(delta)

      if (animation.expired)
        toRemove += animation
    }

    toRemove.foreach(_.dispose())
    animations --= toRemove
  }

  def render(batch: SpriteBatch, shapeRenderer: ShapeRenderer,
      screenCamera: OrthographicCamera) = {
    batch.begin()

    batch.setProjectionMatrix(screenCamera.combined)
    batch.enableBlending()
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

    animations.foreach(_.render(batch, shapeRenderer, screenW, screenH))
    batch.end()
  }
}