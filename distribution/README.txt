Steps to create a release.

1) Prepare the distribution.

a. Make sure that 
		owlcms/pom.xml
		wiki/ReleaseNotes.wiki
		distribution/pom.xml 
		
   all have correct version numbers.
  
b. Clean everything:

	if under Eclipse
		stop the Tomcat server if running
		right-click on owlcmsParent project and select "run as/maven clean"
	else
		cd to owlcmsParent
		mvn clean

c. IF FIRST BUILD on a new checkout perform step "FIRST BUILD" at bottom of this file
		
d. Recompile:
		if under Eclipse,
			1- menu Project/Clean/Clean all projects + rebuild automatically
			2- right-click on owlcmsParent project and select "run as/maven install"
		else run
			mvn install
		
		
e. Create distribution files:

	if under Eclipse
		inside distribution project, right-click on  "prepare uploads.launch" and run it.
	else
		cd to owlcmsParent
		mvn clean assembly:single 


2) Create the windows installer with Advanced Installer Freeware
Because we use the Free edition, these steps are manual.

- go to the distribution project and refresh (F5)
- go to distribution/src/installer/owlcms-cache and delete the contents.
- start src/installer/owlcms.aip
  - In the "Files and Folders" section, go to "Application Data/owlcms" and expand the folder triangle.
  - remove the 
  
      doc
      lib
      owlcms 
      
      directories. If prompted with a warning, select the "Don't Search" button
  - go to the distribution/target/owlcms-x.y.z-windows in Eclipse
  - drag the content to "Application Data/owlcms" (owlcms.exe and doc,lib,owlcms), accept the warnings about overwrite ("Yes to All")
  - update the product version number on the product details page
  - save owlcms.aip, , select "major upgrade"
  - build the installer once.  In spite of completing with "total build time", it will not show "build finished successfully".
    wait a few seconds after the end and close the window.
  - go to the distribution project and refresh (F5)
  - test the installer (it is created in src/installer/owlcms-SetupFiles)


3) Finalize and publish
  - commit all modified directories with "Release x.y.z" as comment
  - tag both repositories with "x.y.z"
  - push
  - in project "distribution" run the maven goals:   antrun:run     (under eclipse "upload to code.google.com.launch")
    (this will send the files to the code.google.com/p/owlcms site)
    
==================================================================================    
    
FIRST BUILD additional steps:

 	Only needed	IF FIRST BUILD at step 1b.
		
				c. If the widgetset needs building (first build, or Vaadin version has changed.)
				
				   cd to owlcms
						mvn clean compile vaadin:update-widgetset gwt:compile -P compile-widgetset   
						(if under Eclipse, run  widgetset.launch  using right-click on the .launch file)
				
				d. cd back to owlcmsParent
				
		END IF FIRST BUILD
 