import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* To make multithreaded, we will distribute portions for each of 4 threads.
 * Conclusions: single threaded showed better results than multithread, moreover 2 threads
 * executed better than 4 threads, but still it is longer than in case of a single thread.
* */
public class Main {
    public static final String SOURCE_FILE = "./resources/many-flowers.jpg";
    public static final String DESTINATION_FILE = "./out/many-flowers.jpg";

    public static void main(String[] args) throws IOException  {
        BufferedImage originalImage = ImageIO.read(new File(SOURCE_FILE));
        BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        long startTime = System.currentTimeMillis();

        recolorSingleThreaded(originalImage, resultImage);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println(duration + "first");

        startTime = System.currentTimeMillis();

        recolorMultiThreaded(originalImage, resultImage, 2);

        endTime = System.currentTimeMillis();
        duration = endTime - startTime;

        System.out.println(duration + "second");

        File outputFile = new File(DESTINATION_FILE);

        ImageIO.write(resultImage, "jpg", outputFile);
    }

    public static void recolorMultiThreaded(BufferedImage originalImage, BufferedImage resultImage, int numberOfThreads) {
        List<Thread> threads = new ArrayList<>();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        for(int i = 0; i < numberOfThreads; i++) {
            final int threadMultiplier = i;

            Thread thread = new Thread(() -> {
                int abscissa = 0;
                int ordinate = threadMultiplier * height;

               recolorImage(originalImage, resultImage, abscissa, ordinate);
            });

            threads.add(thread);
        }

        for(Thread thread : threads) {
            thread.start();

            try {
                thread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void recolorSingleThreaded(BufferedImage originalImage, BufferedImage resultImage) {
        recolorImage(originalImage, resultImage, 0, 0);
    }

    public static void recolorImage(BufferedImage originalImage, BufferedImage resultImage, int abscissa, int ordinate) {
        for(int x = 0; x < abscissa + originalImage.getWidth() && x < originalImage.getWidth(); x++) {
            for(int y = 0;  y < ordinate + originalImage.getHeight() && y < originalImage.getHeight(); y++) {
                recolorPixel(originalImage, resultImage, x, y);
            }
        }
    }

    public static void recolorPixel(BufferedImage originalImage, BufferedImage resultImage, int x, int y) {
        int rgb = originalImage.getRGB(x, y); // получаем битовую карту

        int red = getRed(rgb); //  извлекаем значение каждого цвета по отдельности
        int green = getGreen(rgb);
        int blue = getBlue(rgb);

        int newRed;
        int newGreen;
        int newBlue;

        if(isShadeOfGray(red, green, blue)) {
            newRed = Math.min(255, red + 10); // чтобы не уйти за 255, выберем либо 255 либо red + 10
            newGreen = Math.max(0, green - 80);
            newBlue = Math.max(0, blue - 20);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }

        int newRGB = createRGBFromColors(newRed, newGreen, newBlue);

        setRGB(resultImage, x, y, newRGB);
    }


    /**
     * Gets Raster instance which allows to execute write operations on an image object.
     * setDataElements allows to set pixels and accepts coordinates,
    * */
    public static void setRGB(BufferedImage image, int x, int y, int rgb) {
        image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
    }

    /**
    * Creates hex values from decimals.
    * */
    public static int createRGBFromColors(int red, int green, int blue) {
        int rgb = 0;

        rgb |= blue;
        rgb |= green << 8;
        rgb |= red << 16;
        rgb |= 0xFF000000;

        return rgb;
    }

    public static boolean isShadeOfGray(int red, int green, int blue) {
        return Math.abs(red - green) < 30 && Math.abs(red - blue) < 30 && Math.abs( green - blue) < 30;
    }

    public static int getRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }

    public static int getGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }

    public static int getBlue(int rgb) {
        return rgb & 0x000000FF;
    }
}
