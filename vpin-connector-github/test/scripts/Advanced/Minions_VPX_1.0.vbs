Option Explicit
Randomize

' Thalamus 2018-07-24
' Added/Updated "Positional Sound Playback Functions" and "Supporting Ball & Sound Functions"
' Thalamus 2018-11-01 : Improved directional sounds
' I'm unspired to do this table finished - not that fond of Originals
' !! NOTE : Table not verified yet !!

' Ready for Hauntfreaks animated db2s - see
' https://www.vpforums.org/index.php?app=downloads&showfile=12922
' Need FX_horn.wav to be added.

' Options
' Volume devided by - lower gets higher sound

Const VolDiv = 2000    ' Lower number, louder ballrolling/collition sound
Const VolCol = 10      ' Ball collition divider ( voldiv/volcol )

' The rest of the values are multipliers
'
'  .5 = lower volume
' 1.5 = higher volume

Const VolBump   = 2    ' Bumpers volume.
Const VolGates  = 1    ' Gates volume.
Const VolMetal  = 1    ' Metals volume.
Const VolRB     = 1    ' Rubber bands volume.
Const VolPo     = 1    ' Rubber posts volume.
Const VolPi     = 1    ' Rubber pins volume.
Const VolPlast  = 1    ' Plastics volume.
Const VolWood   = 1    ' Woods volume.
Const VolKick   = 1    ' Kicker volume.
Const VolFlip   = 1    ' Flipper volume.


On Error Resume Next
ExecuteGlobal GetTextFile("controller.vbs")
If Err Then MsgBox "You need the controller.vbs in order to run this table, available in the vp10 package"
On Error Goto 0

Const cGameName = "minions"

Const BallSize = 50 ' 50 is the normal size

' Load the core.vbs for supporting Subs and functions
LoadCoreVBS

Sub LoadCoreVBS
    On Error Resume Next
    ExecuteGlobal GetTextFile("core.vbs")
    If Err Then MsgBox "Can't open core.vbs"
    On Error Goto 0
End Sub

'************
'UltraDMD
'************
Dim UltraDMD
Dim curDir

Dim animatedBorder00
Const UltraDMD_VideoMode_Stretch = 0
Const UltraDMD_VideoMode_Top = 1
Const UltraDMD_VideoMode_Middle = 2
Const UltraDMD_VideoMode_Bottom = 3
Const UltraDMD_Animation_FadeIn = 0
Const UltraDMD_Animation_FadeOut = 1
Const UltraDMD_Animation_ZoomIn = 2
Const UltraDMD_Animation_ZoomOut = 3
Const UltraDMD_Animation_ScrollOffLeft = 4
Const UltraDMD_Animation_ScrollOffRight = 5
Const UltraDMD_Animation_ScrollOnLeft = 6
Const UltraDMD_Animation_ScrollOnRight = 7
Const UltraDMD_Animation_ScrollOffUp = 8
Const UltraDMD_Animation_ScrollOffDown = 9
Const UltraDMD_Animation_ScrollOnUp = 10
Const UltraDMD_Animation_ScrollOnDown = 11
Const UltraDMD_Animation_None = 14

Sub LoadUltraDMD
    'Set UltraDMD = CreateObject("UltraDMD.DMDObject")
	Dim FlexDMD
    Set FlexDMD = CreateObject("FlexDMD.FlexDMD")
    FlexDMD.GameName = cGameName
	Set UltraDMD = FlexDMD.NewUltraDMD()
    UltraDMD.Init

    Dim fso
    Set fso = CreateObject("Scripting.FileSystemObject")
    curDir = fso.GetAbsolutePathName(".")
    Set fso = nothing

    ' A Major version change indicates the version is no longer backward compatible
    If Not UltraDMD.GetMajorVersion = 1 Then
        MsgBox "Incompatible Version of UltraDMD found."
        Exit Sub
    End If

    'A Minor version change indicates new features that are all backward compatible
    If UltraDMD.GetMinorVersion < 0 Then
        MsgBox "Incompatible Version of UltraDMD found.  Please update to version 1.0 or newer."
        Exit Sub
    End If

    UltraDMD.SetProjectFolder curDir & "\Minions.UltraDMD"
    UltraDMD.SetVideoStretchMode UltraDMD_VideoMode_Middle
    Dim imgList
End Sub

Sub DMD_DisplaySceneEx(bkgnd,toptext,topBrightness, topOutlineBrightness, bottomtext, bottomBrightness, bottomOutlineBrightness, animateIn,pauseTime,animateOut)
    If Not UltraDMD is Nothing Then
        'Debug.Print bkgnd
        'Debug.Print toptext
        'Debug.Print topBrightness
        'Debug.Print topOutlineBrightness
        'Debug.Print bottomtext
        'Debug.Print bottomBrightness
        'Debug.Print bottomOutlineBrightness
        'Debug.Print animateIn
        'Debug.Print pauseTime
        'Debug.Print animateOut
        UltraDMD.DisplayScene00Ex bkgnd, toptext, topBrightness, topOutlineBrightness, bottomtext, bottomBrightness, bottomOutlineBrightness, animateIn, pauseTime, animateOut
        If pauseTime > 0 OR animateIn < 14 OR animateOut < 14 Then
            Timer1.Enabled = True
        End If
    End If
End Sub

Sub DMDScene (background, toptext, topbright, bottomtext, bottombright, animatein, pause, animateout, prio)		'regular DMD call with priority
	If prio >= OldDMDPrio Then
		DMDSceneInt background, toptext, topbright, bottomtext, bottombright, animatein, pause, animateout
		OldDMDPrio = prio
	End If
End Sub

Sub AttractAnimUltraDND_Timer
    Me.enabled = 0
    ShowTableInfo
End Sub

Sub BallSaverAnimT_Timer
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "BALL SAVED", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "BALL SAVED", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "BALL SAVED", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "BALL SAVED", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "BALL SAVED", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    BallSaverAnimT.enabled = 0
End Sub

Sub BallSaverAnim()
    BallSaverAnimT.enabled = 1
End sub

Sub JackpotT250_Timer
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "JACKPOT 250000", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "      ", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "JACKPOT 250000", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "      ", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "JACKPOT 250000", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "      ", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "JACKPOT 250000", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    JackpotT250.enabled = 0
End Sub

Sub Jackpot250()
	DOF 145, DOFPulse
    JackpotT250.enabled = 1
End sub

Sub OrbitAninT_Timer
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "ORBIT MAXIMUM", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "    250000    ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "ORBIT MAXIMUM", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "    250000    ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "ORBIT MAXIMUM", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "    250000    ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    OrbitAninT.enabled = 0
End Sub

Sub OrbitAnin()
    OrbitAninT.enabled = 1
End sub

Sub Targets250T_Timer
	DOF 145, DOFPulse
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     250000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     250000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     250000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    Targets250T.enabled = 0
End Sub

Sub TargetsAnim250()
    Targets250T.enabled = 1
End sub

Sub Targets150T_Timer
	DOF 145, DOFPulse
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     150000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     150000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     150000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    Targets150T.enabled = 0
End Sub

Sub TargetsAnim150()
    Targets150T.enabled = 1
End sub

Sub Targets50T_Timer
	DOF 145, DOFPulse
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     50000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     50000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     50000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    Targets50T.enabled = 0
End Sub

Sub TargetsAnim50()
    Targets50T.enabled = 1
End sub

Sub Targets10T_Timer
	DOF 145, DOFPulse
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     10000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     10000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TARGETS COMPLETE", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "     10000     ", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    Targets10T.enabled = 0
End Sub

Sub TargetsAnim10()
    Targets10T.enabled = 1
End sub

Sub MultiballJackpotAnimT_Timer
    DMD_DisplaySceneEx "DMD1.png", "MULTIBALL", 14, 2, "SHOOT JACKPOTS", -1, -1, UltraDMD_Animation_None, 400, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "MULTIBALL", 14, 2, "SHOOT JACKPOTS", -1, -1, UltraDMD_Animation_None, 400, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "MULTIBALL", 14, 2, "SHOOT JACKPOTS", -1, -1, UltraDMD_Animation_None, 400, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "MULTIBALL", 14, 2, "SHOOT JACKPOTS", -1, -1, UltraDMD_Animation_None, 400, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "MULTIBALL", 14, 2, "SHOOT JACKPOTS", -1, -1, UltraDMD_Animation_None, 400, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    MultiballJackpotAnimT.enabled = 0
End Sub

Sub MultiballJackpotAnim()
    MultiballJackpotAnimT.enabled = 1
End sub

Sub TiltAnim()
    TiltT.enabled = 1
End Sub

Sub TiltT_Timer
    TiltT.enabled = 0
  If Tilted = True Then
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "       TILT!", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TILT!     ", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    TiltAnim()
  End If
End Sub

Sub StopAnimUDMD
    Timer2.enabled = 0
Dim iScene
    UltraDMD.CancelRendering
  Select Case iScene
         Case 1 : 	DMD_DisplaySceneEx "DMD1.png", "MINIONS", 15, 4, "PINBALL", -1, -1, UltraDMD_Animation_ScrollOnUp, 5000, UltraDMD_Animation_ScrollOffDown
         Case 2 :   DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_ScrollOnUp, 100, UltraDMD_Animation_ScrollOffDown
         Case 3 :   DMD_DisplaySceneEx "DMD1.png", "CREATED BY", 15, 4, "ROM", -1, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffRight
         Case 4 :   DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_ScrollOnUp, 500, UltraDMD_Animation_ScrollOffDown
         Case 5 :   DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "       TILT!", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
         Case 6 :   DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "TILT!     ", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
  End Select

    iScene = (iScene + 1) MOD 6
End Sub

'////////////////////////////////////////////////////////////////////////////////////////////////////////////////
'----------------------------------------------------------------------------------------------------------------
'////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Sub startB2S(aB2S)
	If B2SOn Then
		Controller.B2SSetData 1,0
		Controller.B2SSetData 2,0
		Controller.B2SSetData 3,0
		Controller.B2SSetData 4,0
		Controller.B2SSetData 5,0
		Controller.B2SSetData 6,0
		Controller.B2SSetData 7,0
		Controller.B2SSetData 8,0
		Controller.B2SSetData 9,0
		Controller.B2SSetData 10,0
		Controller.B2SSetData 11,0
		Controller.B2SSetData 12,0
		Controller.B2SSetData aB2S,1
	End If
End Sub

' Define any Constants
Const TableName = "Minions"
Const myVersion = "1.0"
Const MaxPlayers = 1
Const BallSaverTime = 15 'in seconds
Const MaxMultiplier = 99 'no limit in this game
Const BallsPerGame = 3   ' 3 or 5

' Define Global Variables
Dim PlayersPlayingGame
Dim CurrentPlayer
Dim Credits
Dim Bonus
Dim BonusPoints(4)
Dim BonusMultiplier(4)

Dim BallsRemaining(4)
Dim ExtraBallsAwards(4)
Dim Score(4)
Dim HighScore(4)
Dim HighScoreName(4)
Dim Jackpot
Dim Tilt
Dim TiltSensitivity
Dim Tilted
Dim TotalGamesPlayed
Dim mBalls2Eject
Dim SkillshotValue
Dim bAutoPlunger

' Define Game Control Variables
Dim LastSwitchHit
Dim BallsOnPlayfield
Dim BallsInLock
Dim BallsInHole

' Define Game Flags
Dim bFreePlay
Dim bGameInPlay
Dim bOnTheFirstBall
Dim bBallInPlungerLane
Dim bBallSaverActive
Dim bBallSaverReady
Dim bMultiBallMode
Dim bMusicOn
Dim bSkillshotReady
Dim bExtraBallWonThisBall

Dim plungerIM 'used mostly as an autofire plunger
Dim ttable

Dim x

' *********************************************************************
'                Visual Pinball Defined Script Events
' *********************************************************************

Sub Table1_Init()
    Dim i
    Randomize
    LoadEM
    LoadUltraDMD

   For Each x in DMDOff:x.visible = 1:Next

    'Impulse Plunger as autoplunger
    Const IMPowerSetting = 50 ' Plunger Power
    Const IMTime = 1.1        ' Time in seconds for Full Plunge
    Set plungerIM = New cvpmImpulseP
    With plungerIM
        .InitImpulseP swplunger, IMPowerSetting, IMTime
        .Random 1.5
        .InitExitSnd "bumper_retro", "fx_solenoid"
        .CreateEvents "plungerIM"
    End With

'    ' Misc. VP table objects Initialisation, droptargets, animations...
    VPObjects_Init

    'load saved values, highscore, names, jackpot
    Loadhs

    'Init main variables
    For i = 1 To MaxPlayers
        Score(i) = 0
        BonusPoints(i) = 0
        BonusMultiplier(i) = 1
        BallsRemaining(i) = BallsPerGame
        ExtraBallsAwards(i) = 0
    Next

    ' initalise the DMD display
    DMD_Init

    ' freeplay or coins
    bFreePlay = False 'we want coins

    ' initialse any other flags
    bOnTheFirstBall = False
    bBallInPlungerLane = False
    bBallSaverActive = False
    bBallSaverReady = False
    bMultiBallMode = False
    bGameInPlay = False
	bAutoPlunger = False
    bMusicOn = True
    BallsOnPlayfield = 0
    BallsInLock = 0
    BallsInHole = 0
    LastSwitchHit = ""
    Tilt = 0
    TiltSensitivity = 6
    Tilted = False
    EndOfGame()
	If Credits > 0 Then DOF 128, DOFOn
End Sub

'******
' Keys
'******

Sub Table1_KeyDown(ByVal Keycode)
    If Keycode = AddCreditKey Then
        Credits = Credits + 1
		DOF 128, DOFOn
        If(Tilted = False) Then
            StopAnimUDMD
            DMD_DisplaySceneEx "DMD1.png", "CREDITS: "& Credits, 15, 4, "PRESS START", -1, -1, UltraDMD_Animation_None, 1000, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON  ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", ":  ", 15, 4, "", -1, -1, UltraDMD_Animation_None, 300, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON   ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", ":  ", 15, 4, "", -1, -1, UltraDMD_Animation_None, 300, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON   ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", ":  ", 15, 4, "", -1, -1, UltraDMD_Animation_None, 300, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON   ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMDFlush
            DMD "_", CenterLine(1, "CREDITS: " & Credits), 0, eNone, eNone, eNone, 500, True, "fx_coin"
           ' Playsound "Excellent"
            If NOT bGameInPlay Then ShowTableInfo:
        End If
    End If

    If keycode = PlungerKey Then
        PlungerIM.AutoFire
		If bBallInPlungerLane Then DOF 109, DOFPulse
    End If
    If bGameInPlay AND NOT Tilted Then
        If keycode = LeftTiltKey Then Nudge 90, 6:PlaySound "fx_nudge", 0, 1, -0.1, 0.25:CheckTilt
        If keycode = RightTiltKey Then Nudge 270, 6:PlaySound "fx_nudge", 0, 1, 0.1, 0.25:CheckTilt
        If keycode = CenterTiltKey Then Nudge 0, 7:PlaySound "fx_nudge", 0, 1, 1, 0.25:CheckTilt

        If keycode = MechanicalTilt Then Nudge 0, 4:PlaySound "fx_nudge",0,1,1,0,25:CheckTilt

        If keycode = LeftFlipperKey Then SolLFlipper 1
        If keycode = RightFlipperKey Then SolRFlipper 1

        If keycode = StartGameKey Then
            If((PlayersPlayingGame < MaxPlayers) AND(bOnTheFirstBall = True) ) Then

                If(bFreePlay = True) Then
                    PlayersPlayingGame = PlayersPlayingGame + 1
                    TotalGamesPlayed = TotalGamesPlayed + 1
                Else
                    If(Credits > 0) then
                        PlayersPlayingGame = PlayersPlayingGame + 1
                        TotalGamesPlayed = TotalGamesPlayed + 1
                        Credits = Credits - 1
						If Credits < 1 Then DOF 128, DOFOff
                    Else
                        ' Not Enough Credits to start a game.
                        StopAnimUDMD
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None

                        DMDFlush
                        DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "INSERT COIN"), 0, eNone, eBlink, eNone, 500, True, ""
                    End If
                End If
            End If
        End If
        Else ' If (GameInPlay)

            If keycode = StartGameKey Then
                If(bFreePlay = True) Then
                    If(BallsOnPlayfield = 0) Then
                        ResetForNewGame()
                    End If
                Else
                    If(Credits > 0) Then
                        If(BallsOnPlayfield = 0) Then
                            Credits = Credits - 1
							If Credits < 1 Then DOF 128, DOFOff
                            ResetForNewGame()
                        End If
                    Else
                        ' Not Enough Credits to start a game.
                        StopAnimUDMD
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
                        DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None

                        DMDFlush
                        DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "INSERT COIN"), 0, eNone, eBlink, eNone, 500, True, "Kahn_5"
                        ShowTableInfo:'AttrackModeUDMD
                    End If
                End If
            End If
    End If ' If (GameInPlay)

    If hsbModeActive Then EnterHighScoreKey(keycode)

' Table specific
End Sub

Sub Table1_KeyUp(ByVal keycode)
    If bGameInPLay AND NOT Tilted Then
        If keycode = LeftFlipperKey Then SolLFlipper 0
        If keycode = RightFlipperKey Then SolRFlipper 0
    End If
End Sub

'*************
' Pause Table
'*************

Sub table1_Paused
End Sub

Sub table1_unPaused
End Sub

Sub Table1_Exit()
	Savehs
  Controller.Pause = False
	Controller.Stop
	If Not UltraDMD is Nothing Then
		If UltraDMD.IsRendering Then
			UltraDMD.CancelRendering
		End If
		UltraDMD.Uninit
		UltraDMD = NULL
	End If
End Sub

'********************
' Special JP Flippers
'********************

Sub SolLFlipper(Enabled)
    If Enabled Then
        PlaySoundAtVol SoundFXDOF("fx_flipperup",101,DOFOn,DOFFlippers), LeftFlipper, VolFlip
        LeftFlipper.RotateToEnd
        RotateLaneLightsLeft

    Else
        PlaySoundAtVol SoundFXDOF("fx_flipperdown",101,DOFOff,DOFFlippers), LeftFlipper, VolFlip
        LeftFlipper.RotateToStart
    End If
End Sub

Sub SolRFlipper(Enabled)
    If Enabled Then
        PlaySoundAtVol SoundFXDOF("fx_flipperup",102,DOFOn,DOFFlippers), RightFlipper, VolFlip
        RightFlipper.RotateToEnd
        RightFlipper1.RotateToEnd
        RotateLaneLightsRight

    Else
        PlaySoundAtVol SoundFXDOF("fx_flipperdown",102,DOFOff,DOFFlippers), RightFlipper, VolFlip
        RightFlipper.RotateToStart
        RightFlipper1.RotateToStart
    End If
End Sub

' flippers hit Sound

Sub LeftFlipper_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, -0.05, 0.25
End Sub

Sub RightFlipper_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, 0.05, 0.25
End Sub

Sub RotateLaneLightsLeft
    Dim TempState
    TempState = SP1.State
    SP1.State = SP2.State
    SP2.State = SP3.State
    SP3.State = SP4.State
    SP4.State = TempState
End Sub

Sub RotateLaneLightsRight
    Dim TempState
    TempState = SP4.State
    SP4.State = SP3.State
    SP3.State = SP2.State
    SP2.State = SP1.State
    SP1.State = TempState
End Sub

'*********
' TILT
'*********

'NOTE: The TiltDecreaseTimer Subtracts .01 from the "Tilt" variable every round

