' ****************************************************************
'                       VISUAL PINBALL X
'             JPSalas GhostBusters Slimer Pinball Script
'                         Version 2.0.3
' ****************************************************************
'DOF by Arngrim

Option Explicit
Randomize

Const BallSize = 50    ' 50 is the normal size
Const BallMass = 1     ' 1 is normal ball
Const SongVolume = 0.1 ' 1 is full volume. Value is from 0 to 1

' Load the core.vbs for supporting Subs and functions
LoadCoreFiles

Sub LoadCoreFiles
    On Error Resume Next
    ExecuteGlobal GetTextFile("core.vbs")
    If Err Then MsgBox "Can't open core.vbs"
    ExecuteGlobal GetTextFile("controller.vbs")
    If Err Then MsgBox "You need the controller.vbs in order to run this table, available in the vp10 package"
    On Error Goto 0
End Sub

' Define any Constants
Const cGameName = "jp_ghostbusters"
Const TableName = "slimer"
Const myVersion = "2.0.3"
Const MaxPlayers = 4
Const BallSaverTime = 15 'in seconds
Const MaxMultiplier = 12 'limit to 12x in this game
Const BallsPerGame = 3   ' 3 or 5
Const MaxMultiballs = 5  ' max number of balls during multiballs

' Define Global Variables
Dim PlayersPlayingGame
Dim CurrentPlayer
Dim Credits
Dim BonusPoints(4)
Dim BonusHeldPoints(4)
Dim BonusMultiplier(4)
Dim bBonusHeld
Dim BallsRemaining(4)
Dim ExtraBallsAwards(4)
Dim Score(4)
Dim HighScore(4)
Dim HighScoreName(4)
Dim Jackpot
Dim SuperJackpot
Dim Tilt
Dim TiltSensitivity
Dim Tilted
Dim TotalGamesPlayed
Dim mBalls2Eject
Dim SkillshotValue
Dim bAutoPlunger
Dim bInstantInfo

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
Dim bSkillShotPlayedOnce
Dim bExtraBallWonThisBall
Dim bJustStarted

Dim plungerIM 'used mostly as an autofire plunger
Dim ttable, cbleft, cbright
Dim x

' *********************************************************************
'                Visual Pinball Defined Script Events
' *********************************************************************

Sub Table1_Init()
    LoadEM
    Dim i
    Randomize

    'Impulse Plunger as autoplunger
    Const IMPowerSetting = 45 ' Plunger Power
    Const IMTime = 1.1        ' Time in seconds for Full Plunge
    Set plungerIM = New cvpmImpulseP
    With plungerIM
        .InitImpulseP swplunger, IMPowerSetting, IMTime
        .Random 1.5
        .InitExitSnd SoundFX("fx_kicker", DOFContactors), SoundFX("fx_solenoid", DOFContactors)
        .CreateEvents "plungerIM"
    End With

    Set cbLeft = New cvpmCaptiveBall
    With cbLeft
        .InitCaptive CapTrigger2, CapWall2, Array(CapKicker2, CapKicker2a), 330
        .NailedBalls = 1
        .ForceTrans = 1.4
        .MinForce = 3.5
        .CreateEvents "cbLeft"
        .Start
    End With
    CapKicker2.CreateBall

    Set cbRight = New cvpmCaptiveBall
    With cbRight
        .InitCaptive CapTrigger1, CapWall1, Array(CapKicker1, CapKicker1a), 0
        .NailedBalls = 1
        .ForceTrans = .9
        .MinForce = 3.5
        .CreateEvents "cbRight"
        .Start
    End With
    CapKicker1.CreateBall

    ' Misc. VP table objects Initialisation, droptargets, animations...
    VPObjects_Init

    'load saved values, highscore, names, jackpot
    Loadhs

    'Init main variables

    ' initalise the DMD display
    DMD_Init

    ' freeplay or coins
    bFreePlay = False ' coins yes or no?

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
    bBonusHeld = False
    bJustStarted = True
    bInstantInfo = False
    EndOfGame()
End Sub

'******
' Keys
'******

Sub Table1_KeyDown(ByVal Keycode)
    If Keycode = AddCreditKey Then
        Credits = Credits + 1
        DOF 140, DOFOn
        If(Tilted = False)Then
            DMDFlush
            DMD "_", CL(1, "CREDITS: " & Credits), "", eNone, eNone, eNone, 500, True, "fx_coin"
            If NOT bGameInPlay Then ShowTableInfo
        End If
    End If

    If keycode = PlungerKey Then
        Plunger.Pullback
    End If

    If keycode = LeftTiltKey Then Nudge 90, 6:PlaySound "fx_nudge", 0, 1, -0.1, 0.25:CheckTilt
    If keycode = RightTiltKey Then Nudge 270, 6:PlaySound "fx_nudge", 0, 1, 0.1, 0.25:CheckTilt
    If keycode = CenterTiltKey Then Nudge 0, 7:PlaySound "fx_nudge", 0, 1, 1, 0.25:CheckTilt

    If hsbModeActive Then
        EnterHighScoreKey(keycode)
        Exit Sub
    End If

    ' Table specific

    If bSkillshotSelect Then
        SelectSkillshot(keycode)
    'Exit Sub
    End If

    ' Normal flipper action

    If bGameInPlay AND NOT Tilted Then
        If keycode = LeftFlipperKey Then SolLFlipper 1:InstantInfoTimer.Enabled = True
        If keycode = RightFlipperKey Then SolRFlipper 1:InstantInfoTimer.Enabled = True

        If keycode = StartGameKey Then
            If((PlayersPlayingGame < MaxPlayers)AND(bOnTheFirstBall = True))Then

                If(bFreePlay = True)Then
                    PlayersPlayingGame = PlayersPlayingGame + 1
                    TotalGamesPlayed = TotalGamesPlayed + 1
                    DMDFlush
                    DMD "_", CL(1, PlayersPlayingGame & " PLAYERS"), "", eNone, eBlink, eNone, 500, True, "gb_fanfare1"
                Else
                    If(Credits > 0)then
                        PlayersPlayingGame = PlayersPlayingGame + 1
                        TotalGamesPlayed = TotalGamesPlayed + 1
                        Credits = Credits - 1
                        DMD "_", CL(1, PlayersPlayingGame & " PLAYERS"), "", eNone, eBlink, eNone, 500, True, "gb_fanfare1"
                    Else
                        ' Not Enough Credits to start a game.
                        DOF 140, DOFOff
                        DMDFlush
                        DMD CL(0, "CREDITS " & Credits), CL(1, "INSERT COIN"), "", eNone, eBlink, eNone, 500, True, ""
                    End If
                End If
            End If
        End If
        Else ' If (GameInPlay)

            If keycode = StartGameKey Then
                If(bFreePlay = True)Then
                    If(BallsOnPlayfield = 0)Then
                        ResetForNewGame()
                    End If
                Else
                    If(Credits > 0)Then
                        If(BallsOnPlayfield = 0)Then
                            Credits = Credits - 1
                            ResetForNewGame()
                        End If
                    Else
                        ' Not Enough Credits to start a game.
                        DOF 140, DOFOff
                        DMDFlush
                        DMD CL(0, "CREDITS " & Credits), CL(1, "INSERT COIN"), "", eNone, eBlink, eNone, 500, True, ""
                        ShowTableInfo
                    End If
                End If
            End If
    End If ' If (GameInPlay)
End Sub

Sub Table1_KeyUp(ByVal keycode)

    If keycode = PlungerKey Then
        Plunger.Fire
    End If

    If hsbModeActive Then
		InstantInfoTimer.Enabled = False
		bInstantInfo = False
        Exit Sub
    End If

    ' Table specific

    If bGameInPLay AND NOT Tilted Then
        If keycode = LeftFlipperKey Then
            SolLFlipper 0
            InstantInfoTimer.Enabled = False
            If bInstantInfo Then
                bInstantInfo = False
                DMDScoreNow
            End If
        End If
        If keycode = RightFlipperKey Then
            SolRFlipper 0
            InstantInfoTimer.Enabled = False
            If bInstantInfo Then
                bInstantInfo = False
                DMDScoreNow
            End If
        End If
    End If
End Sub

Sub InstantInfoTimer_Timer
    InstantInfoTimer.Enabled = False
    bInstantInfo = True
    Jackpot = 1000000 + Round(Score(CurrentPlayer) / 10, 0)
    DMD CL(0, "INSTANT INFO"), "", "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "JACKPOT VALUE"), CL(1, FormatScore(Jackpot)), "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "SUPERJACKPOT"), CL(1, FormatScore(SuperJackpot)), "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "PKE LEVEL"), CL(1, FormatScore(PKELevel)), "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "PKE MULTIPLIER"), CL(1, PKEMult), "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "GHOSTS CAUGHT"), CL(1, Ghosts(CurrentPlayer) + GhostsHundreds(CurrentPlayer) * 100), "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "BONUS MULTIPLIER"), CL(1, BonusMultiplier(CurrentPlayer)), "", eNone, eNone, eNone, 800, False, ""
    DMD CL(0, "FIRESTATION HIT"), CL(1, FormatScore(SpinnerValue * SpinnerLevel)), "", eNone, eNone, eNone, 800, False, ""
End Sub

Sub EndFlipperStatus
    If bInstantInfo Then
        bInstantInfo = False
        DMDScoreNow
    End If
End Sub

'*************
' Pause Table
'*************

Sub table1_Paused
End Sub

Sub table1_unPaused
End Sub

Sub table1_Exit
    Savehs
	If Not FlexDMD is Nothing Then FlexDMD.Run = False
End Sub

'********************
'     Flippers
'********************

Sub SolLFlipper(Enabled)
    If Enabled Then
        PlaySoundAt SoundFXDOF("fx_flipperup", 101, DOFOn, DOFFlippers), LeftFlipper
        LeftFlipper.RotateToEnd
        If bSkillshotReady = False AND bSkillshotSelect = False Then
            RotateLaneLightsLeft
        End If
    Else
        PlaySoundAt SoundFXDOF("fx_flipperdown", 101, DOFOff, DOFFlippers), LeftFlipper
        LeftFlipper.RotateToStart
    End If
End Sub

Sub SolRFlipper(Enabled)
    If Enabled Then
        PlaySoundAt SoundFXDOF("fx_flipperup", 102, DOFOn, DOFFlippers), RightFlipper
        RightFlipper.RotateToEnd
        If bSkillshotReady = False AND bSkillshotSelect = False Then
            RotateLaneLightsRight
        End If
    Else
        PlaySoundAt SoundFXDOF("fx_flipperdown", 102, DOFOff, DOFFlippers), RightFlipper
        RightFlipper.RotateToStart
    End If
End Sub

' flippers hit Sound

Sub LeftFlipper_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall)
End Sub

Sub RightFlipper_Collide(parm)
    PlaySound "fx_rubber_flipper", 0, parm / 10, pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall)
End Sub

Sub RotateLaneLightsLeft()
    Dim tmp
    tmp = l28.State
    l28.state = l29.State:l28b.State = l29.State:Bumper1Light.Visible = l29.State
    l29.State = l30.State:l29b.State = l30.State:Bumper2Light.Visible = l30.State
    l30.State = tmp:l30b.State = tmp:Bumper3Light.Visible = tmp
End Sub

Sub RotateLaneLightsRight()
    Dim tmp
    tmp = l30.State
    l30.State = l29.State:l30b.State = l29.State:Bumper3Light.Visible = l29.State
    l29.State = l28.State:l29b.State = l28.State:Bumper2Light.Visible = l28.State
    l28.State = tmp:l28b.State = tmp:Bumper1Light.Visible = tmp
End Sub

'*********
' TILT
'*********

'NOTE: The TiltDecreaseTimer Subtracts .01 from the "Tilt" variable every round

Sub CheckTilt                                    'Called when table is nudged
    If NOT bGameInPlay Then Exit Sub
    Tilt = Tilt + TiltSensitivity                'Add to tilt count
    TiltDecreaseTimer.Enabled = True
    If(Tilt > TiltSensitivity)AND(Tilt < 15)Then 'show a warning
        DMD "_", CL(1, "CAREFUL!"), "", eNone, eBlinkFast, eNone, 500, True, ""
    End if
    If Tilt > 15 Then 'If more that 15 then TILT the table
        Tilted = True
        'display Tilt
        DMDFlush
        DMD "", CL(1, "TILT"), "", eNone, eBlink, eNone, 100, False, ""
        DMD CL(0, "TILT"), "", "", eNone, eBlink, eNone, 100, False, ""
        DisableTable True
        TiltRecoveryTimer.Enabled = True 'start the Tilt delay to check for all the balls to be drained
    End If
End Sub

Sub TiltDecreaseTimer_Timer
    ' DecreaseTilt
    If Tilt > 0 Then
        Tilt = Tilt - 0.1
    Else
        TiltDecreaseTimer.Enabled = False
    End If
End Sub

Sub DisableTable(Enabled)
    If Enabled Then
        'turn off GI and turn off all the lights
        GiOff
        LightSeqTilt.Play SeqAllOff
        'Disable slings, bumpers etc
        LeftFlipper.RotateToStart
        RightFlipper.RotateToStart
        'Bumper1.Force = 0

        LeftSlingshot.Disabled = 1
        RightSlingshot.Disabled = 1
    Else
        'turn back on GI and the lights
        'GiOn
        LightSeqTilt.StopPlay
        'Bumper1.Force = 6
        LeftSlingshot.Disabled = 0
        RightSlingshot.Disabled = 0
        'clean up the buffer display
        DMDFlush
    End If
End Sub

Sub TiltRecoveryTimer_Timer()
    ' if all the balls have been drained then..
    If(BallsOnPlayfield = 0)Then
        ' do the normal end of ball thing (this doesn't give a bonus if the table is tilted)
        EndOfBall()
        TiltRecoveryTimer.Enabled = False
    End If
' else retry (checks again in another second or so)
End Sub

'********************
' Music as wav sounds
'********************

Dim Song
Song = ""

Sub PlaySong(name)
    If bMusicOn Then
        If Song <> name Then
            StopSound Song
            Song = name
            If Song = "m_end" Then
                PlaySound Song, 0, SongVolume  'this last number is the volume, from 0 to 1
            Else
                PlaySound Song, -1, SongVolume 'this last number is the volume, from 0 to 1
            End If
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
OldGiState = -1   'start witht the Gi off

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
        If UBound(tmp) = 3 Then 'we have 4 captive balls on the table (-1 means no balls, 0 is the first ball, 1 is the second..)
            GiOff               ' turn off the gi if no active balls on the table, we could also have used the variable ballsonplayfield.
        Else
            Gion
        End If
    End If
End Sub

Sub GiOn
    DOF 126, DOFOn
    Dim bulb
    For each bulb in aGiLights
        bulb.State = 1
    Next
    Slimer1.opacity = 2000
    Slimer2.opacity = 2000
    Slimer3.opacity = 2000
    Slimer4.opacity = 2000
End Sub

Sub GiOff
    DOF 126, DOFOff
    Dim bulb
    For each bulb in aGiLights
        bulb.State = 0
    Next
    Slimer1.opacity = 200
    Slimer2.opacity = 200
    Slimer3.opacity = 200
    Slimer4.opacity = 200
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
            FlashForms BackFlasher1, 2000, 40, 0:DOF 204, DOFPulse
            FlashForms BackFlasher2, 2000, 40, 0:DOF 217, DOFPulse
            FlashForms BackFlasher3, 2000, 40, 0:DOF 221, DOFPulse
            FlashForms BackFlasher4, 2000, 40, 0:DOF 201, DOFPulse
            FlashForms LeftRampDome1, 2000, 40, 0
            FlashForms LeftRampDome2, 2000, 40, 0
            DOF 209, DOFPulse
            FlashForms StayPuftFlasher, 2000, 40, 0
            FlashForms ectoFlasher, 2000, 40, 0
            FlashForms LibraryFlasher, 2000, 40, 0
            DOF 207, DOFPulse
            FlashForms BooksFlasher, 2000, 40, 0
            FlashForms ContainerFlasher, 2000, 40, 0:DOF 203, DOFPulse
            FlashForms Protonflasher, 2000, 40, 0
            FlashForms l5f, 2000, 40, 0:FlashForms l5, 2000, 40, 0
            FlashForms l6f, 2000, 40, 0:FlashForms l6, 2000, 40, 0
            FlashForMs FirestationFlasher1, 2000, 40, 0:FlashForMs FirestationFlasher2, 2000, 40, 0
            FlashForMs l20, 2000, 40, 0:FlashForMs l20f, 2000, 40, 0
            FlashForMs l21, 2000, 40, 0:FlashForMs l21f, 2000, 40, 0
            FlashForMs l22, 2000, 40, 0:FlashForMs l22f, 2000, 40, 0
            FlashForMs l23, 2000, 40, 0:FlashForMs l23f, 2000, 40, 0
            FlashEffectTimer.Enabled = 0
        Case 2
            Select Case INT(RND * 14)
                Case 0:FlashForms LeftRampDome1, 1000, 40, 0
                Case 1:FlashForms LeftRampDome2, 1000, 40, 0
                    DOF 210, DOFPulse
                Case 2:FlashForms l5f, 1000, 40, 0:FlashForms l5, 1000, 40, 0
                Case 3:FlashForms l6f, 1000, 40, 0:FlashForms l6, 1000, 40, 0
                Case 4:FlashForms StayPuftFlasher, 1000, 40, 0
                Case 5:FlashForms ectoFlasher, 1000, 40, 0
                Case 6:FlashForms LibraryFlasher, 1000, 40, 0
                    DOF 206, DOFPulse
                Case 7:FlashForms BooksFlasher, 1000, 40, 0
                Case 8:FlashForms ContainerFlasher, 1000, 40, 0:DOF 204, DOFPulse
                Case 9:FlashForms Protonflasher, 1000, 40, 0
                Case 10:FlashForms l20, 1000, 40, 0:FlashForms l20f, 1000, 40, 0:FlashForms l21, 1000, 40, 0:FlashForms l21f, 1000, 40, 0
                Case 11:FlashForms l22, 1000, 40, 0:FlashForms l22f, 1000, 40, 0:FlashForms l23, 1000, 40, 0:FlashForms l23f, 1000, 40, 0
                Case 12:FlashForMs BumperFlash, 400, 40, 0:FlashForMs BumperFlash1, 400, 40, 0
                Case 13:FlashForMs FirestationFlasher1, 1000, 40, 0:FlashForMs FirestationFlasher2, 1000, 40, 0
            End Select
            If FEStep = 20 then FlashEffectTimer.Enabled = 0
        Case 3
            Select case FEStep
                Case 0:FlashForms LeftRampDome1, 250, 40, 0:FlashForms LeftRampDome2, 250, 40, 0:DOF 211, DOFPulse:FlashForms StayPuftFlasher, 250, 40, 0
                Case 1:FlashForms ectoFlasher, 250, 40, 0:FlashForms LibraryFlasher, 250, 40, 0:DOF 208, DOFPulse:FlashForms BooksFlasher, 250, 40, 0
                Case 2:FlashForms ContainerFlasher, 250, 40, 0:DOF 205, DOFPulse:FlashForms Protonflasher, 250, 40, 0
                Case 3:FlashForms l5f, 250, 40, 0:FlashForms l5, 250, 40, 0:FlashForms l6f, 250, 40, 0:FlashForms l6, 250, 40, 0
                Case 4:FlashForms LeftRampDome1, 250, 40, 0:FlashForms LeftRampDome2, 250, 40, 0:DOF 211, DOFPulse:FlashForms StayPuftFlasher, 250, 40, 0
                Case 5:FlashForms ectoFlasher, 250, 40, 0:FlashForms LibraryFlasher, 250, 40, 0:DOF 208, DOFPulse:FlashForms BooksFlasher, 250, 40, 0
                Case 6:FlashForms ContainerFlasher, 250, 40, 0:DOF 205, DOFPulse:FlashForms Protonflasher, 250, 40, 0
                Case 7:FlashForms l5f, 250, 40, 0:FlashForms l5, 250, 40, 0:FlashForms l6f, 250, 40, 0:FlashForms l6, 250, 40, 0
                Case 8:FlashForms LeftRampDome1, 250, 40, 0:FlashForms LeftRampDome2, 250, 40, 0:DOF 211, DOFPulse:FlashForms StayPuftFlasher, 250, 40, 0
                Case 9:FlashForms ectoFlasher, 250, 40, 0:FlashForms LibraryFlasher, 250, 40, 0:DOF 208, DOFPulse:FlashForms BooksFlasher, 250, 40, 0
                Case 10:FlashForms ContainerFlasher, 250, 40, 0:DOF 205, DOFPulse:FlashForms Protonflasher, 250, 40, 0
                Case 11:FlashForms l5f, 250, 40, 0:FlashForms l5, 250, 40, 0:FlashForms l6f, 250, 40, 0:FlashForms l6, 250, 40, 0
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
            FlashForms BackFlasher1, 1500, 40, 0:DOF 203, DOFPulse
            FlashForms BackFlasher2, 1500, 40, 0:DOF 218, DOFPulse
            FlashForms BackFlasher3, 1500, 40, 0:DOF 221, DOFPulse
            FlashForms BackFlasher4, 1500, 40, 0:DOF 200, DOFPulse
            BFlashEffectTimer.Enabled = 0
        Case 2
            Select Case INT(RND * 4)
                Case 0:FlashForms BackFlasher1, 500, 40, 0:DOF 215, DOFPulse
                Case 1:FlashForms BackFlasher2, 500, 40, 0:DOF 219, DOFPulse
                Case 2:FlashForms BackFlasher3, 500, 40, 0:DOF 223, DOFPulse
                Case 3:FlashForms BackFlasher4, 500, 40, 0:DOF 213, DOFPulse
            End Select
            If BEStep = 20 then BFlashEffectTimer.Enabled = 0
        Case 3
            Select case BEStep
                Case 0:FlashForms BackFlasher1, 200, 40, 0:DOF 216, DOFPulse
                Case 1:FlashForms BackFlasher2, 200, 40, 0:DOF 220, DOFPulse
                Case 2:FlashForms BackFlasher3, 200, 40, 0:DOF 224, DOFPulse
                Case 3:FlashForms BackFlasher4, 200, 40, 0:DOF 214, DOFPulse
                Case 4:FlashForms BackFlasher1, 200, 40, 0:DOF 216, DOFPulse
                Case 5:FlashForms BackFlasher2, 200, 40, 0:DOF 220, DOFPulse
                Case 6:FlashForms BackFlasher3, 200, 40, 0:DOF 224, DOFPulse
                Case 7:FlashForms BackFlasher4, 200, 40, 0:DOF 214, DOFPulse
                Case 8:FlashForms BackFlasher1, 200, 40, 0:DOF 216, DOFPulse
                Case 9:FlashForms BackFlasher2, 200, 40, 0:DOF 220, DOFPulse
                Case 10:FlashForms BackFlasher3, 200, 40, 0:DOF 224, DOFPulse
                Case 11:FlashForms BackFlasher4, 200, 40, 0:DOF 214, DOFPulse
                Case 12:BFlashEffectTimer.Enabled = 0
            End Select
        Case 4
            Select case BEStep
                Case 0:FlashForms BackFlasher4, 200, 40, 0:DOF 214, DOFPulse
                Case 1:FlashForms BackFlasher3, 200, 40, 0:DOF 224, DOFPulse
                Case 2:FlashForms BackFlasher2, 200, 40, 0:DOF 220, DOFPulse
                Case 3:FlashForms BackFlasher1, 200, 40, 0:DOF 216, DOFPulse
                Case 4:FlashForms BackFlasher4, 200, 40, 0:DOF 214, DOFPulse
                Case 5:FlashForms BackFlasher3, 200, 40, 0:DOF 224, DOFPulse
                Case 6:FlashForms BackFlasher2, 200, 40, 0:DOF 220, DOFPulse
                Case 7:FlashForms BackFlasher1, 200, 40, 0:DOF 216, DOFPulse
                Case 8:FlashForms BackFlasher4, 200, 40, 0:DOF 214, DOFPulse
                Case 9:FlashForms BackFlasher3, 200, 40, 0:DOF 224, DOFPulse
                Case 10:FlashForms BackFlasher2, 200, 40, 0:DOF 220, DOFPulse
                Case 11:FlashForms BackFlasher1, 200, 40, 0:DOF 216, DOFPulse
                Case 12:BFlashEffectTimer.Enabled = 0
            End Select
    End Select
    BEStep = BEStep + 1
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
    If tmp > 0 Then
        Pan = Csng(tmp ^10)
    Else
        Pan = Csng(-((- tmp) ^10))
    End If
