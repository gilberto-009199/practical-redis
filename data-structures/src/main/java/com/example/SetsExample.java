package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.List;
import java.util.Set;

public class SetsExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {

            System.out.println("\n=== Exemplos de Sets ===\n");

            // Limpar dados anteriores
            jedis.del(
                    "seguidores",
                    "artigo:",
                    "artigo:",
                    "grupo",
                    "bloqueados",
                    "interesses",
                    "interesses"
            );

            // 1. Sistema de Seguidores
            System.out.println("1. Sistema de Seguidores:");
            jedis.sadd("seguidores:maria", "joao", "pedro", "ana");
            jedis.sadd("seguidores:maria", "carla");

            boolean joaoSegueMaria = jedis.sismember("seguidores:maria", "joao");
            Set<String> seguidoresMaria = jedis.smembers("seguidores:maria");
            long totalSeguidores = jedis.scard("seguidores:maria");

            System.out.println("João segue Maria: " + joaoSegueMaria);
            System.out.println("Seguidores de Maria: " + seguidoresMaria);
            System.out.println("Total de seguidores: " + totalSeguidores);

            // 2. Sistema de Tags de Artigos
            System.out.println("\n2.️ Sistema de Tags de Artigos:");
            jedis.sadd("artigo:123:tags", "tecnologia", "python", "programacao");
            jedis.sadd("artigo:456:tags", "tecnologia", "redis", "banco-de-dados");

            Set<String> tagsComuns = jedis.sinter("artigo:123:tags", "artigo:456:tags");
            Set<String> todasTags = jedis.sunion("artigo:123:tags", "artigo:456:tags");

            System.out.println("Tags em comum: " + tagsComuns);
            System.out.println("Todas as tags únicas: " + todasTags);

            // 3. Membros de Grupos/Categorias
            System.out.println("\n3. Membros de Grupos/Categorias:");
            jedis.sadd("grupo:premium", "usuario:789", "usuario:456");
            jedis.sadd("grupo:vip", "usuario:123", "usuario:789");

            boolean usuarioPremium = jedis.sismember("grupo:premium", "usuario:789");
            Set<String> usuariosPremiumEVip = jedis.sinter("grupo:premium", "grupo:vip");

            System.out.println("Usuário 789 é premium: " + usuarioPremium);
            System.out.println("Usuários premium E vip: " + usuariosPremiumEVip);

            // 4. Sistema de Bloqueios
            System.out.println("\n4. Sistema de Bloqueios:");
            jedis.sadd("bloqueados:usuario:555", "usuario:777", "usuario:888");

            boolean usuarioBloqueado = jedis.sismember("bloqueados:usuario:555", "usuario:777");
            System.out.println("Usuário 777 está bloqueado: " + usuarioBloqueado);

            jedis.srem("bloqueados:usuario:555", "usuario:888");
            boolean usuario888Bloqueado = jedis.sismember("bloqueados:usuario:555", "usuario:888");
            System.out.println("Usuário 888 ainda bloqueado: " + usuario888Bloqueado);

            // 5. Sistema de Interesses/Preferências
            System.out.println("\n5. Sistema de Interesses/Preferências:");
            jedis.sadd("interesses:usuario:999", "futebol", "tecnologia", "musica");
            jedis.sadd("interesses:usuario:888", "cinema", "tecnologia", "viagens");

            Set<String> interessesExclusivos999 = jedis.sdiff("interesses:usuario:999", "interesses:usuario:888");
            Set<String> interessesExclusivos888 = jedis.sdiff("interesses:usuario:888", "interesses:usuario:999");

            System.out.println("Interesses exclusivos usuário 999: " + interessesExclusivos999);
            System.out.println("Interesses exclusivos usuário 888: " + interessesExclusivos888);

            // Resumo final
            System.out.println("\n Resumo Final:");
            System.out.println("Total seguidores Maria: " + jedis.scard("seguidores:maria"));
            System.out.println("Tags artigo 123: " + jedis.smembers("artigo:123:tags"));
            System.out.println("Tags artigo 456: " + jedis.smembers("artigo:456:tags"));
            System.out.println("Usuários bloqueados: " + jedis.smembers("bloqueados:usuario:555"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}