package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.List;

public class ListExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {
            Jedis redis = jedis;

            System.out.println("\n=== Exemplos de List ===\n");

            // Limpar dados anteriores
            redis.del("tarefas:pending", "chat:room1", "posts:recent");

            // 1. Sistema de Fila de Tarefas
            System.out.println("1.  Sistema de Fila de Tarefas:");

            redis.lpush("tarefas:pending", "processar_pedido_123");
            redis.lpush("tarefas:pending", "enviar_email_456");
            redis.lpush("tarefas:pending", "atualizar_db_789");

            System.out.println("Tarefas na fila: " + redis.llen("tarefas:pending"));

            String tarefaProcessada = redis.rpop("tarefas:pending");
            System.out.println("Tarefa processada: " + tarefaProcessada);
            System.out.println("Tarefas restantes: " + redis.llen("tarefas:pending"));

            // 2. Histórico de Mensagens de Chat
            System.out.println("\n2.  Histórico de Mensagens de Chat:");

            redis.rpush("chat:room1", "João: Olá pessoal!");
            redis.rpush("chat:room1", "Maria: Tudo bem?");
            redis.rpush("chat:room1", "Pedro: Como vocês estão?");
            redis.rpush("chat:room1", "Ana: Alguém online?");

            List<String> mensagens = redis.lrange("chat:room1", 0, 2);
            System.out.println("Últimas 3 mensagens:");
            for (int i = 0; i < mensagens.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + mensagens.get(i));
            }

            // Manter apenas as 3 mensagens mais recentes
            redis.ltrim("chat:room1", 0, 2);
            System.out.println("Mensagens após LTRIM: " + redis.llen("chat:room1"));

            // 3. Timeline de Posts Recentes
            System.out.println("\n3. Timeline de Posts Recentes:");

            redis.lpush("posts:recent", "post_789");
            redis.lpush("posts:recent", "post_456");
            redis.lpush("posts:recent", "post_123");
            redis.lpush("posts:recent", "post_000");
            redis.lpush("posts:recent", "post_111");
            redis.lpush("posts:recent", "post_222");

            List<String> postsRecentes = redis.lrange("posts:recent", 0, 4);
            System.out.println("5 posts mais recentes:");
            for (int i = 0; i < postsRecentes.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + postsRecentes.get(i));
            }

            // Manter apenas os 5 posts mais recentes
            redis.ltrim("posts:recent", 0, 4);
            System.out.println("Posts após LTRIM: " + redis.llen("posts:recent"));

            // Demonstrando todas as operações
            System.out.println("\n Resumo Final:");
            System.out.println("Tarefas pendentes: " + redis.llen("tarefas:pending"));
            System.out.println("Mensagens no chat: " + redis.llen("chat:room1"));
            System.out.println("Posts na timeline: " + redis.llen("posts:recent"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}