package myrpgmaker.editor.cache

import myrpgmaker.model._
import myrpgmaker.model.resource._

class AssetCache(proj: Project) {
  private var tilesetMap = Map[String, Tileset]()
  private var autotileMap = Map[String, Autotile]()

  private def invalidate() = {
    val tilesets = Tileset.list(proj).map(Tileset.readFromDisk(proj, _))
    val autotiles = Autotile.list(proj).map(Autotile.readFromDisk(proj, _))

    tilesetMap = Map(tilesets.map(t => t.name -> t): _*)
    autotileMap = Map(autotiles.map(a => a.name -> a): _*)
  }

  def getTileset(name: String) = {
    if (!tilesetMap.contains(name))
      invalidate()
    tilesetMap(name)
  }
  def getAutotile(name: String) = {
    if (!autotileMap.contains(name))
      invalidate()
    autotileMap(name)
  }
  def getSpriteset(name: String) =
    Spriteset.readFromDisk(proj, name)

  // Invalidate initally to load it all
  invalidate()

}