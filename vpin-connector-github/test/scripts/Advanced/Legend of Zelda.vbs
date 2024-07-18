' ****************************************************************
'                       VISUAL PINBALL X
'          JPSalas script for The Legend of Zelda v1.3.1
' Added most of the UltraDMD and DOF commands from Sindab's mod
' rev 1.3.1 argrim added all the missing DOF commands
' ****************************************************************

Option Explicit

Const BallSize = 50

Const UseReelsDMD = True                             ' you want to use original desktop DMD?
Const UseUltraDMD = True                             ' you want to use UltraDMD?

Const UDMDFilesDir = "%TableDir%\%TableName%" 'tells me where I can find the UltraDMD files
'														the keyword %TableDir% means the folder where the table is stored
'														the keyword %TableName% means the actual filename (without path and extension) of this table

Dim DesktopMode:DesktopMode = Table1.ShowDT
Dim Controller

' Load the core.vbs for supporting subs and functions

LoadCoreVBS

Sub LoadCoreVBS
    On Error Resume Next
    ExecuteGlobal GetTextFile("core.vbs")
    ExecuteGlobal GetTextFile("controller.vbs")
    If Err Then MsgBox "Can't open core.vbs or controller.vbs"
    On Error Goto 0
End Sub

Const cGameName = "zof10"
' Define any Constants
Const TableName = "ZELDA"
Const myVersion = "1.0"
Const MaxPlayers = 4
Const BallSaverTime = 20 'in seconds
Const MaxMultiplier = 7  '7x is the max in this game
Const BallsPerGame = 3

' Define Global Variables
Dim AttractMode
Dim PlayersPlayingGame
Dim CurrentPlayer
Dim Credits
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
Dim bAutoPlunger
Dim bSkillshotReady

'Define This Table objects and variables

Dim plungerIM, plungerIM2, cbLeft
Dim CBonus

Dim bExtraBallWonThisBall
Dim MusicChannelInUse
Dim CurrentMusicTunePlaying

Dim NCount
Dim R1Count
Dim R2Count
Dim K1Count
Dim K2Count
Dim cbCount
Dim K6Count
Dim P4Count
Dim P2Count

' *********************************************************************
'                Visual Pinball Defined Script Events
' *********************************************************************

Sub Table1_Init()
	LoadEM
    Dim i
    Randomize

    'Impulse Plunger as autoplunger
    Const IMPowerSetting = 42 ' Plunger Power
    Const IMTime = 1.1        ' Time in seconds for Full Plunge
    Set plungerIM = New cvpmImpulseP
    With plungerIM
        .InitImpulseP swPlunger, IMPowerSetting, IMTime
        .Random 1.5
        .InitExitSnd SoundFX("fx_solenoid",DOFContactors), SoundFX("fx_solenoid",DOFContactors)
        .CreateEvents "plungerIM"
    End With

    'Impulse Plunger as autoplunger
    Const IMPowerSetting2 = 42 ' Plunger Power
    Const IMTime2 = 1.1        ' Time in seconds for Full Plunge
    Set plungerIM2 = New cvpmImpulseP
    With plungerIM2
        .InitImpulseP swPlunger2, IMPowerSetting2, IMTime2
        .Random 1.5
        .InitExitSnd SoundFX("fx_solenoid",DOFContactors), SoundFX("fx_solenoid",DOFContactors)
        .CreateEvents "plungerIM2"
    End With

    ' Captive Balls
    Set cbLeft = New cvpmCaptiveBall
    With cbLeft
        .InitCaptive CapTrigger, CapWall, Array(CapKicker1, CapKicker2), 10
        .NailedBalls = 1
        .ForceTrans = .95
        .MinForce = 3.5
        '.CreateEvents "cbLeft" 'the events are done later in the script to add the hit sound
        .Start
    End With
    CapKicker1.CreateBall

    ' Misc. VP table objects Initialisation, droptargets, animations...
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
    If UseUltraDMD = True Then DMDU_Init
    DMDInit

    ' freeplay or coins
    bFreePlay = FALSE 'we want coins

    ' Setup the lightning according to the nightday slider
    if table1.nightday <50 Then
        for each i in aGiTopLights:i.intensity = i.intensity + (100 - table1.nightday) / 10:next
        for each i in aGiBottomLights:i.intensity = i.intensity + (100 - table1.nightday) / 10:next
        for each i in aFlashers:i.intensity = i.intensity + (100 - table1.nightday) / 10:next
    End If

    ' initialse any other flags
    bOnTheFirstBall = FALSE
    bBallInPlungerLane = FALSE
    bBallSaverActive = FALSE
    bBallSaverReady = FALSE
    bMultiBallMode = FALSE
    bGameInPlay = FALSE
    bMusicOn = TRUE 'TRUE
    bAutoPlunger = FALSE
    BallsOnPlayfield = 0
    BallsInLock = 0
    BallsInHole = 0
    LastSwitchHit = ""
    Tilt = 0
    TiltSensitivity = 6
    Tilted = FALSE
    ChangeGi "Normal"
    InitDelays 'Gametimer delays
    EndOfGame()

    ' Remove the cabinet rails if in FS mode
    If Table1.ShowDT = False then
        lrail.Visible = False
        rrail.Visible = False
    End If
End Sub

'******
' Keys
'******

Sub Table1_KeyDown(ByVal Keycode)
    If Keycode = AddCreditKey Then
        Credits = Credits + 1
        If(Tilted = FALSE)Then
            PlaySound "fx_coin"
            DMDFlush:DMDU_Flush
            DMD "-", CenterLine(1, "CREDITS: " & Credits), "-", eNone, eNone, eNone, 500, TRUE, ""
            DMDU_ShowCredits
            If NOT bGameInPlay Then ShowTableInfo:DMDU_RepeatIntro
        End If
    End If

    If keycode = PlungerKey Then
        Plunger.Pullback
    End If

    If bGameInPlay AND NOT Tilted Then

        If keycode = LeftTiltKey Then Nudge 90, 1.6:PlaySound SoundFX("fx_nudge",0), 0, 1, -0.1, 0.25:CheckTilt
        If keycode = RightTiltKey Then Nudge 270, 1.6:PlaySound SoundFX("fx_nudge",0), 0, 1, 0.1, 0.25:CheckTilt
        If keycode = CenterTiltKey Then Nudge 0, 2.8:PlaySound SoundFX("fx_nudge",0), 0, 1, 1, 0.25:CheckTilt

        If keycode = LeftFlipperKey Then SolLFlipper 1
        If keycode = RightFlipperKey Then SolRFlipper 1

        If keycode = StartGameKey Then
            If((PlayersPlayingGame <MaxPlayers)AND(bOnTheFirstBall = TRUE))Then

                If(bFreePlay = TRUE)Then
                    PlayersPlayingGame = PlayersPlayingGame + 1
                    TotalGamesPlayed = TotalGamesPlayed + 1
                    DMDU_ShowScoreBoard
                Else
                    If(Credits> 0)then
                        PlayersPlayingGame = PlayersPlayingGame + 1
                        TotalGamesPlayed = TotalGamesPlayed + 1
                        Credits = Credits - 1
                        DMDU_ShowScoreBoard
                    Else
                        ' Not Enough Credits to start a game.
                        DMDFlush
                        DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "INSERT COIN"), "", eNone, eBlink, eNone, 500, TRUE, ""
                        DMDU_Flush:DMDU_ShowCredits
                    End If
                End If
            End If
        End If
        Else ' If (GameInPlay)

            If keycode = StartGameKey Then
                If(bFreePlay = TRUE)Then
                    If(BallsOnPlayfield = 0)Then
                        ResetForNewGame()
                    End If
                Else
                    If(Credits> 0)Then
                        If(BallsOnPlayfield = 0)Then
                            Credits = Credits - 1
                            ResetForNewGame()
                        End If
                    Else
                        ' Not Enough Credits to start a game.
                        DMDFlush:DMDU_Flush
                        DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "INSERT COIN"), "", eNone, eBlink, eNone, 500, TRUE, ""
                        DMDU_ShowCredits
                    ShowTableInfo:DMDU_RepeatIntro
                    End If
                End If
            End If
    End If ' If (GameInPlay)
    If hsbModeActive Then EnterHighScoreKey(keycode)

' Table specific

' test keys

End Sub

Sub Table1_KeyUp(ByVal keycode)

    If bGameInPLay AND NOT Tilted Then
        If keycode = LeftFlipperKey Then SolLFlipper 0
        If keycode = RightFlipperKey Then SolRFlipper 0
    End If

    If keycode = PlungerKey Then
        Plunger.Fire
    End If

' test keys
End Sub

'*************
' Pause Table
'*************

Sub table1_Paused
End Sub

Sub table1_unPaused
    If Isobject(Controller)Then Controller.Pause
End Sub

Sub table1_Exit
    If Isobject(Controller)Then Controller.Pause = False:Controller.Stop
    DMDU_Flush
    Savehs
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
        PlaySound SoundFX("fx_flipperup",DOFFlippers), 0, 1, -0.05, 0.15
        LeftFlipper.RotateToEnd
        LeftFlipper1.RotateToEnd
        DOF dFlipperLeft, 1
        RotateLaneLightsLeft
    Else
        PlaySound SoundFX("fx_flipperdown",DOFFlippers), 0, 1, -0.05, 0.15
        LeftFlipper.RotateToStart
        LeftFlipper1.RotateToStart
        DOF dFlipperLeft, 0
    End If
End Sub

Sub SolRFlipper(Enabled)
    If Enabled Then
        PlaySound SoundFX("fx_flipperup",DOFFlippers), 0, 1, 0.05, 0.15
        RightFlipper.RotateToEnd
        RightFlipper1.RotateToEnd
        DOF dFlipperRight, 1
        RotateLaneLightsRight
    Else
        PlaySound SoundFX("fx_flipperdown",DOFFlippers), 0, 1, 0.05, 0.15
        RightFlipper.RotateToStart
        RightFlipper1.RotateToStart
	    DOF dFlipperRight, 0
    End If
End Sub

' flippers hit Sound

Sub LeftFlipper_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, -0.05, 0.25
End Sub

Sub LeftFlipper1_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, -0.05, 0.25
End Sub

Sub RightFlipper_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, 0.05, 0.25
End Sub

Sub RightFlipper1_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, 0.05, 0.25
End Sub

' Update flipper primitives
Sub flippers_update
    LeftSword.RotZ = LeftFlipper.CurrentAngle
    LeftSword1.RotZ = LeftFlipper1.CurrentAngle
    RightSword.RotZ = RightFlipper.CurrentAngle
    RightSword1.RotZ = RightFlipper1.CurrentAngle
End Sub

Sub RotateLaneLightsLeft
    Dim TempState
    TempState = LightLeftOutlane.State
    LightLeftOutlane.State = LightLeftInlane1.State
    LightLeftInlane1.State = LightLeftInlane.State
    LightLeftInlane.State = LightRightInlane.State
    LightRightInlane.State = LightRightOutlane.State
    LightRightOutlane.State = TempState
End Sub

Sub RotateLaneLightsRight
    Dim TempState
    TempState = LightRightOutlane.State
    LightRightOutlane.State = LightRightInlane.State
    LightRightInlane.State = LightLeftInlane.State
    LightLeftInlane.State = LightLeftInlane1.State
    LightLeftInlane1.State = LightLeftOutlane.State
    LightLeftOutlane.State = TempState
End SUb

'*********
' TILT
'*********

'NOTE: The Game Timer subtracts .01 from the "Tilt" variable every round

Sub CheckTilt                                  'Called when table is nudged
    Tilt = Tilt + TiltSensitivity              'Add to tilt count
    If(Tilt> TiltSensitivity)AND(Tilt <15)Then 'show a warning
        DMD "-", CenterLine(1, "CAREFUL!"), "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_ShowWarning TRUE, "CAREFUL!", 500, "":DMDU_ShowScoreBoard
    End if
    If Tilt> 15 Then 'If more that 15 then TILT the table
        Tilted = TRUE
        'display Tilt
        DMDFlush:DMDU_Flush
        DMD CenterLine(0, "TILT!"), "", "", eBlinkFast, eNone, eNone, 200, FALSE, ""
        DMDU_ShowWarning TRUE, "TILT!", 200, ""
        DisableTable TRUE
        TiltRecoveryDelay = 50 'start the Tilt delay to check for all the balls to be drained
    End If
End Sub

Sub DisableTable(Enabled)
    If Enabled Then
        'turn off GI and turn off all the lights
        GiOff
        'Disable slings, bumpers etc
        LeftFlipper.RotateToStart
        RightFlipper.RotateToStart
        LeftFlipper1.RotateToStart
        RightFlipper1.RotateToStart
        'Bumper1.Force = 0

        LeftSlingshot.Disabled = 1
        RightSlingshot.Disabled = 1
    Else
        'turn back on GI and the lights
        GiOn
        'Bumper1.Force = 6
        LeftSlingshot.Disabled = 0
        RightSlingshot.Disabled = 0
        'clean up the buffer display
        DMDFlush:DMDU_Flush
    End If
End Sub

Sub TiltRecovery()
    ' if all the balls have been drained then..
    If(BallsOnPlayfield = 0)Then
        ' do the normal end of ball thing (this doesn't give a bonus if the table is tilted)
        EndOfBall()
    Else
        ' else retry (checks again in another second or so)
        TiltRecoveryDelay = 50
    End If
End Sub

'********************
' Music using sounds
'********************

Dim Song
Song = ""

Sub PlaySong(sng)
    If bMusicOn Then
        If sng <> Song Then
            StopSound Song
            Song = sng
            PlaySound Song, - 1, 0.15
        End If
    End If
End Sub

Sub StopSong 'stop current song
    StopSound Song
    Song = ""
End Sub

'******************************
'     Game Timer Loop
' used for all the small delays
'******************************

Dim FirstBallDelay
Dim EndOfBall2Delay
Dim BallSaverDelay
Dim CreateNewBallDelay
Dim TiltRecoveryDelay
Dim AddMultiballDelay
Dim BonusCountDelay
Dim MatchDelay
Dim SkillShotDelay

'this table delays
Dim ResetRTargetsDelay

Sub InitDelays 'reset all delays
    FirstBallDelay = 0
    EndOfBall2Delay = 0
    BallSaverDelay = 0
    CreateNewBallDelay = 0
    TiltRecoveryDelay = 0
    AddMultiballDelay = 0
    BonusCountDelay = 0
    MatchDelay = 0
    SkillShotDelay = 0
End Sub

Sub GameTimer_Timer
    ' call realtime subs
    GIUpdateTimer

    ' DecreaseTilt
    If Tilt> 0 Then
        Tilt = Tilt - 0.1
    End If

    ' TiltRecovery Delay
    If TiltRecoveryDelay> 0 Then
        TiltRecoveryDelay = TiltRecoveryDelay - 1
        If TiltRecoveryDelay = 0 Then
            TiltRecovery
        End If
    End If

    'check the delays

    ' This is used to delay the start of a game to allow any attract sequence to
    ' complete.  When it expires it creates a ball for the player to start playing with
    If FirstBallDelay> 0 Then
        FirstBallDelay = FirstBallDelay - 1
        If FirstBallDelay = 0 Then
            ' reset the table for a new ball
            ResetForNewPlayerBall()
            ' create a new ball in the shooters lane
            CreateNewBallDelay = 20
        End If
    End If

    If EndOfBall2Delay> 0 Then
        EndOfBall2Delay = EndOfBall2Delay - 1
        If EndOfBall2Delay = 0 Then
            EndOfBall2
        End If
    End If

    If CreateNewBallDelay> 0 Then
        CreateNewBallDelay = CreateNewBallDelay - 1
        If CreateNewBallDelay = 0 Then
            CreateNewBall
        End If
    End If

    If BallSaverDelay> 0 Then
        BallSaverDelay = BallSaverDelay - 1
        If BallSaverDelay = 60 Then
            LightShootAgain.BlinkInterval = 50 'blink faster
            LightShootAgain.State = 2
        End If
        If BallSaverDelay = 0 Then
            BallSaverExpired
        End If
    End If

    If AddMultiballDelay> 0 Then
        AddMultiballDelay = AddMultiballDelay - 1
        If AddMultiballDelay = 0 Then
            CreateMultiball
        End If
    End If

    If SkillShotDelay> 0 Then
        SkillShotDelay = SkillShotDelay - 1
        If SkillShotDelay = 0 Then
            ResetSkillShot
        End If
    End If

    ' Game specific delays
    If AddRampBallDelay> 0 Then
        AddRampBallDelay = AddRampBallDelay - 1
        If AddRampBallDelay = 0 Then
            CreateRampBall
        End If
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
OldGiState = -1  'start witht the Gi off

Sub ChangeGi(Gi) 'changes the gi color
    Dim gilight
    Select Case Gi
        Case "Normal"
            For each giLight in aGITopLights
                giLight.color = RGB(25, 19, 14)
                giLight.colorfull = RGB(255, 197, 143)
            Next
            For each giLight in aGIBottomLights
                giLight.color = RGB(25, 19, 14)
                giLight.colorfull = RGB(255, 197, 143)
            Next
        Case "Green"
            For each giLight in aGITopLights
                giLight.color = RGB(0, 18, 0)
                giLight.colorfull = RGB(0, 180, 0)
            Next
            For each giLight in aGIBottomLights
                giLight.color = RGB(0, 18, 0)
                giLight.colorfull = RGB(0, 180, 0)
            Next
        Case "Red"
            For each giLight in aGITopLights
                giLight.color = RGB(18, 0, 0)
                giLight.colorfull = RGB(180, 0, 0)
            Next
            For each giLight in aGIBottomLights
                giLight.color = RGB(18, 0, 0)
                giLight.colorfull = RGB(180, 0, 0)
            Next
    End Select
