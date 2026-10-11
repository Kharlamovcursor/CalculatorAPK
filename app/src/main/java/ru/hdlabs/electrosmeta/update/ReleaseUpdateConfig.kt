package ru.hdlabs.electrosmeta.update

import ru.hdlabs.electrosmeta.BuildConfig

object ReleaseUpdateConfig {
    val githubOwner: String = BuildConfig.GITHUB_RELEASES_OWNER.trim()
    val githubRepo: String = BuildConfig.GITHUB_RELEASES_REPO.trim()

    fun isConfigured(): Boolean = githubOwner.isNotEmpty() && githubRepo.isNotEmpty()
}
