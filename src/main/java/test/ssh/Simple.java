/*
 * Copyright 2010 Vrije Universiteit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.ssh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.sshd.client.SshClient;

import ibis.smartsockets.util.ssh.LocalStreamForwarder;
import ibis.smartsockets.util.ssh.PasswordCredential;
import ibis.smartsockets.util.ssh.SSHConnection;
import ibis.smartsockets.util.ssh.SSHUtil;

//import ch.ethz.ssh2.Connection;
//import ch.ethz.ssh2.LocalStreamForwarder;
//import com.trilead.ssh2.Connection;
//import com.trilead.ssh2.LocalStreamForwarder;

public class Simple {

    private static String filename = "/home/jason/.ssh/id_rsa";

    // or "~/.ssh/id_dsa"

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 3) {
            String user = args[0];
            String host = args[1];

            int port = Integer.parseInt(args[2]);

            client(user, host, port);
        } else {
            server();
        }
    }

    private static void server() {

        try {

            @SuppressWarnings("resource")
            ServerSocket ss = new ServerSocket(0);

            System.out.println("Server listening on port: " + ss.getLocalPort());

            Socket s = ss.accept();

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            String reply = in.readUTF();

            System.out.println("Client says: " + reply);

            out.writeUTF("Hello");
            out.flush();

            out.close();
            in.close();
            s.close();

        } catch (Exception e) {
            System.err.println("Eek: " + e);
            e.printStackTrace(System.err);
        }
    }

    private static void client(String user, String host, int port) {

        // We are assuming we connect through SSH to "host:22" and then forward on to a server on that same host on port "port"
        try {
            SshClient client = SSHUtil.createSSHClient();

            SSHConnection connection = SSHUtil.connect("test", client, host, new PasswordCredential(user, new char[0]), 64 * 1024, 10 * 1000);

            /// client.connect(user, host, port);

            // TODO: quick hack.... fix this!!
            // File keyfile = new File(filename);
            // String keyfilePass = "joespass"; // will be ignored if not needed

            // boolean isAuthenticated = conn.authenticateWithPublicKey(user);
            // if (!isAuthenticated) {
            // isAuthenticated = conn.authenticateWithPublicKey(user, keyfile, keyfilePass);
            // }
            //
            // if (isAuthenticated == false)
            // throw new IOException("Authentication failed.");

            LocalStreamForwarder lsf = connection.createLocalStreamForwarder(host, port, 10 * 1000);

            DataInputStream in = new DataInputStream(lsf.getInputStream());
            DataOutputStream out = new DataOutputStream(lsf.getOutputStream());

            out.writeUTF("Hello");
            out.flush();

            String reply = in.readUTF();

            System.out.println("Server says: " + reply);

            out.close();
            in.close();
            lsf.close();
        } catch (Exception e) {
            System.err.println("Eek: " + e);
            e.printStackTrace(System.err);
        }
    }
}
