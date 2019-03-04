package com.example.util

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

// Used for breakpoint with evaluate and log expression at BootStrapHandlers.java:459 (to better see the client bug) :
// ch.pipeline().addAfter(NettyPipeline.HttpDecompressor, "afterDecompressor", new MyLoggingHandler("afterDecompressor", LogLevel.INFO));
// ch.pipeline().addAfter(NettyPipeline.HttpCodec, "afterCodec", new MyLoggingHandler("afterCodec", LogLevel.INFO));
class MyLoggingHandler(name: String, level: LogLevel) : LoggingHandler(name, level) {
    override fun read(ctx: ChannelHandlerContext?) {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "READ SIGNAL"))
        }
        super.read(ctx)
    }
}
