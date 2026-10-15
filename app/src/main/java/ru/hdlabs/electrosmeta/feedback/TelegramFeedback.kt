package ru.hdlabs.electrosmeta.feedback

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import ru.hdlabs.electrosmeta.BuildConfig

object TelegramFeedback {
    /**
     * Личный чат или бот. По умолчанию `https://t.me/Yakofffff`.
     * Переопределение в `local.properties`: `telegram.feedback.url` или `telegram.feedback.bot`.
     */
    fun resolveUri(): Uri? {
        val direct = BuildConfig.TELEGRAM_FEEDBACK_URL.trim()
        if (direct.isNotEmpty()) {
            return direct.toUri()
        }
        val bot = BuildConfig.TELEGRAM_FEEDBACK_BOT.trim().removePrefix("@")
        if (bot.isEmpty()) return null
        val start = "feedback_v${BuildConfig.VERSION_CODE}"
        return "https://t.me/$bot?start=$start".toUri()
    }

    fun isConfigured(): Boolean = resolveUri() != null

    fun open(context: Context): Result<Unit> {
        val uri = resolveUri()
            ?: return Result.failure(IllegalStateException("Telegram не настроен в сборке"))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Result.success(Unit)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setPackage(null)
                    },
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
