# FileSystemHashVisualizer-475-2161_Motta_Craska

Tool: File System Hash Visualizer

Authors: Christian Motta and Sean Craska

The purpose of this tool is to allow an investigator to choose any number of algorithms to
hash the contents of a Windows file system. This tool maintains the filesystem structure,
and uses that to create an easy to navigate visualization of the hashes.

Tests and results for 11/11/16 upload
Tested on each drive of multiple drive system.
When starting at drive root (eg. “C:”), program is unable to process subdirectories of top level directories (eg. cannot dive into directories within “C:\Windows”).
When starting at top level directory (eg. “C:\Windows”), program is able to process entire directory and subdirectories with no issues.
      2. 	Tested on flash drive.
No issues encountered with processing removable devices. 
      3.	Tested on single drive system.
DIR command works differently on single-drive Windows 10 systems. “dir C:\” displays the contents of “C:\Users\$USERNAME”.
      4.	Tested on VMWare folder.
Program cutting off filenames at first instance of “_” character even though it does not do so for other filetypes.
      5.	Tested on ZIP folder
Program treats ZIP folders as standard files. Hashes them instead of diving in.


Solutions tried:
For issues 1 and 3, added code to explicitly cd to specified directory, but did not solve either issue.
At present, unsure while VMWare filenames are being split on “_”.
Being unable to CD into ZIP folders appears to be standard Windows 10 behavior even though one can view the contents through File Explorer.


Future tests:
Testing on other versions of Windows.
Modifying program to use Powershell instead of Command Line.

Goals:
Determine cause of root file issues.
Determine cause of VMWare filename errors.
Solution for single drive systems.
