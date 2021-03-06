/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.openssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Stuart Douglas
 */
public class BasicOpenSSLSocketTest extends AbstractOpenSSLTest {

    @Test
    public void basicOpenSSLTest1() throws IOException, NoSuchAlgorithmException, InterruptedException {

        try (ServerSocket serverSocket = SSLTestUtils.createServerSocket()) {
            final AtomicReference<byte[]> sessionID = new AtomicReference<>();

            Thread acceptThread = new Thread(new EchoRunnable(serverSocket, SSLTestUtils.createSSLContext("TLSv1"), sessionID));
            acceptThread.start();
            final SSLContext sslContext = SSLTestUtils.createClientSSLContext("openssl.TLSv1");
            try (final SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket()) {
                socket.connect(SSLTestUtils.createSocketAddress());
                socket.getOutputStream().write("hello world".getBytes(StandardCharsets.US_ASCII));
                socket.getOutputStream().flush();
                byte[] data = new byte[100];
                int read = socket.getInputStream().read(data);

                Assert.assertEquals("hello world", new String(data, 0, read));
                //TODO: fix client session id
                //Assert.assertArrayEquals(socket.getSession().getId(), sessionID.get());
                serverSocket.close();
                acceptThread.join();
            }
        }
    }

    @Test
    public void basicOpenSSLTest2() throws IOException, NoSuchAlgorithmException, InterruptedException {

        try (ServerSocket serverSocket = SSLTestUtils.createServerSocket()) {
            final AtomicReference<byte[]> sessionID = new AtomicReference<>();

            Thread acceptThread = new Thread(new EchoRunnable(serverSocket, SSLTestUtils.createSSLContext("TLSv1"), sessionID));
            acceptThread.start();
            final SSLContext sslContext = SSLTestUtils.createClientSSLContext("openssl.TLSv1");
            InetSocketAddress socketAddress = (InetSocketAddress) SSLTestUtils.createSocketAddress();
            try (final SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(socketAddress.getAddress(), socketAddress.getPort())) {
                socket.getOutputStream().write("hello world".getBytes(StandardCharsets.US_ASCII));
                socket.getOutputStream().flush();
                byte[] data = new byte[100];
                int read = socket.getInputStream().read(data);

                Assert.assertEquals("hello world", new String(data, 0, read));
                //TODO: fix client session id
                //Assert.assertArrayEquals(socket.getSession().getId(), sessionID.get());
                serverSocket.close();
                acceptThread.join();
            }
        }
    }
}
