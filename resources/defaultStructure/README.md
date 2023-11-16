this directory stores the initialization directories structures. when calling the method "init" [directoryName]

the "directoryName" optional parameter is the name of one directory here. this directory will be copied to the

current root directory (working dir of command line). this way is possible to have multiple structures to use with the

"init" method.

the "init" method calls the "basic" directory by default.

the git/github will not save empty directories. then to save empty directories you can add a file

".gitkeep_initDelete" file in the direcotory, this file will be deleted by the init method.
