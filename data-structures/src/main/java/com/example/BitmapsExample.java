package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class BitmapsExample implements Example {

    @Override
    public void example(JedisPool pool) {
        System.out.println("\n=== Example de Bitmap ===");

        try (var jedis = pool.getResource()) {

            // Limpar dados anteriores para o exemplo
            jedis.del(
                    "online:*",
                    "feature:*",
                    "cliques:*"
            );

            System.out.println("\n1. Status Online de Usuários:");

            // SETBIT - Armazena 1 bit
            jedis.setbit("online", 15, true);    // Usuário 15 está online
            jedis.setbit("online", 23, true);    // Usuário 23 está online

            // GETBIT - Recupera 1 bit
            System.out.println("Usuário 15 online: " + jedis.getbit("online", 15));
            System.out.println("Usuário 99 online: " + jedis.getbit("online", 99));

            System.out.println("\n2. Controle de Acesso VIP:");

            // SETBIT - Define acesso VIP
            jedis.setbit("feature:VIP", 5, true);    // Usuário 5 tem acesso VIP
            jedis.setbit("feature:VIP", 8, true);    // Usuário 8 tem acesso VIP

            // GETBIT - Verifica acesso
            System.out.println("Usuário 5 tem VIP: " + jedis.getbit("feature:VIP", 5));
            System.out.println("Usuário 7 tem VIP: " + jedis.getbit("feature:VIP", 7));

            System.out.println("\n3. Estatísticas de Cliques:");

            // SETBIT - Registra cliques
            jedis.setbit("cliques:link1", 100, true);    // Usuário 100 clicou
            jedis.setbit("cliques:link1", 101, true);    // Usuário 101 clicou
            jedis.setbit("cliques:link1", 102, true);    // Usuário 102 clicou

            // BITCOUNT - Conta total de cliques
            long totalCliques = jedis.bitcount("cliques:link1");
            System.out.println("Total de cliques no link1: " + totalCliques);

            System.out.println("\n4. Dados do cache:");

            System.out.println("+ Online - usuário 15: " + jedis.getbit("online", 15));
            System.out.println("+ Online - usuário 23: " + jedis.getbit("online", 23));
            System.out.println("+ VIP - usuário 5: " + jedis.getbit("feature:VIP", 5));
            System.out.println("+ VIP - usuário 8: " + jedis.getbit("feature:VIP", 8));
            System.out.println("+ Cliques - usuário 100: " + jedis.getbit("cliques:link1", 100));

        } catch (Exception e) {
            System.err.println("Erro no exemplo de bitmaps: " + e.getMessage());
            e.printStackTrace();
        }
    }
}