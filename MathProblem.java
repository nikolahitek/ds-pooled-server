package com.nikolahitek;

import java.io.Serializable;

public class MathProblem implements Serializable {
    public Double firstOperand;
    public Double secondOperand;
    public char operand;

    Double getSolution() {
        switch (operand) {
            case '+': return firstOperand + secondOperand;
            case '-': return firstOperand - secondOperand;
            case '*': return firstOperand * secondOperand;
            case '/': return firstOperand / secondOperand;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return firstOperand + " " + operand + " " + secondOperand + " = " + getSolution();
    }
}
