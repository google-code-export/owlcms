Update procedure for version 2.15.x

- Locate the database folder.
  - Under Windows, there is an entry "Shortcut to Data Files" in the "Competition Management System" entry 
    of the Start Menu
  - For Windows 7 and later, the files should be found under C:\Users\USERNAME\AppData\Roaming\owlcms\db where 
    USERNAME is your actual user name.

- If there is nothing useful to you in the current database, or if you don't mind starting from scratch, just
  remove all the files and you're done.


If you want to update your old database content

- MOVE the *.db files to the "old" folder in the directory where you are reading this file. (the original directory
  where you got the db files from should now be empty)
- run the "update.bat" script (if under Linux, "sh update.bat" should work, the command is the same for both OSes)
- the "new" folder should contain a new .db file.
- MOVE that file to the original location.

