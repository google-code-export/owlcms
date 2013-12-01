Welcome to the Olympic Weightlifting Management System.

- First installation
   1. If you select to run the application at the end of the install (which is the
      default) One of two things can happen

       - in the first case, you do not hava Java installed, and you will be brought to a site where
         you can download and install Java.  You will need to accept all the default options. Once
         you have installed Java, see "Starting the system"

       - in the second case, you already had Java installed, and a black console appeared,You may be
         prompted by the Windows firewall to allow access ("unblock") the application. Please do so
         -- if you do not, you will not be able to use multiple laptops to drive projectors and
         screens.

       See below "Accessing the application"

- Starting the System,

    1. On your desktop, locate the "Start Competition Management System" icon, and double-click on
       it. 
    
    2. The application will start, but will only show up "minimized", that is, in the taskbar at
       the bottom of your screen. 
    
    3. You may be prompted by the Windows firewall to allow access ("unblock") the application.
       Please do so -- if you do not, you will not be able to use multiple laptops to drive
       projectors and screens.

- Accessing the application from the main laptop

    After starting the system, you can access the web interface using the icon "Competition
    App".
    
- Accessing the application from other laptops

    For other laptops to access the system, it is necessary to have unblocked the Windows firewall
    as explained above.
    
    You also need to know the IP address of your laptop. Go to the start menu, and locate the
    "Competition Management System" program group. Under Windows 7, you will need to look under
    "All programs" and scroll down. Inside that group, there is a link called "Network Address".
    Locate the IP address in the left hand column.
    
    For example, the main laptop could be 192.168.0.112
    
    Other laptops would then need to type http://192.168.0.112/owlcms/app/ to get at the application,
    noting that the trailing / is mandatory.  Again *use the actual address reported by the
    tool * -- 192.168.0.112 is just an example
    
    If using tablets/iPods/iPhones for the referees, you will need to use
    http://192.168.0.112/owlcms/m/  noting that the trailing / is mandatory.  Again *use the 
    actual address reported by the tool * -- 192.168.0.112 is just an example
    

- Closing things

   1. The data is always written to a database, so as soon as you are done, you may
      shut things down without fear of losing anything
   2. Just close the black console with the red x at the top right, or right click
      the program in the taskbar at the bottom of the screen


- Uninstalling and updating

   The uninstall process does not actually erase the databases, and installing a new version does
   not erase the old data either.
   
   It is howver a good idea to make a copy of the data files (go to the start menu, locate the
   Competition Management System program group (it may be under "All Programs") and use the
   shortcut to the data files.

