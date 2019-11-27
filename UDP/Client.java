package com.nikolahitek.UDP;

import com.nikolahitek.GivenSolution;
import com.nikolahitek.MathProblem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

public class Client extends Thread {
    private InetAddress serverAddress;
    private int serverPort;
    private DatagramSocket socket;

    Client(InetAddress serverAddress, int serverPort, int port) throws SocketException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        try {
            MathProblem problem = new MathProblem();
            problem.firstOperand = 3.0;
            problem.secondOperand = 7.0;
            problem.operand = '*';

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(problem);
            oos.flush();
            byte[] buff = baos.toByteArray();
            DatagramPacket packet = new DatagramPacket(buff, buff.length, serverAddress, serverPort);
            socket.send(packet);
            System.out.println(problem + "\nsize: " + buff.length);

            buff = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buff, buff.length);
            socket.receive(receivedPacket);
            ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            GivenSolution solution = (GivenSolution) ois.readObject();
            System.out.println(solution);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client(InetAddress.getLocalHost(), 3400, scanner.nextInt());
        client.start();
    }
}
