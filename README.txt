==========================
BladeRunner Android App
build date 2012-06-20
==========================

This package contains the Android BladeRunner demo.  Included with this is a project source code, src.zip, BladeRunner application installer (apk), and demo data for offline use.  

===============
System Requirements
===============
Bladerunner App
- Android 3.2 (API 13)
- Google API's
BladeRunner Source
- https://github.com/ArcGIS/BladeRunner
===============
Disk Contents
===============
+ data
    + demo
        + basemap
            - ImageryTPK.tpk
        + OfflineData
            - Blocks.json
            - Team Areas.json
            - Wind Farm Zones.json
            - Wind Turbine.json
- BladeRunner.apk
- README.txt
- src.zip

===============
Deploy Data
===============
1. Create a demo folder at the root sdcard folder on your device, /mnt/sdcard/demo.  
2. Create a basemap directory and 'OfflineData' directory in  the demo folder you created in step 1.
3. You should now have the following directories:
    /mnt/sdcard/demo/basemap
    /mnt/sdcard/demo/OfflineData
4. Push the contents of the basemap directory and OfflineData directory from the installation archive to your device.
    /mnt/sdcard/demo/basemap/ImageryTPK.tpk
    /mnt/sdcard/demo/OfflineData/Blocks.json
    /mnt/sdcard/demo/OfflineData/Team Areas.json
    /mnt/sdcard/demo/OfflineData/Wind Farm Zones.json
    /mnt/sdcard/demo/OfflineData/Wind Turbine.json

===============
Install and Run App
===============
1. Device Settings
    1. To allow unknown sources to be installed in your tablet, go to Settings → Applications → Check the Unknown sources
    2. To allow mockup locations, go to Settings → Applications → Development → Check Allow mock locations
    3. To allow the use of the GPS make sure that location services and GPS are checked:
        Settings → Location & security → Google Location Services
        Settings → Location & security → Check Use GPS satellites 
2. Install included apk file using adb
    adb install <path to BladeRunner.apk>

===============
Source Code
===============
Source code is included in the src.zip file so you can import the applicaiton into Eclipse to review the source and/or edit the application.  

Import into Eclipse
1. Extract contents of src.zip on disk
2. Use Eclipse import wizard to import the existing project into workspace
    1. From the main menu, select "File > Import"
    2. Select "General > Existing Project into Workspace" and click "Next"
    3. Choose "Select root directory" and click "Browse" to locate the directory you extracted the project to.  Optionally you can choose "Select archive file" if you choose to not open src.zip to disk
    4. Under "Projects" select the BladeRunner project.
    5. Click "Finish"


    
