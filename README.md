# reactor-webclient-netty-bug

The Spring `WebClient` sometimes hangs indefinitely because of a possible bug in the 
[`reactor-netty`](https://github.com/reactor/reactor-netty) and/or [`netty`](https://github.com/netty/netty) projects.
The root of this bug is that the `channel.read()` call in
[`FluxReceive.drainReceiver()`](https://github.com/reactor/reactor-netty/blob/master/src/main/java/reactor/netty/channel/FluxReceive.java#L237)
is sometimes not issued enough times for the Netty channel to finish reading the message. This happens when
`receiverFastpath == false`, which is the case when 
[`Jackson2JsonDecoder`](https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/http/codec/json/Jackson2JsonDecoder.java)
is used by Spring's `WebClient`. The `receiverFastpath == false` condition is a consequence of the Reactor 
[`flatMap`](https://github.com/reactor/reactor-core/blob/master/reactor-core/src/main/java/reactor/core/publisher/Flux.java#L4823)
operator from [`Jackson2Tokenizer`](https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/http/codec/json/Jackson2Tokenizer.java#L86),
which requests at most `maxConcurrency` (with a default of `32`) elements from the upstream publisher.

This bug is very hard to reproduce because it happens when gzip encoding is used on a very small response. From
what I understand, it happens because gzipping a very small response (like `[]`) expands the response size significantly 
and because the [`HttpContentDecompressor`](https://github.com/netty/netty/blob/4.1/codec-http/src/main/java/io/netty/handler/codec/http/HttpContentDecompressor.java)
issues the gzipped response before the corresponding CRC is received. The `channel.read()` signals get issued after
the `HttpContentDecompressor` sends the message up the `ChannelPipeline`, but if the reads from the socket are
"too chunked", there won't be another chance for the `HttpContentDecompressor` to emit a `LastHttpContent`.

I have attached an example `WebClient` and a simple TCP server to reproduce this bug. I know that the example seems
contrived, but I tried to reproduce this bug in the most simple way possible that I could find. In our setup, the
request which hangs is made against an `https` endpoint, so the Netty `SslHandler` also processes the response in the
pipeline, before `HttpContentDecompressor`, which made the bug even harder to track and reproduce.

The Netty Nio event loop thread which processes the response gets stuck in the
[`NioEventLoop`](https://github.com/netty/netty/blob/4.1/transport/src/main/java/io/netty/channel/nio/NioEventLoop.java#L423)
indefinitely in this case, from what I have tested so far.

I do not know yet where the problem lies, but I believe that maybe if the `HttpContentDecompressor` would issue
`channel.read()` operations until in consumes a whole gzip member, before sending the decompressed data up the pipeline,
maybe the pipeline would not get stuck. However, I believe that this problem can still occur even in other circumstances
where the reads from the socket return many small chunks.