Sub CheckTilt                                      'Called when table is nudged
    Tilt = Tilt + TiltSensitivity                  'Add to tilt count
    TiltDecreaseTimer.Enabled = True
    If(Tilt > TiltSensitivity) AND(Tilt < 15) Then 'show a warning
        DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "    CAREFUL    ", -1, -1, UltraDMD_Animation_None, 400, UltraDMD_Animation_None
        DMD "_", CenterLine(1, "CAREFUL!"), 0, eNone, eBlinkFast, eNone, 500, True, ""
    End if
    If Tilt > 15 Then 'If more that 15 then TILT the table
        Tilted = True
        'display Tilt
        TiltAnim()
        DMDFlush
        DMD CenterLine(0, "TILT!"), "", 0, eBlinkFast, eNone, eNone, 200, False, ""
        DisableTable True
        TiltRecoveryTimer.Enabled = True 'start the Tilt delay to check for all the balls to be drained
    End If
End Sub

Sub TiltDecreaseTimer_Timer
    ' DecreaseTilt
    If Tilt > 0 Then
        Tilt = Tilt - 0.1
    Else
        Me.Enabled = False
    End If
End Sub

Sub DisableTable(Enabled)
    If Enabled Then
        'turn off GI and turn off all the lights
        GiOff
        'Disable slings, bumpers etc
        LeftFlipper.RotateToStart
        RightFlipper.RotateToStart
        'Bumper1.Force = 0

        LeftSlingshot.Disabled = 1
        RightSlingshot.Disabled = 1
    Else
        'turn back on GI and the lights
        'GiOn
        'Bumper1.Force = 6
        LeftSlingshot.Disabled = 0
        RightSlingshot.Disabled = 0
        'clean up the buffer display
        DMDFlush
    End If
End Sub

Sub TiltRecoveryTimer_Timer()
    ' if all the balls have been drained then..
    If(BallsOnPlayfield = 0) Then
        ' do the normal end of ball thing (this doesn't give a bonus if the table is tilted)
        EndOfBall()
        Me.Enabled = False
    End If
' else retry (checks again in another second or so)
End Sub

'********************
' Music as wav sounds
'********************

Dim Song
Song = ""

Sub ChangeSong
    If bMusicOn Then
        StopSound Song
        If bGameInPLay = False Then
            Song = "bgout_MinionsUniversalFanfare" & ".mp3"
            PlayMusic Song
        Else
            song = "bgout_MinionsUSA" & ".mp3"
            PlayMusic Song
            If bMultiballMode Then
                StopAllMusic
                Song = "bgout_MinionsGRU" & ".mp3"
                PlayMusic Song
 '           Else
'                If bCatchemMode Then
'                    Song = "mu_catch"
                Else
                    If hsbModeActive Then 'Enter initialls
                       Song = "bgout_MinionsReallygotme" & ".mp3"
                       PlayMusic Song
                    Else
                        Song = "bgout_mu_main"
                    End If
                End If
            End If
            PlaySound Song, -1, 0.1
        End If
'    End If
End Sub

Sub Table1_MusicDone
    If bMusicOn Then
        ChangeSong
    End If
End Sub

'**********************
'     GI effects
' independent routine
' it turns on the gi
' when there is a ball
' in play
'**********************

Dim OldGiState
OldGiState = 0   'start witht the Gi off

Sub ChangeGi(col) 'changes the gi color
    Dim bulb
    For each bulb in aGILights
        SetLightColor bulb, col, -1
    Next
End Sub

Sub GIUpdateTimer_Timer
    Dim tmp, obj
    tmp = Getballs
    If UBound(tmp) <> OldGiState Then
        OldGiState = Ubound(tmp)
        If UBound(tmp) = 0 Then 'we have 4 captive balls on the table (-1 means no balls, 0 is the first ball, 1 is the second..)
            GiOff               ' turn off the gi if no active balls on the table, we could also have used the variable ballsonplayfield.
        Else
            Gion
        End If
    End If
End Sub

Sub GiOn
	DOF 133, DOFOn
    Dim bulb
    For each bulb in aGiLights
        bulb.State = 1
    Next

End Sub

Sub GiOff
	DOF 133, DOFOff
    Dim bulb
    For each bulb in aGiLights
        bulb.State = 0
    Next

End Sub

' GI & light sequence effects

Sub GiEffect(n)
    Select Case n
        Case 0 'all off
            LightSeqGi.Play SeqAlloff
        Case 1 'all blink
            LightSeqGi.UpdateInterval = 4
            LightSeqGi.Play SeqBlinking, , 5, 100
        Case 2 'random
            LightSeqGi.UpdateInterval = 10
            LightSeqGi.Play SeqRandom, 5, , 1000
        Case 3 'upon
            LightSeqGi.UpdateInterval = 4
            LightSeqGi.Play SeqUpOn, 5, 1
        Case 4 ' left-right-left
            LightSeqGi.UpdateInterval = 5
            LightSeqGi.Play SeqLeftOn, 10, 1
            LightSeqGi.UpdateInterval = 5
            LightSeqGi.Play SeqRightOn, 10, 1
    End Select
End Sub

Sub LightEffect(n)
    Select Case n
        Case 0 ' all off
            LightSeqInserts.Play SeqAlloff
        Case 1 'all blink
            LightSeqInserts.UpdateInterval = 4
            LightSeqInserts.Play SeqBlinking, , 5, 100
        Case 2 'random
            LightSeqInserts.UpdateInterval = 10
            LightSeqInserts.Play SeqRandom, 5, , 1000
        Case 3 'upon
            LightSeqInserts.UpdateInterval = 4
            LightSeqInserts.Play SeqUpOn, 10, 1
        Case 4 ' left-right-left
            LightSeqInserts.UpdateInterval = 5
            LightSeqInserts.Play SeqLeftOn, 10, 1
            LightSeqInserts.UpdateInterval = 5
            LightSeqInserts.Play SeqRightOn, 10, 1
    End Select
End Sub

' Flasher Effects

Dim FEStep, FEffect
FEStep = 0
FEffect = 0

Sub FlashEffect(n)
    Select case n
        Case 1:FEStep = 0:FEffect = 1:FlashEffectTimer.Enabled = 1 'all blink
        Case 2:FEStep = 0:FEffect = 2:FlashEffectTimer.Enabled = 1 'random
        Case 3:FEStep = 0:FEffect = 3:FlashEffectTimer.Enabled = 1 'upon
        Case 4:FEStep = 0:FEffect = 4:FlashEffectTimer.Enabled = 1 'ordered random :)
    End Select
End Sub

Sub FlashEffectTimer_Timer()
    Select Case FEffect
        Case 1
            FlashForms TRbulb1b, 2000, 40, 0
            FlashForms TRbulb2b, 2000, 40, 0
            FlashForms TRbulb3b, 2000, 40, 0
            FlashForms SPbulb1b, 2000, 40, 0
            FlashForms SPbulb2b, 2000, 40, 0
            FlashForms SPbulb3b, 2000, 40, 0
            FlashForms Overkillbulb1b, 2000, 40, 0
            FlashForms Overkillbulb2b, 2000, 40, 0
            FlashForms Overkillbulb3b, 2000, 40, 0
            FlashForms SlingFlashL, 2000, 40, 0
            FlashForms SlingFlashR, 2000, 40, 0
            FlashForms LightBumper, 2000, 40, 0
            FlashForms BulbBumper1, 2000, 40, 0
            FlashForms BulbBumper2, 2000, 40, 0
            FlashForms BulbBumper3, 2000, 40, 0
            FlashForms RampsComplete, 2000, 40, 0
            FlashForms TR1, 2000, 40, 0:FlashForms TR2, 2000, 40, 0:FlashForms TR3, 2000, 40, 0:FlashForms TR4, 2000, 40, 0
            FlashForms TRcomplete, 2000, 40, 0:FlashForms Popupcomplete, 2000, 40, 0
            FlashEffectTimer.Enabled = 0
        Case 2
            Select Case INT(RND * 19)
            Case 0:FlashForms TRbulb1b, 2000, 40, 0
            Case 2:FlashForms TRbulb2b, 2000, 40, 0
            Case 3:FlashForms TRbulb3b, 2000, 40, 0
            Case 4:FlashForms SPbulb1b, 2000, 40, 0
            Case 5:FlashForms SPbulb2b, 2000, 40, 0
            Case 6:FlashForms SPbulb3b, 2000, 40, 0
            Case 7:FlashForms Overkillbulb1b, 2000, 40, 0
            Case 8:FlashForms Overkillbulb2b, 2000, 40, 0
            Case 9:FlashForms Overkillbulb3b, 2000, 40, 0
            Case 10:FlashForms SlingFlashL, 2000, 40, 0
            Case 1:FlashForms SlingFlashR, 2000, 40, 0
            Case 12:FlashForms LightBumper, 2000, 40, 0
            Case 13:FlashForms BulbBumper1, 2000, 40, 0
            Case 14:FlashForms BulbBumper2, 2000, 40, 0
            Case 15:FlashForms BulbBumper3, 2000, 40, 0
            Case 16:FlashForms RampsComplete, 2000, 40, 0
            Case 17:FlashForms TR1, 2000, 40, 0:FlashForms TR2, 2000, 40, 0:FlashForms TR3, 2000, 40, 0:FlashForms TR4, 2000, 40, 0
            Case 18:FlashForms TRcomplete, 2000, 40, 0:FlashForms Popupcomplete, 2000, 40, 0
            End Select
            If FEStep = 30 then FlashEffectTimer.Enabled = 0
        Case 3
            Select case FEStep
                Case 0:FlashForms SlingFlashL, 2000, 40, 0
                Case 1:FlashForms SlingFlashR, 2000, 40, 0
                Case 2:FlashForms Flasher3, 2000, 40, 0
                Case 3:FlashForms Flasher4, 2000, 40, 0
                Case 4:'FlashForms Flasher5, 2000, 40, 0
                Case 5:FlashForMs TRcomplete, 1000, 40, 0
                Case 6:FlashForMs Popupcomplete, 1000, 40, 0
                Case 7:FlashForMs Tmultiballcomplete, 1000, 40, 0
                Case 8:FlashForMs RampsComplete, 1000, 40, 0
                Case 9:FlashForMs SPbulbscomplete, 1000, 40, 0
                Case 10:FlashForMs WIZARD, 1000, 40, 0
                Case 11:FlashForMs LightShootAgain, 1000, 40, 0
                Case 12:FlashEffectTimer.Enabled = 0
            End Select
        Case 4
    End Select
    FEStep = FEStep + 1
End Sub

Dim BEStep, BEffect
BEStep = 0
BEffect = 0

Sub BackFlashEffect(n)
    Select case n
        Case 1:BEStep = 0:BEffect = 1:BFlashEffectTimer.Enabled = 1 'all blink
        Case 2:BEStep = 0:BEffect = 2:BFlashEffectTimer.Enabled = 1 'random
        Case 3:BEStep = 0:BEffect = 3:BFlashEffectTimer.Enabled = 1 'Left>Right 3 times
        Case 4:BEStep = 0:BEffect = 4:BFlashEffectTimer.Enabled = 1 'Right>Left 3 times
    End Select
End Sub

Sub BFlashEffectTimer_Timer()
    Select Case BEffect
        Case 1
            FlashForms BackFlasher1, 1500, 40, 0
            FlashForms BackFlasher2, 1500, 40, 0
            FlashForms BackFlasher3, 1500, 40, 0
            FlashForms BackFlasher4, 1500, 40, 0
            FlashForms BackFlasher5, 1500, 40, 0
            FlashForms BackFlasher6, 1500, 40, 0
            FlashForms BackFlasher7, 1500, 40, 0
            BFlashEffectTimer.Enabled = 0
        Case 2
            Select Case INT(RND * 7)
                Case 0:FlashForms BackFlasher1, 500, 40, 0:DOF 156, DOFPulse
                Case 1:FlashForms BackFlasher2, 500, 40, 0:DOF 155, DOFPulse
                Case 2:FlashForms BackFlasher3, 500, 40, 0:DOF 154, DOFPulse
                Case 3:FlashForms BackFlasher4, 500, 40, 0:DOF 153, DOFPulse
                Case 4:FlashForms BackFlasher5, 500, 40, 0:DOF 152, DOFPulse
                Case 5:FlashForms BackFlasher6, 500, 40, 0:DOF 151, DOFPulse
                Case 6:FlashForms BackFlasher7, 500, 40, 0:DOF 150, DOFPulse
            End Select
            If BEStep = 32 then BFlashEffectTimer.Enabled = 0
        Case 3
            Select case BEStep
                Case 0:FlashForms BackFlasher1, 200, 40, 0
                Case 1:FlashForms BackFlasher2, 200, 40, 0
                Case 2:FlashForms BackFlasher3, 200, 40, 0
                Case 3:FlashForms BackFlasher4, 200, 40, 0
                Case 4:FlashForms BackFlasher5, 200, 40, 0
                Case 5:FlashForms BackFlasher6, 200, 40, 0
                Case 6:FlashForms BackFlasher7, 200, 40, 0
                Case 7:FlashForms BackFlasher1, 200, 40, 0
                Case 8:FlashForms BackFlasher2, 200, 40, 0
                Case 9:FlashForms BackFlasher3, 200, 40, 0
                Case 10:FlashForms BackFlasher4, 200, 40, 0
                Case 11:FlashForms BackFlasher5, 200, 40, 0
                Case 12:FlashForms BackFlasher6, 200, 40, 0
                Case 13:FlashForms BackFlasher7, 200, 40, 0
                Case 14:FlashForms BackFlasher1, 200, 40, 0
                Case 15:FlashForms BackFlasher2, 200, 40, 0
                Case 16:FlashForms BackFlasher3, 200, 40, 0
                Case 17:FlashForms BackFlasher4, 200, 40, 0
                Case 18:FlashForms BackFlasher5, 200, 40, 0
                Case 19:FlashForms BackFlasher6, 200, 40, 0
                Case 20:FlashForms BackFlasher7, 200, 40, 0
                Case 21:BFlashEffectTimer.Enabled = 0
            End Select
        Case 4
            Select case BEStep
                Case 0:FlashForms BackFlasher7, 200, 40, 0
                Case 1:FlashForms BackFlasher6, 200, 40, 0
                Case 2:FlashForms BackFlasher5, 200, 40, 0
                Case 3:FlashForms BackFlasher4, 200, 40, 0
                Case 4:FlashForms BackFlasher3, 200, 40, 0
                Case 5:FlashForms BackFlasher2, 200, 40, 0
                Case 6:FlashForms BackFlasher1, 200, 40, 0
                Case 7:FlashForms BackFlasher7, 200, 40, 0
                Case 8:FlashForms BackFlasher6, 200, 40, 0
                Case 9:FlashForms BackFlasher5, 200, 40, 0
                Case 10:FlashForms BackFlasher4, 200, 40, 0
                Case 11:FlashForms BackFlasher3, 200, 40, 0
                Case 12:FlashForms BackFlasher2, 200, 40, 0
                Case 13:FlashForms BackFlasher1, 200, 40, 0
                Case 14:FlashForms BackFlasher7, 200, 40, 0
                Case 15:FlashForms BackFlasher6, 200, 40, 0
                Case 16:FlashForms BackFlasher5, 200, 40, 0
                Case 17:FlashForms BackFlasher4, 200, 40, 0
                Case 18:FlashForms BackFlasher3, 200, 40, 0
                Case 19:FlashForms BackFlasher2, 200, 40, 0
                Case 20:FlashForms BackFlasher1, 200, 40, 0
                Case 21:BFlashEffectTimer.Enabled = 0
            End Select
    End Select
    BEStep = BEStep + 1
End Sub

'******************************
' Diverse Collection Hit Sounds
'******************************

Sub aMetals_Hit(idx):PlaySound "fx_MetalHit", 0, Vol(ActiveBall)*VolMetal, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aRubber_Bands_Hit(idx):PlaySound "fx_rubber", 0, Vol(ActiveBall)*VolRB, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aRubber_Posts_Hit(idx):PlaySound "fx_postrubber", 0, Vol(ActiveBall)*VolPo, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aRubber_Pins_Hit(idx):PlaySound "fx_postrubber", 0, Vol(ActiveBall)*VolPi, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aYellowPins_Hit(idx):PlaySound "fx_postrubber", 0, Vol(ActiveBall)*VolPi, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aPlastics_Hit(idx):PlaySound "fx_PlasticHit", 0, Vol(ActiveBall)*VolPlast, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aGates_Hit(idx):PlaySound "fx_Gate", 0, Vol(ActiveBall)*VolGates, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aWoods_Hit(idx):PlaySound "fx_Woodhit", 0, Vol(ActiveBall)*VolWood, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub
Sub aCaptiveWalls_Hit(idx):PlaySound "fx_collide", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall):End Sub

' Some quotes from the 2 first movies

Sub PlayQuote_timer() 'one quote each 2 minutes
    Dim Quote
    Quote = "gb_quote" & INT(RND * 56) + 1
    PlaySound Quote
End Sub

' Ramp Soundss
Sub RHelp1_Hit()
    StopSound "fx_metalrolling"
    PlaySoundAtVol "fx_ballrampdrop", ActiveBall, 1
End Sub

Sub RHelp2_Hit()
    StopSound "fx_metalrolling"
    PlaySoundAtVol "fx_ballrampdrop", ActiveBall, 1
End Sub

Sub LHelp2_Hit()
    StopSound "fx_metalrolling"
    PlaySoundAtVol "fx_metalrolling", ActiveBall, 1
End Sub

Sub Trigger1_hit()
    StopSound "fx_metalrolling"
    PlaySoundAtVol "fx_metalrolling", ActiveBall, 1
End Sub

Sub Trigger2_hit()
    StopSound "fx_metalrolling"
    PlaySoundAtVol "fx_metalrolling", ActiveBall, 1
End Sub


' *********************************************************************
'                        User Defined Script Events
' *********************************************************************

' Initialise the Table for a new Game
'
Sub ResetForNewGame()
    Dim i

    bGameInPLay = True

    'resets the score display, and turn off attrack mode
    StopAttractMode
    GiOn

    TotalGamesPlayed = TotalGamesPlayed + 1
    CurrentPlayer = 1
    PlayersPlayingGame = 1
    bOnTheFirstBall = True
    For i = 1 To MaxPlayers
        Score(i) = 0
        BonusPoints(i) = 0
        BonusMultiplier(i) = 1
        BallsRemaining(i) = BallsPerGame
        ExtraBallsAwards(i) = 0
        Coins(i) = 0
    Next

    ' initialise any other flags
    bMultiBallMode = False
    Tilt = 0

    ' initialise Game variables
    Game_Init()

    ' you may wish to start some music, play a sound, do whatever at this point

    ' set up the start delay to handle any Start of Game Attract Sequence
    vpmtimer.addtimer 1500, "FirstBall '"
End Sub

' This is used to delay the start of a game to allow any attract sequence to
' complete.  When it expires it creates a ball for the player to start playing with

Sub FirstBall
    ' reset the table for a new ball
    ResetForNewPlayerBall()
    ' create a new ball in the shooters lane
    CreateNewBall()
End Sub

' (Re-)Initialise the Table for a new ball (either a new ball after the player has
' lost one or we have moved onto the next player (if multiple are playing))

Sub ResetForNewPlayerBall()
    ' make sure the correct display is upto date
    AddScore 0

    ' set the current players bonus multiplier back down to 1X
    SetBonusMultiplier 1

    ' reset any drop targets, lights, game modes etc..
    'LightShootAgain.State = 0
    Bonus = 0
    bExtraBallWonThisBall = False
    ResetNewBallLights()

    'This is a new ball, so activate the ballsaver
    bBallSaverReady = True

    'and the skillshot
    'bSkillShotReady = True 'no skillshot in this game

    'Change the music ?

    'Reset any table specific
    TargetBonus = 0
    BumperBonus = 0
    PokemonBonus = 0
    HoleBonus = 0
    EggBonus = 0

if WIZARD.state = 0 then

	' Bumperlanes reset:
