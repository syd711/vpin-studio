package de.mephisto.vpin.server.dmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.ddogleg.struct.DogArray_I32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boofcv.abst.filter.binary.BinaryLabelContourFinder;
import boofcv.abst.filter.binary.InputToBinary;
import boofcv.abst.shapes.polyline.ConfigPolylineSplitMerge;
import boofcv.abst.shapes.polyline.PointsToPolyline;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.factory.filter.binary.ConfigThreshold;
import boofcv.factory.filter.binary.FactoryBinaryContourFinder;
import boofcv.factory.filter.binary.FactoryThresholdBinary;
import boofcv.factory.shape.FactoryPointsToPolyline;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import georegression.struct.point.Point2D_I32;

public class DMDPositionDetector {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionDetector.class);

  protected Class<GrayF32> imageClass = GrayF32.class;
  protected ImageType<GrayF32> imageType = ImageType.SB_F32;

  protected InputToBinary<GrayF32> inputToBinary;
  BinaryLabelContourFinder binaryToContour;
	PointsToPolyline contourToPolyline;


  public DMDPositionDetector() {
    // Input to Binary
		ConfigThreshold thresholdConfig = ConfigThreshold.fixed(15);
    this.inputToBinary = FactoryThresholdBinary.threshold(thresholdConfig, GrayF32.class);

    // Binary to Contour tool
    this.binaryToContour = FactoryBinaryContourFinder.linearChang2004();
		binaryToContour.setConnectRule(ConnectRule.FOUR);

    // Line detector
		ConfigPolylineSplitMerge config = new ConfigPolylineSplitMerge();
		config.loops = true;
    config.convex = true;
		config.minimumSides = 4;
		config.maximumSides = 15;
    config.minimumSideLength = 30;
    config.cornerScorePenalty = 0.5;
    config.thresholdSideSplitScore = 0.2;
    config.maxNumberOfSideSamples = 5;
    this.contourToPolyline = FactoryPointsToPolyline.splitMerge(config);
  }


  public List<Integer> processImage(BufferedImage buffered) {

    GrayF32 input = ConvertBufferedImage.convertFromSingle(buffered, null, imageClass);

    GrayU8 binary = new GrayU8(1, 1);
    binary.reshape(buffered.getWidth(), buffered.getHeight());

    final double timeInSeconds;
    synchronized (this) {
      long before = System.nanoTime();
      inputToBinary.process(input, binary);

      GrayS32 labeled = new GrayS32(1, 1);
      binaryToContour.process(binary, labeled);

      List<Contour> contours = BinaryImageOps.convertContours(binaryToContour);
      contours  = contours.stream().filter(c -> c.external.size() >= 4).collect(Collectors.toList());

      List<List<Point2D_I32>> polylines = contourToPolylines(contours, 6);

      //printPolylines(polylines);
      //drawPolylines(buffered, polylines, "contours.png");

      List<Integer> dmd = polylinesToBiggestRectangle(polylines, buffered.getWidth() * buffered.getHeight());
      
      //drawDMD(buffered, dmd, "outcome.png");
      
      long after = System.nanoTime();
      timeInSeconds = (after - before)*1e-9;

      LOG.info("Image processed in {} s", timeInSeconds);

      return dmd;
    }
  }

  protected List<List<Point2D_I32>> contourToPolylines(List<Contour> contours, int minContourSize) {
    List<List<Point2D_I32>> polylines = new ArrayList<>();
		DogArray_I32 indices = new DogArray_I32();
    for (Contour contour: contours) {
      List<Point2D_I32> points = contour.external;
			if (points.size() < minContourSize)
				continue;
			if (contourToPolyline.process(points, indices)) {
        List<Point2D_I32> l = new ArrayList<>();
				for (int j = 0; j < indices.size; j++) {
					l.add(points.get(indices.get(j)));
				}
        polylines.add(l);
			}
		}
    return polylines;
	}

  protected List<Integer> polylinesToBiggestRectangle(List<List<Point2D_I32>> polylines, double imageSurface) {
    List<Integer> ret = null;
    double maxSurface = 0;
    double bestRatio = Integer.MAX_VALUE;

    for (int i = 0; i < polylines.size(); i++) {
      int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
      List<Point2D_I32> points = polylines.get(i);
      points.add(points.get(0));
      Point2D_I32 pt1 = null, pt2 = null;
      for (int j = 1; j < points.size(); j++) {
        pt1 = points.get(j - 1); 
        pt2 = points.get(j); 
        minX = Math.min(minX, (pt1.x + pt2.x) / 2);
        minY = Math.min(minY, (pt1.y + pt2.y) / 2);
        maxX = Math.max(maxX, (pt1.x + pt2.x) / 2);
        maxY = Math.max(maxY, (pt1.y + pt2.y) / 2);
      }
      double w = maxX - minX;
      double h = maxY - minY;

      // filter polylines that have a very small surface (below 1% of the total surface of the image)
      if (w * h / imageSurface > 0.01) {

        // selection by max of surface
        // if (w * h > maxSurface) {
        //   ret = Arrays.asList(minX, minY, maxX, maxY);
        //   maxSurface = w * h;
        // }

        // selection by closest ratio to 4
        double ratio = Math.abs(w / h - 4);
        if (ratio < bestRatio) {
          ret = Arrays.asList(minX, minY, maxX, maxY);
          bestRatio = ratio;
        }
      }
    }

    return ret;
  }

  //------------------------

  protected void drawPolylines(BufferedImage buffered, List<List<Point2D_I32>> polylines, String filename) {
    BufferedImage b = new BufferedImage(buffered.getWidth(), buffered.getHeight(), buffered.getType());
    Graphics2D g = (Graphics2D) b.getGraphics();
    
      g.drawImage(buffered, 0, 0, null);
      
      g.setColor(Color.GREEN);
      g.setStroke(new BasicStroke(2));
      
      for (int i = 0; i < polylines.size(); i++) {
        List<Point2D_I32> points = polylines.get(i);
        points.add(points.get(0));
        for (int j = 1; j < points.size(); j++) {
          Point2D_I32 pt1 = points.get(j - 1); 
          Point2D_I32 pt2 = points.get(j); 
          g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
        }
      }
    g.dispose();
    try (FileOutputStream fout = new FileOutputStream(new File("c:/temp/", filename))) {
      ImageIO.write(b, "png", fout);
    }
    catch (IOException ioe) {
      LOG.error("cannot write image", ioe);
    }
  }

  protected void drawDMD(BufferedImage buffered, List<Integer> dmd, String filename) {
    if (dmd == null) {
      return;
    }

    BufferedImage b = new BufferedImage(buffered.getWidth(), buffered.getHeight(), buffered.getType());
    Graphics g = b.getGraphics();
    
      g.drawImage(buffered, 0, 0, null);
      g.setColor(Color.GREEN);

      int minX = dmd.get(0);
      int minY = dmd.get(1);
      int maxX = dmd.get(2);
      int maxY = dmd.get(3);

      g.drawLine(minX, minY, minX, maxY);
      g.drawLine(minX, maxY, maxX, maxY);
      g.drawLine(maxX, maxY, maxX, minY);
      g.drawLine(maxX, minY, minX, minY);

      g.dispose();
    try (FileOutputStream fout = new FileOutputStream(new File("c:/temp/", filename))) {
      ImageIO.write(b, "png", fout);
    }
    catch (IOException ioe) {
      LOG.error("cannot write image", ioe);
    }
  }


  protected void printPolylines(List<List<Point2D_I32>> polylines) {
    for (int i = 0; i < polylines.size(); i++) {
			List<Point2D_I32> points = polylines.get(i);
      StringBuilder bld = new StringBuilder();
      for (int j = 0; j < points.size(); j++) {
        Point2D_I32 pt = points.get(j); 
        bld.append(j>0 ? ", " : "").append(pt);
      }
      System.out.println("line " + i + ": " + bld.toString());
    }
	}

  public static void main(String[] args) throws Exception {
    BufferedImage img = null;
    try (FileInputStream is = new FileInputStream("c:/temp/contours.png")) {
      img = ImageIO.read(is);
    }
    DMDPositionDetector detector = new DMDPositionDetector();
    detector.processImage(img);
  }
}
