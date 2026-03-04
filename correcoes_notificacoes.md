# Plano de Correção: Notificações

Este documento detalha as alterações no código necessárias para resolver as falhas de cancelamento acidental e bugs matemáticos do fluxo de notificação. As intervenções localizam-se principalmente nos arquivos `NotificationHelper.kt` e `MainActivity.kt`.

## 1. Evitar Cancelamento Acidental e Reagendamento em Loop

O reagendamento só deve ocorrer naturalmente no início de um novo dia ou caso o aparelho seja reiniciado. Abrir a app não deve apagar o ciclo de horários aleatórios do utilizador.

**Ação:** Remover as chamadas desnecessárias na `MainActivity` e Widgets.

* Em `MainActivity.kt`, localize e comente (ou apague) a chamada a `notificationHelper.scheduleDailyNotificationsOptimized()` dentro do método privado `scheduleNextNotification()`.
* A mesma cautela deve prevalecer para não invocar o agendamento através de atualizações do _Widget_. Deixe o disparo a cargo apenas de `NotificationReceiver` via Boot Completed ou My Package Replaced e do gatilho diário 999.

## 2. Refinar Lógica Diária vs. Fuso Horário

Se um utilizador dorme para além das 00:00h (ex: da meia-noite às 08:00h da manhã), a lógica 24h não funciona (`8 < 1` no bloco `while` retorna falso).

**Ação:** Refatorar o limite no `NotificationHelper.kt` em `scheduleDailyNotificationsOptimized()`.

* Em vez de apenas ler horas, será necessário calcular minutos absolutos com a biblioteca java `Calendar` para suportar sobreposições de madrugada.
* Se `wakeUpTime` (08h) > `sleepTime` (01h), os cálculos matemáticos aleatórios precisam considerar a madrugada ativamente acrescentando 24 horas (`24+1 = 25h`) ou reajustando os índices do Calendário.

## 3. Garantir Foco (Early Return) na Verificação de Metas

Caso o utilizador atinja a meta (ex: 2000ml), o código deve silenciar notificações remanescentes.

**Ação:** Aplicar verificador interno na raiz temporal de disparo.

* Em `NotificationHelper.kt` no evento global de agendamento (dentro do `scheduleDailyNotificationsOptimized`), inclua o seguinte trecho: se `totalAguaHoje >= user.dailyGoal` -> não chame o iterador `while` de agendamento e anule silenciosamente através de um simples "return". O utilizador alcançou a cota.

## 4. Crash Silencioso no `setExactAndAllowWhileIdle()`

Para garantir que os Dispatchers e Timers não rebentem com uma `SecurityException` numa verificação falhada em telemóveis super restritos.

**Ação:** Criar _FallBack_ nas Configurações do Android.

* Em `SettingsActivity.kt` ou num ecrã central (ex: splash de início de permissões do Android 12+), se `alarmManager.canScheduleExactAlarms()` for **falso**, faça intervir diretamente a interface de base:
`startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))`
* Isto forceará a abertura de um painel oficial do SO provando que o app requer esta capacidade para notificar exatamente às horas calculadas.
