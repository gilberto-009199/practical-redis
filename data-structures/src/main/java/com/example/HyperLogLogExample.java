package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class HyperLogLogExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {

            System.out.println("\n=== Exemplos de HyperLogLog ===\n");

            // Limpar dados anteriores
            jedis.del(
                    "visitantes:*",
                    "visualizacoes:*",
                    "cliques:*",
                    "campanha:*"
            );


            // 1. Visitantes Únicos de um Site
            System.out.println("1. Visitantes Únicos de um Site:");
            jedis.pfadd("visitantes:dia:2023-10-01", "user:123", "user:456", "user:789");
            jedis.pfadd("visitantes:dia:2023-10-01", "user:123", "user:999"); // user:123 duplicado

            long visitantesDia1 = jedis.pfcount("visitantes:dia:2023-10-01");
            System.out.println("Visitantes únicos no dia 01/10/2023: ≈" + visitantesDia1);

            // 2. Visitantes Únicos da Semana
            System.out.println("\n2. Visitantes Únicos da Semana:");
            jedis.pfadd("visitantes:dia:2023-10-02", "user:123", "user:777", "user:888");
            jedis.pfadd("visitantes:dia:2023-10-03", "user:456", "user:999", "user:111");

            jedis.pfmerge("visitantes:semana", "visitantes:dia:2023-10-01",
                    "visitantes:dia:2023-10-02", "visitantes:dia:2023-10-03");

            long visitantesSemana = jedis.pfcount("visitantes:semana");
            System.out.println("Visitantes únicos na semana: ≈" + visitantesSemana);



            // 3. Visualizações Únicas de Posts
            System.out.println("\n3. Visualizações Únicas de Posts:");
            jedis.pfadd("visualizacoes:post:123", "user:100", "user:101", "user:102");
            jedis.pfadd("visualizacoes:post:123", "user:100", "user:103"); // user:100 duplicado

            long visualizacoesPost = jedis.pfcount("visualizacoes:post:123");
            System.out.println("Visualizações únicas do post 123: ≈" + visualizacoesPost);



            // 4. Cliques Únicos em Campanhas
            System.out.println("\n4. Cliques Únicos em Campanhas:");
            jedis.pfadd("cliques:campanha:abc", "ip:192.168.1.1", "ip:192.168.1.2", "ip:192.168.1.3");
            jedis.pfadd("cliques:campanha:abc", "ip:192.168.1.1", "ip:192.168.1.4"); // ip duplicado

            long cliquesUnicos = jedis.pfcount("cliques:campanha:abc");
            System.out.println("Cliques únicos na campanha ABC: ≈" + cliquesUnicos);

            // 5. Comparação entre Campanhas
            System.out.println("\n5. Comparação entre Campanhas:");
            jedis.pfadd("campanha:A", "user:1", "user:2", "user:3", "user:4");
            jedis.pfadd("campanha:B", "user:3", "user:4", "user:5", "user:6");

            jedis.pfmerge("campanhas:total", "campanha:A", "campanha:B");

            long campanhaACount = jedis.pfcount("campanha:A");
            long campanhaBCount = jedis.pfcount("campanha:B");
            long totalCount = jedis.pfcount("campanhas:total");

            System.out.println("Usuários únicos Campanha A: ≈" + campanhaACount);
            System.out.println("Usuários únicos Campanha B: ≈" + campanhaBCount);
            System.out.println("Usuários únicos totais (A + B): ≈" + totalCount);

            // Demonstração da precisão aproximada
            System.out.println("\n6. Precisão Aproximada do HyperLogLog:");
            System.out.println("+ Cada HyperLogLog usa apenas ~12KB de memória");
            System.out.println("+ Precisão de aproximadamente 99%");
            System.out.println("+ Ideal para grandes volumes de dados");

            // Mostrar uso de memória aproximado
            long memoryUsage = jedis.memoryUsage("visitantes:dia:2023-10-01");
            System.out.println("Uso de memória aproximado por HyperLogLog: " + memoryUsage + " bytes");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}