# Ключ подписи (обновления APK)

## Файлы (только на вашем ПК, не в Git)

| Файл | Назначение |
|------|------------|
| `release.keystore` | Ключ подписи |
| `keystore.properties` | Пути и пароли для Gradle |
| `signing-credentials.local.txt` | Копия паролей для резервного хранения |

## Сборка release в Android Studio

1. Убедитесь, что есть `keystore.properties` и `release.keystore` в корне проекта.
2. **Build → Generate Signed App Bundle or APK → APK → release**  
   или в терминале: `gradlew assembleRelease`
3. APK: `app/build/outputs/apk/release/app-release.apk`

Все обновления подписывайте **этим же** `release.keystore` и alias `hdlabs`.

## Резервная копия (обязательно)

Скопируйте на флешку / в облако (зашифровано):

- `release.keystore`
- пароли из `signing-credentials.local.txt`

## GitHub Actions (позже)

В Secrets репозитория:

- `KEYSTORE_BASE64` — содержимое `release.keystore` в base64
- `KEYSTORE_PASSWORD`, `KEY_ALIAS` (`hdlabs`), `KEY_PASSWORD`

Не загружайте keystore в публичный репозиторий.
