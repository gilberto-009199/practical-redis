package com.example;

import redis.clients.jedis.JedisPool;

import redis.clients.jedis.util.SafeEncoder;

import java.util.Map;


public class RedisSearch implements Example {

    @Override
    public void example(JedisPool pool) {
        try(var jedis = pool.getResource()){

            System.out.println("=== Redis Search Example ===");

            // Executa FT.CREATE idx:restaurantes ...
            var result = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.CREATE"),
                    "idx:restaurantes".getBytes(),
                    "ON".getBytes(), "HASH".getBytes(),
                    "PREFIX".getBytes(), "1".getBytes(), "restaurante:".getBytes(),
                    "SCHEMA".getBytes(),
                    "nome".getBytes(), "TEXT".getBytes(),
                    "cozinha".getBytes(), "TAG".getBytes()
            );

            System.out.println("✓ Índice 'idx:restaurantes' criado com sucesso! Resultado: " + SafeEncoder.encode((byte[]) result));

            // Insere documentos de exemplo
            jedis.hset("restaurante:1", Map.of(
                    "nome","Pizzaria Napoli",
                    "cozinha","italiana"
            ));

            jedis.hset("restaurante:2", Map.of(
                    "nome","Sushi House",
                    "cozinha","japonesa"
            ));

            System.out.println("✓ Documentos adicionados com sucesso!");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
