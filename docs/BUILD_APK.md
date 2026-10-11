# Сборка первого APK (пошагово)

## 1. Установите Android Studio

Скачайте с [developer.android.com/studio](https://developer.android.com/studio) и установите с компонентом **Android SDK**.

## 2. Откройте проект

1. **File → Open** → папка `D:\Projects\CalculatorAPK`
2. Дождитесь **Gradle Sync** (внизу «BUILD SUCCESSFUL»).

Если SDK не найден: **File → Settings → Android SDK** — установите **Android 15 (API 35)** и **Build-Tools**.

## 3. Debug APK (для себя)

1. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Готовый файл:
   `app\build\outputs\apk\debug\app-debug.apk`

Переименуйте при отправке, например: `HDLabs-Electrosmeta-1.0.0-debug.apk`.

## 4. Release APK (для мастеров) — только через Android Studio

**Не используйте** **Build → Build APK(s)** для раздачи мастерам: это **debug** (~60 МБ, другая подпись).

### Один раз: ключ

```bat
keytool -genkeypair -v -keystore release.keystore -alias hdlabs -keyalg RSA -keysize 2048 -validity 10000
```

Скопируйте `keystore.properties.example` → `keystore.properties` и заполните пути/пароли.

### Каждый релиз в Studio

1. В `app/build.gradle.kts` увеличьте `versionCode` и `versionName` → **Sync**.
2. **Build → Generate Signed App Bundle or APK** → **APK**.
3. Keystore: `release.keystore`, alias `hdlabs` (пароли локально).
4. Вариант сборки: **release** → Finish.
5. Файл после Studio: **`app\release\app-release.apk`** (не путать с `app\build\outputs\apk\release\` — там может лежать старая сборка из Gradle).

Если APK «старый»: **Build → Clean Project**, затем снова Signed APK.

**Полная инструкция (GitHub Releases + проверка обновления):** [RELEASE_GITHUB.md](RELEASE_GITHUB.md).

## 5. Установка на телефон

- Скопируйте APK на телефон.
- Откройте файл → разрешите установку из этого приложения (файлы / Telegram / MAX).
- При предупреждении Play Protect: «Всё равно установить» (для своего APK это нормально).

## 6. GitHub Releases (обновления)

Репозиторий в приложении уже задан: `Kharlamovcursor/CalculatorAPK`.

1. Соберите **signed release** APK (раздел 4).
2. На GitHub: **Releases → New release**, тег **`v{N}`**, где **N = `versionCode`** из `build.gradle.kts` (например `v5`).
3. Прикрепите `app-release.apk`.
4. В приложении: **Настройки → О приложении → Проверить обновления**.

Подробно: [RELEASE_GITHUB.md](RELEASE_GITHUB.md).

Все релизы подписывайте **тем же** `release.keystore`, иначе обновление не установится поверх старой версии.

## 7. Проверка на эмуляторе

**Device Manager → Create Device** → запуск → зелёная кнопка **Run** (▶) в Android Studio.

## Ускорение сборки

При **16 ГБ** RAM: Gradle **~3 ГБ**, Kotlin daemon **~1 ГБ**, **`workers.max=2`**. Не запускайте **gradlew** в терминале, пока Studio собирает проект. Освободить память: `gradlew --stop`.

- Для ежедневной работы удобнее **Android Studio** (Build / Run) — Gradle daemon остаётся в памяти и повторные сборки быстрее.
- В командной строке не добавляйте **`--no-daemon`** без необходимости: каждый запуск с ним холодный и медленнее.
- Для CLI задайте **`JAVA_HOME`** на JDK 21 (как в `gradle.properties`, `org.gradle.java.home`), чтобы не тратить время на поиск JDK.
- Быстрая проверка компиляции без полного APK: `gradlew compileReleaseKotlin` (или `compileDebugKotlin`).
- **Первая** сборка после клонирования или обновления зависимостей долгая — Gradle и Android SDK скачивают артефакты; следующие сборки обычно заметно быстрее.
