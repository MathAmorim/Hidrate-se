Atue como: Um Desenvolvedor Android Sênior especialista em Kotlin e Material Design 3.

Objetivo: Criar um aplicativo nativo completo chamado "Hidrate-se" para monitoramento de ingestão de água. O código deve ser robusto, usar as melhores práticas de ciclo de vida do Android


Crie o aplicativo com base na interface que já foi implementada nos arquivos listados abaixo.
app\src\main\res\layout\ *
app\src\main\res\menu\ *
app\src\main\res\drawable\ *
app\src\main\res\color\ *


A primeiro momento faça somente a interface estar pronta para conpilação, não precisa da logica, somente faça o app abrir e o menu de navegação funcionar.

E faça uma listar de a fazer com as funcionalidades abaixo separada em fases.


1. Painel Principal (Dashboard)
A tela inicial serve como o centro de controle do usuário:

Visualização de Progresso: Um gráfico circular e textos claros mostram quanto o utilizador já bebeu e qual é a meta do dia (ex: "1500 ml / 2500 ml").
Adição Rápida: Três botões práticos (+200ml, +300ml, +500ml) para registar o consumo sem digitar nada.
Próximo Lembrete: Exibe a hora exata da próxima notificação agendada.
Contador de "Streak" (Fogo): Mostra há quantos dias consecutivos o utilizador bate a meta (ex: "🔥 5 dias de meta batida!").
Saudação Personalizada: Cumprimenta o utilizador pelo nome (ex: "Olá, Matheus!").

2. Configuração Inteligente
Perfil do Utilizador: Solicita Nome, Peso e Data de Nascimento.
Cálculo Automático de Meta: O algoritmo define a quantidade ideal de água baseada na idade e peso (seguindo a tabela médica de 25ml a 40ml por kg).
Fórmula:
Até 17 anos: 40ml/kg
18 a 55 anos: 35ml/kg
56 a 65 anos: 30ml/kg
66+ anos: 25ml/kg.

Horário Ativo: O utilizador define a que horas acorda e a que horas vai dormir para garantir que as notificações não toquem de madrugada.

3. Sistema de Notificações
Lembretes Automáticos: Envia notificações push para lembrar de beber água durante o dia.

Ações na Notificação: A notificação é interativa; o utilizador pode adicionar água (+200, +300, +500) diretamente pela barra de notificações, sem abrir o app.
Usar AlarmManager para agendar lembretes.

Inteligência:
O sistema para de notificar se a meta diária já foi atingida.
Os alarmes são agendados aleatoriamente dentro do horário ativo para não serem monótonos.
Se o telemóvel reiniciar, os alarmes são reagendados automaticamente.

4. Gamificação (Conquistas)
Uma aba dedicada a recompensar o utilizador com medalhas desbloqueáveis:
Consistência: Medalhas por bater a meta por 3, 7, 14, 30 e 100 dias seguidos.
Volume: Medalhas por volume total consumido (10L, 50L, 250L) ou metas diárias (50%, 100%, 200%).
Hábitos: Medalhas por beber água cedo (antes das 09:00), usar as notificações rápidas, ou manter-se hidratado até à última hora do dia.

5. Widgets para Ecrã Inicial
O utilizador pode controlar o app sem entrar nele através de dois tamanhos de widget:

Widget Pequeno (5x1): Mostra o progresso, barra visual e botões de +200 e +500ml e mensagens de incentivo ("Meta Batida! 🎉").

6. Menu e Informações
Menu Lateral (Gaveta): Navegação fluida entre Início, Conquistas, Configurações e Sobre.

Ecrã "Sobre": Informações sobre a versão do app, créditos ao programador ("Matheus Amorim") e políticas de privacidade.

7. Robustez Técnica
Reset Diário: O consumo reinicia automaticamente à meia-noite.

Persistência de Dados: Tudo fica salvo localmente no telemóvel.

Tema Escuro: O design é feito nativamente com suporte a Material Design 3 e tema escuro para conforto visual e economia de bateria.


8.  Robustez de Bateria e Suspensão (Crítico):
O AlarmScheduler deve usar setExactAndAllowWhileIdle para garantir que o alarme toque mesmo no modo Doze.
O AndroidManifest deve conter a permissão SCHEDULE_EXACT_ALARM.
Na tela "Sobre" (AboutActivity), adicione uma aba de ajuda que exibe as informações explicando como resolver o problema de recebimento de notificações: "Não está recebendo notificações?" "Em celulares Xiaomi/Samsung, ative o 'Início Automático' e coloque a economia de bateria em 'Sem Restrições' para este app."

---

9. Backup e Restauração de Dados
Status: Implementado (Concluído).

Detalhes Técnicos:
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