End Sub

Sub GIUpdateTimer 'called from the gametimer
    Dim tmp, obj
    tmp = Getballs
    If UBound(tmp) <> OldGiState Then
        OldGiState = Ubound(tmp)
        If UBound(tmp) = 1 Then ' 1: since we have 2 captive balls and 1 for the car animation, then Ubound will show 2, so no balls on the table then turn off gi
            GiOff
        Else
            Gion
        End If
    End If
End Sub

Sub GiOn
    Dim bulb
    For each bulb in aGiTopLights
        bulb.State = 1
    Next
    For each bulb in aGiBottomLights
        bulb.State = 1
    Next
    Bulb1.State = 1:Bulb2.State = 1
End Sub

Sub GiOff
    Dim bulb
    For each bulb in aGiTopLights
        bulb.State = 0
    Next
    For each bulb in aGiBottomLights
        bulb.State = 0
    Next
    Bulb1.State = 0:Bulb2.State = 0
End Sub

' *********************************************************************
'                      Supporting Ball & Sound Functions
' *********************************************************************

Function Vol(ball) ' Calculates the Volume of the sound based on the ball speed
    Vol = Csng(BallVel(ball) ^2 / 2000)
End Function

Function Pan(ball) ' Calculates the pan for a ball based on the X position on the table. "table1" is the name of the table
    Dim tmp
    tmp = ball.x * 2 / table1.width-1
    If tmp> 0 Then
        Pan = Csng(tmp ^10)
    Else
        Pan = Csng(-((- tmp) ^10))
    End If
End Function

Function Pitch(ball) ' Calculates the pitch of the sound based on the ball speed
    Pitch = BallVel(ball) * 20
End Function

Function BallVel(ball) 'Calculates the ball speed
    BallVel = INT(SQR((ball.VelX ^2) + (ball.VelY ^2)))
End Function

'*****************************************
'      JP's VP10 Rolling Sounds
'*****************************************

Const tnob = 9 ' total number of balls
ReDim rolling(tnob)
InitRolling

Sub InitRolling
    Dim i
    For i = 0 to tnob
        rolling(i) = FALSE
    Next
End Sub

Sub RollingTimer_Timer()
    Dim BOT, b
    BOT = GetBalls

    ' stop the sound of deleted balls
    For b = UBound(BOT) + 1 to tnob
        rolling(b) = FALSE
        StopSound("fx_ballrolling" & b)
    Next

    ' exit the sub if no balls on the table
    If UBound(BOT) = -1 Then Exit Sub

    ' play the rolling sound for each ball
    For b = 0 to UBound(BOT)
        If BallVel(BOT(b))> 1 AND BOT(b).z <30 Then
            rolling(b) = TRUE
            PlaySound("fx_ballrolling" & b), -1, Vol(BOT(b)), Pan(BOT(b)), 0, Pitch(BOT(b)), 1, 0
        Else
            If rolling(b) = TRUE Then
                StopSound("fx_ballrolling" & b)
                rolling(b) = FALSE
            End If
        End If
    Next
End Sub

'**********************
' Ball Collision Sound
'**********************

Sub OnBallBallCollision(ball1, ball2, velocity)
    PlaySound("fx_collide"), 0, Csng(velocity) ^2 / 2000, Pan(ball1), 0, Pitch(ball1), 0, 0
End Sub

'******************************
' Diverse Collection Hit Sounds
'******************************

Sub aRubbers_Hit(idx):PlaySound "fx_rubber", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0:End Sub
Sub aPostRubbers_Hit(idx):PlaySound "fx_postrubber", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0:End Sub
Sub aMetals_Hit(idx):PlaySound "fx_MetalHit", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0:End Sub
Sub aPlastics_Hit(idx):PlaySound "fx_PlasticHit", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0:End Sub
Sub aGates_Hit(idx):PlaySound "fx_Gate", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0:End Sub
Sub aWoods_Hit(idx):PlaySound "fx_Woodhit", 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0:End Sub

' Ramp Soundss
Sub REnd1_Hit()
    StopSound "fx_metalrolling"
    PlaySound "fx_ballrampdrop", 0, 1, pan(ActiveBall)
End Sub

Sub REnd2_Hit()
    StopSound "fx_metalrolling"
    PlaySound "fx_ballrampdrop", 0, 1, pan(ActiveBall)
End Sub

' *********************************************************************
'                        User Defined Script Events
' *********************************************************************

' Initialise the Table for a new Game
'
Sub ResetForNewGame()
    Dim i

    bGameInPLay = TRUE

    'resets the score display, and turn off attrack mode
    StopAttractMode
    GiOn

    TotalGamesPlayed = TotalGamesPlayed + 1
    CurrentPlayer = 1
    PlayersPlayingGame = 1
    bOnTheFirstBall = TRUE
    For i = 1 To MaxPlayers
        Score(i) = 0
        BonusPoints(i) = 0
        BonusMultiplier(i) = 1
        BallsRemaining(i) = BallsPerGame
        ExtraBallsAwards(i) = 0
    Next

    ' initialise any other flags
    bMultiBallMode = FALSE
    Tilt = 0

    ' initialise Game variables
    zelda_Init()

    ' you may wish to start some music, play a sound, do whatever at this point

    ' set up the start delay to handle any Start of Game Attract Sequence
    FirstBallDelay = 30
End Sub

' (Re-)Initialise the Table for a new ball (either a new ball after the player has
' lost one or we have moved onto the next player (if multiple are playing))
'
Sub ResetForNewPlayerBall()
    ' make sure the correct display is upto date
    AddScore 0

    ' set the current players bonus multiplier back down to 1X
    SetBonusMultiplier 1

    ' reset any drop targets, lights, game modes etc..
    LightShootAgain.State = 0
    CBonus = 0
    bExtraBallWonThisBall = FALSE
    TurnOffPlayfieldLights()
'PlayMusicForMode 1

End Sub

' Create a new ball on the Playfield
'
Sub CreateNewBall()
    ' create a ball in the plunger lane kicker.
    BallRelease.Createball

    ' There is a (or another) ball on the playfield
    BallsOnPlayfield = BallsOnPlayfield + 1

    ' kick it out..
    PlaySound SoundFX("fx_Ballrel",DOFContactors), 0, 1, 0.1, 0.1
    BallRelease.Kick 90, 4
    DOF dBallRelease, 2

    ' Special for this table
    ' If the bautoplunger flag is set then this is an extra ball, a multiball or a ball that has been saved by the timer
    If bAutoPlunger Then Exit Sub

    ' This is the first ball on the playfield then we could activate the Skillshot and the ball saver
    If BallsOnPlayfield = 1 Then
        PlaySong "m_main"
        bSkillshotReady = TRUE
        bBallSaverReady = TRUE
        If Score(CurrentPlayer) = 0 Then
        'ActiveMode = 1 'set the first mode as the default
        End If
    End If
    Popup1.Isdropped = 0
    Popup2.Isdropped = 0
    Popup3.Isdropped = 0
    DOF dCastleGate, 2
    If(LightG1.State = 1)Then
        LightA1.State = 0
        LightA2.State = 0
        LightA3.State = 0
        LightA4.State = 0
        LightA5.State = 0
        LightG1.State = 0
        TimerA1.Enabled = TRUE
    End If
End Sub

' Add extra balls to the table with autoplunger
' Use it as AddMultiball 4 to add 4 extra balls to the table
Sub AddMultiball(nballs)
    mBalls2Eject = mBalls2Eject + nballs
    AddMultiballDelay = 40
End Sub

' Eject the ball after the delay, AddMultiballDelay
Sub CreateMultiball()
    bAutoPlunger = TRUE

    ' wait if there is a ball in the plunger lane
    If bBallInPlungerLane Then
        AddMultiballDelay = 10
    Else

        ' create a ball in the plunger lane kicker.
        BallRelease.CreateBall

        ' There is a (or another) ball on the playfield
        BallsOnPlayfield = BallsOnPlayfield + 1
        ' if there is 2 or more balls then set the multibal flag
        If BallsOnPlayfield> 1 Then
            bMultiBallMode = TRUE
            DOF dMultiBall, 2
            ChangeGi "Green"
        End If
        ' kick it out..
        PlaySound SoundFX("fx_Ballrel",DOFContactors), 0, 1, 0.1, 0.1
        BallRelease.Kick 90, 4

        mBalls2Eject = mBalls2Eject -1
        If mBalls2Eject Then 'if there are more balls to eject then do that
            AddMultiballDelay = 40
        End If
    End If
End Sub

' The Player has lost his ball (there are no more balls on the playfield).
' Handle any bonus points awarded
'
Sub EndOfBall()
    ' the first ball has been lost. From this point on no new players can join in
    bOnTheFirstBall = FALSE

    ' only process any of this if the table is not tilted.  (the tilt recovery
    ' mechanism will handle any extra balls or end of game)
    If(Tilted = FALSE)Then
        Dim AwardPoints

        ' add in any bonus points (multipled by the bunus multiplier)
        AwardPoints = BonusPoints(CurrentPlayer) * BonusMultiplier(CurrentPlayer)
        AddScore AwardPoints
        'AddDebugText "Bonus Points = " & AwardPoints

        DMD "-", CenterLine(1, "BONUS: " & BonusPoints(CurrentPlayer) & " X" & BonusMultiplier(CurrentPlayer)), "", eNone, eBlink, eNone, 1000, TRUE, ""
        DMDU_BonusText TRUE, 0, "BONUS:", 6, 15, BonusPoints(CurrentPlayer) & " X" & BonusMultiplier(CurrentPlayer), 6, 15, 1500, ""

        ' add a bit of a delay to allow for the bonus points to be added up and start the end of ball part 2
        EndOfBall2Delay = 100
    Else
        ' we were tilted, reset the internal tilted flag and warnings
        Tilted = FALSE
        Tilt = 0
        DisableTable FALSE   'enable again bumpers and slingshots
        EndOfBall2Delay = 10 'no bonus to count so move quickly to the next stage
    End If
End Sub

' The Timer which delays the machine to allow any bonus points to be added up
' has expired.  Check to see if there are any extra balls for this player.
' if not, then check to see if this was the last ball (of the currentplayer)
'
Sub EndOfBall2()
    ' if were tilted, reset the internal tilted flag (this will also
    ' set TiltWarnings back to zero) which is useful if we are changing player LOL
    Tilted = FALSE:DisableTable FALSE

    ' has the player won an extra-ball ? (might be multiple outstanding)
    If(ExtraBallsAwards(CurrentPlayer) <> 0)Then

        'AddDebugText "Extra Ball"
        DMD "-", CenterLine(1, ("EXTRA BALL")), "-", eNone, eBlink, eNone, 1000, TRUE, ""
        DMDU_BonusText TRUE, 16, " ", 0, 0, " ", 0, 0, 1000, ""

        ' yep got to give it to them
        ExtraBallsAwards(CurrentPlayer) = ExtraBallsAwards(CurrentPlayer)- 1

        ' if no more EB's then turn off any shoot again light
        If(ExtraBallsAwards(CurrentPlayer) = 0)Then
            LightShootAgain.State = 0
        End If

        ' You may wish to do a bit of a song AND dance at this point

        ' Create a new ball in the shooters lane
        CreateNewBallDelay = 40
    Else ' no extra balls

        BallsRemaining(CurrentPlayer) = BallsRemaining(CurrentPlayer)- 1

        ' was that the last ball ?
        If(BallsRemaining(CurrentPlayer) <= 0)Then

            'AddDebugText "No More Balls, High Score Entry"

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

    'AddDebugText "EndOfBall - Complete"

    ' are there multiple players playing this game ?
    If(PlayersPlayingGame> 1)Then
        ' then move to the next player
        NextPlayer = CurrentPlayer + 1
        ' are we going from the last player back to the first
        ' (ie say from player 4 back to player 1)
        If(NextPlayer> PlayersPlayingGame)Then
            NextPlayer = 1
        End If
    Else
        NextPlayer = CurrentPlayer
    End If

    'AddDebugText "Next Player = " & NextPlayer

    ' is it the end of the game ? (all balls been lost for all players)
    If((BallsRemaining(CurrentPlayer) <= 0)AND(BallsRemaining(NextPlayer) <= 0))Then
        ' you may wish to do some sort of Point Match free game award here
        ' generally only done when not in free play mode

        ' set the machine into game over mode
        EndOfGame()

    ' you may wish to put a Game Over message on the

    Else
        ' set the next player
        CurrentPlayer = NextPlayer

        ' make sure the correct display is upto date
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
    ' just ended your game then play the end of game tune
    If bGameInPlay Then
        StopSong
        PlaySound "z_gameover"
        DMD "", "", "!", eNone, eNone, eBlink, 3000, TRUE, ""
        DMDU_ShowGameOver
        vpmtimer.addtimer 3000, "StartAttractMode"
    Else
        StartAttractMode 1
    End If
    'AddDebugText "End Of Game"

    bGameInPLay = FALSE

    ' ensure that the flippers are down
    SolLFlipper 0
    SolRFlipper 0

    ' terminate all modes - eject locked balls
    TimerA1.Enabled = 0

    ' set any lights for the attract mode
    GiOff

' you may wish to light any Game Over Light you may have
End Sub

Function Balls
    Dim tmp
    tmp = BallsPerGame - BallsRemaining(CurrentPlayer) + 1
    If tmp> BallsPerGame Then
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
    ' Destroy the ball
    Drain.DestroyBall
    BallsOnPlayfield = BallsOnPlayfield - 1
    ' pretend to knock the ball into the ball storage mech
    PlaySound "fx_drain"

    ' if there is a game in progress AND
    If(bGameInPLay = TRUE)AND(Tilted = FALSE)Then

        ' is the ball saver active,
        If(bBallSaverActive = TRUE)Then

            ' yep, create a new ball in the shooters lane
            '   CreateNewBall
            ' we use the Addmultiball in case the multiballs are being ejected
            AddMultiball 1
        ' you may wish to put something on a display or play a sound at this point

        Else
            ' cancel any multiball if on last ball (ie. lost all other balls)
            '
            If(BallsOnPlayfield = 1)Then
                ' AND in a multi-ball??
                If(bMultiBallMode = TRUE)then
                    ' not in multiball mode any more
                    bMultiBallMode = FALSE
                    ChangeGi "Normal"
                    If DoubleScore = 2 Then 'restore the double scoring multiball
                        ResetDoubleScore
                    End If
                    ' you may wish to change any music over at this point and
                    ' turn off any multiball specific lights
                    PlaySong "m_main"
                End If
            End If

            ' was that the last ball on the playfield
            If(BallsOnPlayfield = 0)Then
                ' handle the end of ball (change player, high score entry etc..)
                EndOfBall()
                ' End Modes and timers
                ChangeGi "Normal"
                CastleDiverter.TimerEnabled = 0
            End If
        End If
    End If
End Sub

' A ball is pressing down the trigger in the shooters lane
'
Sub swPlungerRest_Hit()
    ' some sound according to the ball position
    PlaySound "fx_sensor", 0, 1, 0.15, 0.25
    bBallInPlungerLane = TRUE
    ' turn on LaunchLight
    'LaunchLight.State = 2
    ' remember last trigger hit by the ball.
    If bAutoPlunger Then
        plungerIM.AutoFire
        DOF dAutoPlunger, 2
    End If
    LastSwitchHit = "swPlungerRest"
End Sub

' The Ball has rolled out of the Plunger Lane.  Check to see if a ball saver mechanism
' is needed AND if so fire it up.
'
Sub swPlungerRest_UnHit()
    bBallInPlungerLane = FALSE
    bAutoPlunger = FALSE
    ' turn off LaunchLight
    'LaunchLight.State = 0
    ' if there is a need for a ball saver, then start off a timer
    ' only start if it is currently not running and not in multiball, else it will reset the time period
    If(bBallSaverReady = TRUE)AND(BallSaverTime <> 0)AND(bBallSaverActive <> TRUE)AND(bMultiBallMode = FALSE)Then
        ' AND only if the last trigger hit was the plunger wire.
        ' (ball in the shooters lane)
        If(LastSwitchHit = "swPlungerRest")Then
            'Start the skillshot if ready
            If bSkillShotReady Then
                SkillShotDelay = 100 '5 seconds
                LightSeqTable.Play SeqUpOn, 20
                PlaySound "z_eponaride"
            End If
            EnableBallSaver BallSaverTime
        End If
    End If
End Sub

' The ball saver timer has expired.  Turn it off AND reset the game flag
'
Sub BallSaverExpired()
    BallSaverDelay = 0 ' assure the delay counter is stopped
    ' clear the flag
    bBallSaverActive = FALSE
    ' if you have a ball saver light then turn it off at this point
    LightShootAgain.State = 0
End Sub

Sub EnableBallSaver(seconds)
    ' set our game flag
    bBallSaverActive = TRUE
    bBallSaverReady = FALSE
    ' start the timer
    BallSaverDelay = seconds * 20 'this is 1000 / Gametime interval
    ' if you have a ball saver light you might want to turn it on at this point (or make it flash)
    LightShootAgain.BlinkInterval = 125
    LightShootAgain.State = 2