Bumperscore1.state = 0
Bumperscore2.state = 0
Bumperscore3.state = 0
Bumperscore4.state = 0

	' Minions reset:************************

'MinionPOPUP3DOWN.set true, 1
'MinionPOPUP2DOWN.set true, 1
'MinionPOPUP1DOWN.set true, 1

end if


End Sub

' Create a new ball on the Playfield

Sub CreateNewBall()
    ' create a ball in the plunger lane kicker.
    BallRelease.CreateSizedball BallSize / 2
'    UpdateBallImage
    ' There is a (or another) ball on the playfield
    BallsOnPlayfield = BallsOnPlayfield + 1

    ' kick it out..
    PlaySoundAtVol SoundFXDOF("fx_Ballrel",108,DOFPulse,DOFContactors), Primitive34, 1
    BallRelease.Kick 90, 4
	startB2S(5)

    ' if there is 2 or more balls then set the multibal flag (remember to check for locked balls and other balls used for animations)
	' set the bAutoPlunger flag to kick the ball in play automatically
    If BallsOnPlayfield > 1 Then
		DOF 132, DOFPulse
        bMultiBallMode = True
		bAutoPlunger = True
        ChangeSong
    End If
End Sub

' Add extra balls to the table with autoplunger
' Use it as AddMultiball 4 to add 4 extra balls to the table

Sub AddMultiball(nballs)
    mBalls2Eject = mBalls2Eject + nballs
    CreateMultiballTimer.Enabled = True
End Sub

' Eject the ball after the delay, AddMultiballDelay
Sub CreateMultiballTimer_Timer()
    ' wait if there is a ball in the plunger lane
    If bBallInPlungerLane Then
        Exit Sub
    Else
        CreateNewBall()
        mBalls2Eject = mBalls2Eject -1
        If mBalls2Eject = 0 Then 'if there are no more balls to eject then stop the timer
            Me.Enabled = False
        End If
    End If
End Sub

' The Player has lost his ball (there are no more balls on the playfield).
' Handle any bonus points awarded
'
Sub EndOfBall()
    Dim BonusDelayTime
    ' the first ball has been lost. From this point on no new players can join in
    bOnTheFirstBall = False

    ' only process any of this if the table is not tilted.  (the tilt recovery
    ' mechanism will handle any extra balls or end of game)
    If(Tilted = False) Then
        Dim AwardPoints, TotalBonus
        AwardPoints = 0:TotalBonus = 0


' add in any bonus points (multipled by the bonus multiplier)
AwardPoints = BonusPoints(CurrentPlayer) * BonusMultiplier(CurrentPlayer)
AddScore AwardPoints
'debug.print "Bonus Points = " & AwardPoints
DMD "", CenterLine(1, "BONUS: " & BonusPoints(CurrentPlayer) & " X" & BonusMultiplier(CurrentPlayer) ), 0, eNone, eBlink, eNone, 1000, True, ""
DMD_DisplaySceneEx "DMD1.png", "AWARD: " & BonusPoints(CurrentPlayer), 15, 4, "Bonus", -1, -1, UltraDMD_Animation_None, 1000, UltraDMD_Animation_None
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
'this table uses several bonus

'        DMD CenterLine(0, FormatScore(TotalBonus) ), CenterLine(1, "TOTAL BONUS " & " X" & BonusMultiplier(CurrentPlayer) ), 0, eBlinkFast, eNone, eNone, 1000, True, ""
'        TotalBonus = TotalBonus * BonusMultiplier(CurrentPlayer)

        ' add a bit of a delay to allow for the bonus points to be shown & added up
        BonusDelayTime = 3000
        vpmtimer.addtimer BonusDelayTime, "Addscore TotalBonus '"
    Else
        'no bonus to count so move quickly to the next stage
        BonusDelayTime = 100
    End If
    ' start the end of ball timer which allows you to add a delay at this point
    vpmtimer.addtimer BonusDelayTime, "EndOfBall2 '"
End Sub

' The Timer which delays the machine to allow any bonus points to be added up
' has expired.  Check to see if there are any extra balls for this player.
' if not, then check to see if this was the last ball (of the currentplayer)
'
Sub EndOfBall2()
    ' if were tilted, reset the internal tilted flag (this will also
    ' set TiltWarnings back to zero) which is useful if we are changing player LOL
    Tilted = False
    Tilt = 0
    DisableTable False 'enable again bumpers and slingshots

    ' has the player won an extra-ball ? (might be multiple outstanding)
    If(ExtraBallsAwards(CurrentPlayer) <> 0) Then
        'debug.print "Extra Ball"
        DMD "_", CenterLine(1, ("EXTRA BALL") ), "_", eNone, eBlink, eNone, 1000, True, ""

        ' yep got to give it to them
        ExtraBallsAwards(CurrentPlayer) = ExtraBallsAwards(CurrentPlayer) - 1

        ' if no more EB's then turn off any shoot again light
        If(ExtraBallsAwards(CurrentPlayer) = 0) Then
            LightShootAgain.State = 0
            playsound "Shootagain"
        End If

        ' You may wish to do a bit of a song AND dance at this point

        ' Create a new ball in the shooters lane
        CreateNewBall()
    Else ' no extra balls

        BallsRemaining(CurrentPlayer) = BallsRemaining(CurrentPlayer) - 1

        ' was that the last ball ?
        If(BallsRemaining(CurrentPlayer) <= 0) Then
            'debug.print "No More Balls, High Score Entry"

            ' Submit the currentplayers score to the High Score system
            CheckHighScore()
        ' you may wish to play some music at this point

        Else

            ' not the last ball (for that player)
            ' if multiple players are playing then move onto the next one
            EndOfBallComplete()
        End If
    End If
End Sub

' This function is called when the end of bonus display
' (or high score entry finished) AND it either end the game or
' move onto the next player (or the next ball of the same player)
'
Sub EndOfBallComplete()

    Dim NextPlayer

    'debug.print "EndOfBall - Complete"

    ' are there multiple players playing this game ?
    If(PlayersPlayingGame > 1) Then
        ' then move to the next player
        NextPlayer = CurrentPlayer + 1
        ' are we going from the last player back to the first
        ' (ie say from player 4 back to player 1)
        If(NextPlayer > PlayersPlayingGame) Then
            NextPlayer = 1
        End If
    Else
        NextPlayer = CurrentPlayer
    End If

    'debug.print "Next Player = " & NextPlayer

    ' is it the end of the game ? (all balls been lost for all players)
    If((BallsRemaining(CurrentPlayer) <= 0) AND(BallsRemaining(NextPlayer) <= 0) ) Then
        ' you may wish to do some sort of Point Match free game award here
        ' generally only done when not in free play mode

        ' set the machine into game over mode
        EndOfGame()

    ' you may wish to put a Game Over message on the desktop/backglass

    Else
        ' set the next player
        CurrentPlayer = NextPlayer

        ' make sure the correct display is up to date
        AddScore 0

        ' reset the playfield for the new player (or new ball)
        ResetForNewPlayerBall()

        ' AND create a new ball
        CreateNewBall()
    End If
End Sub

' This function is called at the End of the Game, it should reset all
' Drop targets, AND eject any 'held' balls, start any attract sequences etc..

Sub EndOfGame()
    'debug.print "End Of Game"
    bGameInPLay = False
    ' just ended your game then play the end of game tune
    ChangeSong

    ' ensure that the flippers are down
    SolLFlipper 0
    SolRFlipper 0

    ' terminate all modes - eject locked balls
TRbulb1.state = 0
FlashForMs TRbulb1b, 100, 10, 0  'off
TRbulb2.state = 0
FlashForMs TRbulb2b, 100, 10, 0  'off
TRbulb3.state = 0
FlashForMs TRbulb3b, 100, 10, 0  'off

SPbulb1.state = 0
FlashForMs SPbulb1b, 100, 10, 0  'off
SPbulb2.state = 0
FlashForMs SPbulb2b, 100, 10, 0  'off
SPbulb3.state = 0
FlashForMs SPbulb3b, 100, 10, 0  'off

Overkillbulb1.state = 0
FlashForMs Overkillbulb1b, 100, 10, 0  'off
Overkillbulb2.state = 0
FlashForMs Overkillbulb2b, 100, 10, 0  'off
Overkillbulb3.state = 0
FlashForMs Overkillbulb3b, 100, 10, 0  'off

    ' set any lights for the attract mode
    GiOff
    StartAttractMode 1
' you may wish to light any Game Over Light you may have
End Sub

Function Balls
    Dim tmp
    tmp = BallsPerGame - BallsRemaining(CurrentPlayer) + 1
    If tmp > BallsPerGame Then
        Balls = BallsPerGame
    Else
        Balls = tmp
    End If
End Function

' *********************************************************************
'                      Drain / Plunger Functions
' *********************************************************************

' lost a ball ;-( check to see how many balls are on the playfield.
' if only one then decrement the remaining count AND test for End of game
' if more than 1 ball (multi-ball) then kill of the ball but don't create
' a new one
'
Sub Drain_Hit()
    startB2S(9)
	DOF 126, DOFPulse
      TiltT.enabled = 0
      StopAnimUDMD
    ' Destroy the ball
    Drain.DestroyBall
    BallsOnPlayfield = BallsOnPlayfield - 1
    ' pretend to knock the ball into the ball storage mech
    PlaySoundAtVol "fx_drain", drain, 1

If BallsOnPlayfield = 0 And bBallSaverActive = False then
   MinionPOPUP1DOWNTimer.enabled = 1
   MinionPOPUP2DOWNTimer.enabled = 1
   MinionPOPUP3DOWNTimer.enabled = 1
End If

if ballsOnplayfield = 2 and Tmultiballcomplete.state = 2 then

Tmultiballcomplete.state = 1
StopAllMusic
Song = "bgout_MinionsLONDON" & ".mp3"
PlayMusic Song
'playSound "MinionsLONDON"
checkforWIZARD()

J1.state = 0
J2.state = 0
J3.state = 0
J4.state = 0
end if

    ' if there is a game in progress AND it is not Tilted
    If(bGameInPLay = True) AND(Tilted = False) Then

        ' is the ball saver active,
        If(bBallSaverActive = True) Then

            ' yep, create a new ball in the shooters lane
            ' we use the Addmultiball in case the multiballs are being ejected
            AddMultiball 1
			' we kick the ball with the autoplunger
			bAutoPlunger = True
            ' you may wish to put something on a display or play a sound at this point
 	 '       DMD_DisplaySceneEx "DMD1.png", "", 14, 2, "BALL SAVED", -1, -1, UltraDMD_Animation_ScrollOnUp, 1200, UltraDMD_Animation_ScrollOffDown
            BallSaverAnim
            DMD "_", CenterLine(1, "BALL SAVED"), 0, eNone, eBlinkfast, eNone, 800, True, ""
        Else
            ' cancel any multiball if on last ball (ie. lost all other balls)
            If(BallsOnPlayfield = 1) Then
                ' AND in a multi-ball??
                If(bMultiBallMode = True) then
                    ' not in multiball mode any more
                    bMultiBallMode = False
                    ChangeGi "white"
                    ' you may wish to change any music over at this point and
                    ' turn off any multiball specific lights
               '     ResetJackpotLights
                    ChangeSong
                End If
            End If

            ' was that the last ball on the playfield
            If(BallsOnPlayfield = 0) Then
                ' handle the end of ball (change player, high score entry etc..)
                EndOfBall()
                ' End Modes and timers
'                If bCatchemMode Then StopCatchem_Timer
'                If bcoinfrenzy Then StopCoinFrenzyTimer_Timer
'                If bPikachuTargetMode Then PikachuTargetTimer_Timer
'                If bCharizardMode Then StopCharizardTimer_Timer
'                If bRampBonus Then StopRampBonusTimer_Timer
'                If bLoopBonus Then StopLoopBonusTimer_Timer
'                ReduceBallType
                ChangeGi "white"
            End If
        End If
    End If
End Sub

' The Ball has rolled out of the Plunger Lane and it is pressing down the trigger in the shooters lane
' Check to see if a ball saver mechanism is needed and if so fire it up.

Sub swPlungerRest_Hit()
    'debug.print "ball in plunger lane"
    ' some sound according to the ball position
	DOF 129, DOFOn
    PlaySoundAtVol "fx_sensor", Primitive34, 1
    bBallInPlungerLane = True
    ' turn on Launch light is there is one
'    LaunchLight.State = 2
    ' kick the ball in play if the bAutoPlunger flag is on
    If bAutoPlunger Then
        'debug.print "autofire the ball"
        PlungerIM.AutoFire
		DOF 109, DOFPulse
		bAutoPlunger = False
    End If
    ' if there is a need for a ball saver, then start off a timer
    ' only start if it is ready, and it is currently not running, else it will reset the time period
    If(bBallSaverReady = True) AND(BallSaverTime <> 0) And(bBallSaverActive = False) Then
        EnableBallSaver BallSaverTime
    End If
    'Start the skillshot if ready
    If bSkillShotReady Then
        ResetSkillShotTimer.Interval = 1000 * 5 ' 5 seconds
        ResetSkillShotTimer.Enabled = True
        LightSeqSkillshot.Play SeqAllOff
        LightSeqSkillshotHit.Play SeqBlinking, , 5, 150
    'PlaySound a sound
    End If
    ' remember last trigger hit by the ball.
    LastSwitchHit = "swPlungerRest"
End Sub

' The ball is released from the plunger turn off some flags and check for skillshot

Sub swPlungerRest_UnHit()
	DOF 129, DOFOff
    startB2S(5)
    bBallInPlungerLane = False
    LaserKickP.TransY = 120
    PlaysoundAtVol "bumper_retro", Primitive34, 1
End Sub

Sub Trigger3_Hit
    PlaysoundAtVol  "fx_kicker", ActiveBall, 1
    LaserKickP.TransY = 0
End Sub

sub popupLt_hit()
    PlaySound SoundFXDOF("fx_diverter",130,DOFPulse,DOFContactors) ' TODO
    PopupL.IsDropped = 0
    PopupResetTimer.enabled = 1
end sub

sub PopupResetTimer_Timer()
    PlaySound SoundFXDOF("fx_diverter",130,DOFPulse,DOFContactors) ' TODO
    PopupL.Isdropped = 1
    PopupResetTimer.enabled = 0
end sub

Sub EnableBallSaver(seconds)
    'debug.print "Ballsaver started"
    ' set our game flag
    bBallSaverActive = True
    bBallSaverReady = False
     ' start the timer
    BallSaverTimer.Interval = 1000 * seconds
    BallSaverTimer.Enabled = True
    BallSaverSpeedUpTimer.Interval = 1000 * seconds -(1000 * seconds) / 3
    BallSaverSpeedUpTimer.Enabled = True
    ' if you have a ball saver light you might want to turn it on at this point (or make it flash)
    LightShootAgain.BlinkInterval = 160
    LightShootAgain.State = 2
End Sub

' The ball saver timer has expired.  Turn it off AND reset the game flag
'
Sub BallSaverTimer_Timer()
    debug.print "Ballsaver ended"
    BallSaverTimer.Enabled = False
    ' clear the flag
    bBallSaverActive = False
    ' if you have a ball saver light then turn it off at this point
    LightShootAgain.State = 0
End Sub

Sub BallSaverSpeedUpTimer_Timer()
    debug.print "Ballsaver Speed Up Light"
    BallSaverSpeedUpTimer.Enabled = False
    ' Speed up the blinking
    LightShootAgain.BlinkInterval = 80
    LightShootAgain.State = 2
End Sub

' *********************************************************************
'                      Supporting Score Functions
' *********************************************************************

' Add points to the score AND update the score board
'
Sub AddScore(points)
    If(Tilted = False) Then
        ' add the points to the current players score variable
        Score(CurrentPlayer) = Score(CurrentPlayer) + points' * BallType
        ' update the score displays
        DMDScore
    End if

' you may wish to check to see if the player has gotten a replay
End Sub

' Add bonus to the bonuspoints AND update the score board
'
Sub AddBonus(points)
    If(Tilted = False) Then
        ' add the bonus to the current players bonus variable
        BonusPoints(CurrentPlayer) = BonusPoints(CurrentPlayer) + points
        ' update the score displays
        DMDScore
    End if

' you may wish to check to see if the player has gotten a replay
End Sub

Sub AddCoin(n)
    If(Tilted = False) Then
        ' add the coins to the current players coin variable
        Coins(CurrentPlayer) = Coins(CurrentPlayer) + n
        ' update the score displays
        DMDScore
    End if

    ' check if there is enough coins to enable the update ball
    If Coins(CurrentPlayer) > 249 Then
        BallUpdateLight.State = 2
    Else
        BallUpdateLight.State = 0
    End If
End Sub

' Add some points to the current Jackpot.
'
Sub AddJackpot(points)
    ' Jackpots only generally increment in multiball mode AND not tilted
    ' but this doesn't have to be the case
    If(Tilted = False) Then

        If(bMultiBallMode = True) Then
            Jackpot = Jackpot + points
        ' you may wish to limit the jackpot to a upper limit, ie..
        '	If (Jackpot >= 6000) Then
        '		Jackpot = 6000
        ' 	End if
        End if
    End if
End Sub

' Will increment the Bonus Multiplier to the next level
'
Sub IncrementBonusMultiplier()
    Dim NewBonusLevel

    ' if not at the maximum bonus level
    if(BonusMultiplier(CurrentPlayer) < MaxMultiplier) then
        ' then set it the next next one AND set the lights
        NewBonusLevel = BonusMultiplier(CurrentPlayer) + 1
        SetBonusMultiplier(NewBonusLevel)
    End if
End Sub

' Set the Bonus Multiplier to the specified level AND set any lights accordingly
'
Sub SetBonusMultiplier(Level)
    ' Set the multiplier to the specified level
    BonusMultiplier(CurrentPlayer) = Level

    ' If the multiplier is 1 then turn off all the bonus lights
    If(BonusMultiplier(CurrentPlayer) = 1) Then
 '       LightBonus2x.State = 0
 '       LightBonus3x.State = 0
 '       LightBigBonus.State = 0
    Else
        ' there is a bonus, turn on all the lights upto the current level
        If(BonusMultiplier(CurrentPlayer) >= 2) Then
            If(BonusMultiplier(CurrentPlayer) >= 2) Then
  '              LightBonus2x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 3) Then
   '             LightBonus3x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 4) Then
   '             LightBigBonus.state = 1
            End If
        End If
    ' etc..
    End If
End Sub

Sub IncrementBonus(Amount)
    Dim Value
    AddBonus Amount * 1000
    Bonus = Bonus + Amount
End Sub

Sub AwardExtraBall()
    If NOT bExtraBallWonThisBall Then
        DMD "_", Centerline(1, ("EXTRA BALL WON") ), 0, eNone, eBlink, eNone, 1000, True, SoundFXDOF("fx_knocker",127,DOFPulse,DOFKnocker)
        ExtraBallsAwards(CurrentPlayer) = ExtraBallsAwards(CurrentPlayer) + 1
        bExtraBallWonThisBall = True
        GiEffect 1
        LightEffect 2
    END If
End Sub

Sub AwardSpecial()
    DMD "_", Centerline(1, ("EXTRA GAME WON") ), 0, eNone, eBlink, eNone, 1000, True, SoundFXDOF("fx_knocker",127,DOFPulse,DOFKnocker)
    Credits = Credits + 1
	DOF 128, DOFOn
    GiEffect 1
    LightEffect 1
End Sub

Sub AwardJackpot()
    DMD CenterLine(0, FormatScore(Jackpot) ), Centerline(1, ("JACKPOT") ), 0, eBlinkFast, eBlinkFast, eNone, 1000, True, ""
    PlayFanfare
    AddScore Jackpot
    AddJackpot 100000
    GiEffect 1
    LightEffect 2
End Sub

