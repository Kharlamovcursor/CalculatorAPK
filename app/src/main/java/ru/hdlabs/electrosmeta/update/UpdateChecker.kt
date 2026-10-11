package ru.hdlabs.electrosmeta.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.hdlabs.electrosmeta.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.io.File

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
)

data class UpdateInfo(
    val versionName: String,
    val changelog: String,
    val apkUrl: String,
    val apkFileName: String,
)

interface UpdateSource {
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo?
}

class GitHubUpdateSource(
    private val owner: String,
    private val repo: String,
) : UpdateSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? {
        if (owner.isBlank() || repo.isBlank()) return null
        val url = URL("https://api.github.com/repos/$owner/$repo/releases/latest")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        connection.inputStream.use { stream ->
            val text = stream.bufferedReader().readText()
            val release = json.decodeFromString<GitHubRelease>(text)
            val apk = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                ?: return null
            val remoteCode = parseVersionCode(release.tagName)
            if (remoteCode <= currentVersionCode) return null
            return UpdateInfo(
                versionName = release.tagName,
                changelog = release.body ?: "",
                apkUrl = apk.downloadUrl,
                apkFileName = apk.name,
            )
        }
    }

    private fun parseVersionCode(tag: String): Int {
        val digits = tag.filter { it.isDigit() || it == '.' }
        val parts = digits.split('.').filter { it.isNotEmpty() }
        if (parts.isEmpty()) return BuildConfig.VERSION_CODE
        return parts.take(3).fold(0) { acc, part ->
            acc * 1000 + part.toIntOrNull()?.coerceIn(0, 999) ?: 0
        }
    }
}

object ApkDownloadHelper {
    fun download(url: String, target: File) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 120_000
        }
        connection.inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
