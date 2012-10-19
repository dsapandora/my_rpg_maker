package myrpgmaker.model.resource

import myrpgmaker.lib._
import myrpgmaker.model._
import myrpgmaker.lib.Utils._
import myrpgmaker.lib.FileHelper._
import java.io._
import javax.sound.midi._
import com.badlogic.gdx.audio.{ Music => GdxMusic }
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.utils.Disposable
import com.google.common.io.Files
import com.typesafe.scalalogging.slf4j.LazyLogging
import myrpgmaker.player.FloatTweener

case class MusicMetadata()

case class Music(
  proj: Project,
  name: String,
  metadata: MusicMetadata)
  extends Resource[Music, MusicMetadata]
  with RpgGdxAsset[GdxMusic] {
  def meta = Music

  def isMidi = {
    val extension = Files.getFileExtension(name)
    extension == "midi" || extension == "mid"
  }

  def newPlayer(assets: RpgAssetManager) = {
    if (isMidi)
      new MidiMusicPlayer(this)
    else {
      new GdxMusicPlayer(assets, this)
    }
  }
}

object Music extends MetaResource[Music, MusicMetadata] {
  def rcType = "music"
  def keyExts = Array("wav", "mp3", "ogg", "midi", "mid")

  def defaultInstance(proj: Project, name: String) =
    Music(proj, name, MusicMetadata())
}

trait MusicPlayer extends Disposable {
  def getVolume(): Float
  def setVolume(newVolume: Float)
  def pause()
  def play()
  def setLooping(loop: Boolean)
  def stop()
  def dispose()

  val volumeTweener = new FloatTweener(getVolume, setVolume _)

  def update(delta: Float) = {
    volumeTweener.update(delta)
  }
}

class MidiMusicPlayer(music: Music) extends MusicPlayer with LazyLogging {
  val (sequencer: Sequencer, synthesizer: Synthesizer) = try {
    val sequencer = MidiSystem.getSequencer(false)
    val synthesizer = MidiSystem.getSynthesizer()

    synthesizer.open()

    sequencer.getTransmitter().setReceiver(synthesizer.getReceiver())

    (sequencer, synthesizer)
  } catch {
    case e: Throwable => {
      logger.error(
          "Could not initialize MIDI sequencer: %s".format(e.getMessage()))
      null
    }
  }

  private var _volume = 0.0f

  val sequence: Sequence = try {
    val s = MidiSystem.getSequence(music.newDataStream)
    if (sequencer != null) {
      sequencer.open()
      setVolume(0.0f)
      sequencer.setSequence(s)
    }
    s
  } catch {
    case _: Throwable => {
      logger.error("Could not initialize MIDI sequence")
      null
    }
  }

  def getVolume() = {
    _volume
  }

  def setVolume(newVolume: Float) = {
    _volume = newVolume
    val midiVolume = (newVolume * 127.0).toInt

    for (channel <- synthesizer.getChannels()) {
      channel.controlChange(7, midiVolume)
    }
  }

  def pause() = {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop()
    } else {
      logger.warn("MIDI sequencer null or not open")
    }
  }

  def play() = {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.start()
    } else {
      logger.warn("MIDI sequencer null or not open")
    }
  }

  def setLooping(loop: Boolean) = {
    if (sequencer != null && sequencer.isOpen()) {
      val count = if (loop) Sequencer.LOOP_CONTINUOUSLY else 0
      sequencer.setLoopCount(count)
    } else {
      logger.warn("MIDI sequencer null or not open")
    }
  }

  def stop() = {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop()
      sequencer.setTickPosition(0)
    } else {
      logger.warn("MIDI sequencer null or not open")
    }
  }

  def dispose() = {
    stop()
    if (sequencer != null)
      sequencer.close()
  }
}

/**
 * Because this can either be a MIDI or a normal libgdx music, there is a
 * special interface. No calls will work until the asset is finished loading.
 */
class GdxMusicPlayer(assets: RpgAssetManager, music: Music)
  extends MusicPlayer {

  // TODO: Converted to use asset manager. Now I'm concerned that there cannot
  // be independent instances of the same piece of music. Wouldn't pausing one
  // instance of the music pause all other playing instances of the same file?

  music.loadAsset(assets)

  private var _pendingPlay = false

  override def update(delta: Float) = {
    super.update(delta)

    if (_pendingPlay && music.isLoaded(assets))
      music.getAsset(assets).play()
  }

  def getVolume() = {
    if (music.isLoaded(assets))
      music.getAsset(assets).getVolume()
    else
      0f
  }

  def setVolume(newVolume: Float) = {
    if (music.isLoaded(assets))
      music.getAsset(assets).setVolume(newVolume)
  }

  def setLooping(loop: Boolean) = {
    if (music.isLoaded(assets))
      music.getAsset(assets).setLooping(loop)
  }

  def stop() = {
    if (music.isLoaded(assets))
      music.getAsset(assets).stop()
  }

  def pause() = {
    if (music.isLoaded(assets))
      music.getAsset(assets).pause()
  }

  def play() = {
    if (music.isLoaded(assets))
      music.getAsset(assets).play()
    else
      _pendingPlay = true
  }

  def dispose() = {
    music.dispose(assets)
  }
}