Sub AwardSuperJackpot()
    DMD CenterLine(0, FormatScore(Jackpot * PokemonLevel) ), Centerline(1, ("SUPERJACKPOT") ), 0, eBlinkFast, eBlinkFast, eNone, 1000, True, ""
    PlayFanfare
    AddScore Jackpot * PokemonLevel
    AddJackpot 100000
    GiEffect 1
    LightEffect 2
End Sub

Sub AwardSkillshot()
    DMD CenterLine(0, FormatScore(SkillshotValue) ), Centerline(1, ("SKILLSHOT") ), 0, eBlinkFast, eBlink, eNone, 1000, True, ""
    AddScore SkillshotValue
    ResetSkillShotTimer_Timer
End Sub

'*****************************
'    Load / Save / Highscore
'*****************************

Sub Loadhs
    Dim x
    x = LoadValue(TableName, "HighScore1")
    If(x <> "") Then HighScore(0) = CDbl(x) Else HighScore(0) = 100000 End If
    x = LoadValue(TableName, "HighScore1Name")
    If(x <> "") Then HighScoreName(0) = x Else HighScoreName(0) = "AAA" End If
    x = LoadValue(TableName, "HighScore2")
    If(x <> "") then HighScore(1) = CDbl(x) Else HighScore(1) = 100000 End If
    x = LoadValue(TableName, "HighScore2Name")
    If(x <> "") then HighScoreName(1) = x Else HighScoreName(1) = "BBB" End If
    x = LoadValue(TableName, "HighScore3")
    If(x <> "") then HighScore(2) = CDbl(x) Else HighScore(2) = 100000 End If
    x = LoadValue(TableName, "HighScore3Name")
    If(x <> "") then HighScoreName(2) = x Else HighScoreName(2) = "CCC" End If
    x = LoadValue(TableName, "HighScore4")
    If(x <> "") then HighScore(3) = CDbl(x) Else HighScore(3) = 100000 End If
    x = LoadValue(TableName, "HighScore4Name")
    If(x <> "") then HighScoreName(3) = x Else HighScoreName(3) = "DDD" End If
    x = LoadValue(TableName, "Credits")
    If(x <> "") then Credits = CInt(x) Else Credits = 0 End If
    'x = LoadValue(TableName, "Jackpot")
    'If(x <> "") then Jackpot = CDbl(x) Else Jackpot = 200000 End If
    x = LoadValue(TableName, "TotalGamesPlayed")
    If(x <> "") then TotalGamesPlayed = CInt(x) Else TotalGamesPlayed = 0 End If
End Sub

Sub Savehs
    SaveValue TableName, "HighScore1", HighScore(0)
    SaveValue TableName, "HighScore1Name", HighScoreName(0)
    SaveValue TableName, "HighScore2", HighScore(1)
    SaveValue TableName, "HighScore2Name", HighScoreName(1)
    SaveValue TableName, "HighScore3", HighScore(2)
    SaveValue TableName, "HighScore3Name", HighScoreName(2)
    SaveValue TableName, "HighScore4", HighScore(3)
    SaveValue TableName, "HighScore4Name", HighScoreName(3)
    SaveValue TableName, "Credits", Credits
    'SaveValue TableName, "Jackpot", Jackpot
    SaveValue TableName, "TotalGamesPlayed", TotalGamesPlayed
End Sub



' ***********************************************************
'  High Score Initals Entry Functions - based on Black's code
' ***********************************************************

Dim hsbModeActive
Dim hsEnteredName
Dim hsEnteredDigits(3)
Dim hsCurrentDigit
Dim hsValidLetters
Dim hsCurrentLetter
Dim hsLetterFlash

Sub CheckHighscore()
    Dim tmp
    tmp = Score(1)
    If Score(2) > tmp Then tmp = Score(2)
    If Score(3) > tmp Then tmp = Score(3)
    If Score(4) > tmp Then tmp = Score(4)

    If tmp > HighScore(1) Then 'add 1 credit for beating the highscore
        Credits = Credits + 1
		DOF 128, DOFOn
    End If

    If tmp > HighScore(3) Then
        PlaySound SoundFXDOF("fx_knocker",127,DOFPulse,DOFKnocker)
		DOF 117, DOFPulse
        HighScore(3) = tmp
        'enter player's name
        HighScoreEntryInit()
    Else
        EndOfBallComplete()
    End If
End Sub

Sub HighScoreEntryInit()
    For Each x in DMDOff:x.visible = 1:Next
    hsbModeActive = True
    ChangeSong
    hsLetterFlash = 0

    hsEnteredDigits(0) = " "
    hsEnteredDigits(1) = " "
    hsEnteredDigits(2) = " "
    hsCurrentDigit = 0

    hsValidLetters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ'<>*+-/=\^0123456789`" ' ` is back arrow
    hsCurrentLetter = 1
    DMDFlush()
    HighScoreDisplayNameNow()

    HighScoreFlashTimer.Interval = 250
    HighScoreFlashTimer.Enabled = True
End Sub

Sub EnterHighScoreKey(keycode)
    If keycode = LeftFlipperKey Then
        playsound "fx_Previous"
        hsCurrentLetter = hsCurrentLetter - 1
        if(hsCurrentLetter = 0) then
            hsCurrentLetter = len(hsValidLetters)
        end if
        HighScoreDisplayNameNow()
    End If

    If keycode = RightFlipperKey Then
        playsound "fx_Next"
        hsCurrentLetter = hsCurrentLetter + 1
        if(hsCurrentLetter > len(hsValidLetters) ) then
            hsCurrentLetter = 1
        end if
        HighScoreDisplayNameNow()
    End If

    If keycode = PlungerKey Then
        if(mid(hsValidLetters, hsCurrentLetter, 1) <> "`") then
            playsound "fx_Enter"
            hsEnteredDigits(hsCurrentDigit) = mid(hsValidLetters, hsCurrentLetter, 1)
            hsCurrentDigit = hsCurrentDigit + 1
            if(hsCurrentDigit = 3) then
                HighScoreCommitName()
            else
                HighScoreDisplayNameNow()
            end if
        else
            playsound "fx_Esc"
            hsEnteredDigits(hsCurrentDigit) = " "
            if(hsCurrentDigit > 0) then
                hsCurrentDigit = hsCurrentDigit - 1
            end if
            HighScoreDisplayNameNow()
        end if
    end if
End Sub

Sub HighScoreDisplayNameNow()
    HighScoreFlashTimer.Enabled = False
    hsLetterFlash = 0
    HighScoreDisplayName()
    HighScoreFlashTimer.Enabled = True
End Sub

Sub HighScoreDisplayName()
    Dim i
    Dim TempTopStr
    Dim TempBotStr

    TempTopStr = "YOUR NAME:"
    dLine(0) = ExpandLine(TempTopStr, 0)
    DMDUpdate 0

    TempBotStr = "    > "
    if(hsCurrentDigit > 0) then TempBotStr = TempBotStr & hsEnteredDigits(0)
    if(hsCurrentDigit > 1) then TempBotStr = TempBotStr & hsEnteredDigits(1)
    if(hsCurrentDigit > 2) then TempBotStr = TempBotStr & hsEnteredDigits(2)

    if(hsCurrentDigit <> 3) then
        if(hsLetterFlash <> 0) then
            TempBotStr = TempBotStr & "_"
        else
            TempBotStr = TempBotStr & mid(hsValidLetters, hsCurrentLetter, 1)
        end if
    end if

    if(hsCurrentDigit < 1) then TempBotStr = TempBotStr & hsEnteredDigits(1)
    if(hsCurrentDigit < 2) then TempBotStr = TempBotStr & hsEnteredDigits(2)

    TempBotStr = TempBotStr & " <    "
    dLine(1) = ExpandLine(TempBotStr, 1)
    DMDUpdate 1
End Sub

Sub HighScoreFlashTimer_Timer()
    HighScoreFlashTimer.Enabled = False
    hsLetterFlash = hsLetterFlash + 1
    if(hsLetterFlash = 2) then hsLetterFlash = 0
    HighScoreDisplayName()
    HighScoreFlashTimer.Enabled = True
End Sub

Sub HighScoreCommitName()
    HighScoreFlashTimer.Enabled = False
    hsbModeActive = False
    ChangeSong
    hsEnteredName = hsEnteredDigits(0) & hsEnteredDigits(1) & hsEnteredDigits(2)
    if(hsEnteredName = "   ") then
        hsEnteredName = "YOU"
    end if

    HighScoreName(3) = hsEnteredName
    SortHighscore
    EndOfBallComplete()
End Sub

Sub SortHighscore
    Dim tmp, tmp2, i, j
    For i = 0 to 3
        For j = 0 to 2
            If HighScore(j) < HighScore(j + 1) Then
                tmp = HighScore(j + 1)
                tmp2 = HighScoreName(j + 1)
                HighScore(j + 1) = HighScore(j)
                HighScoreName(j + 1) = HighScoreName(j)
                HighScore(j) = tmp
                HighScoreName(j) = tmp2
            End If
        Next
    Next
End Sub

' *********************************************************************
'   JP's Reduced Display Driver Functions (based on script by Black)
' only 5 effects: none, scroll left, scroll right, blink and blinkfast
' 3 Lines, treats all 3 lines as text. 3rd line is just 1 character
' Example format:
' DMD "text1","text2",number, eNone, eNone, eNone, 250, True, "sound"
' Short names:
' dq = display queue
' de = display effect
' *********************************************************************

Const eNone = 0        ' Instantly displayed
Const eScrollLeft = 1  ' scroll on from the right
Const eScrollRight = 2 ' scroll on from the left
Const eBlink = 3       ' Blink (blinks for 'TimeOn')
Const eBlinkFast = 4   ' Blink (blinks for 'TimeOn') at user specified intervals (fast speed)

Const dqSize = 64

Dim dqHead
Dim dqTail
Dim deSpeed
Dim deBlinkSlowRate
Dim deBlinkFastRate

Dim dCharsPerLine(2)
Dim dLine(2)
Dim deCount(2)
Dim deCountEnd(2)
Dim deBlinkCycle(2)

Dim dqText(2, 64)
Dim dqEffect(2, 64)
Dim dqTimeOn(64)
Dim dqbFlush(64)
Dim dqSound(64)

Sub DMD_Init() 'default/startup values
    Dim i, j
    DMDFlush()
    deSpeed = 20
    deBlinkSlowRate = 5
    deBlinkFastRate = 2
    dCharsPerLine(0) = 16
    dCharsPerLine(1) = 20
    dCharsPerLine(2) = 1
    For i = 0 to 2
        dLine(i) = Space(dCharsPerLine(i) )
        deCount(i) = 0
        deCountEnd(i) = 0
        deBlinkCycle(i) = 0
        dqTimeOn(i) = 0
        dqbFlush(i) = True
        dqSound(i) = ""
    Next
    For i = 0 to 2
        For j = 0 to 64
            dqText(i, j) = ""
            dqEffect(i, j) = eNone
        Next
    Next
    DMD dLine(0), dLine(1), dLine(2), eNone, eNone, eNone, 25, True, ""
End Sub

Sub DMDFlush()
    Dim i
    DMDTimer.Enabled = False
    DMDEffectTimer.Enabled = False
    dqHead = 0
    dqTail = 0
    For i = 0 to 2
        deCount(i) = 0
        deCountEnd(i) = 0
        deBlinkCycle(i) = 0
    Next
End Sub

Sub DMDScore()
    Dim tmp, tmp1, tmp2
    if(dqHead = dqTail) Then
        tmp = RightLine(0, FormatScore(Score(Currentplayer) ) )
        'tmp = CenterLine(0, FormatScore(Score(Currentplayer) ) )
 '       tmp1 = CenterLine(1, "PLAYER" & CurrentPlayer & " j" & Balls & " l" & PokemonBonus & " k" & Coins(Currentplayer) )
        UltraDMD.SetScoreboardBackgroundImage "DMD1.png", 15, 15
        UltraDMD.DisplayScoreboard 1, 1, "" & Score(Currentplayer) , 2000000, 3000000, 4000000, "BALL "& Balls, "CREDITS:" & Credits
        tmp1 = CenterLine(1, "PLAYER " & CurrentPlayer & " BALL " & Balls)
        'tmp1 = FormatScore(Bonuspoints(Currentplayer) ) & " X" &BonusMultiplier(Currentplayer)
        tmp2 = 0
 '       If bCatchemMode Then
 '           tmp1 = RightLine(1, CatchMaxHits-CatchHits & " HITS LEFT")
 '           tmp2 = CatchID
 '       End If
    End If
    DMD tmp, tmp1, tmp2, eNone, eNone, eNone, 25, True, ""
End Sub

Sub DMD(Text0, Text1, Text2, Effect0, Effect1, Effect2, TimeOn, bFlush, Sound)
    if(dqTail < dqSize) Then
        if(Text0 = "_") Then
            dqEffect(0, dqTail) = eNone
            dqText(0, dqTail) = "_"
        Else
            dqEffect(0, dqTail) = Effect0
            dqText(0, dqTail) = ExpandLine(Text0, 0)
        End If

        if(Text1 = "_") Then
            dqEffect(1, dqTail) = eNone
            dqText(1, dqTail) = "_"
        Else
            dqEffect(1, dqTail) = Effect1
            dqText(1, dqTail) = ExpandLine(Text1, 1)
        End If

        if(Text2 = "_") Then
            dqEffect(2, dqTail) = eNone
            dqText(2, dqTail) = "_"
        Else
            dqEffect(2, dqTail) = Effect2
            dqText(2, dqTail) = Text2 'it is always 1 letter
        End If

        dqTimeOn(dqTail) = TimeOn
        dqbFlush(dqTail) = bFlush
        dqSound(dqTail) = Sound
        dqTail = dqTail + 1
        if(dqTail = 1) Then
            DMDHead()
        End If
    End If
End Sub

Sub DMDHead()
    Dim i
    deCount(0) = 0
    deCount(1) = 0
    deCount(2) = 0
    DMDEffectTimer.Interval = deSpeed

    For i = 0 to 2
        Select Case dqEffect(i, dqHead)
            Case eNone:deCountEnd(i) = 1
            Case eScrollLeft:deCountEnd(i) = Len(dqText(i, dqHead) )
            Case eScrollRight:deCountEnd(i) = Len(dqText(i, dqHead) )
            Case eBlink:deCountEnd(i) = int(dqTimeOn(dqHead) / deSpeed)
                deBlinkCycle(i) = 0
            Case eBlinkFast:deCountEnd(i) = int(dqTimeOn(dqHead) / deSpeed)
                deBlinkCycle(i) = 0
        End Select
    Next
    if(dqSound(dqHead) <> "") Then
        PlaySound(dqSound(dqHead) )
    End If
    DMDEffectTimer.Enabled = True
End Sub

Sub DMDEffectTimer_Timer()
    DMDEffectTimer.Enabled = False
    DMDProcessEffectOn()
End Sub

Sub DMDTimer_Timer()
    Dim Head
    DMDTimer.Enabled = False
    Head = dqHead
    dqHead = dqHead + 1
    if(dqHead = dqTail) Then
        if(dqbFlush(Head) = True) Then
            DMDFlush()
            DMDScore()
        Else
            dqHead = 0
            DMDHead()
        End If
    Else
        DMDHead()
    End If
End Sub

Sub DMDProcessEffectOn()
    Dim i
    Dim BlinkEffect
    Dim Temp

    BlinkEffect = False

    For i = 0 to 2
        if(deCount(i) <> deCountEnd(i) ) Then
            deCount(i) = deCount(i) + 1

            select case(dqEffect(i, dqHead) )
                case eNone:
                    Temp = dqText(i, dqHead)
                case eScrollLeft:
                    Temp = Right(dLine(i), dCharsPerLine(i) - 1)
                    Temp = Temp & Mid(dqText(i, dqHead), deCount(i), 1)
                case eScrollRight:
                    Temp = Mid(dqText(i, dqHead), (dCharsPerLine(i) + 1) - deCount(i), 1)
                    Temp = Temp & Left(dLine(i), dCharsPerLine(i) - 1)
                case eBlink:
                    BlinkEffect = True
                    if((deCount(i) MOD deBlinkSlowRate) = 0) Then
                        deBlinkCycle(i) = deBlinkCycle(i) xor 1
                    End If

                    if(deBlinkCycle(i) = 0) Then
                        Temp = dqText(i, dqHead)
                    Else
                        Temp = Space(dCharsPerLine(i) )
                    End If
                case eBlinkFast:
                    BlinkEffect = True
                    if((deCount(i) MOD deBlinkFastRate) = 0) Then
                        deBlinkCycle(i) = deBlinkCycle(i) xor 1
                    End If

                    if(deBlinkCycle(i) = 0) Then
                        Temp = dqText(i, dqHead)
                    Else
                        Temp = Space(dCharsPerLine(i) )
                    End If
            End Select

            if(dqText(i, dqHead) <> "_") Then
                dLine(i) = Temp
                DMDUpdate i
            End If
        End If
    Next

    if(deCount(0) = deCountEnd(0) ) and(deCount(1) = deCountEnd(1) ) and(deCount(2) = deCountEnd(2) ) Then

        if(dqTimeOn(dqHead) = 0) Then
            DMDFlush()
        Else
            if(BlinkEffect = True) Then
                DMDTimer.Interval = 10
            Else
                DMDTimer.Interval = dqTimeOn(dqHead)
            End If

            DMDTimer.Enabled = True
        End If
    Else
        DMDEffectTimer.Enabled = True
    End If
End Sub

Function ExpandLine(TempStr, id) 'id is the number of the dmd line
    If TempStr = "" Then
        TempStr = Space(dCharsPerLine(id) )
    Else
        if(Len(TempStr) > Space(dCharsPerLine(id) ) ) Then
            TempStr = Left(TempStr, Space(dCharsPerLine(id) ) )
        Else
            if(Len(TempStr) < dCharsPerLine(id) ) Then
                TempStr = TempStr & Space(dCharsPerLine(id) - Len(TempStr) )
            End If
        End If
    End If
    ExpandLine = TempStr
End Function

Function FormatScore(ByVal Num) 'it returns a string with commas (as in Black's original font)
    dim i
    dim NumString

    NumString = CStr(abs(Num) )

    For i = Len(NumString) -3 to 1 step -3
        if IsNumeric(mid(NumString, i, 1) ) then
            NumString = left(NumString, i-1) & chr(asc(mid(NumString, i, 1) ) + 48) & right(NumString, Len(NumString) - i)
        end if
    Next
    FormatScore = NumString
End function

Function CenterLine(id, NumString)
    Dim Temp, TempStr
    Temp = (dCharsPerLine(id) - Len(NumString) ) \ 2
    If(Temp + Temp + Len(NumString) ) < dCharsPerLine(id) Then
        TempStr = " " & Space(Temp) & NumString & Space(Temp)
    Else
        TempStr = Space(Temp) & NumString & Space(Temp)
    End If
    CenterLine = TempStr
End Function

Function RightLine(id, NumString)
    Dim Temp, TempStr
    Temp = dCharsPerLine(id) - Len(NumString)
    TempStr = Space(Temp) & NumString
    RightLine = TempStr
End Function

'*********************
' Update DMD - reels
'*********************

Dim DesktopMode:DesktopMode = Table1.ShowDT

Dim Digits(2)

DMDReels_Init

