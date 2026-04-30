package com.timerplugin.render;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class DigitRenderer {
    private static final Map<Character, boolean[][]> MATRICES = new HashMap<>();
    private static Material DIGIT_MATERIAL = Material.ORANGE_CONCRETE;
    private static final Material BACKGROUND_MATERIAL = Material.AIR;

    public static void setDigitMaterial(Material material) {
        if (material != null && material.isBlock()) {
            DIGIT_MATERIAL = material;
        }
    }

    static {
        // 3x5 matrices
        MATRICES.put('0', new boolean[][]{
                {true, true, true},
                {true, false, true},
                {true, false, true},
                {true, false, true},
                {true, true, true}
        });
        MATRICES.put('1', new boolean[][]{
                {false, false, true},
                {false, false, true},
                {false, false, true},
                {false, false, true},
                {false, false, true}
        });
        MATRICES.put('2', new boolean[][]{
                {true, true, true},
                {false, false, true},
                {true, true, true},
                {true, false, false},
                {true, true, true}
        });
        MATRICES.put('3', new boolean[][]{
                {true, true, true},
                {false, false, true},
                {true, true, true},
                {false, false, true},
                {true, true, true}
        });
        MATRICES.put('4', new boolean[][]{
                {true, false, true},
                {true, false, true},
                {true, true, true},
                {false, false, true},
                {false, false, true}
        });
        MATRICES.put('5', new boolean[][]{
                {true, true, true},
                {true, false, false},
                {true, true, true},
                {false, false, true},
                {true, true, true}
        });
        MATRICES.put('6', new boolean[][]{
                {true, true, true},
                {true, false, false},
                {true, true, true},
                {true, false, true},
                {true, true, true}
        });
        MATRICES.put('7', new boolean[][]{
                {true, true, true},
                {false, false, true},
                {false, false, true},
                {false, false, true},
                {false, false, true}
        });
        MATRICES.put('8', new boolean[][]{
                {true, true, true},
                {true, false, true},
                {true, true, true},
                {true, false, true},
                {true, true, true}
        });
        MATRICES.put('9', new boolean[][]{
                {true, true, true},
                {true, false, true},
                {true, true, true},
                {false, false, true},
                {true, true, true}
        });
        // 1x5 matrix for colon
        MATRICES.put(':', new boolean[][]{
                {false},
                {true},
                {false},
                {true},
                {false}
        });
    }

    public static void renderString(Location base, String oldStr, String newStr) {
        int offset = 0;
        for (int i = 0; i < newStr.length(); i++) {
            char newChar = newStr.charAt(i);
            char oldChar = (oldStr != null && i < oldStr.length()) ? oldStr.charAt(i) : ' ';

            if (newChar != oldChar) {
                renderChar(base.clone().add(offset, 0, 0), newChar);
            }

            offset += (newChar == ':') ? 2 : 4;
        }
    }

    private static void renderChar(Location loc, char c) {
        boolean[][] matrix = MATRICES.get(c);
        int totalWidth = (c == ':') ? 2 : 4; // Width + 1 space

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < totalWidth; x++) {
                Block block = loc.clone().add(x, 4 - y, 0).getBlock();
                boolean isPixel = (matrix != null && x < matrix[y].length && matrix[y][x]);
                Material target = isPixel ? DIGIT_MATERIAL : BACKGROUND_MATERIAL;
                if (block.getType() != target) {
                    block.setType(target);
                }
            }
        }
    }

    public static void clearString(Location base) {
        int offset = 0;
        // The standard format is DD:HH:MM:SS
        String template = "00:00:00:00";
        for (char c : template.toCharArray()) {
            int width = (c == ':') ? 2 : 4;
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < width; x++) {
                    Block block = base.clone().add(offset + x, y, 0).getBlock();
                    if (block.getType() != BACKGROUND_MATERIAL) {
                        block.setType(BACKGROUND_MATERIAL);
                    }
                }
            }
            offset += width;
        }
    }
}
