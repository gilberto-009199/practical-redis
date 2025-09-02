# Modulos no Redis

> Resumido do livro.
> Obrigado GPT!!!

Aqui estão os padrões do Redis:


## Resumo:

| Tipo               | Melhor Para                                      | Evite Usar Para                      |
|--------------------|--------------------------------------------------|--------------------------------------|
| Redis Search       | Busca textual, indexação geográfica              | Dados simples chave-valor            |
| Redis Graph        | Relacionamentos complexos, redes sociais        | Dados hierárquicos simples           |
| Redis TimeSeries   | Métricas, IoT, dados temporais                   | Dados não temporais                  |
| Redis JSON         | Documentos JSON, dados aninhados                 | Dados simples sem estrutura          |


---

## 1. Redis Search

**Para que serve**: Busca full-text, indexação secundária e queries geoespaciais avançadas.

**Exemplos práticos**:
```redis
# Criar índice com campos textuais e geoespaciais
FT.CREATE idx:restaurantes ON HASH PREFIX 1 restaurante: 
SCHEMA 
    nome TEXT WEIGHT 5.0 
    descricao TEXT 
    tags TAG 
    localizacao GEO

# Adicionar restaurante
HSET restaurante:1 
    nome "Pizzaria Paulista" 
    descricao "Melhor pizza de São Paulo" 
    tags "italiano,pizza,entrega" 
    localizacao "-23.5505,-46.6333"

# Buscar por texto e localização
FT.SEARCH idx:restaurantes 
    "(@nome:pizza) (@localizacao:[-23.5505 -46.6333 10 km])"

# Buscar por tags
FT.SEARCH idx:restaurantes "@tags:{italiano}"
```

**Use quando**: 
- Precisa de busca textual com relevância
- Queries geoespaciais complexas
- Indexação secundária em campos múltiplos
- Filtros combinados (texto + geo + tags)

---

## 2. Redis Graph

**Para que serve**: Modelar e consultar relacionamentos complexos entre entidades.

**Exemplos práticos**:
```redis
# Criar nós (usuários e restaurantes)
GRAPH.QUERY foodDelivery "CREATE (:Usuario {id: 1, nome: 'João'})"
GRAPH.QUERY foodDelivery "CREATE (:Restaurante {id: 101, nome: 'Pizza Hut'})"

# Criar relacionamentos
GRAPH.QUERY foodDelivery "
MATCH (u:Usuario {id: 1}), (r:Restaurante {id: 101})
CREATE (u)-[:FAVORITO {desde: '2024-01-01'}]->(r)
"

# Consultar restaurantes favoritos de um usuário
GRAPH.QUERY foodDelivery "
MATCH (u:Usuario {nome: 'João'})-[:FAVORITO]->(r:Restaurante)
RETURN r.nome, r.id
"

# Recomendações baseadas em relacionamentos
GRAPH.QUERY foodDelivery "
MATCH (u:Usuario {id: 1})-[:FAVORITO]->(r:Restaurante)<-[:FAVORITO]-(outro:Usuario)
MATCH (outro)-[:FAVORITO]->(recomendacao:Restaurante)
WHERE NOT (u)-[:FAVORITO]->(recomendacao)
RETURN DISTINCT recomendacao.nome, COUNT(*) as score
ORDER BY score DESC
"
```

**Use quando**: 
- Redes sociais ou sistemas de recomendação
- Grafos de conhecimento
- Análise de relacionamentos complexos
- Sistemas de fraud detection

---

## 3. Redis TimeSeries

**Para que serve**: Armazenar e consultar séries temporais com agregações em tempo real.

**Exemplos práticos**:
```redis
# Criar uma série temporal para pedidos por minuto
TS.CREATE pedidos:restaurante:1:por_minuto 
    RETENTION 604800000  # 7 dias em ms
    LABELS restaurante_id 1 tipo_metricas "pedidos"

# Adicionar dados (timestamp automático)
TS.ADD pedidos:restaurante:1:por_minuto * 15

# Consultar agregados
TS.RANGE pedidos:restaurante:1:por_minuto 
    - + 
    AGGREGATION avg 3600000  # média por hora

# Múltricas séries com agregação
TS.MRANGE 
    - + 
    AGGREGATION sum 86400000  # soma por dia
    FILTER restaurante_id=1

# Previsão com ML (usando RedisAI integrado)
TS.MREVRANGE 
    - + 
    AGGREGATION avg 3600000
    FILTER restaurante_id=1
    PREDICT 5  # prever próximas 5 horas
```

**Use quando**: 
- Métricas de aplicação e monitoramento
- Dados de IoT e sensores
- Análise temporal de negócio
- Previsões e tendências

---

## 4. Redis JSON

**Para que serve**: Armazenar e manipular documentos JSON nativamente no Redis.

**Exemplos práticos**:
```redis
# Armazenar perfil completo de restaurante
JSON.SET restaurante:1:perfil . '
{
  "nome": "Pizzaria Paulista",
  "coordenadas": {
    "lat": -23.5505,
    "lon": -46.6333
  },
  "horario_funcionamento": {
    "segunda": {"abre": "11:00", "fecha": "22:00"},
    "sabado": {"abre": "12:00", "fecha": "23:00"}
  },
  "menu": [
    {"nome": "Pizza Margherita", "preco": 45.90},
    {"nome": "Pizza Calabresa", "preco": 48.90}
  ]
}'

# Buscar campos específicos
JSON.GET restaurante:1:perfil .nome .coordenadas.lat

# Atualizar apenas um campo
JSON.SET restaurante:1:perfil .horario_funcionamento.domingo '{"abre": "12:00", "fecha": "22:00"}'

# Query com Redis Search integrado
FT.SEARCH idx:restaurantes 
    "@menu.nome:pizza @menu.preco:[40 50]"
```

**Use quando**: 
- Dados com estrutura complexa e aninhada
- Documentos que precisam de atualizações parciais
- Integração com APIs JSON
- Schemas flexíveis e dinâmicos