End Sub

' *********************************************************************
'                      Supporting Score Functions
' *********************************************************************

' Add points to the score AND update the score board
'
Sub AddScore(points)
    If(Tilted = FALSE)Then
        ' add the points to the current players score variable
        Score(CurrentPlayer) = Score(CurrentPlayer) + points * DoubleScore 'DoubleScore only for this table.
        ' update the score displays
        DMDScore:DMDU_ShowScoreBoard
    End if

' you may wish to check to see if the player has gotten a replay
End Sub

' Add bonus to the bonuspoints AND update the score board
'
Sub AddBonus(points)
    If(Tilted = FALSE)Then
        ' add the bonus to the current players bonus variable
        BonusPoints(CurrentPlayer) = BonusPoints(CurrentPlayer) + points
        ' update the score displays
        DMDScore:DMDU_ShowScoreBoard
    End if

' you may wish to check to see if the player has gotten a replay
End Sub

' Add some points to the current Jackpot.
'
Sub AddJackpot(points)
    ' Jackpots only generally increment in multiball mode AND not tilted
    ' but this doesn't have to be the case
    If(Tilted = FALSE)Then

        If(bMultiBallMode = TRUE)Then
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
    if(BonusMultiplier(CurrentPlayer) <MaxMultiplier)then
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
    If(BonusMultiplier(CurrentPlayer) = 1)Then
        LightBonus2x.State = 0
        LightBonus3x.State = 0
        LightBonus4x.State = 0
        LightBonus5x.State = 0
        LightBonus6x.State = 0
        LightBonus7x.State = 0
    Else
        ' there is a bonus, turn on all the lights upto the current level
        If(BonusMultiplier(CurrentPlayer) >= 2)Then
            If(BonusMultiplier(CurrentPlayer) >= 2)Then
                LightBonus2x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 3)Then
                LightBonus3x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 4)Then
                LightBonus4x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 5)Then
                LightBonus5x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 6)Then
                LightBonus6x.state = 1
            End If
            If(BonusMultiplier(CurrentPlayer) >= 7)Then
                LightBonus7x.state = 1
            End If
        End If
    ' etc..
    End If
End Sub

Sub IncrementCBonus(Amount)
    Dim Value
    AddBonus Amount * 1000
    CBonus = CBonus + Amount
    If(CBonus> 20)Then
        CBonus = 20
    End If
    If(Amount >= 0)Then
    End If
    LightC1.State = 0
    LightC2.State = 0
    LightC3.State = 0
    LightC4.State = 0
    LightC5.State = 0
    LightC6.State = 0
    LightC7.State = 0
    LightC8.State = 0
    LightC9.State = 0
    LightC10.State = 0
    LightC20.State = 0
    Value = CBonus
    If(Value >= 20)Then
        LightC20.BlinkPattern = "10":LightC20.BlinkInterval = 150:LightC20.State = 2
        Value = Value - 20
        LightC1.BlinkPattern = "100000100000":LightC1.BlinkInterval = 75:LightC1.State = 2
        LightC2.BlinkPattern = "010000010000":LightC2.BlinkInterval = 75:LightC2.State = 2
        LightC3.BlinkPattern = "001000001000":LightC3.BlinkInterval = 75:LightC3.State = 2
        LightC4.BlinkPattern = "000100000100":LightC4.BlinkInterval = 75:LightC4.State = 2
        LightC5.BlinkPattern = "000010000010":LightC5.BlinkInterval = 75:LightC5.State = 2
        LightC6.BlinkPattern = "000001000001":LightC6.BlinkInterval = 75:LightC6.State = 2
        LightC7.BlinkPattern = "100000100000":LightC7.BlinkInterval = 75:LightC7.State = 2
        LightC8.BlinkPattern = "010000010000":LightC8.BlinkInterval = 75:LightC8.State = 2
        LightC9.BlinkPattern = "001000001000":LightC9.BlinkInterval = 75:LightC9.State = 2
        LightC10.BlinkPattern = "000100000100":LightC10.BlinkInterval = 75:LightC10.State = 2
    End If
    If(Value >= 10)Then
        LightC10.State = 1
        Value = Value - 10
    End If
    if(Value >= 9)Then LightC9.State = 1 End If
    if(Value >= 8)Then LightC8.State = 1 End If
    if(Value >= 7)Then LightC7.State = 1 End If
    if(Value >= 6)Then LightC6.State = 1 End If
    if(Value >= 5)Then LightC5.State = 1 End If
    if(Value >= 4)Then LightC4.State = 1 End If
    if(Value >= 3)Then LightC3.State = 1 End If
    if(Value >= 2)Then LightC2.State = 1 End If
    if(Value >= 1)Then LightC1.State = 1 End If
End Sub

Sub AwardExtraBall()
    If NOT bExtraBallWonThisBall Then
        DMD "-", Centerline(1, ("EXTRA BALL WON")), "-", eNone, eBlink, eNone, 1000, TRUE, "fx_Knocker"
        DMDU_BonusText TRUE, 0, "YOU WON AN", 6, 15, "EXTRA BALL", 6, 15, 1000, "fx_Knocker"
        ExtraBallsAwards(CurrentPlayer) = ExtraBallsAwards(CurrentPlayer) + 1
        bExtraBallWonThisBall = TRUE
    END If
End Sub

'*****************************
'    Load / Save / Highscore
'*****************************

Sub Loadhs
    Dim x
    x = LoadValue(TableName, "HighScore1")
    If(x <> "")Then HighScore(0) = CDbl(x)Else HighScore(0) = 28000000 End If
    x = LoadValue(TableName, "HighScore1Name")
    If(x <> "")Then HighScoreName(0) = x Else HighScoreName(0) = "CAT" End If
    x = LoadValue(TableName, "HighScore2")
    If(x <> "")then HighScore(1) = CDbl(x)Else HighScore(1) = 27000000 End If
    x = LoadValue(TableName, "HighScore2Name")
    If(x <> "")then HighScoreName(1) = x Else HighScoreName(1) = "FED" End If
    x = LoadValue(TableName, "HighScore3")
    If(x <> "")then HighScore(2) = CDbl(x)Else HighScore(2) = 26000000 End If
    x = LoadValue(TableName, "HighScore3Name")
    If(x <> "")then HighScoreName(2) = x Else HighScoreName(2) = "JPS" End If
    x = LoadValue(TableName, "HighScore4")
    If(x <> "")then HighScore(3) = CDbl(x)Else HighScore(3) = 25000000 End If
    x = LoadValue(TableName, "HighScore4Name")
    If(x <> "")then HighScoreName(3) = x Else HighScoreName(3) = "JOE" End If
    x = LoadValue(TableName, "Credits")
    If(x <> "")then Credits = CInt(x)Else Credits = 0 End If
    x = LoadValue(TableName, "Jackpot")
    If(x <> "")then Jackpot = CInt(x)Else Jackpot = 0 End If
    x = LoadValue(TableName, "TotalGamesPlayed")
    If(x <> "")then TotalGamesPlayed = CInt(x)Else TotalGamesPlayed = 0 End If
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
    SaveValue TableName, "Jackpot", Jackpot
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
Dim sLastBest

Sub CheckHighscore()
    Dim tmp
    tmp = Score(1)
    If Score(2)> tmp Then tmp = Score(2)
    If Score(3)> tmp Then tmp = Score(3)
    If Score(4)> tmp Then tmp = Score(4)

    If tmp> HighScore(1)Then 'add 1 credit for beating the highscore
        Credits = Credits + 1
    End If

    If tmp> HighScore(3)Then
        PlaySound SoundFX("fx_knocker",DOFKnocker)
        HighScore(3) = tmp
        'enter player's name
        HighScoreEntryInit()
    Else
        EndOfBallComplete()
    End If
End Sub

Sub HighScoreEntryInit()
    hsbModeActive = TRUE
    hsLetterFlash = 0

    hsEnteredDigits(0) = " "
    hsEnteredDigits(1) = " "
    hsEnteredDigits(2) = " "
    hsCurrentDigit = 0

    hsValidLetters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ'<>*+-/=\^0123456789`" ' ` is back arrow
    hsCurrentLetter = 1
    DMDFlush:DMDU_QueueSceneWithId "hsc", FALSE, 8, "YOUR NAME:", 6, 15, " ", 6, 15, dfadein, 999999, dnone
    HighScoreDisplayNameNow()

    HighScoreFlashTimer.Interval = 250
    HighScoreFlashTimer.Enabled = TRUE
End Sub

Sub EnterHighScoreKey(keycode)
    If keycode = LeftFlipperKey Then
        playsound "fx_Previous"
        hsCurrentLetter = hsCurrentLetter - 1
        if(hsCurrentLetter = 0)then
            hsCurrentLetter = len(hsValidLetters)
        end if
        HighScoreDisplayNameNow()
    End If

    If keycode = RightFlipperKey Then
        playsound "fx_Next"
        hsCurrentLetter = hsCurrentLetter + 1
        if(hsCurrentLetter> len(hsValidLetters))then
            hsCurrentLetter = 1
        end if
        HighScoreDisplayNameNow()
    End If

    If keycode = PlungerKey Then
        if(mid(hsValidLetters, hsCurrentLetter, 1) <> "`")then
            playsound "fx_Enter"
            hsEnteredDigits(hsCurrentDigit) = mid(hsValidLetters, hsCurrentLetter, 1)
            hsCurrentDigit = hsCurrentDigit + 1
            if(hsCurrentDigit = 3)then
                HighScoreCommitName()
            else
                HighScoreDisplayNameNow()
            end if
        else
            playsound "fx_Esc"
            hsEnteredDigits(hsCurrentDigit) = " "
            if(hsCurrentDigit> 0)then
                hsCurrentDigit = hsCurrentDigit - 1
            end if
            HighScoreDisplayNameNow()
        end if
    end if
End Sub

Sub HighScoreDisplayNameNow()
    HighScoreFlashTimer.Enabled = FALSE
    hsLetterFlash = 0
    HighScoreDisplayName()
    HighScoreFlashTimer.Enabled = TRUE
End Sub

Sub HighScoreDisplayName()
    Dim i
    Dim TempTopStr
    Dim TempBotStr

    TempTopStr = "YOUR NAME:"
    dLine(0) = ExpandLine(TempTopStr, 0)
    DMDUpdate 0

    TempBotStr = "    > "
    if(hsCurrentDigit> 0)then TempBotStr = TempBotStr & hsEnteredDigits(0)
    if(hsCurrentDigit> 1)then TempBotStr = TempBotStr & hsEnteredDigits(1)
    if(hsCurrentDigit> 2)then TempBotStr = TempBotStr & hsEnteredDigits(2)

    if(hsCurrentDigit <> 3)then
        if(hsLetterFlash <> 0)then
            TempBotStr = TempBotStr & "_"
        else
            TempBotStr = TempBotStr & mid(hsValidLetters, hsCurrentLetter, 1)
        end if
    end if

    if(hsCurrentDigit <1)then TempBotStr = TempBotStr & hsEnteredDigits(1)
    if(hsCurrentDigit <2)then TempBotStr = TempBotStr & hsEnteredDigits(2)

    TempBotStr = TempBotStr & " <    "
    dLine(1) = ExpandLine(TempBotStr, 1)
    DMDUpdate 1
    TempBotStr = Replace(TempBotStr, "_", ".")
    DMDU_ModifyScene "hsc", "YOUR NAME:", Mid(TempBotStr, 5, 7), 999999
End Sub

Sub HighScoreFlashTimer_Timer()
    HighScoreFlashTimer.Enabled = FALSE
    hsLetterFlash = hsLetterFlash + 1
    if(hsLetterFlash = 2)then hsLetterFlash = 0
    HighScoreDisplayName()
    HighScoreFlashTimer.Enabled = TRUE
End Sub

Sub HighScoreCommitName()
    HighScoreFlashTimer.Enabled = FALSE
    hsbModeActive = FALSE
    hsEnteredName = hsEnteredDigits(0) & hsEnteredDigits(1) & hsEnteredDigits(2)
    if(hsEnteredName = "   ")then
        hsEnteredName = "JPS"
    end if

    HighScoreName(3) = hsEnteredName
    SortHighscore
    EndOfBallComplete()
End Sub

Sub SortHighscore
    Dim tmp, tmp2, i, j
    For i = 0 to 3
        For j = 0 to 2
            If HighScore(j) <HighScore(j + 1)Then
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
' DMD "text1","text2","!", eNone, eNone, eNone, 250, TRUE, "sound"
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

Dim ReelDMDActive:ReelDMDActive = False
Sub DMDInit() 'default/startup values
    Dim i, j
    DMDFlush()
    deSpeed = 20
    deBlinkSlowRate = 5
    deBlinkFastRate = 2
    dCharsPerLine(0) = 10
    dCharsPerLine(1) = 20
    dCharsPerLine(2) = 1
    For i = 0 to 2
        dLine(i) = Space(dCharsPerLine(i))
        deCount(i) = 0
        deCountEnd(i) = 0
        deBlinkCycle(i) = 0
        dqTimeOn(i) = 0
        dqbFlush(i) = TRUE
        dqSound(i) = ""
    Next
    For i = 0 to 2
        For j = 0 to 64
            dqText(i, j) = ""
            dqEffect(i, j) = eNone
        Next
    Next
    If((UseReelsDMD = True)OR(IsObject(UltraDMD) = False))Then
        ReelDMDActive = True
        For each i in aReels:i.Visible = True:Next
        DMDFlush
        DMD dLine(0), dLine(1), dLine(2), eNone, eNone, eNone, 25, TRUE, ""
	Else
        ReelDMDActive = False
        For each i in aReels:i.Visible = False:Next
    End If
End Sub

Sub DMDFlush()
    Dim i
    If ReelDMDActive = False Then Exit Sub
    DMDTimer.Enabled = FALSE
    DMDEffectTimer.Enabled = FALSE
    dqHead = 0
    dqTail = 0
    For i = 0 to 2
        deCount(i) = 0
        deCountEnd(i) = 0
        deBlinkCycle(i) = 0
    Next

    'clear the backdrop too
    EMReel30.SetValue 0
End Sub

Sub DMDScoreNow()
    DMDFlush:DMDU_Flush
DMDScore:DMDU_ShowScoreBoard
End Sub

Sub DMDScore()
    Dim tmp, tmp1
    if(dqHead = dqTail)Then
        tmp = CenterLine(0, FormatScore(Score(Currentplayer)))
        tmp1 = CenterLine(1, "PLAYER " & CurrentPlayer & " BALL " & Balls)
        'tmp1 = FormatScore(Bonuspoints(Currentplayer) ) & " X" &BonusMultiplier(Currentplayer)
        DMD tmp, tmp1, "-", eNone, eNone, eNone, 25, TRUE, ""
    End If
End Sub

Sub DMD(Text0, Text1, Text2, Effect0, Effect1, Effect2, TimeOn, bFlush, Sound)
    If ReelDMDActive = False Then Exit Sub
    if(dqTail <dqSize)Then
        if(Text0 = "-")Then
            dqEffect(0, dqTail) = eNone
            dqText(0, dqTail) = "-"
        Else
            dqEffect(0, dqTail) = Effect0
            dqText(0, dqTail) = ExpandLine(Text0, 0)
        End If

        if(Text1 = "-")Then
            dqEffect(1, dqTail) = eNone
            dqText(1, dqTail) = "-"
        Else
            dqEffect(1, dqTail) = Effect1
            dqText(1, dqTail) = ExpandLine(Text1, 1)
        End If

        if(Text2 = "-")Then
            dqEffect(2, dqTail) = eNone
            dqText(2, dqTail) = "-"
        Else
            dqEffect(2, dqTail) = Effect2
            dqText(2, dqTail) = Text2 'it is always 1 letter
        End If

        dqTimeOn(dqTail) = TimeOn
        dqbFlush(dqTail) = bFlush
        dqSound(dqTail) = Sound
        dqTail = dqTail + 1
        if(dqTail = 1)Then
            DMDHead()
        End If
    End If
End Sub

Sub DMDHead()
    If ReelDMDActive = False Then Exit Sub
    Dim i
    deCount(0) = 0
    deCount(1) = 0
    deCount(2) = 0
    DMDEffectTimer.Interval = deSpeed

    For i = 0 to 2
        Select Case dqEffect(i, dqHead)
            Case eNone:deCountEnd(i) = 1
            Case eScrollLeft:deCountEnd(i) = Len(dqText(i, dqHead))
            Case eScrollRight:deCountEnd(i) = Len(dqText(i, dqHead))
            Case eBlink:deCountEnd(i) = int(dqTimeOn(dqHead) / deSpeed)
                deBlinkCycle(i) = 0
            Case eBlinkFast:deCountEnd(i) = int(dqTimeOn(dqHead) / deSpeed)
                deBlinkCycle(i) = 0
        End Select
    Next
    if(dqSound(dqHead) <> "")Then
        PlaySound(dqSound(dqHead))
    End If
    DMDEffectTimer.Enabled = TRUE
End Sub

Sub DMDEffectTimer_Timer()
    DMDEffectTimer.Enabled = FALSE
    DMDProcessEffectOn()
End Sub

