package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisJSONExample implements Example{

    @Override
    public void example(JedisPool pool) {
        try (Jedis jedis = pool.getResource()) {

/*
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



 */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
