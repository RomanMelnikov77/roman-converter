package com.pdf.roman_parser.pipeline.layout;

import com.pdf.roman_parser.pipeline.model.BoundingBox;
import com.pdf.roman_parser.pipeline.model.LayoutBlock;
import com.pdf.roman_parser.pipeline.model.PageRaster;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class OpenCvLayoutAnalyzer implements LayoutAnalyzer {

    static {
        OpenCV.loadLocally();
    }

    @Override
    public List<LayoutBlock> analyze(PageRaster pageRaster) throws IOException {
        Mat source = bufferedImageToMat(pageRaster.image());
        Mat gray = new Mat();
        Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 7));
        Mat connected = new Mat();
        Imgproc.morphologyEx(binary, connected, Imgproc.MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(connected, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<LayoutBlock> blocks = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.width < 20 || rect.height < 12) {
                continue;
            }
            BufferedImage blockImage = pageRaster.image().getSubimage(rect.x, rect.y, rect.width, rect.height);
            blocks.add(new LayoutBlock(pageRaster.pageIndex(),
                    new BoundingBox(rect.x, rect.y, rect.width, rect.height),
                    toPng(blockImage)));
        }

        if (blocks.isEmpty()) {
            blocks.add(new LayoutBlock(pageRaster.pageIndex(),
                    new BoundingBox(0, 0, pageRaster.image().getWidth(), pageRaster.image().getHeight()),
                    toPng(pageRaster.image())));
        }

        blocks.sort(Comparator.comparing((LayoutBlock b) -> b.boundingBox().y())
                .thenComparing(b -> b.boundingBox().x()));
        return blocks;
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        int[] data = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        byte[] bytes = new byte[image.getWidth() * image.getHeight() * 3];
        for (int i = 0; i < data.length; i++) {
            bytes[i * 3] = (byte) (data[i] & 0xFF);
            bytes[i * 3 + 1] = (byte) ((data[i] >> 8) & 0xFF);
            bytes[i * 3 + 2] = (byte) ((data[i] >> 16) & 0xFF);
        }
        mat.put(0, 0, bytes);
        return mat;
    }

    private byte[] toPng(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        }
    }
}