Sub DMDTimer_Timer()
    Dim Head
    DMDTimer.Enabled = FALSE
    Head = dqHead
    dqHead = dqHead + 1
    if(dqHead = dqTail)Then
        if(dqbFlush(Head) = TRUE)Then
            DMDFlush()
        'DMDScore()
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

    BlinkEffect = FALSE

    For i = 0 to 2
        if(deCount(i) <> deCountEnd(i))Then
            deCount(i) = deCount(i) + 1

            select case(dqEffect(i, dqHead))
                case eNone:
                    Temp = dqText(i, dqHead)
                case eScrollLeft:
                    Temp = Right(dLine(i), dCharsPerLine(i)- 1)
                    Temp = Temp & Mid(dqText(i, dqHead), deCount(i), 1)
                case eScrollRight:
                    Temp = Mid(dqText(i, dqHead), (dCharsPerLine(i) + 1)- deCount(i), 1)
                    Temp = Temp & Left(dLine(i), dCharsPerLine(i)- 1)
                case eBlink:
                    BlinkEffect = TRUE
                    if((deCount(i)MOD deBlinkSlowRate) = 0)Then
                        deBlinkCycle(i) = deBlinkCycle(i)xor 1
                    End If

                    if(deBlinkCycle(i) = 0)Then
                        Temp = dqText(i, dqHead)
                    Else
                        Temp = Space(dCharsPerLine(i))
                    End If
                case eBlinkFast:
                    BlinkEffect = TRUE
                    if((deCount(i)MOD deBlinkFastRate) = 0)Then
                        deBlinkCycle(i) = deBlinkCycle(i)xor 1
                    End If

                    if(deBlinkCycle(i) = 0)Then
                        Temp = dqText(i, dqHead)
                    Else
                        Temp = Space(dCharsPerLine(i))
                    End If
            End Select

            if(dqText(i, dqHead) <> "-")Then
                dLine(i) = Temp
                DMDUpdate i
            End If
        End If
    Next

    if(deCount(0) = deCountEnd(0))and(deCount(1) = deCountEnd(1))and(deCount(2) = deCountEnd(2))Then

        if(dqTimeOn(dqHead) = 0)Then
            DMDFlush()
        Else
            if(BlinkEffect = TRUE)Then
                DMDTimer.Interval = 10
            Else
                DMDTimer.Interval = dqTimeOn(dqHead)
            End If

            DMDTimer.Enabled = TRUE
        End If
    Else
        DMDEffectTimer.Enabled = TRUE
    End If
End Sub

Function ExpandLine(TempStr, id) 'id is the number of the dmd line
    If TempStr = "" Then
        TempStr = Space(dCharsPerLine(id))
    Else
        if(Len(TempStr)> Space(dCharsPerLine(id)))Then
            TempStr = Left(TempStr, Space(dCharsPerLine(id)))
        Else
            if(Len(TempStr) <dCharsPerLine(id))Then
                TempStr = TempStr & Space(dCharsPerLine(id)- Len(TempStr))
            End If
        End If
    End If
    ExpandLine = TempStr
End Function

Function FormatScore(ByVal Num) 'it returns a string with commas (as in Black's original font)
    dim i
    dim NumString

    NumString = CStr(abs(Num))

    For i = Len(NumString)-3 to 1 step -3
        if IsNumeric(mid(NumString, i, 1))then
            NumString = left(NumString, i-1) & chr(asc(mid(NumString, i, 1)) + 48) & right(NumString, Len(NumString)- i)
        end if
    Next
    FormatScore = NumString
End function

Function CenterLine(id, NumString)
    Dim Temp, TempStr
    Temp = (dCharsPerLine(id)- Len(NumString)) \ 2
    If(Temp + Temp + Len(NumString)) <dCharsPerLine(id)Then
        TempStr = " " & Space(Temp) & NumString & Space(Temp)
    Else
        TempStr = Space(Temp) & NumString & Space(Temp)
    End If
    CenterLine = TempStr
End Function

'*********************
' Update DMD - reels
'*********************

Dim Digits(2)

Digits(0) = Array(EMReel0, EMReel1, EMReel2, EMReel3, EMReel4, EMReel5, EMReel6, EMReel7, EMReel8, EMReel9)
Digits(1) = Array(EMReel10, EMReel11, EMReel12, EMReel13, EMReel14, EMReel15, EMReel16, EMReel17, EMReel18, EMReel19, EMReel20, EMReel21, EMReel22, EMReel23, EMReel24, EMReel25, EMReel26, EMReel27, EMReel28, EMReel29)
Digits(2) = Array(EMReel30)

Sub DMDUpdate(id)
    Dim digit, value
    If id <2 Then 'text reels
        For digit = 0 to dCharsPerLine(id)-1
            value = ASC(mid(dLine(id), digit + 1, 1))-32
            Digits(id)(digit).SetValue value
        Next
    Else 'backdrop reel for animatons
        If dLine(2) = "" Then
            value = 0
            Digits(2)(0).SetValue value
        Else
            value = ASC(dLine(2))-32
            Digits(2)(0).SetValue value
        End If
    End If
End Sub

' ********************************
'   Table info & Attract Mode
' ********************************

Sub ShowTableInfo
    If ReelDMDActive = False Then Exit Sub
    'info goes in a loop only stopped by the credits and the startkey

    DMD "", "", "!", eNone, eNone, eBlink, 2000, FALSE, ""
    If Credits> 0 Then
        DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "PRESS START"), "", eNone, eBlink, eNone, 2000, FALSE, ""
    Else
        DMD CenterLine(0, "CREDITS " & Credits), CenterLine(1, "INSERT COIN"), "", eNone, eBlink, eNone, 2000, FALSE, ""
    End If
    DMD "", "", "#", eNone, eNone, eNone, 3000, FALSE, ""
    DMD "", "", "$", eNone, eNone, eNone, 4000, FALSE, ""
    DMD "", "", "%", eNone, eNone, eNone, 3000, FALSE, ""
    DMD "", "", "&", eNone, eNone, eNone, 3000, FALSE, ""
    DMD "", CenterLine(1, "THE LEGEND OF..."), "", eNone, eScrollLeft, eNone, 2000, FALSE, ""
    DMD CenterLine(0, "ZELDA "), CenterLine(1, "ROM VERS." &myVersion), "", eScrollRight, eScrollLeft, eNone, 2500, FALSE, ""
    DMD Space(dCharsPerLine(0)), Space(dCharsPerLine(1)), "", eScrollLeft, eScrollLeft, eNone, 20, FALSE, ""
    DMD "HIGHSCORES", Space(dCharsPerLine(1)), "", eScrollLeft, eScrollLeft, eNone, 20, FALSE, ""
    DMD "HIGHSCORES", "", "", eBlinkFast, eNone, eNone, 1000, FALSE, ""
    DMD "HIGHSCORES", "1> " &HighScoreName(0) & " " &FormatScore(HighScore(0)), "", eNone, eScrollLeft, eNone, 2000, FALSE, ""
    DMD "-", "2> " &HighScoreName(1) & " " &FormatScore(HighScore(1)), "", eNone, eScrollLeft, eNone, 2000, FALSE, ""
    DMD "HIGHSCORES", "3> " &HighScoreName(2) & " " &FormatScore(HighScore(2)), "", eNone, eScrollLeft, eNone, 2000, FALSE, ""
    DMD "HIGHSCORES", "4> " &HighScoreName(3) & " " &FormatScore(HighScore(3)), "", eNone, eScrollLeft, eNone, 2000, FALSE, ""
    DMD Space(dCharsPerLine(0)), Space(dCharsPerLine(1)), "", eScrollLeft, eScrollLeft, eNone, 500, FALSE, ""
End Sub

Sub StartAttractMode(dummy)
    PlaySong "m_attract"
    StartLightSeq
DMDFlush:ShowTableInfo
    AttractMode = True:DMDU_ShowIntro
End Sub

Sub StopAttractMode
    DMDFlush
    AttractMode = False
    If IsObject(UltraDMD)Then
        DMDU_Flush:UltraDMD.SetScoreboardBackgroundImage Zelda_Images(20), 15, 15
    End If
    LightSeqAttract.StopPlay
    LightSeqFlasher.StopPlay
    StopSong
End Sub

Sub StartLightSeq()
    Real_StartLightSeq()
End Sub

Sub Real_StartLightSeq()
    'flasher sequences
    LightSeqFlasher.Play SeqBlinking, , 5, 90
    LightSeqFlasher.Play SeqBlinking, , 5, 80
    LightSeqFlasher.Play SeqBlinking, , 6, 70
    LightSeqFlasher.Play SeqBlinking, , 6, 60
    LightSeqFlasher.Play SeqBlinking, , 7, 50
    LightSeqFlasher.Play SeqBlinking, , 8, 40
    LightSeqFlasher.Play SeqBlinking, , 8, 30
    LightSeqFlasher.Play SeqRandom, 4, , 4000
    LightSeqFlasher.Play SeqRightOn, 50, 1
    LightSeqFlasher.Play SeqLeftOn, 50, 1
    LightSeqFlasher.Play SeqRightOn, 50, 1
    LightSeqFlasher.Play SeqLeftOn, 50, 1
    LightSeqFlasher.Play SeqRightOn, 50, 1
    LightSeqFlasher.Play SeqLeftOn, 50, 1
    LightSeqFlasher.Play SeqBlinking, , 9, 30
    LightSeqFlasher.Play SeqBlinking, , 8, 40
    LightSeqFlasher.Play SeqBlinking, , 7, 50
    LightSeqFlasher.Play SeqBlinking, , 6, 60
    LightSeqFlasher.Play SeqBlinking, , 6, 70
    LightSeqFlasher.Play SeqBlinking, , 5, 80
    LightSeqFlasher.Play SeqBlinking, , 5, 90
    LightSeqFlasher.Play SeqBlinking, , 5, 100
    LightSeqFlasher.Play SeqRandom, 4, , 4000
    LightSeqFlasher.Play SeqRightOn, 50, 1
    LightSeqFlasher.Play SeqLeftOn, 50, 1
    LightSeqFlasher.Play SeqRightOn, 50, 1
    LightSeqFlasher.Play SeqLeftOn, 50, 1
    LightSeqFlasher.Play SeqRightOn, 50, 1
    LightSeqFlasher.Play SeqLeftOn, 50, 1
    LightSeqFlasher.Play SeqBlinking, , 5, 90
    LightSeqFlasher.Play SeqBlinking, , 5, 80
    LightSeqFlasher.Play SeqBlinking, , 5, 70
    LightSeqFlasher.Play SeqBlinking, , 5, 60
    LightSeqFlasher.Play SeqBlinking, , 5, 50
    LightSeqFlasher.Play SeqBlinking, , 5, 40
    LightSeqFlasher.Play SeqBlinking, , 5, 30
    LightSeqFlasher.Play SeqBlinking, , 15, 20
    LightSeqFlasher.Play SeqBlinking, , 5, 30
    LightSeqFlasher.Play SeqBlinking, , 5, 40
    LightSeqFlasher.Play SeqBlinking, , 5, 50
    LightSeqFlasher.Play SeqBlinking, , 5, 60
    LightSeqFlasher.Play SeqBlinking, , 5, 70
    LightSeqFlasher.Play SeqBlinking, , 5, 80
    LightSeqFlasher.Play SeqBlinking, , 5, 90
    LightSeqFlasher.Play SeqBlinking, , 5, 200
    QueueFlasherAnimation(FlAn1)

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

Sub LightSeqSkillshot_PlayDone()
    LightSeqSkillshot.Play SeqAllOff
End Sub

Sub LightSeqTilt_PlayDone()
    LightSeqTilt.Play SeqAllOff
End Sub

Sub LightSeqDouble_PlayDone()
    LightSeqDouble.Play SeqRandom, 10, , 4000
End Sub

' *********************************************************************
'                     Table Specific Script Starts Here
' *********************************************************************

' tables walls and animations
Sub VPObjects_Init

End Sub

' tables variables and modes init

Dim Mode

Sub zelda_Init()
    bExtraBallWonThisBall = FALSE
    TurnOffPlayfieldLights()
    ChangeGi "Normal"
    'PlayMusicForMode(1)

    DropA1.IsDropped = 1
    DropA2.IsDropped = 1

    '	TimerG1.Set TRUE, 200
    Popup1.Isdropped = 0
    Popup2.Isdropped = 0
    Popup3.Isdropped = 0

    LightM1.State = 0

    P2Count = 1
    P4Count = 1
    K1Count = 1
    K2Count = 1
    cbCount = 1
    K6Count = 1
    NCount = 1
    R1Count = 1
    R2Count = 1
    TargetR1.IsDropped = 0
    TargetR2.IsDropped = 0
    TargetR3.IsDropped = 0
    EndLights
    TargetG1.IsDropped = 1
    TimerA1.Enabled = 1 'droptargets animation
    CastleDiverter.TimerEnabled = 0
'InitDelays
'SkillshotInit
'MainModesInit()
End Sub

Sub ResetSkillShot
    bSkillshotReady = FALSE
End Sub

Sub TurnOffPlayfieldLights()
    LightShootAgain.State = 0

    LightBonus2x.State = 0
    LightBonus3x.State = 0
    LightBonus4x.State = 0
    LightBonus5x.State = 0
    LightBonus6x.State = 0
    LightBonus7x.State = 0

    LightLeftInlane1.State = 1
    LightLeftOutlane.State = 0
    LightRightOutlane.State = 0
    LightLeftInlane.State = 0
    LightRightInlane.State = 0

    LightC1.State = 0
    LightC2.State = 0
    LightC3.State = 0
    LightC4.State = 0
    LightC5.State = 0
    LightC6.State = 0
    LightC7.State = 0
    LightC8.State = 0
    LightC9.State = 0
    LightC10.State = 0
    LightC20.State = 0
End Sub

Sub EndLights()
    LightSD1.State = 0
    LightSD2.State = 0
    LightSD3.State = 0
    LightSD4.State = 0
    LightSD5.State = 0
    LightSD6.State = 0

    LightMult1.State = 0
    LightMult2.State = 0
    LightMult3.State = 0
    LightMult4.State = 0

    LightG1.State = 0

    LightR1.State = 0
    LightR2.State = 0
    LightR3.State = 0

    LightN1.State = 0
    LightN2.State = 0

    LightExtra2.State = 0
    LightPC4.State = 0

    LightExtra3.State = 0
    LightJackpot1.State = 0

    LightPD1.State = 0
    LightPD2.State = 0
    LightPD3.State = 0
    LightPD4.State = 0

    LightPC1.State = 0
    LightPC2.State = 0
    LightPC3.State = 0

    LightMystery1.State = 0
    LightM1.State = 0
    LightSpinner1.State = 0

    LightK5A.State = 0
    LightK5B.State = 0
    LightK5C.State = 0
    LightSpecial1.State = 0
    LightExtra1.State = 0

    LightV1.State = 0
    LightV2.State = 0
    LightV3.State = 0
    LightV4.State = 0
    LightV5.State = 0
    LightV6.State = 0
    LightV7.State = 0

    LightBonus2x.State = 0
    LightBonus3x.State = 0
    LightBonus4x.State = 0
    LightBonus5x.State = 0
    LightBonus6x.State = 0
    LightBonus7x.State = 0

    LightA1.State = 0
    LightA2.State = 0
    LightA3.State = 0
    LightA4.State = 0
    LightA5.State = 0

    LightP1.State = 0
    LightP2.State = 0
    LightP3.State = 0
    LightP4.State = 0
End Sub

' *********************************************************************
'                        Table Object Hit Events
'
' Any target hit sub will follow this:
' - play a sound
' - do some physical movement
' - add a score, bonus
' - check some variables/modes this trigger is a member of
' - set the "LastSwicthHit" variable in case it is needed later
' *********************************************************************

' Slingshots has been hit

Dim LStep, RStep

Sub LeftSlingShot_Slingshot
    If NOT Tilted Then
        PlaySound SoundFX("fx_slingshot",DOFContactors), 0, 1, -0.05, 0.05
        LeftSling4.Visible = 1
        Lemk.RotX = 26
        LStep = 0
        LeftSlingShot.TimerEnabled = 1
        ' add some points
        AddScore 150
        ' remember last trigger hit by the ball
        LastSwitchHit = "LeftSlingShot"
		DOF dSlingLeft, 2
    End If
End Sub

Sub LeftSlingShot_Timer
    Select Case LStep
        Case 1:LeftSLing4.Visible = 0:LeftSLing3.Visible = 1:Lemk.RotX = 14
        Case 2:LeftSLing3.Visible = 0:LeftSLing2.Visible = 1:Lemk.RotX = 2
        Case 3:LeftSLing2.Visible = 0:Lemk.RotX = -10:LeftSlingShot.TimerEnabled = 0
    End Select
    LStep = LStep + 1
End Sub

Sub RightSlingShot_Slingshot
    If Not Tilted Then
        PlaySound SoundFX("fx_slingshot",DOFContactors), 0, 1, 0.05, 0.05
        RightSling4.Visible = 1
        Remk.RotX = 26
        RStep = 0
        RightSlingShot.TimerEnabled = 1
        ' add some points
        AddScore 150
        ' remember last trigger hit by the ball
        LastSwitchHit = "RightSlingShot"
        DOF dSlingRight, 2
    End If
End Sub

Sub RightSlingShot_Timer
    Select Case RStep
        Case 1:RightSLing4.Visible = 0:RightSLing3.Visible = 1:Remk.RotX = 14
        Case 2:RightSLing3.Visible = 0:RightSLing2.Visible = 1:Remk.RotX = 2
        Case 3:RightSLing2.Visible = 0:Remk.RotX = -10:RightSlingShot.TimerEnabled = 0
    End Select
    RStep = RStep + 1
