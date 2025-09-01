# Modelos de Dados no Redis

> Resumido do livro.
> Obrigado GPT!!!

No Redis, tudo é armazenado como **chaves** que apontam para **valores**.
As chaves podem ser qualquer dado binário (até uma imagem!), mas normalmente são textos simples.

## Resumo:

| Tipo        | Melhor Para                  | Evite Usar Para        |
|-------------|------------------------------|------------------------|
| Strings     | Contadores, flags simples    | Dados complexos        |
| Bitmaps     | flags                        | Dados booleanos        |
| Lists       | Filas, históricos ordenados  | Buscas por valor       |
| Sets        | Tags, membros únicos         | Dados ordenados        |
| Hashes      | Objetos com atributos        | Dados não relacionados |
| Sorted Sets | Rankings, prioridades        | Dados não numéricos    |
| HyperLogLog | Contagem aproximada de únicos | Contagem exata         |


### Comandos Fundamentais:

Comandos basicos: `SET` e `GET`.

```redis
SET usuario "João"     # Armazena o valor
GET usuario           # Retorna "João"
```

---

## 1. Strings

**Para que serve**: Armazenar textos, números, JSON ou dados binários. É o tipo mais básico.

**Exemplos práticos**:
```redis
SET nome "gil"
GET nome        # retorna "gil"
SET visits 10     # Armazena a string "10"

INCR visits       # Retorna (integer) 11 (e salva a string "11")
INCRBY visits 5   # Retorna (integer) 16 (e salva a string "16")
DECR visits       # Retorna (integer) 15 (e salva a string "15")
GET visits        # Retorna "15" (uma string!)

SET preco 99.50   # Armazena a string "99.50"
INCRBYFLOAT preco 10.25 # Retorna "109.75" (uma string!)
GET preco         # Retorna "109.75" (uma string!)
```

---

## 3. bitmaps

**Para que serve**: Bitmaps são uma forma eficiente de armazenar e operar em dados binários (valores 0 ou 1) usando operações bit a bit.
São ideais para:
+ Sistemas de presença e status online/offline
+ Controle de features flags e permissões
+ Estatísticas e analytics de acesso
+ Sistemas de votação e acompanhamento de eventos

**Exemplos práticos**:

```redis
SETBIT online 15 1    # Usuário 15 está online
SETBIT online 23 1    # Usuário 23 está online
GETBIT online 15      # Retorna 1 (está online)
GETBIT online 99      # Retorna 0 (está offline)

SETBIT feature:VIP 5 1    # Usuário 5 tem acesso VIP
SETBIT feature:VIP 8 1    # Usuário 8 tem acesso VIP
GETBIT feature:VIP 5      # Retorna 1 (tem acesso)
GETBIT feature:VIP 7      # Retorna 0 (não tem acesso)

SETBIT cliques:link1 100 1    # Usuário 100 clicou
SETBIT cliques:link1 101 1    # Usuário 101 clicou
BITCOUNT cliques:link1        # Retorna 2 (2 cliques no total)
```

---

##  4. Lists

**Para que serve**: Filas, histórico, timelines

**Como funciona**: Como uma fila dupla (adiciona no início ou fim)

**Exemplos**:
```redis
# 1. Sistema de Fila de Tarefas
LPUSH tarefas:pending "processar_pedido_123"
LPUSH tarefas:pending "enviar_email_456"
RPOP tarefas:pending  # Processa a mais antiga: "processar_pedido_123"
LLEN tarefas:pending  # Mostra quantas tarefas restam: 1

# 2.  Histórico de Mensagens de Chat:
RPUSH chat:room1 "João: Olá pessoal!"
RPUSH chat:room1 "Maria: Tudo bem?"
RPUSH chat:room1 "Pedro: Como vocês estão?"
LRANGE chat:room1 0 2  # Mostra as 3 últimas mensagens
LTRIM chat:room1 0 99  # Mantém apenas as 100 mensagens mais recentes

# 3. Timeline de Posts Recentes:
LPUSH posts:recent "post_789"
LPUSH posts:recent "post_456" 
LPUSH posts:recent "post_123"
LRANGE posts:recent 0 4  # Mostra os 5 posts mais recentes
LTRIM posts:recent 0 9   # Mantém apenas os 10 posts mais recentes
```

**Use quando**: Precisa de ordem e pode ter valores repetidos

---

##  5. Sets (Conjuntos Únicos)
**Para que serve**: Membros de um grupo, tags de um artigo, seguidores de um influencer

