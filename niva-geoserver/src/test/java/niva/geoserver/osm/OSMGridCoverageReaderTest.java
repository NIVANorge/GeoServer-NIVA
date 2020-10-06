package niva.geoserver.osm;

import java.awt.Rectangle;
import java.io.File;

import javax.imageio.ImageIO;

import org.geotools.TestData;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;

import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import niva.geotools.osm.OSMGridCoverageFormat;
import niva.geotools.osm.OSMGridCoverageReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OSMGridCoverageReaderTest {

    private File tempdir;


    @Before
    public void createTemp() throws Exception {
        tempdir = new File(
                TestData.file(OSMGridCoverageReaderTest.class, null).getAbsolutePath() + "/temp/");
        tempdir.mkdir();
    }

    @After
    public void deleteTemp() throws Exception {
        if (tempdir != null && tempdir.exists()) {
            tempdir.delete();
        }
    }

    @Test
    public void createOSMCoverageOriginalProjection() throws Exception {
        OSMGridCoverageReader reader = new OSMGridCoverageReader();
        int width = 1080;
        int height = 886;

        CoordinateReferenceSystem crs = CRS.decode("EPSG:900913");
        double x = 10727950.369074;
        double y = 2281448.2387094;
        double dx = (10893054.350148 - x);
        double dy = (2416971.0898403 - y);

        GeneralParameterValue[] parameters = new GeneralParameterValue[] {
                new Parameter<GridGeometry2D>(OSMGridCoverageFormat.READ_GRIDGEOMETRY2D,
                        new GridGeometry2D(new Rectangle(width - 1, height - 1),
                                new Envelope2D(crs, x, y, dx, dy)))};

        GridCoverage2D coverage = reader.read(parameters);
        ImageIO.write(coverage.getRenderedImage(), "png", new File(tempdir, "osm.png"));
    }

}
