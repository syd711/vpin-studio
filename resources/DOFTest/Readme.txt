DirectOutputTest is a simple command-line tool that lets you test your
DOF installation by:

  - viewing the output controller devices that DOF recognizes

  - turning individual outputs on and off

IMPORTANT!  This version requires DOF R3 with support for the Pinscape
Controller.


SETUP:

- Unzip the files to a new folder
- Open a command prompt (CMD.EXE) window and CD to your new folder
- Make sure that your PATH setting (type PATH at the command prompt) 
  contains the DOF install folder with DirectOutput.dll.  Add that
  folder to the PATH if not, with a command like this:

    PATH %PATH%;c:\Visual Pinball\DirectOutput


TO VIEW OUTPUT CONTROLLER DEVICES: 

Run with no arguments, like this:

    DirectOutputTest 


TO TEST INDIVIDUAL OUTPUTS: 

    Method 1: from the DOS command line

	The first argument is the UNIT NUMBER, as reported by the output
	controller list (view by running with no arguments).  Each following
	PAIR of arguments is an output number and an intensity (brightness)
	value from 0 to 255.  0 means OFF and 255 means ON at full intensity.

	For example, to turn output 3 to full intensity and output 7 to half
	intensity, all on unit 1 (the first LedWiz unit):

	    DirectOutputTest 1 3 255 7 128

	(Unit #1, output 3 to 255, output 7 to 128.)

	The DOF unit numbers are:

	    1-16   -> LedWiz units 1-16
	    19     -> PacDrive
	    20-23  -> PacLed64 units 1-4
	    50-65  -> Pinscape Controller units 1-16

    Method 2: interactively within the program

	Run the program with a command line that includes just the unit number 
	you want to test:

	    DirectOutputTest 58

	(Remember, for a list of unit numbers, run the command with no arguments.
	The number you type here is the "Equivalent" item reported.  For a Pinscape
	controller running the latest firmware, you'll see the same unit reported
	twice.  The first one is the LedWiz emulation, and the second is the 
        extended Pinscape protocol.  The LedWiz emulation unit number will be
	from 1 to 16, while the Pinscape unit number will be 51 to 66.  If you
	want to test ports 33 and higher, you have to use the Pinscape protocol,
	since the LedWiz protocol can inherently only address 32 ports.  So choose
	the unit number that you see in the 51-66 range.)

	The program will now display its own command prompt.  You can enter simple
	commands to control individual outputs.  The command format is the same as
	the command line version:

	    <output number> <brightness>

	The brightness is 0 for off to 255 for fully on.  For example, 7 128 turns
	output 7 on at half brightness; 7 128 8 255 turns output 7 on halfway and
	output 8 on at full brightness.  Only the outputs you mention in the command
	are affected.  You can also mix in the command SLEEP <time in seconds>
        between outputs to interpose a delay:

	    7 255 sleep .5 7 0

	That turns output 7 fully on, pauses for half a second, then turns output 7
	off again.