Sub DMDReels_Init
    If DesktopMode Then
        'Desktop
        Digits(0) = Array(EMReel36, EMReel35, EMReel34, EMReel33, EMReel32, EMReel31, EMReel0, EMReel1, EMReel2, EMReel3, EMReel4, EMReel5, EMReel6, EMReel7, EMReel8, EMReel9)
        Digits(1) = Array(EMReel10, EMReel11, EMReel12, EMReel13, EMReel14, EMReel15, EMReel16, EMReel17, EMReel18, EMReel19, EMReel20, EMReel21, EMReel22, EMReel23, EMReel24, EMReel25, EMReel26, EMReel27, EMReel28, EMReel29)
        Digits(2) = Array(EMReel30)
        EMReel37.visible = 0
    Else
        'FS
        Digits(0) = Array(EMReel73, EMReel72, EMReel71, EMReel70, EMReel69, EMReel68, EMReel39, EMReel40, EMReel41, EMReel42, EMReel43, EMReel44, EMReel45, EMReel46, EMReel47, EMReel38)
        Digits(1) = Array(EMReel57, EMReel58, EMReel59, EMReel60, EMReel61, EMReel62, EMReel63, EMReel64, EMReel65, EMReel66, EMReel67, EMReel56, EMReel48, EMReel49, EMReel50, EMReel51, EMReel52, EMReel53, EMReel54, EMReel55)
        Digits(2) = Array(EMReel37)
        EMReel30.visible = 0
    End If
End Sub

Sub DMDUpdate(id)
    Dim digit, value
    If id < 2 Then 'text reels
        For digit = 0 to dCharsPerLine(id) -1
            value = ASC(mid(dLine(id), digit + 1, 1) ) -32
            Digits(id) (digit).SetValue value
        Next
    Else 'backdrop reel for animatons
        If dLine(2) = "" OR dLine(2) = " " Then
            value = 0
        Else
            value = dLine(2)
        End If
        Digits(2) (0).SetValue value
    End If
End Sub

'****************************************
' Real Time updatess using the GameTimer
'****************************************
'used for all the real time updates

Sub GameTimer_Timer
    RollingUpdate
    Minion4.RotY = -(Spinner1.currentangle)
    If DesktopMode = 0 Then

'       UltraDMD.DisplayScene01 "Presentation", "backdmd.gif", "SCENE CANCEL TEST", -1, 13, UltraDMD_Animation_ScrollOnUp, 25000, UltraDMD_Animation_None
'       UltraDMD.SetScoreboardBackgroundImage "backdmd.gif", 15, 15
'       UltraDMD.DisplayScoreboard PlayersPlayingGame, CurrentPlayer, Score(Currentplayer), Score(2), Score(3), Score(4), "BALL " & Balls, "CREDITS:" & Credits
    End If
End Sub




'********************************************************************************************
' Only for VPX 10.2 and higher.
' FlashForMs will blink light or a flasher for TotalPeriod(ms) at rate of BlinkPeriod(ms)
' When TotalPeriod done, light or flasher will be set to FinalState value where
' Final State values are:   0=Off, 1=On, 2=Return to previous State
'********************************************************************************************

Sub FlashForMs(MyLight, TotalPeriod, BlinkPeriod, FinalState) 'thanks gtxjoe for the first myVersion

    If TypeName(MyLight) = "Light" Then

        If FinalState = 2 Then
            FinalState = MyLight.State 'Keep the current light state
        End If
        MyLight.BlinkInterval = BlinkPeriod
        MyLight.Duration 2, TotalPeriod, FinalState
    ElseIf TypeName(MyLight) = "Flasher" Then

        Dim steps

        ' Store all blink information
        steps = Int(TotalPeriod / BlinkPeriod + .5) 'Number of ON/OFF steps to perform
        If FinalState = 2 Then                      'Keep the current flasher state
            FinalState = ABS(MyLight.Visible)
        End If
        MyLight.UserValue = steps * 10 + FinalState 'Store # of blinks, and final state

        ' Start blink timer and create timer subroutine
        MyLight.TimerInterval = BlinkPeriod
        MyLight.TimerEnabled = 0
        MyLight.TimerEnabled = 1
        ExecuteGlobal "Sub " & MyLight.Name & "_Timer:" & "Dim tmp, steps, fstate:tmp=me.UserValue:fstate = tmp MOD 10:steps= tmp\10 -1:Me.Visible = steps MOD 2:me.UserValue = steps *10 + fstate:If Steps = 0 then Me.Visible = fstate:Me.TimerEnabled=0:End if:End Sub"
    End If
End Sub



'******************************************
' Change light color - simulate color leds
' changes the light color and state
' colors: red, orange, yellow, green, blue, white
'******************************************

Sub SetLightColor(n, col, stat)
    Select Case col
        Case "red"
            n.color = RGB(18, 0, 0)
            n.colorfull = RGB(255, 0, 0)
        Case "orange"
            n.color = RGB(18, 3, 0)
            n.colorfull = RGB(255, 64, 0)
        Case "yellow"
            n.color = RGB(18, 18, 0)
            n.colorfull = RGB(255, 255, 0)
        Case "green"
            n.color = RGB(0, 18, 0)
            n.colorfull = RGB(0, 255, 0)
        Case "blue"
            n.color = RGB(0, 18, 18)
            n.colorfull = RGB(0, 255, 255)
        Case "white"
            n.color = RGB(193, 91, 0)
            n.colorfull = RGB(255, 252, 224)
    End Select
    If stat <> -1 Then
        n.State = 0
        n.State = stat
    End If
End Sub

' ********************************
'   Table info & Attract Mode
' ********************************

Sub ShowTableInfo
    'info goes in a loop only stopped by the credits and the startkey
    If Score(1) Then
       DMD CenterLine(0, "LAST SCORE"), CenterLine(1, "PLAYER1 " &FormatScore(Score(1) ) ), 0, eNone, eNone, eNone, 3000, False, ""
       DMD_DisplaySceneEx "DMD1.png", "LAST SCORE", 15, 4, "Score "&Score(1) , -1, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
       DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    End If
    If Score(2) Then
       DMD CenterLine(0, "LAST SCORE"), CenterLine(1, "PLAYER2 " &FormatScore(Score(2) ) ), 0, eNone, eNone, eNone, 3000, False, ""
       DMD_DisplaySceneEx "DMD1.png", "LAST SCORE", 15, 4, "Score "&Score(2) , -1, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
       DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    End If
    If Score(3) Then
       DMD CenterLine(0, "LAST SCORE"), CenterLine(1, "PLAYER3 " &FormatScore(Score(3) ) ), 0, eNone, eNone, eNone, 3000, False, ""
       DMD_DisplaySceneEx "DMD1.png", "LAST SCORE", 15, 4, "Score "&Score(3) , -1, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
       DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    End If
    If Score(4) Then
       DMD CenterLine(0, "LAST SCORE"), CenterLine(1, "PLAYER4 " &FormatScore(Score(4) ) ), 0, eNone, eNone, eNone, 3000, False, ""
       DMD_DisplaySceneEx "DMD1.png", "LAST SCORE", 15, 4, "Score "&Score(4) , -1, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
       DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
    End If
    DMD "", "", 3, eNone, eNone, eBlink, 2000, False, ""

        If Credits > 0 Then
            DMD_DisplaySceneEx "DMD1.png", "CREDITS: "& Credits, 15, 4, "PRESS START", -1, -1, UltraDMD_Animation_None, 1000, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON  ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", ":  ", 15, 4, "", -1, -1, UltraDMD_Animation_None, 300, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON   ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", ":  ", 15, 4, "", -1, -1, UltraDMD_Animation_None, 300, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON   ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", ":  ", 15, 4, "", -1, -1, UltraDMD_Animation_None, 300, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "PRESS START", 15, 4, "   BUTTON   ", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
        Else
            DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "INSERT COIN"), 0, eNone, eBlink, eNone, 2000, False, ""
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, " ", -1, -1, UltraDMD_Animation_None, 50, UltraDMD_Animation_None
            DMD_DisplaySceneEx "DMD1.png", "CREDITS "& Credits, 15, 4, "INSERT COIN", -1, -1, UltraDMD_Animation_None, 700, UltraDMD_Animation_None
       End If


    DMD_DisplaySceneEx "DMD1.png", "JAVIER", 15, 4, "PRESENT", -1, -1, UltraDMD_Animation_ScrollOnLeft, 2000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
 	DMD_DisplaySceneEx "DMD1.png", "MINIONS", 15, 4, "PINBALL", -1, -1, UltraDMD_Animation_ScrollOnLeft, 2000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "CREATED BY", 15, 4, "ROM", -1, -1, UltraDMD_Animation_ScrollOnLeft, 2000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None

    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_ScrollOnLeft, 800, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "", -1, -1, UltraDMD_Animation_None, 100, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 5, UltraDMD_Animation_None
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "1>"&HighScoreName(0) & " " &HighScore(0) , -2, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "2>"&HighScoreName(1) & " " &HighScore(1) , -2, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "3>"&HighScoreName(2) & " " &HighScore(2) , -2, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "HIGHSCORES", 15, 4, "4>"&HighScoreName(3) & " " &HighScore(3) , -2, -1, UltraDMD_Animation_ScrollOnLeft, 3000, UltraDMD_Animation_ScrollOffLeft
    DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "", -1, -1, UltraDMD_Animation_None, 3000, UltraDMD_Animation_None
    UltraDMD.DisplayScoreboard PlayersPlayingGame, CurrentPlayer, Score(1), Score(2), Score(3), Score(4), "Ball " & Balls, "Credits: " & Credits
    AttractAnimUltraDND.enabled = 1


    DMD CenterLine(0, "JAVIER & ROM "), CenterLine(1, "PRESENTS"), 0, eNone, eNone, eNone, 3000, False, ""
    DMD CenterLine(0, "MINIONS "), CenterLine(1, "PINBALL  "), 0, eNone, eNone, eNone, 3000, False, ""
    DMD CenterLine(0, "HIGHSCORES"), Space(dCharsPerLine(1) ), 0, eScrollLeft, eScrollLeft, eNone, 20, False, ""
    DMD CenterLine(0, "HIGHSCORES"), "", 0, eBlinkFast, eNone, eNone, 1000, False, ""
    DMD CenterLine(0, "HIGHSCORES"), "1> " &HighScoreName(0) & " " &FormatScore(HighScore(0) ), 0, eNone, eScrollLeft, eNone, 2000, False, ""
    DMD "_", "2> " &HighScoreName(1) & " " &FormatScore(HighScore(1) ), 0, eNone, eScrollLeft, eNone, 2000, False, ""
    DMD "_", "3> " &HighScoreName(2) & " " &FormatScore(HighScore(2) ), 0, eNone, eScrollLeft, eNone, 2000, False, ""
    DMD "_", "4> " &HighScoreName(3) & " " &FormatScore(HighScore(3) ), 0, eNone, eScrollLeft, eNone, 2000, False, ""
    DMD Space(dCharsPerLine(0) ), Space(dCharsPerLine(1) ), 0, eScrollLeft, eScrollLeft, eNone, 500, False, ""


End Sub

Sub StartAttractMode(dummy)
    ChangeSong
    StartLightSeq
    DMDFlush
    ShowTableInfo
End Sub

Sub StopAttractMode
    Dim bulb
    DMDFlush
    LightSeqAttract.StopPlay
'StopSong
End Sub

Sub StartLightSeq()
    'lights sequences
    LightSeqAttract.UpdateInterval = 25
    LightSeqAttract.Play SeqBlinking, , 5, 150
    LightSeqAttract.Play SeqRandom, 40, , 4000
    LightSeqAttract.Play SeqAllOff
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 50, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqCircleOutOn, 15, 2
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 10
    LightSeqAttract.Play SeqCircleOutOn, 15, 3
    LightSeqAttract.UpdateInterval = 5
    LightSeqAttract.Play SeqRightOn, 50, 1
    LightSeqAttract.UpdateInterval = 5
    LightSeqAttract.Play SeqLeftOn, 50, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 50, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 50, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 40, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 40, 1
    LightSeqAttract.UpdateInterval = 10
    LightSeqAttract.Play SeqRightOn, 30, 1
    LightSeqAttract.UpdateInterval = 10
    LightSeqAttract.Play SeqLeftOn, 30, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 15, 1
    LightSeqAttract.UpdateInterval = 10
    LightSeqAttract.Play SeqCircleOutOn, 15, 3
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 5
    LightSeqAttract.Play SeqStripe1VertOn, 50, 2
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqCircleOutOn, 15, 2
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqStripe1VertOn, 50, 3
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqCircleOutOn, 15, 2
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqStripe2VertOn, 50, 3
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 25, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqStripe1VertOn, 25, 3
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqStripe2VertOn, 25, 3
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqUpOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqDownOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqRightOn, 15, 1
    LightSeqAttract.UpdateInterval = 8
    LightSeqAttract.Play SeqLeftOn, 15, 1
End Sub

Sub LightSeqAttract_PlayDone()
    StartLightSeq()
End Sub

Sub LightSeqTilt_PlayDone()
    LightSeqTilt.Play SeqAllOff
End Sub

'***********************************************************************
' *********************************************************************
'                     Table Specific Script Starts Here
' *********************************************************************
'***********************************************************************

' tables walls and animations
Sub VPObjects_Init
End Sub

' tables variables and modes init

Dim Coins(4)
Dim BumperBonus, EggBonus, HoleBonus, PokemonBonus, TargetBonus

Sub Game_Init()
    bExtraBallWonThisBall = False
    TurnOffPlayfieldLights()
    'Play some Music
    ChangeSong
    'Init Variables
    Jackpot = 250000
'    RattataPos = 0
'    bumperHits = 100
    BumperBonus = 0
'    PikachuHits = 0
'    CatchHits = 0
'    BallInHole = 0
    HoleBonus = 0
'    PokemonBonus = 0
    TargetBonus = 0
'    PikachuTargetValue = 5000
'    ResetPokemonLevel
'    ResetHoleLights
'    BallType = 1:UpdateBallType
'    bCatchemMode = False
'    bEggTargetsCompleted = False
'    EggBonus = 0
'    bLockEnabled = False
'    LockedBalls = 0
'    bcoinfrenzy = False
'    coinstep = 0
'    bPikachuTargetMode = False
'    bCharizardMode = False
'    bRampBonus = FALSE
'    bLoopBonus = FALSE
    MinionPOPUP1collide.IsDropped = 1
    MinionPOPUP2collide.IsDropped = 1
    MinionPOPUP3collide.IsDropped = 1
    PopupL.IsDropped = 1
    STUART.Z = 0

'Init Delays
'Skillshot Init
'MainModes Init()

SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0
SP5.state = 0
Bumperscore1.state = 0
Bumperscore2.state = 0
Bumperscore3.state = 0
Bumperscore4.state = 0
OR1.state = 2
OR2.state = 0
OR3.state = 0
OL1.state = 0
OL2.state = 0
OL3.state = 0
RR1.state = 2
RR2.state = 0
RR3.state = 0
RR4.state = 0
RL1.state = 0
RL2.state = 0
RL3.state = 0
RL4.state = 0
WIZARD.state = 0
Overkill.state = 0
TRcomplete.state = 0
Popupcomplete.state = 0
Tmultiballcomplete.state = 0
RampsComplete.state = 0
SPbulbscomplete.state = 0
TR1.state = 0
TR2.state = 0
TR3.state = 0
TR4.state = 0
Lightlock1.state = 0
Lightlock2.state = 0
Lightlock3.state = 0
Lightlock4.state = 0
Lightlock5.state = 0
Lock1.state = 0
Lock2.state = 0
Lock3.state = 0
J1.state = 0
J2.state = 0
J3.state = 0
J4.state = 0

TRbulb1.state = 0
FlashForMs TRbulb1b, 100, 10, 0  'off
TRbulb2.state = 0
FlashForMs TRbulb2b, 100, 10, 0  'off
TRbulb3.state = 0
FlashForMs TRbulb3b, 100, 10, 0  'off

SPbulb1.state = 0
FlashForMs SPbulb1b, 100, 10, 0  'off
SPbulb2.state = 0
FlashForMs SPbulb2b, 100, 10, 0  'off
SPbulb3.state = 0
FlashForMs SPbulb3b, 100, 10, 0  'off

Overkillbulb1.state = 0
FlashForMs Overkillbulb1b, 100, 10, 0  'off
Overkillbulb2.state = 0
FlashForMs Overkillbulb2b, 100, 10, 0  'off
Overkillbulb3.state = 0
FlashForMs Overkillbulb3b, 100, 10, 0  'off

Overkillbulb1.state = 0
Overkillbulb2.state = 0
Overkillbulb3.state = 0
SPbulb1.state = 0
SPbulb2.state = 0
SPbulb3.state = 0

'Turn off DMD reels
For Each x in DMDOff:x.visible = 0:Next
AttractAnimUltraDND.enabled = 0
StopAnimUDMD
'UltraDMD.CancelRenderingWithId "StopAnimUDMD"
End Sub

Sub ResetSkillShotTimer_Timer
End Sub

Sub TurnOffPlayfieldLights()
    Dim a
    For each a in aLights
        a.State = 0
    Next
End Sub

Sub ResetNewBallLights()
'    LightArrow1.State = 2
'    LightArrow6.State = 2
'    l53.State = 2
End Sub




' *********************************************************************
'                        Table Object Hit Events
'
' Any target hit Sub will follow this:
' - play a sound
' - do some physical movement
' - add a score, bonus
' - check some variables/modes this trigger is a member of
' - set the "LastSwicthHit" variable in case it is needed later
' *********************************************************************

' Slingshots has been hit

Dim LStep, RStep

Sub LeftSlingShot_Slingshot
    If Tilted Then Exit Sub
    'startB2S(6)
    startB2S(7)
    PlaySoundAtVol SoundFXDOF("fx_slingshot",103,DOFPulse,DOFContactors), lemk, 1
    LeftSling4.Visible = 1:LeftSling1.Visible = 0
    Lemk.RotX = 26
    LStep = 0
    LeftSlingShot.TimerEnabled = True
    ' add some points
    AddScore 500
    ' add some effect to the table?
    Gi2.State = 0
    ' remember last trigger hit by the ball
    LastSwitchHit = "LeftSlingShot"
    FlashForms SlingFlashL, 100, 10, 0
	DOF 140, DOFPulse
End Sub

Sub LeftSlingShot_Timer
    Select Case LStep
        Case 1:LeftSLing4.Visible = 0:LeftSLing3.Visible = 1:Lemk.RotX = 14
        Case 2:LeftSLing3.Visible = 0:LeftSLing2.Visible = 1:Lemk.RotX = 2
        Case 3:LeftSLing2.Visible = 0:LeftSling1.Visible = 1:Lemk.RotX = -10:Gi2.State = 1:LeftSlingShot.TimerEnabled = False
    End Select
    LStep = LStep + 1
End Sub

Sub RightSlingShot_Slingshot
    'startB2S(6)
    startB2S(8)
    If Tilted Then Exit Sub
    PlaySoundAtVol SoundFXDOF("fx_slingshot",104,DOFPulse,DOFContactors), remk, 1
    RightSling4.Visible = 1:RightSling1.Visible = 0
    Remk.RotX = 26
    RStep = 0
    RightSlingShot.TimerEnabled = True
    ' add some points
    AddScore 500
    ' add some effect to the table?
    Gi1.State = 0
    ' remember last trigger hit by the ball
    LastSwitchHit = "RightSlingShot"
    FlashForms SlingFlashR, 100, 10, 0
	DOF 141, DOFPulse
End Sub

Sub RightSlingShot_Timer
    Select Case RStep
        Case 1:RightSLing4.Visible = 0:RightSLing3.Visible = 1:Remk.RotX = 14
        Case 2:RightSLing3.Visible = 0:RightSLing2.Visible = 1:Remk.RotX = 2
        Case 3:RightSLing2.Visible = 0:RightSLing1.Visible = 1:Remk.RotX = -10:Gi1.State = 1:RightSlingShot.TimerEnabled = False
    End Select
    RStep = RStep + 1
End Sub

' SPbulbscomplete (LANE SWITCH) SCRIPT *********************************************************************

dim tempstateherolights 'to rotate lights


Sub LeftOutLaneTrigger_Hit()
	startB2S(7)
	DOF 110, DOFPulse
	Addscore(1000)
