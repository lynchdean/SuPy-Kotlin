import Main.logger
import org.jsoup.Jsoup

import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import java.util.concurrent.TimeUnit

class WebPageMonitor @Throws(IOException::class, NoSuchAlgorithmException::class)
constructor(private val url: String) {

    private val referenceDigest: ByteArray

    init {
        this.referenceDigest = setReference(url)
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun setReference(url: String): ByteArray {
        val connection = Jsoup.connect(url)
        val htmlBytes = connection.get().html().toByteArray(charset("UTF-8"))
        val md = MessageDigest.getInstance("MD5")
        return md.digest(htmlBytes)
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun hasChanged(): Boolean {
        val connection = Jsoup.connect(this.url)
        val htmlBytes = connection.get().html().toByteArray(charset("UTF-8"))
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(htmlBytes)
        return !Arrays.equals(digest, this.referenceDigest)
    }

    fun waitForChange(interval: Double): Boolean {
        logger?.info("Waiting for changes on $url")
        var hasChanged = false
        while (!hasChanged) {
            hasChanged = hasChanged()
            if (hasChanged) {
                logger?.info("Page updated")
                return true
            } else {
                logger?.info("No change detected, waiting for $interval second(s)")
            }
            TimeUnit.SECONDS.sleep(interval.toLong())
        }
        return false
    }
}