# Documento de Produto — Kids Launcher VSA

## Visão
Launcher Android familiar que oferece uma experiência inicial simples e controlada para crianças, com curadoria parental de aplicativos, perfis por faixa etária e controles de tempo.

## Usuários
- Responsável: configura PIN, perfis, apps permitidos, vídeos e limite diário.
- Criança: acessa somente os itens liberados no perfil ativo.

## Funcionalidades identificadas no código
- Perfis Pequeno e Junior.
- Lista de aplicativos instalados e whitelist por perfil.
- PIN parental e alteração do PIN.
- Personalização de nome.
- Vídeos selecionados pelo responsável e reprodução via componente de player.
- Limite de tempo diário, tela de tempo encerrado e concessão de tempo adicional.
- Widget de saudação.
- Tema e componentes lúdicos.

## Requisitos de produto
1. Configuração inicial deve conduzir o responsável a definir um PIN próprio.
2. Perfil infantil não deve acessar configurações sem autenticação parental.
3. Lista de apps deve refletir a whitelist do perfil ativo.
4. Limite de tempo deve ser transparente, previsível e reiniciado no dia local correto.
5. Responsável deve conseguir remover apps e vídeos autorizados.
6. Erros de abertura de app/vídeo devem oferecer feedback e não encerrar o launcher.
7. A experiência deve permanecer utilizável com rede indisponível, exceto conteúdo de vídeo online.

## Fora do escopo atual
- Kiosk inviolável em qualquer dispositivo.
- Bloqueio de uso dentro de apps de terceiros sem APIs/permissões adicionais.
- MDM, gestão remota ou sincronização em nuvem.

## Critérios de aceite
- PIN nunca persistido em texto puro.
- Alteração de perfil não mistura whitelists.
- Limite de tempo é aplicado de forma consistente ao retornar de apps.
- Não há coleta oculta de dados da criança.
- Configurações parentais são protegidas por fluxo de autenticação validado.
