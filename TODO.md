# Plano de projeto

### 1. Painel Principal (Dashboard)

A tela inicial serve como o centro de controle do usuário:

Visualização de Progresso: Um gráfico circular e textos claros mostram quanto o utilizador já bebeu e qual é a meta do dia (ex: "1500 ml / 2500 ml").
Adição Rápida: Três botões práticos (+200ml, +300ml, +500ml) para registar o consumo sem digitar nada.
Próximo Lembrete: Exibe a hora exata da próxima notificação agendada.
Contador de "Streak" (Fogo): Mostra há quantos dias consecutivos o utilizador bate a meta (ex: "🔥 5 dias de meta batida!").
Saudação Personalizada: Cumprimenta o utilizador pelo nome (ex: "Olá, Matheus!").

### 2. Configuração Inteligente (Onboarding)

Fluxo de Primeiro Acesso: Ao instalar a app pela primeira vez, o utilizador não tem acesso ao painel de progresso da água sem antes preencher as suas medidas, declarar horas-limite e preencher os diálogos para permitir explicitamente `Notificações`, `Alarmes Exatos` e remoção da `Otimização da Bateria`.
Perfil do Utilizador: Solicita Nome, Peso e Data de Nascimento.
Cálculo Automático de Meta: O algoritmo define a quantidade ideal de água baseada na idade e peso (seguindo a tabela médica de 25ml a 40ml por kg).
Fórmula:
Até 17 anos: 40ml/kg
18 a 55 anos: 35ml/kg
56 a 65 anos: 30ml/kg
66+ anos: 25ml/kg.

Horário Ativo: O utilizador define a que horas acorda e a que horas vai dormir para garantir que as notificações não toquem de madrugada.

### 3. Sistema de Notificações

Lembretes Dinâmicos com "Jitter": Usa AlarmManager para agendar sempre o "próximo lembrete". A hora de disparo calcula precisamente porções com base no que resta da meta diária contra o número de horas para deitar, adicionando ruído de alertamento (uma variação orgânica entre atraso e adiantamentos simulados de ±20%).

Notificação Permanente de Fundo: O Utilizador tem, através das configurações, a opção de prender sempre uma pequena janela no centro de notificações que mostra como está o seu consumo naquele preciso segundo, tal e qual um Dashboard miniatura com três ações imediatas (+200ml, +300ml, +500ml).

Inteligência:
O sistema para de notificar silenciosamente se a meta diária já foi atingida (Cancelamento de Loop).
A "Ponte da Meia Noite" restabelece a nova notificação inaugural sempre na hora em que o alarme define o acordar (sem variação de jitter) no dia seguinte.
Se o telemóvel reiniciar (Receber Intent de BOOT_COMPLETED), o sistema remarca o copo imediatamente se ele for devido.

### 4. Gamificação (Conquistas)

Uma aba dedicada a recompensar o utilizador com medalhas desbloqueáveis:
Consistência: Medalhas por bater a meta por 3, 7, 14, 30 e 100 dias seguidos.
Volume: Medalhas por volume total consumido (10L, 50L, 250L) ou metas diárias (50%, 100%, 200%).
Hábitos: Medalhas por beber água cedo (antes das 09:00), usar as notificações rápidas, ou manter-se hidratado até à última hora do dia.

### 5. Widgets para Ecrã Inicial

O utilizador pode controlar o app sem entrar nele através de dois tamanhos de widget:

Widget Pequeno (5x1): Mostra o progresso, barra visual e botões de +200 e +500ml e mensagens de incentivo ("Meta Batida! 🎉").

### 6. Menu e Informações

Menu Lateral (Gaveta): Navegação fluida entre Início, Conquistas, Configurações e Sobre.

Ecrã "Sobre": Informações sobre a versão do app, créditos ao programador ("Matheus Amorim") e políticas de privacidade.

### 7. Robustez Técnica

Reset Diário: O consumo reinicia automaticamente à meia-noite.

Persistência de Dados: Tudo fica salvo localmente no telemóvel.

Tema Escuro: O design é feito nativamente com suporte a Material Design 3 e tema escuro para conforto visual e economia de bateria.

### 8.  Robustez de Bateria e Suspensão (Crítico)

O `NotificationHelper` deve usar `setExactAndAllowWhileIdle` para garantir que o alarme toque mesmo no modo Doze de sono profundo.
O `AndroidManifest` carrega e pede ativamente as tags rígidas `SCHEDULE_EXACT_ALARM`, `POST_NOTIFICATIONS`.
Para garantir robustez, foi adicionado um espaço gráfico de "Permissões e Funcionamento" inteiro no fundo do próprio ecrã `SettingsActivity`. Este fornece Atalhos de Sistema rápidos que disparam Intents e guiam os utilizadores com precisão — com uma rota especialmente planeada para contornar bloqueios MIUI de `Autostart` nas marcas orientais.

### 9. Backup e Restauração de Dados

- **Criptografia**: Implementado `AES/GCM/NoPadding` com chave fixa (para simplificação) em `BackupManager`.
- **Exportação**:
  - Serialização JSON via Gson (`BackupData`).
  - Salva na pasta **Downloads** via `MediaStore`.
  - Nome do arquivo com timestamp: `backup_hidratese_YYYYMMDD_HHmmss.hds`.
- **Importação**:
  - Seletor de arquivos (`ActivityResultContracts.OpenDocument`).
  - Descriptografia e validação do JSON.
  - **Estratégia de Restore**: Limpa o banco (`clearAllTables`) e re-insere usuário e registros (Substituição Completa).
  - Recálculo automático de conquistas e atualização de widgets após restore.