**Exemplos**:
```redis
# 1. Sistema de Seguidores:
SADD seguidores:maria "joao" "pedro" "ana"  # Maria tem 3 seguidores
SADD seguidores:maria "carla"               # Adiciona mais uma seguidora
SISMEMBER seguidores:maria "joao"           # Verifica se João segue Maria → 1 (true)
SMEMBERS seguidores:maria                   # Lista todos os seguidores de Maria
SCARD seguidores:maria                      # Conta quantos seguidores Maria tem → 4

# 2.️ Sistema de Tags de Artigos:
SADD artigo:123:tags "tecnologia" "python" "programacao"
SADD artigo:456:tags "tecnologia" "redis" "banco-de-dados"
SINTER artigo:123:tags artigo:456:tags      # Tags em comum → "tecnologia"
SUNION artigo:123:tags artigo:456:tags      # Todas as tags únicas

# 3. Membros de Grupos/Categorias:
SADD grupo:premium "usuario:789" "usuario:456"
SADD grupo:vip "usuario:123" "usuario:789"
SISMEMBER grupo:premium "usuario:789"       # Verifica se usuário está no grupo premium → 1
SINTER grupo:premium grupo:vip              # Usuários que estão em AMBOS os grupos → "usuario:789"

4. Sistema de Bloqueios
SADD bloqueados:usuario:555 "usuario:777" "usuario:888"
SISMEMBER bloqueados:usuario:555 "usuario:777"  # Verifica se usuário 777 está bloqueado → 1
SREM bloqueados:usuario:555 "usuario:888"   # Remove bloqueio do usuário 888

5. Sistema de Interesses/Preferências
SADD interesses:usuario:999 "futebol" "tecnologia" "musica"
SADD interesses:usuario:888 "cinema" "tecnologia" "viagens"
SDIFF interesses:usuario:999 interesses:usuario:888  # Interesses exclusivos do usuário 999
```

**Diferença para Lists**: Não tem ordem e não permite duplicatas

---

##  6. Hashes (Tabelas Hash)

**Para que serve**: Objetos com múltiplos atributos (usuários, produtos). 
Exemplos:
 + Perfil de Usuário
 + Produto em E-commerce
 + Carrinho de Compras
 + Configurações de Aplicação
 + Estatísticas de Sessão

```redis
# 1. Perfil de Usuário
HSET usuario:123 nome "João Silva" email "joao@email.com" idade 28 cidade "São Paulo"
HGET usuario:123 nome          # Retorna "João Silva"
HGETALL usuario:123            # Retorna todos os campos e valores
HINCRBY usuario:123 idade 1    # Incrementa idade para 29
HDEL usuario:123 cidade        # Remove o campo cidade

# 2. Produto em E-commerce
HSET produto:789 nome "Smartphone XYZ" preco 999.90 estoque 15 marca "Samsung"
HGET produto:789 preco         # Retorna "999.90"
HINCRBY produto:789 estoque -1 # Decrementa estoque para 14
HSET produto:789 preco 899.90  # Atualiza preço
HKEYS produto:789              # Retorna [nome, preco, estoque, marca]

# 3. Carrinho de Compras
HSET carrinho:usuario:555 produto:789 2    # 2 unidades do produto 789
HSET carrinho:usuario:555 produto:456 1    # 1 unidade do produto 456
HINCRBY carrinho:usuario:555 produto:789 1 # Adiciona mais 1 unidade → 3
HGETALL carrinho:usuario:555               # Mostra todo o carrinho
HDEL carrinho:usuario:555 produto:456      # Remove produto do carrinho

# 4. Configurações de Aplicação
HSET config:app tema "escuro" idioma "pt-BR" notificacoes 1 timeout 30
HGET config:app tema           # Retorna "escuro"
HSETNX config:app tema "claro" # Só define se não existir (não altera)
HSET config:app tema "claro"   # Força alteração para "claro"

# 5. Estatísticas de Sessão
HSET sessao:abc pagina_views 15 tempo_total 3600 ultima_pagina "/home"
HINCRBY sessao:abc pagina_views 1      # Incrementa para 16
HINCRBY sessao:abc tempo_total 120     # Adiciona 2 minutos
HGET sessao:abc ultima_pagina          # Retorna "/home"
```

**Ideal para**: Dados estruturados que são acessados juntos

---

##  7. Sorted Sets (Conjuntos Ordenados)
**Para que serve**: Rankings competitivos onde a ordem importa e precisa ser atualizada frequentemente. Listas de Prioridades onde membro tem um score numérico que determina sua posição automática na prioridade.
Exemplos:
+ Ranking de Jogadores
+ Leaderboard de Produtos Mais Vendidos
+ Sistema de Prioridade de Tarefas
+ Ranking por Tempo de Resposta
+ Sistema de Pontuação por Categoria