End Sub

'*********************
' Inlanes - Outlanes
'*********************

Sub TriggerLeftInLane_Hit()
    If Not Tilted Then
        AddScore 1000
        IncrementCBonus 1
        LightLeftInlane.State = 1
    End If
    LastSwitchHit = "TriggerLeftInLane"
    PlaySound "z_Whip1"
    DOF dLeftInlane, 2
    CheckTriggerLights()
End Sub

Sub TriggerLeftInLane1_Hit()
    If Not Tilted Then
        AddScore 1000
        IncrementCBonus 1
        LightLeftInlane1.State = 1
    End If
    LastSwitchHit = "TriggerLeftInLane1"
    PlaySound "z_Whip2"
    DOF dLeftInlane1, 2
    CheckTriggerLights()
End Sub

Sub TriggerLeftOutLane_Hit()
    If Not Tilted Then
        AddScore 2000
        IncrementCBonus 1
        LightLeftOutlane.State = 1
        If(LightSaveBall.State = 1)Then
            'blink the light and turn it off
            PlungerIM2.AutoFire
            DOF dAutoPlunger2, 2
        End if
    End If
    LastSwitchHit = "TriggerLeftOutLane"
    PlaySound "z_fall1"
    DOF dLeftOutlane, 2
    CheckTriggerLights()
End Sub

Sub TriggerRightInLane_Hit()
    If Not Tilted Then
        AddScore 1000
        IncrementCBonus 1
        LightRightInlane.State = 1
    End If
    LastSwitchHit = "TriggerRightInLane"
    PlaySound "z_Whip2"
    DOF dRightInlane, 2
    CheckTriggerLights()
End Sub

Sub TriggerRightOutLane_Hit()
    If Not Tilted Then
        AddScore 2000
        IncrementCBonus 1
        LightRightOutlane.State = 1
    End If
    LastSwitchHit = "TriggerRightOutLane"
    PlaySound "z_fall2"
    PlaySound "z_ganonlaugh"
    DOF dRightOutlane, 2
    CheckTriggerLights()
End Sub

Sub CheckTriggerLights()
    DMD "-", CenterLine(1, FormatScore(Bonuspoints(Currentplayer)) & " X" &BonusMultiplier(Currentplayer)), "-", eNone, eNone, eNone, 500, TRUE, ""
    DMDU_BonusText TRUE, 0, "BONUS:", 6, 15, BonusPoints(CurrentPlayer) & " X" & BonusMultiplier(CurrentPlayer), 6, 15, 500, ""
    If(LightRightInlane.State = 1)And(LightLeftInlane.State = 1)And(LightLeftInlane1.State = 1)And(LightRightOutlane.State = 1)And(LightLeftOutlane.State = 1)Then
        AddScore 10000
        LightSeqLanes.Play SeqRandom, 4, , 3000
        LightSeqFlasher.Play SeqRandom, 4, , 3000
        QueueFlasherAnimation(FlAn2)
        IncrementBonusMultiplier()
        CheckTextoMult()
        LightRightInlane.State = 0
        LightLeftInlane.State = 0
        LightLeftInlane1.State = 0
        LightRightOutlane.State = 0
        LightLeftOutlane.State = 0
        PlaySound "z_fall2"
    End If
    DMDU_ShowScoreBoard
End Sub

Sub CheckTextoMult()
    If(LightBonus2x.State = 1)And(LightBonus3x.State = 0)And(LightBonus4x.State = 0)And(LightBonus5x.State = 0)And(LightBonus6x.State = 0)And(LightBonus7x.State = 0)Then
        DMD "-", CenterLine(1, "* * * 2 X * * *"), "", eNone, eScrollLeft, eNone, 1000, FALSE, ""
        DMD "-", "SAVE PRINCESS ZELDA", "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 0, "2 X PRINCESS", 6, 15, "ZELDA SAVED", 6, 15, 1000, ""
    End If
    If(LightBonus2x.State = 1)And(LightBonus3x.State = 1)And(LightBonus4x.State = 0)And(LightBonus5x.State = 0)And(LightBonus6x.State = 0)And(LightBonus7x.State = 0)Then
        DMD "-", CenterLine(1, "* * * 3 X * * *"), "", eNone, eScrollLeft, eNone, 1000, FALSE, ""
        DMD "-", "SAVE PRINCESS ZELDA", "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 0, "3 X PRINCESS", 6, 15, "ZELDA SAVED", 6, 15, 1000, ""
    End If
    If(LightBonus2x.State = 1)And(LightBonus3x.State = 1)And(LightBonus4x.State = 1)And(LightBonus5x.State = 0)And(LightBonus6x.State = 0)And(LightBonus7x.State = 0)Then
        DMD "-", CenterLine(1, "* * * 4 X * * *"), "", eNone, eScrollLeft, eNone, 1000, FALSE, ""
        DMD "-", "SAVE PRINCESS ZELDA", "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 0, "4 X PRINCESS", 6, 15, "ZELDA SAVED", 6, 15, 1000, ""
    End If
    If(LightBonus2x.State = 1)And(LightBonus3x.State = 1)And(LightBonus4x.State = 1)And(LightBonus5x.State = 1)And(LightBonus6x.State = 0)And(LightBonus7x.State = 0)Then
        DMD "-", CenterLine(1, "* * * 5 X * * *"), "", eNone, eScrollLeft, eNone, 1000, FALSE, ""
        DMD "-", "SAVE PRINCESS ZELDA", "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 0, "5 X PRINCESS", 6, 15, "ZELDA SAVED", 6, 15, 1000, ""
    End If
    If(LightBonus2x.State = 1)And(LightBonus3x.State = 1)And(LightBonus4x.State = 1)And(LightBonus5x.State = 1)And(LightBonus6x.State = 1)And(LightBonus7x.State = 0)Then
        DMD "-", CenterLine(1, "* * * 6 X * * *"), "", eNone, eScrollLeft, eNone, 1000, FALSE, ""
        DMD "-", "SAVE PRINCESS ZELDA", "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 0, "6 X PRINCESS", 6, 15, "ZELDA SAVED", 6, 15, 1000, ""
    End If
    If(LightBonus2x.State = 1)And(LightBonus3x.State = 1)And(LightBonus4x.State = 1)And(LightBonus5x.State = 1)And(LightBonus6x.State = 1)And(LightBonus7x.State = 1)Then
        DMD "-", CenterLine(1, "* * * 7 X * * *"), "", eNone, eScrollLeft, eNone, 1000, FALSE, ""
        DMD "-", "SAVE PRINCESS ZELDA", "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 0, "7 X PRINCESS", 6, 15, "ZELDA SAVED", 6, 15, 1000, ""
    End If
End Sub

'***********
' A targets
'***********

Sub DropA1_Hit()
    If Not Tilted Then
        PlaySound SoundFX("fx_droptarget",DOFDropTargets), 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        DropA1.IsDropped = 1
        DOF dDropTargetA1, 2
        Addscore 1000
        PlaySound "z_DekuScrub"
        If(LightJackpot1.State = 2)Then
            LightJackpot1.State = 1
        End If
        If(LightExtra3.State = 2)Then
            LightExtra3.State = 1
        End If
    End If
End Sub

