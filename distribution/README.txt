Steps to create a release.

- Make sure that owlcms/pom.xml owlcms/doc/releasenotes.txt and distribution/pom.xml all
  have correct version numbers.
- commit, push

- in project "distribution" run the maven goals:   clean assembly:single
- go to distribution/src/installer/owlcms-cache and delete the contents.
- start src/installer/owlcms.aip and go to Application Data/owlcms
  - remove the doc, lib and owlcms directory.  You will get warnings about files that cannot be deleted,
    ignore the warning (the files will not be removed, that's ok)
  - drag the directories from distribution/target/owlcms-x.y.z-windows  to Application Data/owlcms
  - accept the warnings about overwrite
  - save owlcms.aip
  - build the installer once.  In spite of completing, it will not show "build ok",
    wait a few seconds after the end and cancel.
  - build the installer a second time. This time it will complete with "build ok", hit "ok".
  - test the installer (it is created in src/installer/owlcms-SetupFiles)
  
  
  - commit with "Release x.y.z" as comment
  - tag with "x.y.z"
  - push
  
- in project "distribution" run the maven goals:   antrun:run
 