if ballsonplayfield = 1 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
'playmusic 2, "aaaauaaa", false, 1.0
Playsound "aaaauaaa"
end if
if SP1.state = 0 and SP2.state = 1 and SP3.state = 1 and SP4.state = 1  and SP5.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
'			' set our game flag
'			bBallSaverActive = TRUE
'			' start the timer
'			BallSaverTimer.Interval = constBallSaverTime
'			BallSaverTimer.Enabled = TRUE
'			' if you have a ball saver light you might want to turn it on at this
'			' point (or make it flash)
'			LightShootAgain.State = 2
EnableBallSaver 15

SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0

playsound "FXx"
SP5.state = 2
end if
	If SP5.state = 0 and (SP1.State=0) and WIZARD.state = 0 and ballsonplayfield = 1 then
		SP1.State = 1
	End If
End Sub

Sub LeftInLaneTrigger_Hit()
	startB2S(7)
    startB2S(5)
	DOF 111, DOFPulse
	Addscore(1000)
if SP1.state = 1 and SP2.state = 0 and SP3.state = 1 and SP4.state = 1  and SP5.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
'			' set our game flag
'			bBallSaverActive = TRUE
'			' start the timer
'			BallSaverTimer.Interval = constBallSaverTime
'			BallSaverTimer.Enabled = TRUE
'			' if you have a ball saver light you might want to turn it on at this
'			' point (or make it flash)
'			LightShootAgain.State = 2
EnableBallSaver 15
SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0

playsound "FXx"
SP5.state = 2
end if
	If SP5.state = 0 and (SP2.State=0) and WIZARD.state = 0 and ballsonplayfield = 1 then
		SP2.State=1
	End If
End Sub

Sub RightInLaneTrigger_Hit()
	startB2S(8)
    startB2S(5)
	DOF 112, DOFPulse
	Addscore(1000)
if SP1.state = 1 and SP2.state = 1 and SP3.state = 0 and SP4.state = 1  and SP5.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
'			' set our game flag
'			bBallSaverActive = TRUE
'			' start the timer
'			BallSaverTimer.Interval = constBallSaverTime
'			BallSaverTimer.Enabled = TRUE
'			' if you have a ball saver light you might want to turn it on at this
'			' point (or make it flash)
'			LightShootAgain.State = 2
EnableBallSaver 15
SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0

playsound "FXx"
SP5.state = 2
end if
	If SP5.state = 0 and (SP3.State=0) and WIZARD.state = 0 and ballsonplayfield = 1 then
		SP3.State=1
	End If
End Sub

Sub RightOutLaneTrigger_Hit()
	startB2S(8)
	DOF 113, DOFPulse
	Addscore(1000)
if ballsonplayfield = 1 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 1000
playsound "WHAAT"
end if
if SP1.state = 1 and SP2.state = 1 and SP3.state = 1 and SP4.state = 0  and SP5.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
'			' set our game flag
'			bBallSaverActive = TRUE
'			' start the timer
'			BallSaverTimer.Interval = constBallSaverTime
'			BallSaverTimer.Enabled = TRUE
'			' if you have a ball saver light you might want to turn it on at this
'			' point (or make it flash)
'			LightShootAgain.State = 2
EnableBallSaver 15
SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0

playsound "FXx"
SP5.state = 2
end if
	If SP5.state = 0 and (SP4.State=0) and WIZARD.state = 0 and ballsonplayfield = 1 then
		SP4.State=1
	End If
End Sub

'*************
'Kicker Right
'*************

sub Rightkicker_hit()
    startB2S(6)
BOBP = 0
BOBToy.ENABLED = 1
playsoundatvol "fx_kicker-enter", ActiveBall, 1
'Rightkickertimer.set true, 1000
if SP5.state = 2 and SPbulb1.state = 0 and WIZARD.state = 0 then
addscore 10000
SP5.state = 0
SPbulb1.state = 2
FlashForMs SPbulb1b, 1, 500, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000

playsound "narrator1"
'Rightkickertimer.set true, 3000
'BOBhop.set true, 1

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000
'flasherstimer.set true, 1

'DispDmd1.QueueText "[f3][xc][y5]10000[y18]10000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]10000[y18]10000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 	DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "10000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
        DMD CenterLine(0, "10000"), CenterLine(1,  FormatScore("10000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if SP5.state = 2 and SPbulb1.state = 2 and WIZARD.state = 0 then
addscore 20000
SP5.state = 0
SPbulb1.state = 1
FlashForMs SPbulb1b,  200, 10, 3
SPbulb2.state = 2
FlashForMs SPbulb2b, 1, 500, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 6200
playsound "narrator2"
StopAllMusic
Song = "bgout_MinionsMISSION" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsMISSION", true, 0.7
'Rightkickertimer.set true, 6200
'BOBhop.set true, 1

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000
'flasherstimer.set true, 1

'DispDmd1.QueueText "[f3][xc][y5]20000[y18]20000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]20000[y18]20000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "20000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "20000"), CenterLine(1,  FormatScore("20000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if SP5.state = 2 and SPbulb2.state = 2 and WIZARD.state = 0 then
addscore 30000
SP5.state = 0
SPbulb2.state = 1
FlashForMs SPbulb2b,  200, 10, 3
SPbulb3.state = 2
FlashForMs SPbulb3b,  1, 500, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 8000
playsound "narrator3"
'Rightkickertimer.set true, 8000
'BOBhop.set true, 1

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000
'flasherstimer.set true, 1

'DispDmd1.QueueText "[f3][xc][y5]30000[y18]30000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]30000[y18]30000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 	DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "30000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "30000"), CenterLine(1,  FormatScore("30000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if SP5.state = 2 and SPbulb3.state = 2 and WIZARD.state = 0 then
addscore 100000
SP5.state = 0
SPbulb1.state = 0
SPbulb2.state = 0
SPbulb3.state = 0

'FlashForMs SPbulbscomplete,  400, 100, 1
StopAllMusic
Song = "bgout_MinionsMISSION" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsMISSION", true, 0.7
checkforWIZARD()

'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 8500
playsound "narrator4"
'Rightkickertimer.set true, 8500
'BOBhop.set true, 1

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000
'flasherstimer.set true, 1

'DispDmd1.QueueText "[f3][xc][y5]100000[y18]100000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]100000[y18]100000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 	DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "100000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "100000"), CenterLine(1,  FormatScore("100000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""

end if
vpmtimer.addtimer 1500, "RightkickerRelease '"
end sub

sub RightkickerRelease()
Rightkicker.kick 205, 18, 0.5
'Rightkicker.solenoidpulse
SP5.state = 0
playsound "Shoot"
DOF 116, DOFPulse
DOF 117, DOFPulse
end sub

' SPbulbscomplete (LANE SWITCH) SCRIPT END*********************************************************************

sub popupRt_hit()
    PlaySoundAtVol SoundFXDOF("fx_diverter",130,DOFPulse,DOFContactors), Bumper1, 1
    Popup2.IsDropped = 0
    Popup3.IsDropped = 0
    PopupRResetTimer.enabled = 1
end sub

sub PopupRResetTimer_Timer()
    PlaySoundAtVol SoundFXDOF("fx_diverter",130,DOFPulse,DOFContactors), Bumper1, 1
    Popup2.IsDropped = 0
    Popup3.IsDropped = 0
    PopupRResetTimer.enabled = 0
end sub


'minions movements:
'------------------
'Purple nomnom:
Dim jawP
Sub jawupTimer_Timer
  DOF 135, DOFOn
  Select Case jawp
         Case 1:jawup.objRotX = 1:jawup.objRotY = 15:jawup.objRotZ = -5
         Case 2:jawup.objRotX = 75:jawup.objRotY = 15:jawup.objRotZ = -5:vpmtimer.addtimer 600, "jawdown '"
  End Select
    jawp = jawp + 1
End Sub

Sub jawdown()
    jawup.objRotX = 0:jawup.objRotY = 0:jawup.objRotZ = 0:Gi1.State = 1
    jawp = 0
    jawupTimer.Enabled = 0
    DOF 135, DOFOff
End Sub

'BOB up&down:
Dim BOBP
Sub BOBToy_Timer
  DOF 135, DOFOn
  Select Case BOBP
         Case 1:BOB.Z = -25
         Case 2:BOB.Z = -15
         Case 3:BOB.Z = -20
         Case 4:BOB.Z = -10
         Case 5:BOB.Z = -25
         Case 6:BOB.Z = -15
         Case 7:BOB.Z = -20
         Case 8:BOB.Z = -10
         Case 9:BOB.Z = -5
         Case 10:BOB.Z = 0:Gi1.State = 1:BOBToy.Enabled = 0:DOF 135, DOFOff
  End Select
    BOBP = BOBP + 1
End Sub

'KEVIN wiggle:
Dim KevinP
Sub KevinToy_Timer
  DOF 135, DOFOn
  Select Case KevinP
         Case 1:Kevin.RotY = -55
         Case 2:Kevin.RotY = -35
         Case 3:Kevin.RotY = -45
         Case 4:Kevin.RotY = -35
         Case 5:Kevin.RotY = -55
         Case 6:Kevin.RotY = -45
         Case 7:Kevin.RotY = -35:Gi1.State = 1:KevinToy.Enabled = 0:DOF 135, DOFOff
  End Select
    KevinP = KevinP + 1
End Sub

'STUART hopshort:
Sub StuartToy_Timer
    STUART.Z = 0
    me.enabled = 0
End Sub

' bumpers and upper lanes scripte SCRIPT *********************************************************************

sub TriggerLeftRO_hit()
DOF 114, DOFPulse
if Bumperscore1.state = 1 and Bumperscore2.state = 0 and WIZARD.state = 0 then
Bumperscore2.state = 1
playsound "FXk"
end if
if Bumperscore1.state = 0 and Bumperscore2.state = 0 and WIZARD.state = 0 then
Bumperscore1.state = 1
end if
end sub

sub TriggerRightRO_hit()
DOF 115, DOFPulse
if Bumperscore3.state = 1 and Bumperscore4.state = 0 and WIZARD.state = 0 then
Bumperscore4.state = 1
playsound "FXk"
end if
if Bumperscore3.state = 0 and Bumperscore4.state = 0 and WIZARD.state = 0 then
Bumperscore3.state = 1
end if
end sub

sub bumper1_hit()
startB2S(1)
BulbBumper1b.state = 1
FlashForMs BulbBumper1,  200, 10, 3
STUART.Z = -35
DOF 134, DOFPulse
StuartToy.enabled = 1
PlaysoundAtVol SoundFXDOF("fx_bumper",106,DOFPulse,DOFContactors), Bumper1, VolBump
Addscore 1000
if Bumperscore1.state = 1 and Bumperscore2.state = 1 and Bumperscore3.state = 1 and Bumperscore3.state = 1 then
addscore 5000
BulbBumper1b.state = 0
FlashForMs BulbBumper1,  100, 10, 0
FlashForMs LightBumper, 100, 10, 0
end if
end sub

sub bumper2_hit()
startB2S(2)
BulbBumper2b.state = 1
FlashForMs BulbBumper2,  200, 10, 3
STUART.Z = -35
DOF 134, DOFPulse
StuartToy.enabled = 1
playsoundatvol SoundFXDOF("fx_bumper",105,DOFPulse,DOFContactors), Bumper2, VolBump
Addscore 1000
if Bumperscore1.state = 1 and Bumperscore2.state = 1 and Bumperscore3.state = 1 and Bumperscore3.state = 1 then
addscore 5000
BulbBumper2b.state = 0
FlashForMs BulbBumper2,  100, 10, 0
FlashForMs LightBumper, 100, 10, 0
end if
end sub

sub bumper3_hit()
startB2S(3)
BulbBumper3b.state = 1
FlashForMs BulbBumper3,  200, 10, 3
STUART.Z = -35
DOF 134, DOFPulse
StuartToy.enabled = 1
playsoundatvol SoundFXDOF("fx_bumper",107,DOFPulse,DOFContactors), Bumper3, VolBump
Addscore 1000
if Bumperscore1.state = 1 and Bumperscore2.state = 1 and Bumperscore3.state = 1 and Bumperscore3.state = 1 then
addscore 5000
BulbBumper3b.state = 0
FlashForMs BulbBumper3,  100, 10, 0
FlashForMs LightBumper, 100, 10, 0
end if
end sub
' bumpers and upper lanes scripte SCRIPT END******************************************************************

' 2 orbits SCRIPT *********************************************************************

sub Rightorbittrigger_hit()

if ballsonplayfield = 4 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 Jackpot250()'	DMD_DisplaySceneEx "DMD1.png", "250000", 15, 4, "JACKPOT", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
Playsound "jack6"
'playmusic 4, "jack6", false, 1.0
end if

if ballsonplayfield = 3 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 Jackpot250()'DMD_DisplaySceneEx "DMD1.png", "250000", 15, 4, "JACKPOT", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
Playsound "jack7"

end if

if ballsonplayfield = 2 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 Jackpot250()'DMD_DisplaySceneEx "DMD1.png", "250000", 15, 4, "JACKPOT", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
Playsound "jack5"
end if

if OR3.state = 2 and OL3.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
'Popup2.solenoidpulse(2000)
'Popup3.solenoidpulse(2000)
PopupRResetTimer.enabled = 1
addscore 75000
OR3.state = 1
OL3.state = 2

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]LEFT ORBIT[y18]75000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]LEFT ORBIT[y18]75000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "LEFT ORBIT", 15, 4, "75000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "LEFT ORBIT"), CenterLine(1,  FormatScore("75000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "FX11"
playsound "_bla2"
end if

if OR2.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
'Popup2.solenoidpulse(2000)
'Popup3.solenoidpulse(2000)
PopupRResetTimer.enabled = 1
addscore 25000
OR2.state = 1
OL2.state = 2

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]LEFT ORBIT[y18]75000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]LEFT ORBIT[y18]75000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "LEFT ORBIT", 15, 4, "75000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "LEFT ORBIT"), CenterLine(1,  FormatScore("75000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "FX11"
playsound "_bla3"
end if

if OR1.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
'Popup2.solenoidpulse(2000)
'Popup3.solenoidpulse(2000)
PopupRResetTimer.enabled = 1
addscore 10000
OR1.state = 1
OL1.state = 2

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]LEFT ORBIT[y18]75000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]LEFT ORBIT[y18]75000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "LEFT ORBIT", 15, 4, "75000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "LEFT ORBIT"), CenterLine(1,  FormatScore("75000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "FX11"
playsound "_butthaha"
end if

if OR3.state = 1 and OL3.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
'Popup2.solenoidpulse(2000)
'Popup3.solenoidpulse(2000)
PopupRResetTimer.enabled = 1
end if
if OR2.state = 1 and OL2.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
'Popup2.solenoidpulse(2000)
'Popup3.solenoidpulse(2000)
PopupRResetTimer.enabled = 1
end if
if OR1.state = 1 and OL1.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
'Popup2.solenoidpulse(2000)
'Popup3.solenoidpulse(2000)
PopupRResetTimer.enabled = 1
end if

end sub


sub Leftorbittrigger_hit()

if ballsonplayfield = 4 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 Jackpot250()'DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
Playsound "jack5"
end if

if ballsonplayfield = 3 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 Jackpot250()'DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
Playsound "jack7"
end if

if ballsonplayfield = 2 and WIZARD.state = 0 then
addscore 75000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
 Jackpot250()'DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
Playsound "jack6"
end if


if OL3.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
addscore 75000

OR1.state = 2
OR2.state = 0
OR3.state = 0
OL1.state = 0
OL2.state = 0
OL3.state = 0

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]ORBIT MAXIMUM[y18]75000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]ORBIT MAXIMUM[y18]75000", deFlip, 10000, True
OrbitAnin()'DMD_DisplaySceneEx "DMD1.png", "ORBIT MAXIMUM", 15, 4, "75000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "ORBIT MAXIMUM"), CenterLine(1,  FormatScore("75000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "FX11"
playsound "_molakslap"
end if

if OL2.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
addscore 25000
OL2.state = 1
OR3.state = 2

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'AttractLightSeqAttractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]RIGHT ORBIT[y18]25000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]RIGHT ORBIT[y18]25000", deFlip, 10000, True
DMD_DisplaySceneEx "DMD1.png", "RIGHT ORBIT", 15, 4, "25000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "RIGHT ORBIT"), CenterLine(1,  FormatScore("25000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "FX11"
playsound "_cowhaha"
end if

if OL1.state = 2 and ballsonplayfield = 1 and WIZARD.state = 0 then
addscore 10000
OL1.state = 1
OR2.state = 2

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]RIGHT ORBIT[y18]25000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]RIGHT ORBIT[y18]25000", deFlip, 10000, True
DMD_DisplaySceneEx "DMD1.png", "RIGHT ORBIT", 15, 4, "25000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "RIGHT ORBIT"), CenterLine(1,  FormatScore("25000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
startB2S(4)
playsound "FX11"
playsound "_cestlabanana"
end if

end sub

' 2 orbits SCRIPT END******************************************************************



' TR SCRIPT *********************************************************************

'Lefts Targets
sub TargetR1_hit()
DOF 119, DOFPulse
playsound "FXr"
KevinP = 0
KevinToy.enabled = 1
if TR1.state = 0 and WIZARD.state = 0 then
TR1.state = 1
checktargetsTR1()
end if
end sub


sub TargetR2_hit()
DOF 119, DOFPulse
playsound "FXq"
KevinP = 0
KevinToy.enabled = 1
if TR2.state = 0 and WIZARD.state = 0 then
TR2.state = 1
checktargetsTR1()
end if
end sub


sub TargetR3_hit()
DOF 119, DOFPulse
playsound "FXp"
KevinP = 0
KevinToy.enabled = 1
if TR3.state = 0 and WIZARD.state = 0 then
TR3.state = 1
checktargetsTR1()
end if
end sub


sub TargetR4_hit()
DOF 119, DOFPulse
playsound "FXo"
KevinP = 0
KevinToy.enabled = 1
if TR4.state = 0 and WIZARD.state = 0 then
TR4.state = 1
checktargetsTR1()
end if
end sub


sub checktargetsTR1()
if TR1.state = 1 and TR2.state = 1 and TR3.state = 1 and TR4.state = 1 and TRbulb3.state = 2   then
TR1.state = 0
TR2.state = 0
TR3.state = 0
TR4.state = 0
TRbulb1.state = 0
TRbulb2.state = 0
TRbulb3.state = 0
FlashForMs TRbulb1b, 100, 10, 0  'off
FlashForMs TRbulb2b, 100, 10, 0  'off
FlashForMs TRbulb3b, 100, 10, 0  'off
TRcomplete.state = 1
StopAllMusic
Song = "bgout_MinionsDUNGEON" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsDUNGEON", true, 0.7
checkforWIZARD()

addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]Targets complete[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]Targets complete[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
TargetsAnim250()'DMD_DisplaySceneEx "DMD1.png", "TARGETS COMPLETE", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "TARGETS COMPLETE"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
playsound "FLUFFY2"
end if


if TR1.state = 1 and TR2.state = 1 and TR3.state = 1 and TR4.state = 1 and TRbulb2.state = 2 then
TR1.state = 0
TR2.state = 0
TR3.state = 0
TR4.state = 0
SP5.state = 2
TRbulb2.state = 1
FlashForMs TRbulb2b, 200, 10, 3  'On
TRbulb3.state = 2
FlashForMs TRbulb3b, 1, 500, 1  'Blink

addscore 150000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]Targets complete[y18]150000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]Targets complete[y18]150000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
TargetsAnim150()'DMD_DisplaySceneEx "DMD1.png", "TARGETS COMPLETE", 15, 4, "150000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "TARGETS COMPLETE"), CenterLine(1,  FormatScore("150000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
playsound "FLUFFY1"
end if

