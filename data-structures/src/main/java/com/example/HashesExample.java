package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.Map;

public class HashesExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {
            Jedis redis = jedis;

            System.out.println("\n=== Exemplos de Hashes ===\n");

            // Limpar dados anteriores
            redis.del(
                    "usuario",
                    "produto",
                    "carrinho",
                    "config",
                    "sessao"
            );

            // 1. Perfil de Usuário
            System.out.println("1. Perfil de Usuário:");
            redis.hset("usuario:123", Map.of(
                    "nome", "João Silva",
                    "email", "joao@email.com",
                    "idade", "28",
                    "cidade", "São Paulo"
            ));

            String nome = redis.hget("usuario:123", "nome");
            Map<String, String> usuarioCompleto = redis.hgetAll("usuario:123");
            redis.hincrBy("usuario:123", "idade", 1);
            redis.hdel("usuario:123", "cidade");

            System.out.println("Nome: " + nome);
            System.out.println("Perfil completo: " + usuarioCompleto);
            System.out.println("Idade após incremento: " + redis.hget("usuario:123", "idade"));

            // 2. Produto em E-commerce
            System.out.println("\n2. Produto em E-commerce:");
            redis.hset("produto:789", Map.of(
                    "nome", "Smartphone XYZ",
                    "preco", "999.90",
                    "estoque", "15",
                    "marca", "Samsung"
            ));

            String preco = redis.hget("produto:789", "preco");
            redis.hincrBy("produto:789", "estoque", -1);
            redis.hset("produto:789", "preco", "899.90");

            System.out.println("Preço original: " + preco);
            System.out.println("Preço atualizado: " + redis.hget("produto:789", "preco"));
            System.out.println("Estoque após venda: " + redis.hget("produto:789", "estoque"));
            System.out.println("Campos do produto: " + redis.hkeys("produto:789"));

            // 3. Carrinho de Compras
            System.out.println("\n3. Carrinho de Compras:");
            redis.hset("carrinho:usuario:555", "produto:789", "2");
            redis.hset("carrinho:usuario:555", "produto:456", "1");
            redis.hincrBy("carrinho:usuario:555", "produto:789", 1);

            Map<String, String> carrinho = redis.hgetAll("carrinho:usuario:555");
            System.out.println("Carrinho completo: " + carrinho);

            redis.hdel("carrinho:usuario:555", "produto:456");
            System.out.println("Carrinho após remoção: " + redis.hgetAll("carrinho:usuario:555"));

            // 4. Configurações de Aplicação
            System.out.println("\n4. Configurações de Aplicação:");
            redis.hset("config:app", Map.of(
                    "tema", "escuro",
                    "idioma", "pt-BR",
                    "notificacoes", "1",
                    "timeout", "30"
            ));

            String tema = redis.hget("config:app", "tema");
            boolean temaAlterado = redis.hsetnx("config:app", "tema", "claro") == 0;
            redis.hset("config:app", "tema", "claro");

            System.out.println("Tema original: " + tema);
            System.out.println("Tentativa de alterar com HSETNX: " + temaAlterado);
            System.out.println("Tema após HSET: " + redis.hget("config:app", "tema"));

            // 5. Estatísticas de Sessão
            System.out.println("\n5. Estatísticas de Sessão:");
            redis.hset("sessao:abc", Map.of(
                    "pagina_views", "15",
                    "tempo_total", "3600",
                    "ultima_pagina", "/home"
            ));

            redis.hincrBy("sessao:abc", "pagina_views", 1);
            redis.hincrBy("sessao:abc", "tempo_total", 120);
            String ultimaPagina = redis.hget("sessao:abc", "ultima_pagina");

            System.out.println("Página views incrementada: " + redis.hget("sessao:abc", "pagina_views"));
            System.out.println("Tempo total atualizado: " + redis.hget("sessao:abc", "tempo_total"));
            System.out.println("Última página: " + ultimaPagina);

            // Resumo final
            System.out.println("\n Resumo Final:");
            System.out.println("Tamanho do hash usuário: " + redis.hlen("usuario:123"));
            System.out.println("Valores do produto: " + redis.hvals("produto:789"));
            System.out.println("Configurações da app: " + redis.hgetAll("config:app"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}