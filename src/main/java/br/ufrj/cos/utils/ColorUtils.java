package br.ufrj.cos.utils;

import java.awt.*;
import java.util.Random;

public class ColorUtils {

    public static String generateRandomColorCode() {
        Random random = new Random();
        int colorCode = random.nextInt(0xffffff + 1); // Generate a random color code (0x000000 to 0xffffff)
        return String.format("#%06x", colorCode); // Format the color code as a hexadecimal string with a leading "#" and leading zeros
    }

}