if TR1.state = 1 and TR2.state = 1 and TR3.state = 1 and TR4.state = 1 And TRbulb1.state = 2 then
TR1.state = 0
TR2.state = 0
TR3.state = 0
TR4.state = 0
SP5.state = 2
TRbulb1.state = 1
FlashForMs TRbulb1b, 200, 10, 3  'On
TRbulb2.state = 2
FlashForMs TRbulb2b, 1, 500, 1  'blink

addscore 50000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]Targets complete[y18]50000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]Targets complete[y18]50000", deFlip, 10000, True
TargetsAnim50()'DMD_DisplaySceneEx "DMD1.png", "TARGETS COMPLETE", 15, 4, "50000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "TARGETS COMPLETE"), CenterLine(1,  FormatScore("50000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
StopAllMusic
Song = "bgout_MinionsDUNGEON" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsDUNGEON", true, 0.7
'scoreupdate = false
'flushdmdtimer.set true , 2000
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
playsound "firealarm2"
end if

if TR1.state = 1 and TR2.state = 1 and TR3.state = 1 and TR4.state = 1 and TRbulb1.state = 0 then
TR1.state = 0
TR2.state = 0
TR3.state = 0
TR4.state = 0
SP5.state = 2
TRbulb1.state = 2
FlashForMs TRbulb1b, 1, 500, 1  'Blink

addscore 10000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]Targets complete[y18]10000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]Targets complete[y18]10000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
TargetsAnim10()'DMD_DisplaySceneEx "DMD1.png", "TARGETS COMPLETE", 15, 4, "10000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "TARGETS COMPLETE"), CenterLine(1,  FormatScore("10000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
playsound "firealarm"
end if

end sub

' TR SCRIPT END******************************************************************






' MULTIBALL SCRIPT *********************************************************************
sub T1_hit()
	startB2S(11)
DOF 124, DOFPulse
playsound "FX01"
if Lightlock1.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
Lightlock1.state = 1
checkforlock()
end if
end sub

sub T2_hit()
	startB2S(12)
DOF 124, DOFPulse
playsound "FX02"
if Lightlock2.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
Lightlock2.state = 1
checkforlock()
end if
end sub

sub T3_hit()
    startB2S(10)
DOF 124, DOFPulse
playsound "FX_horn"
if Lightlock3.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
Lightlock3.state = 1
checkforlock()
end if
end sub

sub T4_hit()
	startB2S(11)
DOF 125, DOFPulse
playsound "FX02"
if Lightlock4.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
Lightlock4.state = 1
checkforlock()
end if
end sub

sub T5_hit()
	startB2S(12)
DOF 125, DOFPulse
playsound "FX01"
if Lightlock5.state = 0 and ballsonplayfield = 1 and WIZARD.state = 0 then
Lightlock5.state = 1
checkforlock()
end if
end sub


sub checkforlock()
if Lightlock1.state = 1 and Lightlock2.state = 1 and Lightlock3.state = 1 and Lightlock4.state = 1 and Lightlock5.state = 1 and Lock2.state =1 then
Lock3.state = 2
addscore 50000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 50, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]MULTIBALL[y18]READY", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]MULTIBALL[y18]READY", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "MULTIBALL READY", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD "_", Centerline(1, ("MULTIBALL READY") ), 0, eNone, eBlink, eNone, 1000, True, "purple3"

'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
'playsound "purple3"
end if
if Lightlock1.state = 1 and Lightlock2.state = 1 and Lightlock3.state = 1 and Lightlock4.state = 1 and Lightlock5.state = 1 and Lock1.state =1 and lock3.state = 0 then
Lock2.state = 2
addscore 50000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]LOCK[y18]BALL", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]LOCK[y18]BALL", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "LOCK BALL", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD "_", Centerline(1, ("LOCK BALL") ), 0, eNone, eBlink, eNone, 1000, True, "purple3"
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
'playsound "purple3"
end if
if Lightlock1.state = 1 and Lightlock2.state = 1 and Lightlock3.state = 1 and Lightlock4.state = 1 and Lightlock5.state = 1 and Lock1.state =0 then
Lock1.state = 2
addscore 10000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]LOCK[y18]BALL", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]LOCK[y18]BALL", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "  LOCK BALL  ", -1, -1, UltraDMD_Animation_ScrollOnUp, 2000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("LOCK BALL ") ), 0, eNone, eBlinkFast, eNone, 2000, True, "purple3"

'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
'playsound "purple3"
end if
end sub


sub Lockkicker_hit()
    startB2S(6)
addscore 10000
playsoundatvol "fx_kicker-enter", LockKicker, VolKick
'vpmtimer.addtimer 1000, "Lockkickertimer '"
'Lockkickertimer.set true, 1000

if Lock3.state = 2 and WIZARD.state = 0 then
lock3.state = 0
lock2.state = 0
lock1.state = 0
Lightlock1.state = 0
Lightlock2.state = 0
Lightlock3.state = 0
Lightlock4.state = 0
Lightlock5.state = 0
addscore 100000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 50, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]MULTIBALL[y18]SHOOT JACKPOTS", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]MULTIBALL[y18]SHOOT JACKPOTS", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 3000
MultiballJackpotAnim()'DMD_DisplaySceneEx "DMD1.png", "MULTIBALL", 15, 4, "SHOOT JACKPOTS", -1, -1, UltraDMD_Animation_ScrollOnUp, 3000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "MULTIBALL"), CenterLine(1, "SHOOT JACKPOTS"), 0, eNone, eNone, eNone, 3000, True, "purple5"
'DMD "_", CenterLine(1, ("MULTIBALL SHOOT JACKPOTS") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 7000
'playsound "purple5"

'vpmtimer.addtimer 3000, "Lockkickertimer '"
'Lockkickertimer.set true, 3000

' START MULTIBALL:
'--------------balls get kicked in:
'createballtimer.set true, 4000
'plungertimer.set true, 5000
'createballtimer2.set true, 6000
'plungertimer2.set true, 7000
'createballtimer3.set true, 8000
'plungertimer3.set true, 9000
vpmtimer.addtimer 2000, "Multiballtimer '"

J1.state = 2
J2.state = 2
J3.state = 2
J4.state = 2

Tmultiballcomplete.state = 2
'StopAllMusic
'Song = "bgout_MinionsGRU" & ".mp3"
'PlayMusic Song
'playsound "MinionsGRU"
'AddMultiball 3
'EnableBallSaver 15

end if

if Lock2.state = 2 and WIZARD.state = 0 then
lock2.state = 1
Lightlock1.state = 0
Lightlock2.state = 0
Lightlock3.state = 0
Lightlock4.state = 0
Lightlock5.state = 0

addscore 50000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 50, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]BALL 2[y18]LOCKED", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]BALL 2[y18]LOCKED", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "BALL 2 LOCKED", -1, -1, UltraDMD_Animation_ScrollOnUp, 2000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("BALL 2 LOCKED") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "purple4"

vpmtimer.addtimer 1500, "Lockkickertimer '"
'Lockkickertimer.set true, 3000
end if

if Lock1.state = 2 and WIZARD.state = 0 then
lock1.state = 1
Lightlock1.state = 0
Lightlock2.state = 0
Lightlock3.state = 0
Lightlock4.state = 0
Lightlock5.state = 0

addscore 25000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]BALL 1[y18]LOCKED", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]BALL 1[y18]LOCKED", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "BALL 1 LOCKED", -1, -1, UltraDMD_Animation_ScrollOnUp, 2000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("BALL 1 LOCKED") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 3000
playsound "purple1"
vpmtimer.addtimer 1500, "Lockkickertimer '"
'Lockkickertimer.set true, 3000
end if

If Lock1.state = 0 Or Lock2.state = 0 Or Lock3.state = 0 Or J1.state = 0 Then
vpmtimer.addtimer 1500, "Lockkickertimer '"
End If
end sub

sub Lockkickertimer()
PlaysoundAtVol SoundFXDOF("fx_vukout_LAH",118,DOFPulse,DOFContactors), Lockkicker, VolKick
DOF 117, DOFPulse
Lockkicker.kick 0, 75, 1.5
jawupTimer.enabled = 1
end sub

Sub LockkickerTrigger_hit()
   PlaysoundAtVol "fx_metalrolling", ActiveBall, 1
End Sub

sub Multiballtimer()
AddMultiball 3
EnableBallSaver 15
vpmtimer.addtimer 3000, "Lockkickertimer '"
End Sub

' jackpots are scripted on the triggers they belong to.



' MULTIBALL SCRIPT END******************************************************************




' Ramps SCRIPT *********************************************************************
sub LeftRamp_hit
    startB2S(6)
rampswitchL.RotY = -50
PlaySound "fx_sensor"
'Jackpots during multiball:

if ballsonplayfield = 4 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
PlaySound "jack5"
end if

if ballsonplayfield = 3 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
PlaySound "jack6"
end if

if ballsonplayfield = 2 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
PlaySound "jack7"
end if

'LEFT RAMP NORMAL MODE:

if ballsonplayfield = 1 and RL4.state = 2 and WIZARD.state = 0 then

RR1.state = 2
RR2.state = 0
RR3.state = 0
RR4.state = 0
RL1.state = 0
RL2.state = 0
RL3.state = 0
RL4.state = 0

FlashForMs RampsComplete,  3, 50, 1
StopAllMusic
Song = "bgout_MinionsORLANDO" & ".mp3"
PlayMusic Song


checkforWIZARD()

addscore 150000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2500
PlaySound "KINGBOOOOOB"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]MAXIMUM RAMP[y18]150000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]MAXIMUM RAMP[y18]150000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "MAXIMUM RAMP", 15, 4, "150000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "MAXIMUM RAMP"), CenterLine(1,  FormatScore("150000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if ballsonplayfield = 1 and RL3.state = 2 and WIZARD.state = 0 then
RL3.state = 1
RR4.state = 2

addscore 75000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2000
PlaySound "Ramp03"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT RIGHT RAMP[y18]75000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT RIGHT RAMP[y18]75000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "SHOOT RIGHT RAMP", 15, 4, "75000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT RIGHT RAMP"), CenterLine(1,  FormatScore("75000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if ballsonplayfield = 1 and RL2.state = 2 and WIZARD.state = 0 then
RL2.state = 1
RR3.state = 2

addscore 25000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 1500
PlaySound "Ramp02"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT RIGHT RAMP[y18]25000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT RIGHT RAMP[y18]25000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "SHOOT RIGHT RAMP", 15, 4, "25000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT RIGHT RAMP"), CenterLine(1,  FormatScore("25000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if ballsonplayfield = 1 and RL1.state = 2 and WIZARD.state = 0 then
RL1.state = 1
RR2.state = 2

addscore 10000

'flasherstimer.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 1400
PlaySound "AAAAAAA2"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT RIGHT RAMP[y18]10000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT RIGHT RAMP[y18]10000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "SHOOT RIGHT RAMP", 15, 4, "10000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT RIGHT RAMP"), CenterLine(1,  FormatScore("10000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if
end sub

sub LeftRamp_Unhit()
   rampswitchL.RotY = 0
   PlaySound "fx_metalrolling"
End Sub


sub RightRamp_hit
    startB2S(5)
rampswitchR.RotY = 50
'Jackpots during multiball:

if ballsonplayfield = 4 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "SHOOT RIGHT RAMP", 15, 4, "10000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT RIGHT RAMP"), CenterLine(1,  FormatScore("10000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
playsound "jack7"
end if

if ballsonplayfield = 3 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
playsound  "jack6"
end if

if ballsonplayfield = 2 and WIZARD.state = 0 then
addscore 250000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]JACKPOT[y18]250000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "JACKPOT", 15, 4, "250000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "JACKPOT"), CenterLine(1,  FormatScore("250000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
playsound  "jack5"
end if

'Right RAMP NORMAL MODE:

if ballsonplayfield = 1 and RR4.state = 1 and WIZARD.state = 0 then
FlashForMs RR4,  300, 50, 1

addscore 150000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2500
playsound "KINGBOOOOOB"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]MAXIMUM RAMP[y18]150000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]MAXIMUM RAMP[y18]150000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "MAXIMUM RAMP", 15, 4, "150000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "MAXIMUM RAMP"), CenterLine(1,  FormatScore("150000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if ballsonplayfield = 1 and RR4.state = 2 and WIZARD.state = 0 then
RR4.state = 1
RL4.state = 2

addscore 150000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2500
playsound  "AAAAAAA2"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]MAXIMUM RAMP[y18]150000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]MAXIMUM RAMP[y18]150000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "MAXIMUM RAMP", 15, 4, "150000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "MAXIMUM RAMP"), CenterLine(1,  FormatScore("150000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if ballsonplayfield = 1 and RR3.state = 2 and WIZARD.state = 0 then
RR3.state = 1
RL3.state = 2

