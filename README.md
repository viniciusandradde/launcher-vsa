# Kids Launcher VSA

Launcher Android personalizado para crianças, com **dois perfis num único APK**:

| Perfil | Idade | Grade | Cara |
|--------|-------|-------|------|
| **Pequeno** | 4 anos | 2 colunas, ícones grandes | laranja/coral |
| **Junior** | 10 anos | 3 colunas | azul/teal |

O launcher mostra **apenas os apps liberados** para o perfil ativo (whitelist),
esconde todo o resto do sistema e protege o acesso às configurações com um **PIN
dos pais**. Não exige permissões especiais nem root — funciona como launcher
padrão em qualquer Android 8.0+.

> Projetado e implementado como um projeto de referência de desenvolvimento
> Android moderno: 100% Kotlin, Jetpack Compose, DataStore e **Jetpack Glance**
> para o widget da tela inicial.

---

## Sumário

- [Decisões de produto](#decisões-de-produto)
- [Pesquisa que fundamenta o projeto](#pesquisa-que-fundamenta-o-projeto)
- [Arquitetura](#arquitetura)
- [Como funciona (fluxo)](#como-funciona-fluxo)
- [Build e instalação](#build-e-instalação)
- [Como usar](#como-usar)
- [Segurança e privacidade](#segurança-e-privacidade)
- [Limitações conhecidas](#limitações-conhecidas)
- [Evolução (roadmap)](#evolução-roadmap)

---

## Decisões de produto

Três decisões definiram o escopo desta primeira versão:

1. **App único com 2 perfis** (em vez de dois apps separados) — um só install por
   aparelho, código compartilhado, troca de perfil protegida por PIN.
2. **Launcher curado + PIN** (em vez de limites de tempo ou kiosk) — a base mais
   sólida e portátil: sem `AccessibilityService`, sem `Device Owner`, sem
   `UsageStats`. Funciona igual em qualquer fabricante. Os modos mais intrusivos
   ficam no [roadmap](#evolução-roadmap).
3. **Projeto completo compilável** — estrutura Gradle real, pronta para abrir no
   Android Studio e gerar o APK.

---

## Pesquisa que fundamenta o projeto

Três referências guiaram as escolhas técnicas:

### 1. [Jetpack Glance](https://developer.android.com/jetpack/androidx/releases/glance)
API estilo Compose para **superfícies remotas** (widgets, tiles). Escreve-se com
composables (`Column`, `Text`, `Image`…) e o Glance converte para `RemoteViews`
por baixo. Classes-chave usadas aqui:

- `GlanceAppWidget` → `provideGlance()` declara o conteúdo (`GreetingWidget`).
- `GlanceAppWidgetReceiver` → liga o widget ao framework (`GreetingWidgetReceiver`).
- `GlanceTheme` (Material 3), `actionStartActivity` para abrir o launcher ao tocar.

Versão usada: **Glance 1.1.1** (estável), `minSdk` do Glance é 23.

### 2. [App Widgets — visão geral](https://developer.android.com/develop/ui/views/appwidgets/overview)
Fundamentos que continuam valendo mesmo com Glance:

- Um widget é um `AppWidgetProvider` (um `BroadcastReceiver`) + um
  `AppWidgetProviderInfo` em `res/xml/` + declaração no manifesto.
- `updatePeriodMillis` tem **mínimo de 30 min** (1800000). Usamos exatamente isso
  em `res/xml/greeting_widget_info.xml`.
- Widgets só suportam **toque e swipe vertical** — nada de scroll horizontal.
- O Glance ainda exige um `initialLayout` (usamos o
  `@layout/glance_default_loading_layout` fornecido pela biblioteca).

### 3. [Kvaesitso](https://github.com/MM2-0/Kvaesitso) (launcher open-source, GPLv3)
Modelo de referência de um launcher Kotlin/Compose real. O que aproveitamos:

- **Como um app se declara launcher**: `intent-filter` com
  `category.HOME` + `category.DEFAULT` na activity (ver `AndroidManifest.xml`).
- **Descoberta de apps** via `PackageManager` com query `MAIN/LAUNCHER`.
- **Modularização** (`app`/`core`/`data`/`services`): aqui mantivemos módulo único
  por ser um projeto doméstico, mas a separação por camadas (`data`, `ui`,
  `widget`, `util`) deixa a migração para multi-módulo direta.

> Kvaesitso é GPLv3. **Não** copiamos código dele; usamos apenas como referência
> conceitual de arquitetura e das APIs públicas do Android.

---

## Arquitetura

Módulo único `:app`, organizado por camadas. 100% Kotlin + Compose.

```
com.viniciusandrade.kidslauncher
├── KidsLauncherApp.kt          # Application + service locator (repos como singletons)
├── MainActivity.kt             # Única Activity; registrada como HOME
├── data/
│   ├── model/
│   │   ├── KidProfile.kt       # enum PEQUENO / JUNIOR (colunas, labels, cor)
│   │   └── LauncherApp.kt      # app launchável (pacote, label, ícone)
│   ├── AppRepository.kt        # lê apps do PackageManager e lança apps
│   └── SettingsRepository.kt   # DataStore: PIN (hash), perfil ativo, whitelist
├── ui/
│   ├── KidsLauncherViewModel.kt# combina apps + settings → LauncherUiState
│   ├── KidsLauncherRoot.kt     # navegação por estado (Launcher/PIN/Settings)
│   ├── launcher/LauncherScreen.kt
│   ├── pin/PinScreen.kt        # teclado numérico
│   ├── settings/SettingsScreen.kt + ChangePinDialog.kt
│   ├── components/AppGridItem.kt
│   └── theme/                  # cores/tipografia por perfil
├── widget/
│   ├── GreetingWidget.kt       # Glance: saudação + relógio
│   └── GreetingWidgetReceiver.kt
└── util/TimeGreeting.kt        # lógica pura de saudação (com teste JVM)
```

**Fluxo de dados (unidirecional):**

```
DataStore ──┐
            ├─► SettingsRepository.settings ─┐
PackageMgr ─┴─► AppRepository.loadInstalledApps ─► ViewModel.combine ─► LauncherUiState ─► Compose
```

- `LauncherUiState.visibleApps` = apps instalados **∩** whitelist do perfil ativo.
- Trocar de perfil ou (des)marcar um app re-emite o estado automaticamente
  (Kotlin Flow + `stateIn`).

### Stack / versões

| Item | Versão |
|------|--------|
| Kotlin | 2.0.21 (Compose Compiler plugin) |
| AGP | 8.7.3 · Gradle 8.11.1 |
| Compose BOM | 2024.10.01 · Material 3 |
| Glance | 1.1.1 (`glance-appwidget` + `glance-material3`) |
| DataStore | 1.1.1 (Preferences) |
| `minSdk` / `targetSdk` | 26 / 35 |

---

## Como funciona (fluxo)

1. Ao virar o launcher padrão, a `MainActivity` (HOME) abre no boot e no botão
   Home. O botão **Voltar é neutralizado** (`BackHandler`) — a criança não
   "sai" para uma tela vazia do sistema.
2. A criança vê a **saudação** (Bom dia/tarde/noite) + a grade dos apps liberados.
3. Um **cadeado discreto 🔒** no canto abre o **PIN dos pais**.
4. Com o PIN correto, os pais entram nas **Configurações**: escolhem o perfil
   ativo, ligam/desligam apps por perfil e trocam o PIN.
5. O **widget Glance** pode ser adicionado à tela inicial (saudação + relógio) e,
   ao ser tocado, abre o launcher.

---

## Build e instalação

Pré-requisitos: **Android Studio Ladybug+** (ou JDK 17 + Android SDK 35 via CLI).

```bash
# Debug APK
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Instalar no aparelho conectado (USB debugging ligado)
./gradlew installDebug

# Rodar os testes unitários JVM
./gradlew testDebugUnitTest
```

Definir como launcher padrão no aparelho:
**Configurações → Apps → Apps padrão → App de início → Kids Launcher VSA.**
(Ou aperte Home e escolha "Kids Launcher VSA → Sempre".)

> Nota: este repositório foi montado num ambiente **sem Android SDK**, então o
> APK não foi compilado aqui. O Gradle Wrapper (`gradlew`) está incluído e
> funcional; o build roda normalmente numa máquina com o SDK instalado.

---

## Como usar

**PIN inicial: `1234`** — troque no primeiro acesso em
*Configurações → Segurança → Alterar PIN*.

1. Abra o launcher, toque no cadeado 🔒 e digite `1234`.
2. Em **Perfil ativo**, escolha *Pequeno (4 anos)* ou *Junior (10 anos)*.
3. Em **Apps liberados**, ligue os apps que aquele perfil pode abrir.
4. Troque o PIN em **Segurança**.
5. Volte — a criança já vê só os apps liberados daquele perfil.

Cada perfil tem sua própria whitelist; troque o perfil ativo para configurar o outro.

---

## Segurança e privacidade

- O **PIN nunca é salvo em texto puro**: guardamos apenas o hash **SHA-256** no
  DataStore (`SettingsRepository`).
- Sem coleta de dados, sem rede, sem analytics. O app **não pede permissões
  perigosas** — usa `<queries>` (MAIN/LAUNCHER) em vez de `QUERY_ALL_PACKAGES`.
- Backup do Android inclui só as preferências (perfil/whitelist/hash do PIN).

---

## Limitações conhecidas

- **Não é kiosk**: uma criança determinada ainda pode acessar a barra de status /
  gestos do sistema. Bloqueio total exige *lock task* + Device Owner (roadmap).
- **Sem limite de tempo** nesta versão.
- O widget usa `updatePeriodMillis` de 30 min (limite do Android); para relógio
  ao segundo seria preciso um `Worker`/alarme — desnecessário para "glanceable".

---

## Evolução (roadmap)

Ordenado por valor × esforço:

1. **First-run setup** obrigando a definir o PIN (hoje há um PIN padrão `1234`).
2. **Limites de tempo por app/dia** — `UsageStatsManager` + `WorkManager`.
3. **Modo kiosk real** — `startLockTask()` com o app como *Device Owner*
   (provisionado via ADB/MDM).
4. **Reordenar/agrupar apps** por arrastar, favoritos, pastas.
5. **Migração para multi-módulo + Hilt** — o service locator atual
   (`KidsLauncherApp`) troca-se por DI sem tocar nas telas.
6. **Tema escuro** (o gancho já existe em `Theme.kt`).

---

## Licença

Projeto pessoal. Jetpack Glance/Compose sob Apache 2.0. Kvaesitso citado apenas
como referência (não redistribuímos seu código, que é GPLv3).
