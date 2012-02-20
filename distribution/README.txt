Steps to create a release.

1) Prepare the distribution files.

- Make sure that owlcms/pom.xml owlcms/doc/releasenotes.txt and distribution/pom.xml all
  have correct version numbers.
- Run the      install       goal in owlcms
- commit, push
- in project "distribution" run the maven goals:   clean assembly:single  (configuration "prepare uploads")

2) Create the windows installer with Advanced Installer Freeware
Because we use the Free edition, these steps are manual.

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
  - test the installer (it is created in src/installer/owlcms-SetupFiles)
  

3) Finalize and publish
  - commit with "Release x.y.z" as comment
  - tag with "x.y.z"
  - push
  - in project "distribution" run the maven goals:   antrun:run     (configuration "upload")
    (this will send the files to the code.google.com/p/owlcms site)
 