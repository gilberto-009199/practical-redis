package com.example;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.UnifiedJedis;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class StringsExample implements Example{

    @Override
    public void example(JedisPool pool) {
        System.out.println("\n=== Exemplos de String ===\n");

        var redis = pool.getResource();






    }

}
