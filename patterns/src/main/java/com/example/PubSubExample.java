package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class PubSubExample implements Example {

    private JedisPubSub subscriber;
    private JedisPubSub patternSubscriber;

    public void example(JedisPool pool) {

        System.out.println("=== Pub/Sub Example ===");

        // Criar um subscriber em uma thread separada
        Thread subscriberThread = new Thread(() -> {
            try (var subscriberJedis = pool.getResource()) {
                System.out.println(" Iniciando subscriber...");

                subscriber = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        System.out.println(" Mensagem recebida no canal '" + channel + "': " + message);
                    }

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        System.out.println(" Mensagem padrão no canal '" + channel + "' (padrão: " + pattern + "): " + message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        System.out.println(" Inscrito no canal: " + channel);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        System.out.println(" Saindo do canal: " + channel);
                    }
                };

                // Subscrever nos canais
                subscriberJedis.subscribe(subscriber, "notificacoes:usuario:123", "chat:geral");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Criar um subscriber com padrão em outra thread
        Thread patternSubscriberThread = new Thread(() -> {
            try (var patternSubscriberJedis = pool.getResource()) {
                System.out.println(" Iniciando pattern subscriber...");

                patternSubscriber = new JedisPubSub() {
                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        System.out.println(" Pattern match - Canal: " + channel + ", Mensagem: " + message);
                    }
                };

                // Subscrever com padrão
                patternSubscriberJedis.psubscribe(patternSubscriber, "notificacoes:*");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Iniciar os subscribers
        subscriberThread.start();
        patternSubscriberThread.start();

        // Dar tempo para os subscribers se conectarem
        sleep(1000);

        // Publicar mensagens
        System.out.println("\n Publicando mensagens...");

        try (var publisherJedis = pool.getResource()) {
            // Publicar no canal específico
            long listeners1 = publisherJedis.publish("notificacoes:usuario:123", "Nova mensagem recebida");
            System.out.println("Mensagem 1 publicada para " + listeners1 + " listeners");

            // Publicar no canal geral
            long listeners2 = publisherJedis.publish("chat:geral", "João: Olá a todos!");
            System.out.println("Mensagem 2 publicada para " + listeners2 + " listeners");

            // Publicar em canal que casa com o padrão
            long listeners3 = publisherJedis.publish("notificacoes:usuario:456", "Sua encomenda chegou");
            System.out.println("Mensagem 3 publicada para " + listeners3 + " listeners");

            // Publicar mais algumas mensagens
            publisherJedis.publish("notificacoes:usuario:123", "Lembrete: Reunião às 15h");
            publisherJedis.publish("chat:geral", "Maria: Alguém quer café?");
            publisherJedis.publish("notificacoes:sistema", "Manutenção programada para sábado");

        }

        // Esperar um pouco para as mensagens serem processadas
        sleep(6000);

        System.out.println("\n✅ Demonstração Pub/Sub concluída!");

        try {

            subscriber.unsubscribe();
            subscriberThread.join(1000);

            patternSubscriber.punsubscribe();
            patternSubscriberThread.join(1000);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sleep(int time){
        try { Thread.sleep(time); }
        catch (Exception e){ e.printStackTrace(); }
    }

}