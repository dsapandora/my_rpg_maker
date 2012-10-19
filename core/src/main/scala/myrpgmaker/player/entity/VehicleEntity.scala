package myrpgmaker.player.entity

import myrpgmaker.player._
import myrpgmaker.model.event._
import myrpgmaker.player.Finishable
import com.typesafe.scalalogging.slf4j.LazyLogging

class VehicleEntity(game: RpgGame, vehicleId: Int)
  extends Entity(
      game.spritesets,
      game.mapScreen.mapAndAssetsOption,
      game.mapScreen.allEntities)
  with LazyLogging {
  override def height =
    if (playerInThisVehicle()) EventHeight.OVER.id else EventHeight.SAME.id
  override def trigger = EventTrigger.BUTTON.id

  override def activate(activatorsDirection: Int): Option[Finishable] = {
    Some(game.mapScreen.scriptFactory.runFunction(
        "game.enterVehicle(%d);".format(vehicleId)))
  }

  override def dispose() = {
  }

  def playerInThisVehicle() =
    playerEntity.inVehicle && playerEntity.inVehicleId == vehicleId

  override def currentStep() = {
    if (playerInThisVehicle)
      playerEntity.currentStep()
    else
      super.currentStep()
  }

  override def update(delta: Float, eventsEnabled: Boolean) = {
    super.update(delta, eventsEnabled)

    // Have the correct vehicle 'trail' the player.
    val playerEntity = game.mapScreen.playerEntity
    if (playerInThisVehicle) {
      this.x = playerEntity.x
      this.y = playerEntity.y
      this.dir = playerEntity.dir
    }
  }

  setSprite(game.project.data.enums.vehicles(vehicleId).sprite)
}