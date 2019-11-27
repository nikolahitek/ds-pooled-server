package com.nikolahitek.TCP;

import com.nikolahitek.GivenSolution;
import com.nikolahitek.MathProblem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Worker extends Thread {
    Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    Worker(String host, int port) {
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                MathProblem problem = (MathProblem) ois.readObject();
                System.out.println("Got the problem.");

                Double answer = 20.0;
                GivenSolution solution = new GivenSolution();
                solution.mathProblem = problem;
                solution.answer = answer;
                System.out.println(solution);

                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(solution);
                oos.flush();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Worker worker = new Worker("localhost", 4000);
        worker.start();
    }
}
