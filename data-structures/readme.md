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
LPUSH tarefas:pending "processar_pedido_123"
LPUSH tarefas:pending "enviar_email_456"
RPOP tarefas:pending  # Processa a mais antiga: "processar_pedido_123"
LLEN tarefas:pending  # Mostra quantas tarefas restam: 1

RPUSH chat:room1 "João: Olá pessoal!"
RPUSH chat:room1 "Maria: Tudo bem?"
RPUSH chat:room1 "Pedro: Como vocês estão?"
LRANGE chat:room1 0 2  # Mostra as 3 últimas mensagens
LTRIM chat:room1 0 99  # Mantém apenas as 100 mensagens mais recentes

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
SADD seguidores:maria "joao" "pedro" "ana"  # Maria tem 3 seguidores
SADD seguidores:maria "carla"               # Adiciona mais uma seguidora
SISMEMBER seguidores:maria "joao"           # Verifica se João segue Maria → 1 (true)
SMEMBERS seguidores:maria                   # Lista todos os seguidores de Maria
SCARD seguidores:maria                      # Conta quantos seguidores Maria tem → 4

SADD artigo:123:tags "tecnologia" "python" "programacao"
SADD artigo:456:tags "tecnologia" "redis" "banco-de-dados"
SINTER artigo:123:tags artigo:456:tags      # Tags em comum → "tecnologia"
SUNION artigo:123:tags artigo:456:tags      # Todas as tags únicas

SADD grupo:premium "usuario:789" "usuario:456"
SADD grupo:vip "usuario:123" "usuario:789"
SISMEMBER grupo:premium "usuario:789"       # Verifica se usuário está no grupo premium → 1
SINTER grupo:premium grupo:vip              # Usuários que estão em AMBOS os grupos → "usuario:789"

SADD bloqueados:usuario:555 "usuario:777" "usuario:888"
SISMEMBER bloqueados:usuario:555 "usuario:777"  # Verifica se usuário 777 está bloqueado → 1
SREM bloqueados:usuario:555 "usuario:888"   # Remove bloqueio do usuário 888

SADD interesses:usuario:999 "futebol" "tecnologia" "musica"
SADD interesses:usuario:888 "cinema" "tecnologia" "viagens"
SDIFF interesses:usuario:999 interesses:usuario:888  # Interesses exclusivos do usuário 999
```

**Diferença para Lists**: Não tem ordem e não permite duplicatas

---

##  6. Hashes (Tabelas Hash)
**Para que serve**: Objetos com múltiplos atributos (usuários, produtos)

**Exemplo prático - Perfil de usuário**:
```redis
HSET usuario:123 nome "Maria" idade 28 cidade "SP"
HGET usuario:123 nome          # Retorna "Maria"
HGETALL usuario:123            # Retorna todos campos
```

**Ideal para**: Dados estruturados que são acessados juntos

---

##  7. Sorted Sets (Conjuntos Ordenados)
**Para que serve**: Rankings, leaderboards, prioridades

**Exemplo - Ranking de Jogadores**:
```redis
ZADD ranking 1500 "Ana" 1200 "Pedro" 1800 "Carlos"
ZREVRANGE ranking 0 -1 WITHSCORES  # Ranking decrescente
ZINCRBY ranking 100 "Pedro"        # Aumenta pontuação
```

**Diferencial**: Valores únicos com scores numéricos para ordenação

---

## 8. HyperLogLog (Contagem Aproximada)
**Para que serve**: Contar itens únicos de forma eficiente (usa pouca memória)

**Exemplo - Visitantes Únicos**:
```redis
PFADD visitantes "192.168.1.1" "192.168.1.2"
PFCOUNT visitantes              # Estima ≈2 visitantes únicos
```

**Precisão**: ≈99% com baixo uso de memória
**Use quando**: Precisa de contagem aproximada de milhões de itens
