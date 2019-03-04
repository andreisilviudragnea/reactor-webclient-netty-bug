package com.example.client

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

fun main() {
    // If not on receiverFastPath == true (which happens when org.springframework.core.codec.StringDecoder is used,
    // but not when org.springframework.http.codec.json.Jackson2JsonDecoder is used), there can be times when not
    // enough channel.read() operations are issued (in FluxReceive.drainReceiver() at line 237)
    // Another Netty secondary issue is that the gzip decompressor issues the message even if its CRC has not been
    // received yet, which can be problematic if the CRC were broken (possible in theory, less probable in practice)
    val result = WebClient
        .builder()
        .build()
        .get()
        .uri("http://localhost:6789")
        .retrieve()
        .bodyToMono<List<String>>()
        .block()

    println("final: $result")
    assert(result == emptyList<String>())
}
