# Первый release APK (пошагово в Android Studio)

Проект: `D:\Projects\CalculatorAPK`  
Ключ уже создан: `release.keystore`, `keystore.properties`.

## Шаг 0. JVM 21 (важно)

Если видите ошибку про **Gradle и JVM 25**:

1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
2. **Gradle JDK** → **jbr-21** или **Temurin 21** (не 25)
3. **Apply → OK** → дождитесь Sync

## Шаг 1. Открыть проект

**File → Open** → `D:\Projects\CalculatorAPK` → дождаться **Gradle Sync** без ошибок.

## Шаг 2. Собрать release APK

Перед сборкой в `app/build.gradle.kts` увеличьте `versionCode` и `versionName`, нажмите **Sync**.

**Не нажимайте** **Build → Build APK(s)** — это debug, не для мастеров.

### В Android Studio (рекомендуется)

1. **Build → Generate Signed App Bundle or APK**
2. **APK** → Next
3. **Choose existing…**
   - Key store path: `D:\Projects\CalculatorAPK\release.keystore`
   - Password / alias `hdlabs` — из `signing-credentials.local.txt`
4. **release** → Finish → в уведомлении **locate** откройте папку с APK

### Альтернатива: Gradle в терминале Studio

```bat
gradlew assembleRelease
```

(нужны `keystore.properties` и JDK 21)

## Шаг 3. Где файл

```
D:\Projects\CalculatorAPK\app\build\outputs\apk\release\app-release.apk
```

Скопируйте на рабочий стол как:

`HDLabs-Electrosmeta-1.0.0.apk`

## Шаг 4. Установка на телефон

Отправьте себе в Telegram → открыть → Установить → разрешить установку из Telegram.

## Шаг 5. Обновления (GitHub Releases)

Пошагово с тегами и проверкой в приложении: **[RELEASE_GITHUB.md](RELEASE_GITHUB.md)**.

Кратко:

1. GitHub → **CalculatorAPK → Releases → New release**
2. Тег: **`v5`** (число = `versionCode` в APK, не `versionName`)
3. Прикрепите `app-release.apk`
4. **Настройки → О приложении → Проверить обновления**

При каждом релизе: больший `versionCode`, тот же `release.keystore`.
