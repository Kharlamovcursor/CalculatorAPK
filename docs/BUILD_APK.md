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

## 4. Release APK (для мастеров)

1. Создайте ключ (один раз):

```bat
keytool -genkeypair -v -keystore release.keystore -alias hdlabs -keyalg RSA -keysize 2048 -validity 10000
```

2. Скопируйте `keystore.properties.example` → `keystore.properties` и заполните пути/пароли.

3. **Build → Generate Signed Bundle / APK → APK → release**

Файл: `app\build\outputs\apk\release\app-release.apk`

## 5. Установка на телефон

- Скопируйте APK на телефон.
- Откройте файл → разрешите установку из этого приложения (файлы / Telegram / MAX).
- При предупреждении Play Protect: «Всё равно установить» (для своего APK это нормально).

## 6. GitHub Releases (обновления)

1. Создайте репозиторий, например `hdlabs-electrosmeta-releases`.
2. **Releases → Create new release**, тег `v1.0.1`, приложите `app-release.apk`.
3. В приложении: **Настройки → О приложении** — укажите owner и имя репозитория.
4. **Проверить обновления** → скачать → установить.

Важно: все релизы подписывайте **тем же keystore**, иначе обновление не установится поверх старой версии.

## 7. Проверка на эмуляторе

**Device Manager → Create Device** → запуск → зелёная кнопка **Run** (▶) в Android Studio.
