# Segurança e Privacidade — Kids Launcher VSA

## Dados existentes
O código atual persiste preferências parentais, hash do PIN, whitelist de pacotes, limites/contagem de tempo, nomes personalizados e lista de vídeos por perfil.

## Controles e riscos
- PIN: SHA-256 não é um KDF adequado para segredos de baixa entropia como PIN de quatro dígitos. Planejar migração para PBKDF2/scrypt/Argon2 com salt e parâmetros versionados, sem bloquear dados legados durante atualização.
- PIN padrão: o valor inicial conhecido 1234 é risco de configuração. Implementar onboarding obrigatório, sem assumir que o hash por si só protege o primeiro uso.
- Backup: revisar se preferências de PIN e configurações devem ser incluídas no backup Android. A decisão precisa ser documentada e validada com recuperação.
- YouTube/WebView: validar URLs por allowlist de hosts, evitar conteúdo arbitrário e não expor interfaces JavaScript nativas à página.
- Miniaturas externas: carregam metadados de rede para serviços de terceiros; explicar isso ao responsável e permitir remover vídeos.
- Whitelist: Package visibility deve continuar limitada a atividades MAIN/LAUNCHER; não usar QUERY_ALL_PACKAGES sem justificativa.
- Launcher: não declarar proteção kiosk ou bloqueio total do Android.

## Privacidade infantil
- Minimizar dados e não coletar analytics por padrão.
- Não solicitar localização, contatos, microfone ou câmera sem finalidade essencial e avaliação separada.
- Apresentar informações ao responsável em linguagem simples.
- Definir retenção, exportação e exclusão de dados locais.
- Avaliar base legal e melhor interesse no caso concreto antes de distribuição pública.

## Referência normativa
O tratamento de dados de crianças e adolescentes deve observar e fazer prevalecer seu melhor interesse, conforme art. 14 da LGPD e Enunciado CD/ANPD nº 1/2023. Isso não substitui avaliação jurídica específica.

## Checklist pré-release
- [ ] PIN inicial substituído por onboarding.
- [ ] Migração de hash implementada e testada.
- [ ] URLs de vídeo validadas.
- [ ] Política de backup revisada.
- [ ] Aviso de privacidade para responsáveis.
- [ ] Testes de navegação e autenticação.
- [ ] Revisão de permissões e dependências.
