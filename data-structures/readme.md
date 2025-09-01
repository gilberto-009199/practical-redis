# Modelos de Dados no Redis

> Resumido do livro.
> Obrigado GPT!!!

No Redis, tudo é armazenado como **chaves** que apontam para **valores**.
As chaves podem ser qualquer dado binário (até uma imagem!), mas normalmente são textos simples.

### Comandos Fundamentais:

Comandos basicos: `SET` e `GET`.

```redis
SET usuario "João"     # Armazena o valor
GET usuario           # Retorna "João"
```

---

## 🧵 1. Strings

**Para que serve**: 

**Exemplos práticos**:
```redis
SET nome "gil"
GET nome        # retorna "gil"
SET visits 10     # Armazena a string "10"

INCR visits       # Retorna (integer) 11 (e salva a string "11")
INCRBY visits 5   # Retorna (integer) 16 (e salva a string "16")
DECR visits       # Retorna (integer) 15 (e salva a string "15")
GET visits        # Retorna "15" (uma string!)

SET price 99.50   # Armazena a string "99.50"
INCRBYFLOAT price 10.25 # Retorna "109.75" (uma string!)
GET price         # Retorna "109.75" (uma string!)
```

---

## 🧵 3. bitmaps

**Para que serve**: 

**Exemplos práticos**:

```redis

```

---

## 📃 4. Lists (Listas Ordenadas)
**Para que serve**: Filas, histórico, timelines

**Como funciona**: Como uma fila dupla (adiciona no início ou fim)

**Exemplos**:
```redis
LPUSH tarefas "ler email"     # Adiciona no início
RPUSH tarefas "escrever relatório"  # Adiciona no fim
LRANGE tarefas 0 -1           # Lista tudo
LINDEX tarefas 0              # Pega primeiro item
```

**Use quando**: Precisa de ordem e pode ter valores repetidos

---

## 🎯 5. Sets (Conjuntos Únicos)
**Para que serve**: Membros únicos, tags, seguidores

**Exemplos**:
```redis
SADD tags "redis" "banco" "nosql"  # Adiciona tags
SMEMBERS tags                      # Lista todas tags
SISMEMBER tags "redis"             # Verifica se existe → retorna 1
```

**Diferença para Lists**: Não tem ordem e não permite duplicatas

---

## 🗃️ 6. Hashes (Tabelas Hash)
**Para que serve**: Objetos com múltiplos atributos (usuários, produtos)

**Exemplo prático - Perfil de usuário**:
```redis
HSET usuario:123 nome "Maria" idade 28 cidade "SP"
HGET usuario:123 nome          # Retorna "Maria"
HGETALL usuario:123            # Retorna todos campos
```

**Ideal para**: Dados estruturados que são acessados juntos

---

## 🏆 7. Sorted Sets (Conjuntos Ordenados)
**Para que serve**: Rankings, leaderboards, prioridades

**Exemplo - Ranking de Jogadores**:
```redis
ZADD ranking 1500 "Ana" 1200 "Pedro" 1800 "Carlos"
ZREVRANGE ranking 0 -1 WITHSCORES  # Ranking decrescente
ZINCRBY ranking 100 "Pedro"        # Aumenta pontuação
```

**Diferencial**: Valores únicos com scores numéricos para ordenação

---

## 📊 8. HyperLogLog (Contagem Aproximada)
**Para que serve**: Contar itens únicos de forma eficiente (usa pouca memória)

**Exemplo - Visitantes Únicos**:
```redis
PFADD visitantes "192.168.1.1" "192.168.1.2"
PFCOUNT visitantes              # Estima ≈2 visitantes únicos
```

**Precisão**: ≈99% com baixo uso de memória
**Use quando**: Precisa de contagem aproximada de milhões de itens

---

## 💡 Dicas de Uso:

| Tipo         | Melhor Para                          | Evite Usar Para          |
|--------------|--------------------------------------|--------------------------|
| Strings      | Contadores, flags simples           | Dados complexos          |
| Lists        | Filas, históricos ordenados         | Buscas por valor         |
| Sets         | Tags, membros únicos                | Dados ordenados          |
| Hashes       | Objetos com atributos               | Dados não relacionados   |
| Sorted Sets  | Rankings, prioridades               | Dados não numéricos      |
| HyperLogLog  | Contagem aproximada de únicos       | Contagem exata           |

**Exemplo Real**: Sistema de Posts
- Strings: Contador de likes
- Hash: Dados do post (título, autor, conteúdo)
- Set: Tags do post
- Sorted Set: Posts mais populares