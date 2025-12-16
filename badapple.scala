import scala.sys.process._
import scala.concurrent.duration._
import java.io.ByteArrayInputStream

object BadApple {
  val ASCII_CHARS = " .:-=+*#%@"
  val WIDTH = 80
  val HEIGHT = 40

  def downloadVideo(url: String): Unit = {
    println("Downloading video...")
    s"yt-dlp -f worst -o badapple.mp4 $url".!
  }

  def rgbToAscii(r: Int, g: Int, b: Int): Char = {
    val brightness = (r + g + b) / 3
    val index = (brightness * (ASCII_CHARS.length - 1) / 255).toInt
    ASCII_CHARS(index)
  }

  def extractAndDisplayFrame(time: Double): Unit = {
    val cmd = s"ffmpeg -ss $time -i badapple.mp4 -vframes 1 -vf scale=$WIDTH:$HEIGHT -f rawvideo -pix_fmt rgb24 - 2>/dev/null"
    
    try {
      val pixels = cmd.!!.getBytes
      
      if (pixels.nonEmpty) {
        print("\u001b[2J\u001b[H")
        
        for (y <- 0 until HEIGHT) {
          for (x <- 0 until WIDTH) {
            val idx = (y * WIDTH + x) * 3
            if (idx + 2 < pixels.length) {
              val r = pixels(idx) & 0xFF
              val g = pixels(idx + 1) & 0xFF
              val b = pixels(idx + 2) & 0xFF
              print(rgbToAscii(r, g, b))
            }
          }
          println()
        }
        System.out.flush()
      }
    } catch {
      case _: Exception => // ignore
    }
  }

  def main(args: Array[String]): Unit = {
    val url = if (args.nonEmpty) args(0) else "https://youtu.be/FtutLA63Cp8"
    downloadVideo(url)
    
    val fps = 10.0
    val duration = 30.0
    var time = 0.0
    
    while (time < duration) {
      extractAndDisplayFrame(time)
      Thread.sleep((1000 / fps).toLong)
      time += 1.0 / fps
    }
  }
}
