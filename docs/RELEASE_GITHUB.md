# Релиз APK в Android Studio и публикация на GitHub

Краткая шпаргалка: собрать **подписанный release**, выложить на **GitHub Releases**, проверить **обновление в приложении**.

Репозиторий кода: `Kharlamovcursor/CalculatorAPK` (в APK уже зашит для проверки обновлений).

### Ваш сценарий (APK делаете вы в Studio)

1. Проверьте `versionCode` / `versionName` в `build.gradle.kts` → **Sync** → **Generate Signed APK**.  
2. Напишите «APK готов» — публикация на GitHub **без повторной сборки** (`gh release create`, ~1 мин).

### Быстрая публикация (не гонять Gradle снова)

APK уже в `app\release\app-release.apk`. Публикация **не должна** вызывать `gradlew assembleRelease` — это долго и жрёт RAM.

```powershell
cd D:\Projects\CalculatorAPK
.\gradlew.bat --stop
gh release create v6 --repo Kharlamovcursor/CalculatorAPK --title "1.0.5" --notes "UI основного расчёта, анимации кнопок" "app\release\app-release.apk#HDLabs-Electrosmeta-1.0.5.apk"
```

Тег **`vN`** = **`versionCode`** в собранном APK.

---

## 1. Номер версии (уже настроено в проекте)

Сейчас в проекте (перед сборкой проверьте в файле):

| Строка в `app/build.gradle.kts` | Пример | Зачем |
|---------------------------------|--------|--------|
| `versionCode` | **6** | Тег на GitHub: **`v6`** |
| `versionName` | **1.0.5** | «Версия …» в приложении |

**Где смотреть в Android Studio:** слева **Project** → папка **app** → файл **`build.gradle.kts`** → прокрутить до `defaultConfig` (строки ~19–20).

Если на телефоне стоит старая версия (например 1.0.3 с номером 4), а в файле **5** и **1.0.4** — обновление через GitHub сработает.

Следующий релиз потом: попросите у ассистента увеличить номера (6 и 1.0.5) или сами измените две строки и нажмите **Sync** (слон).

---

## 2. Собрать release APK в Android Studio

### Не путать с debug

| Меню | Результат |
|------|-----------|
| **Build → Build APK(s)** | **debug** (~60 МБ), для быстрых тестов |
| **Build → Generate Signed App Bundle or APK** | **release** (~4–5 МБ), для мастеров и GitHub |

Для релиза нужен только **Signed → APK → release**.

### Пошагово (Signed release)

1. **File → Open** → `D:\Projects\CalculatorAPK`, дождитесь **BUILD SUCCESSFUL**.
2. **Build → Generate Signed App Bundle or APK**.
3. Выберите **APK** → **Next**.
4. **Choose existing…**
   - Key store: `D:\Projects\CalculatorAPK\release.keystore`
   - Пароли и alias `hdlabs` — из `signing-credentials.local.txt` (файл только у вас на ПК).
5. **Next** → отметьте **release** → **Create** / **Finish**.
6. Внизу появится уведомление **locate** — откройте папку.

### Где лежит файл (важно: два разных пути)

| Как собирали | Папка | Когда смотреть |
|--------------|--------|----------------|
| **Generate Signed APK** в Android Studio | `app\release\app-release.apk` | **Ваш случай** — свежий файл здесь |
| `gradlew assembleRelease` в терминале | `app\build\outputs\apk\release\app-release.apk` | Может быть **старым**, если Studio не пересобирала Gradle-папку |

Полный путь после Studio:

```
D:\Projects\CalculatorAPK\app\release\app-release.apk
```

Смотрите **дату изменения** в проводнике (ПКМ → свойства). Если в `build\outputs\...` дата старая — это **не** последняя Signed-сборка.

Если файл не обновился: **Build → Clean Project**, затем снова **Generate Signed APK**.

### JDK

**File → Settings → Build, Execution, Deployment → Build Tools → Gradle**  
**Gradle JDK** = **21** (jbr-21 или Temurin 21), не 25.

---

## 3. Выложить релиз на GitHub

APK в git **не коммитят** (папка `app/build` в `.gitignore`). На GitHub кладут через **Releases**.

### Вариант A — сайт GitHub

1. Откройте: https://github.com/Kharlamovcursor/CalculatorAPK/releases/new  
2. **Choose a tag:** создайте тег **`v6`** (число = `versionCode` из `build.gradle.kts`).  
   Примеры: `versionCode` 5 → тег `v5`, `versionCode` 6 → тег `v6`.
3. **Release title:** например `1.0.5` (как `versionName`).
4. Описание — что изменилось (по желанию).
5. Перетащите **`app-release.apk`** в блок файлов (можно переименовать в `HDLabs-Electrosmeta-1.0.5.apk`).
6. **Publish release**.

### Вариант B — командная строка (если установлен `gh`)

```powershell
cd D:\Projects\CalculatorAPK
gh release create v6 --title "1.0.5" --notes "Описание изменений" `
  "app\build\outputs\apk\release\app-release.apk#HDLabs-Electrosmeta-1.0.5.apk"
```

Тег **`v6`** должен совпадать с **`versionCode`** в APK.

### Код в git (без APK)

Отдельно можно отправить исходники:

```powershell
git add -A
git status   # не должно быть keystore, keystore.properties, паролей
git commit -m "Release 1.0.5"
git push origin main
```

---

## 4. Проверить обновление в приложении

1. На телефоне должна стоять **старая** версия (меньший `versionCode`), подписанная **тем же** `release.keystore`.
2. **Настройки → О приложении → Проверить обновления**.
3. Если на GitHub есть Release с тегом `v{N}` и APK, где `N` **больше** установленного `versionCode`, появится скачивание.
4. Разрешите установку из браузера / файлового менеджера.

### Если обновление не ставится поверх старой

Пришлось удалить старое приложение — значит **другая подпись** (debug вместо release или другой keystore). Дальше ставьте только **signed release** с одним и тем же `release.keystore`.

### Если «релиз не найден» (404)

- На GitHub ещё нет Release с `.apk`, **или**
- На телефоне **старая сборка (1.0.3 и раньше)** — в ней репозиторий **не был вшит**, поля GitHub в настройках убрали, поэтому приложение не знает адрес. **Один раз** установите APK вручную с [Releases](https://github.com/Kharlamovcursor/CalculatorAPK/releases). С **1.0.4** репозиторий уже внутри APK (`Kharlamovcursor/CalculatorAPK`).

---

## 5. Чеклист перед каждым релизом

- [ ] Увеличены `versionCode` и `versionName` в `app/build.gradle.kts`
- [ ] **Generate Signed APK → release** (не Build APK(s))
- [ ] Тег на GitHub: **`v` + versionCode** (например `v6`)
- [ ] В Release прикреплён `.apk`
- [ ] Тот же `release.keystore`, что и в прошлый раз

Подробнее про ключ: [SIGNING.md](SIGNING.md).  
Общая сборка: [BUILD_APK.md](BUILD_APK.md).
