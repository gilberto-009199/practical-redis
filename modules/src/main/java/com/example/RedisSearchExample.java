package com.example;

import redis.clients.jedis.JedisPool;

import redis.clients.jedis.util.SafeEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RedisSearchExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try(var jedis = pool.getResource()){

            System.out.println("\n=== Redis Search Example ===\n");

            // Executa FT.CREATE idx:restaurantes ...
            // @Atencao Não se enviar uma strinbg inteira ou bloco
            // precisa ser instrução por introdução
            var result = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.CREATE"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("ON"),
                    SafeEncoder.encode("HASH"),
                    SafeEncoder.encode("PREFIX"),
                    SafeEncoder.encode("1"),
                    SafeEncoder.encode("restaurante:"),
                    SafeEncoder.encode("SCHEMA"),
                    SafeEncoder.encode("nome"),
                    SafeEncoder.encode("TEXT"),
                    SafeEncoder.encode("cozinha"),
                    SafeEncoder.encode("TAG")
            );

            System.out.println("Índice 'idx:restaurantes' criado com sucesso! Resultado: " + SafeEncoder.encode((byte[]) result));

            jedis.hset("restaurante:1", Map.of(
                    "nome", "Pizzaria Napoli",
                    "cozinha", "italiana"
            ));
            jedis.hset("restaurante:2", Map.of(
                    "nome", "Macar Roma",
                    "cozinha", "italiana"
            ));

            jedis.hset("restaurante:3", Map.of(
                    "nome", "Italone & Pizzaria",
                    "cozinha", "italiana,francesa"
            ));

            jedis.hset("restaurante:4", Map.of(
                    "nome", "Sushi House",
                    "cozinha", "japonesa"
            ));

            jedis.hset("restaurante:5", Map.of(
                    "nome", "Teriak House",
                    "cozinha", "japonesa"
            ));

            jedis.hset("restaurante:6", Map.of(
                    "nome", "Pizzaria Japan",
                    "cozinha", "japonesa,italiana"
            ));

            jedis.hset("restaurante:7", Map.of(
                    "nome", "Sugar House",
                    "cozinha", "japonesa,coreana"
            ));

            // Adicionando mais exemplos para melhorar as buscas
            jedis.hset("restaurante:8", Map.of(
                    "nome", "Boulangerie Paris",
                    "cozinha", "francesa"
            ));

            jedis.hset("restaurante:9", Map.of(
                    "nome", "Crêperie Bretonne",
                    "cozinha", "francesa"
            ));

            jedis.hset("restaurante:10", Map.of(
                    "nome", "Gelateria Roma",
                    "cozinha", "italiana"
            ));

            System.out.println("Documentos adicionados com sucesso!");


            // Buscar comida japonesa
            System.out.println("\n--- Buscando comida: japonesa ---");
            var japonesaResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("@cozinha:{japonesa}")
            );
            printSearchResults(japonesaResult);

            // Buscar comida italiana
            System.out.println("\n--- Buscando comida: italiana ---");
            var italianaResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("@cozinha:{italiana}")
            );
            printSearchResults(italianaResult);

            // Buscar comida francesa
            System.out.println("\n--- Buscando comida: francesa ---");
            var francesaResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("@cozinha:{francesa}")
            );
            printSearchResults(francesaResult);

            // Buscar Pizza
            System.out.println("\n--- Buscando comida: pizzarias ---");
            var pizzaResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("pizzaria")
            );
            printSearchResults(pizzaResult);

            // Buscar Pizza Francesa
            System.out.println("\n--- Buscando comida: pizzaria francesa ---");
            var pizzaFrancesaResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("@cozinha:{francesa} @nome:pizzaria")
            );
            printSearchResults(pizzaFrancesaResult);

            // Buscar Sorvete/Gelato
            System.out.println("\n--- Buscando comida: gelat* ---");
            var sorveteResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("gelat*")
            );
            printSearchResults(sorveteResult);

            // Busca com múltiplas cozinhas
            System.out.println("\n--- Buscando comida: japonesa|coreana ---");
            var multiCozinhaResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("FT.SEARCH"),
                    SafeEncoder.encode("idx:restaurantes"),
                    SafeEncoder.encode("@cozinha:{japonesa|coreana}")
            );
            printSearchResults(multiCozinhaResult);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printSearchResults(Object result) {
        if (!(result instanceof List)) {
            System.out.println("Retorno não é uma lista! Tipo: " + result.getClass().getSimpleName());
            return;
        }

        List<Object> resultList = (List<Object>) result;

        if (resultList.isEmpty()) {
            System.out.println("Nenhum resultado encontrado.");
            return;
        }

        // O primeiro elemento é o total de resultados
        Long totalResults = (Long) resultList.get(0);
        System.out.println("Total de resultados: " + totalResults);

        if (totalResults == 0) {
            System.out.println("Nenhum resultado encontrado.");
            return;
        }

        // Os elementos seguintes são pares: key, fields
        for (int i = 1; i < resultList.size(); i += 2) {
            String key = SafeEncoder.encode((byte[]) resultList.get(i));
            List<Object> fields = (List<Object>) resultList.get(i + 1);

            System.out.println("---");
            System.out.println("ID: " + key);

            // Processar campos - converter para Map
            Map<String, String> restaurante = new HashMap<>();
            for (int j = 0; j < fields.size(); j += 2) {
                String fieldName = SafeEncoder.encode((byte[]) fields.get(j));
                String fieldValue = SafeEncoder.encode((byte[]) fields.get(j + 1));
                restaurante.put(fieldName, fieldValue);
            }


            var keys = restaurante.keySet().stream().collect(Collectors.joining(","));
            var values = restaurante.values().stream().collect(Collectors.joining(","));
            System.out.println("Resumo: ");
            System.out.println("\t chaves: "+ keys);
            System.out.println("\t valoes: "+ values);


        }
    }

}
