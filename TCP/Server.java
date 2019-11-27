package com.nikolahitek.TCP;

import com.nikolahitek.GivenSolution;
import com.nikolahitek.MathProblem;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

    private static final int PORT_CLIENTS = 4001;
    private static final int PORT_WORKERS = 4000;

    public static void main(String[] args) throws IOException {
        ServerSocket clientsServerSocket = new ServerSocket(PORT_CLIENTS);
        ServerSocket workersServerSocket = new ServerSocket(PORT_WORKERS);

        ClientsService clientsService = new ClientsService(clientsServerSocket);
        WorkersService workersService = new WorkersService(workersServerSocket);
        workersService.start();
        clientsService.start();

    }
}

class ClientsService extends Thread {
    private ServerSocket serverSocket;

    ClientsService(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket conn = serverSocket.accept();
                // separate thread
                System.out.println("Client connected.");
                ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                MathProblem problem = (MathProblem) ois.readObject();
                WorkerRunnable.solve(problem, conn);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

class WorkersService extends Thread {
    private ServerSocket serverSocket;

    WorkersService(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket conn = serverSocket.accept();
                System.out.println("Worker connected.");
                Thread thread = new Thread(new WorkerRunnable(conn));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class WorkerRunnable implements Runnable {

    private Socket workerConn;
    private static final LinkedList<Pair<Socket, MathProblem>> pool = new LinkedList<>();

    WorkerRunnable(Socket workerConn) {
        System.out.println("Worker runnable started.");
        this.workerConn = workerConn;
    }

    static void solve(MathProblem problem, Socket clientConn) {
        synchronized (pool) {
            pool.add(pool.size(), new Pair<>(clientConn, problem));
            pool.notifyAll();
            System.out.println("Problem added in pool.");
        }
    }

    @Override
    public void run() {
        while (true) {
            Socket clientConn;
            MathProblem problem;
            synchronized (pool) {
                while (pool.isEmpty()) {
                    try {
                        pool.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Problem fetched from pool.");
                Pair<Socket, MathProblem> pair = pool.remove(0);
                clientConn = pair.getKey();
                problem = pair.getValue();
            }

            try {
                ObjectOutputStream oosWorker = new ObjectOutputStream(workerConn.getOutputStream());
                oosWorker.writeObject(problem);
                oosWorker.flush();
                System.out.println("Worker started working on problem.");

                ObjectInputStream oisWorker = new ObjectInputStream(workerConn.getInputStream());
                GivenSolution solution = (GivenSolution) oisWorker.readObject();
                System.out.println("Worker finished problem.");

                ObjectOutputStream oosClient = new ObjectOutputStream(clientConn.getOutputStream());
                oosClient.writeObject(solution);
                oosClient.flush();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