End Function

Function Pitch(ball) ' Calculates the pitch of the sound based on the ball speed
    Pitch = BallVel(ball) * 20
End Function

Function BallVel(ball) 'Calculates the ball speed
    BallVel = (SQR((ball.VelX ^2) + (ball.VelY ^2)))
End Function

Function AudioFade(ball) 'only on VPX 10.4 and newer
    Dim tmp
    tmp = ball.y * 2 / Table1.height-1
    If tmp > 0 Then
        AudioFade = Csng(tmp ^10)
    Else
        AudioFade = Csng(-((- tmp) ^10))
    End If
End Function

Sub PlaySoundAt(soundname, tableobj) 'play sound at X and Y position of an object, mostly bumpers, flippers and other fast objects
    PlaySound soundname, 0, 1, Pan(tableobj), 0, 0, 0, 0, AudioFade(tableobj)
End Sub

Sub PlaySoundAtBall(soundname) ' play a sound at the ball position, like rubbers, targets
    PlaySound soundname, 0, Vol(ActiveBall), pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0, AudioFade(ActiveBall)
End Sub

'********************************************
'   JP's VP10 Rolling Sounds + Ballshadow
' uses a collection of shadows, aBallShadow
'********************************************

Const tnob = 20 ' total number of balls
Const lob = 4   'number of locked balls
ReDim rolling(tnob)
InitRolling

Sub InitRolling
    Dim i
    For i = 0 to tnob
        rolling(i) = False
    Next
End Sub

Sub RollingUpdate()
    Dim BOT, b, ballpitch, ballvol
    BOT = GetBalls

    ' stop the sound of deleted balls
    For b = UBound(BOT) + 1 to tnob
        rolling(b) = False
        StopSound("fx_ballrolling" & b)
    Next

    ' exit the sub if no balls on the table
    If UBound(BOT) = lob - 1 Then Exit Sub 'there no extra balls on this table

    ' play the rolling sound for each ball and draw the shadow
    For b = lob to UBound(BOT)

		aBallShadow(b).X = BOT(b).X
		aBallShadow(b).Y = BOT(b).Y

        If BallVel(BOT(b) )> 1 Then
            If BOT(b).z <30 Then
                ballpitch = Pitch(BOT(b) )
                ballvol = Vol(BOT(b) )
            Else
                ballpitch = Pitch(BOT(b) ) + 25000 'increase the pitch on a ramp
                ballvol = Vol(BOT(b) ) * 10
            End If
            rolling(b) = True
            PlaySound("fx_ballrolling" & b), -1, ballvol, Pan(BOT(b) ), 0, ballpitch, 1, 0, AudioFade(BOT(b) )
        Else
            If rolling(b) = True Then
                StopSound("fx_ballrolling" & b)
                rolling(b) = False
            End If
        End If
        ' rothbauerw's Dropping Sounds
        If BOT(b).VelZ < -1 and BOT(b).z < 55 and BOT(b).z > 27 Then 'height adjust for ball drop sounds
            PlaySound "fx_balldrop", 0, ABS(BOT(b).velz)/17, Pan(BOT(b)), 0, Pitch(BOT(b)), 1, 0, AudioFade(BOT(b))
        End If
    Next
End Sub
'**********************
' Ball Collision Sound
'**********************

Sub OnBallBallCollision(ball1, ball2, velocity)
    PlaySound("fx_collide"), 0, Csng(velocity) ^2 / 2000, Pan(ball1), 0, Pitch(ball1), 0, 0, AudioFade(ball1)
End Sub

'******************************
' Diverse Collection Hit Sounds
'******************************

Sub aMetals_Hit(idx):PlaySoundAtBall "fx_MetalHit":End Sub
Sub aRubber_Bands_Hit(idx):PlaySoundAtBall "fx_rubber_band":End Sub
Sub aRubber_Posts_Hit(idx):PlaySoundAtBall "fx_rubber_post":End Sub
Sub aRubber_Pins_Hit(idx):PlaySoundAtBall "fx_rubber_pin":End Sub
Sub aYellowPins_Hit(idx):PlaySoundAtBall "fx_rubber_pin":End Sub
Sub aPlastics_Hit(idx):PlaySoundAtBall "fx_PlasticHit":End Sub
Sub aGates_Hit(idx):PlaySoundAtBall "fx_Gate":End Sub
Sub aWoods_Hit(idx):PlaySoundAtBall "fx_Woodhit":End Sub
Sub aCaptiveWalls_Hit(idx):PlaySoundAtBall "fx_collide":End Sub

' Some quotes from the 2 first movies

Sub PlayQuote_timer() 'one quote each 2 minutes
    Dim Quote
    Quote = "gb_quote" & INT(RND * 56) + 1
    PlaySound Quote
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
        BonusHeldPoints(i) = 0
        BonusMultiplier(i) = 1
        BallsRemaining(i) = BallsPerGame
        ExtraBallsAwards(i) = 0
    Next

    ' initialise any other flags
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

    BonusPoints(CurrentPlayer) = 0
    bBonusHeld = False
    bExtraBallWonThisBall = False
    ResetNewBallLights()
    'Reset any table specific
    ResetNewBallVariables

    'This is a new ball, so activate the ballsaver
    bBallSaverReady = True

    'and the skillshot
    bSkillShotPlayedOnce = False
    bSkillShotReady = True

'Change the music ?
End Sub

' Create a new ball on the Playfield

Sub CreateNewBall()
    ' create a ball in the plunger lane kicker.
    BallRelease.CreateSizedball BallSize / 2

    ' There is a (or another) ball on the playfield
    BallsOnPlayfield = BallsOnPlayfield + 1

    ' kick it out..
    PlaySoundAt SoundFXDOF("fx_Ballrel", 121, DOFPulse, DOFContactors), BallRelease
    BallRelease.Kick 90, 4

' if there is 2 or more balls then set the multibal flag (remember to check for locked balls and other balls used for animations)
' set the bAutoPlunger flag to kick the ball in play automatically
    If BallsOnPlayfield > 1 Then
        DOF 129, DOFPulse
        bMultiBallMode = True
        bAutoPlunger = True
        PlaySong "m_multiball"
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
        If BallsOnPlayfield < MaxMultiballs Then
            CreateNewBall()
            mBalls2Eject = mBalls2Eject -1
            If mBalls2Eject = 0 Then 'if there are no more balls to eject then stop the timer
                Me.Enabled = False
            End If
        Else 'the max number of multiballs is reached, so stop the timer
            mBalls2Eject = 0
            Me.Enabled = False
        End If
    End If
End Sub

' The Player has lost his ball (there are no more balls on the playfield).
' Handle any bonus points awarded

Sub EndOfBall()
    Dim AwardPoints, TotalBonus, ii
    AwardPoints = 0
    TotalBonus = 0
    ' the first ball has been lost. From this point on no new players can join in
    bOnTheFirstBall = False

    ' only process any of this if the table is not tilted.  (the tilt recovery
    ' mechanism will handle any extra balls or end of game)

    If(Tilted = False)Then

        ' Count the bonus. This table uses several bonus
        dmdflush

        'Lane Bonus
        AwardPoints = LaneBonus * 1000
        TotalBonus = AwardPoints
        DMD CL(0, FormatScore(AwardPoints)), CL(1, "LANE BONUS"), "", eBlink, eNone, eNone, 800, False, "gb_bonus1"

        'Number of modes completed
        AwardPoints = 0
        For ii = 1 to 9
            AwardPoints = AwardPoints + Modes(ii) ' the Modes() is 1 when completed
        Next
        AwardPoints = AwardPoints * 325130
        TotalBonus = TotalBonus + AwardPoints
        DMD CL(0, FormatScore(AwardPoints)), CL(1, "MODES COMPLETED"), "", eBlink, eNone, eNone, 800, False, "gb_bonus2"

        'Number of Ghosts Caught
        AwardPoints = (Ghosts(CurrentPlayer) + GhostsHundreds(CurrentPlayer) * 100) * 25130
        TotalBonus = TotalBonus + AwardPoints
        DMD CL(0, FormatScore(AwardPoints)), CL(1, "GHOSTS CAUGHT"), "", eBlink, eNone, eNone, 800, False, "gb_bonus3"

        'Amount of PKE Collected
        AwardPoints = PKELevel * 350
        TotalBonus = TotalBonus + AwardPoints
        DMD CL(0, FormatScore(AwardPoints)), CL(1, "PKE COLLECTED"), "", eBlink, eNone, eNone, 800, False, "gb_bonus4"

        ' handle the bonus held
        If bBonusHeld Then
            If Balls = BallsPerGame Then 'this is the last ball, so if bonus held is on then double the bonus
                TotalBonus = TotalBonus * BonusMultiplier(CurrentPlayer) * 2
            Else                         ' add the old bonus
                TotalBonus = TotalBonus * BonusMultiplier(CurrentPlayer) + BonusHeldPoints(CurrentPlayer)
            End If
            bBonusHeld = False
        End If
        DMD CL(0, FormatScore(TotalBonus)), CL(1, "TOTAL BONUS X " &BonusMultiplier(CurrentPlayer)), "", eBlink, eNone, eNone, 1000, False, "gb_bonus5"
        AddScore TotalBonus
        BonusHeldPoints(CurrentPlayer) = TotalBonus 'in case the player get the Bonus Held

        ' add a bit of a delay to allow for the bonus points to be shown & added up
        vpmtimer.addtimer 7000, "EndOfBall2 '"
    Else
        vpmtimer.addtimer 100, "EndOfBall2 '"
    End If
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
    If(ExtraBallsAwards(CurrentPlayer) <> 0)Then
        debug.print "Extra Ball"

        ' yep got to give it to them
        ExtraBallsAwards(CurrentPlayer) = ExtraBallsAwards(CurrentPlayer)- 1

        ' if no more EB's then turn off any shoot again light
        If(ExtraBallsAwards(CurrentPlayer) = 0)Then
            LightShootAgain.State = 0
        End If

        ' You may wish to do a bit of a song AND dance at this point
        DMD "_", CL(1, ("EXTRA BALL")), "_", eNone, eBlink, eNone, 1000, True, "vo_extraball"

        ' Create a new ball in the shooters lane
        CreateNewBall()
    Else ' no extra balls

        BallsRemaining(CurrentPlayer) = BallsRemaining(CurrentPlayer)- 1

        ' was that the last ball ?
        If(BallsRemaining(CurrentPlayer) <= 0)Then
            debug.print "No More Balls, High Score Entry"

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

    debug.print "EndOfBall - Complete"

    ' are there multiple players playing this game ?
    If(PlayersPlayingGame > 1)Then
        ' then move to the next player
        NextPlayer = CurrentPlayer + 1
        ' are we going from the last player back to the first
        ' (ie say from player 4 back to player 1)
        If(NextPlayer > PlayersPlayingGame)Then
            NextPlayer = 1
        End If
    Else
        NextPlayer = CurrentPlayer
    End If

    debug.print "Next Player = " & NextPlayer

    ' is it the end of the game ? (all balls been lost for all players)
    If((BallsRemaining(CurrentPlayer) <= 0)AND(BallsRemaining(NextPlayer) <= 0))Then
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

        ' play a sound if more than 1 player
        If PlayersPlayingGame > 1 Then
            PlaySound "vo_player" &CurrentPlayer
            DMD "", CL(1, "PLAYER " &CurrentPlayer), "", eNone, eNone, eNone, 800, True, ""
        End If
    End If
End Sub

' This function is called at the End of the Game, it should reset all
' Drop targets, AND eject any 'held' balls, start any attract sequences etc..

Sub EndOfGame()
    debug.print "End Of Game"
    bGameInPLay = False
    ' just ended your game then play the end of game tune
    If NOT bJustStarted Then
        PlaySong "m_end"
    End If
    bJustStarted = False
    ' ensure that the flippers are down
    SolLFlipper 0
    SolRFlipper 0

    ' terminate all modes - eject locked balls
    ' most of the modes/timers terminate at the end of the ball
    PlayQuote.Enabled = 0

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
    ' Destroy the ball
    Drain.DestroyBall
    ' Exit Sub ' only for debugging
    BallsOnPlayfield = BallsOnPlayfield - 1

    ' pretend to knock the ball into the ball storage mech
    PlaySoundAt "fx_drain",Drain
    'if Tilted the end Ball modes
    If Tilted Then
        StopEndOfBallModes
    End If

    ' if there is a game in progress AND it is not Tilted
    If(bGameInPLay = True)AND(Tilted = False)Then

        ' is the ball saver active,
        If(bBallSaverActive = True)Then

            ' yep, create a new ball in the shooters lane
            ' we use the Addmultiball in case the multiballs are being ejected
            AddMultiball 1
            ' we kick the ball with the autoplunger
            bAutoPlunger = True
            ' you may wish to put something on a display or play a sound at this point
            DMD "_", CL(1, "BALL SAVED"), "_", eNone, eBlinkfast, eNone, 800, True, ""
        Else
            ' cancel any multiball if on last ball (ie. lost all other balls)
            If(BallsOnPlayfield = 1)Then
                ' AND in a multi-ball??
                If(bMultiBallMode = True)then
                    ' not in multiball mode any more
                    bMultiBallMode = False
                    ChangeGi "white"
                    ' you may wish to change any music over at this point and
                    ' turn off any multiball specific lights
                    'ResetJackpotLights
                    EndSFM
                    EndMassHysteriaMultiball
                    If Modes(0) = 10 Then StopWeCameWeSaw
                    If Modes(0) = 11 Then StopWeAreReadyToBelieveYou
                    If Modes(0) = 12 Then StopAreYouaGod
                    PlaySong "m_main"
                End If
            End If

            ' was that the last ball on the playfield
            If(BallsOnPlayfield = 0)Then
                ' End Modes and timers
                PlaySong "m_wait"
                StopEndOfBallModes
                ChangeGi "white"
                ' handle the end of ball (count bonus, change player, high score entry etc..)
                vpmtimer.addtimer 1500, "EndOfBall '"
            End If
        End If
    End If
End Sub

' The Ball has rolled out of the Plunger Lane and it is pressing down the trigger in the shooters lane
' Check to see if a ball saver mechanism is needed and if so fire it up.

Sub swPlungerRest_Hit()
    debug.print "ball in plunger lane"
    ' some sound according to the ball position
    PlaySoundAt "fx_sensor", swPlungerRest
    bBallInPlungerLane = True
    ' turn on Launch light is there is one
    'LaunchLight.State = 2
    ' kick the ball in play if the bAutoPlunger flag is on
    If bAutoPlunger Then
        debug.print "autofire the ball"
        PlungerIM.AutoFire
        DOF 125, DOFPulse
        bAutoPlunger = False
    End If
    ' if there is a need for a ball saver, then start off a timer
    ' only start if it is ready, and it is currently not running, else it will reset the time period
    If(bBallSaverReady = True)AND(BallSaverTime <> 0)And(bBallSaverActive = False)Then
        EnableBallSaver BallSaverTime
    End If
    'Start the Selection of the skillshot if ready
    If bSkillShotReady Then
        bSkillShotSelect = True
        If bSkillShotPlayedOnce = False Then 'play the sound just once per game
            vpmtimer.addtimer 1500, "PlaySound ""vo_chooseskillshot"" '"
            bSkillShotPlayedOnce = True
        End If
        UpdateSkillshot()
    End If
    ' remember last trigger hit by the ball.
    LastSwitchHit = "swPlungerRest"
End Sub

' The ball is released from the plunger turn off some flags and check for skillshot

Sub swPlungerRest_UnHit()
    bBallInPlungerLane = False
    If bSkillShotReady Then
        LoopGateLeft.Open = True
        bSkillShotSelect = False
        ResetSkillShotTimer.Enabled = 1
    End If
    If NOT bMultiballMode Then
        PlaySong "m_main"
    End If
' turn off LaunchLight
'LaunchLight.State = 0
End Sub

Sub EnableBallSaver(seconds)
    debug.print "Ballsaver started"
    ' set our game flag
    bBallSaverActive = True
    bBallSaverReady = False
    ' start the timer
    BallSaverTimerExpired.Interval = 1000 * seconds
    BallSaverTimerExpired.Enabled = True
    BallSaverSpeedUpTimer.Interval = 1000 * seconds -(1000 * seconds) / 3
    BallSaverSpeedUpTimer.Enabled = True
    ' if you have a ball saver light you might want to turn it on at this point (or make it flash)
    LightShootAgain.BlinkInterval = 160
    LightShootAgain.State = 2
End Sub

' The ball saver timer has expired.  Turn it off AND reset the game flag
'
Sub BallSaverTimerExpired_Timer()
    debug.print "Ballsaver ended"
    BallSaverTimerExpired.Enabled = False
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
    If(Tilted = False)Then
        ' add the points to the current players score variable
        Score(CurrentPlayer) = Score(CurrentPlayer) + points * Multiplier3x * Multiplier2x 'only for this table
        ' update the score displays
        DMDScore
    End if

' you may wish to check to see if the player has gotten a replay
End Sub

' Add bonus to the bonuspoints AND update the score board

Sub AddBonus(points)
    If(Tilted = False)Then
        ' add the bonus to the current players bonus variable
        BonusPoints(CurrentPlayer) = BonusPoints(CurrentPlayer) + points
        ' update the score displays
        DMDScore
    End if

' you may wish to check to see if the player has gotten a replay
End Sub

' Add some points to the current Jackpot.
'
Sub AddJackpot(points) 'not used in this table
' Jackpots only generally increment in multiball mode AND not tilted
' but this doesn't have to be the case
'If(Tilted = False)Then

' If(bMultiBallMode = True) Then
' Jackpot = Jackpot + points
' you may wish to limit the jackpot to a upper limit, ie..
'	If (Jackpot >= 6000) Then
'		Jackpot = 6000
' 	End if
'End if
'End if
End Sub

Sub AddSuperJackpot(points)
    If(Tilted = False)Then

        ' If(bMultiBallMode = True) Then
        SuperJackpot = SuperJackpot + points
    ' you may wish to limit the jackpot to a upper limit, ie..
    '	If (Jackpot >= 6000) Then
    '		Jackpot = 6000
    ' 	End if
    'End if
    End if
End Sub

Sub AddBonusMultiplier(n)
    Dim NewBonusLevel
    ' if not at the maximum bonus level
    if(BonusMultiplier(CurrentPlayer) + n < MaxMultiplier)then
        ' then add and set the lights
        NewBonusLevel = BonusMultiplier(CurrentPlayer) + n
        SetBonusMultiplier(NewBonusLevel)
    End if
End Sub

' Set the Bonus Multiplier to the specified level AND set any lights accordingly
' There is no bonus multiplier lights in this table

Sub SetBonusMultiplier(Level)
    ' Set the multiplier to the specified level
    BonusMultiplier(CurrentPlayer) = Level
' Update the lights
End Sub

Sub AwardExtraBall()
    If NOT bExtraBallWonThisBall Then
        DMD "_", CL(1, ("EXTRA BALL WON")), "_", eNone, eBlink, eNone, 1000, True, SoundFXDOF("fx_Knocker", 122, DOFPulse, DOFKnocker)
        ExtraBallsAwards(CurrentPlayer) = ExtraBallsAwards(CurrentPlayer) + 1
        bExtraBallWonThisBall = True
        GiEffect 1
        LightEffect 2
    END If
End Sub

Sub AwardSpecial()
    DMD "_", CL(1, ("EXTRA GAME WON")), "_", eNone, eBlink, eNone, 1000, True, SoundFXDOF("fx_Knocker", 122, DOFPulse, DOFKnocker)
    Credits = Credits + 1
    DOF 140, DOFOn
    GiEffect 1
    LightEffect 1
End Sub

'in this table the jackpot is always 1 million + 10% of your score

Sub AwardJackpot() 'award a normal jackpot, double or triple jackpot
    If Multiplier3x = 3 Then
        AwardTripleJackpot
        Exit Sub
    End If
    If Multiplier2x = 2 Then
        AwardDoubleJackpot
        Exit Sub
    End If
    Dim tmp
    tmp = "vo_jackpot" & INT(RND * 4 + 1)
    Jackpot = 1000000 + Round(Score(CurrentPlayer) / 10, 0)
    DMD CL(0, FormatScore(Jackpot)), CL(1, ("JACKPOT")), "", eBlinkFast, eBlinkFast, eNone, 1000, True, tmp
    AddScore Jackpot
    GiEffect 1
    LightEffect 2
    FlashEffect 1
End Sub

Sub AwardDoubleJackpot() 'in this table the jackpot is always 1 million + 10% of your score
    Jackpot = (1000000 + Round(Score(CurrentPlayer) / 10, 0)) * 2
    DMD CL(0, FormatScore(Jackpot)), CL(1, ("DOUBLE JACKPOT")), "", eBlinkFast, eBlinkFast, eNone, 1000, True, "vo_doublejackpot"
    AddScore Jackpot
    GiEffect 1
    LightEffect 2
    FlashEffect 1
End Sub

Sub AwardTripleJackpot() 'in this table the jackpot is always 1 million + 10% of your score
    DOF 132, DOFPulse
    Dim tmp
    tmp = "vo_triplejackpot" & INT(RND * 4 + 1)
    Jackpot = (1000000 + Round(Score(CurrentPlayer) / 10, 0)) * 3
    DMD CL(0, FormatScore(Jackpot)), CL(1, ("TRIPLE JACKPOT")), "", eBlinkFast, eBlinkFast, eNone, 1000, True, tmp
    AddScore Jackpot
    GiEffect 1
    LightEffect 2
    FlashEffect 1
