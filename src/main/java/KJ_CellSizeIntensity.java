
import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.measure.*;
import ij.plugin.filter.*;

public class KJ_CellSizeIntensity implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = IJ.getImage();

		String savePath = IJ.getDirectory("Choose where to save your data.");
		String imageName = imp.getShortTitle();

		RoiManager manager = RoiManager.getInstance();
		if (manager == null) {
		    manager = new RoiManager();
		}

		prepBackgroundROIs(imp, manager);

		IJ.run("Channels Tool...");
		CompositeImage ci = (CompositeImage)imp;
		ci.setMode(IJ.GRAYSCALE);
		//todo: select channel two in the channels tool
		ci.updateAndDraw();

		new WaitForUserDialog("Move squares over background").show();

		manager.deselect();
		IJ.run(imp, "Set Measurements...", "mean stack redirect=None decimal=3");
		ResultsTable rt = manager.multiMeasure(imp);
		//rt.show(imageName + " Raw Background Measurements");

		float[][] backgroundMeans = measureBackgroundROIs(imp, manager, rt);

		ResultsTable bgMeansRT = new ResultsTable();
		for (int i = 0; i < backgroundMeans.length; i++) {
			bgMeansRT.incrementCounter();
			for (int j = 0; j < backgroundMeans[i].length; j++) {
				bgMeansRT.addValue("Ch" + (j+1), backgroundMeans[i][j]);
			}
		}
		//bgMeansRT.show(imageName + " Mean Background");

		rt.save(savePath + imageName + "_backgroundRaw_JC.csv");
		bgMeansRT.save(savePath + imageName + "_backgroundMeans_JC.csv");

		bgMeansRT.reset();
		rt.reset();
		manager.reset();
		IJ.setTool("freehand");

		new WaitForUserDialog("Trace cells and add them to ROI Manager. Click OK to continue.").show();
		
		IJ.run(imp, "Set Measurements...", "area mean stack redirect=None decimal=3");
		manager.deselect();
		manager.runCommand(imp, "Measure");

		ResultsTable cellsData = Analyzer.getResultsTable();
		int numCells = cellsData.getCounter();

		ResultsTable finalData = new ResultsTable();
		
		for (int i = 0; i < numCells; i++) {
			int channel = (int) cellsData.getValue("Ch", i);
			int slice = (int) cellsData.getValue("Slice", i);
			double mean = cellsData.getValue("Mean", i);
			double area = cellsData.getValue("Area", i);
			
			float bg = backgroundMeans[slice-1][channel-1];

			finalData.incrementCounter();
			finalData.addValue("Slice", slice);
			finalData.addValue("Channel", channel);
			finalData.addValue("BgMean", bg);
			finalData.addValue("CellMean", mean);
			finalData.addValue("Cell-BG", (mean - bg));
			finalData.addValue("Area", area);
		}

		//finalData.show(imageName + " Cell Size and Intensity Data");			
		finalData.save(savePath + imageName + "_cellSizeIntensity_JC.csv");

		IJ.selectWindow("Results");
		IJ.run("Close");
		IJ.selectWindow("Channels");
		IJ.run("Close");
		imp.close();
		manager.reset();
	}



	void prepBackgroundROIs(ImagePlus imp, RoiManager manager) {
		int width = imp.getWidth();
		int height = imp.getHeight();

		manager.runCommand("show all with labels");
		
		manager.addRoi(new Roi(width*0.2 - 25, height*0.2 - 25, 50, 50));
		manager.addRoi(new Roi(width*0.8 - 25, height*0.2 - 25, 50, 50));
		manager.addRoi(new Roi(width*0.8 - 25, height*0.8 - 25, 50, 50));
		manager.addRoi(new Roi(width*0.2 - 25, height*0.8 - 25, 50, 50));
	}

	float[][] measureBackgroundROIs(ImagePlus imp, RoiManager manager, ResultsTable rt) {
		int numROIs = manager.getCount();

		if (numROIs < 2) {
			IJ.log("You need at least 2 background ROIs");
		}

		float[][] allMeans = new float[numROIs][];

		for (int i = 0; i < numROIs; i++) {
			//IJ.log("Mean" + (i+1) + ":\t" + rt.getColumnIndex("Mean" + (i+1)));
			allMeans[i] = rt.getColumn(rt.getColumnIndex("Mean" + (i+1)));
		}

		int numChannels = imp.getNChannels();
		int numSlices = imp.getNSlices();

		float[][] bgMeans = new float[numSlices][numChannels];

		for (int sl = 0; sl < numSlices; sl++) {
			for (int ch = 0; ch < numChannels; ch++) {
				bgMeans[sl][ch] = 0;
				for (int roi = 0; roi < numROIs; roi++) {
					 bgMeans[sl][ch] += allMeans[roi][numChannels*sl + ch];
				}
				bgMeans[sl][ch] /= numROIs;
			}
		}

		return bgMeans;
	}
}

