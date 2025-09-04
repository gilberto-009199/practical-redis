package com.example;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.SafeEncoder;
import java.util.List;
import java.util.Map;

public class RedisTimeSeriesExample implements Example {

    @Override
    public void example(JedisPool pool) {
        try (Jedis jedis = pool.getResource()) {

            // Criar uma série temporal para pedidos por minuto
            var result = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.CREATE"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto"),
                    SafeEncoder.encode("RETENTION"),
                    SafeEncoder.encode("604800000"),  // 7 dias em ms
                    SafeEncoder.encode("LABELS"),
                    SafeEncoder.encode("restaurante_id"),
                    SafeEncoder.encode("1"),
                    SafeEncoder.encode("tipo_metricas"),
                    SafeEncoder.encode("pedidos")
            );
            System.out.println("Criado Time Series, resultado: " + SafeEncoder.encode((byte[]) result));

            // Adicionar dados (timestamp automático)
            var addResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.ADD"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto"),
                    SafeEncoder.encode("*"),
                    SafeEncoder.encode("15")
            );
            System.out.println("TS.ADD resultado: ");
            processResult(addResult);

            // Adicionar mais dados de exemplo
            jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.ADD"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto"),
                    SafeEncoder.encode("*"),
                    SafeEncoder.encode("20")
            );

            jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.ADD"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto"),
                    SafeEncoder.encode("*"),
                    SafeEncoder.encode("18")
            );

            // Consultar agregados - média por hora
            var rangeResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.RANGE"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto"),
                    SafeEncoder.encode("-"),
                    SafeEncoder.encode("+"),
                    SafeEncoder.encode("AGGREGATION"),
                    SafeEncoder.encode("avg"),
                    SafeEncoder.encode("3600000")  // média por hora
            );
            System.out.println("TS.RANGE com agregação: ");
            processResult(rangeResult);

            // Criar uma segunda série temporal para demonstração
            jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.CREATE"),
                    SafeEncoder.encode("pedidos:restaurante:2:por_minuto"),
                    SafeEncoder.encode("RETENTION"),
                    SafeEncoder.encode("604800000"),
                    SafeEncoder.encode("LABELS"),
                    SafeEncoder.encode("restaurante_id"),
                    SafeEncoder.encode("2"),
                    SafeEncoder.encode("tipo_metricas"),
                    SafeEncoder.encode("pedidos")
            );

            // Adicionar dados à segunda série
            jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.ADD"),
                    SafeEncoder.encode("pedidos:restaurante:2:por_minuto"),
                    SafeEncoder.encode("*"),
                    SafeEncoder.encode("10")
            );

            // Múltiplas séries com agregação - soma por dia
            var mrangeResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.MRANGE"),
                    SafeEncoder.encode("-"),
                    SafeEncoder.encode("+"),
                    SafeEncoder.encode("AGGREGATION"),
                    SafeEncoder.encode("sum"),
                    SafeEncoder.encode("86400000"),  // soma por dia
                    SafeEncoder.encode("FILTER"),
                    SafeEncoder.encode("restaurante_id=1")
            );
            System.out.println("TS.MRANGE resultado: ");
            processResult(mrangeResult);

            // Múltiplas séries com agregação para todos os restaurantes
            var mrangeAllResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.MRANGE"),
                    SafeEncoder.encode("-"),
                    SafeEncoder.encode("+"),
                    SafeEncoder.encode("AGGREGATION"),
                    SafeEncoder.encode("sum"),
                    SafeEncoder.encode("86400000"),
                    SafeEncoder.encode("FILTER"),
                    SafeEncoder.encode("tipo_metricas=pedidos")
            );
            System.out.println("TS.MRANGE todos pedidos: ");
            processResult(mrangeAllResult);

            // Consulta reversa com agregação
            var mrevrangeResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.MREVRANGE"),
                    SafeEncoder.encode("-"),
                    SafeEncoder.encode("+"),
                    SafeEncoder.encode("AGGREGATION"),
                    SafeEncoder.encode("avg"),
                    SafeEncoder.encode("3600000"),  // média por hora
                    SafeEncoder.encode("FILTER"),
                    SafeEncoder.encode("restaurante_id=1")
            );
            System.out.println("TS.MREVRANGE resultado: ");
            processResult(mrevrangeResult);


            // Tentativa de previsão com ML (pode não estar disponível em todas as versões)
            try {
                var predictResult = jedis.sendCommand(
                        () -> SafeEncoder.encode("TS.MREVRANGE"),
                        SafeEncoder.encode("-"),
                        SafeEncoder.encode("+"),
                        SafeEncoder.encode("AGGREGATION"),
                        SafeEncoder.encode("avg"),
                        SafeEncoder.encode("3600000"),
                        SafeEncoder.encode("FILTER"),
                        SafeEncoder.encode("restaurante_id=1"),
                        SafeEncoder.encode("PREDICT"),
                        SafeEncoder.encode("5")  // prever próximas 5 horas
                );
                System.out.println("Previsão ML resultado: " + predictResult);
            } catch (Exception e) {
                System.out.println("Funcionalidade PREDICT não disponível: " + e.getMessage());
            }

            // Consulta adicional: obter informações da série temporal
            var infoResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.INFO"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto")
            );
            System.out.println("TS.INFO resultado: ");
            processResult(infoResult);

            // Consultar os últimos dados
            var lastResult = jedis.sendCommand(
                    () -> SafeEncoder.encode("TS.GET"),
                    SafeEncoder.encode("pedidos:restaurante:1:por_minuto")
            );
            System.out.println("TS.GET último dado: ");
            processResult(lastResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processResult(Object result) {
        processResult(result, 0);
    }

    private void processResult(Object result, int depth) {
        if (result == null) {
            printIndent(depth);
            System.out.println("null");
            return;
        }

        printIndent(depth);

        if (result instanceof Long) {

            System.out.println("Long: " + result);

        } else if (result instanceof String) {

            System.out.println("String: " + result);

        } else if (result instanceof byte[]) {

            String decoded = SafeEncoder.encode((byte[]) result);
            System.out.println("Bytes: " + decoded);

        } else if (result instanceof List) {

            List<?> list = (List<?>) result;
            System.out.println("List with " + list.size() + " elements:");
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                printIndent(depth + 1);
                processResult(item, depth + 2);
            }

        } else if (result instanceof Map) {

            var map = (Map<?, ?>) result;
            System.out.println("Map with " + map.size() + " entries:");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                printIndent(depth + 1);
                System.out.print(entry.getKey() + ": ");
                processResult(entry.getValue(), depth + 2);
            }

        } else {

            System.out.println("Value: " + result.toString());

        }
    }

    private void printIndent(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }
    }
}