**Exemplo - Ranking de Jogadores**:
```redis
 # 1. Ranking de Jogadores
ZADD ranking 1000 "player1" 1500 "player2" 800 "player3"  # Adiciona jogadores com pontuação
ZINCRBY ranking 200 "player3"  # Aumenta pontuação do player3 para 1000
ZREVRANGE ranking 0 2 WITHSCORES  # Top 3 jogadores (maior para menor)
ZRANGE ranking 0 -1 WITHSCORES    # Todos em ordem crescente
ZRANK ranking "player2"           # Posição no ranking (0-based, crescente)
ZREVRANK ranking "player2"        # Posição no ranking (0-based, decrescente)

# 2. Leaderboard de Produtos Mais Vendidos
ZADD vendas:semana 150 "produto:A" 89 "produto:B" 200 "produto:C"
ZINCRBY vendas:semana 50 "produto:B"  # produto:B agora tem 139 vendas
ZREVRANGE vendas:semana 0 4 WITHSCORES  # Top 5 produtos mais vendidos
ZSCORE vendas:semana "produto:C"      # Retorna 200 (vendas do produto C)

 # 3. Sistema de Prioridade de Tarefas
ZADD tarefas:prioridade 1 "tarefa:baixa" 3 "tarefa:media" 5 "tarefa:alta" 10 "tarefa:critica"
ZRANGEBYSCORE tarefas:prioridade 5 10  # Tarefas com prioridade 5 a 10 (alta e crítica)
ZPOPMAX tarefas:prioridade             # Remove e retorna a tarefa mais prioritária

# 4. Ranking por Tempo de Resposta
ZADD response:times 120 "api:login" 45 "api:search" 300 "api:report"
ZINCRBY response:times -10 "api:login"  # Melhora tempo para 110ms
ZREVRANGEBYSCORE response:times 500 100  # APIs com tempo entre 100-500ms (piores)

# 5. Sistema de Pontuação por Categoria
ZADD ranking:arcade 5000 "user:john" 7000 "user:sarah"
ZADD ranking:strategy 3000 "user:john" 6000 "user:mike"
ZUNIONSTORE ranking:total 2 ranking:arcade ranking:strategy AGGREGATE SUM
ZREVRANGE ranking:total 0 -1 WITHSCORES  # Ranking combinado
```

**Diferencial**: Valores únicos com scores numéricos para ordenação

---

## 8. HyperLogLog (Contagem Aproximada)

**Para que serve**: Contar itens únicos de forma eficiente (usa pouca memória).
Exemplos:
+ Visitantes Únicos de um Site
+ Visitantes Únicos da Semana (união de dias)
+ Visualizações Únicas de Posts
+ Cliques Únicos em Campanhas
+ Comparação entre Campanhas


**Exemplo - Visitantes Únicos**:
```redis
# 1. Visitantes Únicos de um Site
PFADD visitantes:dia:2023-10-01 "user:123" "user:456" "user:789"
PFADD visitantes:dia:2023-10-01 "user:123" "user:999"  # user:123 duplicado é ignorado
PFCOUNT visitantes:dia:2023-10-01  # Retorna ≈4 visitantes únicos

# 2. Visitantes Únicos da Semana (união de dias)
PFADD visitantes:dia:2023-10-02 "user:123" "user:777" "user:888"
PFADD visitantes:dia:2023-10-03 "user:456" "user:999" "user:111"

PFMERGE visitantes:semana visitantes:dia:2023-10-01 visitantes:dia:2023-10-02 visitantes:dia:2023-10-03
PFCOUNT visitantes:semana  # Retorna ≈7 visitantes únicos na semana

# 3. Visualizações Únicas de Posts
PFADD visualizacoes:post:123 "user:100" "user:101" "user:102"
PFADD visualizacoes:post:123 "user:100" "user:103"  # user:100 duplicado
PFCOUNT visualizacoes:post:123  # Retorna ≈4 visualizações únicas

# 4. Cliques Únicos em Campanhas
PFADD cliques:campanha:abc "ip:192.168.1.1" "ip:192.168.1.2" "ip:192.168.1.3"
PFADD cliques:campanha:abc "ip:192.168.1.1" "ip:192.168.1.4"  # ip duplicado
PFCOUNT cliques:campanha:abc  # Retorna ≈4 cliques únicos

# 5. Comparação entre Campanhas
PFADD campanha:A "user:1" "user:2" "user:3" "user:4"
PFADD campanha:B "user:3" "user:4" "user:5" "user:6"

PFMERGE campanhas:total campanha:A campanha:B
PFCOUNT campanhas:total  # Retorna ≈6 usuários únicos entre as campanhas
```

**Precisão**: ≈99% com baixo uso de memória
**Use quando**: Precisa de contagem aproximada de milhões de itens
