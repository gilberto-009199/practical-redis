package com.example;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.SafeEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisGraphExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try(var jedis = pool.getResource()){

            System.out.println("\n=== Redis Graph Example ===\n");

            // Criar nós (usuários)
            System.out.println("Criando usuários...");
            var createUsers = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode( "foodDelivery"),
                    // @Atenção Create precisa ser inteiro
                    SafeEncoder.encode("CREATE"+
                                           "(:Usuario {id: 1, nome: 'João', email: 'joao@email.com'}),"+
                                           "(:Usuario {id: 2, nome: 'Maria', email: 'maria@email.com'}),"+
                                           "(:Usuario {id: 3, nome: 'Pedro', email: 'pedro@email.com'})"
                    )
            );
            System.out.println(" Usuários criados: ");
            processGraphResult(createUsers);


            // Criar nós (restaurantes)
            System.out.println("Criando restaurantes...");
            var createRestaurants = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("CREATE " +
                            "(:Restaurante {id: 101, nome: 'Pizza Hut', cozinha: 'italiana'}), " +
                            "(:Restaurante {id: 102, nome: 'Sushi Palace', cozinha: 'japonesa'}), " +
                            "(:Restaurante {id: 103, nome: 'Burger King', cozinha: 'americana'}), " +
                            "(:Restaurante {id: 104, nome: 'Taco Bell', cozinha: 'mexicana'}), " +
                            "(:Restaurante {id: 105, nome: 'Pasta Factory', cozinha: 'italiana'})")
            );
            System.out.println(" Restaurantes criados");
            processGraphResult(createRestaurants);



            // Criar relacionamentos de favoritos
            System.out.println("Criando relacionamentos de favoritos...");
            var createRelationships = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (u1:Usuario {id: 1}), (u2:Usuario {id: 2}), (u3:Usuario {id: 3}), " +
                            "(r1:Restaurante {id: 101}), (r2:Restaurante {id: 102}), " +
                            "(r3:Restaurante {id: 103}), (r4:Restaurante {id: 104}), (r5:Restaurante {id: 105}) " +
                            "CREATE " +
                            "(u1)-[:FAVORITO {desde: '2024-01-01', rating: 5}]->(r1), " +
                            "(u1)-[:FAVORITO {desde: '2024-02-15', rating: 4}]->(r2), " +
                            "(u2)-[:FAVORITO {desde: '2024-01-10', rating: 5}]->(r1), " +
                            "(u2)-[:FAVORITO {desde: '2024-03-01', rating: 4}]->(r5), " +
                            "(u3)-[:FAVORITO {desde: '2024-02-01', rating: 5}]->(r2), " +
                            "(u3)-[:FAVORITO {desde: '2024-02-20', rating: 3}]->(r3), " +
                            "(u3)-[:FAVORITO {desde: '2024-03-05', rating: 4}]->(r4)")
            );
            System.out.println(" Relacionamentos criados");
            processGraphResult(createRelationships);



            // Consulta 1: Restaurantes favoritos de um usuário
            System.out.println("\n1. Restaurantes favoritos do João:");
            var userFavorites = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (u:Usuario {nome: 'João'})-[:FAVORITO]->(r:Restaurante) " +
                            "RETURN r.nome, r.cozinha, r.id ORDER BY r.nome")
            );
            processGraphResult(userFavorites);


            // Consulta 2: Todos os usuários que gostam de um restaurante específico
            System.out.println("\n2. Usuários que gostam do Pizza Hut:");
            var restaurantLovers = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (u:Usuario)-[:FAVORITO]->(r:Restaurante {nome: 'Pizza Hut'}) " +
                            "RETURN u.nome, u.email ORDER BY u.nome")
            );
            processGraphResult(restaurantLovers);

            // Consulta 3: Recomendações baseadas em relacionamentos
            System.out.println("\n3. Recomendações para o João (baseado nos gostos de usuários similares):");
            var recommendations = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (joao:Usuario {nome: 'João'})-[:FAVORITO]->(r:Restaurante)<-[:FAVORITO]-(outro:Usuario) " +
                            "MATCH (outro)-[:FAVORITO]->(recomendacao:Restaurante) " +
                            "WHERE NOT (joao)-[:FAVORITO]->(recomendacao) " +
                            "RETURN DISTINCT recomendacao.nome, recomendacao.cozinha, COUNT(*) as score " +
                            "ORDER BY score DESC LIMIT 5")
            );
            processGraphResult(recommendations);

            // Consulta 4: Restaurantes por tipo de cozinha
            System.out.println("\n4. Todos os restaurantes italianos:");
            var italianRestaurants = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (r:Restaurante {cozinha: 'italiana'}) " +
                            "RETURN r.nome, r.id ORDER BY r.nome")
            );
            processGraphResult(italianRestaurants);

            // Consulta 5: Estatísticas do grafo
            System.out.println("\n5. Estatísticas do grafo:");
            var stats = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (u:Usuario) RETURN COUNT(u) as total_usuarios")
            );
            processGraphResult(stats);

            var stats2 = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH (r:Restaurante) RETURN COUNT(r) as total_restaurantes")
            );
            processGraphResult(stats2);

            var stats3 = jedis.sendCommand(
                    () -> SafeEncoder.encode("GRAPH.QUERY"),
                    SafeEncoder.encode("foodDelivery"),
                    SafeEncoder.encode("MATCH ()-[f:FAVORITO]->() RETURN COUNT(f) as total_favoritos")
            );
            processGraphResult(stats3);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processGraphResult(Object result) {
        if (!(result instanceof List)) {
            System.out.println("\t Retorno não é uma lista! Tipo: " + result.getClass().getSimpleName());
            return;
        }

        var resultList = (List<Object>) result;

        if (resultList.isEmpty()) {
            System.out.println("\t Nenhum resultado encontrado.");
            return;
        }


        // Processar cada item do resultado
        for (int i = 0; i < resultList.size(); i++) {
            var item = (List<Object>) resultList.get(i);
            for (int j = 0; j < item.size(); j++) {
                var item_value = item.get(j);

                if (item_value instanceof byte[]) {
                    // É uma string simples
                    var data = SafeEncoder.encode((byte[]) item_value);
                    System.out.println("\t Dado: " + data);
                } else if (item_value instanceof List) {
                    // É uma lista (pode ser uma linha de resultados)

                    processGraphResult(item);

                } else {
                    // Outro tipo de dado
                    System.out.println("\t Item [" + i + "][" + j + "]: " + item_value + " (" + item_value.getClass().getSimpleName() + ")");
                }

            }
        }

    }


}