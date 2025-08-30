package com.example;

import redis.clients.jedis.JedisPool;

public class Main {

    public static void main( String[] args ){

        var pool = new JedisPool("redis://localhost:6379");

        // Strings in Redis use
        new StringsExample().example(pool);


        pool.close();

    }

}