Sub DropA2_Hit()
    If Not Tilted Then
        PlaySound SoundFX("fx_droptarget",DOFDropTargets), 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        DropA2.IsDropped = 1
        DOF dDropTargetA1, 2
        Addscore 1000
        If(LightJackpot1.State = 1)Then
            DMD "", "", """", eNone, eNone, eBlinkFast, 1000, FALSE, "z_BigDekuBaba"
            DMD "", CenterLine(1, FormatScore("100000")), "", eBlinkFast, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_FlashImage TRUE, 19, 1000
            DMDU_BonusText FALSE, 8, "JACKPOT", 6, 15, "100.000", 6, 15, 1000, "z_BigDekuBaba"
            AddScore 100000
            LightSeqFlasher.Play SeqRandom, 4, , 3000
            DOF dJackpot, 2
            QueueFlasherAnimation(FlAn2)
            LightSeqGi.Play SeqRandom, 4, , 3000
            LightJackpot1.State = 0
        End If
        If(LightExtra3.State = 1)Then
            AwardExtraBall()
            DMD "", "", ")", eNone, eNone, eBlinkFast, 1000, TRUE, "z_ExtraBall"
            DMDU_FlashImage TRUE, 16, 1000
            AddScore 1000
            LightSeqFlasher.Play SeqRandom, 4, , 3000
            QueueFlasherAnimation(FlAn2)
            LightSeqGi.Play SeqRandom, 4, , 3000
            LightExtra3.State = 0
        End If
    End If
End Sub

'************
' N targets
'************

Sub TargetN1_Hit
    If Not Tilted Then
        PlaySound SoundFX("fx_droptarget",DOFDropTargets), 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        LightSeqRed.Play SeqBlinking, , 8, 30
        Addscore 1000
        LightN1.State = 1
        LightSD1.State = 1
        CheckSD
        DMD CenterLine(0, "1000"), CenterLine(1, "SHIELD UP"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, ""
        DMDU_BonusText TRUE, 8, "SHIELD UP", 6, 15, "1.000", 6, 15, 1000, ""
        PlaySound "z_Clang2"
        DOF dTargetN1, 2
    End If
End Sub

Sub TargetN2_Hit
    If Not Tilted Then
        PlaySound "fx_droptarget", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        LightSeqRed.Play SeqBlinking, , 8, 30

        Addscore 1000
        LightN2.State = 1
        LightSD5.State = 1
        CheckSD
        DMD CenterLine(0, "1000"), CenterLine(1, "SHIELD UP"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, ""
        DMDU_BonusText TRUE, 8, "SHIELD UP", 6, 15, "1.000", 6, 15, 1000, ""
        PlaySound "z_Clang2"
        DOF dTargetN2, 2
    End If
End Sub

'************
' R Targets
'************

Sub TargetR1_Hit
    If Not Tilted Then
        PlaySound SoundFX("fx_droptarget",DOFDropTargets), 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        LightSeqGreen.Play SeqBlinking, , 8, 30
        TargetR1.IsDropped = 1
        LightR1.State = 1
        DMD CenterLine(0, "5000"), CenterLine(1, "BOW"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, "z_bow"
        DMDU_BonusText TRUE, 9, "   BOW", 6, 15, "   5.000", 6, 15, 1000, "z_bow"
        AddScore 5000
        LightSD2.State = 1
        CheckSD
        CheckRTargets
        DOF dDropTargetR1, 2
    End If
End Sub

Sub TargetR2_Hit
    If Not Tilted Then
        PlaySound SoundFX("fx_droptarget",DOFDropTargets), 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        LightSeqGreen.Play SeqBlinking, , 8, 30
        TargetR2.IsDropped = 1
        LightR2.State = 1
        PlaySound "z_sword"
        DMD CenterLine(0, "5000"), CenterLine(1, "SWORD"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, "z_sword"
        DMDU_BonusText TRUE, 10, "    SWORD", 6, 15, "    5.000", 6, 15, 1000, "z_sword"
        AddScore 5000
        LightSD3.State = 1
        CheckSD
        CheckRTargets
        DOF dDropTargetR2, 2
    End If
End Sub

Sub TargetR3_Hit
    If Not Tilted Then
        PlaySound SoundFX("fx_droptarget",DOFDropTargets), 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
        LightSeqGreen.Play SeqBlinking, , 8, 30
        TargetR3.IsDropped = 1
        LightR3.State = 1
        DMD CenterLine(0, "5000"), CenterLine(1, "SHIELD"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, "z_shield"
        DMDU_BonusText TRUE, 11, "    SHIELD", 6, 15, "    5.000", 6, 15, 1000, "z_shield"
        AddScore 5000
        LightSD4.State = 1
        CheckSD
        CheckRTargets
        DOF dDropTargetR3, 2
    End If
End Sub

Sub CheckRTargets
    If(LightR1.State + LightR2.State + LightR3.State) = 3 Then
        AddScore 10000
        LightSeqFlasher.Play SeqRandom, 4, , 3000
        QueueFlasherAnimation(FlAn2)
        ' a little delay before reseting the targets and lights
        vpmTimer.AddTimer 1000, "ResetRTargets"
    End If
End Sub

Sub ResetRTargets(d)
    PlaySound SoundFX("fx_resetdrop",DOFContactors), 0, 1, -0.05, 0.05
    TargetR1.IsDropped = 0
    TargetR2.IsDropped = 0
    TargetR3.IsDropped = 0
    LightR1.State = 0
    LightR2.State = 0
    LightR3.State = 0
    DOF dDropTargetR_Reset, 2
End Sub

' Shield Lights - Turn on the Jackpot or the Extra game lights on the right hole

Dim SDCount:SDCount = 0
Sub CheckSD
    If(LightSD1.State = 1)And(LightSD2.State = 1)And(LightSD3.State = 1)And(LightSD4.State = 1)And(LightSD5.State = 1)And(LightSD6.State = 1)Then
        LightSeqShield.Play SeqRandom, 6, , 3000
        LightSeqFlasher.Play SeqRandom, 6, , 3000
        QueueFlasherAnimation(FlAn2)
        LightN1.State = 0
        LightN2.State = 0
        LightR1.State = 0
        LightR2.State = 0
        LightR3.State = 0
        LightSD1.State = 0
        LightSD2.State = 0
        LightSD3.State = 0
        LightSD4.State = 0
        LightSD5.State = 0
        LightSD6.State = 0
        If SDCount Then 'prepare extra life
            LightExtra2.State = 2
            SDCount = 1
        Else 'prepare right jackpot
            LightPC4.State = 2
            SDCount = 0
        End If
    End If
End Sub

'************
' P Triggers
'************

Sub TriggerP1_Hit
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dLeftCastleTrigger, 2
        LightP1.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerP1"
    PlaySound "z_link"
End Sub

Sub TriggerP2_Hit
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    DOF dBehindCastleTrigger, 2
    If ActiveBall.VelX <0 Then bSkillShotReady = FALSE 'turn of the skill if the ball goes to the left and down.
    If NOT Tilted Then
        If ActiveBall.VelX> 0 Then
            LightP2.State = 1
            AddScore 1500
            CheckRollovers
            Select Case P2Count
                Case 1
                    Addscore 1000
                    LightPC1.State = 1
                    P2Count = 2
                Case 2
                    Addscore 2000
                    LightPC2.State = 1
                    P2Count = 3
                Case 3
                    Addscore 3000
                    LightPC3.State = 1
                    P2Count = 4
                Case 4
                    Addscore 6000
                    LightPC4.State = 1
                    DMD "-", CenterLine(1, "GET THE JACKPOT"), "", eNone, eScrollLeft, eNone, 500, TRUE, ""
                    DMDU_BonusText TRUE, 0, "GET THE", 6, 15, "JACKPOT", 6, 15, 1000, ""
                    LightSpinner1.State = 1
                    P2Count = 1
                    LightPC1.State = 0
                    LightPC2.State = 0
                    LightPC3.State = 0
            End Select
            PlaySound "z_watchout"
            PlaySound "z_Out"
        End If
    End If
    LastSwitchHit = "TriggerP2"
End Sub

Sub TriggerP3_Hit
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    DOF dShooterLaneTrigger, 2
    If ActiveBall.VelY> 0 Then bSkillShotReady = FALSE 'turn of the skill if the ball goes down.
    If NOT Tilted Then
        LightP3.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerP3"
    PlaySound "z_Dismount"
End Sub

Sub TriggerP4_Hit
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        LightP4.State = 1
        DOF dTreasureRampTrigger, 2
        CheckRollovers
        Select Case P4Count
            Case 1
                Addscore 1000
                LightPD1.State = 1
                DMD CenterLine(0, "1000"), CenterLine(1, "EPONA"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, ""
                DMDU_BonusText TRUE, 12, "    EPONA", 6, 15, "    1.000", 6, 15, 1000, ""
                P4Count = 2
            Case 2
                Addscore 2000
                LightPD2.State = 1
                DMD CenterLine(0, "2000"), CenterLine(1, "EPONA"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, ""
                DMDU_BonusText TRUE, 12, "    EPONA", 6, 15, "    2.000", 6, 15, 1000, ""
                P4Count = 3
            Case 3
                Addscore 3000
                LightPD3.State = 1
                DMD CenterLine(0, "3000"), CenterLine(1, "EPONA"), "", eBlinkFast, eScrollLeft, eNone, 1000, TRUE, ""
                DMDU_BonusText TRUE, 12, "    EPONA", 6, 15, "    3.000", 6, 15, 1000, ""
                P4Count = 4
            Case 4
                Addscore 3000
                LightPD4.State = 1
                DMD "-", CenterLine(1, "PREPARE ATTACK"), "", eNone, eScrollLeft, eNone, 500, FALSE, ""
                DMD "-", CenterLine(1, "ATTACK BONUS"), "", eNone, eScrollLeft, eNone, 500, TRUE, ""
                DMDU_BonusText TRUE, 0, "PREPARE", 6, 15, "ATTACK", 6, 15, 1000, ""
                DMDU_BonusText FALSE, 0, "ATTACK", 6, 15, "BONUS", 6, 15, 1000, ""

                LightSpinner1.State = 1
                P4Count = 1
        End Select
    End If
    LastSwitchHit = "TriggerP4"
    PlaySound "z_eponaride"
End Sub

'*********
' spinner
'*********

Sub Spinner1_Spin()
    PlaySound "fx_spinner", 0, 1, 0.05, 0
    If NOT Tilted Then
        If(LightSpinner1.State = 1)Then
            AddScore 1000
            DMD CenterLine(0, "1000"), "-", "", eBlinkFast, eNone, eNone, 500, TRUE, ""
            DMDU_BonusText TRUE, 8, "SPINNER:", 6, 15, "1.000", 6, 15, 500, ""
        Else
            AddScore 100
        End If
        DOF dSpinner, 2
        PlaySound "z_swordspin"
        LastSwitchHit = "Spinner1"
    End If
End Sub

'************
' V triggers
'************

Sub TriggerV1_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If ActiveBall.VelY> 0 Then bSkillShotReady = FALSE 'turn of the skill if the ball goes down.
    If NOT Tilted Then
        DOF dRightCastleTrigger, 2
        LightV1.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV1"
End Sub

Sub TriggerV2_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dBlueTrigger1, 2
        LightV2.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV2"
End Sub

Sub TriggerV3_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dBlueTrigger2, 2
        LightV3.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV3"
End Sub

Sub TriggerV4_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dBlueTrigger3, 2
        LightV4.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV4"
End Sub

Sub TriggerV5_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dBlueTrigger4, 2
        LightV5.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV5"
End Sub

Sub TriggerV6_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dBlueTrigger5, 2
        LightV6.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV6"
End Sub

Sub TriggerV7_Hit()
    PlaySound "fx_sensor", 0, 1, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
    If NOT Tilted Then
        DOF dBlueTrigger6, 2
        LightV7.State = 1
        AddScore 1500
        CheckRollovers
    End If
    LastSwitchHit = "TriggerV7"
End Sub

Dim DoubleScore
DoubleScore = 1

Sub CheckRollovers
    If DoubleScore = 1 Then
        If LightP1.State + LightP2.State + LightP3.State + LightP4.State + LightV1.State + LightV2.State + LightV3.State + LightV4.State + LightV5.State + LightV6.State + LightV7.State = 11 Then
            AddMultiball 1
            EnableBallSaver 20
            DoubleScore = 2 'this is a score multiplier
            DMD "-", CenterLine(1, "DOUBLE SCORING!"), "", eNone, eBlinkFast, eNone, 800, FALSE, ""
            DMD "-", CenterLine(1, "MULTIBALL!"), "", eNone, eBlinkFast, eNone, 1000, TRUE, ""
            LightSeqFlasher.Play SeqRandom, 4, , 3000
            DMDU_BonusText TRUE, 0, "DOUBLE", 6, 15, "SCORING", 6, 15, 1000, ""
            DMDU_FlashImage TRUE, 15, 1000
            QueueFlasherAnimation(FlAn2)
            LightSeqDouble.Play SeqRandom, 10, , 4000 'blink the GI lights during this mode
        End If
    End If
End Sub

Sub ResetDoubleScore
    DoubleScore = 1
    LightP1.State = 0
    LightP2.State = 0
    LightP3.State = 0
    LightP4.State = 0
    LightV1.State = 0
    LightV2.State = 0
    LightV3.State = 0
    LightV4.State = 0
    LightV5.State = 0
    LightV6.State = 0
    LightV7.State = 0
    LightSeqDouble.StopPlay
End Sub

' A Targets

Sub TargetA1_Hit()
    If NOT Tilted Then
        LightSeq2Flasher.Play SeqRandom, 2, , 2000
        LightA1.State = 1
        CheckA1
        AddScore 1000
        PlaySound "z_Volvagia"
        DOF dDTargetA1, 2
    End If
End Sub

Sub TargetA2_Hit()
    If NOT Tilted Then
        LightSeq2Flasher.Play SeqRandom, 2, , 2000
        LightA2.State = 1
        CheckA1
        AddScore 1000
        PlaySound "z_Gohma"
        DOF dDTargetA2, 2
    End If
End Sub

Sub TargetA3_Hit()
    If NOT Tilted Then
        LightSeq2Flasher.Play SeqRandom, 2, , 2000
        DOF dDTargetA3, 2
        LightA3.State = 1
        CheckA1
        AddScore 1000
        PlaySound "z_BongoBongo"
    End If
End Sub

Sub TargetA4_Hit()
    If NOT Tilted Then
        LightSeq2Flasher.Play SeqRandom, 2, , 2000
        DOF dDTargetA4, 2
        LightA4.State = 1
        CheckA1
        AddScore 1000
        PlaySound "z_Barinade"
    End If
End Sub

Sub TargetA5_Hit()
    If NOT Tilted Then
        LightSeq2Flasher.Play SeqRandom, 2, , 2000
        DOF dDTargetA5, 2
        LightA5.State = 1
        CheckA1
        AddScore 1000
        PlaySound "z_KingDodongo"
    End If
End Sub

Dim TimerA1Step:TimerA1Step = 0

Sub TimerA1_Timer
    Select case TimerA1Step
        Case 0:PlaySound "fx_Solenoid", 0, 0.3:TargetA2.IsDropped = 0:TargetA4.IsDropped = 0:TargetA1.IsDropped = 1:TargetA3.IsDropped = 1:TargetA5.IsDropped = 1:TimerA1Step = 1
        Case 1:PlaySound "fx_Solenoidoff", 0, 0.3:TargetA2.IsDropped = 1:TargetA4.IsDropped = 1:TargetA1.IsDropped = 0:TargetA3.IsDropped = 0:TargetA5.IsDropped = 0:TimerA1Step = 0
    End Select
End Sub

Sub CheckA1()
    If(LightA1.State + LightA2.State + LightA3.State + LightA4.State + LightA5.State) = 5 Then
        TimerA1.Enabled = FALSE
        TargetA1.IsDropped = 1
        TargetA2.IsDropped = 1
        TargetA3.IsDropped = 1
        TargetA4.IsDropped = 1
        TargetA5.IsDropped = 1
        DMD "-", CenterLine(1, "GUARDIANS ARE DOWN"), "", eNone, eBlinkFast, eNone, 500, FALSE, ""
        DMD "-", CenterLine(1, "KILL GANONDORF!"), "", eNone, eBlinkFast, eNone, 500, TRUE, ""
        TargetG1.IsDropped = 0
        DMDU_BonusText TRUE, 0, "GUARDIANS", 6, 15, "ARE DOWN", 6, 15, 1000, ""
        DMDU_BonusText FALSE, 0, "KILL", 6, 15, "GANONDORF!", 6, 15, 1000, ""
        DOF dDTargetA_Change, 2
        PlaySong "m_ganondorf"
        ChangeGi "Red"
        LightG1.State = 2
    End If
End Sub

Sub TargetG1_Hit
    If NOT Tilted Then
        TargetG1.IsDropped = 1
        LightG1.State = 1
        LightSeqFlasher.Play SeqRandom, 4, , 5000
        QueueFlasherAnimation(FlAn2)
        DMD "-", CenterLine(1, "GANONDORF IS DEAD"), "", eNone, eBlink, eNone, 700, FALSE, ""
        DMD "-", CenterLine(1, "GET THE TRIFORCE"), "", eNone, eBlink, eNone, 700, FALSE, ""
        DMD "-", CenterLine(1, "RESCUE ZELDA"), "", eNone, eBlinkFast, eNone, 700, TRUE, ""
        DMDU_BonusText TRUE, 0, "GANONDORF", 6, 15, "IS DEAD", 6, 15, 1000, ""
        DMDU_BonusText FALSE, 0, "GET THE", 6, 15, "TRIFORC", 6, 15, 1000, ""
        DMDU_BonusText FALSE, 0, "RESCUE", 6, 15, "ZELDA", 6, 15, 1000, ""
        'Open the gates
        vpmTimer.AddTimer 1000, "OpenGates"
        AddScore 5000
        DOF dDTargetG1, 2
        PlaySound "z_link"
        PlaySound "z_attack"
        PlaySound "z_ganondie"
    End If
End Sub

Sub TargetG1_Timer
    If TargetG1p.Z <-65 Then TargetG1p.Z = -60:TargetG1.isdropped = 1:Me.TimerEnabled = 0
    If TargetG1p.Z> 10 Then TargetG1p.Z = -25:TargetG1.isdropped = 0:Me.TimerEnabled = 0
    TargetG1p.Z = TargetG1p.Z + TargetG1.UserValue
End Sub

Sub OpenGates(dummy)
    Popup1.IsDropped = 1
    Popup2.IsDropped = 1
    Popup3.IsDropped = 1
    DOF dCastleGate, 2
    PlaySound "z_doorclose"
    CastleDiverter.TimerEnabled = 1
End Sub

Dim cdivpos:cdivPos = 0

Sub CastleDiverter_Timer
    Select Case cdivpos
        Case 0:CastleDiverter.RotateToEnd:cdivpos = 1
        Case 1:CastleDiverter.RotateToStart:cdivPos = 0
    End Select
    DOF dCastleDiverter, 2
End Sub

' Captive Ball target

' Captive Ball Right - done here to add the ball hit sound.
Sub CapTrigger_Hit:cbLeft.TrigHit ActiveBall:DOF dCaptiveBall, 2:End Sub
Sub CapTrigger_UnHit:cbLeft.TrigHit 0:End Sub
Sub CapWall_Hit:PlaySound "fx_collide", 0, 1, -0.05, 0.15:cbLeft.BallHit ActiveBall:End Sub
Sub CapKicker2_Hit:cbLeft.BallReturn Me:End Sub

Sub cbTarget_Hit()
    PlaySound SoundFX("fx_target",DOFTargets), 0, 1, -0.05, 0.15
    'animate the target
    DOF dcbTarget, 2
    If Not Tilted Then
        PlaySound "z_switch"
        Select Case cbCount
            Case 1
                LightK5A.State = 1
                DMD CenterLine(0, FormatScore("1000")), CenterLine(1, "FLAG 1"), "", eBlink, eScrollLeft, eNone, 500, TRUE, ""
                DMDU_BonusText TRUE, 8, "FLAG 1", 6, 15, "1.000", 6, 15, 1000, ""
                AddScore(1000)
                LightSeqFlasher.Play SeqRandom, 4, , 3000
                QueueFlasherAnimation(FlAn2)
                cbCount = 2
            Case 2
                LightK5B.State = 1
                DMD CenterLine(0, FormatScore("1000")), CenterLine(1, "FLAG 2"), "", eBlink, eScrollLeft, eNone, 500, TRUE, ""
                DMDU_BonusText TRUE, 8, "FLAG 2", 6, 15, "1.000", 6, 15, 1000, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 3000
                QueueFlasherAnimation(FlAn2)
                cbCount = 3
            Case 3
                LightK5C.State = 1
                DMD CenterLine(0, FormatScore("1000")), CenterLine(1, "FLAG 3"), "", eBlink, eScrollLeft, eNone, 500, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 3000
                DMDU_BonusText TRUE, 8, "FLAG 3", 6, 15, "1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                cbCount = 4
            Case 4 'prepare left Jackpot
                LightSpecial1.State = 1
                DropA1.IsDropped = 0
                DropA2.IsDropped = 0
                DOF dDropTargetA_Reset, 2
                LightJackpot1.State = 2
                DMD "-", CenterLine(1, "JACKPOT PREPARED"), "", eNone, eBlinkFast, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 3000
                DMDU_BonusText TRUE, 14, "     JACKPOT", 6, 15, "    PREPARED", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                cbCount = 5
            Case 5 'give extra life or extra ball
                LightExtra1.State = 1
                If(LightMystery1.State = 1)Then
                    DMD "", "", "(", eNone, eNone, eBlinkFast, 1000, FALSE, ""
                    Credits = Credits + 1
                    DMD CenterLine(0, "30000"), CenterLine(1, "CREDITS: " & Credits), "-", eBlinkFast, eNone, eNone, 1000, FALSE, "fx_knocker"
                    DOF dKnocker, 2
                    DMDU_BonusText TRUE, 8, "CREDITS: " & Credits, 6, 15, "30.000", 6, 15, 1000, ""
                    Addscore 30000
                    LightSpecial1.State = 0
                    DMD CenterLine(0, "INCREDIBLE!!!"), CenterLine(1, "FREE GAME"), "-", eNone, eBlink, eNone, 1000, TRUE, "z_YAY"
                    LightSeqFlasher.Play SeqRandom, 4, , 5000
                    DMDU_BonusText FALSE, 0, "INCREDIBLE!!!", 6, 15, "FREE GAME", 6, 15, 1000, "z_YAY"
                    QueueFlasherAnimation(FlAn2)
                    LightSeqEffect.UpdateInterval = 1
                    LightSeqEffect.Play SeqUpOn, 20
                    LightMystery1.State = 0
                Else
                    AwardExtraBall()
                    DMD "", "", ")", eNone, eNone, eBlinkFast, 2000, TRUE, "z_ExtraBall"
                    LightSeqFlasher.Play SeqRandom, 4, , 3000
                    DMDU_BonusText TRUE, 16, " ", 0, 0, " ", 0, 0, 1000, ""
                    QueueFlasherAnimation(FlAn2)
                    LightSeqGi.Play SeqRandom, 4, , 3000
                    Addscore 10000
                End If
                cbCount = 5
                vpmTimer.AddTimer 1000, "ResetcbLights"
        End select
    End If
End Sub

Sub ResetcbLights(d)
    LightK5A.State = 0
    LightK5B.State = 0
    LightK5C.State = 0
    LightSpecial1.State = 0
    LightExtra1.State = 0
    cbCount = 1
End Sub

'**********************
' Left Ramp Castle Hole
'**********************

Sub Kicker1_Hit()
    Dim tmp
    PlaySound "fx_kicker-enter", 0, 1, -0.05, 0.15
    If NOT Tilted Then
        DMD "-", CenterLine(1, "ZELDA IS SAFE"), "", eNone, eBlinkFast, eNone, 800, TRUE, "z_ZeldaLaugh"
        LightSeqFlasher.Play SeqRandom, 4, , 3000
        DMDU_BonusText TRUE, 0, "ZELDA", 6, 15, "IS SAVE", 6, 15, 800, "z_ZeldaLaugh"
        QueueFlasherAnimation(FlAn2)
        LightSeqGI.Play SeqRandom, 4, , 3000

        ' Give multiball if all 4 triforce lights are on.
        If((LightMult1.State + LightMult2.State + LightMult3.State + LightMult4.State) = 4)And(bMultiBallMode = FALSE)Then
            DMD "-", CenterLine(1, "TRIFORCE IS ASSEMBLED"), "", eNone, eScrollLeft, eNone, 200, FALSE, ""
            DMD CenterLine(0, "MULTIBALL"), CenterLine(1, "YOU HAVE THE POWER"), "", eBlinkFast, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_BonusText TRUE, 8, "YOU ASSEMBLED", 6, 15, "TRIFORCE", 6, 15, 1000, ""
            DMDU_BonusText FALSE, 8, "YOU HAVE", 6, 15, "THE POWER", 6, 15, 1000, ""
            DMDU_FlashImage FALSE, 15, 1000
            DMDU_ShowScoreBoard
            LightMult4.State = 0
            LightMult3.State = 0
            LightMult2.State = 0
            LightMult1.State = 0
            EnableBallSaver 20 '20 seconds ballsaver
            AddMultiball 4     ' add 4 multiballs
            PlaySong "m_multiball"
            Addscore 8000
            PlaySound "z_link"
            PlaySound "z_attack"
            PlaySound "z_ganondie"

        'otherwise give Jackpot during multiballs
        ElseIf bMultiBallMode Then
            DMD "", "", """", eNone, eNone, eBlinkFast, 1000, FALSE, ""
            DMD "", CenterLine(1, FormatScore("30000")), "", eNone, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_FlashImage TRUE, 19, 1000
            DMDU_BonusText TRUE, 8, "JACKPOT", 6, 15, "30.000", 6, 15, 1000, ""
            AddScore 30000
            DOF dJackpot, 2
        ElseIf LightMystery1.State = 1 Then
            DMD "", "", "(", eNone, eNone, eBlinkFast, 1000, FALSE, "z_LOOT"
            tmp = 5000 + 5000 * INT(RND(1) * 10)
            DMD CenterLine(0, FormatScore(tmp)), CenterLine(1, "MYSTERY SCORE"), "", eBlink, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_BonusText FALSE, 8, " ", 0, 0, "MYSTERY AWARD", 6, 15, 1000, "z_LOOT"
            DMDU_BonusText TRUE, 8, "MYSTERY SCORE", 6, 15, FormatNumber(tmp, 0, , , -1), 6, 15, 1000, ""
            Addscore tmp
            LightMystery1.State = 0
        Else
            ' otherwise give some points
            Addscore 2500
        End If
    End If
    Kicker1.TimerEnabled = 1
End Sub

Sub Kicker1_Timer
    Kicker1.TimerEnabled = 0
    LightSeqFlasher.Play SeqBlinking, , 8, 30
    QueueFlasherAnimation(FlAn3)
    kicker1.destroyball
    kicker1a.createball
    Kicker1a.kick 270, 6
    DOF dCastleKickerLeft, 2
    LightMystery1.State = 0
    PlaySound "fx_kicker", 0, 1, -0.05, 0.15
    PlaySound "fx_metalrolling", 0, 0.2, 0.05, 0.15
End Sub

'*****************
' Right Ramp hole
'*****************

