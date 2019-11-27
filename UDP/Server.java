package com.nikolahitek.UDP;

import com.nikolahitek.GivenSolution;
import com.nikolahitek.MathProblem;
import javafx.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;

public class Server {

    public static void main(String[] args) throws SocketException {
        ClientsService clientsService = new ClientsService(new DatagramSocket(3400));
        WorkersService workersService = new WorkersService(new DatagramSocket(3401));
        workersService.start();
        clientsService.start();
    }
}

class ClientsService extends Thread {
    private DatagramSocket socket;

    ClientsService(DatagramSocket socket) {
        this.socket = socket;
        System.out.println("ClientsService started.");
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buff = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                socket.receive(packet);
                System.out.println("Client connected.");
                ByteArrayInputStream bais = new ByteArrayInputStream(buff);
                ObjectInputStream ois = new ObjectInputStream(bais);
                MathProblem problem = (MathProblem) ois.readObject();

                WorkerRunnable.solve(problem, new Pair<>(packet.getAddress(), packet.getPort()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class WorkersService extends Thread {
    private DatagramSocket socket;

    WorkersService(DatagramSocket socket) {
        this.socket = socket;
        System.out.println("WorkersService started.");
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buff = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                socket.receive(packet);
                String data = new String(packet.getData());
                if (data.trim().equals("READY")) {
                    System.out.println("Worker connected: " + new String(packet.getData()));
                    Thread thread = new Thread(new WorkerRunnable(packet.getAddress(), packet.getPort()));
                    thread.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class WorkerRunnable implements Runnable {

    private InetAddress workerAddress;
    private int workerPort;
    private DatagramSocket socket;
    private static final LinkedList<Pair<Pair<InetAddress, Integer>, MathProblem>> pool = new LinkedList<>();

    WorkerRunnable(InetAddress workerAddress, int workerPort) throws SocketException {
        this.workerAddress = workerAddress;
        this.workerPort = workerPort;
        this.socket = new DatagramSocket();
        System.out.println("Worker runnable started for: " + workerAddress + ", " + workerPort);
    }

    static void solve(MathProblem problem, Pair<InetAddress, Integer> client) {
        synchronized (pool) {
            pool.add(pool.size(), new Pair<>(client, problem));
            pool.notifyAll();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Pair<InetAddress, Integer> client;
                MathProblem problem;
                synchronized (pool) {
                    while (pool.isEmpty()) {
                        pool.wait();
                    }
                    Pair<Pair<InetAddress, Integer>, MathProblem> data = pool.remove(0);
                    client = data.getKey();
                    problem = data.getValue();
                }

                System.out.println("Sending problem to worker.");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(problem);
                oos.flush();
                byte[] buff = baos.toByteArray();
                DatagramPacket problemPacket = new DatagramPacket(buff, buff.length, workerAddress, workerPort);
                socket.send(problemPacket);

                System.out.println("Receiving solution from worker.");
                buff = new byte[1024];
                DatagramPacket solutionPacket = new DatagramPacket(buff, buff.length);
                socket.receive(solutionPacket);
                ByteArrayInputStream bais = new ByteArrayInputStream(solutionPacket.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                GivenSolution solution = (GivenSolution) ois.readObject();

                System.out.println("Sending solution to client: " + client.getKey() + ", " + client.getValue());
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(solution);
                oos.flush();
                buff = baos.toByteArray();
                DatagramPacket clientPacket = new DatagramPacket(buff, buff.length, client.getKey(), client.getValue());
                socket.send(clientPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
