==========================
BladeRunner Android Open Source Project
==========================

This package contains the Android BladeRunner open source project.  
The source is provided as an Eclipse ArcGIS Android Project.
If you are interested in the latest release APK please download from http://www.arcgis.com/home/item.html?id=01acc4eef0f04c2d9d0796e78938d3cd.  

===============
Requirements
===============
Java Development Kit SE 6 (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
Eclipse Indigo or Juno (http://www.eclipse.org/downloads/)
Android SDK 3.2+ platform (https://developer.android.com/sdk/index.html)
Android ADT plugin for Eclipse (https://developer.android.com/tools/sdk/eclipse-adt.html)
ArcGIS Runtime SDK for Android v2.0 (http://www.esri.com/apps/products/download/index.cfm?fuseaction=download.main&downloadid=813)

BladeRunner application is intended for tablet devices.  

===============
Data Structure
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

===============
Deploy Data
===============
In order to work with the source you will need to install the local tile map cache (tpk) and offline json feature layers to device.  

NOTE: <storage> is used to represent your device external storage location. 
More information about this location at https://developer.android.com/guide/topics/data/data-storage.html#filesExternal.  

1. Create a demo folder at the root sdcard folder on your device, /<storage>/demo.  
2. Create a 'basemap' directory and 'OfflineData' directory in  the demo folder you created in step 1.
3. You should now have the following directories:
    /<storage>/demo/basemap
    /<storage>/demo/OfflineData
4. Push the contents of the basemap directory and OfflineData directory from the installation archive to your device.
    /<storage>/demo/basemap/ImageryTPK.tpk
    /<storage>/demo/OfflineData/Blocks.json
    /<storage>/demo/OfflineData/Team Areas.json
    /<storage>/demo/OfflineData/Wind Farm Zones.json
    /<storage>/demo/OfflineData/Wind Turbine.json
