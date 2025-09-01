package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.util.Set;

public class SetsSortedExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {

            System.out.println("\n=== Exemplos de Sorted Sets ===\n");

            // Limpar dados anteriores
            jedis.del(
                    "ranking",
                    "vendas",
                    "tarefas",
                    "response",
                    "ranking"
            );

            // 1. Ranking de Jogadores
            System.out.println("1. Ranking de Jogadores:");
            jedis.zadd("ranking", 1000, "player1");
            jedis.zadd("ranking", 1500, "player2");
            jedis.zadd("ranking", 800, "player3");

            jedis.zincrby("ranking", 200, "player3"); // player3 ganha 200 pontos

            var top3 = jedis.zrevrangeWithScores("ranking", 0, 2);
            System.out.println("TOP 3 jogadores:");
            for (Tuple player : top3) {
                System.out.println("  " + player.getElement() + ": " + (int)player.getScore() + " pontos");
            }

            var todosOrdenados = jedis.zrangeWithScores("ranking", 0, -1);
            System.out.println("Todos os jogadores (ordem crescente): " + todosOrdenados);

            var posicaoPlayer2 = jedis.zrevrank("ranking", "player2");
            System.out.println("Posição do player2: " + (posicaoPlayer2 + 1) + "º lugar");

            // 2. Leaderboard de Produtos Mais Vendidos
            System.out.println("\n2. Leaderboard de Produtos Mais Vendidos:");
            jedis.zadd("vendas:semana", 150, "produto:A");
            jedis.zadd("vendas:semana", 89, "produto:B");
            jedis.zadd("vendas:semana", 200, "produto:C");

            jedis.zincrby("vendas:semana", 50, "produto:B");

            var topVendas = jedis.zrevrangeWithScores("vendas:semana", 0, 4);
            System.out.println("TOP 5 produtos mais vendidos:");
            for (Tuple produto : topVendas) {
                System.out.println("  " + produto.getElement() + ": " + (int)produto.getScore() + " vendas");
            }

            var vendasProdutoC = jedis.zscore("vendas:semana", "produto:C");
            System.out.println("Vendas do produto C: " + vendasProdutoC.intValue());

            // 3. Sistema de Prioridade de Tarefas
            System.out.println("\n3. Sistema de Prioridade de Tarefas:");
            jedis.zadd("tarefas:prioridade", 1, "tarefa:baixa");
            jedis.zadd("tarefas:prioridade", 3, "tarefa:media");
            jedis.zadd("tarefas:prioridade", 5, "tarefa:alta");
            jedis.zadd("tarefas:prioridade", 10, "tarefa:critica");

            var tarefasPrioritarias = jedis.zrangeByScore("tarefas:prioridade", 5, 10);
            System.out.println("Tarefas prioritárias (5-10): " + tarefasPrioritarias);

            var tarefaMaisPrioritaria = jedis.zpopmax("tarefas:prioridade");

            System.out.println("Tarefa mais prioritária: " + tarefaMaisPrioritaria.getElement() + " (prioridade: " + tarefaMaisPrioritaria.getScore() + ")");


            // 4. Ranking por Tempo de Resposta
            System.out.println("\n4. Ranking por Tempo de Resposta:");
            jedis.zadd("response:times", 120, "api:login");
            jedis.zadd("response:times", 45, "api:search");
            jedis.zadd("response:times", 300, "api:report");

            jedis.zincrby("response:times", -10, "api:login");

            var apisMaisLentas = jedis.zrevrangeByScoreWithScores("response:times", 500, 100);
            System.out.println("APIs mais lentas (100-500ms):");
            for (Tuple api : apisMaisLentas) {
                System.out.println("  " + api.getElement() + ": " + api.getScore() + "ms");
            }

            // 5. Sistema de Pontuação por Categoria
            System.out.println("\n5. Sistema de Pontuação por Categoria:");
            jedis.zadd("ranking:arcade", 5000, "user:john");
            jedis.zadd("ranking:arcade", 7000, "user:sarah");

            jedis.zadd("ranking:strategy", 3000, "user:john");
            jedis.zadd("ranking:strategy", 6000, "user:mike");

            jedis.zunionstore("ranking:total", "ranking:arcade", "ranking:strategy");

            var rankingCompleto = jedis.zrevrangeWithScores("ranking:total", 0, -1);
            System.out.println("Ranking combinado:");
            for (Tuple usuario : rankingCompleto) {
                System.out.println("  " + usuario.getElement() + ": " + (int)usuario.getScore() + " pontos");
            }

            // Resumo final
            System.out.println("\n Resumo Final:");
            System.out.println("Total jogadores no ranking: " + jedis.zcard("ranking"));
            System.out.println("Produto com mais vendas: " + jedis.zrevrange("vendas:semana", 0, 0));
            System.out.println("Tarefas restantes: " + jedis.zcard("tarefas:prioridade"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}