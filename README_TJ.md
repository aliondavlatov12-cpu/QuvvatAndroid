# Қувват — лоиҳаи Android барои GitHub Actions

## Сохтани APK аз телефон
1. ZIP-ро кушоед ва ҳамаи файлҳои дохили папкаи `QuvvatAndroid`-ро ба решаи repository-и GitHub гузоред. **Худи ZIP-ро танҳо ҳамчун файл ба repository нагузоред**.
2. Номи repository-ро масалан `QuvvatAndroid` монед ва branch-и асосиро `main` интихоб кунед.
3. Боварӣ ҳосил кунед, ки `.github/workflows/build-apk.yml` ҳам ба repository бор шудааст.
4. Дар repository бахши **Actions**-ро кушоед.
5. Workflow-и **Build Quvvat APK**-ро интихоб кунед → **Run workflow** → **Run workflow**.
6. Вақте ки run сабз ва `Success` мешавад, онро кушоед. Дар поён қисми **Artifacts** → `Quvvat-debug-APK`-ро зеркашӣ кунед.
7. ZIP-и artifact-ро дар телефон кушоед; дохилаш `app-debug.apk` аст. APK-ро насб карда, иҷозатҳои огоҳинома ва будильникро диҳед.

## Эзоҳ
- GitHub Actions APK-и debug месозад; барои насби шахсӣ мувофиқ аст.
- Ин лоиҳа коди манбаъӣ аст. То он даме ки GitHub Actions бо муваффақият анҷом наёбад, APK-и сохташуда вуҷуд надорад.
- Агар workflow хатогӣ диҳад, дар Actions run-ро кушоед ва қисми error-и сурхро фиристед.