End Sub

Sub AwardSuperJackpot()
    If Multiplier3x = 3 Then
        AwardTripleSuperJackpot
        Exit Sub
    End If
    If Multiplier2x = 2 Then
        AwardDoubleSuperJackpot
        Exit Sub
    End If
    Dim tmp
    DOF 133, DOFPulse
    tmp = "vo_superjackpot" & INT(RND * 5 + 1)
    DMD CL(0, FormatScore(SuperJackpot)), CL(1, ("SUPER JACKPOT")), "", eBlinkFast, eBlinkFast, eNone, 1000, True, tmp
    AddScore SuperJackpot
    GiEffect 1
    LightEffect 2
    FlashEffect 2
End Sub

Sub AwardDoubleSuperJackpot()
    DOF 134, DOFPulse
    DMD CL(0, FormatScore(SuperJackpot * 2)), CL(1, ("2X SUPER JACKPOT")), "", eBlinkFast, eBlinkFast, eNone, 1000, True, "vo_doublesuperjackpot"
    AddScore SuperJackpot * 2
    GiEffect 1
    LightEffect 2
    FlashEffect 2
End Sub

Sub AwardTripleSuperJackpot()
    DOF 135, DOFPulse
    DMD CL(0, FormatScore(SuperJackpot * 3)), CL(1, ("3X SUPER JACKPOT")), "", eBlinkFast, eBlinkFast, eNone, 1000, True, "vo_triplesuperjackpot"
    AddScore SuperJackpot * 3
    GiEffect 1
    LightEffect 2
    FlashEffect 2
End Sub

Sub AwardSkillshot()
    DOF 131, DOFPulse
    SkillshotValue = (1000000 + Round(Score(CurrentPlayer) / 10, 0)) '10% of the score + 1 million
    DMD CL(0, FormatScore(SkillshotValue)), CL(1, ("SKILLSHOT")), "", eBlinkFast, eBlink, eNone, 1000, True, "gb_fanfare6"
    AddScore SkillshotValue
    ResetSkillShotTimer_Timer
    GiEffect 1
    LightEffect 2
End Sub

Sub Congratulation()
    Dim tmp
    tmp = "vo_congrat" & INT(RND * 21 + 1)
    PlaySound tmp
End Sub
'*****************************
'    Load / Save / Highscore
'*****************************

Sub Loadhs
    Dim x
    x = LoadValue(TableName, "HighScore1")
    If(x <> "")Then HighScore(0) = CDbl(x)Else HighScore(0) = 100000 End If
    x = LoadValue(TableName, "HighScore1Name")
    If(x <> "")Then HighScoreName(0) = x Else HighScoreName(0) = "AAA" End If
    x = LoadValue(TableName, "HighScore2")
    If(x <> "")then HighScore(1) = CDbl(x)Else HighScore(1) = 100000 End If
    x = LoadValue(TableName, "HighScore2Name")
    If(x <> "")then HighScoreName(1) = x Else HighScoreName(1) = "BBB" End If
    x = LoadValue(TableName, "HighScore3")
    If(x <> "")then HighScore(2) = CDbl(x)Else HighScore(2) = 100000 End If
    x = LoadValue(TableName, "HighScore3Name")
    If(x <> "")then HighScoreName(2) = x Else HighScoreName(2) = "CCC" End If
    x = LoadValue(TableName, "HighScore4")
    If(x <> "")then HighScore(3) = CDbl(x)Else HighScore(3) = 100000 End If
    x = LoadValue(TableName, "HighScore4Name")
    If(x <> "")then HighScoreName(3) = x Else HighScoreName(3) = "DDD" End If
    x = LoadValue(TableName, "Credits")
    If(x <> "")then Credits = CInt(x)Else Credits = 0 End If

    'x = LoadValue(TableName, "Jackpot")
    'If(x <> "") then Jackpot = CDbl(x) Else Jackpot = 200000 End If
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
    'SaveValue TableName, "Jackpot", Jackpot
    SaveValue TableName, "TotalGamesPlayed", TotalGamesPlayed
End Sub

Sub Clearhs
HighScore(0) = 100000
HighScoreName(0) = "AAA"
HighScore(1) = 100000
HighScoreName(1) = "BBB"
HighScore(2) = 100000
HighScoreName(2) = "CCC"
HighScore(3) = 100000
HighScoreName(3) = "DDD"
    SaveValue TableName, "HighScore1", HighScore(0)
    SaveValue TableName, "HighScore1Name", HighScoreName(0)
    SaveValue TableName, "HighScore2", HighScore(1)
    SaveValue TableName, "HighScore2Name", HighScoreName(1)
    SaveValue TableName, "HighScore3", HighScore(2)
    SaveValue TableName, "HighScore3Name", HighScoreName(2)
    SaveValue TableName, "HighScore4", HighScore(3)
    SaveValue TableName, "HighScore4Name", HighScoreName(3)
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

    If tmp > HighScore(1)Then 'add 1 credit for beating the highscore
        AwardSpecial
    End If

    If tmp > HighScore(3)Then
        vpmtimer.addtimer 2000, "PlaySound ""vo_contratulationsgreatscore"" '"
        HighScore(3) = tmp
        'enter player's name
        HighScoreEntryInit()
    Else
        EndOfBallComplete()
    End If
End Sub

Sub HighScoreEntryInit()
    hsbModeActive = True
    PlaySound "vo_enteryourinitials"
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
        Playsound "fx_Previous"
        hsCurrentLetter = hsCurrentLetter - 1
        if(hsCurrentLetter = 0)then
            hsCurrentLetter = len(hsValidLetters)
        end if
        HighScoreDisplayNameNow()
    End If

    If keycode = RightFlipperKey Then
        Playsound "fx_Next"
        hsCurrentLetter = hsCurrentLetter + 1
        if(hsCurrentLetter > len(hsValidLetters))then
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
            if(hsCurrentDigit > 0)then
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
    if(hsCurrentDigit > 0)then TempBotStr = TempBotStr & hsEnteredDigits(0)
    if(hsCurrentDigit > 1)then TempBotStr = TempBotStr & hsEnteredDigits(1)
    if(hsCurrentDigit > 2)then TempBotStr = TempBotStr & hsEnteredDigits(2)

    if(hsCurrentDigit <> 3)then
        if(hsLetterFlash <> 0)then
            TempBotStr = TempBotStr & "_"
        else
            TempBotStr = TempBotStr & mid(hsValidLetters, hsCurrentLetter, 1)
        end if
    end if

    if(hsCurrentDigit < 1)then TempBotStr = TempBotStr & hsEnteredDigits(1)
    if(hsCurrentDigit < 2)then TempBotStr = TempBotStr & hsEnteredDigits(2)

    TempBotStr = TempBotStr & " <    "
    dLine(1) = ExpandLine(TempBotStr, 1)
    DMDUpdate 1
End Sub

Sub HighScoreFlashTimer_Timer()
    HighScoreFlashTimer.Enabled = False
    hsLetterFlash = hsLetterFlash + 1
    if(hsLetterFlash = 2)then hsLetterFlash = 0
    HighScoreDisplayName()
    HighScoreFlashTimer.Enabled = True
End Sub

Sub HighScoreCommitName()
    HighScoreFlashTimer.Enabled = False
    hsbModeActive = False
    PlaySong "m_end"
    hsEnteredName = hsEnteredDigits(0) & hsEnteredDigits(1) & hsEnteredDigits(2)
    if(hsEnteredName = "   ")then
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
            If HighScore(j) < HighScore(j + 1)Then
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

' *************************************************************************
'   JP's Reduced Display Driver Functions (based on script by Black)
' only 5 effects: none, scroll left, scroll right, blink and blinkfast
' 3 Lines, treats all 3 lines as text. 3rd line is just 1 character
' Example format:
' DMD "text1","text2","backpicture", eNone, eNone, eNone, 250, True, "sound"
' Short names:
' dq = display queue
' de = display effect
' *************************************************************************

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

Dim FlexDMD
Dim DMDScene

Sub DMD_Init() 'default/startup values
	Set FlexDMD = CreateObject("FlexDMD.FlexDMD")
	If Not FlexDMD is Nothing Then
		FlexDMD.TableFile = Table1.Filename & ".vpx"
		FlexDMD.RenderMode = 2
		FlexDMD.Width = 128
		FlexDMD.Height = 36
		FlexDMD.Clear = True
		FlexDMD.GameName = cGameName
		FlexDMD.Run = True
		Set DMDScene = FlexDMD.NewGroup("Scene")
		For i = 40 to 42
			DMDScene.AddActor FlexDMD.NewImage("Dig" & i, "VPX.dempty&dmd=2")
			'Digits(i).Visible = False
		Next
		For i = 0 to 39
			DMDScene.AddActor FlexDMD.NewImage("Dig" & i, "VPX.dempty&dmd=2")
			'Digits(i).Visible = False
		Next
		For i = 0 to 19 ' Top
			DMDScene.GetImage("Dig" & i).SetBounds 4 + i * 6, 3, 8, 12
		Next
		For i = 20 to 39 ' Bottom
			DMDScene.GetImage("Dig" & i).SetBounds 4 + (i - 20) * 6, 3 + 12 + 2, 8, 12
		Next
		For i = 40 to 42 ' Back
			DMDScene.GetImage("Dig" & i).SetBounds (i - 40) * 42, 0, 42, 36
		Next
		FlexDMD.LockRenderThread
		FlexDMD.Stage.AddActor DMDScene
		FlexDMD.UnlockRenderThread
	End If

    Dim i, j
    DMDFlush()
    deSpeed = 20
    deBlinkSlowRate = 5
    deBlinkFastRate = 2
    dCharsPerLine(0) = 20
    dCharsPerLine(1) = 20
    dCharsPerLine(2) = 3
    For i = 0 to 2
        dLine(i) = Space(dCharsPerLine(i))
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
    if(dqHead = dqTail)Then
        tmp = FL(0, "PL " & CurrentPlayer, FormatScore(Score(Currentplayer)))
        tmp1 = FL(1, "o " & Ghosts(CurrentPlayer) + GhostsHundreds(CurrentPlayer) * 100, " BALL " & Balls)
        tmp2 = ""
    End If
    DMD tmp, tmp1, tmp2, eNone, eNone, eNone, 25, True, ""
End Sub

Sub DMDScoreNow
    DMDFlush
    DMDScore
End Sub

Sub DMD(Text0, Text1, Text2, Effect0, Effect1, Effect2, TimeOn, bFlush, Sound)
    if(dqTail < dqSize)Then
        if(Text0 = "_")Then
            dqEffect(0, dqTail) = eNone
            dqText(0, dqTail) = "_"
        Else
            dqEffect(0, dqTail) = Effect0
            dqText(0, dqTail) = ExpandLine(Text0, 0)
        End If

        if(Text1 = "_")Then
            dqEffect(1, dqTail) = eNone
            dqText(1, dqTail) = "_"
        Else
            dqEffect(1, dqTail) = Effect1
            dqText(1, dqTail) = ExpandLine(Text1, 1)
        End If

        if(Text2 = "_")Then
            dqEffect(2, dqTail) = eNone
            dqText(2, dqTail) = "_"
        Else
            dqEffect(2, dqTail) = Effect2
            dqText(2, dqTail) = Text2 'it is always 3 letter in this table
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
    if(dqHead = dqTail)Then
        if(dqbFlush(Head) = True)Then
            DMDScoreNow()
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
                    BlinkEffect = True
                    if((deCount(i)MOD deBlinkSlowRate) = 0)Then
                        deBlinkCycle(i) = deBlinkCycle(i)xor 1
                    End If

                    if(deBlinkCycle(i) = 0)Then
                        Temp = dqText(i, dqHead)
                    Else
                        Temp = Space(dCharsPerLine(i))
                    End If
                case eBlinkFast:
                    BlinkEffect = True
                    if((deCount(i)MOD deBlinkFastRate) = 0)Then
                        deBlinkCycle(i) = deBlinkCycle(i)xor 1
                    End If

                    if(deBlinkCycle(i) = 0)Then
                        Temp = dqText(i, dqHead)
                    Else
                        Temp = Space(dCharsPerLine(i))
                    End If
            End Select

            if(dqText(i, dqHead) <> "_")Then
                dLine(i) = Temp
                DMDUpdate i
            End If
        End If
    Next

    if(deCount(0) = deCountEnd(0))and(deCount(1) = deCountEnd(1))and(deCount(2) = deCountEnd(2))Then

        if(dqTimeOn(dqHead) = 0)Then
            DMDFlush()
        Else
            if(BlinkEffect = True)Then
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
        TempStr = Space(dCharsPerLine(id))
    Else
        if(Len(TempStr) > Space(dCharsPerLine(id)))Then
            TempStr = Left(TempStr, Space(dCharsPerLine(id)))
        Else
            if(Len(TempStr) < dCharsPerLine(id))Then
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

Function CL(id, NumString) 'center line
    Dim Temp, TempStr
	NumString = LEFT(NumString, dCharsPerLine(id))
    Temp = (dCharsPerLine(id)- Len(NumString)) \ 2
    TempStr = Space(Temp) & NumString & Space(Temp)
    CL = TempStr
End Function

Function RL(id, NumString) 'right line
    Dim Temp, TempStr
	NumString = LEFT(NumString, dCharsPerLine(id))
    Temp = dCharsPerLine(id)- Len(NumString)
    TempStr = Space(Temp) & NumString
    RL = TempStr
End Function

Function FL(id, aString, bString) 'fill line
    Dim tmp, tmpStr
	aString = LEFT(aString, dCharsPerLine(id))
	bString = LEFT(bString, dCharsPerLine(id))
    tmp = dCharsPerLine(id)- Len(aString)- Len(bString)
	If tmp <0 Then tmp = 0
    tmpStr = aString & Space(tmp) & bString
    FL = tmpStr
End Function

'**************
' Update DMD
'**************

Sub DMDUpdate(id)
    Dim digit, value

	If Not FlexDMD is Nothing Then FlexDMD.LockRenderThread
    Select Case id
        Case 0 'top text line
            For digit = 0 to 19
                DMDDisplayChar mid(dLine(0), digit + 1, 1), digit
            Next
        Case 1 'bottom text line
            For digit = 20 to 39
                DMDDisplayChar mid(dLine(1), digit -19, 1), digit
            Next
        Case 2 ' back image - back animations
            If dLine(2) = "" Then dLine(2) = "@@@"
            For digit = 40 to 42
                DMDDisplayChar mid(dLine(2), digit -39, 1), digit
            Next
    End Select
	If Not FlexDMD is Nothing Then FlexDMD.UnlockRenderThread
End Sub

Sub DMDDisplayChar(achar, adigit)
    If achar = "" Then achar = " "
    achar = ASC(achar)
    Digits(adigit).ImageA = Chars(achar)
	If Not FlexDMD is Nothing Then 
		if Chars(achar) = "title2" Or Chars(achar) = "jp2" Then
			DMDScene.GetImage("Dig" & adigit).Bitmap = FlexDMD.NewImage("", "VPX." & Chars(achar) & "&dmd2=2&add").Bitmap
		Else
			DMDScene.GetImage("Dig" & adigit).Bitmap = FlexDMD.NewImage("", "VPX." & Chars(achar) & "&dmd=2&add").Bitmap
		End If
	End If
End Sub

'****************************
' JP's new DMD using flashers
'****************************

Dim Digits, Chars(255)

DMDInit

Sub DMDInit
    Dim i

        Digits = Array(digit001, digit002, digit003, digit004, digit005, digit006, digit007, digit008, digit009, digit010, _
            digit011, digit012, digit013, digit014, digit015, digit016, digit017, digit018, digit019, digit020, _
            digit021, digit022, digit023, digit024, digit025, digit026, digit027, digit028, digit029, digit030, _
            digit031, digit032, digit033, digit034, digit035, digit036, digit037, digit038, digit039, digit040, _
            digit041, digit042, digit043)

    For i = 0 to 255:Chars(i) = "dempty":Next

    Chars(32) = "dempty"
    Chars(35) = "jp1"
    Chars(36) = "jp2"
    Chars(37) = "jp3"
    Chars(38) = "title1"
    Chars(39) = "title2"
    Chars(40) = "title3"
    Chars(46) = "dot"      '.
    Chars(48) = "d0"       '0
    Chars(49) = "d1"       '1
    Chars(50) = "d2"       '2
    Chars(51) = "d3"       '3
    Chars(52) = "d4"       '4
    Chars(53) = "d5"       '5
    Chars(54) = "d6"       '6
    Chars(55) = "d7"       '7
    Chars(56) = "d8"       '8
    Chars(57) = "d9"       '9
    Chars(60) = "dless"    '<
    Chars(61) = "dequal"   '=
    Chars(62) = "dgreater" '>
    Chars(64) = "bkempty"  '@
    Chars(65) = "da"       'A
    Chars(66) = "db"       'B
    Chars(67) = "dc"       'C
    Chars(68) = "dd"       'D
    Chars(69) = "de"       'E
    Chars(70) = "df"       'F
    Chars(71) = "dg"       'G
    Chars(72) = "dh"       'H
    Chars(73) = "di"       'I
    Chars(74) = "dj"       'J
    Chars(75) = "dk"       'K
    Chars(76) = "dl"       'L
    Chars(77) = "dm"       'M
    Chars(78) = "dn"       'N
    Chars(79) = "do"       'O
    Chars(80) = "dp"       'P
    Chars(81) = "dq"       'Q
    Chars(82) = "dr"       'R
    Chars(83) = "ds"       'S
    Chars(84) = "dt"       'T
    Chars(85) = "du"       'U
    Chars(86) = "dv"       'V
    Chars(87) = "dw"       'W
    Chars(88) = "dx"       'X
    Chars(89) = "dy"       'Y
    Chars(90) = "dz"       'Z
    Chars(94) = "dup"      '^
    '    Chars(95) = '_
    Chars(96) = "d0a"  '0.
    Chars(97) = "d1a"  '1. 'a
    Chars(98) = "d2a"  '2. 'b
    Chars(99) = "d3a"  '3. 'c
    Chars(100) = "d4a" '4. 'd
    Chars(101) = "d5a" '5. 'e
    Chars(102) = "d6a" '6. 'f
    Chars(103) = "d7a" '7. 'g
    Chars(104) = "d8a" '8. 'h
    Chars(105) = "d9a" '9  'i
    Chars(111) = "do2" 'o
    Chars(112) = "dp2" 'p 'p dark
    Chars(113) = "dk2" 'q 'k dark
    Chars(114) = "de2" 'r 'e dark
    ' bumper images
    Chars(131) = "b1"
    Chars(132) = "b2"
    Chars(133) = "b3"
    Chars(134) = "b4"
    Chars(135) = "b5"
    Chars(136) = "b6"
    Chars(137) = "b7"
    Chars(138) = "b8"
    Chars(139) = "b9"
    Chars(140) = "b10"
    Chars(141) = "b11"
    Chars(142) = "b12"
    Chars(143) = "b13"
    Chars(144) = "b14"
    Chars(145) = "b15"
    Chars(146) = "b16"
    Chars(147) = "b17"
    Chars(148) = "b18"
    Chars(149) = "b19"
    Chars(150) = "b20"
    Chars(151) = "b21"
    Chars(152) = "b22"
    Chars(153) = "b23"
    Chars(154) = "b24"
    Chars(155) = "b25"
    Chars(156) = "b26"
    Chars(157) = "b27"
    Chars(158) = "b28"
    Chars(159) = "b29"
    Chars(160) = "b30"
End Sub

'****************************************
' Real Time updatess using the GameTimer
'****************************************
'used for all the real time updates

Sub GameTimer_Timer
    RollingUpdate
' add any other real time update subs, like gates or diverters
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
            n.color = RGB(255, 252, 224)
            n.colorfull = RGB(193, 91, 0)
        Case "purple"
            n.color = RGB(128, 0, 128)
            n.colorfull = RGB(255, 0, 255)
        Case "amber"
            n.color = RGB(193, 49, 0)
            n.colorfull = RGB(255, 153, 0)
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
    Dim tmp
    'info goes in a loop only stopped by the credits and the startkey
    If Score(1)Then
        DMD CL(0, "LAST SCORE"), CL(1, "PLAYER1 " &FormatScore(Score(1))), "", eNone, eNone, eNone, 3000, False, ""
    End If
    If Score(2)Then
        DMD CL(0, "LAST SCORE"), CL(1, "PLAYER2 " &FormatScore(Score(2))), "", eNone, eNone, eNone, 3000, False, ""
    End If
    If Score(3)Then
        DMD CL(0, "LAST SCORE"), CL(1, "PLAYER3 " &FormatScore(Score(3))), "", eNone, eNone, eNone, 3000, False, ""
    End If
    If Score(4)Then
        DMD CL(0, "LAST SCORE"), CL(1, "PLAYER4 " &FormatScore(Score(4))), "", eNone, eNone, eNone, 3000, False, ""
    End If
    DMD "", CL(1, "GAME oVER"), "", eNone, eBlink, eNone, 2000, False, ""
    If bFreePlay Then
        DMD CL(0, "FREE PLAY"), CL(1, "PRESS START"), "", eNone, eBlink, eNone, 2000, False, ""
    Else
        If Credits > 0 Then
            DMD CL(0, "CREDITS " & Credits), CL(1, "PRESS START"), "", eNone, eBlink, eNone, 2000, False, ""
        Else
            DMD CL(0, "CREDITS " & Credits), CL(1, "INSERT COIN"), "", eNone, eBlink, eNone, 2000, False, ""
        End If
    End If
    tmp = chr(35)&chr(36)&chr(37)
    DMD "", "", tmp, eNone, eNone, eNone, 3000, False, "" 'jpsalas presents
    tmp = chr(38)&chr(39)&chr(40)
    DMD "", "", tmp, eNone, eNone, eNone, 4000, False, "" 'title
    DMD CL(0, "HIGHSCORES"), Space(dCharsPerLine(1)), "", eScrollLeft, eScrollLeft, eNone, 20, False, ""
    DMD CL(0, "HIGHSCORES"), "", "", eBlinkFast, eNone, eNone, 1000, False, ""
    DMD CL(0, "HIGHSCORES"), "1> " &HighScoreName(0) & " " &FormatScore(HighScore(0)), "", eNone, eScrollLeft, eNone, 2000, False, ""
    DMD "_", "2> " &HighScoreName(1) & " " &FormatScore(HighScore(1)), "", eNone, eScrollLeft, eNone, 2000, False, ""
    DMD "_", "3> " &HighScoreName(2) & " " &FormatScore(HighScore(2)), "", eNone, eScrollLeft, eNone, 2000, False, ""
    DMD "_", "4> " &HighScoreName(3) & " " &FormatScore(HighScore(3)), "", eNone, eScrollLeft, eNone, 2000, False, ""
    DMD Space(dCharsPerLine(0)), Space(dCharsPerLine(1)), "", eScrollLeft, eScrollLeft, eNone, 500, False, ""
End Sub

Sub StartAttractMode(dummy)
    StartLightSeq
    DMDFlush
    ShowTableInfo
    StartRainbow "arrows"
