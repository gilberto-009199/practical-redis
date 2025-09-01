package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.List;

public class ListExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {


            System.out.println("\n=== Exemplos de List ===\n");

            // Limpar dados anteriores
            jedis.del(
                    "tarefas:*",
                    "chat:*",
                    "posts:*");

            // 1. Sistema de Fila de Tarefas
            System.out.println("1.  Sistema de Fila de Tarefas:");

            jedis.lpush("tarefas:pending", "processar_pedido_123");
            jedis.lpush("tarefas:pending", "enviar_email_456");
            jedis.lpush("tarefas:pending", "atualizar_db_789");

            System.out.println("Tarefas na fila: " + jedis.llen("tarefas:pending"));

            String tarefaProcessada = jedis.rpop("tarefas:pending");
            System.out.println("Tarefa processada: " + tarefaProcessada);
            System.out.println("Tarefas restantes: " + jedis.llen("tarefas:pending"));

            // 2. Histórico de Mensagens de Chat
            System.out.println("\n2.  Histórico de Mensagens de Chat:");

            jedis.rpush("chat:room1", "João: Olá pessoal!");
            jedis.rpush("chat:room1", "Maria: Tudo bem?");
            jedis.rpush("chat:room1", "Pedro: Como vocês estão?");
            jedis.rpush("chat:room1", "Ana: Alguém online?");

            List<String> mensagens = jedis.lrange("chat:room1", 0, 2);
            System.out.println("Últimas 3 mensagens:");
            for (int i = 0; i < mensagens.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + mensagens.get(i));
            }

            // Manter apenas as 3 mensagens mais recentes
            jedis.ltrim("chat:room1", 0, 2);
            System.out.println("Mensagens após LTRIM: " + jedis.llen("chat:room1"));

            // 3. Timeline de Posts Recentes
            System.out.println("\n3. Timeline de Posts Recentes:");

            jedis.lpush("posts:recent", "post_789");
            jedis.lpush("posts:recent", "post_456");
            jedis.lpush("posts:recent", "post_123");
            jedis.lpush("posts:recent", "post_000");
            jedis.lpush("posts:recent", "post_111");
            jedis.lpush("posts:recent", "post_222");

            List<String> postsRecentes = jedis.lrange("posts:recent", 0, 4);
            System.out.println("5 posts mais recentes:");
            for (int i = 0; i < postsRecentes.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + postsRecentes.get(i));
            }

            // Manter apenas os 5 posts mais recentes
            jedis.ltrim("posts:recent", 0, 4);
            System.out.println("Posts após LTRIM: " + jedis.llen("posts:recent"));

            // Demonstrando todas as operações
            System.out.println("\n Resumo Final:");
            System.out.println("+ Tarefas pendentes: " + jedis.llen("tarefas:pending"));
            System.out.println("+ Mensagens no chat: " + jedis.llen("chat:room1"));
            System.out.println("+ Posts na timeline: " + jedis.llen("posts:recent"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}