package com.nikolahitek.TCP;

import com.nikolahitek.GivenSolution;
import com.nikolahitek.MathProblem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Thread {
    Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    Client(String host, int port) {
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            MathProblem problem = new MathProblem();
            problem.firstOperand = 5.0;
            problem.secondOperand = 10.0;
            problem.operand = '+';
            System.out.println(problem);

            oos.writeObject(problem);
            oos.flush();

            ois = new ObjectInputStream(socket.getInputStream());
            GivenSolution solution = (GivenSolution) ois.readObject();
            System.out.println(solution);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 4001);
        client.start();
    }
}
