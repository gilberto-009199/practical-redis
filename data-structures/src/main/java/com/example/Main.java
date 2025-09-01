package com.example;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPool;

public class Main {

    public static void main( String[] args ){

        var redis = new GenericContainer<>(
                DockerImageName.parse("redis")
        )
        .withExposedPorts(6379);

        redis.start();


        var redisHost = redis.getHost();
        var redisPort = redis.getMappedPort(6379);

        System.out.println("Redis Container: ");
        System.out.printf("\t + Redis host: %s\n", redisHost);
        System.out.printf("\t + Redis port: %s\n", redisPort);

        var pool = new JedisPool(redisHost, redisPort);

        // Strings in Redis use
        //new StringsExample().example(pool);

        // Bitmaps in Redis use
        //new BitmapsExample().example(pool);

        // List in Redis use
        //new ListExample().example(pool);


        // Sets in Redis use
        new SetsExample().example(pool);


        pool.close();
        redis.stop();
        redis.close();

    }

}
