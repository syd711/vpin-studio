' This is a dummy VBS script to test VBS parsing
' The file can contain several scenario, they are separated by line of =
' Next line is the test case name and it is mandatory

'===============================================================
' Exotic GameName

' use _ to split const on several lines
Const myExoticName = "rab_320",_
			tableName = "ex""'otic" & " 'and' " & "com'""plex", _ ' with a "comment" at the end"   ' with a "comment"
			BallSize = 54, _
			UseSolenoids  = True

 With Controller
	 .GameName = myExoticName

'===============================================================
' Stranger Things (original 2017)

Const TableName = "StrangerThings" : Const cGameName = "stranger_things"  ' set but not used

Dim curDir:curDir = fso.GetAbsolutePathName(".")
Dim DirName
DirName = "to be replaced"
DirName = curDir& "\" &TableName& ".UltraDMD"

Set UltraDMD = CreateObject("UltraDMD.DMDObject")
If Not fso.FolderExists(DirName) Then _
		Msgbox "UltraDMD userfiles directory '" & DirName & "' does not exist." & CHR(13) & "No graphic images will be displayed on the DMD"
UltraDMD.SetProjectFolder DirName

'===============================================================
' Star Wars (Data East 1992) 

Option Explicit
Randomize

'*******************************************
'  User Options
'*******************************************
'----- VRRoom -----
Const VRRoom = 0					'0 - VRRoom off, 1 - Minimal Room, 2 - Ultra Minimal Room, 3 - 360 Room
Const VRTop = 0						'0 - None, 1 - Vader, 2 - R2D2 (Animated)
Const Image360 = 1					'1 - Falcon, 2 - Rebel Base, 3 - Crash Site, 4 - Imperial Base, 5 - Imperial Corridor, 6 - Rebel Cruiser
'----- Other -----
Const Cabinetmode = 0				'0 - Off, 1 - On

Dim cGameName: cGameName = "stwr_107"		'Patched

Sub table1_Init
    vpmInit me
    With Controller
        .GameName = cGameName:If Err Then MsgBox"Can't start Game"&cGameName&vbNewLine&Err.Description:Exit Sub

	End With
End Sub

'===============================================================
' America's Most Haunted

'note that this scenario from America's Most Haunted does not contain rom

Sub LoadUltraDMD
	On Error Resume Next
    Set UltraDMD = CreateObject("UltraDMD.DMDObject")
    Dim fso
    Set fso = CreateObject("Scripting.FileSystemObject")
    Dim curDir
    curDir = fso.GetAbsolutePathName(".")
    Set fso = nothing
	UltraDMD.SetProjectFolder curDir & "\America's Most Haunted.UltraDMD"	' Interesting as folder contains a ' but is not the start of a comment
End Sub


'===============================================================
' The Goonies (Original 2021)

Const cGameName = "goonies"
Const TableName = "The_Goonies"
Const myVersion = "1.4"
Const MaxPlayers = 4      ' from 1 to 4
Const BallsPerGame = 3    ' usually 3 or 5

' Not in the original game, but here for the conflict with .GameName in flex sub
Sub table1_Init
    vpmInit me
    With Controller
        .GameName = "TheGoonies"
	End With
End Sub

'**************************
'   Flex DMD

Sub FlexDMDInit()
	Dim fso,curdir
	Set FlexDMD = CreateObject("FlexDMD.FlexDMD")

	With FlexDMD
		.GameName = cGameName
		.TableFile = Table1.Filename & ".vpx"
		.Color = RGB(255, 88, 32)
		.RenderMode = FlexDMD_RenderMode_DMD_GRAY_4
		.ProjectFolder = "./TheGooniesDMD/"
		.Run = True
	End With
	FlexIntro
End Sub

'**************************
'   PinUp Player USER Config

dim PuPDMDDriverType: PuPDMDDriverType=0   ' 0=LCD DMD, 1=RealDMD 2=FullDMD (large LCD)
dim useRealDMDScale : useRealDMDScale=0    ' 0 or 1 for RealDMD scaling.  Choose which one you prefer. 1 is usually best for 128x32
dim useDMDVideos    : useDMDVideos=true    ' true or false to use DMD splash videos.
Dim pGameName       : pGameName="TheGoonies"  'pupvideos foldername, probably set to cGameName in realworld

Dim PuPlayer
dim PUPDMDObject  'for realtime mirroring.
Dim pInAttract : pInAttract=false   'pAttract mode

'*************  starts PUP system,  must be called AFTER b2s/controller running so put in last line of table1_init
Sub PuPInit
    Set PuPlayer = CreateObject("PinUpPlayer.PinDisplay")   
    PuPlayer.B2SInit "", pGameName
End Sub

'===============================================================
' Cyber Race (Original 2023)

Const cGameName = "cyberrace", myVersion = "1.3.0"

LoadCoreFiles
Sub LoadCoreFiles
	ExecuteGlobal GetTextFile("controller.vbs")
    Controller.GameName = "cyber_race"             'modified for testing
	Set Controller = CreateObject("B2S.Server")
	Controller.B2SName = cGameName
	B2SOn = True
End Sub

Sub InitFlexDMD()
	Set FlexDMD = CreateObject("FlexDMD.FlexDMD")
	FlexDMD.GameName = Table1.Filename
    FlexDMD.TableFile = Table1.Filename & ".vpx"
	FlexDMD.ProjectFolder = "./CyberRaceDMD/"
	FlexDMD.Run = True

	Set DmdQ = New Queue
	Set DmdQ.FlexDMDItem = FlexDMD
	DmdQ.FlexDMDOverlayAssets = Array("BGBlack|image","BG001|image","BG006|image","BG002|image","BG003|image","BG004|image","BGSuperJackpot|video","BGRaceWon|video","BG005|image","BGBoost|video","BGBetMode|video","BGCyber|video","BGEmp|video","BGNodes|video","BGSkills|video","BGEngine|video","BGCooling|video","BGFuel|video","BGNode|video","BGNodeComplete|video","BGRace1|video","BGRace2|video","BGRace3|video","BGRace4|video","BGRace5|video","BGRace6|video""BGRaceLocked|video","BGBonus1|video","BGBonus2|video","BGBonus3|video","BGBonus4|video","BGBonus5|video","BGJackpot|video","TextSmalLine1|text","TextSmalLine2|text","TextSmalLine3|text","TextSmalLine4|text","TextSmalLine5|text","TextSmalLine6|text","TextSmalLine7|text", "Mystery0|video", "Mystery1|video", "Mystery2|video", "Mystery3|video", "Mystery4|video", "TiltWarning|video", "Tilt|video", "ExtraBall|video", "ShootAgain|video", "BGWizardMode|video", "BGHyper|video", "BGWizJackpot|video", "BGWizEnd|video")
	CreateGameDMD()
End Sub

'===============================================================
' Pink Floyd The Wall (Original 2020)

Const cGameName = "Pink_Floyd"
Const TableName = "Pink Floyd"
Const myVersion = "0.81"
Const DebugGeneral = True
Const cAssetsFolder="SEPF"
Const cDefaultDMDColor = "PowderBlue"	

Sub DMD_Init
	If turnonultradmd = 0 then exit sub
	ExecuteGlobal GetTextFile("UltraDMD_Options.vbs")
	InitUltraDMD cAssetsFolder,cGameName
End Sub

Sub Table1_Exit()
	ExitUltraDMD
End Sub
