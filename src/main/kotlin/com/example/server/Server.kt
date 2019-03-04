package com.example.server

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.ServerSocket

const val RESPONSE_START = "HTTP/1.1 200 OK\r\n" +
        "Content-Encoding: gzip\r\n" +
        "Content-Type: application/json;charset=UTF-8\r\n" +
        "Transfer-encoding: chunked\r\n" +
        "Connection: keep-alive\r\n" +
        "\r\n"

fun main() {
    val welcomeSocket = ServerSocket(6789)

    while (true) {
        val connectionSocket = welcomeSocket.accept()

        val inFromClient = BufferedReader(InputStreamReader(connectionSocket.getInputStream()))
        repeat(6) {
            println(inFromClient.readLine())
        }

        val outToClient = DataOutputStream(connectionSocket.getOutputStream())

        bugResponse(outToClient)

        outToClient.close()
    }
}

private fun bugResponse(outToClient: DataOutputStream) {
    outToClient.writeBytes(RESPONSE_START)

    val array1 = intArrayOf(
        0x31, 0x32, 0x0d, 0x0a,
        0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x8a, 0x8e, 0x05, 0x00, 0x00, 0x00,
        0xff, 0xff, 0x0d, 0x0a
    ).map(Int::toByte).toByteArray()

    outToClient.write(array1, 0, array1.size)

    val array2 = intArrayOf(
        0x61, 0x0d, 0x0a, 0x03, 0x00, 0x29, 0xbb, 0x4c, 0x0d, 0x02, 0x00, 0x00
//            0x00, 0x0d, 0x0a
    ).map(Int::toByte).toByteArray()

    outToClient.write(array2, 0, array2.size)

    Thread.sleep(1_000)

    val array21 = intArrayOf(
        0x00, 0x0d, 0x0a
    ).map(Int::toByte).toByteArray()

    outToClient.write(array21, 0, array21.size)

    Thread.sleep(1_000)

    val array3 = byteArrayOf(0x30, 0x0d, 0x0a, 0x0d, 0x0a)

    outToClient.write(array3, 0, array3.size)
}

private fun okResponse(outToClient: DataOutputStream) {
    val array1 = intArrayOf(
        0x31, 0x32, 0x0d, 0x0a,
        0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x8a, 0x8e, 0x05, 0x00, 0x00, 0x00,
        0xff, 0xff, 0x0d, 0x0a
    ).map(Int::toByte).toByteArray()

    val array2 = intArrayOf(
        0x61, 0x0d, 0x0a, 0x03, 0x00, 0x29, 0xbb, 0x4c, 0x0d, 0x02, 0x00, 0x00, 0x00, 0x0d, 0x0a
    ).map(Int::toByte).toByteArray()

    val array3 = byteArrayOf(0x30, 0x0d, 0x0a, 0x0d, 0x0a)

    outToClient.write(RESPONSE_START.toByteArray() + array1 + array2 + array3)
}