End Sub

Sub StopAttractMode
    LightSeqAttract.StopPlay
    StopRainbow
    DMDScoreNow
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

Sub LightSeqSkillshot_PlayDone()
    LightSeqSkillshot.Play SeqAllOff
End Sub

'*************************
' Rainbow Changing Lights
'*************************

Dim RGBStep, RGBFactor, Red, Green, Blue, RainbowLights

Sub StartRainbow(n)
    RainbowLights = n
    RGBStep = 0
    RGBFactor = 1
    Red = 255
    Green = 0
    Blue = 0
    RainbowTimer.Enabled = 1
End Sub

Sub StopRainbow()
    Dim obj
    RainbowTimer.Enabled = 0
    If RainbowLights = "arrows" Then
        For each obj in aLEDLights
            SetLightColor obj, "white", 0
        Next
    ElseIf RainbowLights = "gi" Then
        For each obj in aGiLights
            SetLightColor obj, "white", 0
        Next
    End If
End Sub

Sub RainbowTimer_Timer 'rainbow led light color changing
    Dim obj
    Select Case RGBStep
        Case 0 'Green
            Green = Green + RGBFactor
            If Green > 255 then
                Green = 255
                RGBStep = 1
            End If
        Case 1 'Red
            Red = Red - RGBFactor
            If Red < 0 then
                Red = 0
                RGBStep = 2
            End If
        Case 2 'Blue
            Blue = Blue + RGBFactor
            If Blue > 255 then
                Blue = 255
                RGBStep = 3
            End If
        Case 3 'Green
            Green = Green - RGBFactor
            If Green < 0 then
                Green = 0
                RGBStep = 4
            End If
        Case 4 'Red
            Red = Red + RGBFactor
            If Red > 255 then
                Red = 255
                RGBStep = 5
            End If
        Case 5 'Blue
            Blue = Blue - RGBFactor
            If Blue < 0 then
                Blue = 0
                RGBStep = 0
            End If
    End Select
    If RainbowLights = "arrows" Then
        For each obj in aLEDLights
            obj.color = RGB(Red \ 10, Green \ 10, Blue \ 10)
            obj.colorfull = RGB(Red, Green, Blue)
        Next
    ElseIf RainbowLights = "gi" Then
        For each obj in aGiLights
            obj.color = RGB(Red \ 10, Green \ 10, Blue \ 10)
            obj.colorfull = RGB(Red, Green, Blue)
        Next
    End If
End Sub

'***********************************************************************
' *********************************************************************
'                     Table Specific Script Starts Here
' *********************************************************************
'***********************************************************************

' droptargets, animations, etc
Sub VPObjects_Init
    LScoleriTarget.IsDropped = 1
    RScoleriTarget.IsDropped = 1
	LeftScoopK.IsDropped = 1
	RightScoopK.IsDropped = 1
End Sub

' tables variables and modes init

Dim bSkillshotSelect 'used to select the skillshot you want
Dim UpperSkillShot
Dim LowerSkillshot
Dim PKELevel 'used in the PKE frenzy, increased by the top lanes, the proton pack targets, right ramp
Dim PKEMult
Dim Ghosts(4)
Dim GhostsHundreds(4)
Dim bSuperJackpot
Dim bDoubleSuperJackpot
Dim bTripleSuperJackpot
Dim bJackpot
Dim bLoopinSupers
Dim HiddenJackpot
Dim bMassHysteria
Dim SpinnerValue
Dim SpinnerLevel
Dim LeftScoleri
Dim RightScoleri
Dim GozerAward
Dim bTerrorDogActive
Dim TerrorDogAward
Dim SymmetricalBookStep
Dim FirestationHoleHits
Dim ModesCompleted
Dim Multiplier2x
Dim Multiplier3x
Dim SpookedLibrarianHits
Dim BackOffManHits
Dim WeGotOneHits
Dim bSlimerSuperJackpot
Dim HeSlimedMeHits
Dim HeSlimedMeValue
Dim TheBallroomHits
Dim WhoBroughttheDogHits
Dim SpookCentralHits
Dim GozerianValue
Dim GozerianHits
Dim StayPuftHits
Dim StayPuftMultiballHits
Dim Gear(5)
Dim ectoHits
Dim NextGhostAward
Dim SlimerDefeats
Dim JumpSuitHits
Dim LaneBonus
Dim bStorageMultiball
Dim SMBHits

Sub Game_Init() 'called at the start of a new game
    Dim i
    bExtraBallWonThisBall = False
    TurnOffPlayfieldLights()
    'Play some Music
    PlaySong "m_main"
    'Init Variables
    bSkillshotSelect = False
    UpperSkillShot = 0
    LowerSkillshot = 0
    PKELevel = 100
    PKEMult = 8
    Jackpot = 1000000
    SuperJackpot = 1000000
    ResetBumpers
    Ghosts(1) = 0 'captured ghosts
    Ghosts(2) = 0
    Ghosts(3) = 0
    Ghosts(4) = 0
    GhostsHundreds(1) = 0
    GhostsHundreds(2) = 0
    GhostsHundreds(3) = 0
    GhostsHundreds(4) = 0
    NextGhostAward = 0
    InitGhostTarget
    bSlimerUp = False
    bPlayfieldSlimed = False
    SlimerHits = 0
    SlimerDefeats = 0
    bSlimerSuperJackpot = False
    bSuperJackpot = False
    bDoubleSuperJackpot = False
    bTripleSuperJackpot = False
    bJackpot = False
    bLoopinSupers = False
    HiddenJackpot = 0
    bMassHysteria = False
    SpinnerValue = 1000
    SpinnerLevel = 1
    StorageLights 0
    bMultiBallMode = False
    bBonusHeld = False
    LeftScoleri = 0
    RightScoleri = 0
    GozerAward = 0
    bTerrorDogActive = False
    TerrorDogAward = 4000000
    SymmetricalBookStep = 0
    FirestationHoleHits = 0
    Multiplier2x = 1
    Multiplier3x = 1
    SpookedLibrarianHits = 0
    BackOffManHits = 0
    WeGotOneHits = 0
    bSlimerSuperJackpot = False
    HeSlimedMeHits = 0
    HeSlimedMeValue = 1000000
    TheBallroomHits = 0
    WhoBroughttheDogHits = 0
    SpookCentralHits = 0
    GozerianValue = 1000000
    GozerianHits = 0
    StayPuftHits = 0
    StayPuftMultiballHits = 0
    Gear(1) = 0
    Gear(2) = 0
    Gear(3) = 0
    Gear(4) = 0
    Gear(5) = 0
    ectoHits = 0
    JumpSuitHits = 0
    LaneBonus = 0
    bStorageMultiball = false
    SMBHits = 0
    'Init Delays/Timers
    PlayQuote.Enabled = 1
    'Skillshot Init
    'MainModes Init()
    ModesCompleted = 0
    For i = 0 to 12
        Modes(i) = 0
    Next

    'Init lights
    'powerpack lights
    For each x in aProtonLights
        x.State = 2
    Next
    Light1.State = 0
    Light2.State = 0
    Light3.State = 0
    Light4.State = 0
    'storage facility lights
    ContainerFlasher1. Visible = 0
    ContainerFlasher2. Visible = 0
    ContainerFlasher3. Visible = 0
    ContainerFlasher4. Visible = 0
    ContainerFlasher5. Visible = 0
    ContainerFlasher6. Visible = 0
End Sub

Sub StopEndOfBallModes() 'this sub is called after the last ball is drained
    ResetSkillShotTimer_Timer
    If bSlimerUp Then SlimerMoveDown
    If bLoopinSupers Then LoopTimerExpired_Timer
    NROEATimerExpired_Timer
    PKETimerExpired_Timer
    EndScoleriBrothers
    GozerHurryUpExpired_Timer
    TerrorDogExpired_Timer
    TwoXTimerExpired_Timer
    ThreeXTimerExpired_Timer
    HologramStop
    EndMassHysteriaMultiball
    If Modes(0)Then StopMode Modes(0) 'a mode is active so stop it
End Sub

Sub ResetNewBallVariables()           'reset variables for a new ball or player
    bPlayfieldSlimed = False
    bSlimerSuperJackpot = False
    bSuperJackpot = False
    bDoubleSuperJackpot = False
    bTripleSuperJackpot = False
    bJackpot = False
    HiddenJackpot = 0
    LaneBonus = 0
    SuperJackpot = 1000000
    PKELevel = 100
End Sub

Sub ResetNewBallLights() 'turn on or off the needed lights before a new ball is released
    l55.State = 0        'super jackpot
    UpdateGhostLights
End Sub

Sub TurnOffPlayfieldLights()
    Dim a
    For each a in aLights
        a.State = 0
    Next
    Bumper1Light.Visible = 0
    Bumper2Light.Visible = 0
    Bumper3Light.Visible = 0
End Sub

Sub SelectSkillshot(keycode)
    If keycode = LeftFlipperKey Then
        Playsound "fx_Previous"
        UpperSkillShot = (UpperSkillShot + 1)MOD 3
        UpdateSkillShot
    End If
    If keycode = RightFlipperKey Then
        Playsound "fx_Next"
        LowerSkillshot = (LowerSkillshot + 1)MOD 6
        UpdateSkillShot
    End If
End Sub

Sub UpdateSkillShot() 'Updates the DMD & lights with the chosen skillshots
    LightSeqSkillshot.Play SeqAllOff
    'turn off lights
    l28.State = 0:l28b.State = 0:Bumper1Light. Visible = 0
    l29.State = 0:l29b.State = 0:Bumper2Light. Visible = 0
    l30.State = 0:l30b.State = 0:Bumper3Light. Visible = 0
    l60.State = 0
    l61.State = 0
    l62.State = 0
    l63.State = 0
    l64.State = 0
    l67.State = 0
    DMDFlush
    Select Case UpperSkillShot
        Case 0:DMD CL(0, "  +5 BONUS X    pqE"), "_", "", eNone, eNone, eNone, 25, False, "":l30.State = 2:l30b.State = 2
        Case 1:DMD CL(0, "LIT PLAYFIELD X pKr"), "_", "", eNone, eNone, eNone, 25, False, "":l29.State = 2:l29b.State = 2
        Case 2:DMD CL(0, "+3M SUP.JACKPOT Pqr"), "_", "", eNone, eNone, eNone, 25, False, "":l28.State = 2:l28b.State = 2
    End Select
    Select Case LowerSkillshot
        Case 0:DMD "_", CL(1, "^ START MODE"), "", eNone, eNone, eNone, 25, False, "":SetLightColor l60, "blue", 2:LightSeqladder1.Play SeqUpOn, 5, 1
        Case 1:DMD "_", CL(1, "ADV. SPINNER LEVEL"), "", eNone, eNone, eNone, 25, False, "":SetLightColor l61, "blue", 2
        Case 2:DMD "_", CL(1, "START ^ MODE"), "", eNone, eNone, eNone, 25, False, "":SetLightColor l62, "blue", 2:LightSeqladder2.Play SeqUpOn, 5, 1
        Case 3:DMD "_", CL(1, "START MODE ^"), "", eNone, eNone, eNone, 25, False, "":SetLightColor l63, "blue", 2:LightSeqladder3.Play SeqUpOn, 5, 1
        Case 4:DMD "_", CL(1, "START PKE FRENZY"), "", eNone, eNone, eNone, 25, False, "":SetLightColor l64, "blue", 2
        Case 5:DMD "_", CL(1, "LIGHT NROESPA"), "", eNone, eNone, eNone, 25, False, "":SetLightColor l67, "blue", 2
    End Select
End Sub

Sub ResetSkillShotTimer_Timer 'timer to reset the skillshot lights & variables
    ResetSkillShotTimer.Enabled = 0
    bSkillShotReady = False
    bSkillShotSelect = False
    l28.State = 0:l28b.State = 0
    l29.State = 0:l29b.State = 0
    l30.State = 0:l30b.State = 0
    Bumper1Light. Visible = 0
    Bumper2Light. Visible = 0
    Bumper3Light. Visible = 0
    SetLightColor l60, "white", 0
    SetLightColor l61, "white", 0
    SetLightColor l62, "white", 0
    SetLightColor l63, "white", 0
    SetLightColor l64, "white", 0
    SetLightColor l67, "white", 0
    LoopGateLeft.Open = False
    LightSeqSkillshot.StopPlay
    DMDScoreNow
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
    PlaySoundAt SoundFXDOF("fx_slingshot", 103, DOFPulse, DOFContactors), Lemk
    DOF 104, DOFPulse
    LeftSling4.Visible = 1:LeftSling1.Visible = 0
    Lemk.RotX = 26
    LStep = 0
    LeftSlingShot.TimerEnabled = True
    ' add some points
    AddScore 110
    ' add some effect to the table?
    FlashForMs l20, 1000, 50, 0:FlashForMs l20f, 1000, 50, 0
    FlashForMs l21, 1000, 50, 0:FlashForMs l21f, 1000, 50, 0
    ' remember last trigger hit by the ball
    LastSwitchHit = "LeftSlingShot"
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
    If Tilted Then Exit Sub
    PlaySoundAt SoundFXDOF("fx_slingshot", 105, DOFPulse, DOFContactors),Remk
    DOF 106, DOFPulse
    RightSling4.Visible = 1:RightSling1.Visible = 0
    Remk.RotX = 26
    RStep = 0
    RightSlingShot.TimerEnabled = True
    ' add some points
    AddScore 110
    ' add some effect to the table?
    FlashForMs l22, 1000, 50, 0:FlashForMs l22f, 1000, 50, 0
    FlashForMs l23, 1000, 50, 0:FlashForMs l23f, 1000, 50, 0
    ' remember last trigger hit by the ball
    LastSwitchHit = "RightSlingShot"
End Sub

Sub RightSlingShot_Timer
    Select Case RStep
        Case 1:RightSLing4.Visible = 0:RightSLing3.Visible = 1:Remk.RotX = 14:
        Case 2:RightSLing3.Visible = 0:RightSLing2.Visible = 1:Remk.RotX = 2:
        Case 3:RightSLing2.Visible = 0:RightSLing1.Visible = 1:Remk.RotX = -10:Gi1.State = 1:RightSlingShot.TimerEnabled = False
    End Select
    RStep = RStep + 1
End Sub

'******************
' Firestation Spinner
'******************
' The spinner is used to increase the value of the Firestation shots (the left captive ball)
' SpinnerValue starts at 1000 and it is increased by 1000 by Pop bumper awards and Tobin awards
' SpinnerLevel is advanced by left orbit shots and Pop bumper awards

Sub Spinner_Spin()
    If Tilted Then Exit Sub
    PlaySoundAt "fx_spinner", Spinner
    ' increase super jackpot if light l66 is blinking
    If l66.State = 2 then
        AddSuperJackpot 100000
    End If
    AddScore SpinnerValue
End Sub

'*********************
' Inlanes - Outlanes
'*********************

Sub sw1_Hit()
    PlaySoundAt "fx_sensor", sw1
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    AddScore 10000
    ' change some light
    FlashForms l5f, 1000, 40, 0:FlashForms l5, 1000, 40, 0
    LastSwitchHit = "sw1"
' do some check
End Sub

Sub sw2_Hit()
    PlaySoundAt "fx_sensor", sw2
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    If l12.State = 1 Then 'catch ghost
        l12.State = 0
        CatchGhost 1
    End If
    AddScore 10000
    ' change some light
    FlashForms l5f, 1000, 40, 0:FlashForms l5, 1000, 40, 0
    LastSwitchHit = "sw2"
' do some check
End Sub

Sub sw3_Hit()
    PlaySoundAt "fx_sensor", sw3
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    AddScore 10000
    ' change some light
    FlashForms l5f, 1000, 40, 0:FlashForms l5, 1000, 40, 0
    LastSwitchHit = "sw3"
    ' do some check
    JumpSuitHits = JumpSuitHits + 1
    CheckJumpSuitHits
End Sub

Sub sw4_Hit()
    PlaySoundAt "fx_sensor", sw4
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    AddScore 10000
    ' change some light
    FlashForms l6f, 1000, 40, 0:FlashForms l6, 1000, 40, 0
    LastSwitchHit = "sw4"
    ' do some check
    JumpSuitHits = JumpSuitHits + 1
    CheckJumpSuitHits
End Sub

Sub sw5_Hit()
    PlaySoundAt "fx_sensor", sw5
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    If l11.State = 1 Then 'catch ghost
        l11.State = 0
        CatchGhost 1
    End If
    AddScore 10000
    ' change some light
    FlashForms l6f, 1000, 40, 0:FlashForms l6, 1000, 40, 0
    LastSwitchHit = "sw5"
' do some check
End Sub

Sub sw6_Hit()
    PlaySoundAt "fx_sensor", sw6
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    AddScore 10000
    ' change some light
    FlashForms l6f, 1000, 40, 0:FlashForms l6, 1000, 40, 0
    LastSwitchHit = "sw6"
' do some check
End Sub

'************
' Other lanes
'************

Sub sw13_Hit() 'ecto googles and other modes
    If Tilted Then Exit Sub
    FlashForms ectoFlasher, 1000, 40, 0
    If bSkillShotReady AND l63.State = 2 AND ActiveBall.VelY < 0 Then 'the ball moves up
        AwardSkillshot
        'start mode right loop
        If Modes(6) = 0 Then 'the light is on/1 when the mode is finished
            StartMode 6
        ElseIF Modes(7) = 0 Then
            StartMode 7
        ElseIF Modes(8) = 0 Then
            StartMode 8
        ElseIF Modes(9) = 0 Then
            StartMode 9
        End If
    End If
    ' count the hits and activate the ecto googles
    ectoHits = ectoHits + 1
    CheckEctoHits
    If l16.State = 1 Then 'catch ghost
        l16.State = 0
        CatchGhost 1
    End If
End Sub

Sub sw12_Hit() 'left loop
    PlaySoundAt"fx_sensor", sw12
    If Tilted Then Exit Sub
    if bSkillShotReady AND l61.State = 2 AND ActiveBall.VelX > 0 Then
        AwardSkillshot
        'increment bonus x 5
        AddBonusMultiplier 5
    End If
    If l14.State = 1 Then 'catch ghost
        l14.State = 0
        CatchGhost 1
    End If
    Select Case Modes(0)
        Case 2
            if l61.State = 2 Then
                BackFlashEffect 1
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckBackOffManHits
                Exit Sub
            End If
        Case 3
            If l61.State = 2 Then
                BackFlashEffect 1
                WeGotOneHits = WeGotOneHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckWeGotOneHits
                Exit Sub
            End If
        Case 5
            If l61.State = 2 Then
                BackFlashEffect 1
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
                Exit Sub
            End If
        Case 6
            If l61.State = 2 Then
                BackFlashEffect 1
                l61.State = 0
                WhoBroughttheDogHits = WhoBroughttheDogHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckWhoBroughttheDogHits
                Exit Sub
            End If
        Case 7
            If l61.State = 2 Then
                BackFlashEffect 1
                l61.State = 0
                SpookCentralHits = SpookCentralHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckSpookCentralHits
                Exit Sub
            End If
        Case 8
            If l61.State = 2 Then
                BackFlashEffect 1
                GozerianValue = GozerianValue + 1000000
                DMD CL(0, "GOZERIAN VALUE IS"), CL(1, FormatScore(GozerianValue)), "", eNone, eBlinkFast, eNone, 800, True, ""
                Exit Sub
            End If
        Case 9
            If l61.State = 2 Then
                l61.State = 0
                SetLightColor l63, "orange", 2
                StayPuftHits = StayPuftHits + 1
                'do something
                PlaySound "gb_blast3"
                FlashForMs StayPuftFlasher, 1500, 50, 0
                LightEffect 2
                Exit Sub
            End If
    End Select
    If bLoopinSupers Then
        AwardSuperJackpot
        Exit Sub
    End If

    If HiddenJackpot = 1 Then
        AwardJackpot
        Exit Sub
    End If

    If bPlayfieldSlimed Then
        bPlayfieldSlimed = False
        l61.State = 0
        l63.State = 0
        LightEffect 1
        bSlimerSuperJackpot = True
        PlaySound "vo_getthesuperjackpot"
        SetLightColor l62, "red", 2
    End If
    AddScore 10000
    ' change some light
    LastSwitchHit = "sw12"
' do some check
End Sub

Sub sw11_Hit() 'right loop
    PlaySoundAt "fx_sensor", sw11
    StayPuftShake
    If Tilted or bSkillShotReady Then Exit Sub
    Select Case Modes(0)
        Case 0 ' if no mode is active then check to see if there is a ready mode
            If l48.State = 2 Then
                StartMode 6
            ElseIF l49.State = 2 Then
                StartMode 7
            ElseIF l50.State = 2 Then
                StartMode 8
            ElseIF l51.State = 2 Then
                StartMode 9
            End If
        Case 1
            If l63.State = 2 Then
                SpookedLibrarianHits = SpookedLibrarianHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckSpookedHits
                l66.State = 2 'enable the increase of value of the superjackpot in the spinner
                Exit Sub
            End If
        Case 2
            If l63.State = 2 Then
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckBackOffManHits
                Exit Sub
            End If
        Case 3
            If l63.State = 2 Then
                WeGotOneHits = WeGotOneHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckWeGotOneHits
                Exit Sub
            End If
        Case 5
            If l63.State = 2 Then
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
                Exit Sub
            End If
        Case 6
            If l63.State = 2 Then
                l63.State = 0
                WhoBroughttheDogHits = WhoBroughttheDogHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, "gb_terrordog1"
                CheckWhoBroughttheDogHits
                Exit Sub
            End If
        Case 7
            If l63.State = 2 Then
                l63.State = 0
                SpookCentralHits = SpookCentralHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckSpookCentralHits
                Exit Sub
            End If
        Case 8
            If l63.State = 2 Then
                GozerianValue = GozerianValue + 1000000
                DMD CL(0, "GOZERIAN VALUE IS"), CL(1, FormatScore(GozerianValue)), "", eNone, eBlinkFast, eNone, 800, True, ""
                Exit Sub
            End If
        Case 9
            If l63.State = 2 Then
                SetLightColor l63, "red", 1
                DMD CL(0, "STAY PUFT"), CL(1, "MARSHMALLOW MAN"), "", eNone, eBlinkFast, eNone, 1000, False, "vo_aimformarsmellowman"
                DMD "_", CL(1, "MULTIBALL"), "", eNone, eBlinkFast, eNone, 1000, True, ""
                AddMultiball StayPuftHits
                Exit Sub
            ElseIf l63.State = 1 Then
                StayPuftMultiballHits = StayPuftMultiballHits + 1
                CheckStayPuftHits()
                Exit Sub
            End If
    End Select
    If bLoopinSupers Then
        AwardSuperJackpot
        Exit Sub
    End If
    If HiddenJackpot = 3 Then
        AwardJackpot
        Exit Sub
    End If
    If bPlayfieldSlimed Then
        bPlayfieldSlimed = False
        l61.State = 0
        l63.State = 0
        LightEffect 1
        bSlimerSuperJackpot = True
        SetLightColor l62, "red", 2
    End If

    AddScore 10000
    ' change some extra light, do other checks
    LastSwitchHit = "sw11"