' kicker2a is the exit kicker for kicker2, kicker3 and kicker6 so we create a ballstack so we don't loose balls during multiball

Dim rBalls2Eject, AddRampBallDelay
rBalls2Eject = 0

Sub AddRampBall(nballs)
    rBalls2Eject = rBalls2Eject + nballs
    AddRampBallDelay = 40
End Sub

' Eject the ball/balls after the delay, AddRampBallDelay
Sub CreateRampBall()
    ' remove the visible ball in kicker 2
    kicker2.destroyball
    ' create a ball in the plunger lane kicker.
    kicker2a.CreateBall
    ' kick it out..
    Kicker2a.Kick 170, 6
    DOF dCastleKickerRight, 2
    PlaySound SoundFX("fx_popper",DOFContactors), 0, 1, 0.05, 0.15
    PlaySound "fx_metalrolling", 0, 0.2, 0.05, 0.15

    rBalls2Eject = rBalls2Eject -1
    If rBalls2Eject Then   'if there are more balls to eject then do that
        kicker2.Createball 'create a visible ball ready to be kikcked out
        AddRampBallDelay = 20
    End If
End Sub

Sub Kicker2_Hit()
    PlaySound "fx_kicker-enter", 0, 1, 0.05, 0.15
    AddRampBall 1 'we let the ball be visible until ejected
    If Not Tilted Then
        PlaySound "z_ocarina"
        LightSeqGI.Play SeqRandom, 4, , 3000
        If(LightPD4.State = 1)Then
            LightPD1.State = 0
            LightPD2.State = 0
            LightPD3.State = 0
            LightPD4.State = 0
            P4Count = 1
            DMD CenterLine(0, FormatScore("25000")), CenterLine(1, "OCARINA SCORE"), "", eBlink, eBlink, eNone, 1000, FALSE, ""
            DMD "-", CenterLine(1, "MULTIBALL"), "", eNone, eBlink, eNone, 1000, TRUE, ""
            DMDU_BonusText TRUE, 8, "OCARINA SCORE", 6, 15, "25.000", 6, 15, 1000, ""
            DMDU_FlashImage FALSE, 15, 1000
            AddScore 25000
            AddMultiball 1
        End If

        Select Case K2Count
            Case 1
                DMD "-", CenterLine(1, "CASTLE 1 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 1 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 2
            Case 2
                DMD "-", CenterLine(1, "CASTLE 2 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 2 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 3
            Case 3
                DMD "-", CenterLine(1, "CASTLE 3 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 3 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 4
            Case 4
                DMD "-", CenterLine(1, "CASTLE 4 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 4 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 5
            Case 5
                DMD "-", CenterLine(1, "CASTLE 5 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 5 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                LightSeqGi.Play SeqRandom, 4, , 4000
                EnableBallSaver 20
                K2Count = 6
            Case 6
                DMD "-", CenterLine(1, "CASTLE 6 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 6 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 7
            Case 7
                DMD "-", CenterLine(1, "CASTLE 7 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 7 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 8
            Case 8
                DMD "-", CenterLine(1, "CASTLE 8 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 8 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 9
            Case 9
                DMD "-", CenterLine(1, "CASTLE 9 RETURN"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                DMDU_BonusText TRUE, 13, "  CASTLE 9 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                QueueFlasherAnimation(FlAn2)
                K2Count = 10
            Case 10
                DMD "-", CenterLine(1, "CASTLE 10 RETURN"), "", eNone, eBlink, eNone, 1000, FALSE, ""
                DMD "-", CenterLine(1, "JACKPOT PREPARED"), "", eNone, eBlink, eNone, 1000, TRUE, ""
                DMDU_BonusText TRUE, 13, " CASTLE 10 EXIT", 6, 15, "    1.000", 6, 15, 1000, ""
                DMDU_BonusText FALSE, 14, "     JACKPOT", 6, 15, "    PREPARED", 6, 15, 1000, ""
                AddScore 1000
                LightSeqFlasher.Play SeqRandom, 4, , 4000
                QueueFlasherAnimation(FlAn2)
                DropA1.IsDropped = 1
                DOF dDropTargetA1, 2
                DropA2.IsDropped = 1
                DOF dDropTargetA2, 2
                LightJackpot1.State = 2
                K2Count = 1
        End Select
    End If
End Sub

'************************************************
' Right castle hole - the triforce multiball hole
'************************************************

Sub Kicker3_Hit()
    PlaySound "fx_hole-enter", 0, 1, 0.05, 0.15
    Kicker3.Destroyball:AddRampBall 1
If NOT Tilted Then
    If(LightMult1.State = 0)And(LightMult2.State = 0)And(LightMult3.State = 0)And(LightMult4.State = 0)And(bMultiBallMode = FALSE)Then
        LightMult1.State = 1
        DMD "-", CenterLine(1, "1ST TRIFORCE PIECE"), "", eNone, eScrollLeft, eNone, 500, FALSE, ""
        DMD "-", CenterLine(1, "ASSEMBLE THE TRIFORCE"), "", eNone, eScrollLeft, eNone, 500, TRUE, ""
        DMDU_BonusText TRUE, 8, "1ST PIECE OF", 6, 15, "TRIFORCE", 6, 15, 1000, ""
        DMDU_BonusText FALSE, 8, "ASSEMBLE", 6, 15, "THE TRIFORCE", 6, 15, 1000, ""

        PlaySound "z_triforce1"
        Addscore 1000
        Kicker3.TimerEnabled = 1
        Exit Sub
    End If
    If(LightMult1.State = 1)And(lightMult2.State = 0)And(LightMult3.State = 0)And(LightMult4.State = 0)And(bMultiBallMode = FALSE)Then
        LightMult2.State = 1
        DMD "-", CenterLine(1, "2ND TRIFORCE PIECE"), "", eNone, eScrollLeft, eNone, 500, FALSE, ""
        DMD "-", CenterLine(1, "ASSEMBLE THE TRIFORCE"), "", eNone, eScrollLeft, eNone, 800, TRUE, ""
        DMDU_BonusText TRUE, 8, "2ND PIECE OF", 6, 15, "TRIFORCE", 6, 15, 1000, ""
        DMDU_BonusText FALSE, 8, "ASSEMBLE", 6, 15, "THE TRIFORCE", 6, 15, 1000, ""

        PlaySound "z_triforce1"
        Addscore 2000
        Kicker3.TimerEnabled = 1
        Exit Sub
    End If
    If(LightMult1.State = 1)And(LightMult2.State = 1)And(LightMult3.State = 0)And(LightMult4.State = 0)And(bMultiBallMode = FALSE)Then
        LightMult3.State = 1
        DMD "-", CenterLine(1, "3RD TRIFORCE PIECE"), "", eNone, eScrollLeft, eNone, 500, FALSE, ""
        DMD "-", CenterLine(1, "ASSEMBLE THE TRIFORCE"), "", eNone, eScrollLeft, eNone, 800, TRUE, ""
        DMDU_BonusText TRUE, 8, "3RD PIECE OF", 6, 15, "TRIFORCE", 6, 15, 1000, ""
        DMDU_BonusText FALSE, 8, "ASSEMBLE", 6, 15, "THE TRIFORCE", 6, 15, 1000, ""

        PlaySound "z_triforce2"
        Addscore 3000
        Kicker3.TimerEnabled = 1
        Exit Sub
    End If
    If(LightMult1.State = 1)And(LightMult2.State = 1)And(LightMult3.State = 1)And(LightMult4.State = 0)And(bMultiBallMode = FALSE)Then
        LightMult4.State = 1
        DMD "-", CenterLine(1, "TRIFORCE IS ASSEMBLED"), "", eNone, eScrollLeft, eNone, 500, FALSE, ""
        DMD "-", CenterLine(1, "ASSEMBLE THE TRIFORCE"), "", eNone, eScrollLeft, eNone, 800, TRUE, ""
        DMDU_BonusText TRUE, 8, "YOU ASSEMBLED", 6, 15, "TRIFORCE", 6, 15, 1000, ""
        PlaySound "z_triforce3"
        Addscore 4000
        Kicker3.TimerEnabled = 1
        Exit Sub
    End If
    If(LightMult1.State = 1)And(LightMult2.State = 1)And(LightMult3.State = 1)And(LightMult4.State = 1)Then 'triforce score
        DMD CenterLine(0, FormatScore("5000")), CenterLine(1, "TRIFORCE SCORE"), "", eBlink, eBlinkFast, eNone, 1000, TRUE, "z_LOOT"
        DMDU_BonusText TRUE, 8, "TRIFORCE SCORE", 6, 15, "5.000", 6, 15, 500, "z_LOOT"
        Addscore 5000
    End If
End If
End Sub

'*****************
' Right Ramp hole
'*****************

Sub Kicker4_Hit()
    Dim tmp
    PlaySound "fx_Kicker-Enter", 0, 1, 0.05, 0.15
    If NOT Tilted Then
        If LightExtra2.State = 2 Then
            AwardExtraBall()
            DMD "", "", ")", eNone, eNone, eBlinkFast, 1000, FALSE, "z_ExtraBall"
            DMD CenterLine(0, FormatScore("10000")), CenterLine(1, "EXTRA-LIFE"), "", eBlink, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_BonusText TRUE, 16, " ", 0, 0, " ", 0, 0, 1000, "z_ExtraBall"
            DMDU_BonusText FALSE, 8, "EXTRA-LIFE", 6, 15, "10.000", 6, 15, 1000, ""

            Addscore 10000
            LightExtra2.State = 0
        ElseIf LightPC4.State = 2 Then
            DMD "", "", """", eNone, eNone, eBlinkFast, 1000, FALSE, "z_LOOT"
            DMD "", CenterLine(1, FormatScore("50000")), "", eBlink, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_BonusText TRUE, 8, "JACKPOT!", 0, 0, "50.000", 6, 15, 1000, ""
            AddScore 50000
            LightPC4.State = 0
        ElseIf LightMystery1.State = 1 Then
            DMD "", "", "(", eNone, eNone, eBlinkFast, 1000, FALSE, "z_LOOT"
            tmp = 5000 + 5000 * INT(RND(1) * 10)
            DMD CenterLine(0, FormatScore(tmp)), CenterLine(1, "MYSTERY SCORE"), "", eBlink, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_BonusText TRUE, 8, "MYSTERY SCORE", 6, 15, FormatNumber(tmp, 0, , , -1), 6, 15, 1000, ""
            Addscore tmp
            LightMystery1.State = 0
        Else
            LightSD8.State = 1
            CheckSD
            PlaySound "z_chest"
            AddScore 5000
        End If
    End If
    Flasher5.State = 2
    DOF dLowerRFlasher, 1
    Kicker4.TimerEnabled = 1
End Sub

Sub Kicker4_Timer()
    Me.TimerEnabled = 0
    Flasher5.State = 0
    DOF dLowerRFlasher, 0
    'LightSeqFlasher.Play SeqBlinking, , 6, 60
    Kicker4.kick 230, 16
    PlaySound "fx_Kicker", 0, 1, 0.05, 0.15
    DOF dKickerRight, 2
    PlaySound "z_heart"
End Sub

'*****************************
' Hidden castle back entrance
'*****************************

Sub Kicker6_Hit()
    PlaySound "fx_Kicker-Enter", 0, 1, 0.05, 0.15
    DOF dSecretPassageFound, 2
    Kicker6.Destroyball:AddRampBall 1
If NOT Tilted Then
    DMD "-", "FOUND SECRET PASSAGE", "", eNone, eBlink, eNone, 1000, TRUE, "z_secret"
    DMDU_BonusText TRUE, 23, "", 0, 0, "", 0, 0, 1000, "z_secret"
    If bSkillshotReady Then ' give skillshot or superskillshot!!!!
        LightSeqFlasher.Play SeqRandom, 4, , 4000
        QueueFlasherAnimation(FlAn2)
        bSkillshotReady = FALSE
        If LastSwitchHit = "TriggerV1" Then
            DMD CenterLine(0, "SKILLSHOT"), CenterLine(1, "50000"), "", eBlink, eBlinkFast, eNone, 1000, TRUE, "vo_skillshot"
            DMDU_BonusText FALSE, 8, "SKILLSHOT", 6, 15, "50.000", 6, 15, 1000, "vo_skillshot"
            AddScore 50000
        End If
        If LastSwitchHit = "TriggerP2" Then
            DMD CenterLine(0, "SUPERSKILL"), CenterLine(1, "100000"), "", eBlink, eBlinkFast, eNone, 1000, TRUE, "vo_superskillshot"
            DMDU_BonusText FALSE, 8, "SUPERSKILL", 6, 15, "100.000", 6, 15, 1000, "vo_superskillshot"
            AddScore 100000
        End If
    Else
        LightSeqFlasher.Play SeqRandom, 4, , 2000
        QueueFlasherAnimation(FlAn2)
        AddScore 5000
        K6Count = K6Count + 1
        If K6Count = 5 Then
            DropA1.IsDropped = 0
            DropA2.IsDropped = 0
            DOF dDropTargetA_Reset, 2
            LightJackpot1.State = 2
            DMD "-", CenterLine(1, "JACKPOT PREPARED"), "", eNone, eBlinkFast, eNone, 1000, TRUE, ""
            DMDU_BonusText TRUE, 14, "     JACKPOT", 6, 15, "    PREPARED", 6, 15, 1000, ""
            K6Count = 0
        End If
    End If
End If
End Sub

'*****************
' castle targets
'*****************

Sub TargetA_Hit()
    PlaySound SoundFX("fx_target",DOFTargets), 0, 1, -0.05, 0.15
    If NOT Tilted Then
        LightSeqRed.Play SeqBlinking, , 8, 30
        LightSD6.State = 1
        CheckSD
        LightSaveBall.State = 1
        LightMystery1.State = 0
        Addscore 1000
        PlaySound "z_pound"
        DOF dTargetA, 2
    End If
End Sub

Sub TargetB_Hit()
    PlaySound SoundFX("fx_target",DOFTargets), 0, 1, 0.05, 0.15
    If NOT Tilted Then
        LightSeqRed.Play SeqBlinking, , 8, 30
        DOF dTargetB, 2
        LightSD7.State = 1
        CheckSD
        LightSaveBall.State = 0
        LightMystery1.State = 1
        Addscore 1000
        PlaySound "z_pound"
    End If
End Sub

'***********************************************************************************
'****            				 	UltraDMD add-on								****
'***********************************************************************************
Dim UltraDMD, UltraDMD_Backgroundfile, UltraDMDScoreBoardInterrupted
' effects for text elements

Const dfadein = 0
Const dfadeout = 1
Const dzoomin = 2
Const dzoomout = 3
Const dscrolloffleft = 4
Const dscrolloffright = 5
Const dscrollonleft = 6
Const dscrollonright = 7
Const dscrolloffup = 8
Const dscrolloffdown = 9
Const dscrollonup = 10
Const dscrollondown = 11
Const dnone = 14

Dim Zelda_Images
Zelda_Images = Array("Clear.png", "ZeldaGameOver.png", "ZeldaIntro1.png", "ZeldaIntro2.png", "ZeldaIntro3.png", "ZeldaIntro4.png", "ZeldaIntro5.png", "ZeldaHighScores.png", _
    "ZeldaBorderDashed.gif", "Bow.png", "Sword.png", "Shield.png", "Epona.png", "Castle.png", "Crown.png", "ZeldaMultiBall.gif", "ZeldaExtraBall.gif",                       _
    "Placeholder", "ZeldaBorderThick.png", "ZeldaJackpot.gif", "ZeldaScoreBoard.png", "ZeldaInsertCoin.png", "ZeldaPressStart.png", "ZeldaSecretPassage.gif",                _
    "ZeldaCredits.png")

' High level UltraDMD calls (to prevent from direct use of the UltraDMD API inside subs)
Sub DMDU_Init():DMDUltra_Init:End Sub

Sub DMDU_Flush()
    If IsObject(UltraDMD)Then
        DMDUltra_Flush
    End If
End Sub

Sub DMDU_SetBackgroundImage(image_index)
    If NOT IsObject(UltraDMD)Then
        Exit Sub
    End If
    UltraDMD_Backgroundfile = Zelda_Images(image_index)
End Sub

Sub DMDU_ShowWarning(flush, text, duration, sound)
    If IsObject(UltraDMD)Then
        DMDU_SetBackgroundImage 18
        DMDUltra_QueueScene00Ex flush, "", 0, 0, text, 15, 0, dfadein, duration, dfadeout
        If((sound <> "")AND(ReelDMDActive = False))Then PlaySound sound
        UltraDMDScoreBoardInterrupted = True
    End If
End Sub

Sub DMDU_BonusText(flush, background, toptext, toptextbrightness, topoutlinebrightness, bottomtext, bottomtextbrightness, bottomoutlinebrightness, duration, sound)
    If IsObject(UltraDMD)Then
        DMDU_SetBackgroundImage background
        DMDUltra_QueueScene00Ex flush, toptext, toptextbrightness, topoutlinebrightness, bottomtext, bottomtextBrightness, bottomoutlinebrightness, dfadein, duration, dfadeout
        If((sound <> "")AND(ReelDMDActive = False))Then PlaySound sound
        UltraDMDScoreBoardInterrupted = True
    End If
End Sub

Sub DMDU_FlashImage(flush, id, dur)
    If IsObject(UltraDMD)Then
        DMDU_SetBackgroundImage id
        DMDUltra_QueueScene00Ex flush, " ", 0, 0, " ", 0, 0, dfadein, dur, dnone
        UltraDMDScoreBoardInterrupted = True
    End If
End Sub

Dim RomShown:RomShown = False
Sub DMDU_ShowIntro
    If IsObject(UltraDMD)Then
        If RomShown = False Then
            DMDU_ShowRomVersion
            RomShown = True
        End If
        DMDU_RepeatIntro
    End If
End Sub

Sub DMDU_ShowRomVersion
    DMDU_Flush:DMDU_SetBackgroundImage 0:DMDUltra_QueueScene00 FALSE, "ROM VERS." &myVersion, 15, " ", 0, 15, 1000, dscrolloffup
End Sub

Sub DMDU_RepeatIntro
    Dim i
    If IsObject(UltraDMD)Then
        DMDU_ShowGameOver
        DMDU_ShowCredits
        DMDU_SetBackgroundImage 2:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dfadein, 3000, dfadeout
        DMDU_SetBackgroundImage 3:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dfadein, 3000, dfadeout
        DMDU_SetBackgroundImage 4:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dzoomin, 4000, dzoomout
        DMDU_SetBackgroundImage 0
        DMDU_SetBackgroundImage 5:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dscrollonright, 3000, dscrolloffright
        DMDU_SetBackgroundImage 6:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dscrollonleft, 3000, dscrolloffleft
        DMDU_SetBackgroundImage 7
        For i = 1 to 3:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dfadein, 0, dfadeout:Next
        DMDU_SetBackgroundImage 8
        DMDUltra_QueueScene00 FALSE, " ", -1, "1> " & HighScoreName(0) & " " & FormatNumber(HighScore(0), 0, , , -1), 15, dscrollonup, 3000, dscrolloffup
        DMDUltra_QueueScene00 FALSE, " ", -1, "2> " & HighScoreName(1) & " " & FormatNumber(HighScore(1), 0, , , -1), 15, dscrollonup, 3000, dscrolloffup
        DMDUltra_QueueScene00 FALSE, " ", -1, "3> " & HighScoreName(2) & " " & FormatNumber(HighScore(2), 0, , , -1), 15, dscrollonup, 3000, dscrolloffup
        DMDUltra_QueueScene00 FALSE, " ", -1, "4> " & HighScoreName(3) & " " & FormatNumber(HighScore(3), 0, , , -1), 15, dscrollonup, 3000, dfadeout
    End If
End Sub

Sub DMDU_ShowScoreBoard
    If IsObject(UltraDMD)Then
        If UltraDMDScoreBoardInterrupted = False Then
            UltraDMD.DisplayScoreboard PlayersPlayingGame, CurrentPlayer, Score(1), Score(2), Score(3), Score(4), "Player " & CurrentPlayer, "Ball " & Balls
        End If
    End If
End Sub

Sub DMDU_ShowGameOver
    Dim i
    If IsObject(UltraDMD)Then
        DMDU_SetBackgroundImage 1
        For i = 1 to 3:DMDUltra_QueueScene00 FALSE, " ", 0, " ", 0, dfadein, 0, dfadeout:Next
    End If
End Sub

Sub DMDU_ShowCredits
    Dim i
    If IsObject(UltraDMD)Then
        DMDU_SetBackgroundImage 24
        DMDUltra_QueueScene00Ex FALSE, "", 0, 0, "         " & Credits, 11, 6, dzoomin, 1000, dnone
        If AttractMode = True Then
            DMDU_SetBackgroundImage ABS(CBool(Credits)) + 21
            For i = 1 to 3:DMDUltra_QueueScene00Ex FALSE, "", 0, 0, "", 0, 0, dfadein, 0, dfadeout:Next
        Else
            UltraDMDScoreBoardInterrupted = True
        End If
    End If
End Sub

Sub DMDU_QueueSceneWithId(id, flush, background, toptext, topbrightness, topoutlinebrightness, bottomtext, bottomBrightness, bottomoutlinebrightness, animation_in, duration, animation_out)
    If IsObject(UltraDMD)Then
        DMDU_SetBackgroundImage background
        DMDUltra_QueueScene00ExWithId id, flush, toptext, topbrightness, topoutlinebrightness, bottomtext, bottomBrightness, bottomoutlinebrightness, animation_in, duration, animation_out
    End If
End Sub

Sub DMDU_ModifyScene(id, toptext, bottomtext, duration)
    If IsObject(UltraDMD)Then DMDUltra_ModifyScene00Ex id, toptext, bottomtext, duration
End Sub

Dim UltraDMDTimer_Counter
Sub UltraDMDTimer_Timer
    If AttractMode = True Then
        If UltraDMDTimer_Counter = 0 Then
            If sLastBest> 0 Then
                UltraDMD.SetScoreboardBackgroundImage Zelda_Images(0), 15, 15
                UltraDMD.DisplayScoreboard PlayersPlayingGame, sLastBest, Score(1), Score(2), Score(3), Score(4), "LAST SCORE(S)", ""
                UltraDMDTimer_Counter = 30
            Else
                DMDU_RepeatIntro
            End If
        Else
            UltraDMDTimer_Counter = UltraDMDTimer_Counter - 1
        End If
    Else
        If UltraDMDScoreBoardInterrupted = True Then
            If UltraDMD.IsRendering = False Then
                UltraDMDScoreBoardInterrupted = False
                DMDU_ShowScoreBoard
            End If
        End If
    End If
End Sub

' Here come the low level calls that make use of the API
Sub DMDUltra_Init
    'Set UltraDMD = CreateObject("UltraDMD.DMDObject")
	Dim FlexDMD
    Set FlexDMD = CreateObject("FlexDMD.FlexDMD")
    If FlexDMD is Nothing Then
		MsgBox "No UltraDMD found.  This table MAY run without it."
        Exit Sub
    End If
    FlexDMD.GameName = cGameName
	Set UltraDMD = FlexDMD.NewUltraDMD()
    UltraDMD.Init

    If Not UltraDMD.GetMajorVersion = 1 Then
        MsgBox "Incompatible Version of UltraDMD found."
        Exit Sub
    End If

    If UltraDMD.GetMinorVersion <1 Then
        MsgBox "Incompatible Version of UltraDMD found. Please update to version 1.1 or newer."
        Exit Sub
    End If

    Dim fso:Set fso = CreateObject("Scripting.FileSystemObject")
    Dim curDir:curDir = fso.GetAbsolutePathName(".")

    Dim DirName:DirName = UDMDFilesDir
    DirName = Replace(DirName, "%TableDir%", curDir)
    DirName = Replace(DirName, "%TableName%", Table1.Filename)

    If Not fso.FolderExists(DirName)Then _
            Msgbox "UltraDMD userfiles directory '" & DirName & "' does not exist." & CHR(13) & "No graphic images will be displayed on the DMD"
    UltraDMD.SetProjectFolder DirName
    UltraDMDTimer.Enabled = 1
End Sub

Sub DMDUltra_QueueScene00(flush, toptext, topbrightness, bottomtext, bottombrightness, animation_in, duration, animation_out)
    If flush = True Then UltraDMD.CancelRendering
    UltraDMD.DisplayScene00 UltraDMD_Backgroundfile, toptext, topbrightness, bottomtext, bottombrightness, animation_in, duration, animation_out
End Sub

Sub DMDUltra_QueueScene00Ex(flush, toptext, topbrightness, topoutlinebrightness, bottomtext, bottomBrightness, bottomoutlinebrightness, animation_in, duration, animation_out)
    If flush = True Then UltraDMD.CancelRendering
    UltraDMD.DisplayScene00Ex UltraDMD_Backgroundfile, toptext, topbrightness, topoutlinebrightness, bottomtext, bottomBrightness, bottomoutlinebrightness, animation_in, duration, animation_out
End Sub

Sub DMDUltra_QueueScene00ExWithId(id, flush, toptext, topbrightness, topoutlinebrightness, bottomtext, bottomBrightness, bottomoutlinebrightness, animation_in, duration, animation_out)
    If flush = True Then UltraDMD.CancelRendering
    UltraDMD.DisplayScene00ExwithID id, flush, UltraDMD_Backgroundfile, toptext, topbrightness, topoutlinebrightness, bottomtext, bottomBrightness, bottomoutlinebrightness, animation_in, duration, animation_out
End Sub

Sub DMDUltra_ModifyScene00Ex(id, toptext, bottomtext, duration)
    UltraDMD.ModifyScene00Ex id, toptext, bottomtext, duration
End Sub

Sub DMDUltra_Flush
    UltraDMD.CancelRendering
    UltraDMDScoreBoardInterrupted = False
End Sub

'***********************************************************************************
'****            				 	DOF add-on									****
'***********************************************************************************
Sub DOF(dofevent, dofstate)
    If IsObject(Controller)Then
        If dofstate = 2 Then
            Controller.B2SSetData dofevent, 1:Controller.B2SSetData dofevent, 0
        Else
            Controller.B2SSetData dofevent, dofstate
        End If
    End If
End Sub

Sub CreditsTimer_Timer
    If bGameInPlay Then                                                                                 ' is game in progress ?
        DOF dCreditButtonBlink, 0:DOF dCreditButton, 1:DOF dStartButtonBlink, 0                         ' no blinking lights here during game play!
        If Credits> 1 Then                                                                              ' are credits avail?
            DOF dStartButton, 1                                                                         ' I yes, then start button light is permanent on to indicate credit avail
        Else                                                                                            ' otherwise ...
            DOF dStartButton, 0                                                                         ' start button light is off to indicate no more credit
        End If
    Else                                                                                                ' so currently no game in progress
        If Credits> 1 Then                                                                              ' are credits avail?
            DOF dStartButton, 0:DOF dStartButtonBlink, 1:DOF dCreditButton, 1:DOF dCreditButtonBlink, 0 'start button light is blinking to indicate game can be started
        Else                                                                                            ' otherwise ...
            DOF dStartButton, 0:DOF dStartButtonBlink, 0:DOF dCreditButton, 0:DOF dCreditButtonBlink, 1 ' credit button light is blinking to indicate credit is needed
        End If
    End If
End Sub

Dim FlAn1, FlAn2, FlAn3
FlAn1 = Array(Array(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0), _
    Array(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0),           _
    Array(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0),           _
    Array(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0),           _
    Array(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0),           _
    Array(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0))

FlAn2 = Array(Array(1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0),                                                                                                                                                                                     _
    Array(0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0),                                                                                                                                                                                               _
    Array(0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0),                                                                                                                                                                                               _
    Array(0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0),                                                                                                                                                                                               _
    Array(1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0),                                                                                                                                                                                               _
    Array(0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0))

FlAn3 = Array(Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),                                                                                                                                                                                        _
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),                                                                                                                                                                                                  _
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),                                                                                                                                                                                                  _
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),                                                                                                                                                                                                  _
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0),                                                                                                                                                                                                  _
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0))

Dim DOF_Flashers:DOF_Flashers = Array(dLowerLFlasher, dUpperLLFlasher, dUpperLFlasher, dUpperRFlasher, dUpperRRFlasher, dLowerRFlasher)
Dim DOF_FlasherLights:DOF_FlasherLights = Array(flasher6, flasher1, flasher2, flasher3, flasher4, flasher5)

Sub QueueFlasherAnimation(animation)
    AnimationCurrent = animation:
    FlasherDOF_Clear:AnimIdx = 0:FlasherDOF.Interval = 150:FlasherDOF.Enabled = True:AnimationRunning = True
End Sub
Sub StopFlasherAnimation:FlasherDOF.Enabled = False:FlasherDOF_Clear:AnimationRunning = False:End Sub
Sub FlasherDOF_Clear:Dim i:For each i in DOF_Flashers:DOF i, 0:Next:End Sub

Dim AnimIdx, AnimationCurrent, AnimationRunning
Sub FlasherDOF_Timer
    Dim aptr, tmp
    For aptr = 0 to 5
        tmp = AnimationCurrent(aptr)
        DOF_FlasherLights(aptr).State = tmp(AnimIdx)
        DOF DOF_Flashers(aptr), tmp(AnimIdx)
    Next
    AnimIdx = AnimIdx + 1
    If AnimIdx> uBound(AnimationCurrent(0))Then StopFlasherAnimation
End Sub

Sub swShooterLane_Hit:DOF dBallInShooterLane, 1:End Sub
Sub swShooterLane_UnHit:DOF dBallInShooterLane, 0:End Sub
Sub Gate1_Hit():DOF dShooterLaneGate, 2:End Sub
Sub Gate3_Hit():DOF dTreasureRampGate, 2:End Sub

'***********************************************************************************
'****				       		DOF reference	 	     				    	****
'***********************************************************************************
Const dBallRelease = 101         ' ball release
Const dFlipperLeft = 102         ' flipper left
Const dFlipperRight = 103        ' flipper right
Const dSlingLeft = 104           ' slingshot left hit
Const dSlingRight = 105          ' slingshot right hit
Const dKnocker = 106             ' knocker fired
Const dBallInShooterLane = 111   ' ball in shooterlane
Const dShooterLaneGate = 112     ' shooterlane gate hit
Const dLeftInlane = 113          ' left inlane right
Const dLeftInlane1 = 114         ' left inlane left
Const dLeftOutlane = 115         ' reft outlane
Const dRightInlane = 116         ' right inlane
Const dRightOutlane = 117        ' right outlane
Const dAutoPlunger = 118         ' autoplunger in shooterlane
Const dAutoPlunger2 = 119        ' autoplunger in left outlane
Const dKickerRight = 120         ' right lower kicker
Const dDropTargetR1 = 121        ' lower left droptarget 1 (left)
Const dDropTargetR2 = 122        ' lower left droptarget 2 (center)
Const dDropTargetR3 = 123        ' lower left droptarget 3 (right)
Const dDropTargetR_Reset = 124   ' reset (raise) lower left droptargets
Const dDropTargetA1 = 125        ' upper left droptarget 1 down (in treasure ramp)
Const dDropTargetA2 = 125        ' upper left droptarget 2 down (in treasure ramp)
Const dDropTargetA_Reset = 126   ' reset (raise) upper left droptargets
Const dTargetN1 = 127            ' right red target 1
Const dTargetN2 = 128            ' right red target 2
Const dTreasureRampGate = 129    ' gate at the end of the treasure ramp
Const dSpinner = 130             ' left spinner
Const dTargetA = 131             ' top target left (near castle)
Const dTargetB = 132             ' top target right (near castle)
Const dDTargetA1 = 133           ' top droptarget Voloagia (attention -> only hit event)
Const dDTargetA2 = 134           ' top droptarget Ghoma (attention -> only hit event)
Const dDTargetA3 = 135           ' top droptarget Bongo (attention -> only hit event)
Const dDTargetA4 = 136           ' top droptarget Barinade (attention -> only hit event)
Const dDTargetA5 = 137           ' top droptarget Dodongo (attention -> only hit event)
Const dDTargetG1 = 138           ' top droptarget Ganondorf (attention -> only hit event)
Const dDTargetA_Change = 139     ' any status change (drop or raise) on the dDTargetA1-dDTargetA5 and dDTargetG1 drop targets
Const dTreasureRampTrigger = 140 ' star trigger in treasure ramp (purple) hit
Const dShooterLaneTrigger = 141  ' star trigger in shooter lane (green) hit
Const dRightCastleTrigger = 142  ' star trigger in right castle outlane (red) hit
Const dLeftCastleTrigger = 143   ' star trigger in left castle outlane (red) hit
Const dBehindCastleTrigger = 144 ' star trigger behind the castle (hidden - not visible) hit
Const dBlueTrigger1 = 145        ' most left blue star trigger in fron of catsle hit
Const dBlueTrigger2 = 146        ' blue star trigger in fron of catsle hit
Const dBlueTrigger3 = 147        ' blue star trigger in fron of catsle hit
Const dBlueTrigger4 = 148        ' blue star trigger in fron of catsle hit
Const dBlueTrigger5 = 149        ' blue star trigger in fron of catsle hit
Const dBlueTrigger6 = 150        ' most right blue star trigger in fron of catsle hit
Const dCastleGate = 161          ' popup gate at castle entry (fires on any status change)
Const dCastleDiverter = 162      ' diverter inside the castle (fires on any status change)
Const dCaptiveBall = 163         ' captive ball hit
Const dcbTarget = 164            ' target at the end of the captive ball lane hit
Const dCastleKickerRight = 165   ' Kicker right to the castle (kicks ball into right wire track)
Const dCastleKickerLeft = 166    ' Kicker left inside the castle (kicks ball into left wire track)
Const dSecretPassageFound = 170  ' kicker 6 hit -> displays "Secret Passage Found" on the DMD
Const dMultiBall = 171           ' multiball mode has started
Const dCreditButton = 172        ' credit buttons needs to be switched on
Const dCreditButtonBlink = 173   ' credit Button needs to blink
Const dStartButton = 174         ' start Button needs to be switched on
Const dStartButtonBlink = 175    ' start Button needs to blink
Const dJackpot = 176             ' Jackpot is payed
Const dUpperLLFlasher = 201      ' upper outer left flasher (red)
Const dUpperLFlasher = 202       ' upper center left flasher (green)
Const dUpperRFlasher = 203       ' upper center right flasher (red)
Const dUpperRRFlasher = 204      ' upper outer right flasher (green)
Const dLowerRFlasher = 205       ' lower right flasher (red)
Const dLowerLFlasher = 206       ' lower left flasher (green)