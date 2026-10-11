package ru.hdlabs.electrosmeta.update

import ru.hdlabs.electrosmeta.BuildConfig

object ReleaseUpdateConfig {
    private const val DEFAULT_OWNER = "Kharlamovcursor"
    private const val DEFAULT_REPO = "CalculatorAPK"

    val githubOwner: String =
        BuildConfig.GITHUB_RELEASES_OWNER.trim().ifEmpty { DEFAULT_OWNER }
    val githubRepo: String =
        BuildConfig.GITHUB_RELEASES_REPO.trim().ifEmpty { DEFAULT_REPO }

    fun isConfigured(): Boolean = githubOwner.isNotEmpty() && githubRepo.isNotEmpty()
}
