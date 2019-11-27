package com.nikolahitek;

import java.io.Serializable;

public class GivenSolution implements Serializable {
    public MathProblem mathProblem;
    public Double answer;

    boolean isCorrect() {
        return answer.equals(mathProblem.getSolution());
    }

    @Override
    public String toString() {
        return mathProblem.toString() + "\nGiven answer " + answer + " is " + isCorrect();
    }
}
