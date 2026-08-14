# ProgTV — Android TV / AOSP app

Cliente IPTV para **Android TV e AOSP** (mínimo **Android 8.0 / API 26**), navegação por **controle remoto (D-pad)**,
tema **azul/preto/glass**. Consome o endpoint público `/channels` do backend ProgTV.

## Recursos
- Splash animada "ProgTV" (2s).
- Navegação **Favoritos (fixo) → Categorias → Canais** com back inteligente (CANAIS → CATEGORIA → FECHA MENU).
- **Hold-OK** (segurar OK no controle) **alterna** favorito (marca/desmarca estrela) — persistido localmente (DataStore).
- Player **Media3/ExoPlayer** com **buffering em KB/s**: spinner central no carregamento; badge no canto superior
  direito quando trava no meio.
- Card de canal: logo, nome e **programa atual (EPG)** (ou "Ao vivo").
- **Configurações**: troca a URL base da API em runtime (útil para testar contra o backend local).
- Tela de **erro detalhado** quando o backend está fora do ar.

## Arquitetura
Clean Architecture + MVVM + UDF. Camadas `domain` / `data` / `ui` / `di` (Hilt). Retrofit + kotlinx-serialization,
Media3, Coil, DataStore, Coroutines/Flow.

## Build & run (Windows)

Pré-requisitos: Android SDK + um JDK 17 (o **JBR** do Android Studio serve).

```bat
:: aponte o Gradle para o JDK do Android Studio
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
gradlew.bat :app:assembleDebug
```

O APK sai em `app\build\outputs\apk\debug\app-debug.apk`.

Instalar/rodar em um emulador/dispositivo (Android TV 8):

```bat
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell monkey -p dev.jvfl.progtv -c android.intent.category.LEANBACK_LAUNCHER 1
```

### URL base
- Debug aponta para `http://10.0.2.2:3000` (loopback do host no emulador → backend local).
- Release aponta para `https://iptv.jvfl.dev`.
- Ajustável em runtime na tela **Configurações**.