addscore 75000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2500
playsound  "Ramp03"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT LEFT RAMP[y18]75000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT LEFT RAMP[y18]75000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "SHOOT LEFT RAMP", 15, 4, "75000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT LEFT RAMP"), CenterLine(1,  FormatScore("75000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if

if ballsonplayfield = 1 and RR2.state = 2 and WIZARD.state = 0 then
RR2.state = 1
RL2.state = 2

addscore 25000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2500
playsound  "Ramp02"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT LEFT RAMP[y18]25000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT LEFT RAMP[y18]25000", deFlip, 10000, True
DMD_DisplaySceneEx "DMD1.png", "SHOOT LEFT RAMP", 15, 4, "25000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT LEFT RAMP"), CenterLine(1,  FormatScore("25000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
StopAllMusic
Song = "bgout_MinionsORLANDO" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsORLANDO", true, 0.7
'scoreupdate = false
'flushdmdtimer.set true , 2000

end if

if ballsonplayfield = 1 and RR1.state = 2 and WIZARD.state = 0 then
RR1.state = 1
RL1.state = 2

addscore 10000

'flasherstimerb.set true, 1
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 2500
playsound  "KINGBOOOOOB"

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqDownOn, 80, ,1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT LEFT RAMP[y18]10000", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT LEFT RAMP[y18]10000", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "SHOOT LEFT RAMP", 15, 4, "10000", -1, -1, UltraDMD_Animation_ScrollOnUp, 1000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT LEFT RAMP"), CenterLine(1,  FormatScore("10000")), 0, eBlink, eBlinkFast, eNone, 1000, True, ""
end if
end sub

sub RightRamp_Unhit()
   rampswitchL.RotY = 0
   PlaySound "fx_metalrolling"
End Sub

' Ramps SCRIPT END******************************************************************





' OVERKILL KICKER & MINION POPUPS ******************************************************************

'MinionPOPUP1:
sub MinionPOPUP1Timer_Timer()
me.enabled = false
if MinionPOPUP1.TransY = 0 then
MinionPOPUP1.TransY = 70
playsound "solenoidminions"
'MinionPOPUP1collide.collidable = true
MinionPOPUP1collide.Isdropped = 0
DOF 120, DOFPulse
end if
end sub

sub MinionPOPUP1DOWNTimer_Timer()
me.enabled = false
if MinionPOPUP1.TransY = 70 then
MinionPOPUP1.TransY = 0
PlaysoundAtVol SoundFXDOF("fx_droptarget",120,DOFPulse,DOFContactors), MininoPopUp1, 1
MinionPOPUP1collide.Isdropped = 1
end if
end sub

'Sub MinionPOPUP1W_hit
'    MinionPOPUP1W.Isdropped = 1
'    MinionPOPUP1DOWNTimer.enabled = 1
'    MinionsHits()
'End Sub



'MinionPOPUP2:
sub MinionPOPUP2Timer_Timer()
me.enabled = false
if MinionPOPUP2.TransY = 0 then
MinionPOPUP2.TransY = 100
 PlaysoundAtVol "solenoidminions", MinionPopup2, 1
'MinionPOPUP1collide.collidable = true
MinionPOPUP2collide.Isdropped = 0
DOF 121, DOFPulse
end if
end sub

sub MinionPOPUP2DOWNTimer_Timer()
me.enabled = false
if MinionPOPUP2.TransY = 100 then
MinionPOPUP2.TransY = 0
playsoundAtVol SoundFXDOF("fx_droptarget",121,DOFPulse,DOFContactors), MinionPopup2, 1
MinionPOPUP2collide.Isdropped =  1
end if
end sub

'Sub MinionPOPUP2W_hit
'    MinionPOPUP2W.Isdropped = 1
'    MinionPOPUP2DOWNTimer.enabled = 1
'    MinionsHits()
'End Sub

'MinionPOPUP3:
sub MinionPOPUP3Timer_Timer()
me.enabled = false
if MinionPOPUP3.TransY = 0 then
MinionPOPUP3.TransY = 40
playsound "solenoidminions"
'MinionPOPUP1collide.collidable = true
MinionPOPUP3collide.Isdropped = 0
DOF 122, DOFPulse
end if
end sub

sub MinionPOPUP3DOWNTimer_Timer()
me.enabled = false
if MinionPOPUP3.TransY = 40 then
MinionPOPUP3.TransY = 0
MinionPOPUP3collide.Isdropped = 1
 PlaysoundAtVol SoundFXDOF("fx_droptarget",122,DOFPulse,DOFContactors), ActiveBall, 1
end if
end sub

'Sub MinionPOPUP3W_hit
'    MinionPOPUP3W.Isdropped = 1
'    MinionPOPUP3DOWNTimer.enabled = 1
'    MinionsHits()
'End Sub



Dim RamdonM:RamdonM = 0
Sub MinionsRamdon_Timer
    RamdonM = RamdonM + 1
   Select Case RamdonM
          Case 1: MinionPOPUP1.TransY = 70:playsoundAtVol SoundFXDOF("solenoidminions",120,DOFPulse,DOFcontactors), MinionPopUp1
          Case 2: MinionPOPUP2.TransY = 100:playsoundAtVol SoundFXDOF("solenoidminions",121,DOFPulse,DOFcontactors), MinionPOPUP2
          Case 3: MinionPOPUP3.TransY = 0:playsoundAtVol SoundFXDOF("solenoidminions",122,DOFPulse,DOFcontactors), MinionPOPUP3
          Case 4: MinionPOPUP2.TransY = 0:playsoundAtVol SoundFXDOF("solenoidminions",121,DOFPulse,DOFcontactors), MinionPOPUP2
          Case 6: MinionPOPUP1.TransY = 0: RamdonM = 0 :MinionPOPUP3Timer.enabled = 1 :Me.enabled = 0
   End Select

End Sub

'Minion above entrance rotates if kickercenter is hit:
Minion4Init
Dim cBall

Sub Minion4Init
    Set cBall = ckicker.createball
    ckicker.Kick 0, 0
End Sub

Sub Minion4Timer_Timer
	DOF 146, DOFPulse
    cball.vely = 350: FlashForMs Flasher4, 800, 50, 0:DOF 143, DOFPulse
    playsound "motorpulse"
    vpmtimer.addtimer 1500, "Minion4Timer1 '"
    Me.enabled = 0
End Sub

Sub Minion4Timer1
	DOF 146, DOFPulse
    cball.vely = 300: FlashForMs Flasher4, 800, 50, 0:DOF 143, DOFPulse
    playsound "motorpulse"
    vpmtimer.addtimer 1500, "Minion4Timer2 '"
End Sub
Sub Minion4Timer2
	DOF 146, DOFPulse
    cball.vely = 300: FlashForMs Flasher4, 800, 50, 0:DOF 143, DOFPulse
    playsound "motorpulse"
    vpmtimer.addtimer 1500, "Minion4Timer3 '"
End Sub
Sub Minion4Timer3
	DOF 146, DOFPulse
    cball.vely = 300: FlashForMs Flasher4, 800, 50, 0:DOF 143, DOFPulse
    playsound "motorpulse"
    MinionsRamdon.enabled = 1
End Sub





sub orbitcenter_hit()
if ballsonplayfield = 1 and MinionPOPUP3.TransY = 0 and MinionPOPUP2.TransY = 0 and MinionPOPUP1.TransY = 0 then
Divertercenter.RotateToEnd
DOF 131, DOFPulse
divertertimer.enabled = true
FlashForMs overkill, 1500, 100, 0
FlashForMs Flasher4, 800, 50, 0:DOF 143, DOFPulse
end if

if ballsonplayfield = 1 and MinionPOPUP1.TransY = 70 then
end if
if ballsonplayfield = 1 and MinionPOPUP2.TransY = 100 then
end if
if ballsonplayfield = 1 and MinionPOPUP3.TransY = 40 then
end if

end sub

sub divertertimer_timer()
'divertertimer.set false
divertertimer.enabled = 0
Divertercenter.RotateToStart
DOF 131, DOFPulse
if wizard.state = 2 then
overkill.state = 2
FlashForMs Flasher4, 800, 50, 0:DOF 143, DOFPulse
end if
end sub

sub changemusic1_Timer
changemusic1.enabled = false
'playsound = "MinionsUSA"
StopAllMusic
Song = "bgout_MinionsUSA" & ".mp3"
PlayMusic Song
end sub

sub finallightshowstop_Timer
finallightshowstop.enabled = false
WIZARD.state = 0
end sub

sub kickercenter_hit()
    startB2S(6)
  playsoundAtVol "fx_kicker-enter", kickercenter, 1
if WIZARD.state = 2 then
Overkill.state =0
FlashForMs Flasher4, 1000, 50, 0:DOF 144, DOFPulse
Kickercenter.destroyball
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
'******LAST SHOT SEQUENCE*****
addscore 50000000

'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 10000
playsound "gru2"
changemusic1.enabled =  true

'Plungertimersimple.set true, 9000
'plungertimer.set true, 10000

LightSeqAttract1.Play SeqBlinking , , 7, 500
finallightshowstop.enabled = true
BackFlashEffect 2

'DispDmd1.QueueText "[f3][xc][y5]50 MILLION[y18]50 MILLION", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]50 MILLION[y18]50 MILLION", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "50 MILLION", -1, -1, UltraDMD_Animation_ScrollOnUp, 2000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("50 MILLION ") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
'Minion4rotate.set true, 1
Minion4Timer.enabled = 1


'BOBhop.set true, 1

MinionPOPUP1Timer.enabled = 1
MinionPOPUP1DOWNTimer.enabled = 1
MinionPOPUP2Timer.enabled = 1
MinionPOPUP2DOWNTimer.enabled = 1
MinionPOPUP3Timer.enabled = 1
MinionPOPUP3DOWNTimer.enabled = 1



'******RESET FOR NEW PLAYER BALL*****
'******RESET FOR NEW PLAYER BALL*****
'******RESET FOR NEW PLAYER BALL*****

	' In and Outlanes reset:
SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0

	' Bumperlanes reset:
Bumperscore1.state = 0
Bumperscore2.state = 0
Bumperscore3.state = 0
Bumperscore4.state = 0

	' Orbits reset:
OR1.state = 2
OR2.state = 0
OR3.state = 0
OL1.state = 0
OL2.state = 0
OL3.state = 0

	' Ramps reset:
RR1.state = 2
RR2.state = 0
RR3.state = 0
RR4.state = 0
RL1.state = 0
RL2.state = 0
RL3.state = 0
RL4.state = 0

end if
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****
'******LAST SHOT SEQUENCE: RESET FOR NEW PLAYER BALL*****

if WIZARD.state = 0 then
'MinionPOPUP1Timer.enabled = 1
'MinionPOPUP1DOWNTimer.enabled = 0
'MinionPOPUP2Timer.enabled = 1
'MinionPOPUP2DOWNTimer.enabled = 0
'MinionPOPUP3Timer.enabled = 1
StopAllMusic
Kickercenter.destroyball
Minion4Timer.enabled = 1
vpmtimer.addtimer 8000, "UnaBolaMas '"
end if


if Popupcomplete.state = 1 and WIZARD.state = 0 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 5000
playsound "overkill13"
'Plungertimersimple.set true, 4000
'plungertimer.set true, 5000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "SHOOT MINIONS", -1, -1, UltraDMD_Animation_ScrollOnUp, 6000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("SHOOT MINIONS") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
end if

if Overkillbulb3.state = 2 and WIZARD.state = 0 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 7000
playsound "overkill9"
'Plungertimersimple.set true, 6000
'plungertimer.set true, 7000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "SHOOT MINIONS", -1, -1, UltraDMD_Animation_ScrollOnUp, 6000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("SHOOT MINIONS") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
end if

if Overkillbulb2.state = 2 and WIZARD.state = 0 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 6000
playsound "overkill6"
'Plungertimersimple.set true, 5000
'plungertimer.set true, 6000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "SHOOT MINIONS", -1, -1, UltraDMD_Animation_ScrollOnUp, 6000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("SHOOT MINIONS") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
end if

if Overkillbulb1.state = 2 and WIZARD.state = 0 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 9000
playsound "_despicablepflrp"
'Plungertimersimple.set true, 8000
'plungertimer.set true, 9000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "SHOOT MINIONS", -1, -1, UltraDMD_Animation_ScrollOnUp, 6000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("SHOOT MINIONS") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
end if

if Overkillbulb1.state = 0 and WIZARD.state = 0 then
'effectmusic 1, fadevolume, 0, 500
'musicin.set true, 9000
playsound "_bottomhilarious"
'Plungertimersimple.set true, 8000
'plungertimer.set true, 9000

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT[y18]MINIONS", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "SHOOT MINIONS", -1, -1, UltraDMD_Animation_ScrollOnUp, 6000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("SHOOT MINIONS") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
end if
end sub

'MinionPOPUPS WALL HITS (popuplights)

'MinionPOPUP3:
sub MinionPOPUP3collide_hit()
FlashForms SlingFlashL, 200, 100, 0
DOF 142, DOFPulse
FlashForms SlingFlashR, 200, 100, 0
DOF 143, DOFPulse
playsound SoundFXDOF("bumper1",123,DOFPulse,DOFShaker)
playsound "flapclosed"
playsound "solenoidminions"
playsound "FX33"
MinionPOPUP3DOWNTimer.enabled = true
'next pops up:
MinionPOPUP2Timer.enabled = true

if Popupcomplete.state = 1 then
addscore 100000
DOF 145, DOFPulse
end if
if Overkillbulb3.state = 2 then
addscore 75000
end if
if Overkillbulb2.state = 2 then
addscore 50000
end if
if Overkillbulb1.state = 2 then
addscore 25000
end if
if Overkillbulb1.state = 0 then
addscore 10000
end if

end sub
'MinionPOPUP3 end

'MinionPOPUP2:
sub MinionPOPUP2collide_hit()
FlashForMs SlingFlashL, 200, 100, 0
DOF 142, DOFPulse
FlashForMs SlingFlashR, 200, 100, 0
DOF 143, DOFPulse
playsound SoundFXDOF("bumper1",123,DOFPulse,DOFShaker)
playsound "flapclosed"
playsound "solenoidminions"
playsound "FX32"
MinionPOPUP2DOWNTimer.enabled = true
'next pops up:
MinionPOPUP1Timer.enabled = true

if Popupcomplete.state = 1 then
addscore 100000
end if
if Overkillbulb3.state = 2 then
addscore 75000
end if
if Overkillbulb2.state = 2 then
addscore 50000
end if
if Overkillbulb1.state = 2 then
addscore 25000
end if
if Overkillbulb1.state = 0 then
addscore 10000
end if

end sub
'MinionPOPUP2 end

'MinionPOPUP1:
sub MinionPOPUP1collide_hit()
FlashForms SlingFlashL, 200, 100, 0
DOF 142, DOFPulse
FlashForms SlingFlashR, 200, 100, 0
DOF 143, DOFPulse
playsound SoundFXDOF("bumper1",123,DOFPulse,DOFShaker)
playsound "flapclosed"
playsound "solenoidminions"
MinionPOPUP1DOWNTimer.enabled = true
playsound "FX31"
'NONE pops up:



if Popupcomplete.state = 1 then
addscore (100000)

checkforWIZARD()
end if
if Overkillbulb3.state = 2 then
Overkillbulb1.state = 0
FlashForMs Overkillbulb1b, 100, 10, 0  'off
Overkillbulb2.state = 0
FlashForMs Overkillbulb2b, 100, 10, 0  'off
Overkillbulb3.state = 0
FlashForMs Overkillbulb3b, 100, 10, 0  'off
addscore 75000
FlashForMs Popupcomplete,  300, 50, 1
StopAllMusic
Song = "bgout_MinionsVNC" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsVNC", true, 0.7

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
BackFlashEffect 2
'Attractlightstimer.set true, 1000

'DispDmd1.QueueText "[f3][xc][y5]MODE[y18]COMPLETED", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]MODE[y18]COMPLETED", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 2000
DMD_DisplaySceneEx "DMD1.png", "", 15, 4, "MODE COMPLETED", -1, -1, UltraDMD_Animation_ScrollOnUp, 2000, UltraDMD_Animation_ScrollOffDown
DMD "_", CenterLine(1, ("MODE COMPLETED") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""

checkforWIZARD()
end if
if Overkillbulb2.state = 2 then
addscore 50000
Overkillbulb2.state = 1
FlashForMs Overkillbulb2b, 200, 10, 3   'on
Overkillbulb3.state = 2
FlashForMs Overkillbulb3b, 1, 500, 1  'blink
end if
if Overkillbulb1.state = 2 then
addscore 25000
StopAllMusic
Song = "bgout_MinionsVNC" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsVNC", true, 0.7
Overkillbulb1.state = 1
FlashForMs Overkillbulb1b, 200, 10, 3   'on
Overkillbulb2.state = 2
FlashForMs Overkillbulb2b, 1, 500, 1  'blink
end if
if Overkillbulb1.state = 0 then
addscore 10000
Overkillbulb1.state = 2
FlashForMs Overkillbulb1b, 1, 500, 1  'blink
end if

end sub
'MinionPOPUP1 end

Sub UnaBolaMas()
    BallsOnPlayfield = BallsOnPlayfield -1
    CreateNewBall()
    bAutoPlunger = True
    bBallInPlungerLane = True
    EnableBallSaver 15
    song = "bgout_MinionsUSA" & ".mp3"
    PlayMusic Song
End Sub

' NOT FINISHED YET...........................

' OVERKILL KICKER & MINION POPUPS END***************************************************************

sub checkforWIZARD()
if TRcomplete.state = 1 and Popupcomplete.state = 1 and Tmultiballcomplete.state = 1 and RampsComplete.state = 1 and SPbulbscomplete.state = 1 then

' ....shoot ONLY overkill for 50 million points......
WIZARD.state = 2
Overkill.state = 2
Flasher4.state = 2

' ....INSERTS OFF
TRcomplete.state = 0
Popupcomplete.state = 0
Tmultiballcomplete.state = 0
RampsComplete.state = 0
SPbulbscomplete.state = 0
TR1.state = 0
TR2.state = 0
TR3.state = 0
TR4.state = 0
SP1.state = 0
SP2.state = 0
SP3.state = 0
SP4.state = 0
SP5.state = 0
RL1.state = 0
RL2.state = 0
RL3.state = 0
RL4.state = 0
RR1.state = 0
RR2.state = 0
RR3.state = 0
RR4.state = 0
Lightlock1.state = 0
Lightlock2.state = 0
Lightlock3.state = 0
Lightlock4.state = 0
Lightlock5.state = 0
Lock1.state = 0
Lock2.state = 0
Lock3.state = 0
OR1.state = 0
OR2.state = 0
OR3.state = 0
OL1.state = 0
OL2.state = 0
OL3.state = 0
J1.state = 0
J2.state = 0
J3.state = 0
J4.state = 0
Bumperscore1.state = 0
Bumperscore2.state = 0
Bumperscore3.state = 0
Bumperscore4.state = 0

' ....SIGN BULBS OFF
TRbulb1.state = 0
TRbulb2.state = 0
TRbulb3.state = 0
Overkillbulb1.state = 0
Overkillbulb2.state = 0
Overkillbulb3.state = 0
SPbulb1.state = 0
SPbulb2.state = 0
SPbulb3.state = 0

' ....BALL drains for last shot sequence:
StopAllMusic
Song = "bgout_MinionsWIZARD" & ".mp3"
PlayMusic Song
'playmusic 1, "MinionsWIZARD", true, 1.0

LightSeqAttract1.UpdateInterval = 5
LightSeqAttract1.Play SeqUpOn, 80, 1
'Attractlights.UpdateInterval = 5
'Attractlights.Play SeqUpOn, 80, 1
'Attractlightstimer.set true, 1000

'flasherstimer.set true, 1

'DispDmd1.QueueText "[f3][xc][y5]SHOOT HIDEOUT[y18]for 50 MILLION", deFlip, 10000, True
'DispDmd2.QueueText "[f3][xc][y5]SHOOT HIDEOUT[y18]for 50 MILLION", deFlip, 10000, True
'scoreupdate = false
'flushdmdtimer.set true , 10000
DMD_DisplaySceneEx "DMD1.png", "SHOOT HIDEOUT", 15, 4, "FOR 50 MILLION", -1, -1, UltraDMD_Animation_ScrollOnUp, 3000, UltraDMD_Animation_ScrollOffDown
DMD CenterLine(0, "SHOOT HIDEOUT"), CenterLine(1, "FOR 50 MILLION "), 0, eNone, eNone, eNone, 3000, True, ""
'DMD "_", CenterLine(1, ("SHOOT HIDEOUT FOR 50 MILLION") ), 0, eNone, eBlinkFast, eNone, 2000, True, ""
'			bBallSaverActive = TRUE'
'			' start the timer
'			BallSaverTimer.Interval = constBallSaverTime
'			BallSaverTimer.Enabled = TRUE
'			' if you have a ball saver light you might want to turn it on at this
'			' point (or make it flash)
'			LightShootAgain.State = 2
EnableBallSaver 15
end if
end sub

Sub StopAllMusic:EndMusic:End Sub

' *******************************************************************************************************
' Positional Sound Playback Functions by DJRobX
' PlaySound sound, 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 0, 1, AudioFade(ActiveBall)
' *******************************************************************************************************

' Play a sound, depending on the X,Y position of the table element (especially cool for surround speaker setups, otherwise stereo panning only)
' parameters (defaults): loopcount (1), volume (1), randompitch (0), pitch (0), useexisting (0), restart (1))
' Note that this will not work (currently) for walls/slingshots as these do not feature a simple, single X,Y position

Sub PlayXYSound(soundname, tableobj, loopcount, volume, randompitch, pitch, useexisting, restart)
  PlaySound soundname, loopcount, volume, AudioPan(tableobj), randompitch, pitch, useexisting, restart, AudioFade(tableobj)
End Sub

' Set position as table object (Use object or light but NOT wall) and Vol to 1

Sub PlaySoundAt(soundname, tableobj)
  PlaySound soundname, 1, 1, AudioPan(tableobj), 0,0,0, 1, AudioFade(tableobj)
End Sub

'Set all as per ball position & speed.

Sub PlaySoundAtBall(soundname)
  PlaySoundAt soundname, ActiveBall
End Sub

'Set position as table object and Vol manually.

Sub PlaySoundAtVol(sound, tableobj, Volum)
  PlaySound sound, 1, Volum, Pan(tableobj), 0,0,0, 1, AudioFade(tableobj)
End Sub

'Set all as per ball position & speed, but Vol Multiplier may be used eg; PlaySoundAtBallVol "sound",3

Sub PlaySoundAtBallVol(sound, VolMult)
  PlaySound sound, 0, Vol(ActiveBall) * VolMult, Pan(ActiveBall), 0, Pitch(ActiveBall), 0, 1, AudioFade(ActiveBall)
End Sub

'Set position as bumperX and Vol manually.

Sub PlaySoundAtBumperVol(sound, tableobj, Vol)
  PlaySound sound, 1, Vol, Pan(tableobj), 0,0,1, 1, AudioFade(tableobj)
End Sub

'*********************************************************************
'                     Supporting Ball & Sound Functions
'*********************************************************************

Function AudioFade(tableobj) ' Fades between front and back of the table (for surround systems or 2x2 speakers, etc), depending on the Y position on the table. "table1" is the name of the table
  Dim tmp
  tmp = tableobj.y * 2 / table1.height-1
  If tmp > 0 Then
    AudioFade = Csng(tmp ^10)
  Else
    AudioFade = Csng(-((- tmp) ^10) )
  End If
End Function

Function AudioPan(tableobj) ' Calculates the pan for a tableobj based on the X position on the table. "table1" is the name of the table
  Dim tmp
  tmp = tableobj.x * 2 / table1.width-1
  If tmp > 0 Then
    AudioPan = Csng(tmp ^10)
  Else
    AudioPan = Csng(-((- tmp) ^10) )
  End If
End Function

Function Pan(ball) ' Calculates the pan for a ball based on the X position on the table. "table1" is the name of the table
    Dim tmp
    tmp = ball.x * 2 / table1.width-1
    If tmp > 0 Then
        Pan = Csng(tmp ^10)
    Else
        Pan = Csng(-((- tmp) ^10) )
    End If
End Function

Function AudioFade(ball) ' Can this be together with the above function ?
  Dim tmp
  tmp = ball.y * 2 / Table1.height-1
  If tmp > 0 Then
    AudioFade = Csng(tmp ^10)
  Else
    AudioFade = Csng(-((- tmp) ^10) )
  End If
End Function

Function Vol(ball) ' Calculates the Volume of the sound based on the ball speed
  Vol = Csng(BallVel(ball) ^2 / VolDiv)
End Function

Function Pitch(ball) ' Calculates the pitch of the sound based on the ball speed
  Pitch = BallVel(ball) * 20
End Function

Function BallVel(ball) 'Calculates the ball speed
  BallVel = INT(SQR((ball.VelX ^2) + (ball.VelY ^2) ) )
End Function

'*****************************************
'      JP's VP10 Rolling Sounds
'*****************************************

Const tnob = 20 ' total number of balls
Const lob = 1   'number of locked balls
ReDim rolling(tnob)
InitRolling

Sub InitRolling
    Dim i
    For i = 0 to tnob
        rolling(i) = False
    Next
End Sub

Sub RollingUpdate()
    Dim BOT, b, ballpitch
    BOT = GetBalls

    ' stop the sound of deleted balls
    For b = UBound(BOT) + 1 to tnob
        rolling(b) = False
        StopSound("fx_ballrolling" & b)
    Next

    ' exit the sub if no balls on the table
    If UBound(BOT) = 3 Then Exit Sub 'there are always 4 balls on this table

    ' play the rolling sound for each ball

    For b = 0 to UBound(BOT)
      If BallVel(BOT(b) ) > 1 Then
        rolling(b) = True
        if BOT(b).z < 30 Then ' Ball on playfield
          PlaySound("fx_ballrolling" & b), -1, Vol(BOT(b) ), Pan(BOT(b) ), 0, Pitch(BOT(b) ), 1, 0, AudioFade(BOT(b) )
        Else ' Ball on raised ramp
          PlaySound("fx_ballrolling" & b), -1, Vol(BOT(b) )*.5, Pan(BOT(b) ), 0, Pitch(BOT(b) )+50000, 1, 0, AudioFade(BOT(b) )
        End If
      Else
        If rolling(b) = True Then
          StopSound("fx_ballrolling" & b)
          rolling(b) = False
        End If
      End If
    Next
End Sub

'**********************
' Ball Collision Sound
'**********************

Sub OnBallBallCollision(ball1, ball2, velocity)
    PlaySound("fx_collide"), 0, Csng(velocity) ^2 / (VolDiv/VolCol), Pan(ball1), 0, Pitch(ball1), 0, 0, AudioFade(ball1)
End Sub


