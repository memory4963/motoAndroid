package com.bolo4963gmail.motoandroid;

import org.junit.Test;

import java.util.Scanner;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    @Test
    public void attempt() {
        Scanner scanner = new Scanner("21 35");
        int Tom = scanner.nextInt();
        int Paul = scanner.nextInt();

        boolean exist = false;
        boolean stop = false;

        if (Tom < 20) {
            if (Paul <= 21) {
                exist = true;
                if (Paul == 21) {
                    stop = true;
                }
            }
        } else if (Tom == 20) {

            exist = true;
            if (Paul - Tom > 1) {
                stop = true;
            }
        } else if (Tom == 21) {

            exist = true;
            if (Tom - Paul > 1) {
                stop = true;
            }
        } else if (Tom < 29) {
            if (Math.abs(Tom - Paul) <= 2) {
                exist = true;
                if (Math.abs(Tom - Paul) == 2) {
                    stop = true;
                }
            }
        } else {
            if (Tom == 29) {
                if (Paul <= 30) {
                    exist = true;
                    if (Paul == 30) {
                        stop = true;
                    }
                }
            } else if (Tom == 30) {
                if (Paul > 27 && Paul < 30) {
                    exist = true;
                    stop = true;
                }
            }
        }

        if (exist) {
            System.out.printf("true ");
            if (stop) {
                System.out.printf("true");
            } else {
                System.out.printf("false");
            }
        } else {
            System.out.printf("false");
        }
    }
}