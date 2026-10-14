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
    val name: String? = null,
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
            setRequestProperty("User-Agent", "HDLabs-Electrosmeta-Android")
        }
        val code = connection.responseCode
        val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.readText() ?: ""
        if (code !in 200..299) {
            throw IllegalStateException(
                when (code) {
                    404 -> "Релиз не найден ($owner/$repo). " +
                        "Если у вас версия до 1.0.4 — установите новый APK вручную с GitHub Releases."
                    403 -> "Доступ запрещён (лимит API или приватный репозиторий)"
                    else -> "GitHub ответил: HTTP $code"
                },
            )
        }
        val release = json.decodeFromString<GitHubRelease>(text)
        val apk = release.assets.firstOrNull { it.name.equals("app-release.apk", ignoreCase = true) }
            ?: release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            ?: throw IllegalStateException("В последнем Release нет файла .apk")
        val remoteCode = ReleaseVersionParser.parseVersionCode(release.tagName, BuildConfig.VERSION_CODE)
        if (remoteCode <= currentVersionCode) return null
        return UpdateInfo(
            versionName = ReleaseVersionParser.displayVersionName(
                release.tagName,
                remoteCode,
                release.name,
            ),
            changelog = release.body ?: "",
            apkUrl = apk.downloadUrl,
            apkFileName = apk.name,
        )
    }

}

object ApkDownloadHelper {
    private val trustedHosts = setOf("github.com", "objects.githubusercontent.com")

    fun download(url: String, target: File) {
        val parsed = URL(url)
        val host = parsed.host.lowercase()
        require(trustedHosts.contains(host) || host.endsWith(".githubusercontent.com")) {
            "Недоверенный источник обновления: $host"
        }
        require(parsed.protocol.equals("https", ignoreCase = true)) { "Обновление доступно только по HTTPS" }
        val connection = (parsed.openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 120_000
            instanceFollowRedirects = true
        }
        connection.inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        verifyApkFile(target)
    }

    fun verifyApkFile(file: File) {
        require(file.exists() && file.length() > 10_000) { "Загруженный файл повреждён или пуст" }
        file.inputStream().use { input ->
            val header = ByteArray(4)
            val read = input.read(header)
            require(read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()) {
                "Файл не является корректным APK"
            }
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
