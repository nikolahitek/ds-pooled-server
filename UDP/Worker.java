package com.nikolahitek.UDP;

import com.nikolahitek.GivenSolution;
import com.nikolahitek.MathProblem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

public class Worker extends Thread {
    private InetAddress serverAddress;
    private int serverPort;
    private DatagramSocket socket;

    Worker(InetAddress serverAddress, int serverPort, int port) throws SocketException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        try {
            byte[] buff = "READY".getBytes();
            DatagramPacket startPacket = new DatagramPacket(buff, buff.length, serverAddress, serverPort);
            socket.send(startPacket);

            while (true) {
                buff = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(buff, buff.length);
                socket.receive(receivedPacket);
                ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                MathProblem problem = (MathProblem) ois.readObject();
                System.out.println(problem);

                GivenSolution solution = new GivenSolution();
                solution.mathProblem = problem;
                solution.answer = 21.0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(solution);
                oos.flush();
                buff = baos.toByteArray();
                DatagramPacket packet = new DatagramPacket(buff, buff.length, receivedPacket.getAddress(), receivedPacket.getPort());
                socket.send(packet);
                System.out.println(solution);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        Scanner scanner = new Scanner(System.in);
        Worker worker = new Worker(InetAddress.getLocalHost(), 3401, scanner.nextInt());
        worker.start();
    }
}
