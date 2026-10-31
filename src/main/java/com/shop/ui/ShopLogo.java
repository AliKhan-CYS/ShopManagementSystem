package com.shop.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/** Shared Shop Desk brand mark for the login screen, app window, and Windows package. */
public final class ShopLogo {
    private ShopLogo() { }

    public static BufferedImage render(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        float unit = size / 100f;
        g.scale(unit, unit);

        g.setColor(new Color(8, 47, 73));
        g.fill(new RoundRectangle2D.Float(2, 2, 96, 96, 27, 27));
        g.setPaint(new GradientPaint(14, 14, new Color(34, 211, 238), 82, 88, new Color(14, 165, 233)));
        g.fill(new RoundRectangle2D.Float(2, 2, 96, 96, 27, 27));

        // Store awning and storefront: a simple, legible mark at both icon and logo sizes.
        g.setColor(new Color(255, 255, 255, 245));
        g.fillRoundRect(22, 28, 56, 8, 4, 4);
        g.fillRoundRect(25, 37, 50, 9, 3, 3);
        g.setColor(new Color(14, 116, 144));
        g.fillRoundRect(25, 40, 50, 6, 2, 2);
        g.setColor(Color.WHITE);
        g.fillRoundRect(28, 47, 44, 29, 4, 4);
        g.setColor(new Color(14, 165, 233));
        g.fillRoundRect(34, 53, 13, 23, 2, 2);
        g.fillRoundRect(51, 53, 14, 10, 2, 2);
        g.setColor(new Color(8, 47, 73));
        g.fillRoundRect(51, 67, 14, 9, 2, 2);
        g.dispose();
        return image;
    }

    /** Writes a 32-bit DIB ICO understood by the Windows jpackage resource editor. */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: ShopLogo <output.ico>");
        BufferedImage image = render(256);
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(args[0]))) {
            writeLittleShort(out, 0); writeLittleShort(out, 1); writeLittleShort(out, 1);
            out.write(0); out.write(0); out.write(0); out.write(0);
            writeLittleShort(out, 1); writeLittleShort(out, 32);
            int xorBytes = 256 * 256 * 4;
            int maskBytes = 256 * 256 / 8;
            writeLittleInt(out, 40 + xorBytes + maskBytes);
            writeLittleInt(out, 22);

            writeLittleInt(out, 40); // BITMAPINFOHEADER
            writeLittleInt(out, 256);
            writeLittleInt(out, 512); // ICO DIB height includes XOR and AND masks.
            writeLittleShort(out, 1);
            writeLittleShort(out, 32);
            writeLittleInt(out, 0);
            writeLittleInt(out, xorBytes);
            writeLittleInt(out, 0); writeLittleInt(out, 0);
            writeLittleInt(out, 0); writeLittleInt(out, 0);
            for (int y = 255; y >= 0; y--) {
                for (int x = 0; x < 256; x++) {
                    int argb = image.getRGB(x, y);
                    out.write(argb & 0xff);
                    out.write((argb >>> 8) & 0xff);
                    out.write((argb >>> 16) & 0xff);
                    out.write((argb >>> 24) & 0xff);
                }
            }
            // Every pixel is represented by the 32-bit alpha channel; the legacy mask is clear.
            for (int i = 0; i < maskBytes; i++) out.write(0);
        }
    }

    private static void writeLittleShort(BufferedOutputStream out, int value) throws IOException {
        out.write(value & 0xff); out.write((value >>> 8) & 0xff);
    }

    private static void writeLittleInt(BufferedOutputStream out, int value) throws IOException {
        out.write(value & 0xff); out.write((value >>> 8) & 0xff);
        out.write((value >>> 16) & 0xff); out.write((value >>> 24) & 0xff);
    }
}
