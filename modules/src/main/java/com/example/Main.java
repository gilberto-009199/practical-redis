package com.example;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;

public class Main {

    public static void main( String[] args ){

        var redis = new GenericContainer<>(
                // @atencao redis stack vem com Graph
                DockerImageName.parse("redislabs/redismod")
        )
        .withExposedPorts(6379);

        redis.start();


        var redisHost = redis.getHost();
        var redisPort = redis.getMappedPort(6379);

        System.out.println("Redis Container: ");
        System.out.printf("\t + Redis host: %s\n", redisHost);
        System.out.printf("\t + Redis port: %s\n", redisPort);

        var pool = new JedisPool(redisHost, redisPort);

        // 1. Redis Search example
        //new RedisSearchExample().example(pool);

        // 2. Redis Graph Example
        new RedisGraphExample().example(pool);

        pool.close();
        redis.stop();
        redis.close();

    }

}
