package com.pio.roguelike.map;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ASCIITextureInfo {
    public ASCIITextureInfo(String name) {
        char_width = 19;
        char_height = 38;
        char_metrics = new HashMap<Character, CharMetric>();
        char znak;
        try {
            FileReader file = new FileReader(name);
            Scanner input = new Scanner(file);
            input.nextLine();
            input.nextLine();
            input.nextLine();
            int n = input.nextInt();
            input.nextLine();
            System.out.println(n);
            while(input.hasNextLine())
            {
                CharMetric temp = new CharMetric();
                znak = (char) input.nextInt();
                temp.pos_x = input.nextInt();
                temp.pos_y = input.nextInt();
                temp.width = input.nextInt();
                temp.height = input.nextInt();
                temp.offset_x = input.nextInt();
                temp.offset_y = input.nextInt();
                char_metrics.put(znak, temp);
                input.nextLine();
            }
            input.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public CharMetric get(char c) {
        return char_metrics.get(c);
    }

    public int char_width() {
        return char_width;
    }

    public int char_height() {
        return char_height;
    }

    /// Max character width
    int char_width;
    /// Max character height
    int char_height;

    public Map<Character, CharMetric> char_metrics;
}
