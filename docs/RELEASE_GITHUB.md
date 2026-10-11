# Релиз APK в Android Studio и публикация на GitHub

Краткая шпаргалка: собрать **подписанный release**, выложить на **GitHub Releases**, проверить **обновление в приложении**.

Репозиторий кода: `Kharlamovcursor/CalculatorAPK` (в APK уже зашит для проверки обновлений).

### Ваш сценарий (APK делаете вы в Studio)

1. **Пункт 1** — версия уже **1.0.4 (5)** в проекте, можно не трогать.  
2. **Пункт 2** — вы: **Generate Signed APK → release**.  
3. Напишите ассистенту: «APK готов» — он выложит **GitHub Release** с тегом **`v5`** и подскажет проверку обновления.

---

## 1. Номер версии (уже настроено в проекте)

**Вам ничего менять не нужно**, если собираете релиз **1.0.4** — в проекте уже стоит:

| Строка в `app/build.gradle.kts` | Значение | Зачем |
|---------------------------------|----------|--------|
| `versionCode = 5` | **5** | Скрытый номер для обновлений; на GitHub тег будет **`v5`** |
| `versionName = "1.0.4"` | **1.0.4** | То, что видно в приложении: «Версия 1.0.4» |

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

### Где лежит файл

```
D:\Projects\CalculatorAPK\app\build\outputs\apk\release\app-release.apk
```

Проверьте **дату изменения** файла — она должна быть сегодняшней.

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

На репозитории ещё нет **опубликованного** Release с файлом `.apk`.

---

## 5. Чеклист перед каждым релизом

- [ ] Увеличены `versionCode` и `versionName` в `app/build.gradle.kts`
- [ ] **Generate Signed APK → release** (не Build APK(s))
- [ ] Тег на GitHub: **`v` + versionCode** (например `v6`)
- [ ] В Release прикреплён `.apk`
- [ ] Тот же `release.keystore`, что и в прошлый раз

Подробнее про ключ: [SIGNING.md](SIGNING.md).  
Общая сборка: [BUILD_APK.md](BUILD_APK.md).
