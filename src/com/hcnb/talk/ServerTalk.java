package com.hcnb.talk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerTalk extends JFrame {
    private static final int PORT = 8899;
    private JTextArea serverTa = new JTextArea();
    private JPanel btnTool = new JPanel();
    private JButton startBtn = new JButton("启动");
    private JButton stopBtn = new JButton("停止");
    private ServerSocket ss = null;
    private Socket s = null;
    private boolean isStart = false;
    private CopyOnWriteArrayList<ClientConn> ccList = new CopyOnWriteArrayList<ClientConn>();

    public ServerTalk() throws HeadlessException {
        this.setTitle("服务器端");
        btnTool.add(startBtn);
        btnTool.add(stopBtn);
        this.add(serverTa, BorderLayout.CENTER);
        this.add(btnTool, BorderLayout.SOUTH);
        this.setBounds(0, 0, 500, 500);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isStart = false;
                try {
                    if (ss != null) {
                        ss.close();
                    }
                    System.out.println("服务器停止");
                    serverTa.append("服务器已断开");
                    System.exit(0);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        serverTa.setEditable(false);
        this.setVisible(false);
        startServer();
    }

    //服务器启动
    public void startServer() {
        try {
            try {
                ss = new ServerSocket(PORT);
                isStart = true;
                System.out.println("服务器启动成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (isStart) {
                Socket s = ss.accept();
                synchronized (ccList) {
                    ccList.add(new ClientConn(s));
                }
                System.out.println("一个客户端已连接" + s.getInetAddress() + "/" + s.getPort());
                serverTa.append("一个客户端已连接" + s.getInetAddress() + "/" + s.getPort() + "\n");
            }
        } catch (SocketException e) {
            System.out.println("服务器中断");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //服务器停止
    public void stopServer() {

    }

    //服务端的连接对象
    class ClientConn implements Runnable {
        Socket s = null;

        public ClientConn(Socket s) {
            this.s = s;
            (new Thread(this)).start();
        }

        //同时接收客户端信息
        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(s.getInputStream());
                //循环接收信息
                while (isStart) {
                    String msg = dis.readUTF();
                    System.out.println(s.getInetAddress() + ": " + msg );
                    String sendMsg = s.getInetAddress() + "|" + s.getPort() + "  :" + msg;
                    //遍历ccList来调用send();
                    Iterator<ClientConn> it = ccList.iterator();
                    while (it.hasNext()) {
                        ClientConn clientConn = it.next();
                        clientConn.send(sendMsg);
                    }
                }
            } catch (SocketException e) {
                System.out.println("一个客户端下线了!");
                serverTa.append(s.getInetAddress() + "|" + s.getPort() + "  已下线 \n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //每一个连接对象数据发送的方法
        public void send(String msg) {
            try {
                DataOutputStream dos = new DataOutputStream(this.s.getOutputStream());
                dos.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        ServerTalk serverTalk = new ServerTalk();

    }
}
