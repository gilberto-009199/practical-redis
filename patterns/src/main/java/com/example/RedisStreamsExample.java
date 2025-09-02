package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import redis.clients.jedis.StreamEntryID;

import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;

import java.util.Map;
import java.util.HashMap;

public class RedisStreamsExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (var jedis = pool.getResource()) {
            Jedis redis = jedis;

            System.out.println("=== Redis Streams Example ===");

            // Limpar dados anteriores
            redis.del("pedidos");
            redis.del("grupo-consumidores");

            // 1. Adicionar eventos ao stream
            System.out.println("\n1. Adicionando eventos ao stream...");

            Map<String, String> fields1 = new HashMap<>();
            fields1.put("produto", "iPhone");
            fields1.put("valor", "4999.90");
            fields1.put("cliente", "João");

            StreamEntryID id1 = redis.xadd("pedidos", StreamEntryID.NEW_ENTRY, fields1);
            System.out.println(" Pedido 1 adicionado: " + id1);

            Map<String, String> fields2 = new HashMap<>();
            fields2.put("produto", "Macbook");
            fields2.put("valor", "8999.90");
            fields2.put("cliente", "Maria");

            StreamEntryID id2 = redis.xadd("pedidos", StreamEntryID.NEW_ENTRY, fields2);
            System.out.println(" Pedido 2 adicionado: " + id2);

            // Adicionar mais alguns pedidos
            Map<String, String> fields3 = new HashMap<>();
            fields3.put("produto", "iPad");
            fields3.put("valor", "2999.90");
            fields3.put("cliente", "Pedro");
            redis.xadd("pedidos", StreamEntryID.NEW_ENTRY, fields3);

            // 2. Ler eventos do stream
            System.out.println("\n2. Lendo eventos do stream...");

            var streams = new HashMap<String, StreamEntryID>();
            streams.put("pedidos", new StreamEntryID(0, 0));

            var entries = redis.xread(
                    XReadParams.xReadParams().count(10),
                    streams
            );

            System.out.println(" Pedidos no stream:");
            for (var entry : entries) {
                for (var streamEvent : entry.getValue()) {
                    System.out.println("   ID: " + streamEvent.getID());
                    System.out.println("   Dados: " + streamEvent.getFields());
                    System.out.println("   ---");
                }
            }

            // 3. Criar grupo de consumidores
            System.out.println("\n3. Criando grupo de consumidores...");
            try {
                redis.xgroupCreate("pedidos", "grupo-consumidores", null, true);
                System.out.println("Grupo 'grupo-consumidores' criado!");
            } catch (Exception e) {
                System.out.println("ℹ️  Grupo já existe: " + e.getMessage());
            }

            // 4. Consumir mensagens como parte do grupo
            System.out.println("\n4. Consumindo mensagens com consumer group...");

            Map<String, StreamEntryID> groupStreams = new HashMap<>();
            groupStreams.put("pedidos", StreamEntryID.UNRECEIVED_ENTRY); // > significa mensagens não lidas

            var groupEntries = redis.xreadGroup(
                    "grupo-consumidores",
                    "consumidor-1",
                    XReadGroupParams.xReadGroupParams().count(10),
                    groupStreams
            );

            System.out.println(" Pedidos consumidos pelo grupo:");
            for (var entry : groupEntries) {
                for (var streamEvent : entry.getValue()) {
                    System.out.println("   ID: " + streamEvent.getID());
                    System.out.println("   Dados: " + streamEvent.getFields());

                    // Marcar como processado (XACK)
                    redis.xack("pedidos", "grupo-consumidores", streamEvent.getID());
                    System.out.println("   ✅ Confirmado (ACK)");
                    System.out.println("   ---");
                }
            }

            // 5. Verificar pending messages
            System.out.println("\n5. Verificando mensagens pendentes...");
            var pending = redis.xpending("pedidos", "grupo-consumidores");
            System.out.println("Mensagens pendentes: " + pending.getTotal());

            // 6. Adicionar mais eventos e consumir
            System.out.println("\n6. Adicionando e consumindo mais eventos...");

            Map<String, String> fields4 = new HashMap<>();
            fields4.put("produto", "AirPods");
            fields4.put("valor", "1299.90");
            fields4.put("cliente", "Ana");
            redis.xadd("pedidos", StreamEntryID.NEW_ENTRY, fields4);

            // Consumir novo evento
            var newEntries = redis.xreadGroup(
                    "grupo-consumidores",
                    "consumidor-1",
                    XReadGroupParams.xReadGroupParams().count(1),
                    groupStreams
            );

            for (var entry : newEntries) {
                for (var streamEvent : entry.getValue()) {
                    System.out.println("   Novo pedido: " + streamEvent.getFields());
                    redis.xack("pedidos", "grupo-consumidores", streamEvent.getID());
                }
            }

            // 7. Informações do stream
            System.out.println("\n7. Informações do stream:");
            var info = redis.xinfoStream("pedidos");
            System.out.println("   Comprimento do stream: " + info.getLength());
            System.out.println("   Último ID gerado: " + info.getLastGeneratedId());

            System.out.println("\n Demonstração Redis Streams concluída!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}