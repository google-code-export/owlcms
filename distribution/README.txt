Steps to create a release.

1) Prepare the distribution.

a. Make sure that 
		owlcms/pom.xml
		wiki/ReleaseNotes.wiki
		distribution/pom.xml 
		
   all have correct version numbers.
  
b. cd to owlcmsParent
		mvn clean
		(if under Eclipse, right-click on owlcmsParent project and select "run as/maven clean")
		
c. If the widgetset needs building (first build, or Vaadin version has changed.)

   cd to owlcms
		mvn clean compile vaadin:update-widgetset gwt:compile -P compile-widgetset   
		(if under Eclipse, run  widgetset.launch  using right-click on the .launch file)
		
d. cd to owlcmsParent
		
		if under Eclipse,
		- menu Project/Clean/Clean all projects + rebuild automatically
		- right-click on owlcmsParent project and select "run as/maven install"
		
		else run
		mvn install
		
e. cd distribution 
		mvn clean assembly:single 
    	(if under Eclipse, run  "prepare uploads.launch" using right-click on the .launch file)
    	
f. commit, push.



2) Create the windows installer with Advanced Installer Freeware
Because we use the Free edition, these steps are manual.

- go to the distribution project and refresh (F5)
- go to distribution/src/installer/owlcms-cache and delete the contents.
- start src/installer/owlcms.aip
  - In the "files" section, go to "Application Data/owlcms"
  - remove the doc, lib and owlcms directory.  You will get warnings about files that cannot be deleted,
    ignore the warning (the files will not be removed, that's what we want)
  - drag the three directories from distribution/target/owlcms-x.y.z-windows  to Application Data/owlcms
  - accept the warnings about overwrite
  - save owlcms.aip
  - build the installer once.  In spite of completing, it will not show "build ok",
    wait a few seconds after the end and cancel.
  - build the installer a second time. This time it will complete with "build ok", hit "ok".
  - go to the distribution project and refresh (F5)
  - test the installer (it is created in src/installer/owlcms-SetupFiles)


3) Finalize and publish
  - commit with "Release x.y.z" as comment
  - tag with "x.y.z"
  - push
  - in project "distribution" run the maven goals:   antrun:run     (configuration "upload")
    (this will send the files to the code.google.com/p/owlcms site)
 