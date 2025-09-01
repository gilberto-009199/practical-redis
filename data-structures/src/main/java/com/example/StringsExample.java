package com.example;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.UnifiedJedis;

public class StringsExample implements Example {

    @Override
    public void example(JedisPool pool) {
        System.out.println("\n=== Exemplos de String ===\n");

        try (var redis = pool.getResource()) {

            // SET - Armazena valores simples
            redis.set("nome", "gil");
            redis.set("visitas", "100");
            redis.set("preco", "10.10");
            redis.set("produto:1:nome", "Notebook Gamer");
            redis.set("produto:1:preco", "2500.00");

            // INCR - Incrementa em 1 (valor inteiro)
            redis.incr("visitas");
            redis.incr("visitas");

            // INCRBY - Incrementa por um valor específico (inteiro)
            redis.incrBy("visitas", 5);
            redis.incrBy("contador_pedidos", 3);

            // DECR - Decrementa em 1 (valor inteiro)
            redis.decr("estoque");
            redis.decr("estoque");

            // DECRBY - Decrementa por um valor específico (inteiro)
            redis.decrBy("estoque", 5);

            // INCRBYFLOAT - Incrementa por um valor float/decimal
            redis.incrByFloat("preco", 2.50);
            redis.incrByFloat("saldo", 100.75);

            // GET - Recupera valores
            var nome = redis.get("nome");
            var visitas = redis.get("visitas");
            var preco = redis.get("preco");
            var estoque = redis.get("estoque");
            var saldo = redis.get("saldo");
            var contadorPedidos = redis.get("contador_pedidos");
            var produtoNome = redis.get("produto:1:nome");
            var produtoPreco = redis.get("produto:1:preco");

            // MSET - Define múltiplos valores de uma vez
            redis.mset(
                    "usuario:1:nome", "Ana Silva",
                    "usuario:1:email", "ana@email.com",
                    "usuario:1:idade", "28"
            );

            // MGET - Recupera múltiplos valores de uma vez
            var usuarioDados = redis.mget("usuario:1:nome", "usuario:1:email", "usuario:1:idade");

            // STRLEN - Obtém o tamanho da string
            var tamanhoNome = redis.strlen("nome");

            // APPEND - Adiciona texto ao final de uma string existente
            redis.append("nome", "berto"); // Nome se torna "gilberto"

            // GETRANGE - Obtém parte de uma string
            var parteNome = redis.getrange("nome", 0, 2); // "gil"

            // SETRANGE - Substitui parte de uma string
            redis.setrange("nome", 3, "son"); // Nome se torna "gilson"

            nome = redis.get("nome");

            System.out.println("Dados do cache:");
            System.out.printf("\t Nome: %s \n", nome);
            System.out.printf("\t Visitas: %s \n", visitas);
            System.out.printf("\t Preço: %s \n", preco);
            System.out.printf("\t Estoque: %s \n", estoque);
            System.out.printf("\t Saldo: %s \n", saldo);
            System.out.printf("\t Contador de Pedidos: %s \n", contadorPedidos);
            System.out.printf("\t Produto: %s - R$ %s \n", produtoNome, produtoPreco);
            System.out.printf("\t Tamanho do nome: %d \n", tamanhoNome);
            System.out.printf("\t Parte do nome (0-2): %s \n", parteNome);

            System.out.println("\nDados do usuário (MSET/MGET):");
            for (int i = 0; i < usuarioDados.size(); i++) {
                System.out.printf("\t %s \n", usuarioDados.get(i));
            }

            // Comandos com tempo de expiração
            redis.setex("sessao:usuario:123", 300, "token_abc123"); // Expira em 300 segundos (5 minutos)
            redis.psetex("sessao_temporaria", 60000, "dados_temporarios"); // Expira em 60000 milissegundos (1 minuto)

            // SETNX - Define valor apenas se a chave não existir
            Long resultadoSetNx = redis.setnx("chave_unica", "valor_unico");
            System.out.printf("\t SETNX resultado: %d (1=definido, 0=já existia) \n", resultadoSetNx);

            // GETSET - Define novo valor e retorna o valor antigo
            String antigoValor = redis.getSet("contador", "1000");
            System.out.printf("\t GETSET - Valor antigo: %s, Novo valor: %s \n", antigoValor, redis.get("contador"));

        } catch (Exception e) {
            System.err.println("Erro no exemplo de strings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}