End Sub

Sub sw7_Hit() 'bumper exit lane
    PlaySoundAt "fx_sensor", sw7
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    If l17.State = 1 Then 'catch ghost
        l17.State = 0
        CatchGhost 1
    End If
    'AddScore 10000
    ' change some light
    LastSwitchHit = "sw7"
' do some check
End Sub

'*************
' PKE lanes
'*************

Sub sw8_Hit()
    DOF 118, DOFPulse
    PlaySoundAt "fx_sensor", sw8
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    if bSkillShotReady Then
        If l28.State = 2 Then
            AwardSkillshot
            'increment bonus x 5
            AddBonusMultiplier 5
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    AddScore 10000
    PKELevel = PKELevel + 150
    IncreasePKEMult
    ' change some light
    Bumper1Light.Visible = 1
    l28.State = 1
    l28b.State = 1
    LastSwitchHit = "sw8"
    ' do some check
    CheckPKELights
End Sub

Sub sw9_Hit()
    DOF 119, DOFPulse
    PlaySoundAt "fx_sensor", sw9
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    if bSkillShotReady Then
        If l29.State = 2 Then
            AwardSkillshot
            'light playfield multiplier lights
            Enable2XMultiplier
            Enable3XMultiplier
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    AddScore 10000
    PKELevel = PKELevel + 150
    IncreasePKEMult
    ' change some light
    Bumper2Light.Visible = 1
    l29.State = 1
    l29b.State = 1
    LastSwitchHit = "sw9"
    ' do some check
    CheckPKELights
End Sub

Sub sw10_Hit()
    DOF 120, DOFPulse
    PlaySoundAt "fx_sensor", sw10
    If Tilted Then Exit Sub
    LaneBonus = LaneBonus + 1
    if bSkillShotReady Then
        If l30.State = 2 Then
            AwardSkillshot
            'add 3 million to super jackpot
            AddSuperJackpot 3000000
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    AddScore 10000
    PKELevel = PKELevel + 150
    IncreasePKEMult
    ' change some light
    Bumper3Light.Visible = 1
    l30.State = 1
    l30b.State = 1
    LastSwitchHit = "sw10"
    ' do some check
    CheckPKELights
End Sub

'********
' Ramps
'********

Sub LeftRampDone_Hit()
    If Tilted Then Exit Sub
    if bSkillShotReady Then
        If l62.State = 2 Then
            AwardSkillshot
            'Start a ramp mode
            If Modes(3) = 0 Then
                StartMode 3
            ElseIF Modes(4) = 0 Then
                StartMode 4
            ElseIF Modes(5) = 0 Then
                StartMode 5
            End If
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    FlashForms LeftRampDome1, 1000, 40, 0
    FlashForms LeftRampDome2, 1000, 40, 0
    DOF 210, DOFPulse
    If l15.State = 1 Then 'catch ghost
        l15.State = 0
        CatchGhost 1
    End If
    Select Case Modes(0)
        Case 0 ' if no mode is active then check to see if there is a ready mode
            If l44.State = 2 Then
                StartMode 3
            ElseIF l45.State = 2 Then
                StartMode 4
            ElseIF l46.State = 2 Then
                StartMode 5
            End If
        Case 1
            SpookedLibrarianHits = SpookedLibrarianHits + 1
            AddScore 5000000
            DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
            CheckSpookedHits
            l66.State = 2 'enable the increase of value of the superjackpot in the spinner
            Exit Sub
        Case 2
            If l62.State = 2 Then
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckBackOffManHits
                Exit Sub
            End If
        Case 3
            If l62.State = 2 Then
                WeGotOneHits = WeGotOneHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckWeGotOneHits
                Exit Sub
            End If
        Case 4
            If l62.State = 2 Then
                HeSlimedMeHits = HeSlimedMeHits + 1
                CheckHeSlimedMeHits
                HeSlimedMeValue = HeSlimedMeValue + 1000000
                DMD CL(0, FormatScore(HeSlimedMeValue)), CL(1, "SLIMER JACKPOT"), "", eBlinkFast, eNone, eNone, 800, True, ""
                Exit Sub
            End If
        Case 5
            If l62.State = 2 Then
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
                Exit Sub
            End If
        Case 6
            If l62.State = 2 Then
                l62.State = 0
                WhoBroughttheDogHits = WhoBroughttheDogHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckWhoBroughttheDogHits
                Exit Sub
            End If
        Case 7
            If l62.State = 2 Then
                If SpookCentralHits > 4 Then
                    l62.State = 0
                End If
                SpookCentralHits = SpookCentralHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckSpookCentralHits
                Exit Sub
            End If
        Case 8
            If l62.State = 2 Then
                SetLightColor l65, "red", 2
                DMD "", CL(1, "GOZERIAN IS LIT"), "", eNone, eBlinkFast, eNone, 800, True, "vo_fireatgozer"
                Exit Sub
            End If
        Case 9
            If l62.State = 2 Then
                l62.State = 0
                SetLightColor l63, "orange", 2
                StayPuftHits = StayPuftHits + 1
                'do something
                'PlaySound""
                FlashForMs StayPuftFlasher, 1500, 50, 0
                LightEffect 2
                Exit Sub
            End If
        Case 10
            AwardJackpot
            Exit Sub
    End Select
    If bTripleSuperJackpot Then
        AwardTripleSuperJackpot
        If NOT bMultiBallMode Then 'turn off the jackpot if not in multiball mode
            bTripleSuperJackpot = False
            l55.State = 0
            l62.State = 0
        End If
        Exit Sub
    End If
    If bSlimerSuperJackpot Then
        AwardSuperJackpot
        bSlimerSuperJackpot = False
        l62.State = 0
        l55.State = 0
        Exit Sub
    End If
    If bSuperJackpot Then
        AwardSuperJackpot
        If NOT bMultiBallMode Then 'turn off the jackpot if not in multiball mode
            bSuperJackpot = False
            l55.State = 0
            l62.State = 0
        End If
        Exit Sub
    End If
    If HiddenJackpot = 2 Then
        AwardJackpot
        Exit Sub
    End If
End Sub

Sub RightRampDone_Hit()
    Dim tmp
    If Tilted Then Exit Sub
    PKELevel = PKELevel + 150
    If bSkillShotReady Then
        If l64.State = 2 Then
            AwardSkillshot
            'start PKE frenzy
            StartPKEfrenzy
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    If l19.State = 1 Then 'catch ghost
        l19.State = 0
        CatchGhost 1
    End If
    If bTerrorDogActive Then
        tmp = "gb_terrordog" &INT(RND * 3 + 1)
        FlashEffect 1
        AddScore TerrorDogAward
        DMD CL(0, FormatScore(TerrorDogAward)), CL(1, "TERROR DOG AWARD"), "", eBlinkFast, eNone, eNone, 800, True, tmp
        TerrorDogAward = TerrorDogAward + 500000
    End If
    FlashForms ContainerFlasher, 1000, 40, 0:DOF 204, DOFPulse
    Select Case Modes(0)
        Case 2
            If l64.State = 2 Then
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckBackOffManHits
                Exit Sub
            End If
        Case 3
            If l64.State = 2 Then
                WeGotOneHits = WeGotOneHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckWeGotOneHits
                Exit Sub
            End If
        Case 5
            If l64.State = 2 Then
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
                Exit Sub
            End If
        Case 7
            If l64.State = 2 Then
                l64.State = 0
                SpookCentralHits = SpookCentralHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckSpookCentralHits
                Exit Sub
            End If
        Case 8
            If l64.State = 2 Then
                SetLightColor l65, "red", 2
                DMD "", CL(1, "GOZERIAN IS LIT"), "", eNone, eBlinkFast, eNone, 800, True, "vo_fireatgozerthegozerian"
                Exit Sub
            End If
        Case 9
            If l64.State = 2 Then
                l64.State = 0
                SetLightColor l63, "orange", 2
                StayPuftHits = StayPuftHits + 1
                'do something
                'PlaySound""
                FlashForMs StayPuftFlasher, 1500, 50, 0
                LightEffect 2
                Exit Sub
            End If
        Case 10
            AwardJackpot
            Exit Sub
    End Select
    If HiddenJackpot = 1 Then
        AwardJackpot
        Exit Sub
    End If
    If l54.State = 1 Then 'PKE Frenzy is active.
        DMD CL(0, FormatScore(PKELevel * PKEMult)), CL(1, "PKE JACKPOT"), "", eBlinkFast, eNone, eNone, 800, True, "gb_fanfare7"
        AddScore PKELevel * PKEMult
        IncreasePKEMult
    End If
    If l54.State = 2 Then 'activate PKE frenzy and the PKE Meter
        l70.State = 1
        Gear(3) = 1
        CheckGear
        StartPKEfrenzy
    End If
End Sub

'********
' Bumper
'********

Dim b1, b2, b3 'the values of the actual award for each bumper. Get 3 of a kind and get an award

Sub ResetBumpers()
    b1 = INT(RND * 30)
    b2 = INT(RND * 30)
    b3 = INT(RND * 30)
End Sub

Sub Bumper1_Hit
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    PlaySoundAt SoundFXDOF("fx_bumper", 107, DOFPulse, DOFContactors), Bumper1
    DOF 108, DOFPulse
    FlashForMs BumperFlash, 500, 50, 0:FlashForMs BumperFlash1, 500, 50, 0
    AddScore 500 + 4500 * Bumper1Light.Visible 'a bumper scores 500 points and 5000 points when lit
    If b1 <> b2 AND b1 <> b3 Then
        b1 = (b1 + 1)MOD 30
    End If
    CheckBumpers
End Sub

Sub Bumper2_Hit
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    PlaySoundAt SoundFXDOF("fx_bumper", 109, DOFPulse, DOFContactors), Bumper2
    DOF 110, DOFPulse
    FlashForMs BumperFlash, 500, 50, 0:FlashForMs BumperFlash1, 500, 50, 0
    AddScore 500 + 4500 * Bumper1Light.Visible 'a bumper scores 100 points and 1000 points when lit
    If b2 <> b1 AND b2 <> b3 Then
        b2 = (b2 + 1)MOD 30
    End If
    CheckBumpers
End Sub

Sub Bumper3_Hit
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    PlaySoundAt SoundFXDOF("fx_bumper", 111, DOFPulse, DOFContactors), Bumper3
    DOF 112, DOFPulse
    FlashForMs BumperFlash, 500, 50, 0:FlashForMs BumperFlash1, 500, 50, 0
    AddScore 500 + 4500 * Bumper1Light.Visible 'a bumper scores 100 points and 1000 points when lit
    If b3 <> b1 AND b3 <> b2 Then
        b3 = (b3 + 1)MOD 30
    End If
    CheckBumpers
End Sub

' Check the bumper awards

Sub CheckBumpers()
    ' 1st display the awards
    Dim tmp
    tmp = chr(131 + b1) & chr(131 + b2) & chr(131 + b3)
    DMD "", "", tmp, eNone, eNone, eNone, 100, True, ""

    If b1 = b2 AND b1 = b3 Then 'give award
        DMD "", "", tmp, eNone, eNone, eBlinkFast, 1000, True, "gb_fanfare"
        GiveBumperAward
    End If
End Sub

Sub GiveBumperAward()
    Select Case b1
        Case 1:l58.State = 2:PlaySound "vo_tobinspiritguideislit" 'Light Tobin's Spirit Guide
        Case 2:StartTerrorDog                                     'Start Terror Dog
        Case 3:StartScoleriBrothers                               'Start Scoleri Brothers
        Case 4:StartScoleriBrothers                               'Start Scoleri Brothers
        Case 5:LitRandomGhost:LitRandomGhost:LitRandomGhost       'Light Three Ghosts
        Case 6:LitRandomGhost:LitRandomGhost                      'Light two Ghosts
        Case 7:StorageLights 1                                    'Light Storage Facility
        Case 8:Enable2XMultiplier:Enable3XMultiplier              'Light Playfield Multipliers
        Case 9:l53.State = 2                                      'Light Extra Ball
        Case 10:AddSuperJackpot 1000000                           'Increase Super Jackpot
        Case 11:SpinnerLevel = SpinnerLevel + 1                   'Increase Spinner Level
        Case 12:SpinnerValue = SpinnerValue + 1000                'Increase Spinner Value
        Case 13:CatchGhost 3                                      'Catch Three Ghosts
        Case 14:CatchGhost 2                                      'Catch two Ghosts
        Case 15:CatchGhost 1                                      'Catch One Ghost
        Case 16:SpinnerValue = SpinnerValue + 1000                'Bump Spinner Value
        Case 17:bBonusHeld = True                                 'Bonus Held
        Case 18:AddScore INT(RND * 9 + 1) * 200000                'Bigger Points
        Case 19:AddScore INT(RND * 9 + 1) * 100000                'Big Points
        Case 20:AwardSpecial                                      'Award Special
        Case 21:SpinnerLevel = SpinnerLevel + 1                   'Advance Spinner Level
        Case 22:AddBonusMultiplier 3                              'Advance Bonus X by 3X
        Case 23:AddBonusMultiplier 1                              'Advance Bonus X by 1X
        Case 24:AddTime                                           'Add Time
        Case 25                                                   'Lit Add A Ball
            If bMultiBallMode Then
                AddMultiball 1
            Else
                l57.State = 2
            End If
        Case 26:PKELevel = PKELevel + 300 '+300 PKE Level
        Case 27:PKELevel = PKELevel + 100 '+100 PKE Level
        Case 28:AddBonusMultiplier 3      '+3 Bonus X
        Case 29:AddBonusMultiplier 2      '+2 Bonus X
        Case 30:AddBonusMultiplier 1      '+1 Bonus X
    End Select

    ResetBumpers
End Sub

'**************
' Proton Targets
'**************

Sub Target1_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 113, DOFPulse, DOFTargets), Target1
    If Tilted Then Exit Sub
    FlashForms Protonflasher, 300, 40, 0
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    IncreasePKEMult
    If l10.State = 2 Then
        AwardProtonPack
    Else
        l73.State = 1
        AddScore 5000
        PKELevel = PKELevel + 150
        CheckProtonTargets
    End If
    LastSwitchHit = "target1"
End Sub

Sub Target2_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 113, DOFPulse, DOFTargets), Target2
    If Tilted Then Exit Sub
    FlashForms Protonflasher, 300, 40, 0
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    IncreasePKEMult
    If l10.State = 2 Then
        AwardProtonPack
    Else
        l74.State = 1
        AddScore 5000
        PKELevel = PKELevel + 150
        CheckProtonTargets
    End If
    LastSwitchHit = "target2"
End Sub

Sub Target3_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 113, DOFPulse, DOFTargets), Target3
    If Tilted Then Exit Sub
    FlashForms Protonflasher, 300, 40, 0
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    IncreasePKEMult
    If l10.State = 2 Then
        AwardProtonPack
    Else
        l75.State = 1
        AddScore 5000
        PKELevel = PKELevel + 150
        CheckProtonTargets
    End If
    LastSwitchHit = "target3"
End Sub

Sub AwardProtonPack()
    l10.State = 0
    AddScore PKELevel * PKEMult
    DMD CL(0, FormatScore(PKELevel * PKEMult)), CL(1, "PROTON PACK COLLECTED"), "", eBlinkFast, eNone, eNone, 1500, True, "gb_protonpack"
    Flasheffect 1
    l68.State = 1
    l73.State = 0
    l74.State = 0
    l75.State = 0
    ' check if you got all the gear
    Gear(1) = 1
    CheckGear
End Sub

'*******************
' Multiplier targets
'*******************

Sub Target4_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 115, DOFPulse, DOFTargets), Target4
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    If l36.State = 2 Then
        l36.State = 1
        Multiplier2x = 2
        DMD "PLAYFIELD MULTIPLIER", CL(1, "IS NOW " & Multiplier3x * Multiplier2x & " X"), "", eNone, eBlinkFast, eNone, 1500, True, "gb_fanfare6"
        TwoXTimerExpired.Enabled = 0:TwoXTimerExpired.Enabled = 1
        TwoXTimerBlink.Enabled = 0:TwoXTimerBlink.Enabled = 1
    End If
    AddScore 1500
    LastSwitchHit = "target4"
End Sub

Sub Target5_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 115, DOFPulse, DOFTargets), Target5
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    If l37.State = 2 Then
        l37.State = 1
        Multiplier3x = 3
        DMD "PLAYFIELD MULTIPLIER", CL(1, "IS " & Multiplier3x * Multiplier2x & " X"), "", eNone, eBlinkFast, eNone, 1500, True, "gb_fanfare6"
        ThreeXTimerExpired.Enabled = 0:ThreeXTimerExpired.Enabled = 1
        ThreeXTimerExpired.Enabled = 0:ThreeXTimerExpired.Enabled = 1
    End If
    AddScore 1500
    LastSwitchHit = "target5"
End Sub

Sub TwoXTimerExpired_Timer()
    TwoXTimerExpired.Enabled = 0
    TwoXTimerBlink.Enabled = 0
    l36.State = 0
    Multiplier2x = 1
End Sub

Sub TwoXTimerBlink_Timer()
    TwoXTimerBlink.Enabled = 0
    l36.BlinkInterval = 50
    l36.State = 2
End Sub

Sub ThreeXTimerExpired_Timer()
    ThreeXTimerExpired.Enabled = 0
    ThreeXTimerBlink.Enabled = 0
    l37.State = 0
    Multiplier3x = 1
End Sub

Sub ThreeXTimerBlink_Timer()
    ThreeXTimerBlink.Enabled = 0
    l37.BlinkInterval = 50
    l37.State = 2
End Sub

Sub Enable2xMultiplier() ' blink the light so it can be activated
    l36.BlinkInterval = 100
    l36.State = 2
End Sub

Sub Enable3xMultiplier()
    l37.BlinkInterval = 100
    l37.State = 2
End Sub

'********************
' Slimer/Ghost target
'********************

Dim GhostTargetHits, GhostTargetLevel

Sub Target6_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 115, DOFPulse, DOFTargets), Target6
    If Tilted Then Exit Sub
    FlashForms SlimerFlasher, 800, 40, 0
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    LitRandomGhost
    GhostTargetHits = GhostTargetHits + 1
    LitGhostLetter
    AddScore 5500
    LastSwitchHit = "target6"
End Sub

Sub InitGhostTarget
    GhostTargetLevel = 0 'start with 5 ghost target lights on
    GhostTargetHits = 5 - GhostTargetLevel
    LitGhostLetter
End Sub

Sub IncreaseGhostLevel
    GhostTargetLevel = GhostTargetLevel + 1
    If GhostTargetLevel > 5 Then GhostTargetLevel = 5
    GhostTargetHits = 5 - GhostTargetLevel
    LitGhostLetter
End Sub

Sub LitGhostLetter()
    Select Case GhostTargetHits
        Case 0:l31.State = 2:l32.State = 0:l33.State = 0:l34.State = 0:l35.State = 0
        Case 1:l31.State = 1:l32.State = 2:l33.State = 0:l34.State = 0:l35.State = 0:PlaySound "vo_completetheghostletters"
        Case 2:l31.State = 1:l32.State = 1:l33.State = 2:l34.State = 0:l35.State = 0
        Case 3:l31.State = 1:l32.State = 1:l33.State = 1:l34.State = 2:l35.State = 0
        Case 4:l31.State = 1:l32.State = 1:l33.State = 1:l34.State = 1:l35.State = 2
        Case 5:l31.State = 2:l32.State = 2:l33.State = 2:l34.State = 2:l35.State = 2:FlashForms SlimerFlasher, 800, 40, 1
        Case 6:LightEffect 1:SlimerMoveUp
    End Select
End Sub

'****************
' Gozer target
'****************

