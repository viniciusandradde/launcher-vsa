# Arquitetura — Kids Launcher VSA

## Stack
- Kotlin e Android Gradle Plugin.
- Jetpack Compose e Material 3.
- DataStore Preferences para persistência.
- Kotlin Flow e ViewModel para estado.
- Coil para miniaturas.
- Player YouTube incorporado em WebView.
- Jetpack Glance para widget.
- Testes JVM com JUnit.

## Organização atual
- data/model: KidProfile, LauncherApp e KidVideo.
- data: descoberta/abertura de apps e persistência parental.
- ui: estado de tela, navegação, launcher, PIN, configurações, player e widgets.
- util: saudação, integração YouTube e utilitários.

## Fluxo de estado
DataStore + PackageManager → repositories → KidsLauncherViewModel → LauncherUiState → Compose.

A lista infantil é a interseção entre aplicativos instalados e whitelist do perfil ativo.

## Persistência
DataStore armazena PIN hash, perfil, whitelist, limite de tempo, uso diário, nomes e vídeos por perfil. Alterações no schema devem manter compatibilidade com instalações existentes.

## Limites de plataforma
O aplicativo é um launcher Android comum, não um kiosk garantido. HOME intent filter não impede acesso a gestos, configurações rápidas ou recursos do sistema. Bloqueio forte exige provisionamento Device Owner/Lock Task e política específica.

## Decisões
- Manter módulo único enquanto o domínio e equipe forem pequenos.
- Não adicionar Hilt ou multi-módulo sem ganho mensurável.
- Manter regras puras testáveis fora dos composables.
- Tratar integração de player externo como fronteira de rede e conteúdo.
