package com.hcnb.talk;

import javax.swing.*;
import java.awt.*;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientTalk extends JFrame {
    private JTextArea ta = new JTextArea(10, 20);
    private JTextField tf = new JTextField(20);
    private static final String CONN_STR="127.0.0.1";
    private static final int CONN_PORT=8888;
    private DataOutputStream dos=null;
    private boolean isConn=false;

    private Socket s=null;

    public ClientTalk() throws HeadlessException {
        super();
    }

    public void init() {
        this.setTitle("客户端窗口");
        this.add(ta, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(ta);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setBounds(300, 300, 600, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ta.setEditable(false);//要求文字框区域不能编辑
        tf.requestFocus();//设置光标聚焦
        tf.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sendMsg = tf.getText() + "\n";
                if (sendMsg.trim().length() == 0) {
                    return;
                }
                Date date = new Date();
                tf.setText("");
                String time=getTimeShort(date);
//                ta.append(time+"\n");
//                ta.append(sendMsg+"\n");
                send(sendMsg);
            }
        });
        try {
            s=new Socket(CONN_STR,CONN_PORT);
            isConn=true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setVisible(true);
        new Thread(new Receiver()).start();
    }

    public static String getTimeShort(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(time);
        return dateString;
    }

    //发送信息至服务器
    private void send(String msg){
        try {
            dos=new DataOutputStream(s.getOutputStream());
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //多线程接收服务器的类,实现Runnable接口
    class Receiver implements Runnable{

        @Override
        public void run() {
            try {
                while (isConn){
                    DataInputStream dis=new DataInputStream(s.getInputStream());
                    String receiveMsg = dis.readUTF();
                    ta.append(receiveMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        ClientTalk ct = new ClientTalk();
        ct.init();
    }
}
