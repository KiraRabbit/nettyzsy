package com.zsy.nettyzsy;

import com.zsy.nettyzsy.netty.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyzsyApplication implements CommandLineRunner {

    @Autowired
    private Server server;

    public static void main(String[] args) {

        SpringApplication.run(NettyzsyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        server.bind(8710);
    }
}
