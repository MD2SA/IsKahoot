# Prioridade Máxima
- [ ] **[Dificil]** Implementar Barreira de Equipas
- [ ] **[FÁCIL]** Garantir que nao usamos nada da biblioteca concurrent e que todas as cenas estao prontas para lidar com concorrencia
                    - Existe ai perdido um ou outro atomic integer e sets e listas que nao estao preparadas para ser concurrents
- [X] **[Médio|Dificil]** Arranjar problema de reutilizacao do mesmo quiz (ao reutilizar aparece como se o quiz nao tivesse mais perguntas)
                         - Solucao foi facil mas secalhar manhosa, agora curQuestion pertence ao gameState

## Prioridade Alta
- [X] **[Médio|Dificil]** Implementar unregister from game/ Lidar com interrupcao de fim de jogo em DealWithClient
- [ ] **[Médio]** Implementar Thread Pool do zero para usar no game manager

### Prioridade Média
- [x] **[Fácil]** Ecra de fim de jogo

#### Prioridade Baixa
- [x] **[Fácil]** GUI com username e teamID constante
- [X] **[Médio|Dificil]** Refactor no servidor para ficar melhor
- [ ] **[Médio|Fácil]** Refactor no cliente para ficar melhor
- [ ] **[Médio|Facil]** Mostrar ecra enquanto espera que outros se juntem
- [ ] **[Médio|Facil]** Mostrar ecra com contagem decrescente  para proxima pergunta
