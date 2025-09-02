package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.args.GeoUnit;
import java.util.List;

public class GeospatialExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {

            System.out.println("=== GeoSpatial Example ===");

            // Limpar dados anteriores
            jedis.del("restaurantes:sp");

            // 1. Adicionar localizações
            System.out.println("\n1. Adicionando localizações...");
            jedis.geoadd("restaurantes:sp",
                    -23.5505, -46.6333,
                    "Madero");
            jedis.geoadd("restaurantes:sp",
                    -23.5637, -46.6520,
                    "Fogo de Chao");
            jedis.geoadd("restaurantes:sp",
                    -23.5560, -46.6610,
                    "Outback");
            jedis.geoadd("restaurantes:sp",
                    -23.5440, -46.6390,
                    "Applebee's");
            jedis.geoadd("restaurantes:sp",
                    -23.5700, -46.6450,
                    "China in Box");

            System.out.println(" Restaurantes adicionados!");

            // 2. Buscar restaurantes próximos a um ponto (10km de raio)
            System.out.println("\n2. Buscando restaurantes próximos ao centro de SP...");
            var restaurantesProximos = jedis.georadius(
                    "restaurantes:sp",
                    -23.5505, -46.6333,  // Centro de São Paulo
                    10,                  // Raio de 10km
                    GeoUnit.KM,
                    redis.clients.jedis.params.GeoRadiusParam.geoRadiusParam()
                            .withDist()      // Incluir distância
                            .sortAscending() // Ordenar por proximidade
            );

            System.out.println(" Restaurantes em 10km do centro:");
            for (var restaurante : restaurantesProximos) {
                System.out.println("   " + restaurante.getMemberByString() +
                        " - " + String.format("%.2f", restaurante.getDistance()) + "km");
            }

            // 3. Buscar por membro mais próximo
            System.out.println("\n3. Buscando restaurantes próximos ao Madero...");
            var proximosAoMadero = jedis.georadiusByMember(
                    "restaurantes:sp",
                    "Madero",            // Ponto de referência
                    5,                   // Raio de 5km
                    GeoUnit.KM,
                    redis.clients.jedis.params.GeoRadiusParam.geoRadiusParam()
                            .withDist()      // Incluir distância
                            .sortAscending() // Ordenar por proximidade
            );

            System.out.println(" Restaurantes em 5km do Madero:");
            for (var restaurante : proximosAoMadero) {
                if (!restaurante.getMemberByString().equals("Madero")) {
                    System.out.println("   " + restaurante.getMemberByString() +
                            " - " + String.format("%.2f", restaurante.getDistance()) + "km");
                }
            }

            // 4. Calcular distância entre dois pontos
            System.out.println("\n4. Calculando distância entre Madero e Fogo de Chao...");
            var distancia = jedis.geodist("restaurantes:sp", "Madero", "Fogo de Chao", GeoUnit.KM);
            if (distancia != null) {
                System.out.println("   Distância: " + String.format("%.2f", distancia) + "km");
            }

            // 5. Obter coordenadas de um membro
            System.out.println("\n5. Obtendo coordenadas do Madero...");
            var coordenadas = jedis.geopos("restaurantes:sp", "Madero");
            if (!coordenadas.isEmpty() && coordenadas.get(0) != null) {
                var coord = coordenadas.get(0);
                System.out.println("   Latitude: " + coord.getLatitude());
                System.out.println("   Longitude: " + coord.getLongitude());
            }

            // 6. Obter Geohash de um membro
            System.out.println("\n6. Obtendo Geohash do Madero...");
            var geohashes = jedis.geohash("restaurantes:sp", "Madero");
            if (!geohashes.isEmpty()) {
                System.out.println("   Geohash: " + geohashes.get(0));
            }

            // 7. Buscar com limite de resultados
            System.out.println("\n7. Top 3 restaurantes mais próximos do centro:");
            var top3 = jedis.georadius(
                    "restaurantes:sp",
                    -23.5505, -46.6333,
                    50, // Raio grande para pegar todos
                    GeoUnit.KM,
                    redis.clients.jedis.params.GeoRadiusParam.geoRadiusParam()
                            .withDist()
                            .sortAscending()
                            .count(3) // Apenas 3 resultados
            );

            for (var restaurante : top3) {
                System.out.println("   " + restaurante.getMemberByString() +
                        " - " + String.format("%.2f", restaurante.getDistance()) + "km");
            }

            System.out.println("\n Demonstração Geospatial concluída!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}