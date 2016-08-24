KJ_CellSizeIntensity
Created by: Kim Jennings (Kriegsfeld Lab, UC Berkeley)

This fiji plugin is designed to measure the area and mean gray value above background of selected ROIs using stacked images.
It results in 3 output .csv files 
	- raw background mean gray value measurements, 
	- mean background gray value per slice, 
	- ROIs' area and mean gray value, as well as each ROI's gray value minus the background value for that slice ("intensity above background")

First, the user is prompted to select where data files will be saved.
Next, the plugin will create 4 square ROIs using the ROI Manager, and prompt the user to move the squares over the background.
	- you must have a minimum of 2 background ROIs to continue, otherwise plugin with error. 
Once the user clicks OK, the mean gray value for each square is calculated across all slices and channels.
Next, the user is prompted to select their ROIs and add them to the ROI manager.
Once the user clicks OK, the area and mean gray value of each ROI is measured, data .csv files are exported, and all windows close.