Sub Target7_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 116, DOFPulse, DOFTargets), Target7
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    Select Case Modes(0)
        Case 2
            If l65.State = 2 Then
                FlashForMs LeftRampDome2, 1500, 50, 0
                DOF 212, DOFPulse
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckBackOffManHits
                Exit Sub
            End If
        Case 5
            If l65.State = 2 Then
                FlashForMs LeftRampDome2, 1500, 50, 0
                DOF 212, DOFPulse
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
                Exit Sub
            End If
        Case 8
            If l65.State = 2 Then
                FlashForMs LeftRampDome2, 1500, 50, 0
                DOF 212, DOFPulse
                GozerianHits = GozerianHits + 1
                DMD CL(0, FormatScore(GozerianValue)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                Addscore GozerianValue
                CheckGozerianHits
                Exit Sub
            End If
        Case 9
            If l65.State = 2 Then
                l65.State = 0
                SetLightColor l63, "orange", 2
                StayPuftHits = StayPuftHits + 1
                'do something
                'PlaySound""
                FlashForMs StayPuftFlasher, 1500, 50, 0
                LightEffect 2
                Exit Sub
            End If
    End Select
    If l76.State = 2 Then 'Gozer Hurry Up is active
        GozerHurryUpExpired_Timer
        AddScore GozerAward
        DMD CL(0, FormatScore(GozerAward)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
        Exit Sub
    Else
        AddScore 5000
    End If
    LastSwitchHit = "target7"
End Sub

'********************
' Terror Dog target
'********************

Sub Target8_Hit()
    Dim tmp
    PlaySoundAt SoundFXDOF("fx_target", 117, DOFPulse, DOFTargets), Target8
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    If bTerrorDogActive Then
        tmp = "gb_terrordog" &INT(RND * 3 + 1)
        FlashEffect 1
        AddScore TerrorDogAward
        DMD CL(0, FormatScore(TerrorDogAward)), "TERROR DOG AWARD", "", eBlinkFast, eNone, eNone, 800, True, tmp
        TerrorDogAward = TerrorDogAward + 500000
    Else
        FlashForms TerrorDogFlasher, 800, 40, 0
        AddScore 5000
    End If
    LastSwitchHit = "target8"
End Sub

'*******************************
' Books target: captured ball 1
'*******************************

Sub Target9_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 115, DOFPulse, DOFTargets), Target9
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    FlashForMs BooksFlasher, 1500, 50, 0
    LitGhostLetter
    If l47.State = 2 Then
        AdvanceSymmetricalBook
    End If
    If l47.State = 0 Then
        Flashforms l47, 10000, 100, 0
    End If
    AddScore 2500
    LastSwitchHit = "target9"
End Sub

'**************************************
' Firestation target: left captured ball
'**************************************

Sub Target10_Hit()
    PlaySoundAt SoundFXDOF("fx_target", 114, DOFPulse, DOFTargets), Target10
    If Tilted Then Exit Sub
    If bSkillShotReady Then ResetSkillShotTimer_Timer
    If l18.State = 1 Then 'catch ghost
        l18.State = 0
        CatchGhost 1
    End If
    FlashForms FirestationFlasher1, 800, 40, 0
    FlashForms FirestationFlasher2, 800, 40, 0
    Select Case Modes(0)
        Case 5
            If l59.State = 2 Then
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
                Exit Sub
            End If
    End Select
    'check the target hits
    FirestationHoleHits = FirestationHoleHits + 1
    CheckFirestationLights
    AddScore 5000 + SpinnerValue * SpinnerLevel
    LastSwitchHit = "target10"
End Sub

Sub CheckFirestationLights() ' select lights depending on the number of hits, and give the corresponding award
    LightSeqFirestation.Play SeqRandom, 6, , 1000
    Select Case FirestationHoleHits
        Case 1:l38.State = 2:l59.State = 2
        Case 2:l38.State = 1:l59.State = 0:StorageLights 1
        Case 3:l39.State = 2:l59.State = 2
        Case 4:l39.State = 1:l59.State = 0:l57.State = 2:DMD "", "ADD A BALL IS LIT", "", eNone, eBlinkFast, eNone, 1500, True, "gb_fanfare6":FlashEffect 1
        Case 5:l40.State = 2:l59.State = 2
        Case 6:l40.State = 1:l59.State = 0:Enable2xMultiplier:Enable3xMultiplier:DMD "", "MULTIPLIERS ARE LIT", "", eNone, eBlinkFast, eNone, 1500, True, "gb_fanfare6":FlashEffect 1
        Case 7:l41.State = 2:l59.State = 2
        Case 8
            l38.State = 0:l39.State = 0:l40.State = 0:l41.State = 0:l59.State = 0
            DMD "", "SUPERJACKPOT IS LIT", "", eNone, eBlinkFast, eNone, 1500, True, "gb_fanfare6"
            FlashEffect 2
            bSuperJackpot = True
            SetLightColor l62, "red", 2
            l55.State = 2
            FirestationHoleHits = 0
    End Select
End Sub

'**************************
' LeftScoop: Tobin's Spirit
'**************************

Sub LeftScoop_Hit()
    Dim tmp
    PlaySoundAt "fx_kicker_enter",LeftScoop
    If Tilted Then
        LeftScoopExit
        Exit Sub
    End If
    if bSkillShotReady Then
        If l60.State = 2 Then
            AwardSkillshot
            'start left scoop mode skillshot
            If Modes(1) = 0 Then
                StartMode 1
            ElseIF Modes(2) = 0 Then
                StartMode 2
            End If
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    If l53.State = 2 Then ' extra Ball
        l53.State = 0
        AwardExtraBall
    End If
    Select Case Modes(0)
        Case 0 ' if no mode is active then check to see if there is a ready mode
            If l42.State = 2 Then
                StartMode 1
            ElseIF l43.State = 2 Then
                StartMode 2
            End If
        Case 2
            If l60.State = 2 Then
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, "gb_blast1"
                CheckBackOffManHits 'the ball will exit from the CheckBackOffManHits routine
                Exit Sub
            End If
        Case 5
            If l60.State = 2 Then
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, "gb_blast2"
                CheckTheBallroomHits
                vpmtimer.addtimer 1000, "FlashLeftScoop '"
                vpmtimer.addtimer 2000, "LeftScoopExit '"
                Exit Sub
            End If
    End Select

    If l57.State = 2 AND bMultiBallMode Then ' add a multiball if running a multiball mode
        AddMultiball 1
        tmp = INT(RND * 3)
        Select Case tmp
            Case 0:tmp = "vo_addaball"
            Case 1:tmp = "vo_balladded"
            Case 2:tmp = "vo_balladded2"
        End Select
        DMD CL(0, "ADD-A-BALL"), "", "", eBlinkFast, eNone, eNone, 800, True, tmp
        l57.State = 0
    End If
    If l58.State = 2 Then ' give tobin Award
        TobinMysteryAward 'after the tobin animation it will kick the ball out
        Exit Sub
    End If
    If l25.State Then
        LibrarianAward 'after the librarian award it will kick the ball out
        Exit Sub
    Else
        l25.State = 2
    End If
    vpmtimer.addtimer 1000, "FlashLeftScoop '"
    vpmtimer.addtimer 2000, "LeftScoopExit '"
End Sub

Sub FlashLeftScoop
    FlashForms l5f, 1000, 40, 0
    FlashForms LibraryFlasher, 1000, 40, 0
    DOF 206, DOFPulse
End Sub

Sub LeftScoopExit
    PlaySoundAt SoundFXDOF("fx_kicker", 122, DOFPulse, DOFContactors), LeftScoop
    DOF 123, DOFPulse
    leftScoop.Kick 150, 25
	leftScoopk.IsDropped = 0
	vpmtimer.addtimer 100, "leftScoopK.IsDropped = 1 '"
End Sub

Sub TobinMysteryAward() 'n is the number of awards it will give you
    Dim tmp, tmp2, ii
    l58.State = 1
    DMD "TOBINS SPIRIT GUIDE", "   +3X BONUS X", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "   +100 PKE LEVEL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "  START BROTHERS", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "    BONUS HELD", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "BUMP SPINNER LEVEL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "   LIT EXTRA BALL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "  START PKE FRENZY", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", " LIT STORAGE FACIL.", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "  START TERROR DOG", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "    BIG POINTS", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "TOBINS SPIRIT GUIDE", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "    BONUS HELD", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "BUMP SPINNER VALUE", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "   LIT EXTRA BALL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "  START PKE FRENZY", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", " LIT STORAGE FACIL.", "", eNone, eNone, eNone, 25, False, "fx_spinner"

    For ii = 1 to 5
        If ii < 3 Then
            tmp = INT(RND * 15)
        Else
            tmp = INT(RND * 50)
        End If
        Select Case tmp
            Case 0, 33: '+3X Bonus Multiplier
                DMD "TOBINS SPIRIT GUIDE", "   +3X BONUS X", "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                AddBonusMultiplier 3
            Case 1, 34, 47: ' +100 PKE Level
                DMD "TOBINS SPIRIT GUIDE", CL(1, "+100 PKE LEVEL"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                PKELevel = PKELevel + 100
            Case 2, 35: ' +300 PKE Level
                DMD "TOBINS SPIRIT GUIDE", CL(1, "+300 PKE LEVEL"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                PKELevel = PKELevel + 300
            Case 3, 36: ' +1M Super Jackpot Value
                DMD "TOBINS SPIRIT GUIDE", CL(1, "+1M SUPER JACKPOT"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                AddSuperJackpot 1000000
            Case 4, 37: ' +10M Super Jackpot Value
                DMD "TOBINS SPIRIT GUIDE", CL(1, "+10M SUPER JACKPOT"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                AddSuperJackpot 10000000
            Case 5, 20, 38, 48, 45: ' Big Points
                DMD "TOBINS SPIRIT GUIDE", CL(1, "BIG POINTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                tmp2 = INT(RND * 9 + 1) * 100000
                AddScore tmp2
            Case 6, 39, 49: ' Bigger Points
                DMD "TOBINS SPIRIT GUIDE", CL(1, "BIGGER POINTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                tmp2 = INT(RND * 9 + 1) * 100000 + 1000000
                AddScore tmp2
            Case 7, 40: ' Biggest Points
                DMD "TOBINS SPIRIT GUIDE", CL(1, "BIGGEST POINTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                tmp2 = INT(RND * 9 + 1) * 100000 + 2000000
                AddScore tmp2
            Case 8, 30: ' Bonus Held
                DMD "TOBINS SPIRIT GUIDE", CL(1, "BONUS HELD"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                bBonusHeld = True
            Case 9, 41: ' Bump Spinner Value
                DMD "TOBINS SPIRIT GUIDE", CL(1, "BUMP SPINNER LEVEL"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                SpinnerLevel = SpinnerLevel + 2
            Case 10: ' Capture X Ghosts ???
                tmp2 = INT(RND * 9 + 1)
                DMD "TOBINS SPIRIT GUIDE", CL(1, "CAPTURED " &tmp2& " GHOSTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                CatchGhost tmp2
            Case 11, 27: ' Give Light Storage Facility
                StorageLights 1
            Case 12, 42: ' Increase Spinner Value
                DMD "TOBINS SPIRIT GUIDE", CL(1, "+ SPINNER VALUE"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                SpinnerValue = SpinnerValue + 1000
            Case 13: ' Light 2x Playfield Multiplier
                DMD "TOBINS SPIRIT GUIDE", CL(1, "2X IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                Enable2XMultiplier
            Case 14: ' Light Extra Ball
                DMD "TOBINS SPIRIT GUIDE", CL(1, "EXTRABALL IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_extraballislit"
                l53.State = 2
            Case 15: ' Light Negative Reinforcement
                DMD "TOBINS SPIRIT GUIDE", CL(1, "NROEA IS LIT"), "",eNone, eBlinkFast, eNone, 1000, True, "vo_nroespa"
                l24.State = 2
            Case 16, 43: ' Light One Ghost
                DMD "TOBINS SPIRIT GUIDE", CL(1, "LIT ONE GHOST"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                LitRandomGhost
            Case 17, 44: ' Light Two Ghosts
                DMD "TOBINS SPIRIT GUIDE", CL(1, "LIT TWO GHOSTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                LitRandomGhost
                LitRandomGhost
            Case 18: ' Light Modes
                DMD "TOBINS SPIRIT GUIDE", CL(1, "LIT MODES"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                LitNextModes
            Case 19: ' Light Super Jackpot
                DMD "TOBINS SPIRIT GUIDE", "SUPERJACKPOT IS LIT", "",eNone, eBlinkFast, eNone, 1000, True, "vo_getthesuperjackpot"
                bSuperJackpot = True
                SetLightColor l62, "red", 2
                l55.State = 2
            Case 21, 46: ' Spot GHOST
                DMD "TOBINS SPIRIT GUIDE", CL(1, "SPOT GHOST"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                GhostTargetHits = 5
                LitGhostLetter
            Case 22: ' Start Brothers
                DMD "TOBINS SPIRIT GUIDE", CL(1, "SCOLERI BROTHERS"), "",eNone, eBlinkFast, eNone, 1000, True, ""
                StartScoleriBrothers
            Case 23: ' Start Gozer Hurry Up
                DMD "TOBINS SPIRIT GUIDE", CL(1, "GOZER HURRY UP"), "",eNone, eBlinkFast, eNone, 1000, True, "vo_aimatgozer"
                StartGozerHurryUp
            Case 24: ' Start Loopin Supers
                DMD "TOBINS SPIRIT GUIDE", CL(1, "LOOPIN SUPERS"), "",eNone, eBlinkFast, eNone, 1000, True, ""
                StartLoopinSupers
            Case 25: ' Start PKE Frenzy
                StartPKEfrenzy
            Case 26: ' Light Playfield Multipliers
                DMD "TOBINS SPIRIT GUIDE", "MULTIPLIERS ARE LIT", "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                Enable2XMultiplier:Enable3XMultiplier
            Case 28: ' Start Terror Dog Hurry Up
                DMD "TOBINS SPIRIT GUIDE", CL(1, "TERROR DOG"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                StartTerrorDog
            Case 29: ' Symmetrical Book Stacking
                DMD "TOBINS SPIRIT GUIDE", CL(1, "BOOK STACKING"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare6"
                AdvanceSymmetricalBook
            Case 21: ' Award Special
                AwardSpecial
            Case 32: ' Award Extra Ball
                AwardExtraBall
        End Select
    Next
    l58.State = 0
    vpmtimer.addtimer 8000, "FlashLeftScoop '"
    vpmtimer.addtimer 10000, "LeftScoopExit '"
End Sub

Sub LibrarianAward() 'similar to Tobin's award, but only gives one award - do not start any modes
    Dim tmp, tmp2
    DMD " LIBRARIANS AWARD", "    +3X BONUS X", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "   +100 PKE LEVEL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "    BONUS HELD", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "BUMP SPINNER LEVEL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "   LIT EXTRA BALL", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "  START PKE FRENZY", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "    BIG POINTS", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "    BONUS HELD", "", eNone, eNone, eNone, 25, False, "fx_spinner"
    DMD "_", "BUMP SPINNER VALUE", "", eNone, eNone, eNone, 25, False, "fx_spinner"

    tmp = INT(RND * 20)
    Select Case tmp
        Case 0, 1: ' +100 PKE Level
            DMD " LIBRARIANS AWARD", CL(1, "+100 PKE LEVEL"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            PKELevel = PKELevel + 100
        Case 2, 3: ' +1M Super Jackpot Value
            DMD " LIBRARIANS AWARD", CL(1, "+1M SUPER JACKPOT"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            AddSuperJackpot 1000000
        Case 4, 5: ' Big Points
            DMD " LIBRARIANS AWARD", CL(1, "BIG POINTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            tmp2 = INT(RND * 5 + 1) * 100000
            AddScore tmp2
        Case 6, 7: ' Bigger Points
            DMD " LIBRARIANS AWARD", CL(1, "BIGGER POINTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            tmp2 = INT(RND * 5 + 5) * 100000
            AddScore tmp2
        Case 8: ' Bonus Held
            DMD " LIBRARIANS AWARD", CL(1, "BONUS HELD"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            bBonusHeld = True
        Case 9: ' Bump Spinner Value
            DMD " LIBRARIANS AWARD", CL(1, "BUMP SPINNER LEVEL"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            SpinnerLevel = SpinnerLevel + 2
        Case 10: ' Capture X Ghosts ???
            tmp2 = INT(RND * 4 + 1)
            DMD " LIBRARIANS AWARD", CL(1, "CAPTURED " &tmp2& " GHOSTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            CatchGhost tmp2
        Case 11, 12: ' Increase Spinner Value
            DMD " LIBRARIANS AWARD", CL(1, "+ SPINNER VALUE"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            SpinnerValue = SpinnerValue + 1000
        Case 13: ' Light 2x Playfield Multiplier
            DMD " LIBRARIANS AWARD", CL(1, "2X IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            Enable2XMultiplier
        Case 14, 15, 16: ' Light One Ghost
            DMD " LIBRARIANS AWARD", CL(1, "LIT ONE GHOST"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            LitRandomGhost
        Case 17: ' Light Two Ghosts
            DMD " LIBRARIANS AWARD", CL(1, "LIT TWO GHOSTS"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            LitRandomGhost
            LitRandomGhost
        Case 18, 19: ' Spot GHOST
            DMD " LIBRARIANS AWARD", CL(1, "SPOT GHOST"), "", eNone, eBlinkFast, eNone, 1000, True, "gb_fanfare7"
            GhostTargetHits = 5
            LitGhostLetter
    End Select
    l25.State = 0
    vpmtimer.addtimer 1000, "FlashLeftScoop '"
    vpmtimer.addtimer 2000, "LeftScoopExit '"
End Sub

'******************
' RightScoop: NROEA
'******************

Sub RightScoop_Hit()
    PlaySoundAt "fx_kicker_enter", RightScoop
    If Tilted Then
        RightScoopExit
        Exit Sub
    End If
    if bSkillShotReady Then
        If l67.State = 2 Then
            AwardSkillshot
            'LIGHT NROESPA
            DMD "", CL(1, "NROEA IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_nroespa"
            l24.State = 2
        Else
            ResetSkillShotTimer_Timer
        End If
    End If
    If l13.State = 1 Then 'catch ghost
        l13.State = 0
        CatchGhost 1
    End If
    Select Case Modes(0)
        Case 1
            SpookedLibrarianHits = SpookedLibrarianHits + 1
            AddScore 5000000
            DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
            CheckSpookedHits
            l66.State = 2 'enable the increase of value of the superjackpot in the spinner
        Case 2
            If l67.State = 2 Then
                BackOffManHits = BackOffManHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckBackOffManHits
            End If
        Case 5
            If l67.State = 2 Then
                TheBallroomHits = TheBallroomHits + 1
                AddScore 5000000
                DMD CL(0, FormatScore(5000000)), "", "", eBlinkFast, eNone, eNone, 800, True, ""
                CheckTheBallroomHits
            End If
    End Select
    vpmtimer.addtimer 1000, "FlashRightScoop '"
    vpmtimer.addtimer 2000, "RightScoopExit '"
End Sub

Sub FlashRightScoop
    FlashForms l6f, 1000, 40, 0
    FlashForms ContainerFlasher, 1000, 40, 0:DOF 204, DOFPulse
End Sub

Sub RightScoopExit
    PlaySoundAt SoundFXDOF("fx_kicker", 124, DOFPulse, DOFContactors),RightScoop
    DOF 123, DOFPulse
    RightScoop.Kick 226, 16
	RightScoopk.IsDropped = 0
	vpmtimer.addtimer 100, "RightScoopk.IsDropped = 1 '"
End Sub

'*****************
' Collecting Gear
'*****************
'Every ghostbuster needs the following gear:
'  · Proton Pack 	Gear(1)
'  · Ghost Trap		Gear(2)
'  · PKE Meter		Gear(3)
'  · Ecto Goggles	Gear(4)
'  · Jump Suit		Gear(5)
' To collect the gear:
' -shoot proton targets to collect the proton pack
' -defeat Slimer twice to collect the Ghost Trap
' -light the 3 PKE lights at the top lanes to collect the PKE meter
' -shoot the right loop 3 times to collect the Ecto Goggles & to activate them
' -hit 4 times the flipper inlanes to get the Jump Suit

Sub CheckGear() 'when all the gear is collected then start "Are you a god?" mode (mode 12)
    If Gear(1) + Gear(2) + Gear(3) + Gear(4) + Gear(5) = 5 Then
        StartAreYouaGod
    End If
End Sub

' Gear 1: Proton Pack
Sub CheckProtonTargets()
    If l73.State + l74.State + l75.State = 3 Then
        PKELevel = PKELevel + 300
        LightEffect 2
        light1.State = 2
        light2.State = 2
        light3.State = 2
        light4.State = 2
        l73.State = 2
        l74.State = 2
        l75.State = 2
        l10.State = 2
    End If
End Sub

' Gear 2 Ghost Trap
Sub SlimerHitCheck
    If SlimerHits = 3 Then 'defeat slimer
        PlaySound "gb_slimerscream"
        SlimerHits = 0
        SlimerDefeats = SlimerDefeats + 1
        If SlimerDefeats >= 2 Then ' get the Ghost Trap
            DMD "", CL(1, "GHOST TRAP COLLECTED"), "", eNone, eBlink, eNone, 800, True, ""
            SlimerDefeats = 0
            LightEffect 2
            l69.State = 1
            Gear(2) = 1
            CheckGear
        End If
        FlashEffect 3
        LightEffect 1
        SlimerMoveDown
        SlimePlayfield
        LitNextModes
        CatchGhost 20
    Else
        If SlimerHits = 2 Then
            PlaySound "vo_aimforslimer"
        End If
        PlaySound "gb_slimer1"
        FlashEffect 1
    End If
End Sub

' Gear 3 PKE Meter
Sub CheckPKELights()
    If l28.State + l29.State + l30.State = 3 Then
        PlaySound "gb_pke"
        PKELevel = PKELevel + 300
        LightSeqPKElanes.Play SeqRandom, 3, , 2000
        FlashForms Bumper1Light, 2000, 40, 0
        FlashForms Bumper2Light, 2000, 40, 0
        FlashForms Bumper3Light, 2000, 40, 0
        l28.State = 0
        l29.State = 0
        l30.State = 0
        l28b.State = 0
        l29b.State = 0
        l30b.State = 0
        l54.State = 2 'ready to start PKE Frenzy at the right ramp and to pick up the pke meter
    End If
End Sub

' Gear 4 Ecto Googles
Sub CheckEctoHits
    FlashForms ectoFlasher, 800, 40, 0
    If ectoHits = 3 Then
        l71.State = 1
        HologramStart
        Gear(4) = 1
        CheckGear
    End If
    If ectoHits > 3 then 'When the hologram is active you may capture 3 to 5 ghosts
        CatchGhost INT(RND * 3 + 3)
    End If
End Sub

' Gear 5 Jump Suit
Sub CheckJumpSuitHits
    If JumpSuitHits >= 4 Then
        JumpSuitHits = 0
        l72.State = 1
        Gear(5) = 1
        CheckGear
    End If
End Sub

'*************************
'Slimer UP/DOWN Animation
'*************************

Dim SlimerPos, SlimerDir, SlimerShakePos, SlimerShakeDir, SlimerHitPos, SlimerHits
Dim bSlimerUp, bPlayfieldSlimed

SlimerPos = -105
SlimerShakePos = 0

Sub SlimerLocation(param)
    Select Case param
        Case 1:Slimer.X = 198:Slimer.Y = 682:SlimerTrigger1.Enabled = 1:SlimerTrigger2.Enabled = 0:SlimerTrigger3.Enabled = 0:Camera.RotZ = 200
        Case 2:Slimer.X = 291:Slimer.Y = 636:SlimerTrigger2.Enabled = 1:SlimerTrigger1.Enabled = 0:SlimerTrigger3.Enabled = 0:Camera.RotZ = 210
        Case 3:Slimer.X = 397:Slimer.Y = 615:SlimerTrigger3.Enabled = 1:SlimerTrigger1.Enabled = 0:SlimerTrigger2.Enabled = 0:Camera.RotZ = 220
    End Select
End Sub

Sub SlimerAnimTimer_Timer()
    SlimerShakeTimer.Enabled = 0
    SlimerLocationTimer.Enabled = 0
    SlimerPos = SlimerPos + SlimerDir
    'Slimer is moving up
    If SlimerPos >= 0 Then
        DOF 127, DOFOff
        Me.Enabled = 0
        SlimerPos = 0
        SlimerShakeDir = 1
        SlimerShakeTimer.Enabled = 1
        SlimerLocationTimer.Enabled = 1
        bSlimerUp = True
    End If
    'Slimer is moving down
    If SlimerPos <= -105 Then
        DOF 127, DOFOff
        Me.Enabled = 0
        SlimerPos = -105
        SlimerTrigger1.Enabled = 0
        SlimerTrigger2.Enabled = 0
        SlimerTrigger3.Enabled = 0
        IncreaseGhostLevel
    End If
    Slimer.Transz = SlimerPos
    Camera.RotX = - SlimerPos / 4
End Sub

Sub SlimerShakeTimer_Timer
    SlimerShakePos = SlimerShakePos + SlimerShakeDir
    'Slimer is moving up
    If SlimerShakePos > 10 Then
        SlimerShakeDir = -1
    End If
    'Slimer is moving down
    If SlimerShakePos < 0 Then
        SlimerShakeDir = 1
    End If
    Slimer.Transz = SlimerShakePos
    Camera.RotX = - SlimerShakePos / 4
End Sub

Sub SlimerMoveUp()
    PlaySound "gb_slimer2"
    PlaySound "vo_slimer1"
    SlimerLocation INT(RND * 3 + 1)
    SlimerDir = 2
    SlimerAnimTimer.Enabled = 1
    DOF 127, DOFOn
End Sub

Sub SlimerMoveDown()
    SlimerDir = -2
    SlimerAnimTimer.Enabled = 1
    bSlimerUp = False
    DOF 127, DOFOn
End Sub

Sub SlimerTrigger1_Hit()
    If bSlimerUp AND ActiveBall.VelY < 0 Then
        If Modes(0) = 4 Then
            CompleteHeSlimedMe
            Exit Sub
        End If
        SlimerHitPos = 6:SlimerHitTimer.Enabled = 1
        DOF 139, DOFOn
        SlimerHits = SlimerHits + 1
        SlimerHitCheck
    End If
End Sub

Sub SlimerTrigger2_Hit()
    If bSlimerUp AND ActiveBall.VelY < 0 Then
        If Modes(0) = 4 Then
            CompleteHeSlimedMe
            Exit Sub
        End If
        SlimerHitPos = 6:SlimerHitTimer.Enabled = 1
        DOF 139, DOFOn
        SlimerHits = SlimerHits + 1
        SlimerHitCheck
    End If
End Sub

Sub SlimerTrigger3_Hit()
    If bSlimerUp AND ActiveBall.VelY < 0 Then
        If Modes(0) = 4 Then
            CompleteHeSlimedMe
            Exit Sub
        End If
        SlimerHitPos = 6:SlimerHitTimer.Enabled = 1
        DOF 139, DOFOn
        SlimerHits = SlimerHits + 1
        SlimerHitCheck
    End If
End Sub

Sub SlimerHitTimer_Timer()
    Slimer.TransX = SlimerHitPos
    If SlimerHitPos <= 0.1 AND SlimerHitPos >= -0.1 Then Me.Enabled = False:DOF 139, DOFOff:Exit Sub
    If SlimerHitPos < 0 Then
        SlimerHitPos = ABS(SlimerHitPos)- 0.1
    Else
        SlimerHitPos = - SlimerHitPos + 0.1
    End If
End Sub

Sub SlimerLocationTimer_Timer
    SlimerLocation INT(RND * 3 + 1)
End Sub

Sub SlimePlayfield() 'lits 2 lights
    If RND < 0.5 Then
        PlaySound "vo_slimeus2"
    Else
        PlaySound "vo_slimeus"
    End If
    bPlayfieldSlimed = True
    SetLightColor l61, "green", 2
    SetLightColor l63, "green", 2
End Sub

'***************************
'StayPuft Animation / shake
'***************************

Dim StayPuftPos

Sub StayPuftShake()
    StayPuftPos = 3
    StayPuftShakeTimer.Enabled = True
    DOF 128, DOFOn
End Sub

Sub StayPuftShakeTimer_Timer()
    StayPuft.RotY = StayPuftPos
    If StayPuftPos <= 0.1 AND StayPuftPos >= -0.1 Then Me.Enabled = False:DOF 128, DOFOff:Exit Sub
    If StayPuftPos < 0 Then
        StayPuftPos = ABS(StayPuftPos)- 0.1
    Else
        StayPuftPos = - StayPuftPos + 0.1
    End If
End Sub

'********************
' Hologram animation
'********************

Dim HoloPos, HoloDir, HoloStep, HoloRandom, HoloAngle

Sub HologramStart()
    Hologram.visible = 1
    HoloPos = 0
    HoloDir = 1
    HoloStep = 0
    HoloRandom = 0
    HoloAngle = 0
    Hologram.Image = "ghost" &INT(RND * 17 + 1)
    HologramTimer.Enabled = 1
    HologramTimerExpired.Enabled = 1
End Sub

Sub HologramStop
    Hologram.visible = 0
    HologramTimer.Enabled = 0
    HologramTimerExpired.Enabled = 0
    If ectoHits > 2 Then
        ectoHits = 0
    End If
End Sub

Sub HologramTimer_Timer()
    Dim radianes
    HoloPos = HoloPos + HoloDir
    If HoloPos < -25 Then HoloDir = 1:HoloRandom = HoloRandom + 1
    If HoloPos > 25 Then HoloDir = -1
    If HoloPos = 0 AND HoloRandom = 5 Then
        HoloStep = INT(4 * RND)
        HoloRandom = 0
        Hologram.TransX = 0
        Hologram.TransY = 0
        Hologram.Rotz = 0
        Hologram.Roty = 0
    End If
    Select Case HoloStep
        Case 0 'zoom
            Hologram.Size_X = HoloGram.Size_X + HoloDir
            Hologram.Size_Y = HoloGram.Size_Y + HoloDir
        Case 1 'Rotate Y axis
            Hologram.Roty = Holopos / 2
        Case 2 'rotation circle
            radianes = HoloAngle * (3.1415 / 180)
            Hologram.TransX = 10 * Cos(radianes)
            Hologram.TransY = 10 * Sin(radianes)
            HoloAngle = (HoloAngle + 2)MOD 360
        Case 3 'Rotate Z axis
            Hologram.RotZ = HoloPos
    End Select
End Sub

Sub HologramTimerExpired_Timer()
    HologramStop
End Sub

'******************
' Catching Ghosts
'******************
' to catch ghosts:

Sub LitRandomGhost() '9 lights. lits a random ghost light
    aCatchGhostLights(INT(RND * 8)).State = 1
End Sub

Sub LitAllGhosts()
    dim lamp
    For each lamp in aCatchGhostLights
        lamp.State = 1
    Next
End Sub

Sub CatchGhost(n)
    FlashEffect 1
    PlaySound "gb_ghostcaught"
    Ghosts(CurrentPlayer) = Ghosts(CurrentPlayer) + n

    If bStorageMultiball Then 'during the storage multiball enable the 3xsuperjackpot after catching 9 ghosts
        SMBHits = SMBHits + 1
        If SMBHits = 9 Then
            LightEffect 2
            FlashEffect 2
            PlaySound "gb_blast3"
            SMBHits = 0
            bTripleSuperJackpot = True
            SetLightColor l62, "red", 2
            l55.State = 2
        End If
    End If

    If Ghosts(CurrentPlayer) >= 100 AND l7.State = 0 Then
        l7.State = 1
        PlaySound "vo_100ghostscaught"
        StartMassHysteriaMultiball ' Starts Mass Hysteria Multiball

    ElseIf Ghosts(CurrentPlayer) >= 80 AND l4.State = 0 Then
        l4.State = 1
        PlaySound "vo_80ghostscaught"
        StartPKEfrenzy ' Starts PKE Frenzy

    ElseIf Ghosts(CurrentPlayer) >= 60 AND l3.State = 0 Then
        l3.State = 1
        DMD CL(0,"STARTING"), CL(1, "LOOPIN SUPERS"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_60ghostscaught"
        StartLoopinSupers ' Starts Loopin Supers

    ElseIf Ghosts(CurrentPlayer) >= 50 AND l56.State = 0 Then
        StorageLights 1   'light storage facility

    ElseIf Ghosts(CurrentPlayer) >= 49 AND l53.State = 0 Then
        l53.State = 2
        DMD "", CL(1, "EXTRA BALL IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_extraballislit" ' Lights Extra Ball

    ElseIf Ghosts(CurrentPlayer) >= 40 AND l2.State = 0 Then
        l2.State = 1
        DMD "", CL(1, "NROEA IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_40ghostscaught" ' Lights Negative Reinforcement (at right saucer/scoop)
        l24.State = 2

    ElseIf Ghosts(CurrentPlayer) >= 30 AND l56.State = 0 Then
        StorageLights 1 'light storage facility

    ElseIf Ghosts(CurrentPlayer) >= 20 AND l1.State = 0 Then
        l1.State = 1
        l58.State = 2
        DMD "TOBINS SPIRIT GUIDE", CL(1, "IS LIT"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_20ghostscaught" ' Lights Tobin's Spirit Guide (at left scoop)
        vpmtimer.addtimer 2000, "PlaySound""vo_tobinspiritguideislit"" '"

    ElseIf Ghosts(CurrentPlayer) >= 10 AND l56.State = 0 Then
        StorageLights 1 'light storage facility

    ElseIf Ghosts(CurrentPlayer) > 10 Then
        PlayRandomGhostQuote
    End If

    ' check if more than 100 ghosts are caught and reset the lights to enable the modes again
    If Ghosts(CurrentPlayer) > 100 Then
        Ghosts(CurrentPlayer) = Ghosts(CurrentPlayer)MOD 100
        GhostsHundreds(CurrentPlayer) = GhostsHundreds(CurrentPlayer) + 1
    End If

    ' Update the status lights
    UpdateGhostLights
End Sub

Sub PlayRandomGhostQuote
    Select Case INT(RND * 8)
        Case 0:PlaySound "vo_ghostsareeverywhere"
        Case 1:PlaySound "vo_getthem"
        Case 2:PlaySound "vo_anotheronedown"
        Case 3:PlaySound "vo_anotherbitethedust"
    End Select
End Sub

Sub UpdateGhostLights()
    l1.State = 0
    l2.State = 0
    l3.State = 0
    l4.State = 0
    l7.State = 0
    If Ghosts(CurrentPlayer) >= 100 Then
        l7.State = 1
    End If
    If Ghosts(CurrentPlayer) >= 80 Then
        l4.State = 1
    End If
    If Ghosts(CurrentPlayer) >= 60 Then
        l3.State = 1
    End If
    If Ghosts(CurrentPlayer) >= 40 Then
        l2.State = 1
    End If
    If Ghosts(CurrentPlayer) >= 20 Then
        l1.State = 1
    End If
End Sub
'**************
' Loopin Supers
'**************
' super jackpots on the upper loop

Sub StartLoopinSupers()
    PlaySound "vo_superloopers"
    bLoopinSupers = True
    SetLightColor l63, "red", 2
    SetLightColor l61, "red", 2
    'open the gates
    LoopGateLeft.Open = True
    LoopGateRight.open = True
    'start timer
    LoopTimerExpired.Enabled = 1
End Sub

Sub LoopTimerExpired_Timer()
    LoopTimerExpired.Enabled = 0
    l61.State = 0
    l63.State = 0
    bLoopinSupers = False
    LoopGateLeft.Open = False
    LoopGateRight.open = False
End Sub

'*****************
'   PKE Frenzy
'*****************
' PKE Jackpot at the right ramp
' The Jackpot is PKELevel * PKEMult
' PKE Mult starter at 8x and it is reduced every 4 seconds
' PKE Mult increases each time you score a PKE Jackpot
' Max PKEMult is 12

Sub StartPKEfrenzy()
    PKEMult = 8
    l54.State = 1
    SetLightColor l64, "red", 2
    DMD CL(0, "STARTING"), CL(1, "PKE FRENZY"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_startedpkefrenzy"
    PKEMultTimer.Enabled = 1
    PKETimerExpired.Enabled = 1
End Sub

Sub PKETimerExpired_Timer()
    PKETimerExpired.Enabled = 0
    PKEMultTimer.Enabled = 0
    l54.State = 0
    l64.State = 0
End Sub

Sub PKEMultTimer_Timer()
    ReducePKEMult
End Sub

Sub ReducePKEMult()
    PKEMult = PKEMult -1
    If PKEMult < 1 Then
        PKEMult = 1
    End If
End Sub

Sub IncreasePKEMult()
    PKEMult = PKEMult + 1
    If PKEMult > 12 Then
        PKEMult = 12
    End If
End Sub

'***************************************
' Negative Reinforcement on ESP Ability
'***************************************
' Starter after capturing 80 ghosts
' In this mode the Jackpot is enabled in one the ramps and loops, but the light is off :)
' You have to guess where the Jackpot is and hit as many times as you can in 20 seconds :)
' HiddenJackpot value can be 1 (left loop), 2 (left ramp), 3 (right loop) and 4 (right ramp)

Sub StartNROEA()
    DMD " JACKPOT IS HIDDEN", "SHOOT LOOP & RAMPS", "", eNone, eBlink, eNone, 1000, True, "vo_nroespa"
    l24.State = 1
    HiddenJackpot = INT(RND * 4 + 1)
    NROEATimerExpired.Enabled = 1
End Sub

Sub NROEATimerExpired_Timer()
    NROEATimerExpired.Enabled = 0
    HiddenJackpot = 0
End Sub

'*************************
' Mass Hysteria Multiball
'*************************
' Starter after capturing 100 ghosts
' 6 Ball Multiball
' Collect Jackpots and after several Jackpot you get a SuperJackpot, and back again to collect Jackpots
' Hit the Left Captive ball several times to light Add-A-Ball, hit again to collect
' Gi lights go bananas during the mode

Sub StartMassHysteriaMultiball()
    DMD CL(0, "MASS HYSTERIA"), CL(1, "MULTIBALL"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_masshysteriamultiball"
    bMassHysteria = True
    bJackpot = True
    SetLightColor l62, "red", 2
    SetLightColor l64, "red", 2
    AddMultiball 5
End Sub

Sub EndMassHysteriaMultiball()
    bMassHysteria = False
    bJackpot = False
    l62.State = 0
    l64.State = 0
End Sub

'******************************************
'  Storage Facility Lock & Multiball (SFM)
'******************************************

Sub StorageLock_Hit()
    Dim waittime
    waittime = 100

    If l56.State = 2 Then
        BallsInLock = BallsInLock + 1
        Select Case BallsInLock
            Case 1
                FlashForms ContainerFlasher1, 800, 40, 1
                FlashForms ContainerFlasher2, 800, 40, 1
                DMD "", CL(1, "BALL 1 LOCKED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_ball1locked"
                waittime = 1000
            Case 2
                FlashForms ContainerFlasher3, 800, 40, 1
                FlashForms ContainerFlasher4, 800, 40, 1
                DMD "", CL(1, "BALL 2 LOCKED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_ball2locked"
                waittime = 1000
            Case 3
                FlashForms ContainerFlasher5, 800, 40, 1
                FlashForms ContainerFlasher6, 800, 40, 1
                DMD "", CL(1, "BALL 3 LOCKED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_ball3locked"
                StartSFM
                waittime = 2000
        End Select
    End If

    vpmtimer.addtimer waittime, "StorageLockExit '"
End Sub

Sub StartSFM() 'Multiball
    DMD "", CL(1, "SF MULTIBALL"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_storagemultiball"
    BallsInLock = 0
    l56.State = 0
    bStorageMultiball = True
    SMBHits = 0
    LitAllGhosts
    StartRainbow "gi"
    FlashForms ContainerFlasher, 2000, 40, 1:DOF 203, DOFPulse
    AddMultiball 3
End Sub

Sub StorageLockExit()
    StorageLock.Kick 0, 27, 1.56
    DOF 141, DOFPulse
End Sub

Sub StorageLights(stat)
    Dim lamp
    If stat then
        DMD "", CL(1, "STORAGE IS LIT"), "", eNone, eBlinkFast, eNone, 1500, True, "vo_storageislit"
        FlashEffect 1
        For each lamp in aStorageLights
            lamp.State = 2
        Next
        l56.state = 2
    Else
        For each lamp in aStorageLights
            lamp.State = 0
        Next
        l56.state = 0
    End If
End Sub

Sub EndSFM()
    Dim lamp
    bStorageMultiball = False
    ContainerFlasher1.Visible = 0
    ContainerFlasher2.Visible = 0
    ContainerFlasher3.Visible = 0
    ContainerFlasher4.Visible = 0
    ContainerFlasher5.Visible = 0
    ContainerFlasher6.Visible = 0
    For each lamp in aStorageLights
        lamp.State = 0
    Next
    StopRainbow
End Sub

'*********************
' Scoleri Brothers
'*********************
' 2 droptargets
' hit both under 10 seconds to start 3x super jackpot on the center ramp

Sub StartScoleriBrothers()
    Select Case INT(RND * 3)
        Case 0:PlaySound "vo_scoleribrothers"
        Case 1:PlaySound "vo_evenonebrotherisbad"
        Case 2:PlaySound "vo_thosebrothersareevil"
    End Select
    l26.State = 2
    l27.State = 2
    ScoleriResetTimer_Timer
End Sub

Sub EndScoleriBrothers()
    l26.State = 0
    l27.State = 0
    LeftScoleri = 0
    RightScoleri = 0
    PlaySoundAt SoundFXDOF("fx_droptarget", 136, DOFPulse, DOFContactors), LScoleriTarget
    ScoleriResetTimer.Enabled = 0
    LScoleriTarget.IsDropped = 1
    RScoleriTarget.IsDropped = 1
    If l47.State = 1 Then ' turn off the lights if started from the right captive ball award
        l47.State = 0
    End If
End Sub

Sub ScoleriResetTimer_Timer()
    ScoleriResetTimer.Enabled = 0
    l26.State = 2
    l27.State = 2
    PlaySoundAt SoundFXDOF("fx_resetdrop", 136, DOFPulse, DOFContactors), LScoleriTarget
    LeftScoleri = 0
    RightScoleri = 0
    LScoleriTarget.IsDropped = 0
    RScoleriTarget.IsDropped = 0
End Sub

Sub LScoleriTarget_Hit()
    PlaySoundAt SoundFXDOF("fx_droptarget", 137, DOFPulse, DOFContactors), LScoleriTarget
    PlaySound "gb_electricity1", 0, 1, -0.05, 0.05
    LightEffect 1
    FlashEffect 1
End Sub

Sub LScoleriTarget_Dropped()
    LeftScoleri = 1
    l26.State = 1
    If bGameInPlay Then
        If RightScoleri Then ' enable triple jackpot
            bTripleSuperJackpot = True
            SetLightColor l62, "red", 2
            l55.State = 2
            EndScoleriBrothers
        Else
            ScoleriResetTimer.Enabled = 1
        End If
    End If
End Sub

Sub RScoleriTarget_Hit()
    PlaySoundAt SoundFXDOF("fx_droptarget", 137, DOFPulse, DOFContactors), RScoleriTarget
    PlaySound "gb_electricity2", 0, 1, 0.05, 0.05
    LightEffect 1
    FlashEffect 1
End Sub

Sub RScoleriTarget_Dropped()
    RightScoleri = 1
    l27.State = 1
    If bGameInPlay Then
        If LeftScoleri Then ' enable triple jackpot
            PlaySound "gb_brothers"
            bTripleSuperJackpot = True
            SetLightColor l62, "red", 2
            l55.State = 2
            EndScoleriBrothers
        Else
            ScoleriResetTimer.Enabled = 1
        End If
    End If
End Sub

'****************
' Gozer Hurry Up
'****************
' Hit Gozer for 12M points
' but the prize is reduced by 1M each 2 seconds :)

Sub StartGozerHurryUp()
    GozerAward = 12000000
    l76.State = 2
    GozerHurryUpExpired.Enabled = 1
    GozerHurryUp.Enabled = 1
End Sub

Sub GozerHurryUp_Timer()
    FlashForms LeftRampDome2, 1000, 50, 0
    DOF 212, DOFPulse
    GozerAward = GozerAward - 1000000
    DMD "", "GOZER: " &FormatScore(GozerAward), "", eNone, eNone, eNone, 500, True, ""
End Sub

Sub GozerHurryUpExpired_Timer()
    GozerHurryUp.Enabled = 0
    GozerHurryUpExpired.Enabled = 0
    l76.State = 0
    If l47.State = 1 Then ' turn off the lights if started from the right captive ball award
        l47.State = 0
    End If
End Sub

'*********************
' Terror Dog Hurry Up
'*********************
' uses the light l52, if it blinks then it is on
' collect at the right ramp and at the terror dog target
' 20 seconds Hurry up
' the award starts at 4M and it is increased by hitting the terror dog and Gozer standup targets

Sub StartTerrorDog()
    Select Case INT(RND * 3)
        Case 0:PlaySound "vo_okwhobroughtthedog"
        Case 1:PlaySound "vo_someonecallthedogcatcher"
        Case 2:PlaySound "vo_wholetthedogout"
    End Select
    l52.State = 2
    bTerrorDogActive = True
    TerrorDogAward = 4000000
    TerrorDogExpired.Enabled = 1
End Sub

Sub TerrorDogExpired_Timer()
    l52.State = 0
    bTerrorDogActive = False
    TerrorDogExpired.Enabled = 0
    If l47.State = 1 Then ' turn off the lights if started from the right captive ball award
        l47.State = 0
    End If
End Sub

'***************************
' Symmetrical Book Stacking
'***************************
' shoot the right captive ball to lit an award for 10 seconds:
' the awards: Scoleri Brothers, Terror Dog Hurry Up (twice) and Gozer The Gozerian Hurry Up.

Sub AdvanceSymmetricalBook()
    l47.State = 1
    SymmetricalBookStep = SymmetricalBookStep + 1
    If SymmetricalBookStep = 5 Then
        SymmetricalBookStep = 1
    End If
    Select Case SymmetricalBookStep
        Case 1:DMD "", CL(1, "SCOLERI BROTHERS"), "", eNone, eBlinkFast, eNone, 1500, True, "":StartScoleriBrothers
        Case 2, 3:DMD "", CL(1, "TERROR DOG"), "", eNone, eBlinkFast, eNone, 1500, True, "gb_fanfare6":StartTerrorDog
        Case 4:DMD "", CL(1, "GOZER HURRY UP"), "", eNone, eBlinkFast, eNone, 1500, True, "vo_aimforgozer":StartGozerHurryUp
    End Select
End Sub

'************************
' Add Time to a Hurry Up
'************************
' this "Add Time" does reset the timer if it is active, but it won't reset the score values

Sub AddTime()
    If TerrorDogExpired.Enabled Then
        TerrorDogExpired.Enabled = 0
        TerrorDogExpired.Enabled = 1
        PlaySound "vo_timeadded"
    End If
    If GozerHurryUpExpired.Enabled Then
        GozerHurryUpExpired.Enabled = 0
        GozerHurryUpExpired.Enabled = 1
        PlaySound "vo_timeadded"
    End If
    If NROEATimerExpired.Enabled Then
        NROEATimerExpired.Enabled = 0
        NROEATimerExpired.Enabled = 1
        PlaySound "vo_timeadded"
    End If
    If LoopTimerExpired.Enabled Then
        LoopTimerExpired.Enabled = 0
        LoopTimerExpired.Enabled = 1
        PlaySound "vo_timeadded"
    End If
    If PKETimerExpired.Enabled Then
        PKETimerExpired.Enabled = 0
        PKETimerExpired.Enabled = 1
        PlaySound "vo_timeadded"
    End If
End Sub

'************************
'  Modes & Mode Start
'************************

Dim Modes(12)

' Modes(0) will have the active mode number
' when a mode is not active Modes(n) = 0
' when a mode is active Modes(n) = 2
' when a mode is completed Modes(n) = 1
' this is to be consistent with the light states

' Only one mode can be active at a time.
' If you loose the ball you'll need to restart the mode :)
' You cannot start a mode if multiballs are on.
' To start a mode: defeat Slimer, complete another mode, or get it as a skillshot or a Tobin award

'There are 3 ladders:
' leftscoop (Library), left ramp and right loop

' ladder 1: Library's saucer
' 1 Spooked Librarian
' 2 back off man

' ladder 2:left ramp
' 3 we got one
' 4 he slimed me
' 5 the ballroom

' ladder 3: right loop
' 6 ok who brought the dog
' 7 spook central
' 8 gozer the gozerian
' 9 stay puft mashmallow man

' mini wizard modes
' 10 we came we saw we kicked its...  This modes starts after completing a ladder
' 11 we are ready to believe you	  This mode starts after completing the 3 ladders

' 12 are you a God? 				  This mode starts after everything is completed.

Sub LitNextModes()                             'turns on the lamps of the modes that can be started
    If(Modes(0) > 0)Then Exit Sub               'exit as there is an active mode
    If Modes(2) + Modes(5) + Modes(9) = 3 Then ' all the ladders er finished
        StartWeAreReadyToBelieveYou
        Exit Sub
    End If
    ' ladder 1
    If Modes(1) = 0 Then
        l42.State = 2
    ElseIf Modes(2) = 0 Then
        l43.State = 2
    End If

    'ladder 2
    If Modes(3) = 0 Then
        l44.State = 2
    ElseIf Modes(4) = 0 Then
        l45.State = 2
    ElseIf Modes(5) = 0 Then
        l46.State = 2
    End If

    'ladder 3
    If Modes(6) = 0 Then
        l48.State = 2
    ElseIf Modes(7) = 0 Then
        l49.State = 2
    ElseIf Modes(8) = 0 Then
        l50.State = 2
    ElseIf Modes(9) = 0 Then
        l51.State = 2
    End If
End Sub

Sub StartMode(n)
    Dim i
    Modes(0) = n
    Modes(n) = 2
    'Update lights
    For i = 1 to 9
        If i <> n then
            If Modes(i) = 2 Then 'turn off the blinking lights of the modes that are not started
                Modes(i) = 0
            End If
        End If
    Next
    UpdateModeLights
    Select Case n
        Case 1:StartSpookedLibrarian
        Case 2:StartBackOffMan
        Case 3:StartWeGotOne
        Case 4:StartHeSlimedMe
        Case 5:StartTheBallroom
        Case 6:StartWhoBroughttheDog
        Case 7:StartSpookCentral
        Case 8:StartGozerian
        Case 9:StartStayPuft
    End Select
End Sub

Sub StopMode(n) 'called at the end of a ball
    Select Case n
        Case 1:StopSpookedLibrarian
        Case 2:StopBackOffMan
        Case 3:StopWeGotOne
        Case 4:StopHeSlimedMe
        Case 5:StopTheBallroom
        Case 6:StopWhoBroughttheDog
        Case 7:StopSpookCentral
        Case 8:StopGozerian
        Case 9:StopStayPuft
    End Select
End Sub

Sub UpdateModeLights() 'update the lights according to the mode state
    l42.State = Modes(1)
    l43.State = Modes(2)
    l44.State = Modes(3)
    l45.State = Modes(4)
    l46.State = Modes(5)
    l48.State = Modes(6)
    l49.State = Modes(7)
    l50.State = Modes(8)
    l51.State = Modes(9)
    l8.State = Modes(10)
    l9.State = Modes(11)
    l77.State = Modes(12)
End Sub

'***************************
' Mode 1: Spooked Librarian
'***************************
' targets: left ramp, right orbit, right ramp, right saucer
' hit any of them 3 times
' arrows are blue
' after one shot the spinner light is lit, and shots to the left orbit will
' then increase the super jackpot base value based upon the number of spins that occur.

Sub StartSpookedLibrarian()
    DMD " SPOOKED LIBRARIAN", CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1500, True, "vo_spookedlibrarian"
    ' reset the count each time you start the mode
    SpookedLibrarianHits = 0
    'turn on The mode lights, in blue color
    SetLightColor l62, "blue", 2
    SetLightColor l63, "blue", 2
    SetLightColor l64, "blue", 2
    SetLightColor l67, "blue", 2
End Sub

Sub StopSpookedLibrarian() 'called at the end of a ball
    Modes(0) = 0
    Modes(1) = 0
    UpdateModeLights
    l66.State = 0
    l62.State = 0
    l63.State = 0
    l64.State = 0
    l67.State = 0
End Sub

Sub CheckSpookedHits()
    If SpookedLibrarianHits > 2 Then '3 or more hits completes the mode
        DMD " SPOOKED LIBRARIAN", CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 1500, True, ""
        Congratulation:FlashEffect 1
        Modes(0) = 0
        Modes(1) = 1 'completed
        UpdateModeLights
        'stop the mode lights
        l66.State = 0 'spinner
        l62.State = 0
        l63.State = 0
        l64.State = 0
        l67.State = 0
        'restore jackpot lights in case jackpots or super jackpots are active
        RestoreArrowLights
        'lit the modes to get ready to start the next one
        LitNextModes
    End If
End Sub

Sub RestoreArrowLights 'called after a mode is finished to restore mostly jackpot lights
    If bSlimerSuperJackpot Then SetLightColor l62, "red", 2:l55.State = 2
    If bSuperJackpot Then SetLightColor l62, "red", 2:l55.State = 2
    If bDoubleSuperJackpot Then SetLightColor l62, "red", 2:l55.State = 2
    If bTripleSuperJackpot Then SetLightColor l62, "red", 2:l55.State = 2
    If bJackpot Then SetLightColor l62, "red", 2:SetLightColor l64, "red", 2
    If bPlayfieldSlimed Then SetLightColor l61, "green", 2:SetLightColor l63, "green", 2
End Sub

'***************************
' Mode 2: Back Off Man!
'***************************
'hit any of the lit shots 5 times, end with the left scoop

Sub StartBackOffMan()
    DMD CL(0, "BACK OFF MAN"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1500, True, "vo_backoffman"
    ' reset the count each time you start the mode
    BackOffManHits = 0
    CheckBackOffManHits
End Sub

Sub StopBackOffMan() 'end of ball, not complete the mode
    Modes(0) = 0
    Modes(2) = 0
    UpdateModeLights
    BackOffManHits = 0
    l60.state = 0
    l61.state = 0
    l62.State = 0
    l63.State = 0
    l64.State = 0
    l65.State = 0
    l67.State = 0
End Sub

Sub CheckBackOffManHits() 'check nr of hits + update arrow lights
    Select Case BackOffManHits
        Case 0:SetLightcolor l64, "blue", 2:SetLightcolor l67, "blue", 2
        Case 1:l67.State = 0:SetLightcolor l64, "blue", 2:SetLightcolor l63, "blue", 2
        Case 2:l64.State = 0:SetLightcolor l63, "blue", 2:SetLightcolor l65, "blue", 2
        Case 3:l63.State = 0:SetLightcolor l62, "blue", 2:SetLightcolor l65, "blue", 2
        Case 4:l65.State = 0:SetLightcolor l61, "blue", 2:SetLightcolor l62, "blue", 2
        Case 5:l61.State = 0:l62.State = 0:SetLightcolor l60, "blue", 2 'leftscoop
        Case 6:                                                         'complete the mode
            DMD CL(0, "BACK OFF MAN"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 1500, True, ""
            Congratulation:FlashEffect 1
            l60.State = 0
            Modes(0) = 0
            Modes(2) = 1 'completed
            UpdateModeLights
            'Start We Came We Saw We kick... multiball with 2 balls
            StartWeCameWeSaw 2
            vpmtimer.addtimer 1000, "FlashLeftScoop '"
            vpmtimer.addtimer 2000, "LeftScoopExit '"
    End Select
End Sub

'***************************
' Mode 3: We Got One!
'***************************
' alternate shots between left loop/left ramp and right loop/right ramp.
' you need to do this twice (2 shots to the left and two shots to the right

Sub StartWeGotOne()
    DMD CL(0, "WE GOT ONE!"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, ""
    If RND < 0.5 Then
        PlaySound "vo_wegotone"
    Else
        PlaySound "vo_wegotone2"
    End If
    ' reset the count each time you start the mode
    WeGotOneHits = 0
    CheckWeGotOneHits
End Sub

Sub StopWeGotOne()
    Modes(0) = 0
    Modes(3) = 0
    UpdateModeLights
    l61.State = 0:l62.State = 0
    l63.State = 0:l64.State = 0
End Sub

Sub CheckWeGotOneHits() 'check nr of hits + update arrow lights
    Select Case WeGotOneHits
        Case 0:l63.State = 0:l64.State = 0:SetLightcolor l61, "blue", 2:SetLightcolor l62, "blue", 2
        Case 1:l61.State = 0:l62.State = 0:SetLightcolor l63, "blue", 2:SetLightcolor l64, "blue", 2
        Case 2:l63.State = 0:l64.State = 0:SetLightcolor l61, "blue", 2:SetLightcolor l62, "blue", 2
        Case 3:l61.State = 0:l62.State = 0:SetLightcolor l63, "blue", 2:SetLightcolor l64, "blue", 2
        Case 4: 'complete the mode
            DMD CL(0, "WE GOT ONE!"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 1500, True, ""
            Congratulation:FlashEffect 1
            l61.State = 0:l62.State = 0
            l63.State = 0:l64.State = 0
            Modes(0) = 0
            Modes(3) = 1 'completed
            UpdateModeLights
            'restore jackpot lights in case jackpots or super jackpots are active
            RestoreArrowLights
            'lit the modes to get ready to start the next one
            LitNextModes
    End Select
End Sub

'***************************
' Mode 4: He Slimed Me!
'***************************
' Shoot the left ramp to build up the mode jackpot. After 3 shots Slimer will appear.
' Hitting Slimer will then collect the jackpot and end the mode.
' note: You can continue to build the jackpot up via the left ramp throughout the mode, even if Slimer is up.

Sub StartHeSlimedMe()
    DMD CL(0, "HE SLIMED ME!"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_heslimedme"
    ' reset the count each time you start the mode
    HeSlimedMeHits = 0
    SetLightcolor l62, "green", 2
    CheckHeSlimedMeHits
End Sub

Sub StopHeSlimedMe()
    Modes(0) = 0
    Modes(4) = 0
    UpdateModeLights
    l62.State = 0
End Sub

Sub CheckHeSlimedMeHits()
    If HeSlimedMeHits = 3 Then
        SlimerMoveUp
        DMD CL(0, "FIRE AT SLIMER"), CL(1, "IS " & FormatScore(HeSlimedMeValue)), "", eNone, eBlinkFast, eNone, 800, True, "vo_fireatslimer"
    End If
End Sub

Sub CompleteHeSlimedMe()
    SlimerMoveDown

    DMD CL(0, "HE SLIMED ME!"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 800, False, ""
    Congratulation:FlashEffect 1
    AddScore HeSlimedMeValue
    DMD "_", CL(1, FormatScore(HeSlimedMeValue)), "", eNone, eBlinkFast, eNone, 800, True, ""
    l62.State = 0
    Modes(0) = 0
    Modes(4) = 1 'completed
    UpdateModeLights
    'restore jackpot lights in case jackpots or super jackpots are active
    RestoreArrowLights
    'lit the modes to get ready to start the next one
    LitNextModes
End Sub

'***************************
' Mode 5: The Ballroom
'***************************
' Hit 6 random shots around the playfield, end with the left ramp

Sub StartTheBallroom()
    DMD CL(0, "THE BALLROOM"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_theballroom"
    ' reset the count each time you start the mode
    TheBallroomHits = 0
    CheckTheBallroomHits 'turn on the first light
End Sub

Sub StopTheBallroom()
    Modes(0) = 0
    Modes(5) = 0
    TheBallroomLight 8 '8 is not used so it simply turns off all the lights
End Sub

Sub CheckTheBallroomHits()
    Dim tmp
    tmp = INT(RND * 7)
    Select Case TheBallroomHits
        Case 0, 1, 2, 3, 4, 5:TheBallroomLight tmp
        Case 6:TheBallroomLight 7
        Case 7 'complete the mode
            DMD CL(0, "THE BALLROOM"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 800, True, ""
            Congratulation:FlashEffect 1
            Modes(0) = 0
            Modes(5) = 1
            TheBallroomLight 8
            UpdateModeLights
            'Start We Came We Saw We kick... multiball with 3 balls
            StartWeCameWeSaw 3
    End Select
End Sub

Sub TheBallroomLight(n)
    l59.State = 0:l60.State = 0
    l61.State = 0:l62.State = 0
    l63.State = 0:l64.State = 0
    l65.State = 0:l67.State = 0
    Select Case n
        Case 0:SetLightColor l59, "purple", 2
        Case 1:SetLightColor l60, "purple", 2
        Case 2:SetLightColor l61, "purple", 2
        Case 3:SetLightColor l67, "purple", 2
        Case 4:SetLightColor l63, "purple", 2
        Case 5:SetLightColor l64, "purple", 2
        Case 6:SetLightColor l65, "purple", 2
        Case 7:SetLightColor l62, "purple", 2
    End Select
End Sub

'************************************
' Mode 6: Okay, Who Brought the Dog?
'************************************
' Shoot each orbit and left ramp to run away from the Terror Dog and complete the mode.

Sub StartWhoBroughttheDog()
    DMD "WHO BROUGHT THE DOG", CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_okwhobroughtthedog"
    ' reset the count each time you start the mode
    WhoBroughttheDogHits = 0
    SetLightColor l61, "yellow", 2
    SetLightColor l62, "yellow", 2
    SetLightColor l63, "yellow", 2
End Sub

Sub StopWhoBroughttheDog()
    Modes(0) = 0
    Modes(6) = 0
    l61.State = 0
    l62.State = 0
    l63.State = 0
End Sub

Sub CheckWhoBroughttheDogHits()
    If WhoBroughttheDogHits = 3 Then
        DMD "WHO BROUGHT THE DOG", CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 800, False, ""
        Congratulation:FlashEffect 1
        AddScore 6000000
        DMD "_", CL(1, FormatScore(6000000)), "", eNone, eBlinkFast, eNone, 800, True, ""
        Modes(0) = 0
        Modes(6) = 1
        UpdateModeLights
        'restore jackpot lights in case jackpots or super jackpots are active
        RestoreArrowLights
        'lit the modes to get ready to start the next one
        LitNextModes
    End If
End Sub

'***************************
' Mode 7: Spook Central
'***************************
'Hit the left ramp 4 times, then 4 shots appear:
'left orbit, left ramp, right orbit, and right ramp.
' Collecting all 4 shots ends the mode.

Sub StartSpookCentral()
    DMD CL(0, "SPOOK CENTRAL"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_spookedcentral"
    ' reset the count each time you start the mode
    SpookCentralHits = 0
    SetLightColor l62, "amber", 2
End Sub

Sub StopSpookCentral()
    Modes(0) = 0
    Modes(7) = 0
    l61.State = 0
    l62.State = 0
    l63.State = 0
    l64.State = 0
End Sub

Sub CheckSpookCentralHits()
    Select Case SpookCentralHits
        Case 1:PlaySound "3moreshots"
        Case 2:PlaySound "2moreshots"
        Case 3:PlaySound "1moreshot"
        Case 4:
            SetLightColor l61, "amber", 2
            SetLightColor l62, "amber", 2
            SetLightColor l63, "amber", 2
            SetLightColor l64, "amber", 2
        Case 8
            DMD CL(0, "SPOOK CENTRAL"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 800, False, ""
            Congratulation:FlashEffect 1
            AddScore 6000000
            DMD "_", CL(1, FormatScore(6000000)), "", eNone, eBlinkFast, eNone, 800, True, ""
            Modes(0) = 0
            Modes(7) = 1
            UpdateModeLights
            'restore jackpot lights in case jackpots or super jackpots are active
            RestoreArrowLights
            'lit the modes to get ready to start the next one
            LitNextModes
    End Select
End Sub

'***************************
' Mode 8: Gozer the Gozerian
'***************************
'The right and left orbit build the Gozer value.
'Shots to the left or right ramp enable it to be collected.
'then hit the Gozer target to collect.

Sub StartGozerian()
    DMD CL(0, "GOZER THE GOZERIAN"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_gozerthegozerian"
    ' reset the count each time you start the mode
    GozerianHits = 0
    SetLightColor l61, "yellow", 2
    SetLightColor l62, "amber", 2
    SetLightColor l63, "yellow", 2
    SetLightColor l64, "amber", 2
End Sub

Sub StopGozerian()
    Modes(0) = 0
    Modes(8) = 0
    l61.State = 0
    l62.State = 0
    l63.State = 0
    l64.State = 0
End Sub

Sub CheckGozerianHits()
    Select Case GozerianHits
        Case 1:PlaySound "1moreshot"
        Case 2
            DMD CL(0, "GOZER THE GOZERIAN"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 1500, True, ""
            Congratulation:FlashEffect 1
            Modes(0) = 0
            Modes(8) = 1
            UpdateModeLights
            l61.State = 0
            l62.State = 0
            l63.State = 0
            l64.State = 0
            l65.State = 0
            'restore jackpot lights in case jackpots or super jackpots are active
            RestoreArrowLights
            'lit the modes to get ready to start the next one
            LitNextModes
    End Select
End Sub

'**********************************
' Mode 9: Stay Puft Marshmallow Man
'**********************************
'Hit the white arrows, and then hit the right orbit
'and a multiball starts based upon how many of the shots you hit before hitting the right orbit.
'the orbits will lit and hit the orbits 5 times to to destroy Stay Puft
StayPuftHits = 0
StayPuftMultiballHits = 0

Sub StartStayPuft()
    DMD CL(0, "MARSHMALLOW MAN"), CL(1, "STARTED"), "", eNone, eBlinkFast, eNone, 1000, True, "vo_staypuft"
    ' reset the count each time you start the mode
    StayPuftHits = 0
    SetLightColor l61, "white", 2
    SetLightColor l62, "white", 2
    SetLightColor l64, "white", 2
    SetLightColor l65, "white", 2
End Sub

Sub StopStayPuft()
    Modes(0) = 0
    Modes(9) = 0
    l61.State = 0
    l62.State = 0
    l63.State = 0
    l64.State = 0
    l65.State = 0
End Sub

Sub CheckStayPuftHits()
    FlashForMs StayPuftFlasher, 1500, 40, 0
    Select Case StayPuftMultiballHits
        Case 1:DMD "_", CL(1, "5 MORE SHOTS"), "", eNone, eBlinkFast, eNone, 800, True, "5moreshots"
        Case 3:DMD "_", CL(1, "4 MORE SHOTS"), "", eNone, eBlinkFast, eNone, 800, True, "4moreshots"
        Case 4:DMD "_", CL(1, "3 MORE SHOTS"), "", eNone, eBlinkFast, eNone, 800, True, "3moreshots"
        Case 5:DMD "_", CL(1, "2 MORE SHOTS"), "", eNone, eBlinkFast, eNone, 800, True, "2moreshots"
        Case 6:DMD "_", CL(1, "1 MORE SHOTS"), "", eNone, eBlinkFast, eNone, 800, True, "1moreshot"
        Case 7
            DMD CL(0, "MARSHMALLOW MAN"), CL(1, "COMPLETED"), "", eNone, eBlinkFast, eNone, 1500, True, ""
            Congratulation:FlashEffect 1
            Modes(0) = 0
            Modes(9) = 1
            UpdateModeLights
            l61.State = 0
            l62.State = 0
            l63.State = 0
            l64.State = 0
            l65.State = 0
            'Start We Came We Saw We kick... multiball with 4 balls
            StartWeCameWeSaw 4
    End Select
End Sub

'************************************************************
' Mini Wizard Mode 1 (10): We Came, We Saw, We Kicked Its ...
'************************************************************
' This is a 2, 3 or 4 ball multiball where all shots are super jackpots and they all stay lit for the
' entire mode.

Sub StartWeCameWeSaw(n) 'n is the number of multiballs depending on the ladder that has been completed
    If RND < 0.5 Then
        PlaySound "vo_wecamewesaw"
    Else
        PlaySound "vo_wecamewesaw2"
    End If
    AddMultiball n
    vpmtimer.addtimer 1500, "PlaySound ""vo_multiball"" '"
    Modes(0) = 10
    l8.State = 2
    SetLightColor l62, "purple", 2
    SetLightColor l64, "purple", 2
End Sub

Sub StopWeCameWeSaw() 'called at the end of multiball
    Modes(0) = 0
    l8.State = 0
    l62.State = 0
    l64.State = 0
    'restore jackpot lights in case jackpots or super jackpots are active
    RestoreArrowLights
    'lit the modes to get ready to start the next one
    LitNextModes
End Sub

'****************************************************
' Mini Wizard Mode 2 (11): We’re Ready to Believe You
'****************************************************
' 3 ball multiball mode.
' hit 3 times the left captive ball to lit add a ball, hit the saucer to collect.
' jackpots a enabled on the left an right ramps and the loops, change randomly

Sub StartWeAreReadyToBelieveYou
    Modes(0) = 11
    WeAreReadyTimer_Timer 'turn on one of the arrow lights
    WeAreReadyTimer.Enabled = 1
    AddMultiball 3
    PlaySound "vo_multiball"
End Sub

Sub StopWeAreReadyToBelieveYou 'called at the end of multiball
    Modes(0) = 0
    WeAreReadyTimer.Enabled = 1
    l61.State = 0:l62.State = 0:l63.State = 0:l64.State = 0
    'restore jackpot lights in case jackpots or super jackpots are active
    RestoreArrowLights
End Sub

Sub WeAreReadyTimer_Timer 'changes the arrow lights (jackpots) randomly
    Dim tmp
    tmp = INT(RND * 4)
    Select Case tmp
        Case 0:SetLightColor l61, "blue", 2:l62.State = 0:l63.State = 0:l64.State = 0
        Case 1:SetLightColor l62, "blue", 2:l61.State = 0:l63.State = 0:l64.State = 0
        Case 2:SetLightColor l63, "blue", 2:l62.State = 0:l61.State = 0:l64.State = 0
        Case 3:SetLightColor l64, "blue", 2:l62.State = 0:l63.State = 0:l61.State = 0
    End Select
End Sub

'********************************************
' Mini Wizard Mode 3 (12): Are you a God?
'********************************************
' 3 ball multiball starts after all the gear is collected
' all the arrows blink with rainbow colors
' each hit on the targets will give double jackpots

Sub StartAreYouaGod()
    If Modes(0) = 0 Then ' if no other mode is running then start it
        Modes(0) = 12
        l68.State = 2
        l69.State = 2
        l70.State = 2
        l71.State = 2
        l72.State = 2
        StartRainbow "arrows"
        LightSeqGear.Play SeqRandom, 5, , 4000
        AddMultiball 3
        PlaySound "vo_multiball"
    End If
End Sub

Sub LightSeqGear_PlayDone()
    LightSeqGear.Play SeqRandom, 5, , 4000
End Sub

Sub StopAreYouaGod() ' called after the multiball, reset all the gear lights and variables so they can be turned on again
    Dim ii
    Modes(0) = 0
    'reset gear variables
    Gear(1) = 0
    Gear(2) = 0
    Gear(3) = 0
    Gear(4) = 0
    Gear(5) = 0
    'reset gear lights
    l68.State = 0
    l69.State = 0
    l70.State = 0
    l71.State = 0
    l72.State = 0
    'reset other lights
    'proton pack lights
    l73.State = 0
    l74.State = 0
    l75.State = 0
    'pke lights
    l28.State = 0
    l29.State = 0
    l30.State = 0
    l28b.State = 0
    l29b.State = 0
    l30b.State = 0
    HologramStop
    LightSeqGear.StopPlay
    For each ii in aLEDlights
        SetLightColor ii, "white", 0
    Next
    StopRainbow
End Sub