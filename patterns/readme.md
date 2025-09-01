# Padroes no Redis

> Resumido do livro.
> Obrigado GPT!!!

Aqui estão os padrões do Redis:

## Resumo:

| Tipo               | Melhor Para                  | Evite Usar Para          |
|--------------------|------------------------------|--------------------------|
| Pub/Sub            | Mensageria em tempo real     | Mensagens persistentes   |
| Geospatial         | Localização e geolocalização | Dados não espaciais      |
| Redis Streams      | Log de eventos e streaming   | Dados simples            |

## 1. Pub/Sub (Publish/Subscribe)

**Para que serve**: Sistema de mensageria em tempo real onde publicadores enviam mensagens e subscribers recebem em canais.

**Exemplos práticos**:
```redis
# Terminal 1 - Subscribe em canais
SUBSCRIBE notificacoes:usuario:123
SUBSCRIBE chat:geral

# Terminal 2 - Publica mensagens
PUBLISH notificacoes:usuario:123 "Nova mensagem recebida"
PUBLISH chat:geral "João: Olá a todos!"

# Subscribe com padrão (usando PSUBSCRIBE)
PSUBSCRIBE notificacoes:*

# Publica em múltiplos canais que casam com o padrão
PUBLISH notificacoes:usuario:456 "Sua encomenda chegou"
```

**Use quando**: Precisa de comunicação em tempo real, chat, notificações push, broadcast de mensagens.

---

## 2. Geospatial indexes

**Para que serve**: Armazenar e consultar dados baseados em localização geográfica (coordenadas latitude/longitude).

**Exemplos práticos**:
```redis
# Adicionar localizações
GEOADD restaurantes:sp -23.5505 -46.6333 "Madero" -23.5637 -46.6520 "Fogo de Chao"

# Buscar restaurantes próximos a um ponto (10km de raio)
GEORADIUS restaurantes:sp -23.5505 -46.6333 10 km WITHDIST

# Buscar por membro mais próximo
GEORADIUSBYMEMBER restaurantes:sp "Madero" 5 km WITHDIST

# Calcular distância entre dois pontos
GEODIST restaurantes:sp "Madero" "Fogo de Chao" km

# Obter coordenadas de um membro
GEOHASH restaurantes:sp "Madero"
```

**Use quando**: Sistemas de localização, encontrar pontos próximos, cálculo de distâncias, apps de delivery.

---

## 3. Redis Streams

**Para que serve**: Log de eventos append-only para processamento de streams de dados em tempo real.

**Exemplos**:
```redis
# Adicionar eventos ao stream
XADD pedidos:* produto "iPhone" valor 4999.90 cliente "João"
XADD pedidos:* produto "Macbook" valor 8999.90 cliente "Maria"

# Ler eventos do stream
XREAD COUNT 10 STREAMS pedidos 0

# Criar grupo de consumidores
XGROUP CREATE pedidos grupo-consumidores $ MKSTREAM

# Consumir mensagens como parte do grupo
XREADGROUP GROUP grupo-consumidores consumidor-1 COUNT 10 STREAMS pedidos >
```

**Use quando**: Processamento de eventos, message brokering, logs auditáveis, sistemas de filas complexas.