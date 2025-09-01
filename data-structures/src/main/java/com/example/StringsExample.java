package com.example;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.UnifiedJedis;

public class StringsExample implements Example {

    @Override
    public void example(JedisPool pool) {
        System.out.println("\n=== Exemplos de String ===\n");

        try (var jedis = pool.getResource()) {

            // Limpar dados anteriores
            jedis.del(
                    "nome:*",
                    "visitas:*",
                    "preco:*",
                    "produto:*",
                    "visitas:*",
                    "contador_pedidos:*",
                    "estoque:*",
                    "preco:*",
                    "saldo:*",
                    "usuario:*"
            );

            // SET - Armazena valores simples
            jedis.set("nome", "gil");
            jedis.set("visitas", "100");
            jedis.set("preco", "10.10");
            jedis.set("produto:1:nome", "Notebook Gamer");
            jedis.set("produto:1:preco", "2500.00");

            // INCR - Incrementa em 1 (valor inteiro)
            jedis.incr("visitas");
            jedis.incr("visitas");

            // INCRBY - Incrementa por um valor específico (inteiro)
            jedis.incrBy("visitas", 5);
            jedis.incrBy("contador_pedidos", 3);

            // DECR - Decrementa em 1 (valor inteiro)
            jedis.decr("estoque");
            jedis.decr("estoque");

            // DECRBY - Decrementa por um valor específico (inteiro)
            jedis.decrBy("estoque", 5);

            // INCRBYFLOAT - Incrementa por um valor float/decimal
            jedis.incrByFloat("preco", 2.50);
            jedis.incrByFloat("saldo", 100.75);

            // GET - Recupera valores
            var nome = jedis.get("nome");
            var visitas = jedis.get("visitas");
            var preco = jedis.get("preco");
            var estoque = jedis.get("estoque");
            var saldo = jedis.get("saldo");
            var contadorPedidos = jedis.get("contador_pedidos");
            var produtoNome = jedis.get("produto:1:nome");
            var produtoPreco = jedis.get("produto:1:preco");

            // MSET - Define múltiplos valores de uma vez
            jedis.mset(
                    "usuario:1:nome", "Ana Silva",
                    "usuario:1:email", "ana@email.com",
                    "usuario:1:idade", "28"
            );

            // MGET - Recupera múltiplos valores de uma vez
            var usuarioDados = jedis.mget("usuario:1:nome", "usuario:1:email", "usuario:1:idade");

            // STRLEN - Obtém o tamanho da string
            var tamanhoNome = jedis.strlen("nome");

            // APPEND - Adiciona texto ao final de uma string existente
            jedis.append("nome", "berto"); // Nome se torna "gilberto"

            // GETRANGE - Obtém parte de uma string
            var parteNome = jedis.getrange("nome", 0, 2); // "gil"

            // SETRANGE - Substitui parte de uma string
            jedis.setrange("nome", 3, "son"); // Nome se torna "gilson"

            nome = jedis.get("nome");

            System.out.println("Dados do cache:");
            System.out.printf("+ Nome: %s \n", nome);
            System.out.printf("+ Visitas: %s \n", visitas);
            System.out.printf("+ Preço: %s \n", preco);
            System.out.printf("+ Estoque: %s \n", estoque);
            System.out.printf("+ Saldo: %s \n", saldo);
            System.out.printf("+ Contador de Pedidos: %s \n", contadorPedidos);
            System.out.printf("+ Produto: %s - R$ %s \n", produtoNome, produtoPreco);
            System.out.printf("+ Tamanho do nome: %d \n", tamanhoNome);
            System.out.printf("+ Parte do nome (0-2): %s \n", parteNome);

            System.out.println("\nDados do usuário (MSET/MGET):");
            for (int i = 0; i < usuarioDados.size(); i++) {
                System.out.printf("\t %s \n", usuarioDados.get(i));
            }

            // Comandos com tempo de expiração
            jedis.setex("sessao:usuario:123", 300, "token_abc123"); // Expira em 300 segundos (5 minutos)
            jedis.psetex("sessao_temporaria", 60000, "dados_temporarios"); // Expira em 60000 milissegundos (1 minuto)

            // SETNX - Define valor apenas se a chave não existir
            Long resultadoSetNx = jedis.setnx("chave_unica", "valor_unico");
            System.out.printf("\t SETNX resultado: %d (1=definido, 0=já existia) \n", resultadoSetNx);

            // GETSET - Define novo valor e retorna o valor antigo
            String antigoValor = jedis.getSet("contador", "1000");
            System.out.printf("\t GETSET - Valor antigo: %s, Novo valor: %s \n", antigoValor, jedis.get("contador"));

        } catch (Exception e) {
            System.err.println("Erro no exemplo de strings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}