Dim PAzar
Dim RimoV
Dim LuzRimoV
Dim RimoR
Dim LuzRimoR
Dim Pritz
Dim Baila
Dim TelM
Dim IRA
Dim Fdespertar
Dim BonusLV
Dim MultiPts
Dim Sentar
Dim BonusTi
Dim BonusACU
Dim Flashin
Dim music
Dim Vid
Dim Rendor
Dim bolas
Dim BolasM


'*******************************************************************
'                           DMD
'-------------------------------------------------------------------
Dim UltraDMD

Const UltraDMD_VideoMode_Stretch = 0
Const UltraDMD_VideoMode_Top = 1
Const UltraDMD_VideoMode_Middle = 2
Const UltraDMD_VideoMode_Bottom = 3

Sub LoadUltraDMD
	Dim FlexDMD
    Set FlexDMD = CreateObject("FlexDMD.FlexDMD")
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
        MsgBox "Incompatible Version of UltraDMD found.  Please update to version 1.1 or newer."
        Exit Sub
    End If
	UltraDMD.SetVideoStretchMode UltraDMD_VideoMode_Middle
    UltraDMD.SetProjectFolder curDir & "\YFDmD"
	Dim imgList
End Sub

Sub DMD_DisplayScene()
    If Not UltraDMD is Nothing Then
        UltraDMD.DisplayScene00 casti, "Young",14 ,5 ,"Frankenstein" ,15,6, 1, 1000, 1
        If pauseTime > 0 OR animateIn < 14 OR animateOut < 14 Then
           Timer1DmD.Enabled = True
        End If
    End If
End Sub


Sub DMD_DisplaySceneEx(bkgnd,toptext,topBrightness, topOutlineBrightness, bottomtext, bottomBrightness, bottomOutlineBrightness, animateIn,pauseTime,animateOut)
    If Not UltraDMD is Nothing Then
        Debug.Print bkgnd
        Debug.Print toptext
        Debug.Print topBrightness
        Debug.Print topOutlineBrightness
        Debug.Print bottomtext
        Debug.Print bottomBrightness
        Debug.Print bottomOutlineBrightness
        Debug.Print animateIn
        Debug.Print pauseTime
        Debug.Print animateOut
        UltraDMD.DisplayScene00Ex bkgnd, toptext, topBrightness, topOutlineBrightness, bottomtext, bottomBrightness, bottomOutlineBrightness, animateIn, pauseTime, animateOut
        If pauseTime > 0 OR animateIn < 14 OR animateOut < 14 Then
            Timer1DmD.Enabled = True
        End If
    End If
End Sub

Dim p1
Dim p2
Dim p3
Dim p4
dim stretchmode
Dim spincount
Dim credits
Dim ball
Dim iScene
Dim numBumbers
Dim tickCount

Sub Table1_Init()
    p1 = -50000
	p2 = 0
    p3 = 0
    p4 = 0
    stretchmode = 1
    spincount = 0
    credits = 0
    ball = 0
    iScene = 18
    numBumbers = 0
	MultiPts = 1
	BonusACU = 100
	LTestigo.state=0
	CheckComienzo()


	LoadUltraDMD
    UltraDMD.SetVideoStretchMode UltraDMD_VideoMode_Middle
	OnScoreboardChanged()
End Sub

Sub Table1_Exit()
  If Not UltraDMD is Nothing Then
    If UltraDMD.IsRendering Then
      UltraDMD.CancelRendering
    End If
	UltraDMD.Uninit
    UltraDMD = NULL
  End If
End Sub

dim curDir
dim WinScriptHost

'-------------------------------------------------------------------

Sub Timer1sDmD_Timer() 
    If Not UltraDMD.IsRendering Then
        'When the scene finishes rendering, then immediately display the scoreboard
        OnScoreboardChanged()
    End If
End Sub
'-------------------------------------------------------------------
Sub OnScoreboardChanged()
        UltraDMD.DisplayScoreboard 3, 1, p1, p2, p3, p4, "credits " & credits, "ball " & ball
End Sub

Sub Timer1DmD_Timer()
    If Not UltraDMD.IsRendering Then
        'When the scene finishes rendering, then immediately display the scoreboard
        Timer1DmD.Enabled = False
        OnScoreboardChanged()
    End If
	
End Sub

Sub TimerTest_Timer()
    If UltraDMD.IsRendering Then
        tickCount = tickCount + 100
'       void ModifyScene00(string id, string toptext, string bottomtext);
    	UltraDMD.ModifyScene00 "timerTest", "10s timing test", CStr(tickCount)
    End If
End Sub


'*******************************************************************
'***************** Keys ********************************************
Sub Table1_KeyDown(ByVal keycode)

	If keycode = PlungerKey Then
		Plunger.PullBack
		PlaySound "plungerpull",0,1,0.25,0.25
	End If

	If keycode = LeftFlipperKey Then
		LeftFlipper.RotateToEnd
		LeftFlipper1.RotateToEnd
		LeftFlipper2.RotateToEnd
		PlaySound "fx_flipperup", 0, .67, -0.05, 0.05
	End If

	If keycode = LeftFlipperKey and LMDA.state=2 Then
		LeftFlipper.RotateToEnd
		LeftFlipper1.RotateToEnd
		LeftFlipper2.RotateToEnd
		PlaySound "fx_flipperup", 0, .67, -0.05, 0.05
		PAzar = CINT(rnd*24)
		CheckDardo()
	End If

    
	If keycode = RightFlipperKey Then
		RightFlipper.RotateToEnd
		RightFlipper1.RotateToEnd
		RightFlipper2.RotateToEnd
		RightFlipper3.RotateToEnd
		PlaySound "fx_flipperup", 0, .67, 0.05, 0.05
	End If
    
	If keycode = LeftTiltKey Then
		Nudge 90, 2
	End If
    
	If keycode = RightTiltKey Then
		Nudge 270, 2
	End If
    
	If keycode = CenterTiltKey Then
		Nudge 0, 2
	End If

'------------ Botones para Activar los Switch de tapa --------------------------------
	'---Letra G  ****  Switch 01
	If keycode = 34 and LAlarm.State=0 Then
	'PlaySound
	End If

	If keycode = 34 and LAlarm.State=1 Then
	'PlaySound
	End If

	If keycode = 34 and LAlarm.State=2 and Volt>49 and Volt<70 Then
	TimerASuich.Enabled=True
	medidor.RotY=+10
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 34 and LAlarm.State=2 and Volt>69 and Volt<80 Then
	TimerASuich.Enabled=True
	medidor.RotY=+20
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 34 and LAlarm.State=2 and Volt>79 and Volt<90 Then
	TimerASuich.Enabled=True
	medidor.RotY=+30
	p1 = p1 + (30000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 34 and LAlarm.State=2 and Volt>89 and Volt<101 Then
	TimerASuich.Enabled=True
	medidor.RotY=+40
	p1 = p1 + (40000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If keycode = 34 and LAlarm.State=2 and Volt>100 Then
	TimerASuich.Enabled=True
	medidor.RotY=+50
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'---Letra H  ****  Switch 02
	If keycode = 35 and LAlarm1.State=0 Then
	'PlaySound
	End If

	If keycode = 35 and LAlarm1.State=1 Then
	'PlaySound
	End If

	If keycode = 35 and LAlarm.State=2 and Volt>49 and Volt<70 Then
	TimerASuich.Enabled=True
	medidor.RotY=+10
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 35 and LAlarm1.State=2 and Volt>69 and Volt<80 Then
	TimerASuich.Enabled=True
	medidor.RotY=+20
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 35 and LAlarm1.State=2 and Volt>79 and Volt<90 Then
	TimerASuich.Enabled=True
	medidor.RotY=+30
	p1 = p1 + (30000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 35 and LAlarm1.State=2 and Volt>89 and Volt<101 Then
	TimerASuich.Enabled=True
	medidor.RotY=+40
	p1 = p1 + (40000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If keycode = 35 and LAlarm1.State=2 and Volt>100 Then
	TimerASuich.Enabled=True
	medidor.RotY=+50
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'---Letra J  ****  Switch 03
	If keycode = 36 and LAlarm2.State=0 Then
	'PlaySound
	End If

	If keycode = 36 and LAlarm2.State=1 Then
	'PlaySound
	End If

	If keycode = 36 and LAlarm2.State=2 and Volt>49 and Volt<70 Then
	TimerASuich.Enabled=True
	medidor.RotY=+10
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 36 and LAlarm2.State=2 and Volt>69 and Volt<80 Then
	TimerASuich.Enabled=True
	medidor.RotY=+20
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 36 and LAlarm2.State=2 and Volt>79 and Volt<90 Then
	TimerASuich.Enabled=True
	medidor.RotY=+30
	p1 = p1 + (30000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 36 and LAlarm2.State=2 and Volt>89 and Volt<101 Then
	TimerASuich.Enabled=True
	medidor.RotY=+40
	p1 = p1 + (40000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If keycode = 36 and LAlarm2.State=2 and Volt>100 Then
	TimerASuich.Enabled=True
	medidor.RotY=+50
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If keycode = 42 and LViolRitmo.State=2 Then
		LL1.State=1
		CheckSitV()
	End If

	If keycode = 54 and LViolRitmo.State=2 Then
	   LR1.State=1
	   CheckSitV()
	End If

	If keycode = 42 and LRMA01.State=1 Then
		LL1.State=1
		CheckSitR()
	End If

	If keycode = 54 and LRMA01.State=1 Then
	   LR1.State=1
	   CheckSitR()
	End If

	If keycode = 6 Then
		PlaySound "CoinIn"
		credits = credits + 1
        OnScoreboardChanged()
		CheckComienzo()
	End If

    If keycode = 2 Then
		If credits > 0 and ball > 0 Then
		PlaySound "insertCoin"
		End If

		If credits = 0 and ball>0 Then
		PlaySound "aprietaEl1"
		End If

        If credits > 0 and ball=0 Then
			UltraDMD.CancelRendering()
			PlaySound "aprietaEl1"
            credits = credits - 1
			ball = ball + 3
			Addbolas (+6)
			LVIntro.state=0
			TimerVIntro.enabled=False
			Vid=0
			LTestigo.state=1
			Music = "YFloop.mp3"
			PlayMusic music
		CheckVideoIntro()
		CheckTVideo()
		OnScoreboardChanged()
        End If
    End If

	
	If keycode = 31 Then

		If ball=4 and bolas=8 Then
		plunger.createball
		PlaySound "BallRelease"
		addbolas (-1)
		OnScoreboardChanged
		End If

		If ball=3 and bolas=6 Then
		plunger.createball
		PlaySound "BallRelease"
		addbolas (-1)
		OnScoreboardChanged
		End If

		If ball=2 and bolas=4 Then
		plunger.createball
		PlaySound "BallRelease"
		addbolas (-1)
		OnScoreboardChanged
		End If

		If ball=1 and bolas=2 Then
		plunger.createball
		PlaySound "BallRelease"
		addbolas (-1)
		OnScoreboardChanged
		End If
	End If

   	If keycode = 37 Then
		p1 =0
		CheckPuntaje()
		OnScoreboardChanged()
	End If

	If keycode = 3 Then
		PlaySound "cargador"
		Luramp.state=1
		CheckLuramp()
	End If

	If keycode = 4 Then
		PlaySound "cargador"
		Luramp.state=0
		CheckLuramp()
	End If

End Sub


Sub CheckLuramp()
		If Luramp.state=0 Then
		RampDer.collidable=True
		RampIzq.collidable=True
		RampManoDerINF.RotX= 72
		RampIZQinf.RotX= 68
		End If

		If Luramp.state=1 Then
		RampDer.collidable=False
		RampIzq.collidable=False
		RampManoDerINF.RotX= 90
		RampIZQinf.RotX= 90
		End If

End Sub

'----------------------------------------------------------------------------
Sub Table1_KeyUp(ByVal keycode)

	If keycode = PlungerKey Then
		Plunger.Fire
		PlaySound "plunger",0,1,0.25,0.25
	End If
    
	If keycode = LeftFlipperKey Then
		LeftFlipper.RotateToStart
		LeftFlipper1.RotateToStart
		LeftFlipper2.RotateToStart
		PlaySound "fx_flipperdown", 0, 1, -0.05, 0.05
	End If
    
	If keycode = RightFlipperKey Then
		RightFlipper.RotateToStart
		RightFlipper1.RotateToStart
		RightFlipper2.RotateToStart
		RightFlipper3.RotateToStart
		PlaySound "fx_flipperdown", 0, 1, 0.05, 0.05
	End If

End Sub

'*******************************************************************
'******************** Atract Mode **********************************

'-------------Music/ Musica
Sub Table1_MusicDone()
	PlayMusic music
	Music = "YF_Audio_intro.mp3"
End Sub

'-----------------------------
Sub CheckComienzo()
	If LTestigo.State=0 Then
	Music = "YF_Audio_intro.mp3"
	PlayMusic music
	End If

	If LTestigo.State=1 Then
	Music = "YFloop.mp3"
	PlayMusic music
	End If

End Sub

'*******************************************************************
'*********************** Drain *************************************
Sub TimerFrBola_Timer()
	If LFrBolaViva.state=1 Then
	P1 = P1 + 30000
	End If

	If LFrbolaViva.state=0 Then
	TimerFrBola.enabled=False
	End If
End Sub

Sub RightOutlane_Hit()
	PlaySound "bolaPerdida"
End Sub

Sub LeftOutlane_Hit()
	PlaySound "bolaPerdida"
End Sub

Sub checkbolasm()
	If bolasm =3 Then
	Bolasm = 0
	Lmultiball.state=0
	End If
End Sub

Sub Drain_Hit()

	If Lmultiball.state=1 Then
	PlaySound "CoinMenosCredit"
	Drain.DestroyBall
	Addbolasm (+1)
	CheckbolasM()
	End If

	If bolas > 1 Then
	PlaySound "CoinMenosCredit"
	Drain.DestroyBall
	ball = ball - 1
	Addbolas (-1)
	OnScoreboardChanged
	p1 = p1 + (bonusACU*MultiPts)
	AddBonusLV (-BonusLV)
	CheckPuntaje()
	End If

	If bolas = 1 and Credits = 0 Then
	PlaySound "CoinMenosCredit"
	Drain.DestroyBall
	ball = ball - 1
	Addbolas (-1)
	OnScoreboardChanged
	p1 = p1 + (bonusACU*MultiPts)
	AddBonusLV (-BonusLV)
	CheckPuntaje()
	End If

	If bolas = 1 and Credits > 0 Then
	PlaySound "CoinMenosCredit"
	Drain.DestroyBall
	ball = ball + 3	
	addbolas (+7)
	credits = credits - 1
	OnScoreboardChanged
	p1 = p1 + (bonusACU*MultiPts)
	AddBonusLV (-BonusLV)
	CheckPuntaje()
	End If


	If bolas = 0 and ball = 0 and LFrBall.state=2 Then
	Drain.DestroyBall
	Plunger.CreateBall.Color=rgb(54,140,42) 'agregar ruido especial por frankenball
	PlaySound "BallRelease"
	TimerFrBola.enabled=True
	LFrBolaViva.state=1
	LFrball.state=0
	bolas = bolas + 1
	OnScoreboardChanged
	p1 = p1 + (bonusACU*MultiPts)
	AddBonusLV (-BonusLV)
	CheckPuntaje()
	End If

	If bolas = 0 and ball = 0 and LFrball.state=0 Then

	TimerFrBola.enabled=False
	LFrbolaViva.state=0

	Drain.DestroyBall
	'GAME OVER
	PlaySound "gameOver"
	bolas = 0
	ball = 0
	LFUE01.state=0
	LFUE02.state=0
	LFUEg03.state=0
	LFUE04.state=0
	LFUE05.state=0
	
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0

	PuertaIzq.IsDropped=False
	PuertaDer.Isdropped=False

	LESP01.state=0
	LESP02.state=0
	LESP03.state=0
	LESP04.State=0
	LESP05.state=0
	LdLESP01.state=0
	LdLESP02.state=0
	LdLESP03.state=0
	LdLESP04.State=0
	LdLESP05.state=0	

	TLibroC01.IsDropped=False
	TLibroC02.IsDropped=False
	TLibroC03.IsDropped=False
	TLibroC04.IsDropped=False
	TLibroC05.IsDropped=False
	LLMC01.State=0
	LLMC02.State=0
	LLMC03.State=0
	LLMC04.State=0
	LLMC05.State=0
	LdBiblCom01.state=0
	LdBiblCom02.state=0
	LdBiblCom03.state=0
	LdBiblCom04.state=0
	LdBiblCom05.state=0
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0

	TLibroP01.IsDropped=False
	TLibroP02.IsDropped=False
	TLibroP03.IsDropped=False
	TLibroP04.IsDropped=False
	TLibroP05.IsDropped=False
	LLSV01.State=0
	LdLLSV01.State=0
	LLSV02.State=0
	LdLLSV02.State=0
	LLSV03.State=0
	LdLLSV03.State=0
	LLSV04.State=0
	LdLLSV04.State=0
	LLSV05.State=0
	LdLLSV05.State=0
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0

	TKndelabro.IsDropped=False
	WallBiblio.IsDropped=False
	Biblio.TransY= 0
	Candel.TransY= 0
	VelaCand.TransY= 0
	Biblio.Collidable=True
	Candel.Collidable=True
	VelaCand.Collidable=True
	LCDB01.State=0
	LdLCDB01.State=0

	LUNI01.State=0
	LUNI02.State=0
	LUNI03.State=0
	LUNI04.State=0
	LUNI05.State=0
	LUNI06.State=0
	LdLUNI01.State=0
	LdLUNI02.State=0
	LdLUNI03.State=0
	LdLUNI04.State=0
	LdLUNI05.State=0
	LdLUNI06.State=0
	Pcamilla.TransX=0
	Nerd.TransY=0
	TFrede.IsDropped=False
	TCamilla.IsDropped=True

	LEspecialA.State=0
	TimerEspecial.enabled=False
	AddPlanka (-Planka)
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	LCZF01.State=0
	LdLCZF01.State=0
	LMES01.State=0
	FCabeza.Visible=False
	FQuijada.Visible=False
	FTorso.Visible=False
	FPelvis.Visible=False
	FBrazoD.Visible=False
	FBrazoI.Visible=False
	FAntBrazoD.Visible=False
	FAntBrazoI.Visible=False
	FManoD.Visible=False
	FManoI.Visible=False
	FPiernaD.Visible=False
	FPiernaI.Visible=False
	FAntPiernaD.Visible=False
	FAntPiernaI.Visible=False
	FPieD.Visible=False
	FPieI.Visible=False
	jarroCereb3.Visible=False
	LECF01.State=0
	LECF02.State=0
	LECF03.State=0
	LECF04.State=0
	LECF05.State=0
	LECF06.State=0
	LECF07.State=0
	LECF08.State=0
	LECF09.State=0
	LECF10.State=0
	LECF11.State=0
	LECF12.State=0
	LECF13.State=0
	LECF14.State=0
	LECF15.State=0
	LECF16.State=0
	LCHD01.State=0
	LdLCHD01.State=0
	LCAN01.State=0
	LdLCAN01.State=0
	LCCO01.State=0
	LdLCCO01.State=0
	Tcereb1.IsDropped=False
	Tcereb2.IsDropped=False
	Tcereb3.IsDropped=False
	LRLPG01.State=0
	LRLPG02.State=0
	LRLPG03.State=0
	LRPI01.State=0
	LdLRPI01.State=0
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	AddPartes (-Partes)
	AddPartesM (-PartesM)
	AldeanoP.IsDropped=True
	AldeanoP1.IsDropped=True
	AldeanoP2.IsDropped=True
	AldeanoP3.IsDropped=True
	AldeanoP4.IsDropped=True
	AldeanoP5.IsDropped=True
	AldeanoP6.IsDropped=True
	AldeanoP7.IsDropped=True
	AldeanoP8.IsDropped=True
	AldeanoP9.IsDropped=True
	MultiPts = 1
	BonusACU= 100
	LMulX2.State=0
	LdLMulX2.State=0
	LMulX3.State=0
	LdLMulX3.State=0
	LMulX4.State=0
	LdLMulX4.State=0
	LMulX5.State=0
	LdLMulX5.State=0
	LMulX6.State=0
	LdLMulX6.State=0
	LFrBall.state=0

	LMPpalE2.state=0

	LBLI01.State=0
	LdLBLI01.State=0
	LBLG01.State=0
	LdLBLG01.State=0
	LMCA01.State=0
	LdLMCA01.State=0
	LESP01.State=0
	LdLESP01.State=0

	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0	
	LIndicaLIGO.State=2
	LIGO01.State=0
	LdLIGO01.state=0
	LIGO02.State=0
	LdLIGO02.state=0
	LIGO03.State=0
	LdLIGO03.state=0
	LIGO04.state=0
	LdLIGO04.state=0
	LIndicaFDK.State=2
	LFDK01.state=0
	LdLFDK01.State=0
	LFDK02.state=0
	LdLFDK02.State=0
	LFDK03.State=0
	LdLFDK03.State=0
	LFDK04.state=0
	LdLFDK04.State=0
	LFDK05.state=0
	LdLFDK05.State=0
	LFDK06.state=0
	LdLFDK06.State=0
	LFDK07.state=0
	LdLFDK07.State=0
	LFDK08.state=0
	LdLFDK08.State=0
	LIndicaFNKT.state=2
	LFKT01.state=0
	LFKT02.state=0
	LFKT03.state=0
	LFKT04.state=0
	LFKT05.state=0
	LFKT06.state=0
	LFKT07.state=0
	LFKT08.state=0
	LFKT09.state=0
	LFKT10.state=0
	LFKT11.state=0
	LFKT12.state=0

	LCRE01.State=0
	Lapida.TransY=0
	Lapida.Collidable=True

	LHPS01.State=0
	LdLHPS01.State=0

	Blokeador01.IsDropped=True
	Blokeador02.IsDropped=True
	Blokeador03.IsDropped=True
	BlokCentral.Visible=False
	BlokCentral.Collidable=False
	LPDR01.State=0
	LdLPDR01.State=0
	LPDR02.State=0
	LdLPDR02.State=0
	LPDR03.State=0
	LdLPDR03.State=0
	LPDR04.State=0
	LdLPDR04.State=0
	LBBD01.State=0
	LdLBBD01.State=0
	LBBC01.State=0
	LdBloCent.State=0
	LBBI01.State=0
	LdLBBI01.State=0
	
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0

	LBrainTwist.State=0
	LdLBrainTwist.State=0
	LBTwist01.state=0
	LBTwist02.State=0
	LbrainL.State=0
	LbrainR.state=0
	LTBrainL.state=0
	LdLTBrainL.state=0
	LTBrainR.state=0
	LdLTBrainR.state=0

	LMTI01.State=0
	LdLMTI01.State=0
	LIDP01.State=0
	LdLIDP01.State=0
	LIDP02.State=0
	LdLIDP02.State=0
	LIDP03.State=0
	LdLIDP03.State=0
	LTQA01.State=0
	LdLTQA01.State=0

	LTQA01.State=0
	LTQA02.State=0
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=0
	LdLTQA02.State=0
	LdLTQA03.State=0
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0

	TStacion.IsDropped=False
	LMLCMTR.State=0
	LdLMLCMTR.State=0
	LFPP01.State=0
	LFPP02.State=0
	LFPP03.State=0
	LdLFPP01.State=0
	LdLFPP02.State=0
	LdLFPP03.State=0
	
	LViolRitmo.State=0
	LL.state=0
	LR.state=0
	LL1.state=0
	LR1.state=0

	TarimaRITZ.IsDropped=True
	TarimaRITZ.Visible=False

	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=0

	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False

	LESC01.State=0
	LESC02.State=0
	LESC03.State=0
	LESC04.State=0
	LESC05.State=0
	LESC06.State=0
	LdLESC01.State=0
	LdLESC02.State=0
	LdLESC03.State=0
	LdLESC04.State=0
	LdLESC05.State=0
	LdLESC06.State=0
	LRMA01.State=0
	LdLRMA01.State=0

	LMUS01.State=0
	LdLMUS01.State=0
	LTOM01.State=0
	LdLTOM01.State=0

	LAMM01.State=0
	agarradera.RotZ= -300
	agarradera.Visible=False
	LAMM02.State=0
	agarradera1.RotZ= -300
	agarradera1.Visible=False
	LAMM03.State=0
	agarradera2.RotZ= -300
	agarradera2.Visible=False
	LIRA.State=0
	Lsentado.State=0
	LFAM01.state=0

	LICC01.State=0
	
	LEspecialA.State=0

	L250K.state=0	
	L500K.state=0
	L1M.state=0
	L10M.state=0
	L50M.state=0
	L100M.state=0
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0

	LFrBall.state=0
	LuIntdCast.state=0
	DMD_DisplaySceneEx "gameover.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	

	OnScoreboardChanged
	p1 = p1 + (bonusACU*MultiPts)
	p2 = 1
	AddBonusLV (-BonusLV)
	CheckPuntaje()
	'GAME OVER
	LTestigo.state=0
	CheckComienzo()
	LVIntro.state=1
	End If

End Sub


Sub Addbolas(b0l) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	bolas = bolas + b0l ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

Sub AddbolasM(b0lm) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	bolasM = bolasM + b0lm ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

'Sub Plunger_Init()
'	Plunger.CreateBall
'	PlaySound "ballrelease",0,1,0,0.25
'End Sub

'Sub BallReleaseGate_Hit()
	'PlaySound "ballrelease",0,1,0,0.25
'End Sub
'******************************************************************
'*******************   Bumpers  ***********************************

Sub Bumper1_Hit()
	PlaySound "chispa"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub Bumper2_Hit()
	PlaySound "chispa"
		p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub
'-------------------------------------------------------------
Sub BumperCasaL_Hit()
	PlaySound "bumperpueblo"
	p1 = p1 + (200*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub BumperCasaR_Hit()
	PlaySound "bumperpueblo"
	p1 = p1 + (200*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub BumperCasaC_Hit()
	PlaySound "bumperpueblo"
	p1 = p1 + (200*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub



'*****GI Lights On
dim xx

For each xx in GI:xx.State = 1: Next

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
        Pan = Csng(-((- tmp) ^10) )
    End If
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

Const tnob = 5 ' total number of balls
ReDim rolling(tnob)
InitRolling

Sub InitRolling
    Dim i
    For i = 0 to tnob
        rolling(i) = False
    Next
End Sub

Sub RollingTimer_Timer()
    Dim BOT, b
    BOT = GetBalls

	' stop the sound of deleted balls
    For b = UBound(BOT) + 1 to tnob
        rolling(b) = False
        StopSound("fx_ballrolling" & b)
    Next

	' exit the sub if no balls on the table
    If UBound(BOT) = -1 Then Exit Sub

	' play the rolling sound for each ball
    For b = 0 to UBound(BOT)
        If BallVel(BOT(b) ) > 1 AND BOT(b).z < 30 Then
            rolling(b) = True
            PlaySound("fx_ballrolling" & b), -1, Vol(BOT(b) ), Pan(BOT(b) ), 0, Pitch(BOT(b) ), 1, 0
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
	PlaySound("fx_collide"), 0, Csng(velocity) ^2 / 2000, Pan(ball1), 0, Pitch(ball1), 0, 0
End Sub

'************************************
' What you need to add to your table
'************************************

' a timer called RollingTimer. With a fast interval, like 10
' one collision sound, in this script is called fx_collide
' as many sound files as max number of balls, with names ending with 0, 1, 2, 3, etc
' for ex. as used in this script: fx_ballrolling0, fx_ballrolling1, fx_ballrolling2, fx_ballrolling3, etc


'******************************************
' Explanation of the rolling sound routine
'******************************************

' sounds are played based on the ball speed and position

' the routine checks first for deleted balls and stops the rolling sound.

' The For loop goes through all the balls on the table and checks for the ball speed and 
' if the ball is on the table (height lower than 30) then then it plays the sound
' otherwise the sound is stopped, like when the ball has stopped or is on a ramp or flying.

' The sound is played using the VOL, PAN and PITCH functions, so the volume and pitch of the sound
' will change according to the ball speed, and the PAN function will change the stereo position according
' to the position of the ball on the table.


'**************************************
' Explanation of the collision routine
'**************************************

' The collision is built in VP.
' You only need to add a Sub OnBallBallCollision(ball1, ball2, velocity) and when two balls collide they 
' will call this routine. What you add in the sub is up to you. As an example is a simple Playsound with volume and paning
' depending of the speed of the collision.


Sub Pins_Hit (idx)
	PlaySound "pinhit_low", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
End Sub

Sub Targets_Hit (idx)
	PlaySound "target", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 0, 0
End Sub

Sub Metals_Thin_Hit (idx)
	PlaySound "metalhit_thin", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
End Sub

Sub Metals_Medium_Hit (idx)
	PlaySound "metalhit_medium", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
End Sub

Sub Metals2_Hit (idx)
	PlaySound "metalhit2", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
End Sub

Sub Gates_Hit (idx)
	PlaySound "gate4", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
End Sub

Sub Spinner_Spin
	PlaySound "fx_spinner",0,.25,0,0.25
End Sub

Sub Rubbers_Hit(idx)
 	dim finalspeed
  	finalspeed=SQR(activeball.velx * activeball.velx + activeball.vely * activeball.vely)
 	If finalspeed > 20 then 
		PlaySound "fx_rubber2", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
	End if
	If finalspeed >= 6 AND finalspeed <= 20 then
 		RandomSoundRubber()
 	End If
End Sub

Sub Posts_Hit(idx)
 	dim finalspeed
  	finalspeed=SQR(activeball.velx * activeball.velx + activeball.vely * activeball.vely)
 	If finalspeed > 16 then 
		PlaySound "fx_rubber2", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
	End if
	If finalspeed >= 6 AND finalspeed <= 16 then
 		RandomSoundRubber()
 	End If
End Sub

Sub RandomSoundRubber()
	Select Case Int(Rnd*3)+1
		Case 1 : PlaySound "rubber_hit_1", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
		Case 2 : PlaySound "rubber_hit_2", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
		Case 3 : PlaySound "rubber_hit_3", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
	End Select
End Sub

Sub LeftFlipper_Collide(parm)
 	RandomSoundFlipper()
End Sub

Sub RightFlipper_Collide(parm)
 	RandomSoundFlipper()
End Sub

Sub RandomSoundFlipper()
	Select Case Int(Rnd*3)+1
		Case 1 : PlaySound "flip_hit_1", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
		Case 2 : PlaySound "flip_hit_2", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
		Case 3 : PlaySound "flip_hit_3", 0, Vol(ActiveBall), Pan(ActiveBall), 0, Pitch(ActiveBall), 1, 0
	End Select
End Sub
'**********************************************************
'*************** DIMS *************************************
Dim Movi
Dim Partes
Dim PartesM
Dim Volt
Dim Timo
Dim Planka
Dim Agarra
Dim Suich
Dim Tswitch
Dim ContMED
Dim EsposaF
Dim Turba







'**********************************************************
'************ Video Intro *********************************
Sub LVintro_init()
	LVIntro.state=1
	CheckVideoIntro()
End Sub

Sub CheckVideoIntro()
	If LVIntro.state=1 Then
	TimerVIntro.enabled=true
	LVIntro.state=0
	CheckTVideo()
	End If
	
End Sub

Sub TimerVIntro_Timer()
	addVId (+1)
	CheckTVideo()
End Sub

Sub CheckTVideo()
	If Vid=5 and LVIntro.state=0 Then
	Trendor.enabled=True
	AddRendor (420)
	DMD_DisplaySceneEx "01MBruks.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 6000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If

	If Vid=15 and LVIntro.state=0 Then
	Trendor.enabled=True
	AddRendor (50)
	DMD_DisplaySceneEx "02Gabrom.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 5000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If

	If Vid=24 and LVIntro.state=0 Then
	Trendor.enabled=True
	AddRendor (70)
	DMD_DisplaySceneEx "03YFcartel.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 7000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If

	If Vid=35 and LVIntro.state=0 Then
	DMD_DisplaySceneEx "04Intro.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 24000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If

	If Vid=63 and LVIntro.state=0 Then
	TimerVIntro.enabled=False
	AddVid (-Vid)
	End If

End Sub

'-----------------------------------------------------------
Sub AddVid(v1D) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	Vid = Vid + V1D ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'**********************************************************
'**********************************************************
'**************** Tren de Inicio **************************
Sub TrigDPlungr_Hit()
	LbolPlung.state=1
	CheckTrenM
End Sub

Sub KickerIni_Hit()
	'PlaySound
	TimerIni.Enabled=True
	CheckTrenM()
End Sub

Sub TimerIni_Timer()
	If LbolPlung.state=1 Then
	AddMovi (+1)
	CheckTrenM()
	End If
End Sub

Sub CheckTrenM()
	If Movi = 0 and LbolPlung.state=1 Then
	Tren.TransX = 0
	Tren.RotY = 90
	Tren.TransZ = 0
	Vagon.TransX = 0
	Vagon.RotY = 90
	Vagon.TransZ = 0
	Furgon.TransX = 0
	Furgon.RotY = 90
	Furgon.TransZ = 0
	End If

	If Movi = 1 and LbolPlung.state=1 Then
	PlaySound "tren"
	Tren.TransX =50
	Tren.RotY = 90
	Tren.TransZ = 0
	Vagon.TransX = 50
	Vagon.RotY = 90
	Vagon.TransZ = 0
	Furgon.TransX = 50
	Furgon.RotY = 90
	Furgon.TransZ = 0
		If rendor=0 Then
		Trendor.enabled=True
		AddRendor (36)
		DMD_DisplaySceneEx "TrenAtrans.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3600, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Movi = 2 and LbolPlung.state=1 Then
	Tren.TransX = 100
	Tren.RotY = 90
	Tren.TransZ = 0
	Vagon.TransX = 100
	Vagon.RotY = 90
	Vagon.TransZ = 0
	Furgon.TransX = 100
	Furgon.RotY = 90
	Furgon.TransZ = 0
	End If

	If Movi = 3 and LbolPlung.state=1 Then
	Tren.TransX = 150
	Tren.RotY = 160
	Tren.TransZ = -25
	Vagon.TransX = 150
	Vagon.RotY = 160
	Vagon.TransZ = -45
	Furgon.TransX = 155
	Furgon.RotY = 180
	Furgon.TransZ = 0
	End If

	If Movi = 4 and LbolPlung.state=1 Then
	Tren.TransX = 180
	Tren.RotY = 130
	Tren.TransZ = -80
	Vagon.TransX = 170
	Vagon.RotY = 130
	Vagon.TransZ = -140
	Furgon.TransX = 210
	Furgon.RotY = 160
	Furgon.TransZ = -60
	End If

	If Movi = 5 and LbolPlung.state=1 Then
	Tren.TransX = 210
	Tren.RotY = 110
	Tren.TransZ = -140
	Vagon.TransX = 190
	Vagon.RotY = 120
	Vagon.TransZ = -180
	Furgon.TransX = 200
	Furgon.RotY = 130
	Furgon.TransZ = -190
	End If
	
	If Movi = 6 and LbolPlung.state=1 Then
	Tren.TransX = 240
	Tren.RotY = 90
	Tren.TransZ = -200
	Vagon.TransX = 190
	Vagon.RotY = 100
	Vagon.TransZ = -240
	Furgon.TransX = 160
	Furgon.RotY = 100
	Furgon.TransZ = -290
	End If

	If Movi = 7 and LbolPlung.state=1 Then
	Tren.TransX = 280
	Tren.RotY = 80
	Tren.TransZ = -250
	Vagon.TransX = 220
	Vagon.RotY = 90
	Vagon.TransZ = -280
	Furgon.TransX = 160
	Furgon.RotY = 92
	Furgon.TransZ = -330
	End If

	If Movi = 8 and LbolPlung.state=1 Then
	Tren.TransX = 340
	Tren.RotY = 70
	Tren.TransZ = -290
	Vagon.TransX = 245
	Vagon.RotY = 75
	Vagon.TransZ = -340
	Furgon.TransX = 190
	Furgon.RotY = 80
	Furgon.TransZ = -375
	End If

	If Movi = 9 and LbolPlung.state=1 Then
	Tren.TransX = 410
	Tren.RotY = 50
	Tren.TransZ = -410
	Vagon.TransX = 240
	Vagon.RotY = 45
	Vagon.TransZ = -500
	Furgon.TransX = 220
	Furgon.RotY = 55
	Furgon.TransZ = -495
	End If

	If Movi = 10 and LbolPlung.state=1 Then
	Tren.TransX = 460
	Tren.RotY = 40
	Tren.TransZ = -460
	Vagon.TransX = 230
	Vagon.RotY = 30
	Vagon.TransZ = -570
	Furgon.TransX = 225
	Furgon.RotY = 40
	Furgon.TransZ = -565
	End If
	
	If Movi = 11 and LbolPlung.state=1 Then
	Tren.TransX = 300
	Tren.RotY = 20
	Tren.TransZ = -600
	Vagon.TransX = 210
	Vagon.RotY = 25
	Vagon.TransZ = -600
	Furgon.TransX = 160
	Furgon.RotY = 30
	Furgon.TransZ = -610
	End If

	If Movi = 12 and LbolPlung.state=1 Then
	Tren.TransX = 230
	Tren.RotY = 10
	Tren.TransZ = -650
	Vagon.TransX = 145
	Vagon.RotY = 15
	Vagon.TransZ = -640
	Furgon.TransX = 210
	Furgon.RotY = 30
	Furgon.TransZ = -620
	End If

	If Movi = 13 and LbolPlung.state=1 Then
	Tren.TransX = 150
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 65
	Vagon.RotY = 5
	Vagon.TransZ = -680
	Furgon.TransX = 70
	Furgon.RotY = 15
	Furgon.TransZ = -660
	End If

	If Movi = 14 and LbolPlung.state=1 Then
	Tren.TransX = 170
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 27
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = -20
	Furgon.RotY = 5
	Furgon.TransZ = -680
	End If

	If Movi = 15 and LbolPlung.state=1 Then
	Tren.TransX = 190
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 50
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = -55
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If
	
	If Movi = 16 and LbolPlung.state=1 Then
	Tren.TransX = 210
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 70
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = -35
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 17 and LbolPlung.state=1 Then
	Tren.TransX = 230
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 90
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = -15
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 18 and LbolPlung.state=1 Then
	Tren.TransX = 250
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 110
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 5
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 19 and LbolPlung.state=1 Then
	Tren.TransX = 270
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 150
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 25
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 20 and LbolPlung.state=1 Then
	Tren.TransX = 290
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 150
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 45
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If
	
	If Movi = 21 and LbolPlung.state=1 Then
	Tren.TransX = 310
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 170
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 65
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 22 and LbolPlung.state=1 Then
	Tren.TransX = 330
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 190
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 85
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 23 and LbolPlung.state=1 Then
	Tren.TransX = 350
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 210
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 105
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 24 and LbolPlung.state=1 Then
	Tren.TransX = 370
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 230
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 125
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 25 and LbolPlung.state=1 Then
	Tren.TransX =390
	Tren.RotY =0
	Tren.TransZ = -690
	Vagon.TransX = 250
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 145
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If
	
	If Movi = 26 and LbolPlung.state=1 Then
	Tren.TransX = 410
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 270
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 165
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 27 and LbolPlung.state=1 Then
	Tren.TransX = 430
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 290
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 185
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 28 and LbolPlung.state=1 Then
	Tren.TransX = 450
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 310
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 205
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 29 and LbolPlung.state=1 Then
	Tren.TransX = 470
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 330
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 225
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 30 and LbolPlung.state=1 Then
	Tren.TransX = 490
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX =350
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 245
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If
	
	If Movi = 31 and LbolPlung.state=1 Then
	Tren.TransX = 510
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 370
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 265
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 32 and LbolPlung.state=1 Then
	Tren.TransX = 530
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 390
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 285
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 33 and LbolPlung.state=1 Then
	Tren.TransX = 550
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 410
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 305
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 34 and LbolPlung.state=1 Then
	Tren.TransX = 570
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 430
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 325
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 35 and LbolPlung.state=1 Then
	Tren.TransX = 590
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 450
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 345
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If
	
	If Movi = 36 and LbolPlung.state=1 Then
	Tren.TransX = 610
	Tren.RotY = 0
	Tren.TransZ = -690
	Vagon.TransX = 470
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 365
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 37 and LbolPlung.state=1 Then
	Vagon.TransX = 490
	Vagon.RotY = 0
	Vagon.TransZ = - 688
	Furgon.TransX = 385
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 38 and LbolPlung.state=1 Then
	Vagon.TransX = 510
	Vagon.RotY = 0 
	Vagon.TransZ = -688
	Furgon.TransX = 405
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 39 and LbolPlung.state=1 Then
	Vagon.TransX = 530
	Vagon.RotY = 0
	Vagon.TransZ = -688
	Furgon.TransX = 425
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 40 and LbolPlung.state=1 Then
	Furgon.TransX = 445
	Furgon.RotY = 0
	Furgon.TransZ = -688
	End If

	If Movi = 41 and LbolPlung.state=1 Then
	Tren.TransX = 0
	Tren.RotY = 90
	Tren.TransZ = 0
	Vagon.TransX = 0
	Vagon.RotY = 90
	Vagon.TransZ = 0
	Furgon.TransX = 0
	Furgon.RotY = 90
	Furgon.TransZ = 0
	TimerIni.enabled = False
	KickerIni.DestroyBall
	AddMovi (-movi)
	KickerIgor.CreateBall
	KickerIgor.Kick 335, 30
	LbolPlung.state=0
	PlaySound "Kickerkik"
	End If

	If Movi = 41 and LbolPlung.state=0 Then
	Tren.TransX = 0
	Tren.RotY = 90
	Tren.TransZ = 0
	Vagon.TransX = 0
	Vagon.RotY = 90
	Vagon.TransZ = 0
	Furgon.TransX = 0
	Furgon.RotY = 90
	Furgon.TransZ = 0
	TimerIni.enabled = False
	KickerIni.DestroyBall
	Movi = 0
	KickerIgor.CreateBall
	KickerIgor.Kick 335, 30
	LbolPlung.state=0
	PlaySound "Kickerkik"
	End If
	

End Sub


'----------------Contador de animacion TREN ------------------------

Sub AddMovi(mus) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Movi = Movi + mus ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

'*******************************************************************
'****************** Biblioteca *************************************

Sub TLibroC01_Hit()
	PlaySound "TargtDown"
	TLibroC01.IsDropped=True
	LLMC01.State=2
	LLMC02.State=2
	LLMC03.State=2
	LLMC04.State=2
	LLMC05.State=2
	LdBiblCom01.state=2
	LdBiblCom02.state=2
	LdBiblCom03.state=2
	LdBiblCom04.state=2
	LdBiblCom05.state=2
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro()
	CheckPuntaje()
End Sub

Sub TLibroC02_Hit()
	PlaySound "TargtDown"
	TLibroC02.IsDropped=True
	LLMC01.State=2
	LLMC02.State=2
	LLMC03.State=2
	LLMC04.State=2
	LLMC05.State=2
	LdBiblCom01.state=2
	LdBiblCom02.state=2
	LdBiblCom03.state=2
	LdBiblCom04.state=2
	LdBiblCom05.state=2
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro()
	CheckPuntaje()
End Sub


Sub TLibroC03_Hit()
	PlaySound "TargtDown"
	TLibroC03.IsDropped=True
	LLMC01.State=2
	LLMC02.State=2
	LLMC03.State=2
	LLMC04.State=2
	LLMC05.State=2
	LdBiblCom01.state=2
	LdBiblCom02.state=2
	LdBiblCom03.state=2
	LdBiblCom04.state=2
	LdBiblCom05.state=2
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro()
	CheckPuntaje()
End Sub

Sub TLibroC04_Hit()
	PlaySound "TargtDown"
	TLibroC04.IsDropped=True
	LLMC01.State=2
	LLMC02.State=2
	LLMC03.State=2
	LLMC04.State=2
	LLMC05.State=2
	LdBiblCom01.state=2
	LdBiblCom02.state=2
	LdBiblCom03.state=2
	LdBiblCom04.state=2
	LdBiblCom05.state=2
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro()
	CheckPuntaje()
End Sub

Sub TLibroC05_Hit()
	PlaySound "TargtDown"
	TLibroC05.IsDropped=True
	LLMC01.State=2
	LLMC02.State=2
	LLMC03.State=2
	LLMC04.State=2
	LLMC05.State=2
	LdBiblCom01.state=2
	LdBiblCom02.state=2
	LdBiblCom03.state=2
	LdBiblCom04.state=2
	LdBiblCom05.state=2
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro()
	CheckPuntaje()
End Sub

Sub CheckTLibro()
	If LLMC01.State=2 Then
	Timer10s.Enabled=True
	End If

	If TLibroC01.IsDropped=True and TLibroC02.IsDropped=True and TLibroC03.IsDropped=True and TLibroC04.IsDropped=True and TLibroC05.IsDropped=True Then
	LLMC01.State=1
	LLMC02.State=1
	LLMC03.State=1
	LLMC04.State=1
	LLMC05.State=1
	LdBiblCom01.state=0
	LdBiblCom02.state=0
	LdBiblCom03.state=0
	LdBiblCom04.state=0
	LdBiblCom05.state=0
	LdBiblComAct01.state=1
	LdBiblComAct02.state=1
	LdBiblComAct03.state=1
	LdBiblComAct04.state=1
	LdBiblComAct05.state=1
	p1 = p1 + (2000*MultiPts)
	PlaySound "LaVela"
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (37)
		Trendor.enabled=True
		DMD_DisplaySceneEx "libroCom.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub Timer10s_Timer()
	If LLMC01.State=2 Then
	Timer10s.Enabled=False
	PlaySound "TargtUP"
	TLibroC01.IsDropped=False
	TLibroC02.IsDropped=False
	TLibroC03.IsDropped=False
	TLibroC04.IsDropped=False
	TLibroC05.IsDropped=False
	LLMC01.State=0
	LLMC02.State=0
	LLMC03.State=0
	LLMC04.State=0
	LLMC05.State=0
	LdBiblCom01.state=0
	LdBiblCom02.state=0
	LdBiblCom03.state=0
	LdBiblCom04.state=0
	LdBiblCom05.state=0
	LdBiblComAct01.state=0
	LdBiblComAct02.state=0
	LdBiblComAct03.state=0
	LdBiblComAct04.state=0
	LdBiblComAct05.state=0
	p1 = p1 - (2000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LLMC01.State=1 Then
	Timer10s.Enabled=False
	LCDB01.State=2
	LdLCDB01.State=2
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (37)
		Trendor.enabled=True
		DMD_DisplaySceneEx "candelabro.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub


'---------------------- Candelabro --------------------------------	
	
Sub TKndelabro_Hit()
	If LCDB01.State=0 Then
	PlaySound "LaVela"
	p1 = p1 + (250*MultiPts)
	End If

	If LCDB01.State=2 Then
	PlaySound "LibroPribado"
	TKndelabro.IsDropped=True
	WallBiblio.IsDropped=True
	Biblio.TransY= -120
	Candel.TransY= -120
	VelaCand.TransY= -120
	Biblio.Collidable=False
	Candel.Collidable=False
	VelaCand.Collidable=False
	LCDB01.State=1
	LdLCDB01.State=1
	p1 = p1 + (4000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
	AddRendor (37)
	Trendor.enabled=True
	DMD_DisplaySceneEx "libropri.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3700, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'--------------------- Biblio Privada -----------------------------

Sub TLibroP01_Hit()
	If LCDB01.State=1 Then
	PlaySound "TargtDown"
	TLibroP01.IsDropped=True
	LLSV01.State=2
	LdLLSV01.State=2
	LLSV02.State=2
	LdLLSV02.State=2
	LLSV03.State=2
	LdLLSV03.State=2
	LLSV04.State=2
	LdLLSV04.State=2
	LLSV05.State=2
	LdLLSV05.State=2
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro01()
	CheckPuntaje()
	End If
End Sub

Sub TLibroP02_Hit()
	If LCDB01.State=1 Then
	PlaySound "TargtDown"
	TLibroP02.IsDropped=True
	LLSV01.State=2
	LdLLSV01.State=2
	LLSV02.State=2
	LdLLSV02.State=2
	LLSV03.State=2
	LdLLSV03.State=2
	LLSV04.State=2
	LdLLSV04.State=2
	LLSV05.State=2
	LdLLSV05.State=2
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro01()
	CheckPuntaje()
	End If
End Sub

Sub TLibroP03_Hit()
	If LCDB01.State=1 Then
	PlaySound "TargtDown"
	TLibroP03.IsDropped=True
	LLSV01.State=2
	LdLLSV01.State=2
	LLSV02.State=2
	LdLLSV02.State=2
	LLSV03.State=2
	LdLLSV03.State=2
	LLSV04.State=2
	LdLLSV04.State=2
	LLSV05.State=2
	LdLLSV05.State=2
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro01()
	CheckPuntaje()
	End If
End Sub

Sub TLibroP04_Hit()
	If LCDB01.State=1 Then
	PlaySound "TargtDown"
	TLibroP04.IsDropped=True
	LLSV01.State=2
	LdLLSV01.State=2
	LLSV02.State=2
	LdLLSV02.State=2
	LLSV03.State=2
	LdLLSV03.State=2
	LLSV04.State=2
	LdLLSV04.State=2
	LLSV05.State=2
	LdLLSV05.State=2
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro01()
	CheckPuntaje()
	End If
End Sub

Sub TLibroP05_Hit()
	If LCDB01.State=1 Then
	PlaySound "TargtDown"
	TLibroP05.IsDropped=True
	LLSV01.State=2
	LdLLSV01.State=2
	LLSV02.State=2
	LdLLSV02.State=2
	LLSV03.State=2
	LdLLSV03.State=2
	LLSV04.State=2
	LdLLSV04.State=2
	LLSV05.State=2
	LdLLSV05.State=2
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckTLibro01()
	CheckPuntaje()
	End If
End Sub

Sub CheckTLibro01()
	If LLSV01.State=2 Then
	TimerBiblioP.Enabled=True
	End If

	If TLibroP01.IsDropped=True and TLibroP02.IsDropped=True and TLibroP03.IsDropped=True and TLibroP04.IsDropped=True and TLibroP05.IsDropped=True Then
	LLSV01.State=1
	LdLLSV01.State=0
	LLSV02.State=1
	LdLLSV02.State=0
	LLSV03.State=1
	LdLLSV03.State=0
	LLSV04.State=1
	LdLLSV04.State=0
	LLSV05.State=1
	LdLLSV05.State=0
	LdLLSVact01.State=1
	LdLLSVact02.State=1
	LdLLSVact03.State=1
	LdLLSVact04.State=1
	LdLLSVact05.State=1
	PlaySound "diarioV"
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub



Sub TimerBiblioP_Timer()
	If LLSV01.State=2 Then
	TimerBiblioP.Enabled=False
	PlaySound "TargtUP"
	TLibroP01.IsDropped=False
	TLibroP02.IsDropped=False
	TLibroP03.IsDropped=False
	TLibroP04.IsDropped=False
	TLibroP05.IsDropped=False
	LLSV01.State=0
	LdLLSV01.State=0
	LLSV02.State=0
	LdLLSV02.State=0
	LLSV03.State=0
	LdLLSV03.State=0
	LLSV04.State=0
	LdLLSV04.State=0
	LLSV05.State=0
	LdLLSV05.State=0
	LdLLSVact01.State=0
	LdLLSVact02.State=0
	LdLLSVact03.State=0
	LdLLSVact04.State=0
	LdLLSVact05.State=0
	p1 = p1 - (4000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LLSV01.State=1 Then
	TimerBiblioP.Enabled=False
	LLCH01.State=1
	AddMultiPts (+1)
	CheckMultiplicaX2()
	CheckMultiplicaX3()
	CheckMultiplicaX4()
	CheckMultiplicaX5()
	CheckMultiplicaX6()
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		Addrendor (37)
		Trendor.enabled=True
		DMD_DisplaySceneEx "DiarioVic.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub



'*******************************************************************
'************ RAMPAS A FRANKY **************************************

'-----------------Inactivas-----------------------------------------

Sub KickerPieDer_Hit()
	If LRDP01.State=0 Then
	KickerPieDer.Kick 350, 9
	p1 = p1 + (200*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LRDP01.State=2 or LRDP01.State=2 Then
	KickerPieDer.Kick 45, 20
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub KickerManoDer_Init()
	KickerManoDer.enabled=False
End Sub

Sub KickerManoDer_Hit()
	If LRMD01.State=0 Then
	KickerManoDer.enabled=False
	RampDer.collidabel=False
	End If

	If LRMD01.State=2 or LRMD01.State=1 Then
	KickerManoDer.enabled=True
	KickerManoDer.Kick 0, 14
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub KickerPieIzq_Init()
	KickerPieIzq.enabled=False
End Sub

Sub KickerPieIzq_Hit()
	If LRPI01.State=0 Then
	KickerPieIzq.enabled=False
	RampIzq.collidabel=False
	End If

	If LRPI01.State=2 or LRPI01.State=1 Then
	KickerPieIzq.enabled=True
	KickerPieIzq.Kick 317, 16
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub


Sub KickerManoIzq_Init()
	KickerManoIzq.enabled=False
End Sub


Sub KickerManoIzq_Hit()
	If LRMI01.State=0 Then
	KickerManoIzq.enabled=False
	RampIzq.collidabel=False
	End If

	If LRMI01.State=2 or LRMI01.State=1 Then
	KickerManoIzq.enabled=True
	KickerManoIzq.Kick 317, 18
	p1 = p1 +(2000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub


'*************************************************************
'******************** Kikers a Cast **************************
Sub KickerACAST_Hit
	KickerACAST.kick 315, 45
	PlaySound "rayos"
		If rendor=0 Then
		Addrendor (12)
		Trendor.enabled=True
		DMD_DisplaySceneEx "casti.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1200, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
End Sub

Sub KickerACAST1_Hit
	KickerACAST1.kick 315, 47
	PlaySound "rayos"
		If rendor=0 Then
		Addrendor (12)
		Trendor.enabled=True
		DMD_DisplaySceneEx "casti.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1200, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
End Sub

Sub KickerACAST2_Hit
	KickerACAST2.kick 315, 50
	PlaySound "rayos"
		If rendor=0 Then
		Addrendor (12)
		Trendor.enabled=True
		DMD_DisplaySceneEx "casti.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1200, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
End Sub


'**************************************************************
'*************   KICKER BANCO y esp 03 *********************************

'-------------------- sin mision------------------------------
Sub KickerBanco_Hit()
	If LESP03.State=0 Then
	PlaySound "kickerKik"
	LESP03.State=1
	LdLESP03.State=1
	KickerBanco.Kick 280, 15
	p1 = p1 +(1000*MultiPts) 
	OnScoreboardChanged
	CheckEspecial()
	CheckPuntaje()
	End If

	If LESP03.State=1 Then
	PlaySound "kickerKik"
	LESP03.State=1
	LdLESP03.State=1
	KickerBanco.Kick 280, 15
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckEspecial()
	CheckPuntaje()
	End If
End Sub
'**************************************************************
'************* ESPECIAL 01 y Kicker IGOR  y O **********************

Sub KickerIgor_Hit()
	If LESP01.State=0 Then 
	PlaySound "kickerKik"
	LESP01.State=1
	LdLESP01.State=1
	LIGO03.State=1
	LdLIGO03.state=1
	KickerIgor.Kick 335, 30
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckEspecial()
	CheckPuntaje()
	End If

	If LESP01.State=2 Then
	PlaySound "kickerKik"
	LIGO03.State=1
	LdLIGO03.state=1
	LMCA01.State=2
	LdLMCA01.State=2
	LESP01.State=0
	LdLESP01.State=0
	KickerIgor.Kick 335, 30
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckCiegoEsp()
	CheckEspecial()
	CheckPuntaje()
	End If

	If LESP01.State=1 Then
	PlaySound "kickerKik"
	LESP01.State=1
	LdLESP01.State=1
	LIGO03.State=1
	LdLIGO03.state=1
	KickerIgor.Kick 335, 30
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckEspecial()
	CheckPuntaje()
	End If
End Sub

'**************************************************************
'************* KICKER SPECIAL 05 ******************************
Sub KickerESP05_Hit()
	If LESP05.state=0 Then
	PlaySound "kickerKik"
	LFKT09.state=1
	LESP05.State=1
	LdLESP05.State=1
	KickerEsp05.DestroyBall
	KickerIgor.Createball
	KickerIgor.Kick 330, 35
	p1 = p1 + (3000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckEspecial()
	CheckPuntaje()
	End If
	
	If LESP05.state=1 Then
	PlaySound "kickerKik"
	LFKT09.state=1
	LESP05.State=1
	LdLESP05.State=1
	KickerEsp05.Kick 330, 35
	p1 = p1 + (150*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckEspecial()
	CheckPuntaje()
	End If
End Sub

'**************************************************************
'************* KICKER SPECIAL 02 ******************************
Sub KickerESP02_Hit()
	If LESP02.state=0 Then
	PlaySound "kickerKik"
	LESP02.State=1
	LdLESP02.State=1
	LFDK03.State=1
	LdLFDK03.State=1
	LFDK05.State=1
	LdLFDK05.State=1
	LFKT06.State=1
	LFKT10.State=1
	KickerESP02.Kick 70, 14
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckEspecial()
	CheckPuntaje()
	End If
	
	If LESP02.state=1 Then
	PlaySound "kickerKik"
	LESP02.State=1
	LdLESP02.State=1
	LFDK03.State=1
	LdLFDK03.State=1
	LFDK05.State=1
	LdLFDK05.State=1
	LFKT06.State=1
	LFKT10.State=1
	KickerESP02.Kick 70, 14
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckEspecial()
	CheckPuntaje()
	End If
End Sub

'**************************************************************
'************* KICKER SPECIAL 04 ******************************
Sub KickerCuevaDel_Hit()
	If LESP04.state=0 Then
	PlaySound "kickerKik"
	LESP04.State=1
	LdLESP04.State=1
	KickerCuevaDel.Kick 175, 10
	p1 = p1 +(10000*MultiPts) 
	OnScoreboardChanged
	CheckEspecial()
	CheckPuntaje()
	End If
	
	If LESP04.state=1 Then
	PlaySound "kickerKik"
	LESP04.State=1
	LdLESP04.State=1
	KickerCuevaDel.Kick 175, 10
	p1 = p1 +(5000*MultiPts) 
	OnScoreboardChanged
	CheckEspecial()
	CheckPuntaje()
	End If

	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "kickerKik"
	AddFdespertar (-15)
	LESP04.State=1
	LdLESP04.State=1
	KickerCuevaDel.Kick 175, 10
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckDespertar()
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "kickerKik"
	AddIra (-15)
	AddTurba (-1)
	AddFdespertar (-15)
	LESP04.State=1
	LdLESP04.State=1
	KickerCuevaDel.Kick 175, 10
	p1 = p1 +(5000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckEspecial()
	CheckDespertar()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

End Sub

'**************************************************************
'************* KICKER Cueva Posterior *************************
Sub KickerCuevaPost_Hit()
	If LDespierto.State=0 Then
	PlaySound "kickerKik"
	AddFdespertar (-15)
	KickerCuevaDel.Kick 175, 10
	CheckDespertar()
	DMD_DisplaySceneEx "cueva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1100, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "kickerKik"
	AddIra (-15)
	AddTurba (-1)
	AddFdespertar (-15)
	LESP04.State=1
	LdLESP04.State=1
	KickerCuevaDel.Kick 175, 10
	CheckIRA()
	CheckDespertar()
	DMD_DisplaySceneEx "cueva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1100, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	p1 = p1 + (7000*MultiPts)
	PlaySound "kickerKik"
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
	CheckFparla()
	End If
End Sub

'**************************************************************
'************* KICKER Central *************************
Sub KickerCent_Hit()
	If LDespierto.State=0 Then
	PlaySound "kickerKik"
	LFDK04.State=1
	LdLFDK04.State=1
	KickerCent.Kick 15, 10
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "kickerKik"
	LFDK04.State=1
	LdLFDK04.State=1
	KickerCent.Kick 15, 10
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckPuntaje()
	End If
End Sub

'**************************************************************
'*************** Mision PPal Etapa 02 *************************
Sub PuertaDer_Hit()
	If LLCH01.State=1 Then
	PlaySound "knokers"
	PuertaDer.IsDropped=True
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub PuertaIzq_Hit()
	If LLCH01.State=1 Then
	PlaySound "knokers"
	PuertaIzq.IsDropped=True
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub KickerCast_Hit()
	If LMPpalE2.State=0 Then
	PlaySound "aprietael1"
	LMPpalE2.State=2
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddMultiPts (+1)
	CheckMultiplicaX3()
	CheckMultiplicaX4()
	CheckMultiplicaX5()
	CheckMultiplicaX6()
	p1 = p1 + (25000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "casti.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LMPpalE2.State=2 and Partes=0 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	AddPartes (+1)
	KickerCuevaPost.CreateBall
	KickerCuevaPost.Kick 290, 16
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=true
		DMD_DisplaySceneEx "pelvis.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	LSPR.State=2
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "torso.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=1 and LECF01.State=1 Then
	PlaySound "Partes"
	PlaySound "Partes"
	AddPartes (+1)
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "torso.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=2 and LECF02.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "brazo.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=3 and LECF03.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "brazo.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=4 and LECF04.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "brazo.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=5 and LECF05.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "brazo.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=6 and LECF06.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "mano.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=7 and LECF07.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "mano.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=8 and LECF08.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pierna.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=9 and LECF09.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pierna.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=10 and LECF10.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pierna.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=11 and LECF11.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pierna.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=12 and LECF12.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=0
		DMD_DisplaySceneEx "pie.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=13 and LECF13.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pie.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=14 and LECF14.State=1 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	AddPartes (+1)
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()	
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "cabeza.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	'----- Cabeza -----
	If Partes=15 and LECF15.State=0 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	'PlaySound
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Lab.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If Partes=15 and LECF15.State=2 and PartesM>15 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	'PlaySound
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Lab.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=15 and LECF15.State=2 and PartesM=15 Then
	PlaySound "Partes"
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	KickerCast.DestroyBall
	KickerACereb.CreateBall
	KickerACereb.Kick 270, 12
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "crebro.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'----- Cerebro ----

	If Partes=16 and PartesM<16 and LECF16.State=0 Then
	LECF16.State=2
	p1 = p1 + (15000*MultiPts) 
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Lab.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=16 and PartesM<16 and LECF16.State=2 Then
	LCCO01.State=2
	LdLCCO01.State=2
	LCAN01.State=2
	LdLCAN01.State=2
	LCHD01.State=2
	LdLCHD01.State=2
	p1 = p1 + (15000*MultiPts)
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Lab.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Partes=16 And PartesM=16 Then
	KickerCast.DestroyBall
	KickerACereb.CreateBall
	KickerACereb.Kick 270, 12
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (50)
		Trendor.enabled=True
		DMD_DisplaySceneEx "FrankyCompleto.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 5000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	'--------------conteo de partes --------------------------------
	
	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "kickerkik"
	AddFdespertar (-5)
	AddIRA (-10)
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	CheckDespertar()
	CheckIRA()
	CheckFparla()
	End If

	If LDespierto.State=1 Then
	PlaySound "kickerkik"
	AddFdespertar (-5)
	AddIRA (-10)
	AddFdespertar (-10)
	AddTurba (-1)
	KickerCast.DestroyBall
	KickerCuevaDel.CreateBall
	KickerCuevaDel.Kick 170, 7
	CheckTurr()
	CheckDespertar()
	CheckIRA()
	CheckFparla()
	End If

End Sub

'*******************************************************************
'******************   TUMBA   **************************************

'----------------  Activa ----------------------------------------
Sub TLapida_Hit()
	If LSPR.State=0 Then
	PlaySound"Lapida"
	p1 = p1 + (100*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'---01
	If LSPR.State=2 and Partes=1 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	p1 = p1 + (10000*MultiPts)
	KickerBodyParts.enabled=True
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=1 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor=30
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=1 and LECF01.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---02
	 If LSPR.State=2 and Partes=2 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
	End If

	If LSPR.State=2 and Partes=2 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=2 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End	If

	If LSPR.State=2 and Partes=2 and LECF02.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---03
	If LSPR.State=2 and Partes=3 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=3 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=3 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=3 and LECF03.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---04
	If LSPR.State=2 and Partes=4 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=4 and sepulcro.Collidable=False Then
	'PlaySound""

	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=4 Then
	'PlaySound""
	p1 = p1 +(10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=4 and LECF04.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---05
	If LSPR.State=2 and Partes=5 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=5 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=5 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=5 and LECF05.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---06
	If LSPR.State=2 and Partes=6 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=6 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=6 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=6 and LECF06.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---07
	If LSPR.State=2 and Partes=7 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=7 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=7 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=7 and LECF07.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---08
	If LSPR.State=2 and Partes=8 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=8 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=8 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=8 and LECF08.State=1 Then
	'PlaySound""
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---09
	If LSPR.State=2 and Partes=9 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=9 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=9 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=9 and LECF09.State=1 Then
	'PlaySound""
	p1 = (p1 +10000)*MultiPts 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---10
	If LSPR.State=2 and Partes=10 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=10 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=10 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=10 and LECF10.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---11
	If LSPR.State=2 and Partes=11 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=11 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=11 Then
	'PlaySound""
	p1 =  p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=11 and LECF11.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---12
	If LSPR.State=2 and Partes=12 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=12 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=12 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=12 and LECF12.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---13
	If LSPR.State=2 and Partes=13 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=13 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=13 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=13 and LECF13.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---14
	If LSPR.State=2 and Partes=14 Then
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=14 and sepulcro.Collidable=False Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and PartesM=14 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=14 and LECF14.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---15
	If LSPR.State=2 and Partes=15 Then
	'PlaySound""
	sepulcro.TransY= -30
	sepulcro.Collidable=False
	KickerBodyParts.enabled=True
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If LSPR.State=2 and PartesM=15 and sepulcro.Collidable=False Then
	'PlaySound""
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=15 and LECF15.State=1 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	'---16
	If LSPR.State=2 and Partes=16 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If LSPR.State=2 and PartesM=16 Then
	'PlaySound""
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LSPR.State=2 and Partes=16 and LECF16.State=1 Then
	'PlaySound""
		If rendor=0 Then
		Addrendor (30)
		Trendor.enabled=True
		DMD_DisplaySceneEx "BodyParts.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

'-------------------- Mision 02 Esposa Franky --------------------------------------
	If LCZF01.State=2 Then
	AddEsposaF (+1)
	CheckM2Esposa()
	p1 = p1 + (7000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	End If
End Sub

Sub KickerBodyParts_init()
	KickerBodyParts.enabled=False
End Sub


Sub KickerBodyParts_Hit()
	If Partes=0 Then
	KickerBodyParts.enabled=False
	End If
	
	If Partes=1 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=2
	LdLRMD01.State=2
	LECF01.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts) 
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=2 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=2
	LdLRMD01.State=2
	LECF02.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts) 
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If Partes=3 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=2
	LdLRMD01.State=2
	LECF03.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=4 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMI01.State=2
	LdLRMI01.State=2
	LRMD01.State=0
	LdLRMD01.State=0
	LECF04.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts) 
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=5 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=2
	LdLRMD01.State=2
	LECF05.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=6 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMI01.State=2
	LdLRMI01.State=2
	LRMD01.State=0
	LdLRMD01.State=0
	LECF06.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts) 
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If Partes=7 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=2
	LdLRMD01.State=2
	LECF07.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=8 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMI01.State=2
	LdLRMI01.State=2
	LRMD01.State=0
	LdLRMD01.State=0
	LECF08.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	
	If Partes=9 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=2
	LdLRDP01.State=2
	LECF09.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=10 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	LRPI01.State=2
	LdLRPI01.State=2
	LECF10.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If Partes=11 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRPI01.State=0
	LdLRPI01.State=0
	LRDP01.State=2
	LdLRDP01.State=2
	LECF11.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=12 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	LRPI01.State=2
	LdLRPI01.State=2
	LECF12.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts) 
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If Partes=13 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRPI01.State=0
	LdLRPI01.State=0
	LRDP01.State=2
	LdLRDP01.State=2
	LECF13.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=14 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	LRPI01.State=2
	LdLRPI01.State=2
	LECF14.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Partes=15 Then
	PlaySound"Desentierra"
	KickerBodyParts.Kick 315, 10
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	LRPI01.State=0
	LdLRPI01.State=0
	LECF15.State=2
	sepulcro.TransY= 0
	sepulcro.Collidable=True
	AddPartesM (+1)
	p1 = p1 + (1000*MultiPts)
	KickerBodyParts.enabled=False
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub



'*******************************************************************
'************ RAMPAS A FRANKY **************************************

'----------------  Activas  ----------------------------------------

'*********** Tapines de Rampas *************************************




'*******************************************************************
'************* KICKERS DE MESA *************************************

Sub KickerManoD_Hit()
	'------Pelvis
	If PartesM=1 and Partes=1 Then
	FPelvis.Visible=True
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF01.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'------Torso
	If PartesM=2 and Partes=2 and LHPS01.State=0 Then
	FTorso.Visible=True
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=2 and Partes=2 and LHPS01.State=2 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=2 and Partes=2 and LHPS01.State=1 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF02.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'-------- Brazo Der ---------------------------------------
	If PartesM=3 and Partes=3 and LHPS01.State=0 Then
	FBrazoD.Visible=True
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=3 and Partes=3 and LHPS01.State=2 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=3 and Partes=3 and LHPS01.State=1 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF03.State=1
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'------- AnnteBrazo Der ---------------------------------------------
	If PartesM=5 and Partes=5 and LHPS01.State=0 Then
	FAntBrazoD.Visible=True
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=5 and Partes=5 and LHPS01.State=2 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = (p1 +1000)*MultiPts 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=5 and Partes=5 and LHPS01.State=1 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF05.State=1
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

'------------  Mado Der --------------------------------------------
	If PartesM=7 and Partes=7 and LHPS01.State=0 Then
	FManoD.Visible=True
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=7 and Partes=7 and LHPS01.State=2 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=7 and Partes=7 and LHPS01.State=1 Then
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF07.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LCZF01.State=2 Then
	AddFdespertar (+5)
	KickerManoD.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	CheckDespertar()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

End Sub


Sub KickerManoI_Hit()
		'------- Brazo Izq -----------------------------------------
	If PartesM=4 and Partes=4 and LHPS01.State=0 Then
	FBrazoI.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=4 and Partes=4 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=4 and Partes=4 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF04.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'---------- AnteBrazo Izq ------------------------------------------
	If PartesM=6 and Partes=6 and LHPS01.State=0 Then
	FAntBrazoI.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=6 and Partes=6 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged

	End If

	If PartesM=6 and Partes=6 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF06.State=1
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	'-------------- Mano Izq -------------------------------------------
	If PartesM=8 and Partes=8 and LHPS01.State=0 Then
	FManoI.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=8 and Partes=8 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=8 and Partes=8 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF08.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LCZF01.State=2 Then
	AddFdespertar (+5)
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	CheckDespertar()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub KickerPieD_Hit()
'----------- Pierna Der --------------------------------------------
	If PartesM=9 and Partes=9 and LHPS01.State=0 Then
	FPiernaD.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=9 and Partes=9 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = (p1 +1000)*MultiPts 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=9 and Partes=9 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF09.State=1
	p1 = (p1 +1000)*MultiPts 
	OnScoreboardChanged
	CheckPuntaje()
	End If
'----------- AntePierna Der ----------------------------------------
	If PartesM=10 and Partes=10 and LHPS01.State=0 Then
	FAntPiernaD.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=10 and Partes=10 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=10 and Partes=10 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF10.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
'----------- Pie Der -----------------------------------------------
	If PartesM=11 and Partes=11 and LHPS01.State=0 Then
	FPieD.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 +(1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=11 and Partes=11 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 +(1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=11 and Partes=11 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF11.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LCZF01.State=2 Then
	AddFdespertar (+5)
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	CheckDespertar()
	p1 = p1 +(1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub KickerPieI_Hit()
'----------- Pierna Izq --------------------------------------------
	If PartesM=12 and Partes=12 and LHPS01.State=0 Then
	FPiernaI.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	End If
	
	If PartesM=12 and Partes=12 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=12 and Partes=12 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF12.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
'----------- Antepierna Izq ----------------------------------------
	If PartesM=13 and Partes=13 and LHPS01.State=0 Then
	FAntPiernaI.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	LdLHPS01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If PartesM=13 and Partes=13 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=13 and Partes=13 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF13.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
'----------- Pie Izq -----------------------------------------------
	If PartesM=14 and Partes=14 and LHPS01.State=0 Then
	FPieI.Visible=True
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LHPS01.State=2
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged	
	CheckPuntaje()
	End If
	
	If PartesM=14 and Partes=14 and LHPS01.State=2 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=14 and Partes=14 and LHPS01.State=1 Then
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	LECF14.State=1
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LCZF01.State=2 Then
	AddFdespertar (+5)
	KickerManoI.DestroyBall
	KickerCent.CreateBall
	KickerCent.Kick 350, 15
	CheckDespertar()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

'----------- Cabeza ------------------------------------------------
Sub KickerCabeza_Hit()
	If Partes=15 and PartesM=15 and LHPS01.State=0 Then
	FCabeza.Visible=True
	FQuijada.Visible=True
	LHPS01.State=2
	KickerCabeza.DestroyBall
	KickerIgor.CreateBall
	KickerIgor.Kick 330, 35
	End If

	If Partes=15 and PartesM=15 and LHPS01.State=1 Then
	LECF15.State=1
	AddPartes (+1)
	KickerCabeza.DestroyBall
	KickerIgor.CreateBall
	KickerIgor.Kick 335, 35
	LHPS01.State=0
	LdLHPS01.State=0
	AddMultiPts (+1)
	CheckMultiplicaX4()
	CheckMultiplicaX5()
	CheckMultiplicaX6()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

'----------- Cerebro -----------------------------------------------
	If Partes=16 and PartesM=16 and LHPS01.State=0 Then
	jarroCereb3.Visible=False
	LHPS01.State=2
	LdLHPS01.State=2
	KickerCabeza.DestroyBall
	KickerIgor.CreateBall
	KickerIgor.Kick 335, 30

	End If

	If Partes=16 and PartesM=16 and LHPS01.State=1 Then
	LECF16.State=1
	KickerCabeza.DestroyBall
	KickerIgor.CreateBall
	KickerIgor.Kick 335, 30
	LHPS01.State=0
	LdLHPS01.State=0
	LMES01.State=2
	AddMultiPts (+1)
	CheckMultiplicaX5()
	CheckMultiplicaX6()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub
 
'*******************************************************************
'******************** HILO *****************************************
Sub THilo_Hit()
	If LHPS01.State=0 and Partes=0 Then
	PlaySound "Round"
	p1 = p1 + (100*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LHPS01.State=0 and Partes=1 Then
	PlaySound "Round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LHPS01.State=1 Then
	PlaySound "Round"
	p1 = p1 + (1200*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LHPS01.State=2 Then
	PlaySound "Round"
	LHPS01.State=1
	LdLHPS01.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LCZF01.State=2 Then
	PlaySound "Round"
	AddFdespertar (+2)
	CheckDespertar()
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.state=0 and LCZf01.State=2 Then
	PlaySound "Round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LAMM01.state=1 Then
	PlaySound "Round"
	AddIra (-1)
	AddTurba (-1)
	FManoD.RotX=85
	CheckIRA()
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.State=1 Then
	PlaySound "Round"
	AddIra (-5)
	AddTurba (-1)
	FmanoD.Rotx=100
	CheckIRA()
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
	CheckFparla()
	End If
	
End Sub




'*******************************************************************
'******************** Banco de Cerebros ****************************

'---------- Cerebros -----------------
'---------- ABNORMAL -----------------
Sub Tcereb2_Hit()
	If LCAN01.State=0 Then
	PlaySound "VidrioCer"
	p1 = p1 + (100*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=16 Then
	PlaySound "VidrioCer"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If PartesM=15 Then
	PlaySound "targtDown"
	Tcereb2.IsDropped=True
	LCAN01.State=1
	LdLCAN01.State=1
	AddPartesM (+1)
	LRLPG01.State=2
	LRLPG02.State=2
	LRLPG03.State=2
	p1 = p1 + (25000*MultiPts) 
	OnScoreboardChanged
	CheckCereb()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (15)
		TRendor.enabled=True
		DMD_DisplaySceneEx "cerebro.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub
'---------Hans Delbrook---------------
Sub Tcereb1_Hit()
	If LCHD01.State=0 Then
	PlaySound "VidrioCer"
	End If

	If PartesM=16 Then
	PlaySound "VidrioCer"
	End If

	If PartesM=15 Then
	PlaySound "targtDown"
	Tcereb2.IsDropped=True
	LCHD01.State=1
	LdLCHD01.State=1
	AddPartesM (+1)
	LRLPG01.State=2
	LRLPG02.State=2
	LRLPG03.State=2
	p1 = p1 + (2000*MultiPts)
	OnScoreboardChanged
	CheckCereb()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (15)
		TRendor.enabled=True
		DMD_DisplaySceneEx "cerebro.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub
'------- Charles Opye ----------------	
Sub Tcereb3_Hit()
	If LCCO01.State=0 Then
	PlaySound "VidrioCer"
	End If

	If PartesM=16 Then
	PlaySound "VidrioCer"
	End If

	If PartesM=15 Then
	PlaySound "TargtDown"
	Tcereb2.IsDropped=True
	LCCO01.State=1
	LdLCCO01.State=1
	AddPartesM (+1)
	LRLPG01.State=2
	LRLPG02.State=2
	LRLPG03.State=2
	p1 = p1 + (1500*MultiPts) 
	OnScoreboardChanged
	CheckCereb()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (15)
		TRendor.enabled=True
		DMD_DisplaySceneEx "cerebro.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckCereb()
	If Tcereb1.IsDropped=True and Tcereb2.IsDropped=True and Tcereb3.IsDropped=True Then
	LFAM01.State=2
	End If
End Sub

'---------- Rayos --------------------
Sub Trelamp1_Hit()
	If PartesM=0 Then
	PlaySound "rayo"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If PartesM=15 Then
	PlaySound"cristales"
	p1 = p1 - (3000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If PartesM=16 Then
	PlaySound "cristales"
	AddPartesM (-1)
	p1 = p1 - (3000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCZF01.State=2 Then
	PlaySound "rayo"
	AddFdespertar (+1)
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckDespertar()
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub Trelamp2_Hit()
	If PartesM=0 Then
	PlaySound "rayo"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If PartesM=15 Then
	PlaySound "Cristales"
	p1 = p1 - (3000*MultiPts) 
	OnScoreboardChanged
	'PlaySound""
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If PartesM=16 Then
	PlaySound "Cristales"
	AddPartesM (-1)
	p1 = p1 - (3000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCZF01.State=2 Then
	PlaySound "rayo"
	AddFdespertar (+1)
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckDespertar()
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub Trelamp3_Hit()
	If PartesM=0 Then
	PlaySound "rayo"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If PartesM=15 Then
	PlaySound "cristales"
	p1 = p1 - (3000*MultiPts) 
	OnScoreboardChanged
	'PlaySound""
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If PartesM=16 Then
	PlaySound "Cristales"
	AddPartesM (-1)
	p1 = p1 - (3000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCZF01.State=2 Then
	PlaySound "rayo"
	AddFdespertar (+1)
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckDespertar()
	CheckPuntaje()
		If Rendor=0 Then
		AddRendor (10)
		DMD_DisplaySceneEx "rayo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'---------- Reseteador ---------------
Sub ResetCereb_Hit()
	If LFAM01.State=0 Then
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	PlaySound"round"
	CheckPuntaje()
	End If

	If LFAM01.State=2 Then
	PlaySound "TargtUP"
	Tcereb1.IsDropped=False 
	Tcereb2.IsDropped=False
	Tcereb3.IsDropped=False
	LFAM01.State=0
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
'------Movimiento de agarraderas ----------------------
'-------------Agarradera 01 -----------------------------------
	If LMES01.State=2 and LAMM01.State=0 Then
	LAMM01.State=2
	TimerAAgarr.Enabled=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckAgarrM()
	CheckAgarrF()
	CheckPuntaje()
	End If

	If LMES01.State=2 and LAMM01.State=2 Then
	'PlaySound
	End If
'-------------Agarradera 02 -----------------------------------
	If LMES01.State=2 and LAMM01.State=1 and LAMM02.State=0 Then
	LAMM02.State=2
	TimerAAgarr.Enabled=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckAgarrM1()
	CheckAgarrF()
	CheckPuntaje()
	End If
	
	If LMES01.State=2 and LAMM02.State=2 Then
	'PlaySound
	End If
'-------------Agarradera 03-------------------------------------
	If LMES01.State=2 and LAMM01.State=1 and LAMM02.State=1 and LAMM03.State=0 Then
	LAMM03.State=2
	TimerAAgarr.Enabled=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckAgarrM2()
	CheckAgarrF()
	CheckPuntaje()
	End If
	
	If LMES01.State=2 and LAMM03.State=2 Then
	PlaySound "round"
	End If

End Sub

'---------------- Conntador de Partes ------------------------------
Sub AddPartes(Part) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Partes = Partes + Part ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'---------------- Cuenta de partes en Manos ------------------------
Sub AddPartesM(Partm) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    PartesM = PartesM + Partm ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub


'*******************************************************************
'********* MISION PPAL ETAPA 4  Electricidad  **********************

'---------------Animadores------------------------------------------
'---------Timer Animacion Agarraderas ( TimerAAgarr)----------------

Sub TimerAgarr_Timer()
	AddAgarra (+1)
	CheckAgarrM()
	CheckAgarrF()
End Sub

Sub CheckAgarrM()
	If Agarra= 1 and LAMM01.State=2 Then
	agarradera.RotZ= -290
	PlaySound "agarra"
	End If

	If Agarra= 2 and LAMM01.State=2 Then
	agarradera.RotZ= -280
	End If

	If Agarra= 3 and LAMM01.State=2 Then
	agarradera.RotZ= -270
	End If

	If Agarra= 4 and LAMM01.State=2 Then
	agarradera.RotZ= -260
	agarradera.Visible=true
	End If

	If Agarra= 5 and LAMM01.State=2 Then
	agarradera.RotZ= -250
	End If

	If Agarra= 6 and LAMM01.State=2 Then
	agarradera.RotZ= -240
	End If

	If Agarra= 7 and LAMM01.State=2 Then
	agarradera.RotZ= -230
	End If

	If Agarra= 8 and LAMM01.State=2 Then
	agarradera.RotZ= -220
	End If

	If Agarra= 9 and LAMM01.State=2 Then
	agarradera.RotZ= -210
	End If

	If Agarra= 10 and LAMM01.State=2 Then
	agarradera.RotZ= -200
	End If

	If Agarra= 11 and LAMM01.State=2 Then
	agarradera.RotZ= -190
	End If

	If Agarra= 12 and LAMM01.State=2 Then
	agarradera.RotZ= -180
	End If

	If Agarra= 13 and LAMM01.State=2 Then
	agarradera.RotZ= -170
	End If

	If Agarra= 14 and LAMM01.State=2 Then
	agarradera.RotZ= -160
	End If

	If Agarra= 15 and LAMM01.State=2 Then
	agarradera.RotZ= -150
	End If

	If Agarra= 16 and LAMM01.State=2 Then
	agarradera.RotZ= -140
	End If

	If Agarra= 17 and LAMM01.State=2 Then
	agarradera.RotZ= -130
	End If

	If Agarra= 18 and LAMM01.State=2 Then
	agarradera.RotZ= -120
	End If

	If Agarra= 19 and LAMM01.State=2 Then
	agarradera.RotZ= -110
	End If

	If Agarra= 20 and LAMM01.State=2 Then
	agarradera.RotZ= -100
	End If

	If Agarra= 21 and LAMM01.State=2 Then
	agarradera.RotZ= -90
	End If

	If Agarra= 22 and LAMM01.State=2 Then
	agarradera.RotZ= -80
	End If

	If Agarra= 23 and LAMM01.State=2 Then
	agarradera.RotZ= -70
	End If

	If Agarra= 24 and LAMM01.State=2 Then
	agarradera.RotZ= -60
	End If

	If Agarra= 25 and LAMM01.State=2 Then
	agarradera.RotZ= -50
	End If

	If Agarra= 26 and LAMM01.State=2 Then
	agarradera.RotZ= -40
	End If

	If Agarra= 27 and LAMM01.State=2 Then
	agarradera.RotZ= -30
	End If

	If Agarra= 28 and LAMM01.State=2 Then
	agarradera.RotZ= -20
	End If

	If Agarra= 29 and LAMM01.State=2 Then
	agarradera.RotZ= -10
	End If

	If Agarra= 30 and LAMM01.State=2 Then
	agarradera.RotZ= 0
	LAMM01.State=1
	AddFdespertar (-10)
	CheckDespertar()
	End If
End Sub

Sub CheckAgarrM1()
	If Agarra= 1 and LAMM02.State=2 Then
	agarradera1.RotZ= -290
	PlaySound "agarra"
	End If

	If Agarra= 2 and LAMM02.State=2 Then
	agarradera1.RotZ= -280
	End If

	If Agarra= 3 and LAMM02.State=2 Then
	agarradera1.RotZ= -270
	End If

	If Agarra= 4 and LAMM02.State=2 Then
	agarradera1.RotZ= -260
	agarradera1.Visible=true
	End If

	If Agarra= 5 and LAMM02.State=2 Then
	agarradera1.RotZ= -250
	End If

	If Agarra= 6 and LAMM02.State=2 Then
	agarradera1.RotZ= -240
	End If

	If Agarra= 7 and LAMM02.State=2 Then
	agarradera1.RotZ= -230
	End If

	If Agarra= 8 and LAMM02.State=2 Then
	agarradera1.RotZ= -220
	End If

	If Agarra= 9 and LAMM02.State=2 Then
	agarradera1.RotZ= -210
	End If

	If Agarra= 10 and LAMM02.State=2 Then
	agarradera1.RotZ= -200
	End If

	If Agarra= 11 and LAMM02.State=2 Then
	agarradera1.RotZ= -190
	End If

	If Agarra= 12 and LAMM02.State=2 Then
	agarradera1.RotZ= -180
	End If

	If Agarra= 13 and LAMM02.State=2 Then
	agarradera1.RotZ= -170
	End If

	If Agarra= 14 and LAMM02.State=2 Then
	agarradera1.RotZ= -160
	End If

	If Agarra= 15 and LAMM02.State=2 Then
	agarradera1.RotZ= -150
	End If

	If Agarra= 16 and LAMM02.State=2 Then
	agarradera1.RotZ= -140
	End If

	If Agarra= 17 and LAMM02.State=2 Then
	agarradera1.RotZ= -130
	End If

	If Agarra= 18 and LAMM02.State=2 Then
	agarradera1.RotZ= -120
	End If

	If Agarra= 19 and LAMM02.State=2 Then
	agarradera1.RotZ= -110
	End If

	If Agarra= 20 and LAMM02.State=2 Then
	agarradera1.RotZ= -100
	End If

	If Agarra= 21 and LAMM02.State=2 Then
	agarradera1.RotZ= -90
	End If

	If Agarra= 22 and LAMM02.State=2 Then
	agarradera1.RotZ= -80
	End If

	If Agarra= 23 and LAMM02.State=2 Then
	agarradera1.RotZ= -70
	End If

	If Agarra= 24 and LAMM02.State=2 Then
	agarradera1.RotZ= -60
	End If

	If Agarra= 25 and LAMM02.State=2 Then
	agarradera1.RotZ= -50
	End If

	If Agarra= 26 and LAMM02.State=2 Then
	agarradera1.RotZ= -40
	End If

	If Agarra= 27 and LAMM02.State=2 Then
	agarradera1.RotZ= -30
	End If

	If Agarra= 28 and LAMM02.State=2 Then
	agarradera1.RotZ= -20
	End If

	If Agarra= 29 and LAMM02.State=2 Then
	agarradera1.RotZ= -10
	End If

	If Agarra= 30 and LAMM02.State=2 Then
	agarradera1.RotZ= 0
	LAMM01.State=1
	AddFdespertar (-10)
	CheckDespertar()
	End If
End Sub

Sub CheckAgarrM2()
	If Agarra= 1 and LAMM03.State=2 Then
	agarradera2.RotZ= -290
	PlaySound "agarra"
	End If

	If Agarra= 2 and LAMM03.State=2 Then
	agarradera2.RotZ= -280
	End If

	If Agarra= 3 and LAMM03.State=2 Then
	agarradera2.RotZ= -270
	End If

	If Agarra= 4 and LAMM03.State=2 Then
	agarradera2.RotZ= -260
	agarradera2.Visible=true
	End If

	If Agarra= 5 and LAMM03.State=2 Then
	agarradera2.RotZ= -250
	End If

	If Agarra= 6 and LAMM03.State=2 Then
	agarradera2.RotZ= -240
	End If

	If Agarra= 7 and LAMM03.State=2 Then
	agarradera2.RotZ= -230
	End If

	If Agarra= 8 and LAMM03.State=2 Then
	agarradera2.RotZ= -220
	End If

	If Agarra= 9 and LAMM03.State=2 Then
	agarradera2.RotZ= -210
	End If

	If Agarra= 10 and LAMM03.State=2 Then
	agarradera2.RotZ= -200
	End If

	If Agarra= 11 and LAMM03.State=2 Then
	agarradera2.RotZ= -190
	End If

	If Agarra= 12 and LAMM03.State=2 Then
	agarradera2.RotZ= -180
	End If

	If Agarra= 13 and LAMM03.State=2 Then
	agarradera2.RotZ= -170
	End If

	If Agarra= 14 and LAMM03.State=2 Then
	agarradera2.RotZ= -160
	End If

	If Agarra= 15 and LAMM03.State=2 Then
	agarradera2.RotZ= -150
	End If

	If Agarra= 16 and LAMM03.State=2 Then
	agarradera2.RotZ= -140
	End If

	If Agarra= 17 and LAMM03.State=2 Then
	agarradera2.RotZ= -130
	End If

	If Agarra= 18 and LAMM03.State=2 Then
	agarradera2.RotZ= -120
	End If

	If Agarra= 19 and LAMM03.State=2 Then
	agarradera2.RotZ= -110
	End If

	If Agarra= 20 and LAMM03.State=2 Then
	agarradera2.RotZ= -100
	End If

	If Agarra= 21 and LAMM03.State=2 Then
	agarradera2.RotZ= -90
	End If

	If Agarra= 22 and LAMM03.State=2 Then
	agarradera2.RotZ= -80
	End If

	If Agarra= 23 and LAMM03.State=2 Then
	agarradera2.RotZ= -70
	End If

	If Agarra= 24 and LAMM03.State=2 Then
	agarradera2.RotZ= -60
	End If

	If Agarra= 25 and LAMM03.State=2 Then
	agarradera2.RotZ= -50
	End If

	If Agarra= 26 and LAMM03.State=2 Then
	agarradera2.RotZ= -40
	End If

	If Agarra= 27 and LAMM03.State=2 Then
	agarradera2.RotZ= -30
	End If

	If Agarra= 28 and LAMM03.State=2 Then
	agarradera2.RotZ= -20
	End If

	If Agarra= 29 and LAMM03.State=2 Then
	agarradera2.RotZ= -10
	End If

	If Agarra= 30 and LAMM03.State=2 Then
	agarradera2.RotZ= 0
	LAMM03.State=1
	AddFdespertar (-10)
	CheckDespertar()
	CheckIRA()
	End If
End Sub

Sub CheckAgarrF()
	If Agarra=30 Then
	TimerAAgarr.Enabled=False
	AddAgarra (-Agarra)
	End If
End Sub
'---------Timer Animacion Timon (TimerATimon)-----------------------
Sub Timon01_Hit()
	If LMES01.State=0 Then
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	PlaySound "timon"
	CheckPuntaje()
	End If

	If LMES01.State=2 and LTIM01.State=1 Then
	PlaySound "timon"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LMES01.State=2 and LTIM01.State=2 Then
	PlaySound "timon"
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LMES01.State=2 and LTIM01.State=0 Then
	PlaySound "timon"
	LTIM01.State=2
	LdLTIM01.State=2
	TimerATimon.Enabled=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (35)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Timon.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub TimerATimon_Timer()
	AddTimo (+1)
	CheckTimonM()
	CheckTimonF()
End Sub

Sub CheckTimonM()
	If Timo=1 and LTIM01.State=2 Then
	PlaySound "timon"
	Timon.RotZ= 5
	End If

	If Timo=2 and LTIM01.State=2 Then
	Timon.RotZ= 10
	End If
	
	If Timo=3 and LTIM01.State=2 Then
	Timon.RotZ= 15
	End If

	If Timo=4 and LTIM01.State=2 Then
	Timon.RotZ= 20
	End If

	If Timo=5 and LTIM01.State=2 Then
	Timon.RotZ= 25
	End If

	If Timo=6 and LTIM01.State=2 Then
	Timon.RotZ= 30
	End If

	If Timo=7 and LTIM01.State=2 Then
	Timon.RotZ= 35
	End If
	
	If Timo=8 and LTIM01.State=2 Then
	Timon.RotZ= 40
	End If

	If Timo=9 and LTIM01.State=2 Then
	Timon.RotZ= 45
	End If

	If Timo=10 and LTIM01.State=2 Then
	Timon.RotZ= 50
	End If

	If Timo=11 and LTIM01.State=2 Then
	Timon.RotZ= 55
	End If

	If Timo=12 and LTIM01.State=2 Then
	Timon.RotZ= 60
	End If
	
	If Timo=13 and LTIM01.State=2 Then
	Timon.RotZ= 65
	End If

	If Timo=14 and LTIM01.State=2 Then
	Timon.RotZ= 70
	End If

	If Timo=15 and LTIM01.State=2 Then
	Timon.RotZ= 75
	End If

	If Timo=16 and LTIM01.State=2 Then
	Timon.RotZ= 80
	End If

	If Timo=17 and LTIM01.State=2 Then
	Timon.RotZ= 85
	End If
	
	If Timo=18 and LTIM01.State=2 Then
	Timon.RotZ= 90
	End If

	If Timo=19 and LTIM01.State=2 Then
	Timon.RotZ= 95
	End If

	If Timo=20 and LTIM01.State=2 Then
	Timon.RotZ= 100
	End If

	If Timo=21 and LTIM01.State=2 Then
	Timon.RotZ= 105
	End If

	If Timo=22 and LTIM01.State=2 Then
	Timon.RotZ= 110
	End If
	
	If Timo=23 and LTIM01.State=2 Then
	Timon.RotZ= 115
	End If

	If Timo=24 and LTIM01.State=2 Then
	Timon.RotZ= 120
	End If

	If Timo=25 and LTIM01.State=2 Then
	Timon.RotZ= 125
	End If

	If Timo=26 and LTIM01.State=2 Then
	Timon.RotZ= 130
	End If

	If Timo=27 and LTIM01.State=2 Then
	Timon.RotZ= 135
	End If
	
	If Timo=28 and LTIM01.State=2 Then
	Timon.RotZ= 140
	End If

	If Timo=29 and LTIM01.State=2 Then
	Timon.RotZ= 145
	End If

	If Timo=30 and LTIM01.State=2 Then
	Timon.RotZ= 150
	End If

	If Timo=31 and LTIM01.State=2 Then
	Timon.RotZ= 155
	End If

	If Timo=32 and LTIM01.State=2 Then
	Timon.RotZ= 160
	End If
	
	If Timo=33 and LTIM01.State=2 Then
	Timon.RotZ= 165
	End If

	If Timo=34 and LTIM01.State=2 Then
	Timon.RotZ= 170
	End If

	If Timo=35 and LTIM01.State=2 Then
	Timon.RotZ= 175
	End If

	If Timo=36 and LTIM01.State=2 Then
	Timon.RotZ= 180
	End If

	If Timo=37 and LTIM01.State=2 Then
	Timon.RotZ= 185
	End If
	
	If Timo=38 and LTIM01.State=2 Then
	Timon.RotZ= 190
	End If

	If Timo=39 and LTIM01.State=2 Then
	Timon.RotZ= 195
	End If

	If Timo=40 and LTIM01.State=2 Then
	Timon.RotZ= 200
	End If

	If Timo=41 and LTIM01.State=2 Then
	Timon.RotZ= 205
	End If

	If Timo=42 and LTIM01.State=2 Then
	Timon.RotZ= 210
	End If
	
	If Timo=43 and LTIM01.State=2 Then
	Timon.RotZ= 215
	End If

	If Timo=44 and LTIM01.State=2 Then
	Timon.RotZ= 220
	End If

	If Timo=45 and LTIM01.State=2 Then
	Timon.RotZ= 225
	End If

	If Timo=46 and LTIM01.State=2 Then
	Timon.RotZ= 230
	End If

	If Timo=47 and LTIM01.State=2 Then
	Timon.RotZ= 235
	End If
	
	If Timo=48 and LTIM01.State=2 Then
	Timon.RotZ= 240
	End If

	If Timo=49 and LTIM01.State=2 Then
	Timon.RotZ= 245
	End If

	If Timo=50 and LTIM01.State=2 Then
	Timon.RotZ= 250
	End If

	If Timo=51 and LTIM01.State=2 Then
	Timon.RotZ= 255
	End If

	If Timo=52 and LTIM01.State=2 Then
	Timon.RotZ= 260
	End If
	
	If Timo=53 and LTIM01.State=2 Then
	Timon.RotZ= 265
	End If

	If Timo=54 and LTIM01.State=2 Then
	Timon.RotZ= 270
	End If

	If Timo=55 and LTIM01.State=2 Then
	Timon.RotZ= 275
	End If

	If Timo=56 and LTIM01.State=2 Then
	Timon.RotZ= 280
	End If

	If Timo=57 and LTIM01.State=2 Then
	Timon.RotZ= 285
	End If
	
	If Timo=58 and LTIM01.State=2 Then
	Timon.RotZ= 290
	End If

	If Timo=59 and LTIM01.State=2 Then
	Timon.RotZ= 295
	End If

	If Timo=60 and LTIM01.State=2 Then
	Timon.RotZ= 300
	End If

	If Timo=61 and LTIM01.State=2 Then
	Timon.RotZ= 305
	End If

	If Timo=62 and LTIM01.State=2 Then
	Timon.RotZ= 310
	End If
	
	If Timo=63 and LTIM01.State=2 Then
	Timon.RotZ= 315
	End If

	If Timo=64 and LTIM01.State=2 Then
	Timon.RotZ= 320
	End If

	If Timo=65 and LTIM01.State=2 Then
	Timon.RotZ= 325
	End If

	If Timo=66 and LTIM01.State=2 Then
	Timon.RotZ= 330
	End If

	If Timo=67 and LTIM01.State=2 Then
	Timon.RotZ= 335
	End If
	
	If Timo=68 and LTIM01.State=2 Then
	Timon.RotZ= 340
	End If

	If Timo=69 and LTIM01.State=2 Then
	Timon.RotZ= 345
	End If

	If Timo=70 and LTIM01.State=2 Then
	Timon.RotZ= 350
	End If

	If Timo=71 and LTIM01.State=2 Then
	Timon.RotZ= 355
	End If

	If Timo=72 and LTIM01.State=2 Then
	Timon.RotZ= 0
	LTIM01.State=1
	LdLTIM01.State=1
	End If
End Sub

Sub CheckTimonF()
	If Agarra=72 Then
	TimerATimon.Enabled=False
	AddTimo (-Timo)
	End If
End Sub
'000000000000000 Switchs de Mech 00000000000000000000000000000000000
Sub switch1_Hit()
	If LTIM01.State=0 or LTIM01.State=2 Then
	PlaySound "ruidoasuich"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LTIM01.State=1 and LSWM01.State=1 Then
	PlaySound "ruidoasuich"
	AddVolt (+1)
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckCarga()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "switch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LTIM01.State=1 and LSWM01.State=0 Then
	PlaySound "ruidoasuich"
	AddVolt (+20)
	LSWM01.State=2
	LdLSWM01.State=2
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckMechSw()
	CheckCarga()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "switch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LTIM01.State=1 and LSWM01.State=2 Then
	PlaySound "ruidoasuich"
	AddVolt (+5)
	p1 = p1 + (2500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckCarga()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "switch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub switch2_Hit()
	If LTIM01.State=0 or LTIM01.State=2 Then
	PlaySound "ruidoasuich"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LTIM01.State=1 and LSWM02.State=1 Then
	PlaySound "ruidoasuich"
	AddVolt (+1)
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckCarga()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "switch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LTIM01.State=1 and LSWM02.State=0 Then
	PlaySound "ruidoasuich"
	AddVolt (+20)
	LSWM02.State=2
	LdLSWM02.State=2
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckMechSw()
	CheckCarga()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "switch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LTIM01.State=1 and LSWM02.State=2 Then
	PlaySound "ruidoasuich"
	AddVolt (+5)
	p1 = (p1 +2500)*MultiPts 
	OnScoreboardChanged
	CheckCarga()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "switch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub switch3_Hit()
	If LTIM01.State=0 or LTIM01.State=2 Then
	PlaySound "ruidoasuich"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	'PlaySound
	CheckPuntaje()
	End If

	If LTIM01.State=1 and LSWM03.State=1 Then
	PlaySound "ruidoasuich"
	AddVolt (+1)
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckCarga()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "3rswitch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LTIM01.State=1 and LSWM03.State=0 Then
	PlaySound "ruidoasuich"
	AddVolt (+20)
	LSWM03.State=2
	LdLSWM03.State=2
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckMechSw()
	CheckCarga()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "3rswitch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LTIM01.State=1 and LSWM03.State=2 Then
	PlaySound "ruidoasuich"
	AddVolt (+5)
	p1 = p1 + (2500*MultiPts)
	OnScoreboardChanged
	CheckCarga()
	CheckPuntaje()
		If rendor=0 Then
		AddRendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "3rswitch.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckMechSw()
	If LSWM01.State=2 and LSWM02.State=2 and LSWM03.State=2 Then
	LSWM01.State=1
	LdLSWM01.State=1
	LSWM02.State=1
	LdLSWM02.State=1
	LSWM03.State=1
	LdLSWM03.State=1
	PlaySound "ruidoelectric"
	End If
End Sub

'++++++++++++++++++++ Boton de Carga Disponible ++++++++++++++++++++
Sub CheckCarga()
	If Volt>50 and Volt<100 Then
	LCBT01.State=2
	LdLCBT01.State=2
	End If

	If Volt>99 Then
	LCBT01.State=1
	LdLCBT01.State=1
	End If
End Sub

'(aca mismo poner las condiciones que hacen que cuanto mas
'voltaje dara mas tiempo para activar el Switch tapa, ademas
'Ese tiempo que de tambien dara un valor, ese valor determina
'cuanto gira el medid0r electrico y cando esten los 3
'switches se activa la palanca, segun el valor que hizo
'girar al medidor, Franky seguira muerto, revive o se rostiza

Sub Cargador_Hit()
	If LCBT01.State=0 Then
	PlaySound "cargador"
	p1 = p1 + (700*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
'-------------- 01 Tapa Switch 01 ----------------------------------
	If LCBT01.State=2 and Volt>50 and Volt<76 and LAlarm.State=0 Then
	PlaySound "switch01"
	Tswitch01.Enabled=True
	AddContMED (+2)
	LAlarm.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressG.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCBT01.State=2 and Volt>75 and Volt<100 and LAlarm.State=0 Then
	PlaySound "switch01"
	Tswitch01.Enabled=True
	AddContMED (+3)
	LAlarm.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressG.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	


	If LCBT01.State=1 and Volt>99 and Volt<126 and LAlarm.State=0 Then
	PlaySound "switch01"
	Tswitch01.Enabled=True
	AddContMED (+4)
	LAlarm.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressG.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LCBT01.State=1 and Volt>125 and Volt<151 and LAlarm.State=0 Then
	PlaySound "switch01"
	Tswitch01.Enabled=True
	AddContMED (+5)
	LAlarm.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressG.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LCBT01.State=1 and Volt>150 and LAlarm.State=0 Then
	PlaySound "switch01"
	Tswitch01.Enabled=True
	AddContMED (+6)
	LAlarm.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressG.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
'-------------- 02 Tapa Switch 02 ----------------------------------
	If LCBT01.State=2 and Volt>50 and Volt<76 and LAlarm.State=1 and LAlarm1.State=0 Then
	PlaySound "switch2"
	Tswitch01.Enabled=True
	AddContMED (+2)
	LAlarm1.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressH.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCBT01.State=2 and Volt>75 and Volt<100 and LAlarm.State=1 and LAlarm1.State=0 Then
	PlaySound "switch2"
	Tswitch01.Enabled=True
	AddContMED (+3)
	LAlarm1.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressH.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If LCBT01.State=1 and Volt>99 and Volt<126 and LAlarm.State=1 and LAlarm1.State=0 Then
	PlaySound "switch2"
	Tswitch01.Enabled=True
	AddContMED (+4)
	LAlarm1.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressH.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCBT01.State=1 and Volt>125 and Volt<151 and LAlarm.State=1 and LAlarm1.State=0 Then
	PlaySound "switch2"
	Tswitch01.Enabled=True
	AddContMED (+5)
	LAlarm1.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressH.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCBT01.State=1 and Volt>150 and LAlarm.State=1 and LAlarm1.State=0 Then
	PlaySound "switch2"
	Tswitch01.Enabled=True
	AddContMED (+6)
	LAlarm1.State=2
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressH.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
'-------------- 03 Tapa Switch 03 ----------------------------------
	If LCBT01.State=2 and Volt>50 and Volt<76 and LAlarm.State=1 and LAlarm1.State=1 and LAlarm2.State=0 Then
	PlaySound "switch3"
	Tswitch01.Enabled=True
	AddContMED (+2)
	LAlarm2.State=2
	TimerFlasero.Enabled=true
	CheckSuich3Flash()
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressJ.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LCBT01.State=2 and Volt>75 and Volt<100 and LAlarm.State=1 and LAlarm1.State=1 and LAlarm2.State=0  Then
	PlaySound "switch3"
	Tswitch01.Enabled=True
	AddContMED (+3)
	LAlarm2.State=2
	TimerFlasero.Enabled=true
	CheckSuich3Flash()
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressJ.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If LCBT01.State=1 and Volt>99 and Volt<126 and LAlarm.State=1 and LAlarm1.State=1 and LAlarm2.State=0 Then
	PlaySound "switch3"
	Tswitch01.Enabled=True
	AddContMED (+4)
	LAlarm2.State=2
	TimerFlasero.Enabled=true
	CheckSuich3Flash()
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressJ.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LCBT01.State=1 and Volt>125 and Volt<151 and LAlarm.State=1 and LAlarm1.State=1 and LAlarm2.State=0 Then
	PlaySound "switch3"
	Tswitch01.Enabled=True
	AddContMED (+5)
	LAlarm2.State=2
	TimerFlasero.Enabled=true
	CheckSuich3Flash()
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressJ.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LCBT01.State=1 and Volt>150 and LAlarm.State=1 and LAlarm1.State=1 and LAlarm2.State=0 Then
	PlaySound "switch3"
	Tswitch01.Enabled=True
	AddContMED (+6)
	LAlarm2.State=2
	TimerFlasero.Enabled=true
	CheckSuich3Flash()
	CheckMED()
		If rendor=0 Then
		Addrendor (18)
		Trendor.enabled=True
		DMD_DisplaySceneEx "pressJ.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1800, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub Tswitch01_Timer()
	AddContMED (-1)
	CheckMED()
End Sub

Sub CheckMED()
	If ContMED=0 Then
	Tswitch01.Enabled=False
	End If
End Sub

'--------------------- Animacion Switch Tapa ----------------------------------
Sub TimerASuich_Timer()
	AddSuich (+1)
	CheckTapaSuichM()
	CheckTapaSuichF()
End Sub

Sub CheckTapaSuichM()
'--------Swich Tapa 01 ----------------
	If Suich=1 and LAlarm.State=2 Then
	PSwitch1.RotX=85
	End If

	If Suich=2 and LAlarm.State=2 Then
	PSwitch1.RotX=80
	End If

	If Suich=3 and LAlarm.State=2 Then
	PSwitch1.RotX=75
	End If
	
	If Suich=4 and LAlarm.State=2 Then
	PSwitch1.RotX=70
	End If

	If Suich=5 and LAlarm.State=2 Then
	PSwitch1.RotX=65
	End If

	If Suich=6 and LAlarm.State=2 Then
	PSwitch1.RotX=60
	End If

	If Suich=7 and LAlarm.State=2 Then
	PSwitch1.RotX=55
	End If

	If Suich=8 and LAlarm.State=2 Then
	PSwitch1.RotX=50
	End If

	If Suich=9 and LAlarm.State=2 Then
	PSwitch1.RotX=45
	End If
	
	If Suich=10 and LAlarm.State=2 Then
	PSwitch1.RotX=40
	End If

	If Suich=11 and LAlarm.State=2 Then
	PSwitch1.RotX=35
	End If

	If Suich=12 and LAlarm.State=2 Then
	PSwitch1.RotX=30
	End If

	If Suich=13 and LAlarm.State=2 Then
	PSwitch1.RotX=25
	End If
	
	If Suich=14 and LAlarm.State=2 Then
	PSwitch1.RotX=20
	End If

	If Suich=15 and LAlarm.State=2 Then
	PSwitch1.RotX=15
	End If

	If Suich=16 and LAlarm.State=2 Then
	PSwitch1.RotX=10
	End If

	If Suich=17 and LAlarm.State=2 Then
	PSwitch1.RotX=05
	End If

	If Suich=18 and LAlarm.State=2 Then
	PSwitch1.RotX=0
	End If

	If Suich=19 and LAlarm.State=2 Then
	PSwitch1.RotX=-5
	End If
	
	If Suich=20 and LAlarm.State=2 Then
	PSwitch1.RotX=-10
	End If

	If Suich=21 and LAlarm.State=2 Then
	PSwitch1.RotX=-15
	End If

	If Suich=22 and LAlarm.State=2 Then
	PSwitch1.RotX=-20
	End If

	If Suich=23 and LAlarm.State=2 Then
	PSwitch1.RotX=-25
	End If
	
	If Suich=24 and LAlarm.State=2 Then
	PSwitch1.RotX=-30
	End If

	If Suich=25 and LAlarm.State=2 Then
	PSwitch1.RotX=-35
	End If

	If Suich=26 and LAlarm.State=2 Then
	PSwitch1.RotX=-40
	End If

	If Suich=27 and LAlarm.State=2 Then
	PSwitch1.RotX=-45
	End If

	If Suich=28 and LAlarm.State=2 Then
	PSwitch1.RotX=-50
	End If

	If Suich=29 and LAlarm.State=2 Then
	PSwitch1.RotX=-55
	End If
	
	If Suich=30 and LAlarm.State=2 Then
	PSwitch1.RotX=-60
	End If

	If Suich=31 and LAlarm.State=2 Then
	PSwitch1.RotX=-65
	End If

	If Suich=32 and LAlarm.State=2 Then
	PSwitch1.RotX=-70
	End If

	If Suich=33 and LAlarm.State=2 Then
	PSwitch1.RotX=-75
	End If
	
	If Suich=34 and LAlarm.State=2 Then
	PSwitch1.RotX=-80
	End If

	If Suich=35 and LAlarm.State=2 Then
	PSwitch1.RotX=-85
	PlaySound "ruidoasuich"
	End If

	If Suich=36 and LAlarm.State=2 Then
	PSwitch1.RotX=-90
	LAlarm.State=1
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	CheckAlarmMED()
	End If

'--------Swich Tapa 02 ----------------
	If Suich=1 and LAlarm1.State=2 Then
	PSwitch2.RotX=85
	End If

	If Suich=2 and LAlarm1.State=2 Then
	PSwitch2.RotX=80
	End If

	If Suich=3 and LAlarm1.State=2 Then
	PSwitch2.RotX=75
	End If
	
	If Suich=4 and LAlarm1.State=2 Then
	PSwitch2.RotX=70
	End If

	If Suich=5 and LAlarm1.State=2 Then
	PSwitch2.RotX=65
	End If

	If Suich=6 and LAlarm1.State=2 Then
	PSwitch2.RotX=60
	End If

	If Suich=7 and LAlarm1.State=2 Then
	PSwitch2.RotX=55
	End If

	If Suich=8 and LAlarm1.State=2 Then
	PSwitch2.RotX=50
	End If

	If Suich=9 and LAlarm1.State=2 Then
	PSwitch2.RotX=45
	End If
	
	If Suich=10 and LAlarm1.State=2 Then
	PSwitch2.RotX=40
	End If

	If Suich=11 and LAlarm1.State=2 Then
	PSwitch2.RotX=35
	End If

	If Suich=12 and LAlarm1.State=2 Then
	PSwitch2.RotX=30
	End If

	If Suich=13 and LAlarm1.State=2 Then
	PSwitch2.RotX=25
	End If
	
	If Suich=14 and LAlarm1.State=2 Then
	PSwitch2.RotX=20
	End If

	If Suich=15 and LAlarm1.State=2 Then
	PSwitch2.RotX=15
	End If

	If Suich=16 and LAlarm1.State=2 Then
	PSwitch2.RotX=10
	End If

	If Suich=17 and LAlarm1.State=2 Then
	PSwitch2.RotX=05
	End If

	If Suich=18 and LAlarm1.State=2 Then
	PSwitch2.RotX=0
	End If

	If Suich=19 and LAlarm1.State=2 Then
	PSwitch2.RotX=-5
	End If
	
	If Suich=20 and LAlarm1.State=2 Then
	PSwitch2.RotX=-10
	End If

	If Suich=21 and LAlarm1.State=2 Then
	PSwitch2.RotX=-15
	End If

	If Suich=22 and LAlarm1.State=2 Then
	PSwitch2.RotX=-20
	End If

	If Suich=23 and LAlarm1.State=2 Then
	PSwitch2.RotX=-25
	End If
	
	If Suich=24 and LAlarm1.State=2 Then
	PSwitch2.RotX=-30
	End If

	If Suich=25 and LAlarm1.State=2 Then
	PSwitch2.RotX=-35
	End If

	If Suich=26 and LAlarm1.State=2 Then
	PSwitch2.RotX=-40
	End If

	If Suich=27 and LAlarm1.State=2 Then
	PSwitch2.RotX=-45
	End If

	If Suich=28 and LAlarm1.State=2 Then
	PSwitch2.RotX=-50
	End If

	If Suich=29 and LAlarm1.State=2 Then
	PSwitch2.RotX=-55
	End If
	
	If Suich=30 and LAlarm1.State=2 Then
	PSwitch2.RotX=-60
	End If

	If Suich=31 and LAlarm1.State=2 Then
	PSwitch2.RotX=-65
	End If

	If Suich=32 and LAlarm1.State=2 Then
	PSwitch2.RotX=-70
	End If

	If Suich=33 and LAlarm1.State=2 Then
	PSwitch2.RotX=-75
	End If
	
	If Suich=34 and LAlarm1.State=2 Then
	PSwitch2.RotX=-80
	End If

	If Suich=35 and LAlarm1.State=2 Then
	PSwitch2.RotX=-85
	PlaySound "ruidoasuich"
	End If

	If Suich=36 and LAlarm1.State=2 Then
	PSwitch2.RotX=-90
	LAlarm1.State=1
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	CheckAlarmMED()
	End If


'--------Swich Tapa 03 ----------------
	If Suich=1 and LAlarm2.State=2 Then
	PSwitch3.RotX=85
	End If

	If Suich=2 and LAlarm2.State=2 Then
	PSwitch3.RotX=80
	End If

	If Suich=3 and LAlarm2.State=2 Then
	PSwitch3.RotX=75
	End If
	
	If Suich=4 and LAlarm2.State=2 Then
	PSwitch3.RotX=70
	End If

	If Suich=5 and LAlarm2.State=2 Then
	PSwitch3.RotX=65
	End If

	If Suich=6 and LAlarm2.State=2 Then
	PSwitch3.RotX=60
	End If

	If Suich=7 and LAlarm2.State=2 Then
	PSwitch3.RotX=55
	End If

	If Suich=8 and LAlarm2.State=2 Then
	PSwitch3.RotX=50
	End If

	If Suich=9 and LAlarm2.State=2 Then
	PSwitch3.RotX=45
	End If
	
	If Suich=10 and LAlarm2.State=2 Then
	PSwitch3.RotX=40
	End If

	If Suich=11 and LAlarm2.State=2 Then
	PSwitch3.RotX=35
	End If

	If Suich=12 and LAlarm2.State=2 Then
	PSwitch3.RotX=30
	End If

	If Suich=13 and LAlarm2.State=2 Then
	PSwitch3.RotX=25
	End If
	
	If Suich=14 and LAlarm2.State=2 Then
	PSwitch3.RotX=20
	End If

	If Suich=15 and LAlarm2.State=2 Then
	PSwitch3.RotX=15
	End If

	If Suich=16 and LAlarm2.State=2 Then
	PSwitch3.RotX=10
	End If

	If Suich=17 and LAlarm2.State=2 Then
	PSwitch3.RotX=05
	End If

	If Suich=18 and LAlarm2.State=2 Then
	PSwitch3.RotX=0
	End If

	If Suich=19 and LAlarm2.State=2 Then
	PSwitch3.RotX=-5
	End If
	
	If Suich=20 and LAlarm2.State=2 Then
	PSwitch3.RotX=-10
	End If

	If Suich=21 and LAlarm2.State=2 Then
	PSwitch3.RotX=-15
	End If

	If Suich=22 and LAlarm2.State=2 Then
	PSwitch3.RotX=-20
	End If

	If Suich=23 and LAlarm2.State=2 Then
	PSwitch3.RotX=-25
	End If
	
	If Suich=24 and LAlarm2.State=2 Then
	PSwitch3.RotX=-30
	End If

	If Suich=25 and LAlarm2.State=2 Then
	PSwitch3.RotX=-35
	End If

	If Suich=26 and LAlarm2.State=2 Then
	PSwitch3.RotX=-40
	End If

	If Suich=27 and LAlarm2.State=2 Then
	PSwitch3.RotX=-45
	End If

	If Suich=28 and LAlarm2.State=2 Then
	PSwitch3.RotX=-50
	End If

	If Suich=29 and LAlarm2.State=2 Then
	PSwitch3.RotX=-55
	End If
	
	If Suich=30 and LAlarm2.State=2 Then
	PSwitch3.RotX=-60
	End If

	If Suich=31 and LAlarm2.State=2 Then
	PSwitch3.RotX=-65
	End If

	If Suich=32 and LAlarm2.State=2 Then
	PSwitch3.RotX=-70
	End If

	If Suich=33 and LAlarm2.State=2 Then
	PSwitch3.RotX=-75
	End If
	
	If Suich=34 and LAlarm2.State=2 Then
	PSwitch3.RotX=-80
	End If

	If Suich=35 and LAlarm2.State=2 Then
	PSwitch3.RotX=-85
	PlaySound "ruidoasuich"
	End If

	If Suich=36 and LAlarm2.State=2 Then
	PSwitch3.RotX=-90
	LAlarm2.State=1
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	CheckAlarmMED()
	End If
End Sub

Sub CheckAlarmMED()
	If LAlarm.State=1 and LAlarm1.State=1 and LAlarm2.State=1 Then
	LPAL01.State=2
	LdLPAL01.State=2
	End If
End Sub

Sub CheckTapaSuichF()
	If Suich=36 Then
	AddSuich (-Suich)
	End If
End Sub

'------------  Palanca ---------------------------------------------
Sub TPalanca_Hit()
	If LPAL01.State=0 Then
	PlaySound "Ruidosuich"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LPAL01.State=2 Then
	PlaySound "Ruidosuich"
	TimerAPalank.Enabled=True
	TimerFlasero.enabled=true
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	checkpalancaflash()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "palank.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub
	

Sub TimerAPalank_Timer()
	AddPlanka (+1)
	CheckPalancaM()
	CheckPalancaF()
End Sub

Sub CheckPalancaM()
	If Planka=0 and LPAL01.State=1 Then
	Palank.RotZ=50
	End If

	If Planka=1 and LPAL01.State=2 Then
	Palank.RotZ=45
	End If

	If Planka=2 and LPAL01.State=2 Then
	Palank.RotZ=40
	End If

	If Planka=3 and LPAL01.State=2 Then
	Palank.RotZ=35
	End If
	
	If Planka=4 and LPAL01.State=2 Then
	Palank.RotZ=30
	End If

	If Planka=5 and LPAL01.State=2 Then
	Palank.RotZ=25
	End If

	If Planka=6 and LPAL01.State=2 Then
	Palank.RotZ=20
	End If

	If Planka=7 and LPAL01.State=2 Then
	Palank.RotZ=15
	End If
	
	If Planka=8 and LPAL01.State=2 Then
	Palank.RotZ=10
	End If

	If Planka=9 and LPAL01.State=2 Then
	Palank.RotZ=5
	End If
	
	If Planka=10 and LPAL01.State=2 Then
	Palank.RotZ=0
	LPAL01.State=1
	End If
End Sub

Sub CheckPalancaF()
	If Planka=10 and LPAL01.State=1 and Volt<96 Then'no llega a revivir y de debe recargar aparato
	PlaySound "norevive"
	AddPlanka (-Planka)
	p1 = p1 + (10000*MultiPts)
	CheckPuntaje()
	OnScoreboardChanged
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	TimerFlasero.enabled=false
	checkpalancaflash()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "ElekAFrank.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Planka=10 and LPAL01.State=1 and Volt<95 and Volt>269 Then 'no llega a revivir y de debe recargar aparato
	PlaySound "norevive"
	AddPlanka (-Planka)
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	timerflasero.enabled=false
	checkpalancaflash()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "ElekAFrank.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Planka=10 and LPAL01.State=1 and Volt<268 and Volt>329 Then 'its alive!
	PlaySound "itsalive"
	AddPlanka (-Planka)
	p1 = p1 + (70000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	LCZF01.State=2
	LdLCZF01.State=2
	AddMultiPts (+1)
	CheckMultiplicaX6()
	TimerFlasero.enabled=false
	checkpalancaflash()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "ElekAFrank.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	'------------- Franky muere
	If Planka=10 and LPAL01.State=1 and Volt>328 Then 'mucho voltaje Mata Franky se resetea mesa y banco cerebros
	PlaySound "Fmuere"
	AddPlanka (-Planka)
	p1 = p1 - (50000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	LCZF01.State=0
	LdLCZF01.State=0
	LMES01.State=0
	FCabeza.Visible=False
	FQuijada.Visible=False
	FTorso.Visible=False
	FPelvis.Visible=False
	FBrazoD.Visible=False
	FBrazoI.Visible=False
	FAntBrazoD.Visible=False
	FAntBrazoI.Visible=False
	FManoD.Visible=False
	FManoI.Visible=False
	FPiernaD.Visible=False
	FPiernaI.Visible=False
	FAntPiernaD.Visible=False
	FAntPiernaI.Visible=False
	FPieD.Visible=False
	FPieI.Visible=False
	jarroCereb3.Visible=False
	LECF01.State=0
	LECF02.State=0
	LECF03.State=0
	LECF04.State=0
	LECF05.State=0
	LECF06.State=0
	LECF07.State=0
	LECF08.State=0
	LECF09.State=0
	LECF10.State=0
	LECF11.State=0
	LECF12.State=0
	LECF13.State=0
	LECF14.State=0
	LECF15.State=0
	LECF16.State=0
	LCHD01.State=0
	LdLCHD01.State=0
	LCAN01.State=0
	LdLCAN01.State=0
	LCCO01.State=0
	LdLCCO01.State=0
	Tcereb1.IsDropped=False
	Tcereb2.IsDropped=False
	Tcereb3.IsDropped=False
	LRLPG01.State=0
	LRLPG02.State=0
	LRLPG03.State=0

	LRPI01.State=0
	LdLRPI01.State=0
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	LMulX2.State=0
	LdLMulX2.State=0
	LMulX3.State=0
	LdLMulX3.State=0
	LMulX4.State=0
	LdLMulX4.State=0
	LMulX5.State=0
	LdLMulX5.State=0
	LMulX6.State=0
	LdLMulX6.State=0
	AddPartes (-Partes)
	AddPartesM (-PartesM)
	TimerFlasero.enabled=false
	checkpalancaflash()
		If rendor=0 Then
		Addrendor (35)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Frankymuerelek.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub
'*********** Contadores Volt y Anim MeCH y Mesa ********************
'---------------- Voltage    --------------------
Sub AddVolt(Vtg) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Volt = Volt + Vtg ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'0000000000000000000000000000000000000000000000000000000000000000000
'---------------- Animacion Timon ----------------------------------
Sub AddTimo(TTimo) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Timo = Timo + TTimo ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'--------------- Animacion Palanka --------------------------------
Sub AddPlanka(PLK) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Planka = Planka + PLK ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'---------------- Animacion agarraderas-----------------------------
Sub AddAgarra(Deras) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Agarra = Agarra + Deras ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'---------------- Animacion Switchs Tapa ---------------------------
Sub AddSuich(Swt) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Suich = Suich + Swt ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'---------------- Tiempo de Switchs --------------------------------
Sub AddTswitch(Tsui) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Tswitch = Tswitch + Tsui ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'-------------------------------------------------------------------
Sub AddContMED(ConMe) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    ContMED = ContMED + ConMe ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'----------------- Para Mision 02: Esposa Franky -------------------------
Sub AddEsposaF(Fposa) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    EsposaF = EsposaF + Fposa ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'------------------------ Dardos  ----------------------------------
Sub AddPAzar(azar) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    PAzar = PAzar + azar ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

'********************************************************************
'************ Mision 02 Esposa Franky *******************************
Sub CheckM2Esposa()
	If EsposaF=4 Then
	PlaySound "esposaf01"
	LCRE01.State=2
	Lapida.TransY=-120
	Lapida.Collidable=False
	End If

	If EsposaF=8 Then
	PlaySound "esposaf02"
	AddIra (-25)
	AddTurba (-1)
	LCRE01.State=0
	Lapida.TransY=0
	Lapida.Collidable=True
	p1 = p1 + (25000*MultiPts)
	OnScoreboardChanged
	CheckIRA()
	CheckTurr()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (80)
		Trendor.enabled=True
		DMD_DisplaySceneEx "esposaFrank.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 8000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub
	
'********************************************************************	
'************** Mision Dardos  (mision 04 ) ************************************
Sub KickerOficial_Hit()
	If LLCH01.State=0 Then
	PlaySound "kickerkik"
	KickerOficial.Kick 165, 7
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LLCH01.State=1 Then
	PlaySound "DardEmp"
	LMDA.State=2
	LdLMDA.State=2
	TimerAzar.enabled=True
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "misiondardos.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'----------------- Tira Dardos  -----------------------------------
Sub CheckDardo()
	If AZAR=0 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=1
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=1 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=1
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=2 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=1
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=3 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=1
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=4 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=1
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=5 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=1
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=6 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=1
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=7 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=1
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=8 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=1
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=9 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=1
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=10 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=1
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=11 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=1
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=12 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=1
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=13 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=1
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=14 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=1
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=15 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=1
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=16 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=1
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=17 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=1
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=18 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=1
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=19 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=1
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=20 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=1
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=21 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=1
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=22 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=1
	LTDD24.State=0
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=23 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=1
	LTDD25.State=0
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If AZAR=24 and LMDA.State=2 Then
	playSound "dardazo"
	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=1
	CheckResu()
		If rendor=0 Then
		Addrendor (4)
		Trendor.enabled=True
		DMD_DisplaySceneEx "tiradardos.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 400, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'---------------- Reset Dardos
Sub TimerAzar_Timer()
	TimerAzar.enabled=False
	AddPAzar (-PAzar)
	LMDA.State=0
	LdLMDA.State=0

	LTDD01.State=0
	LTDD02.State=0
	LTDD03.State=0
	LTDD04.State=0
	LTDD05.State=0
	LTDD06.State=0
	LTDD07.State=0
	LTDD08.State=0
	LTDD09.State=0
	LTDD10.State=0
	LTDD11.State=0
	LTDD12.State=0
	LTDD13.State=0
	LTDD14.State=0
	LTDD15.State=0
	LTDD16.State=0
	LTDD17.State=0
	LTDD18.State=0
	LTDD19.State=0
	LTDD20.State=0
	LTDD21.State=0
	LTDD22.State=0
	LTDD23.State=0
	LTDD24.State=0
	LTDD25.State=0
	
	KickerOficial.Kick 165, 8
End Sub

Sub CheckResu() 'donde quedo el dardo final, que puntos da
	If LTDD01.State=1 and LMDA.State=0 Then
	'playSound
	p1 = p1 + (18000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo01.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD02.State=1 and LMDA.State=0 Then
	p1 = p1 + (12000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo02.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD03.State=1 and LMDA.State=0 Then
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo03.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD04.State=1 and LMDA.State=0 Then
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo04.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD05.State=1 and LMDA.State=0 Then
	p1 = p1 + (8000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo05.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD06.State=1 and LMDA.State=0 Then
	p1 = p1 + (14000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo06.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD07.State=1 and LMDA.State=0 Then
	p1 = p1 + (9000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo07.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD08.State=1 and LMDA.State=0 Then
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo08.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD09.State=1 and LMDA.State=0 Then
	p1 = p1 + (4000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
			If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo09.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD10.State=1 and LMDA.State=0 Then
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo10.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD11.State=1 and LMDA.State=0 Then
	p1 = p1 + (11000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo11.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD12.State=1 and LMDA.State=0 Then
	p1 = p1 + (11000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo12.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD13.State=1 and LMDA.State=0 Then
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo13.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD14.State=1 and LMDA.State=0 Then
	p1 = p1 + (6000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo14.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD15.State=1 and LMDA.State=0 Then
	p1 = p1 + (6000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo15.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD16.State=1 and LMDA.State=0 Then
	p1 = p1 + (8000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo16.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD17.State=1 and LMDA.State=0 Then
	p1 = p1 + (16000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo17.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD18.State=1 and LMDA.State=0 Then
	p1 = p1 + (3000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo18.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD19.State=1 and LMDA.State=0 Then
	p1 = p1 + (19000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo19.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD20.State=1 and LMDA.State=0 Then
	p1 = p1 + (15000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo20.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD21.State=1 and LMDA.State=0 Then
	p1 = p1 + (32000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo21.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD22.State=1 and LMDA.State=0 Then
	p1 = p1 + (7000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo22.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD23.State=1 and LMDA.State=0 Then
	p1 = p1 + (3000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo23.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD24.State=1 and LMDA.State=0 Then
	p1 = p1 + (17000*MultiPts) 
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo24.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LTDD25.State=1 and LMDA.State=0 Then
	p1 = p1 + (4000*MultiPts)
	OnScoreboardChanged
	'playSound
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "dardo25.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'******************************************************************
'*********** Mision Turba IRACUNDA ( mision 05 ) ******************
Sub TPueblo01_Hit()
	If LDespierto.State=0 Then
	PlaySound "pueblo01"
	p1 = p1 + (600*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LIDP01.State=0 Then
	PlaySound "pueblo01"
	LIDP01.State=1
	LdLIDP01.State=1
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckChisme()
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LIDP01.State=1 Then
	PlaySound "pueblo01"
	LIDP01.State=0
	LdLIDP01.State=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckChisme()
	CheckPuntaje()
	End If

	If LIDP01.State=2 Then
	PlaySound "pueblo01"
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	AddTurba (+1)
	CheckTurbaAV()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "turbapensando.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub TPueblo02_Hit()
	If LDespierto.State=0 Then
	PlaySound "pueblo02"
	p1 = p1 + (600*MultiPts) 
	OnScoreboardChanged
	'PlaySound
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LIDP02.State=0 Then
	PlaySound "pueblo02"
	LIDP02.State=1
	LdLIDP02.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckChisme()
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LIDP02.State=1 Then
	PlaySound "pueblo02"
	LIDP02.State=0
	LdLIDP02.State=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckChisme()
	CheckPuntaje()
	End If

	If LIDP02.State=2 Then
	PlaySound "pueblo02"
	AddTurba (+1)
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	CheckTurbaAV()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "turbapensando.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub TPueblo03_Hit()
	If LDespierto.State=0 Then
	PlaySound "pueblo03"
	p1 = p1 + (600*MultiPts) 
	OnScoreboardChanged
	'PlaySound
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LIDP03.State=0 Then
	PlaySound "pueblo03"
	LIDP03.State=1
	LdLIDP03.State=1
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckChisme()
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LIDP03.State=1 Then
	PlaySound "pueblo03"
	LIDP03.State=0
	LdLIDP03.State=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckChisme()
	CheckPuntaje()
	End If

	If LIDP03.State=2 Then
	PlaySound "pueblo03"
	AddTurba (+1)
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	CheckTurbaAV()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (15)
		Trendor.enabled=True
		DMD_DisplaySceneEx "turbapensando.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckChisme()
	If LIDP01.State=1 and LIDP02.State=1 and LIDP03.State=1 Then
	PlaySound "alinchar"
	LMTI01.State=2
	LdLMTI01.State=2
	LIDP01.State=2
	LdLIDP01.State=2
	LIDP02.State=2
	LdLIDP02.State=2
	LIDP03.State=2
	LdLIDP03.State=2
	LTQA01.State=2
	LdLTQA01.State=2
	AddTurba (+1)
		If rendor=0 Then	
		Addrendor (26)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Turba.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2600, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckTurbaAV()
	
	If Turba=1 Then
	PlaySound "tavanza"
	LIDP01.State=0
	LdLIDP01.State=0
	LIDP02.State=0
	LdLIDP02.State=0
	LIDP03.State=0
	LdLIDP03.State=0
	LTQA02.State=0
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	End If

	If Turba>1 and Turba<6 Then
	PlaySound "tavanza"
	TimerFlasero.enabled=True
	LTQA01.State=1
	LTQA02.State=0
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=0
	LdLTQA03.State=0
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>5 and Turba<11 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=2
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=2
	LdLTQA03.State=0
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>10 and Turba<16 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=0
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts)
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>15 and Turba<21 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=2
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=2
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>20 and Turba<26 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>25 and Turba<31 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=2
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=2
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts)
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>30 and Turba<36 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=1
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=1
	LdLTQA05.State=0
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>35 and Turba<41 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=1
	LTQA05.State=2
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=1
	LdLTQA05.State=2
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts)
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>40 and Turba<46 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=1
	LTQA05.State=1
	LTQA06.State=0
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=1
	LdLTQA05.State=1
	LdLTQA06.State=0
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>45 and Turba<51 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=1
	LTQA05.State=1
	LTQA06.State=2
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=1
	LdLTQA05.State=1
	LdLTQA06.State=2
	p1 = p1 - (1000*MultiPts)
	OnScoreboardChanged
	CheckTurr()
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TurbaAva.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Turba>50 Then
	PlaySound "tavanza"
	LTQA01.State=1
	LTQA02.State=1
	LTQA03.State=1
	LTQA04.State=1
	LTQA05.State=1
	LTQA06.State=1
	LdLTQA01.State=1
	LdLTQA02.State=1
	LdLTQA03.State=1
	LdLTQA04.State=1
	LdLTQA05.State=1
	LdLTQA06.State=1
	'turba mata a franky*******************
	TimerFlasero.enabled=False
	CheckTurr()
	End If
End Sub

Sub CheckTurr()

	If LTQA01.State=1 and LTQA02.State=1 and LTQA03.State=1 and LTQA04.State=1 and LTQA05.State=1 and LTQA06.State=1 and LIDP01.State=2 and LdLIDP01.State=2 and LIDP02.State=2 and LdLIDP02.State=2 and LIDP03.State=2 and LdLIDP03.State=2 Then
	AddTurba (-Turba)
	TimerFlasero.enabled=False
	LMTI01.State=0
	LdLMTI01.State=0
	LIDP01.State=0
	LdLIDP01.State=0
	LIDP02.State=0
	LdLIDP02.State=0
	LIDP03.State=0
	LdLIDP03.State=0
	LTQA01.State=0
	LTQA02.State=0
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=0
	LdLTQA02.State=0
	LdLTQA03.State=0
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	End If

	If LTQA01.State=1 and LTQA02.State=1 and LTQA03.State=1 and LTQA04.State=1 and LTQA05.State=1 and LTQA06.State=1 Then
	PlaySound "Fmuere"
	AddTurba (-Turba)
	LMTI01.State=0
	LdLMTI01.State=0
	LIDP01.State=0
	LdLIDP01.State=0
	LIDP02.State=0
	LdLIDP02.State=0
	LIDP03.State=0
	LdLIDP03.State=0
	LTQA01.State=0
	LTQA02.State=0
	LTQA03.State=0
	LTQA04.State=0
	LTQA05.State=0
	LTQA06.State=0
	LdLTQA01.State=0
	LdLTQA02.State=0
	LdLTQA03.State=0
	LdLTQA04.State=0
	LdLTQA05.State=0
	LdLTQA06.State=0
	AddPlanka (-Planka)
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	LCZF01.State=0
	LdLCZF01.State=0
	LMES01.State=0
	FCabeza.Visible=False
	FQuijada.Visible=False
	FTorso.Visible=False
	FPelvis.Visible=False
	FBrazoD.Visible=False
	FBrazoI.Visible=False
	FAntBrazoD.Visible=False
	FAntBrazoI.Visible=False
	FManoD.Visible=False
	FManoI.Visible=False
	FPiernaD.Visible=False
	FPiernaI.Visible=False
	FAntPiernaD.Visible=False
	FAntPiernaI.Visible=False
	FPieD.Visible=False
	FPieI.Visible=False
	jarroCereb3.Visible=False
	LECF01.State=0
	LECF02.State=0
	LECF03.State=0
	LECF04.State=0
	LECF05.State=0
	LECF06.State=0
	LECF07.State=0
	LECF08.State=0
	LECF09.State=0
	LECF10.State=0
	LECF11.State=0
	LECF12.State=0
	LECF13.State=0
	LECF14.State=0
	LECF15.State=0
	LECF16.State=0
	LCHD01.State=0
	LdLCHD01.State=0
	LCAN01.State=0
	LdLCAN01.State=0
	LCCO01.State=0
	LdLCCO01.State=0
	Tcereb1.IsDropped=False
	Tcereb2.IsDropped=False
	Tcereb3.IsDropped=False
	LRLPG01.State=0
	LRLPG02.State=0
	LRLPG03.State=0

	LRPI01.State=0
	LdLRPI01.State=0
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	LMulX2.State=0
	LdLMulX2.State=0
	LMulX3.State=0
	LdLMulX3.State=0
	LMulX4.State=0
	LdLMulX4.State=0
	LMulX5.State=0
	LdLMulX5.State=0
	LMulX6.State=0
	LdLMulX6.State=0
	AddPartes (-Partes)
	AddPartesM (-PartesM)
	p1 = p1 - (100000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub
'Falta los botones que reducen los puntos de turba








'---------------- conteo de bronca pop ----------------------------
'------------------------ Dardos  ----------------------------------
Sub AddTurba (Tur) 'we also need to Dim Score in the beginning of script. all Variables should go in the beginning of script.
    Turba = Turba + Tur ' This adds your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'-------------------------------------------------------------------
'*******************************************************************


'*******************************************************************
'**************  Minision Rank  (Mision 7)  ************************
Sub TletraR_Hit()
	If LPDR01.State=0 Then
	playsound "Round"
	LPDR01.State=1
	LdLPDR01.State=1
	LFDK02.State=1
	LdLFDK02.State=1
	LFDK06.State=1
	LdLFDK06.State=1
	LFKT02.State=1
	LIGO04.State=1
	LdLIGO04.state=1
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckNombres()
	CheckRank()
	End If

	If LPDR01.State=1 Then
	playsound "Round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LPDR01.State=2 Then
	playsound "Round"
	LPDR01.State=0
	LdLPDR01.State=0
	'PlaySound
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub TletraA_Hit()
	If LPDR02.State=0 Then
	playsound "Round"
	LPDR02.State=1
	LdLPDR02.State=1
	LFKT03.State=1
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckRank()
	CheckPuntaje()
	End If

	If LPDR02.State=1 Then
	playsound "Round"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LPDR02.State=2 Then
	playsound "Round"
	LPDR02.State=0
	LdLPDR02.State=0
	'PlaySound
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub TletraN_Hit()
	If LPDR03.State=0 Then
	playsound "Round"
	LPDR03.State=1
	LdLPDR03.State=1
	LFKT04.State=1
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	CheckNombres()
	CheckRank()
	End If

	If LPDR03.State=1 Then
	playsound "Round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LPDR03.State=2 Then
	LPDR03.State=0
	LdLPDR03.State=0
	playsound "Round"
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub TletraK_Hit()
	If LPDR04.State=0 Then
	playsound "Round"
	LPDR04.State=1
	LdLPDR04.State=1
	LFDK08.State=1
	LdLFDK08.State=1
	LFKT05.State=1
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckRank()
	CheckPuntaje()
	End If

	If LPDR04.State=1 Then
	playsound "Round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LPDR04.State=2 Then
	LPDR04.State=0
	LdLPDR04.State=0
	playsound "Round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub CheckRank()
	If LPDR01.State=0 and LPDR02.State=0 and LPDR03.State=0 and LPDR04.State=0 Then
	Blokeador01.IsDropped=True
	Blokeador02.IsDropped=True
	Blokeador03.IsDropped=True
	BlokCentral.Visible=False
	BlokCentral.Collidable=False
	End If

	If LPDR01.State=1 and LPDR02.State=1 and LPDR03.State=1 and LPDR04.State=1 Then
	LPDR01.State=2
	LdLPDR01.State=2
	LPDR02.State=2
	LdLPDR02.State=2
	LPDR03.State=2
	LdLPDR03.State=2
	LPDR04.State=2
	LdLPDR04.State=2
	LBBD01.State=2
	LdLBBD01.State=2
	LBBC01.State=2
	LdBloCent.State=2
	LBBI01.State=2
	LdLBBI01.State=2
	Blokeador01.IsDropped=False
	Blokeador02.IsDropped=False
	Blokeador03.IsDropped=False
	BlokCentral.Visible=True
	BlokCentral.Collidable=True
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub Blokeador01_Hit()
	PlaySound "vidriocer"
	Blokeador01.IsDropped=True
	Blokeador02.IsDropped=True
	Blokeador03.IsDropped=True
	BlokCentral.Visible=False
	BlokCentral.Collidable=False
	LPDR01.State=0
	LdLPDR01.State=0
	LPDR02.State=0
	LdLPDR02.State=0
	LPDR03.State=0
	LdLPDR03.State=0
	LPDR04.State=0
	LdLPDR04.State=0
	LBBD01.State=0
	LdLBBD01.State=0
	LBBC01.State=0
	LdBloCent.State=0
	LBBI01.State=0
	LdLBBI01.State=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub Blokeador02_Hit()
	PlaySound "vidriocer"
	Blokeador01.IsDropped=True
	Blokeador02.IsDropped=True
	Blokeador03.IsDropped=True
	BlokCentral.Visible=False
	BlokCentral.Collidable=False
	LPDR01.State=0
	LdLPDR01.State=0
	LPDR02.State=0
	LdLPDR02.State=0
	LPDR03.State=0
	LdLPDR03.State=0
	LPDR04.State=0
	LdLPDR04.State=0
	LBBD01.State=0
	LdLBBD01.State=0
	LBBC01.State=0
	LdBloCent.State=0
	LBBI01.State=0
	LdLBBI01.State=0
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub Blokeador03_Hit()
	PlaySound "vidriocer"
	Blokeador01.IsDropped=True
	Blokeador02.IsDropped=True
	Blokeador03.IsDropped=True
	BlokCentral.Visible=False
	BlokCentral.Collidable=False
	LPDR01.State=0
	LdLPDR01.State=0
	LPDR02.State=0
	LdLPDR02.State=0
	LPDR03.State=0
	LdLPDR03.State=0
	LPDR04.State=0
	LdLPDR04.State=0
	LBBD01.State=0
	LdLBBD01.State=0
	LBBC01.State=0
	LdBloCent.State=0
	LdBloCent.State=0
	LBBI01.State=0
	LdLBBI01.State=0
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
	
'*******************************************************************

'*********** Mision del Ciego Ayuda ( mision 09 ) ****************** 
Sub TletraI_Hit()
	If Ldespierto.State=0 and LBLI01.State=0 Then
	PlaySound "Round"
	LBLI01.State=1
	LdLBLI01.State=1
	LFDK07.State=1
	LdLFDK07.State=1
	LFKT11.State=1
	LIGO01.State=1
	LdLIGO01.State=1
	CheckNombres()
	CheckCiego()
	End If

	If Ldespierto.State=0 and LBLI01.State=1 Then
	PlaySound "Round"
	End If

	If Ldespierto.State=1 and LBLI01.State=0 Then
	PlaySound "Round"
	LBLI01.State=2
	LdLBLI01.State=2
	LFDK07.State=1
	LdLFDK07.State=1
	LFKT11.State=1
	LIGO01.State=1
	LdLIGO01.State=1
	CheckNombres()
	CheckCiego()
	End If

	If Ldespierto.State=1 and LBLI01.State=1 Then
	PlaySound "Round"
	LBLI01.State=2
	LdLBLI01.State=2
	'PlaySound
	CheckNombres()
	CheckCiego()
	End If

	If Ldespierto.State=1 and LBLI01.State=2 Then
	PlaySound "Round"
	LBLI01.State=1
	LdLBLI01.State=1
	CheckNombres()
	'PlaySound
	End If
End Sub


Sub TletraG_Hit()
	If Ldespierto.State=0 and LBLG01.State=0 Then
	PlaySound "Round"
	LBLG01.State=1
	LdLBLG01.State=1
	LFDK07.State=1
	LdLFDK07.State=1
	LFKT11.State=1
	LIGO01.State=1
	LdLIGO01.State=1
	p1 = p1 + (500*MultiPts)
	OnScoreboardChanged
	CheckNombres()
	CheckCiego()
	CheckPuntaje()
	End If

	If Ldespierto.State=0 and LBLG01.State=1 Then
	PlaySound "Round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If Ldespierto.State=1 and LBLG01.State=0 Then
	PlaySound "Round"
	LBLG01.State=2
	LdLBLG01.State=2
	LFDK07.State=1
	LdLFDK07.State=1
	LFKT11.State=1
	LIGO01.State=1
	LdLIGO01.State=1
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	CheckCiego()
	CheckPuntaje()
	End If

	If Ldespierto.State=1 and LBLG01.State=1 Then
	PlaySound "Round"
	LBLG01.State=2
	LdLBLG01.State=2
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	'PlaySound
	CheckCiego()
	CheckPuntaje()
	End If

	If Ldespierto.State=1 and LBLG01.State=2 Then
	PlaySound "Round"
	LBLG01.State=1
	LdLBLG01.State=1
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckNombres()
	'PlaySound
	CheckPuntaje()
	End If
End Sub


Sub CheckCiego()
	If LBLI01.State=2 or LBLG01.State=2 Then
	PlaySound "Mciego"
	LMCA01.State=1
	LdLMCA01.State=1
	LESP01.State=2
	LdLESP01.State=2
	End If
End Sub

Sub CheckCiegoEsp()
	If LMCA01.State=2 Then
	TimerCiego.enabled=True
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (20)
		Trendro.enabled=True
		DMD_DisplaySceneEx "Misionciego.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub TimerCiego_Timer()
	TimerCiego.enabled=False
	LBLI01.State=0
	LdLBLI01.State=0
	LBLG01.State=0
	LdLBLG01.State=0
	LMCA01.State=0
	LdLMCA01.State=0
	LESP01.State=0
	LdLESP01.State=0
End Sub

'*******************************************************************
'***************  Minision LOCOMOTORA ( mision 08) ******************
Sub TStacion_Hit()
	PlaySound "Todosbordo"
	TStacion.IsDropped=True
	LMLCMTR.State=1
	LdLMLCMTR.State=1
	LFPP01.State=2
	LFPP02.State=2
	LFPP03.State=2
	LdLFPP01.State=2
	LdLFPP02.State=2
	LdLFPP03.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckLoco()
End Sub
	
Sub CheckLoco()
	If LMLCMTR.State=1 Then
	TimerLoco.Enabled=True
	End If
End Sub

Sub TriggerLoco_Hit()
	If LMLCMTR.State=1 Then
	PlaySound "loco"
	p1 = p1 + (15000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub TimerLoco_Timer()
	TimerLoco.enabled=False
	TStacion.IsDropped=False
	LMLCMTR.State=0
	LdLMLCMTR.State=0
	LFPP01.State=0
	LFPP02.State=0
	LFPP03.State=0
	LdLFPP01.State=0
	LdLFPP02.State=0
	LdLFPP03.State=0
End Sub

'******************************************************************
'************ Mision UNI ( mision 06 ) ****************************
Sub TFrede_Hit()
	TFrede.IsDropped=True
	PlaySound "fronkonstin"
	LUNI06.State=2
	LdLUNI06.State=2
	p1 = p1 + (3500*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		addrendor (35)
		Trendor.enabled=True
		DMD_DisplaySceneEx "FredeUni.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 3500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
End Sub

Sub TUni01_Hit()
	If LUNI06.State=0 Then
	Playsound "round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI06.State=2 Then
	PlaySound "round"
	LUNI01.State=1
	LdLUNI01.State=1
	CheckUNILuz()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=2 Then
	LUNI06.State=1
	LdLUNI06.State=1
	Nerd.TransY=15
	PlaySound "NerdPregunta"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "UniNerd.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 Then
	PlaySound "continuemos"
	PCamilla.TransX=+20
	CheckCamillaPos()
	p1 = p1 + (2500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 and PCamilla.TransX=100=True Then
	p1 = p1 + (25000*MultiPts)
	PlaySound "paciente"
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "camilla.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub TUni02_Hit()
	If LUNI06.State=0 Then
	PlaySound "round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI06.State=2 Then
	PlaySound "round"
	LUNI02.State=1
	LdLUNI02.State=1
	CheckUNILuz()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=2 Then
	LUNI06.State=1
	LdLUNI06.State=1
	Nerd.TransY=15
	PlaySound "NerdPregunta"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "UniNerd.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 Then
	PCamilla.TransX=+20
	PlaySound "continuemos"
	p1 = p1 + (2500*MultiPts) 
	OnScoreboardChanged
	CheckCamillaPos()
	CheckPuntaje()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 and PCamilla.TransX=100=True Then
	p1 = p1 + (25000*MultiPts) 
	OnScoreboardChanged
	PlaySound "paciente"
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "camilla.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub TUni03_Hit()
	If LUNI06.State=0 Then
	PlaySound "Round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI06.State=2 Then
	LUNI03.State=1
	LdLUNI03.State=1
	PlaySound "round"
	CheckUNILuz()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=2 Then
	LUNI06.State=1
	LdLUNI06.State=1
	PlaySound "NerdPregunta"
	Nerd.TransY=15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "UniNerd.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 Then
	PCamilla.TransX=+20
	PlaySound "continuemos"
	p1 = p1 + (2500*MultiPts) 
	OnScoreboardChanged
	CheckCamillaPos()
	CheckPuntaje()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 and PCamilla.TransX=100=True Then
	Nerd.TransY=0
	PlaySound "round"
	p1 = p1 + (25000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "camilla.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub TUni04_Hit()
	If LUNI06.State=0 Then
	PlaySound "round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI06.State=2 Then
	PlaySound "round"
	LUNI04.State=1
	LdLUNI04.State=1
	CheckUNILuz()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=2 Then
	LUNI06.State=1
	LdLUNI06.State=1
	Nerd.TransY=15
	PlaySound "NerdPregunta"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 Then
	PCamilla.TransX=+20
	CheckCamillaPos()
	p1 = p1 + (2500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 and PCamilla.TransX=100=True Then
	p1 = p1 + (25000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "camilla.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub TUni05_Hit()
	If LUNI06.State=0 Then
	PlaySound "round"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI06.State=2 Then
	PlaySound "round"
	LUNI05.State=1
	LdLUNI05.State=1
	CheckUNILuz()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=2 Then
	PlaySound "NerdPregunta"
	LUNI06.State=1
	LdLUNI06.State=1
	Nerd.TransY=15
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "UniNerd.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 Then
	PCamilla.TransX=+20
	PlaySound "continuemos"
	CheckCamillaPos()
	p1 = p1 + (2500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.State=1 and PCamilla.TransX=100=True Then
	p1 = p1 + (25000*MultiPts) 
	PlaySound "paciente"
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (23)
		Trendor.enabled=True
		DMD_DisplaySceneEx "camilla.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckUNILuz()
	If LUNI01.State=1 and LUNI02.State=1 and LUNI03.State=1 and LUNI04.State=1 and LUNI05.State=1 Then
	LUNI01.State=2
	LUNI02.State=2
	LUNI03.State=2
	LUNI04.State=2
	LUNI05.State=2
	LdLUNI01.State=2
	LdLUNI02.State=2
	LdLUNI03.State=2
	LdLUNI04.State=2
	LdLUNI05.State=2
	CheckUni()
	End If
End Sub

Sub CheckUni()
	If LUNI01.State=2 and LUNI02.State=2 and LUNI03.State=2 and LUNI04.State=2 and LUNI05.State=2 and LUNI06.state=1 Then
	TimerUNI.enabled=True
	PlaySound "ring"
	End If
End Sub

Sub CheckCamillaPos()
	If Pcamilla.TransX<100 Then
	TCamilla.IsDropped=True
	End If

	If Pcamilla.TransX=100 Then
	TCamilla.IsDropped=False
	End If
End Sub

Sub TCamilla_Init()
	TCamilla.IsDropped=True
End Sub

Sub TCamilla_Hit()
	TCamilla.IsDropped=True
	PlaySound "auch"
	p1 = p1 + (35000*MultiPts) 
	OnScoreboardChanged
	TimerUNI.Enabled=False
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (9)
		Trendor.enabled=true
		DMD_DisplaySceneEx "caeViejo.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 900, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
End Sub

Sub TimerUNI_Timer()
	TimerUNI.Enabled=False
	LUNI01.State=0
	LUNI02.State=0
	LUNI03.State=0
	LUNI04.State=0
	LUNI05.State=0
	LUNI06.State=0
	LdLUNI01.State=0
	LdLUNI02.State=0
	LdLUNI03.State=0
	LdLUNI04.State=0
	LdLUNI05.State=0
	LdLUNI06.State=0
	Pcamilla.TransX=0
	Nerd.TransY=0
	TFrede.IsDropped=False
	TCamilla.IsDropped=True
	PlaySound "Ring"
End Sub

'******************************************************************
'**************** Mision Violin ( mision 10 ) *********************
Sub TViolin_Hit()
	If LDespierto.State=0 Then
	PlaySound "frau"
	TViolin.IsDropped=True
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "Frau"
	TViolin.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	'Da puntos
		If rendor=0 Then
		AddRendor (9)
		Trendor.enabled=True
		DMD_DisplaySceneEx "violno.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 900, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub KickerViolin_Hit()
	If LDespierto.State=0 Then
	PlaySound "brujerycaball"
	KickerViolin.DestroyBall
	KickerCuevaPost.CreateBall
	KickerCuevaPost.Kick 315, 18
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 and LMTI01.State=0 Then
	PlaySound "elviolin"
	LViolRitmo.State=2
	TimerVIolin.Enabled=True
	CheckRimoV()
		If rendor=0 Then
		AddRendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "misionViolin.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub TimerVIolin_Timer()
	AddRimoV (+1)
	CheckRimoV()
End Sub

Sub CheckRimoV()
	If RimoV=1 and LViolRitmo.state=2 Then
	PlaySound "mviolin"
	End If
	'---------------------------------------------------------------
	If RimoV=3 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "mantengaRitmo.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If RimoV=4 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=6 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
		If rendor=0 Then
		Addrendor (50)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Violin.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 5000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If RimoV=7 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=9 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=10 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=13 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
	End If
	
	If RimoV=14 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=18 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
	End If
	
	If RimoV=19 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=21 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
	End If
	
	If RimoV=22 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=25 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=26 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=29 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=30 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=32 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
	End If
	
	If RimoV=33 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=34 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=35 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=37 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
	End If
	
	If RimoV=38 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=40 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=41 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=44 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=45 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=47 and LViolRitmo.State=2 Then
	LL.State=1
	CheckSitV()
	End If
	
	If RimoV=48 and LViolRitmo.State=2 Then
	LL.State=0
	CheckSitV()
	End If
	'---------------------------------------------------------------
	If RimoV=51 and LViolRitmo.State=2 Then
	LR.State=1
	CheckSitV()
	End If
	'---- son 15 puntos posibles
	If RimoV=52 and LViolRitmo.State=2 Then
	LR.State=0
	CheckSitV()
	End If
	'---Finaliza
	If RimoV=55 and LViolRitmo.State=2 Then
	LViolRitmo.State=0
	AddRimoV (-RimoV)
	KickerViolin.DestroyBall
	KickerCuevaPost.CreateBall
	KickerCuevaPost.Kick 315, 18
	CheckFinV()
	End If
End Sub

Sub CheckSitV()
	If LL.State=0 and LL1.State=1 Then
	AddLuzRimoV (-1)
	CheckLuzV()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=True
		DMD_DisplaySceneEx "mal.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LL.State=1 and LL1.State=1 Then
	AddLuzRimoV (+1)
	CheckLuzV()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=True
		DMD_DisplaySceneEx "bien.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LR.State=0 and LR1.State=1 Then
	AddLuzRimoV (-1)
	CheckLuzV()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=True
		DMD_DisplaySceneEx "mal.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LR.State=1 and LR1.State=1 Then
	AddLuzRimoV (+1)
	CheckLuzV()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=True
		DMD_DisplaySceneEx "bien.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckLuzV()
	If LuzRimoV<0 Then
	AddIRA (+15)
	p1 = p1 - (15000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoV>0 and LuzRimoV<3 Then
	AddIra (+10)
	p1 = p1 - (10000*MultiPts) 
	CheckIRA
	OnScoreboardChanged
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoV>2 and LuzRimoV<5 Then
	AddIRA (-1)
	AddTurba (-1)
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoV>4 and LuzRimoV<6 Then
	AddIRA (-5)
	AddFdespertar (-5)
	AddTurba (-1)
	p1 = p1 + (25000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckDespertar()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoV>5 and LuzRimoV<11 Then
	AddIRA (-10)
	AddFdespertar (-10)
	AddTurba (-1)
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	CheckTurr()
	CheckDespertar()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoV>10 Then
	AddIRA (-15)
	AddFdespertar (-50)
	AddTurba (-1)
	p1 = p1 + (100000*MultiPts) 
	OnScoreboardChanged
	CheckTurr ()
	CheckDespertar()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
End Sub

Sub CheckFinV()
	If LViolRitmo.State=0 and LuzRimoV<0 Then
	AddLuzRimoV (+5000)
	TimerLimpia.Enabled=True
	End If

	If LViolRitmo.State=0 and LuzRimoV>0 Then
	AddLuzRimoV (-LuzRimoV)
	End If
End Sub

Sub TimerLimpia_Timer()
	TimerLimpia.Enabled=False
	AddLuzRimoV (-LuzRimoV)
End Sub

'-------------------------------------------------------------------
'---------------- Cuenta Ritmo Violin ------------------------------
Sub AddRimoV(Rim0V) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	RimoV = RimoV + Rim0V ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

Sub AddLuzRimoV(lRim0V) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	LuzRimoV = LuzRimoV + lRim0V ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'*******************************************************************

'*******************************************************************
'******************  Mision RITZ ***********************************
Sub TarimaRITZ_Init()
	TarimaRITZ.IsDropped=True
	TarimaRITZ.Visible=False
End Sub

Sub TColTelonI_Hit()
	If LDespierto.State=0 Then
	PlaySound "round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "round"
	AddPritz (+1)
	p1 = p1 +(5000*MultiPts) 
	OnScoreboardChanged
	CheckPPritz()
	CheckPuntaje()
	End If
End Sub

Sub TColTelonD_Hit()
	If LDespierto.State=0 Then
	PlaySound "round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "round"
	AddPritz (+1)
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPPritz()
	CheckPuntaje()
	End If
End Sub

Sub CheckPPritz()
	If Pritz>5 and Pritz<11 Then
	PlaySound "Ritz01"
	End If

	If Pritz>12 and Pritz<20 Then
	PlaySound "Ritz01"
	End If

	If Pritz=20 then
		PlaySound "Ritz03"
		If rendor=0 Then
		Addrendor (45)
		Trendor.enabled=True
		DMD_DisplaySceneEx "TITZprep.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 4500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If Pritz>20 Then
	PlaySound "Ritz02"
	LRMA01.State=2
	LdLRMA01.State=2
	End If
End Sub

Sub KickerARitz_Hit()
	If LRMA01.State=0 Then
	PlaySound "kickerKik"
	KickerARitz.DestroyBall
	KickerIgor.CreateBall
	KickerIgor.Kick 335, 35
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LRMA01.State=2 Then
	KickerARitz.Kick 0, 18
	PlaySound"kickerkik"
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "MisionRITZ.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub KickerRitz_Hit()
	PlaySound "kickerkik"
	LRMA01.State=1
	LdLRMA01.State=1
	TimerTelon.Enabled=True
End Sub

Sub TimerTelon_Timer()
	If LRMA01.State=1 Then
	AddTelM (+1)
	CheckTelonM()
	End If
End Sub

Sub CheckTelonM()
	If TelM=1 and LRMA=1 Then
	PlaySound "presenta"
	ChapTelon.TransX=10
	ChapTelon1.TransX=10
	ChapTelon2.TransX=10
	ChapTelon3.TransX=10
	ChapTelon4.TransX=10
	End If

	If TelM=2 and LRMA=1 Then
	ChapTelon.TransX=20
	ChapTelon1.TransX=20
	ChapTelon2.TransX=20
	ChapTelon3.TransX=20
	ChapTelon4.TransX=20
	End If

	If TelM=3 and LRMA=1 Then
	ChapTelon.TransX=30
	ChapTelon1.TransX=30
	ChapTelon2.TransX=30
	ChapTelon3.TransX=30
	ChapTelon4.TransX=30
	End If

	If TelM=4 and LRMA=1 Then
	ChapTelon.TransX=40
	ChapTelon1.TransX=40
	ChapTelon2.TransX=40
	ChapTelon3.TransX=40
	ChapTelon4.TransX=40
	End If

	If TelM=5 and LRMA=1 Then
	ChapTelon.TransX=50
	ChapTelon1.TransX=50
	ChapTelon2.TransX=50
	ChapTelon3.TransX=50
	ChapTelon4.TransX=50
	End If

	If TelM=6 and LRMA=1 Then
	ChapTelon.TransX=60
	ChapTelon1.TransX=60
	ChapTelon2.TransX=60
	ChapTelon3.TransX=60
	ChapTelon4.TransX=60
	End If

	If TelM=7 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=70
	ChapTelon4.TransX=70
	End If

	If TelM=8 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=80
	ChapTelon2.TransX=80
	ChapTelon3.TransX=80
	ChapTelon4.TransX=80
	End If

	If TelM=9 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=90
	ChapTelon2.TransX=90
	ChapTelon3.TransX=90
	ChapTelon4.TransX=90
	End If

	If TelM=10 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=100
	ChapTelon2.TransX=100
	ChapTelon3.TransX=100
	ChapTelon4.TransX=100
	End If

	If TelM=11 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=110
	ChapTelon2.TransX=110
	ChapTelon3.TransX=110
	ChapTelon4.TransX=110
	End If

	If TelM=12 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=120
	ChapTelon3.TransX=120
	ChapTelon4.TransX=120
	End If

	If TelM=13 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=130
	ChapTelon3.TransX=130
	ChapTelon4.TransX=130
	End If

	If TelM=14 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=140
	ChapTelon3.TransX=140
	ChapTelon4.TransX=140
	End If
	
	If TelM=15 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=150
	ChapTelon3.TransX=150
	ChapTelon4.TransX=150
	End If
	
	If TelM=16 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=160
	ChapTelon3.TransX=160
	ChapTelon4.TransX=160
	End If

	If TelM=17 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=170
	ChapTelon4.TransX=170
	End If

	If TelM=18 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=180
	ChapTelon4.TransX=180
	End If

	If TelM=19 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=190
	ChapTelon4.TransX=190
	End If
	
	If TelM=20 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=200
	ChapTelon4.TransX=200
	End If

	If TelM=21 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=210
	ChapTelon4.TransX=210
	End If

	If TelM=22 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=220
	ChapTelon4.TransX=220
	End If

	If TelM=23 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=220
	ChapTelon4.TransX=230
	End If

	If TelM=24 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=220
	ChapTelon4.TransX=240
	End If

	If TelM=25 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=220
	ChapTelon4.TransX=250
	End If

	If TelM=26 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=220
	ChapTelon4.TransX=260
	danza.Visible=True
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	End If

	If TelM=27 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=120
	ChapTelon2.TransX=170
	ChapTelon3.TransX=220
	ChapTelon4.TransX=270
	Danza.TransY=140
	danza1.TransY=140
	danza2.TransY=140
	danza3.TransY=140
	danza4.TransY=140
	danza5.TransY=140
	danza6.TransY=140
	danza7.TransY=140
	danza8.TransY=140
	danza9.TransY=140
	danza10.TransY=140
	danza11.TransY=140
	End If

	If TelM=28 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=110
	ChapTelon2.TransX=160
	ChapTelon3.TransX=230
	ChapTelon4.TransX=270
	End If

	If TelM=29 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=100
	ChapTelon2.TransX=150
	ChapTelon3.TransX=240
	ChapTelon4.TransX=270
	End If

	If TelM=30 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=90
	ChapTelon2.TransX=140
	ChapTelon3.TransX=250
	ChapTelon4.TransX=270
	End If

	If TelM=31 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=80
	ChapTelon2.TransX=130
	ChapTelon3.TransX=260
	ChapTelon4.TransX=270
	End If

	If TelM=32 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=120
	ChapTelon3.TransX=270
	ChapTelon4.TransX=270
	End If

	If TelM=33 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=110
	ChapTelon3.TransX=270
	ChapTelon4.TransX=270
	End If

	If TelM=34 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=100
	ChapTelon3.TransX=270
	ChapTelon4.TransX=270
	End If

	If TelM=35 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=90
	ChapTelon3.TransX=270
	ChapTelon4.TransX=270
	End If

	If TelM=36 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=80
	ChapTelon3.TransX=270
	ChapTelon4.TransX=270
	End If

	If TelM=37 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=270
	ChapTelon4.TransX=270
	TimerTelon.enabled=False
	LESC01.State=1
	LESC02.State=1
	LESC03.State=1
	LESC04.State=1
	LESC05.State=1
	LESC06.State=1
	LdLESC01.State=1
	LdLESC02.State=1
	LdLESC03.State=1
	LdLESC04.State=1
	LdLESC05.State=1
	LdLESC06.State=1
	TimerRitz.enabled=True
	CheckBalando()
	End If

	If TelM=38 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=250
	ChapTelon4.TransX=270
	End If

	If TelM=39 and LRMA=1 Then
	PlaySound "abucheo"
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=240
	ChapTelon4.TransX=270
	End If

	If TelM=40 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=230
	ChapTelon4.TransX=270
	End If

	If TelM=41 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=220
	ChapTelon4.TransX=270
	End If

	If TelM=42 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=210
	ChapTelon4.TransX=260
	End If

	If TelM=43 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=200
	ChapTelon4.TransX=250
	End If

	If TelM=44 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=190
	ChapTelon4.TransX=240
	End If

	If TelM=44 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=180
	ChapTelon4.TransX=230
	End If

	If TelM=44 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=170
	ChapTelon4.TransX=220
	Danza.TransY=0
	danza1.TransY=0
	danza2.TransY=0
	danza3.TransY=0
	danza4.TransY=0
	danza5.TransY=0
	danza6.TransY=0
	danza7.TransY=0
	danza8.TransY=0
	danza9.TransY=0
	danza10.TransY=0
	danza11.TransY=0
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	End If

	If TelM=45 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=160
	ChapTelon4.TransX=210
	End If

	If TelM=46 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=150
	ChapTelon4.TransX=200
	End If

	If TelM=47 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=130
	ChapTelon4.TransX=180
	End If

	If TelM=48 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=120
	ChapTelon4.TransX=170
	End If

	If TelM=49 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=110
	ChapTelon4.TransX=160
	End If

	If TelM=50 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=100
	ChapTelon4.TransX=150
	End If

	If TelM=51 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=90
	ChapTelon4.TransX=140
	End If

	If TelM=52 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=80
	ChapTelon4.TransX=130
	End If

	If TelM=53 and LRMA=1 Then
	ChapTelon.TransX=70
	ChapTelon1.TransX=70
	ChapTelon2.TransX=70
	ChapTelon3.TransX=70
	ChapTelon4.TransX=120
	End If

	If TelM=54 and LRMA=1 Then
	ChapTelon.TransX=60
	ChapTelon1.TransX=60
	ChapTelon2.TransX=60
	ChapTelon3.TransX=60
	ChapTelon4.TransX=110
	End If

	If TelM=55 and LRMA=1 Then
	ChapTelon.TransX=50
	ChapTelon1.TransX=50
	ChapTelon2.TransX=50
	ChapTelon3.TransX=50
	ChapTelon4.TransX=100
	End If

	If TelM=56 and LRMA=1 Then
	ChapTelon.TransX=40
	ChapTelon1.TransX=40
	ChapTelon2.TransX=40
	ChapTelon3.TransX=40
	ChapTelon4.TransX=90
	End If

	If TelM=57 and LRMA=1 Then
	ChapTelon.TransX=30
	ChapTelon1.TransX=30
	ChapTelon2.TransX=30
	ChapTelon3.TransX=30
	ChapTelon4.TransX=80
	End If

	If TelM=58 and LRMA=1 Then
	ChapTelon.TransX=20
	ChapTelon1.TransX=20
	ChapTelon2.TransX=20
	ChapTelon3.TransX=20
	ChapTelon4.TransX=70
	End If

	If TelM=59 and LRMA=1 Then
	ChapTelon.TransX=10
	ChapTelon1.TransX=10
	ChapTelon2.TransX=10
	ChapTelon3.TransX=10
	ChapTelon4.TransX=60
	End If

	If TelM=60 and LRMA=1 Then
	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=50
	End If

	If TelM=61 and LRMA=1 Then
	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=40
	End If

	If TelM=62 and LRMA=1 Then
	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=30
	End If

	If TelM=63 and LRMA=1 Then
	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=20
	End If

	If TelM=64 and LRMA=1 Then
	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=10
	End If

	If TelM=65 and LRMA=1 Then
	ChapTelon.TransX=0
	ChapTelon1.TransX=0
	ChapTelon2.TransX=0
	ChapTelon3.TransX=0
	ChapTelon4.TransX=0
	TimerTelon.enabled=False
	AddTelM (-TelM)
	LESC01.State=0
	LESC02.State=0
	LESC03.State=0
	LESC04.State=0
	LESC05.State=0
	LESC06.State=0
	LdLESC01.State=0
	LdLESC02.State=0
	LdLESC03.State=0
	LdLESC04.State=0
	LdLESC05.State=0
	LdLESC06.State=0
	LRMA01.State=0
	LdLRMA01.State=0
	KickerRitz.DestroyBall
	KickerCuevaPost.CreateBall
	KickerCuevaPost.Kick 315, 18
	CheckFinR()


	End If
End Sub


Sub TimerRitz_Timer()
	If LESC01.State=1 Then
	AddBaila (+1)
	CheckBalando()
	End If
End Sub

Sub CheckBalando()
	'=================================
	If Baila=3 and LESC01.State=1 Then
	PlaySound "bailaRitz"
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=4 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=6 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=7 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=10 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=11 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=13 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=14 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=16 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=17 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=20 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=4 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=23 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=24 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=25 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=26 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=30 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=31 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=32 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=33 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=37 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=38 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=40 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=41 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=44 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=45 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=50 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=51 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=52 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=53 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=54 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=55 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=57 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=58 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=60 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=61 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=63 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=64 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=66 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=67 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=68 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=69 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=71 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=72 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=73 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=74 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=77 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=78 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=80 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=81 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=83 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=84 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=86 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=87 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=88 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=89 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=91 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=92 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=93 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=94 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=97 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=98 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=99 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=100 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=103 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=104 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=105 and LESC01.State=1 Then
	LR.State=1
	CheckSitR()
	End If
	
	If RimoV=106 and LESC01.State=1 Then
	LR.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=107 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=108 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=================================
	If Baila=110 and LESC01.State=1 Then
	LL.State=1
	CheckSitR()
	End If
	
	If RimoV=111 and LESC01.State=1 Then
	LL.State=0
	CheckSitR()
	End If
	'=====Son 36 puntos posibles=====
	If RimoV=115 and LESC01.State=1 Then
	LL.State=0
	TimerTelon.enabled=True
	CheckSitR()
	End If
End Sub


Sub CheckFinR()
	If LRMA01.State=0 and LuzRimoR<0 Then
	AddLuzRimoR (+5000)
	TimerLimpiaR.Enabled=True
	End If

	If LRMA01.State=0 and LuzRimoR>0 Then
	AddLuzRimoR (-LuzRimoR)
	End If
End Sub

Sub CheckSitR()
	If LL.State=0 and LL1.State=1 Then
	AddLuzRimoR (-1)
	CheckLuzR()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=true
		DMD_DisplaySceneEx "mal.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LL.State=1 and LL1.State=1 Then
	AddLuzRimoR (+1)
	CheckLuzR()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=true
		DMD_DisplaySceneEx "bien.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LR.State=0 and LR1.State=1 Then
	AddLuzRimoR (-1)
	CheckLuzR()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=true
		DMD_DisplaySceneEx "mal.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LR.State=1 and LR1.State=1 Then
	AddLuzRimoR (+1)
	CheckLuzR()
		If rendor=0 Then
		Addrendor (5)
		Trendor.enabled=true
		DMD_DisplaySceneEx "bien.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 500, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub


Sub CheckLuzR()
	If LuzRimoR<0 Then
	AddIra (+200)
	CheckIRA()
	p1 = p1 - (100000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=True
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=1 Then
	AddIra (+20)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=True
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=1
	LdLTOM01.State=1
	p1 = p1 - (50000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=2 Then
	AddIra (+15)
	p1 = p1 - (25000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=True
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=1
	LdLTOM01.State=1
	p1 = p1 - (25000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=3 Then
	AddIra (+15)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=True
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=1
	LdLTOM01.State=1
	p1 = p1 - (15000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=4 Then
	AddIra (+10)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=True
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=1
	LdLTOM01.State=1
	p1 = p1 - (10000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=5 Then
	AddIra (+5)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=True
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=1
	LdLTOM01.State=1
	p1 = p1 - (5000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=6 Then
	AddIra (+1)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=True
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	p1 = p1 - (1000*MultiPts) 
	OnScoreboardChanged
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=7 Then
	AddIra (-1)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=True
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=8 Then
	AddIra (-5)
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=True
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=9 Then
	AddIra (-5)
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=True
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=10 Then
	AddIra (-5)
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=True
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=11 Then
	AddIra (-5)
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=True
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=12 Then
	AddIra (-5)
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=True
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=13 Then
	AddIra (-5)
	p1 = p1 + (10000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=True
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=14 Then
	AddIra (-5)
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=True
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=15 Then
	AddIra (-5)
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=True
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=16 Then
	AddIra (-5)
	p1 = p1 + (20000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=True
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=17 Then
	AddIra (-5)
	p1 = p1 + (50000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=True
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=18 Then
	AddIra (-5)
	p1 = p1 + (50000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=True
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=19 Then
	AddIra (-5)
	p1 = p1 + (50000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=True
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=20 Then
	AddIra (-10)
	p1 = p1 + (100000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=True
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=21 Then
	AddIra (-10)
	p1 = p1 + (100000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=True
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()	
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=22 Then
	AddIra (-10)
	p1 = p1 + (100000*MultiPts)
	OnScoreboardChanged
	danza.Visible=True
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=23 Then
	AddIra (-10)
	p1 = p1 + (2500000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=True
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=2
	LdLMUS01.State=2
	LTOM01.State=2
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=24 Then
	AddIra (-10)
	p1 = p1 + (250000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=True
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=25 Then
	AddIra (-10)
	p1 = p1 + (500000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=True
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=27 Then
	AddIra (-10)
	p1 = p1 + (1000000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=True
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=28 Then
	AddIra (-10)
	p1 = p1 + (1000000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=True
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=29 Then
	AddIra (-10)
	p1 = p1 + (5000000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=True
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=30 Then
	AddIra (-15)
	p1 = p1 + (10000000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=True
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=31 Then
	AddIra (-15)
	p1 = p1 + (20000000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=True
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=32 Then
	AddIra (-15)
	p1 = p1 + (25000000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=True
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If
	
	If LuzRimoR=33 Then
	AddIra (-15)
	p1 = p1 + (50000000*MultiPts)
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=True
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=34 Then
	AddIra (-15)
	p1 = p1 + (100000000*MultiPts) 
	OnScoreboardChanged
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=False
	danza10.Visible=True
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckPuntaje()
	CheckFparla()
	End If

	If LuzRimoR=35 Then
	AddIra (-15)
	KickerCuevaDel.Createball
	KickerCuevaPost.Createball
	KickerCast.Createball
	KickerCuevaDel.kick 165, 7
	KickerCuevaPost.kick 315, 20
	KickerCast.kick 230, 25
	Lmultiball.state=1
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=False
	danza9.Visible=True
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckFparla()
		If rendor=0 Then
		Addrendor (26)
		Trendor.enabled=True
		DMD_DisplaySceneEx "MultiBall.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2600, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LuzRimoR=36 Then
	AddIra (-15)
	Credits +1
	danza.Visible=False
	danza1.Visible=False
	danza2.Visible=False
	danza3.Visible=False
	danza4.Visible=False
	danza5.Visible=False
	danza6.Visible=False
	danza7.Visible=False
	danza8.Visible=True
	danza9.Visible=False
	danza10.Visible=False
	danza11.Visible=False
	LMUS01.State=1
	LTOM01.State=2
	LdLMUS01.State=1
	LdLTOM01.State=2
	AddTurba (-1)
	CheckTurr ()
	CheckIRA()
	CheckFparla()
	End If
End Sub

Sub TimerLimpiaR_Timer()
	TimerLimpiaR.Enabled=False
	AddLuzRimoR (-LuzRimoR)
End Sub

'------- resultado de tomate o musica y tiempo de efecto ------
Sub CheckTOMUS()
	If LESC01.State=0 and LTOM01.State=2 and LMUS01=2 Then
	LMUS01.state=0
	LTOM01.State=0
	LdLMUS01.State=0
	LdLTOM01.State=0
	End If

	If LESC01.State=0 and LTOM01.State=1 and LMUS01=2 Then
	TimerTOMUS.enabled=True
	TomatRitz()
	End If

	If LESC01.State=0 and LTOM01.State=2 and LMUS01=1 Then
	TimerTOMUS.enabled=True
	MusiRitz()
	End If
End Sub

Sub MusiRitz()
	If LESC01.State=0 and LTOM01.State=2 and LMUS01=1 and TimerTOMUS.enabled=True Then
	p1 = p1 + (200000000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub TomatRitz()
	If LESC01.State=0 and LTOM01.State=1 and LMUS01=2 and TimerTOMUS.enabled=True Then
	p1 = p1 - (100000000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

Sub TimerTOMUS_Timer()
	TimerTOMUS.enabled=False
	LMUS01.state=0
	LTOM01.State=0
	LdLMUS01.State=0
	LdLTOM01.State=0
End Sub

'-------------------------------------------------------------------
'---------------- Cuenta Ritmo RITZ  -------------------------------
Sub AddRimoR(Rim0R) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	RimoR = RimoR + Rim0R ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

Sub AddLuzRimoR(lRim0R) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	LuzRimoR = LuzRimoR + lRim0R ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

Sub AddPritz(PRTZ) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	LuzPritz = Pritz + PRTZ ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'-------------- para animacion
Sub AddBaila(Ba0) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	Baila = Baila + Ba0 ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

Sub AddTelM(Tel0) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	TelM = TelM + Tel0 ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

'*******************************************************************

'*******************************************************************
'*****************  Botones fuego y letra F  ***********************
Sub Fuego_Hit()
	If LDespierto.State=0 and LCZF01.State=0 Then
	PlaySound "fuegou"
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "fuegou"
	AddFdespertar (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckDespertar()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LDespierto.State=1 Then
	PlaySound "fuego"
	AddIra (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LMCA01.State=2 and TimerCIego.Enabled=True Then
	PlaySound "fuegoamigo"
	AddIra (-10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=0=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=-10=True Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=10=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub Fuego1_Hit()
	If LDespierto.State=0 and LCZF01.State=0 Then
	PlaySound "fuegou"
	'puntos
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "fuegou"
	AddFdespertar (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckDespertar()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "fuego"
	AddIra (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LMCA01.State=2 and TimerCIego.Enabled=True Then
	PlaySound "fuegoamigo"
	AddIra (-10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=0=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=-10=True Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=10=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub Fuego2_Hit()
	If LDespierto.State=0 and LCZF01.State=0 Then
	PlaySound "fuegou"
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "fuegou"
	AddFdespertar (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	CheckDespertar()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "fuego"
	AddIra (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LMCA01.State=2 and TimerCIego.Enabled=True Then
	PlaySound "fuegoamigo"
	AddIra (-10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=0=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If


	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=-10=True Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=10=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUEg03.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub Fuego3_Hit()
	If LDespierto.State=0 and LCZF01.State=0 Then
	PlaySound "fuegou"
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "fuegou"
	AddFdespertar (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	CheckDespertar()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=1 Then
	PlaySound "fuego"
	AddIra (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LMCA01.State=2 and TimerCIego.Enabled=True Then
	PlaySound "fuegoamigo"
	AddIra (-10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=0=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=-10=True Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=10=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE04.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

End Sub

Sub Fuego4_Hit()
	If LDespierto.State=0 and LCZF01.State=0 Then
	PlaySound "fuegou"
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If
	
	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "fuegou"
	AddFdespertar (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	CheckDespertar()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()

	End If

	If LDespierto.State=1 Then
	PlaySound "fuego"
	AddIra (+10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LMCA01.State=2 and TimerCIego.Enabled=True Then
	PlaySound "fuegoamigo"
	AddIra (-10)
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=0=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=-10=True Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	CheckIRA()
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
	CheckFparla()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If LDespierto.State=1 and LAMM01.State=1 and FpieD.RotZ=10=true Then
	PlaySound "fuego"
	AddIra (-5)
	FpieD.RotZ=-10
	LFDK01.State=1
	LdLFDK01.State=1
	LFKT01.State=1
	LFUE05.State=1
	CheckNombres()
	CheckIRA()
	CheckFparla()
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
		If rendor=0 Then
		Addrendor (17)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Fuego.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1700, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'******************************************************************
'************ MULTIPLICADOREs y sus LUCES *************************

Sub CheckMultiplicaX2()
	If LLCH01.State=1 Then
	PlaySound "bonus"
	LMulX2.State=1
	LdLMulX2.State=1
	AddMultiPts (+1)
		If rendor=0 Then
		Addrendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "X2.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckMultiplicaX3()
	If LLCH01.State=1 and LMPpalE2.State=2 Then
	PlaySound "bonus"
	LMulX3.State=1
	LdLMulX3.State=1
	AddMultiPts (+1)
		If rendor=0 Then
		Addrendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "X3.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckMultiplicaX4()
	If LLCH01.State=1 and LMPpalE2.State=2 and LECF15.State=1 Then
	PlaySound "bonus"
	LMulX4.State=1
	LdLMulX4.State=1
	AddMultiPts (+1)
		If rendor=0 Then
		Addrendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "X4.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckMultiplicaX5()
	If LLCH01.State=1 and LMPpalE2.State=2 and LECF15.State=1 and LECF16.State=1 Then
	PlaySound "bonus"
	LMulX5.State=1
	LdLMulX5.State=1
	AddMultiPts (+1)
		If rendor=0 Then
		Addrendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "X5.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckMultiplicaX6()
	If LLCH01.State=1 and LMPpalE2.State=2 and LECF15.State=1 and LECF16.State=1 and LCZF01.State=2 Then
	PlaySound "bonus"
	LMulX6.State=1
	LdLMulX6.State=1
	AddMultiPts (+1)
		If rendor=0 Then
		Addrendor (13)
		Trendor.enabled=True
		DMD_DisplaySceneEx "X6.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 1300, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

'------------------------------------------------------------------
'---------------- Cuentamultiplicador   ------------------------------
Sub AddMultiPts(Pxp) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	MultiPts = MultiPts + Pxp ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'Entonces el puntaje comun seria del DMD p1 o el que sea
'multiplicado por MultiPts, es decir P1*MultiPts

'******************************************************************
'************* Sistema de Bouns Nombres ***************************
'frederik 
'LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1
'frankenstein
'LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1
'Igor
'LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1
Sub CheckNombres()
'IGOR  incompleto
If LIGO01.State=0 or LIGO02.State=0 or LIGO03.State=0 or LIGO04.state=0 Then
LIndicaLIGO.State=2
End If
'FREDERIK incompleto
If LFDK01.state=0 Or LFDK02.state=0 or LFDK03.State=0 or LFDK04.state=0 or LFDK05.state=0 or LFDK06.state=0 or LFDK07.state=0 or LFDK08.state=0 Then
LIndicaFDK.State=2
End If
'FRANKENSTEIN incompleto
If LFKT01.state=0 or LFKT02.state=0 or LFKT03.state=0 or LFKT04.state=0 or LFKT05.state=0 or LFKT06.state=0 or LFKT07.state=0 or LFKT08.state=0 Or LFKT09.state=0 or LFKT10.state=0 or LFKT11.state=0 or LFKT12.state=0 Then
LIndicaFNKT.State=2
End If
'-------- Solo Igor
If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=2 and LIndicaFNKT.state=2 Then
LIndicaLIGO.State=0
PlaySound "igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
TimerBonusLet.enabled=True
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=2 and LIndicaFNKT.state=1 Then
LIndicaLIGO.State=0
PlaySound "igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=2 and LIndicaFNKT.state=0 Then
LIndicaLIGO.State=0
PlaySound "igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=0 and LIndicaFNKT.state=2 Then
LIndicaLIGO.State=0
PlaySound "igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=1 and LIndicaFNKT.state=2 Then
LIndicaLIGO.State=0
PlaySound "Igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=1 and LIndicaFNKT.state=1 Then
LIndicaLIGO.State=0
PlaySound "igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIGO01.State=1 and LIGO02.State=1 and LIGO03.State=1 and LIGO04.state=1 and LIndicaFDK.state=0 and LIndicaFNKT.state=0 Then
LIndicaLIGO.State=0
PlaySound "Igor"
LIGO01.State=2
LdLIGO01.State=2
LIGO02.State=2
LdLIGO02.State=2
LIGO03.State=2
LdLIGO03.State=2
LIGO04.state=2
LdLIGO04.State=2
CheckNombres()
	If rendor=0 Then
	addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If
'-------- Solo Frederik
If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=2 and LIndicaFNKT.State=2 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LdLFDK02.State=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
TimerBonusLet.enabled=True
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=2 and LIndicaFNKT.State=1 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=2 and LIndicaFNKT.State=0 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LdLFDK02.State=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=1 and LIndicaFNKT.State=2 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LdLFDK02.State=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=0 and LIndicaFNKT.State=2 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LdLFDK02.State=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If


If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=1 and LIndicaFNKT.State=1 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LdLFDK02.State=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik,png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFDK01.state=1 and LFDK02.state=1 and LFDK03.State=1 and LFDK04.state=1 and LFDK05.state=1 and LFDK06.state=1 and LFDK07.state=1 and LFDK08.state=1 and LIndicaLIGO.State=0 and LIndicaFNKT.State=0 Then
LIndicaFDK.State=0
PlaySound "frederik"
LFDK01.state=2
LdLFDK01.State=2
LFDK02.state=2
LdLFDK02.State=2
LFDK03.State=2
LdLFDK03.State=2
LFDK04.state=2
LdLFDK04.State=2
LFDK05.state=2
LdLFDK05.State=2
LFDK06.state=2
LdLFDK06.State=2
LFDK07.state=2
LdLFDK07.State=2
LFDK08.state=2
LdLFDK08.State=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If
'-------- Solo Frankenstein
If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=2 and LIndicaLIGO.state=2 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
TimerBonusLet.enabled=True
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
CheckNombres()
End If

If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=2 and LIndicaLIGO.state=1 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=2 and LIndicaLIGO.state=0 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=1 and LIndicaLIGO.state=2 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=0 and LIndicaLIGO.state=2 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=1 and LIndicaLIGO.state=1 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LFKT01.state=1 and LFKT02.state=1 and LFKT03.state=1 and LFKT04.state=1 and LFKT05.state=1 and LFKT06.state=1 and LFKT07.state=1 and LFKT08.state=1 and LFKT09.state=1 and LFKT10.state=1 and LFKT11.state=1 and LFKT12.state=1 and LIndicaFDK.State=0 and LIndicaLIGO.state=0 Then
LIndicaFNKT.state=0
PlaySound "yofrankenstain"
LFKT01.state=2
LFKT02.state=2
LFKT03.state=2
LFKT04.state=2
LFKT05.state=2
LFKT06.state=2
LFKT07.state=2
LFKT08.state=2
LFKT09.state=2
LFKT10.state=2
LFKT11.state=2
LFKT12.state=2
CheckNombres()
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If
'-------- Igor y Frederick
If LIndicaLIGO.State=0 and LIndicaFDK.state=0 and LIndicaFNKT.state=1 Then
LIndicaLIGO.State=2
LIndicaFDK.state=2
LFDK01.state=0
LdLFDK01.State=0
LIGO01.state=0
LdLIGO01.state=0
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor_Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

If LIndicaLIGO.State=0 and LIndicaFDK.state=0 and LIndicaFNKT.state=2 Then
LEspCast.state=2
LEspCast1.State=2
LEspCast2.State=2
LvamoCast.State=2
LdLvamoCast.State=2
TimerBonusLet.enabled=True
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor_Frederik.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

'-------- Igor y Frankenstein
If LIndicaLIGO.State=0 and LIndicaFDK.state=1 and LIndicaFNKT.state=0 Then
LIndicaLIGO.State=2
LIndicaFNKT.state=2
LFKT01.state=0
LIGO01.state=0
LdLIGO01.state=0
End If

If LIndicaLIGO.State=0 and LIndicaFDK.state=2 and LIndicaFNKT.state=0 Then
LEspCast.state=2
LEspCast1.State=2
LEspCast2.State=2
LvamoCast.State=2
LdLvamoCast.State=2
TimerBonusLet.enabled=True
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Igor_Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

'-------- Frederik y Frankenstein
If LIndicaFDK.State=0 and LIndicaFNKT.state=0 and LIndicaLIGO.state=1 Then
LIndicaLIGO.State=2
LIndicaFNKT.state=2
LFKT01.state=0
LFDK01.state=0
LdLFDK01.State=0
End If

If LIndicaFDK.State=0 and LIndicaFNKT.state=0 and LIndicaLIGO.state=2 Then
PlaySound "yofrankenstain"
LEspCast.state=2
LEspCast1.State=2
LEspCast2.State=2
LvamoCast.State=2
LdLvamoCast.State=2
TimerBonusLet.enabled=True	
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik Frankenstein.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

'-------- Igor y Frederik, Frankenstein
If LIndicaLIGO.State=0 and LIndicaFDK.state=0 and LIndicaFNKT.state=0 Then
PlaySound "yofrankenstain"
LEspCast.state=2
LEspCast1.State=2
LEspCast2.State=2
LvamoCast.State=2
LdLvamoCast.State=2
TimerBonusLet.enabled=True
	If rendor=0 Then
	Addrendor (20)
	Trendor.enabled=True
	DMD_DisplaySceneEx "Frederik Frankenstein_Igor.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true
	End If
End If

End Sub


Sub TimerBonusLet_Timer()
	AddBonusTI (+1)
	CheckBonusTi()
End Sub

Sub CheckBonusTi()
	If BonusTi=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=2 and LIndicaFNKT.state=2 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0
	LIndicaLIGO.State=2
	LIGO01.State=0
	LdLIGO01.state=0
	LIGO02.State=0
	LdLIGO02.state=0
	LIGO03.State=0
	LdLIGO03.state=0
	LIGO04.state=0
	LdLIGO04.state=0
	CheckNombres()
	End If
	
	If BonusTi=2 and LIndicaLIGO.state=2 and LIndicaFDK.State=0 and LIndicaFNKT.state=2 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0
	LIndicaFDK.State=2
	LFDK01.state=0
	LdLFDK01.State=0
	LFDK02.state=0
	LdLFDK02.State=0
	LFDK03.State=0
	LdLFDK03.State=0
	LFDK04.state=0
	LdLFDK04.State=0
	LFDK05.state=0
	LdLFDK05.State=0
	LFDK06.state=0
	LdLFDK06.State=0
	LFDK07.state=0
	LdLFDK07.State=0
	LFDK08.state=0
	LdLFDK08.State=0
	CheckNombres()
	End If

	If BonusTi=2 and LIndicaLIGO.state=2 and LIndicaFDK.State=2 and LIndicaFNKT.state=0 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0
	LIndicaFNKT.state=2
	LFKT01.state=0
	LFKT02.state=0
	LFKT03.state=0
	LFKT04.state=0
	LFKT05.state=0
	LFKT06.state=0
	LFKT07.state=0
	LFKT08.state=0
	LFKT09.state=0
	LFKT10.state=0
	LFKT11.state=0
	LFKT12.state=0
	CheckNombres()
	End If

	If BonusTi=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=0 and LIndicaFNKT.state=2 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0
	LIndicaLIGO.State=2
	LIGO01.State=0
	LdLIGO01.state=0
	LIGO02.State=0
	LdLIGO02.state=0
	LIGO03.State=0
	LdLIGO03.state=0
	LIGO04.state=0
	LdLIGO04.state=0
	LIndicaFDK.State=2
	LFDK01.state=0
	LdLFDK01.State=0
	LFDK02.state=0
	LdLFDK02.State=0
	LFDK03.State=0
	LdLFDK03.State=0
	LFDK04.state=0
	LdLFDK04.State=0
	LFDK05.state=0
	LdLFDK05.State=0
	LFDK06.state=0
	LdLFDK06.State=0
	LFDK07.state=0
	LdLFDK07.State=0
	LFDK08.state=0
	LdLFDK08.State=0
	CheckNombres()
	End If

	If BonusTi=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=2 and LIndicaFNKT.state=0 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0
	LIndicaLIGO.State=2
	LIGO01.State=0
	LdLIGO01.state=0
	LIGO02.State=0
	LdLIGO02.state=0
	LIGO03.State=0
	LdLIGO03.state=0
	LIGO04.state=0
	LdLIGO04.state=0
	LIndicaFNKT.state=2
	LFKT01.state=0
	LFKT02.state=0
	LFKT03.state=0
	LFKT04.state=0
	LFKT05.state=0
	LFKT06.state=0
	LFKT07.state=0
	LFKT08.state=0
	LFKT09.state=0
	LFKT10.state=0
	LFKT11.state=0
	LFKT12.state=0
	CheckNombres()
	End If

	If BonusTi=2 and LIndicaLIGO.state=2 and LIndicaFDK.State=0 and LIndicaFNKT.state=0 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0
	LIndicaFDK.State=2
	LFDK01.state=0
	LdLFDK01.State=0
	LFDK02.state=0
	LdLFDK02.State=0
	LFDK03.State=0
	LdLFDK03.State=0
	LFDK04.state=0
	LdLFDK04.State=0
	LFDK05.state=0
	LdLFDK05.State=0
	LFDK06.state=0
	LdLFDK06.State=0
	LFDK07.state=0
	LdLFDK07.State=0
	LFDK08.state=0
	LdLFDK08.State=0
	LIndicaFNKT.state=2
	LFKT01.state=0
	LFKT02.state=0
	LFKT03.state=0
	LFKT04.state=0
	LFKT05.state=0
	LFKT06.state=0
	LFKT07.state=0
	LFKT08.state=0
	LFKT09.state=0
	LFKT10.state=0
	LFKT11.state=0
	LFKT12.state=0
	CheckNombres()
	End If

	If BonusTi=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=0 and LIndicaFNKT.state=0  and LDespierto.state=0 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0	
	LIndicaLIGO.State=2
	LIGO01.State=0
	LdLIGO01.state=0
	LIGO02.State=0
	LdLIGO02.state=0
	LIGO03.State=0
	LdLIGO03.state=0
	LIGO04.state=0
	LdLIGO04.state=0
	LIndicaFDK.State=2
	LFDK01.state=0
	LdLFDK01.State=0
	LFDK02.state=0
	LdLFDK02.State=0
	LFDK03.State=0
	LdLFDK03.State=0
	LFDK04.state=0
	LdLFDK04.State=0
	LFDK05.state=0
	LdLFDK05.State=0
	LFDK06.state=0
	LdLFDK06.State=0
	LFDK07.state=0
	LdLFDK07.State=0
	LFDK08.state=0
	LdLFDK08.State=0
	LIndicaFNKT.state=2
	LFKT01.state=0
	LFKT02.state=0
	LFKT03.state=0
	LFKT04.state=0
	LFKT05.state=0
	LFKT06.state=0
	LFKT07.state=0
	LFKT08.state=0
	LFKT09.state=0
	LFKT10.state=0
	LFKT11.state=0
	LFKT12.state=0
	CheckNombres()
	End If
'******************************************************************
'*********** FRANKENBOLA ******************************************
	If BonusTi=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=0 and LIndicaFNKT.state=0  and LDespierto.state=1 Then
	TimerBonusLet.enabled=False
	AddBonusTi (-Bonusti)
	LvamoCast.state=0
	LdLvamoCast.State=0
	LEspCast.state=0
	LEspCast1.state=0
	LEspCast2.state=0	
	LIndicaLIGO.State=2
	LIGO01.State=0
	LdLIGO01.state=0
	LIGO02.State=0
	LdLIGO02.state=0
	LIGO03.State=0
	LdLIGO03.state=0
	LIGO04.state=0
	LdLIGO04.state=0
	LIndicaFDK.State=2
	LFDK01.state=0
	LdLFDK01.State=0
	LFDK02.state=0
	LdLFDK02.State=0
	LFDK03.State=0
	LdLFDK03.State=0
	LFDK04.state=0
	LdLFDK04.State=0
	LFDK05.state=0
	LdLFDK05.State=0
	LFDK06.state=0
	LdLFDK06.State=0
	LFDK07.state=0
	LdLFDK07.State=0
	LFDK08.state=0
	LdLFDK08.State=0
	LIndicaFNKT.state=2
	LFKT01.state=0
	LFKT02.state=0
	LFKT03.state=0
	LFKT04.state=0
	LFKT05.state=0
	LFKT06.state=0
	LFKT07.state=0
	LFKT08.state=0
	LFKT09.state=0
	LFKT10.state=0
	LFKT11.state=0
	LFKT12.state=0
	LFrBall.State=2
	CheckFrankenBola()
	CheckNombres()
	End If
End Sub


Sub CheckFrankenBola()
	If LFrBall.state=0 Then
	'no da la frankenbola
	End If

	If LFrBall.state=2 Then
	'agrega la 4 bola, que es la frankenball esto va en El 
	'drain con un if que dice, si se pierde tercer bola y
	'la luz de frankenbola esta estado=2 en vez de perder 
	'sale la frankenbola
	CheckFrankenballFlsh()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Frankenball.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub TriggerNombr_Hit()
	If LvamoCast.State=0 and LIndicaLIGO.state=2 and LIndicaFDK.State=2 and LIndicaFNKT.state=2 Then
	'playsound
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=2 and LIndicaFNKT.state=2 Then
	AddBonusLV (+1)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=2 and LIndicaFDK.State=0 and LIndicaFNKT.state=2 Then
	AddBonusLV (+2)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=2 and LIndicaFDK.State=2 and LIndicaFNKT.state=0 Then
	AddBonusLV (+3)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=0 and LIndicaFNKT.state=2 Then
	AddBonusLV (+4)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=2 and LIndicaFNKT.state=0 Then
	AddBonusLV (+5)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=2 and LIndicaFDK.State=0 and LIndicaFNKT.state=0 Then
	AddBonusLV (+6)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If

	If LvamoCast.State=2 and LIndicaLIGO.state=0 and LIndicaFDK.State=0 and LIndicaFNKT.state=0 Then
	AddBonusLV (+7)
	CheckBonusLV()
	LuIntdCast.state=1
	TimerFlasero.enabled=True
	CheckRelampagon()
	End If
End Sub
	
Sub CheckBonusLV()
	'de acuerdo al nivel de bonus, sera el premio en puntos, y los
	'puntos seran entregados cuando se pierda la bola
	If BonusLV<1 Then
	'cuando pierde pelota se agregan pts=
	End If

	If BonusLV=1 Then
	AddBonusACU (100000)
	'cuando pierde pelota se agregan pts=
	End If

	If BonusLV=2 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (150000)
	End If

	If BonusLV=3 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (200000)
	End If

	If BonusLV=4 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (250000)
	End If
	
	If BonusLV=5 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (300000)
	End If
	
	If BonusLV=6 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (400000)
	End If

	If BonusLV=7 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (500000)
	End If

	If BonusLV=8 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (750000)
	End If
	
	If BonusLV=9 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (1000000)
	End If

	If BonusLV=10 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (1500000)
	End If

	If BonusLV>10 and BonusLV<21 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (2500000)
	End If

	If BonusLV>20 and BonusLV<31 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (5000000)
	End If

	If BonusLV>30 and BonusLV<41 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (6000000)
	End If

	If BonusLV>40 and BonusLV<51 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (7000000)
	End If

	If BonusLV>50 and BonusLV<61 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (8000000)
	End If

	If BonusLV>60 and BonusLV<71 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (9000000)
	End If

	If BonusLV>70 and BonusLV<81 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (10000000)
	End If

	If BonusLV>80 and BonusLV<91 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (15000000)
	End If

	If BonusLV>90 and BonusLV<101 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (20000000)
	End If

	If BonusLV>100 and BonusLV<151 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (25000000)
	End If
	
	If BonusLV>150 and BonusLV<201 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (30000000)
	End If

	If BonusLV>200 Then
	'cuando pierde pelota se agregan pts=
	AddBonusACU (50000000)
	End If
End Sub

'aca arriba el cartelito de nivel de Bonus
'ylos ptos que da


'****** FALTA HACER SCRIPT DE ENTREGA DE BONUS AL PERDER BOLA Y
'****** EL RESETEO DE PUNTOS DE BONUSLV A 0 QUE ESO SIGNIFICA

'------------------------------------------------------------------
'----------------  Nivel del Bonus   ------------------------------
Sub AddBonusLV(BoLV) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	BonusLV = BonusLV + BoLV ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'---------------- Tiempo de Bonus    ------------------------------
Sub AddBonusTi(Bonti) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	BonusTi = BonusTi + Bonti ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

Sub AddBonusACU(Bonku) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	BonusACU = BonusACU + Bonku ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

'******************************************************************
'************ Despertar a Franky **********************************
Sub Jeringa_Hit()
	If LDespierto.State=0 and LCZF01.State=0 Then
	PlaySound "sedante"
	p1 = p1 + (500*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.State=0 and LCZF01.State=2 Then
	PlaySound "sedante"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	AddFdespertar (-10)
	CheckPuntaje()
	CheckDespertar()
	End If
	
	If LDespierto.State=1 and LAMM01.state=1 and FpieI.Rotz=0=True Then
	PlaySound "sedante"
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	FpieI.Rotz=-10
	AddFdespertar (-10)
	AddIra (-10)
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
	CheckDespertar()
	CheckIRA()
	CheckFparla()
	End If

	If LDespierto.State=1 and LAMM01.state=1 and FpieI.Rotz=10=True Then
	PlaySound "sedante"
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	FpieI.Rotz=-10
	AddFdespertar (-10)
	AddIra (-10)
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
	CheckDespertar()
	CheckIRA()
	CheckFparla()
	End If
	
	If LDespierto.State=1 and LAMM01.state=1 and FpieI.Rotz=-10=True Then
	PlaySound "sedante"
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	FpieI.Rotz=10
	AddFdespertar (-10)
	AddIra (-10)
	AddTurba (-1)
	CheckPuntaje()
	CheckTurr ()
	CheckDespertar()
	CheckIRA()
	CheckFparla()
	End If
End Sub

Sub CheckDespertar()
	If Fdespertar>99 Then
	PlaySound "despiert"
	LDespierto.State=1
	CheckDesperto()
	End If
	
	If LDespierto.state=1 and Fdespertar<50 Then
	LDespierto.State=0
	CheckDesperto()
	End If

End Sub

'---------- Animacion de sentarse/acostarse --------------------------

Sub CheckDesperto()
	If LDespierto.State=1 and LAMM01.State=0 and LSentado.State=0 Then
	TimerSienta.enabled=True
	End If

	If LDespierto.State=0 and LSentado.State=1 Then
	TimerAcuesta.enabled=True
	End If
End Sub

Sub TimerSienta_Timer()
	AddSentar (+1)
	CheckTSienta()
End Sub

Sub TimerAcuesta_Timer()
	AddSentar (-1)
	CheckTSienta()
End Sub


Sub CheckTSienta()
	'---0
	If sentar=0 Then
	FCabeza.RotX=90
	FQuijada.RotX=90
	FTorso.RotX=90
	FBrazoD.RotX=90
	FBrazoI.RotX=90
	FAntBrazoD.RotX=90
	FAntBrazoI.RotX=90
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---1
	If sentar=1 Then
	PlaySound "sesienta"
	FCabeza.RotX=85
	FQuijada.RotX=85
	FTorso.RotX=85
	FBrazoD.RotX=85
	FBrazoI.RotX=85
	FAntBrazoD.RotX=85
	FAntBrazoI.RotX=85
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---2
	If sentar=2 Then
	FCabeza.RotX=80
	FQuijada.RotX=80
	FTorso.RotX=80
	FBrazoD.RotX=80
	FBrazoI.RotX=80
	FAntBrazoD.RotX=80
	FAntBrazoI.RotX=80
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---3
	If sentar=3 Then
	FCabeza.RotX=75
	FQuijada.RotX=75
	FTorso.RotX=75
	FBrazoD.RotX=75
	FBrazoI.RotX=75
	FAntBrazoD.RotX=75
	FAntBrazoI.RotX=75
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---4
	If sentar=4 Then
	FCabeza.RotX=70
	FQuijada.RotX=70
	FTorso.RotX=70
	FBrazoD.RotX=70
	FBrazoI.RotX=70
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---5
	If sentar=5 Then
	FCabeza.RotX=65
	FQuijada.RotX=65
	FTorso.RotX=65
	FBrazoD.RotX=65
	FBrazoI.RotX=65
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---6
	If sentar=6 Then
	FCabeza.RotX=60
	FQuijada.RotX=60
	FTorso.RotX=60
	FBrazoD.RotX=60
	FBrazoI.RotX=60
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---7
	If sentar=7 Then
	FCabeza.RotX=55
	FQuijada.RotX=55
	FTorso.RotX=55
	FBrazoD.RotX=55
	FBrazoI.RotX=55
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---8
	If sentar=8 Then
	FCabeza.RotX=50
	FQuijada.RotX=50
	FTorso.RotX=50
	FBrazoD.RotX=50
	FBrazoI.RotX=50
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---9
	If sentar=9 Then
	FCabeza.RotX=45
	FQuijada.RotX=45
	FTorso.RotX=45
	FBrazoD.RotX=45
	FBrazoI.RotX=45
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---10
	If sentar=10 Then
	FCabeza.RotX=40
	FQuijada.RotX=40
	FTorso.RotX=40
	FBrazoD.RotX=40
	FBrazoI.RotX=40
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---11
	If sentar=11 Then
	FCabeza.RotX=35
	FQuijada.RotX=35
	FTorso.RotX=35
	FBrazoD.RotX=35
	FBrazoI.RotX=35
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---12
	If sentar=12 Then
	FCabeza.RotX=30
	FQuijada.RotX=30
	FTorso.RotX=30
	FBrazoD.RotX=30
	FBrazoI.RotX=30
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---13
	If sentar=13 Then
	FCabeza.RotX=25
	FQuijada.RotX=25
	FTorso.RotX=25
	FBrazoD.RotX=25
	FBrazoI.RotX=25
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---14
	If sentar=14 Then
	FCabeza.RotX=20
	FQuijada.RotX=20
	FTorso.RotX=20
	FBrazoD.RotX=20
	FBrazoI.RotX=20
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---15
	If sentar=15 Then
	FCabeza.RotX=15
	FQuijada.RotX=15
	FTorso.RotX=15
	FBrazoD.RotX=20
	FBrazoI.RotX=20
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---16
	If sentar=16 Then
	FCabeza.RotX=10
	FQuijada.RotX=10
	FTorso.RotX=10
	FBrazoD.RotX=20
	FBrazoI.RotX=20
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---17
	If sentar=17 Then
	FCabeza.RotX=5
	FQuijada.RotX=5
	FTorso.RotX=5
	FBrazoD.RotX=20
	FBrazoI.RotX=20
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
	'---18
	If sentar=18 Then
	FCabeza.RotX=0
	FQuijada.RotX=0
	FTorso.RotX=0
	FBrazoD.RotX=20
	FBrazoI.RotX=20
	FAntBrazoD.RotX=70
	FAntBrazoI.RotX=70
	CheckTSientaFinalS()
	CheckTSientaFinalA()
	End If
End Sub

Sub CheckTSientaFinalS()
	If Sentar=18 Then
	Lsentado.State=1
	TimerSienta.enabled=False
	End If
End Sub

Sub CheckTSientaFinalA()
	If Sentar=0 Then
	Lsentado.State=0
	TimerAcuesta.enabled=False
	End If
End Sub

'**************** FRANKY SIGUE BOLA CON LA MIRADA ***************
'---------- animacion de Seguir bola con cabeza y quijada -------
'un radio de trigers sobre la tabla moveran las partes

Sub TCabeza01_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= 55
	FQuijada.RotZ= 55
	End If
End Sub

Sub TCabeza02_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= 45
	FQuijada.RotZ= 45
	End If
End Sub

Sub TCabeza03_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= 35
	FQuijada.RotZ= 35
	End If
End Sub

Sub TCabeza04_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= 25
	FQuijada.RotZ= 25
	End If
End Sub

Sub TCabeza05_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= 15
	FQuijada.RotZ= 15
	End If
End Sub

Sub TCabeza06_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= 5
	FQuijada.RotZ= 5
	End If
End Sub

Sub TCabeza07_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= -5
	FQuijada.RotZ= -5
	End If
End Sub

Sub TCabeza08_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= -15
	FQuijada.RotZ= -15
	End If
End Sub

Sub TCabeza09_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= -25
	FQuijada.RotZ= -25
	End If
End Sub

Sub TCabeza10_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= -35
	FQuijada.RotZ= -35
	End If
End Sub

Sub TCabeza11_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= -45
	FQuijada.RotZ= -45
	End If
End Sub

Sub TCabeza12_Hit()
	If LDespierto.state=1 and Lsentado.state=1 Then
	FCabeza.RotZ= -55
	FQuijada.RotZ= -55
	End If
End Sub




'------------------------------------------------------------------
'----------------Puntos de despertar-------------------------------
Sub AddFdespertar(Fdespi) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	Fdespertar = Fdespertar + Fdespi ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'----------------- para animacion sentado/acostado------------------
Sub AddSentar(cntar) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	Sentar = Sentar + cntar ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'******************************************************************


'******************************************************************
'*******************  Modos de Ira ********************************

Sub CheckIRA()
	'Agregar si tiene candados, menos de 99 pero 
	'numero alto los va soltando
	'agregar este check en donde la Luz Lamm queda prendida
	If IRA<75 Then
	LIRA.State=0
	CheckBronka()
	CheckIRAFlas()
	End If

	If IRA>74 and IRA<90  And LAMM03.State=0 Then
	LIRA.State=0
	PlaySound "grr"
	CheckBronka()
	CheckIRAFlas()
	End If

	If IRA>74 and IRA<90 And LAMM03.State=1 Then
	PlaySound "grr"
	LAMM03.State=0
	agarradera2.RotZ= -300
	agarradera2.Visible=False
	LIRA.State=0
	CheckBronka()
	CheckIRAFlas()
	End If

	If IRA>89 and IRA<100 and LAMM02.State=0 and LAMM03.state=0 Then
	LIRA.State=0
	PlaySound "grr"
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If IRA>89 and IRA<100 and LAMM02.State=1 and LAMM03.state=0 Then
	PlaySound "grr"
	LAMM02.State=0
	agarradera1.RotZ= -300
	agarradera1.Visible=False
	LIRA.State=0
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	
	If IRA>89 and IRA<100 and LAMM02.State=1 and LAMM03.state=1 Then
	PlaySound "grr"
	LAMM02.State=0
	agarradera1.RotZ= -300
	agarradera1.Visible=False
	LAMM03.State=0
	agarradera2.RotZ= -300
	agarradera2.Visible=False
	LIRA.State=0
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If IRA>99 and LAMM01.State=0 and LAMM02.State=0 and LAMM03.state=0 and Lsentado.state=0 Then
	PlaySound "gritobronca"
	LIRA.State=1
	Lsentado.State=1
	p1 = p1 + (100000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckDesperto()
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If IRA>99 and LAMM01.State=1 and LAMM02.State=0 and LAMM03.state=0 and Lsentado.state=0 Then
	LAMM01.State=0
	PlaySound "gritobronca"
	agarradera.RotZ= -300
	agarradera.Visible=False
	LIRA.State=1
	Lsentado.State=1
	p1 = p1 + (100000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	CheckDesperto()
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If IRA>99 and LAMM01.State=1 and LAMM02.State=1 and LAMM03.state=0 and Lsentado.state=0 Then
	LAMM01.State=0
	PlaySound "gritobronca"
	agarradera.RotZ= -300
	agarradera.Visible=False
	LAMM02.State=0
	agarradera1.RotZ= -300
	agarradera1.Visible=False
	LIRA.State=1
	Lsentado.State=1
	p1 = p1 + (100000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	CheckDesperto()
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If

	If IRA>99 and LAMM01.State=1 and LAMM02.State=1 and LAMM03.state=1 and Lsentado.state=0 Then
	LAMM01.State=0
	PlaySound "gritobronca"
	agarradera.RotZ= -300
	agarradera.Visible=False
	LAMM02.State=0
	agarradera1.RotZ= -300
	agarradera1.Visible=False
	LAMM03.State=0
	agarradera2.RotZ= -300
	agarradera2.Visible=False
	LIRA.State=1
	Lsentado.State=1
	p1 = p1 + (100000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
	CheckDesperto()
	CheckBronka()
	CheckIRAFlas()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub CheckBronka()
	If LIRA.State=1 Then
	TimerDIRA.enabled=True
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Ira.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
	If LIRA.State=0 Then
	TimerDIRA.enabled=False
	End If
End Sub

Sub TimerDIRA_Timer()
	If Lira.State=1 Then
	p1= (p1 -20000)
	OnScoreboardChanged
	CheckPuntaje()
	End If
End Sub

'------------------------------------------------------------------
'---------------- Contador de Ira -------------------------------
Sub AddIRA(Fira) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	IRA = IRA + Fira ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'******************************************************************
'******************************************************************
'***************** Sistema ESPECIAL de las 5 **********************
Sub CheckEspecial()
	If LESP01.State=1 and LESP02.State=1 and LESP03.State=1 and LESP04.State=1 and LESP05.State=1 Then
	LEspecialA.State=1
	Playsound "alinchar"
	'luces especiales
	AldeanoP.IsDropped=False
	AldeanoP1.IsDropped=False
	AldeanoP2.IsDropped=False
	AldeanoP3.IsDropped=False
	AldeanoP4.IsDropped=False
	AldeanoP5.IsDropped=False
	AldeanoP6.IsDropped=False
	AldeanoP7.IsDropped=False
	AldeanoP8.IsDropped=False
	AldeanoP9.IsDropped=False
	TimerEspecial.enabled=True
	CheckESpecial5()
	End If
End Sub

Sub TimerEspecial_Timer()
	If LEspecialA.State=1 and AldeanoP.IsDropped=True and AldeanoP1.IsDropped=True and AldeanoP2.IsDropped=True and AldeanoP3.IsDropped=True and AldeanoP4.IsDropped=True and AldeanoP5.IsDropped=True and AldeanoP6.IsDropped=True and AldeanoP7.IsDropped=True and AldeanoP8.IsDropped=True and AldeanoP9.IsDropped=True Then
	LEspecialA.State=0
	TimerEspecial.enabled=False
	Credits +1
	KickerCuevaDel.Createball
	KickerCuevaPost.Createball
	KickerCast.Createball
	KickerCuevaDel.kick 165, 7
	KickerCuevaPost.kick 315, 20
	KickerCast.kick 230, 25
	Lmultiball.state=1
		
	DMD_DisplaySceneEx "MultiBall.png", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2600, UltraDMD_Animation_ScrollOffDown
	Timer1sDMD.Enabled=true



	
		Elseif LEspecialA.State=1 and AldeanoP.IsDropped=False or AldeanoP1.IsDropped=False or AldeanoP2.IsDropped=False or AldeanoP3.IsDropped=False or AldeanoP4.IsDropped=False or AldeanoP5.IsDropped=False or AldeanoP6.IsDropped=False or AldeanoP7.IsDropped=False or AldeanoP8.IsDropped=False or AldeanoP9.IsDropped=False Then
	LEspecialA.State=0
	TimerEspecial.enabled=False
	PlaySound "Fmuere"
	AddPlanka (-Planka)
	LCBT01.State=0
	LdLCBT01.State=0
	LSWM01.State=0
	LdLSWM01.State=0
	LSWM02.State=0
	LdLSWM02.State=0
	LSWM03.State=0
	LdLSWM03.State=0
	LTIM01.State=0
	LdLTIM01.State=0
	AddVolt (-Volt)
	PSwitch1.RotX=90
	PSwitch2.RotX=90
	PSwitch3.RotX=90
	LPAL01.State=0
	LdLPAL01.State=0
	LAlarm.State=0
	LAlarm1.State=0
	LAlarm2.State=0
	LCZF01.State=0
	LdLCZF01.State=0
	LMES01.State=0
	FCabeza.Visible=False
	FQuijada.Visible=False
	FTorso.Visible=False
	FPelvis.Visible=False
	FBrazoD.Visible=False
	FBrazoI.Visible=False
	FAntBrazoD.Visible=False
	FAntBrazoI.Visible=False
	FManoD.Visible=False
	FManoI.Visible=False
	FPiernaD.Visible=False
	FPiernaI.Visible=False
	FAntPiernaD.Visible=False
	FAntPiernaI.Visible=False
	FPieD.Visible=False
	FPieI.Visible=False
	jarroCereb3.Visible=False
	LECF01.State=0
	LECF02.State=0
	LECF03.State=0
	LECF04.State=0
	LECF05.State=0
	LECF06.State=0
	LECF07.State=0
	LECF08.State=0
	LECF09.State=0
	LECF10.State=0
	LECF11.State=0
	LECF12.State=0
	LECF13.State=0
	LECF14.State=0
	LECF15.State=0
	LECF16.State=0
	LCHD01.State=0
	LdLCHD01.State=0
	LCAN01.State=0
	LdLCAN01.State=0
	LCCO01.State=0
	LdLCCO01.State=0
	Tcereb1.IsDropped=False
	Tcereb2.IsDropped=False
	Tcereb3.IsDropped=False
	LRLPG01.State=0
	LRLPG02.State=0
	LRLPG03.State=0

	LRPI01.State=0
	LdLRPI01.State=0
	LRMD01.State=0
	LdLRMD01.State=0
	LRMI01.State=0
	LdLRMI01.State=0
	LRDP01.State=0
	LdLRDP01.State=0
	AddPartes (-Partes)
	AddPartesM (-PartesM)
	AldeanoP.IsDropped=True
	AldeanoP1.IsDropped=True
	AldeanoP2.IsDropped=True
	AldeanoP3.IsDropped=True
	AldeanoP4.IsDropped=True
	AldeanoP5.IsDropped=True
	AldeanoP6.IsDropped=True
	AldeanoP7.IsDropped=True
	AldeanoP8.IsDropped=True
	AldeanoP9.IsDropped=True
	LMulX2.State=0
	LdLMulX2.State=0
	LMulX3.State=0
	LdLMulX3.State=0
	LMulX4.State=0
	LdLMulX4.State=0
	LMulX5.State=0
	LdLMulX5.State=0
	LMulX6.State=0
	LdLMulX6.State=0
	p1 = p1 - (500000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
		If rendor=0 Then
		Addrendor (20)
		Trendor.enabled=True
		DMD_DisplaySceneEx "Frankymuerelek.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2600, UltraDMD_Animation_ScrollOffDown
		Timer1sDMD.Enabled=true
		End If
	End If
End Sub

Sub AldeanoP_Init()
	AldeanoP.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP_Hit()	
	AldeanoP.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
	
Sub AldeanoP1_Init()
	AldeanoP1.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP1_Hit()	
	AldeanoP1.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP2_Init()
	AldeanoP2.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP2_Hit()	
	AldeanoP2.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP3_Init()
	AldeanoP3.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP3_Hit()	
	AldeanoP3.IsDropped=True
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP4_Init()
	AldeanoP4.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP4_Hit()	
	AldeanoP4.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP5_Init()
	AldeanoP5.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP5_Hit()	
	AldeanoP5.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP6_Init()
	AldeanoP6.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP6_Hit()	
	AldeanoP6.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP7_Init()
	AldeanoP7.IsDropped=True
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	CheckPuntaje()	
End Sub
Sub AldeanoP7_Hit()	
	AldeanoP7.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP8_Init()
	AldeanoP8.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP8_Hit()	
	AldeanoP8.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub

Sub AldeanoP9_Init()
	AldeanoP9.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub
Sub AldeanoP9_Hit()	
	AldeanoP9.IsDropped=True
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
End Sub


'---------------------------------------------------------------------
'*********************************************************************************
'******************* minimision brain twist *************************************
Sub TBrainTwist_Hit()
	If LCZF01.State=0 Then
	PlaySound "round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LDespierto.state=1 and LCZF01.State=2 Then
	PlaySound "1respuesta"
	LBrainTwist.State=1
	LdLBrainTwist.State=1
	LBTwist01.state=2
	LBTwist02.State=2
	p1 = p1 + (2000*MultiPts) 
	OnScoreboardChanged
	CheckTwist()
	CheckPuntaje()
	End If

	If LBrainTwist.State=2 Then
	PlaySound "Twistcerebral"
	LBrainTwist.State=0
	LdLBrainTwist.State=0
	LBTwist01.state=0
	LBTwist02.State=0
	LbrainL.State=0
	LbrainR.state=0
	LTBrainL.state=0
	LdLTBrainL.state=0
	LTBrainR.state=0
	LdLTBrainR.state=0
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	AddIra (-100)
	AddTurba (-20)
	CheckTurr ()
	CheckIRA()
	CheckTwist()
	CheckPuntaje()
	CheckFparla()
	End If
End Sub

Sub TBrainR_Hit()
	If LBrainTwist.State=0 Then
	PlaySound "round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	CheckPuntaje()
	End If

	If LBrainTwist.State=2 Then
	PlaySound "cerebros"
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckPuntaje()
	End If

	If LBrainTwist.State=1 Then
	PlaySound "electrotwist"
	LTBrainL.state=1
	LdLTBrainL.state=1
	LbrainL.State=1
	p1 = p1 + (5000*MultiPts) 
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckTwist()
	'PlaySound
	CheckPuntaje()
	End If
End Sub

Sub TBrainL_Hit()
	If LBrainTwist.State=0 Then
	PlaySound "round"
	p1 = p1 + (1000*MultiPts) 
	OnScoreboardChanged
	End If

	If LBrainTwist.State=2 Then
	PlaySound "cerebros"
	p1 = p1 + (1000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	End If

	If LBrainTwist.State=1 Then
	PlaySound "electrotwist"
	LTBrainR.state=1
	LdLTBrainR.state=1
	LbrainR.State=1
	p1 = p1 + (5000*MultiPts)
	OnScoreboardChanged
	AddTurba (-1)
	CheckTurr ()
	CheckTwist()
	CheckPuntaje()
	'PlaySound
	End If
End Sub

Sub CheckTwist()
	If LBrainTwist.State=1 and LTBrainL.State=1 and LTBrainR.state=1 and LBTwist01.state=2 and LBTwist02.state=2 and LbrainL.state=1 and LbrainR.state=1 Then
	PlaySound "eltwist"
	LBrainTwist.state=2
	LdLBrainTwist.State=2
			If rendor=0 Then
			Addrendor (20)
			Trendor.enabled=True
			DMD_DisplaySceneEx "BrTwist.gif", "", -1, 5, "", -1, -1, UltraDMD_Animation_ScrollOnRight, 2000, UltraDMD_Animation_ScrollOffDown
			Timer1sDMD.Enabled=true
			End If
	End If
End Sub


'agregar lo de la bola lokeada
'agregar el check y el sistema de cuadro con topes de puntos
'agregar los flashers

'************************************************************************************
'**************** ESQUEMA CUADRO DE PUNTOS ***************************

Sub CheckPuntaje()

	'Cada luz que queda fija hace que franky si esta 
	'despierto diga una frase y la frase depende de 
	'si tiene mucha o poca ira
	If p1<250000 Then
	L250K.state=2
	L500K.state=0
	L1M.state=0
	L10M.state=0
	L50M.state=0
	L100M.state=0
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If
	'250mil     premio
	If p1>249999 and p1<500001 Then
	L250K.state=1	
	L500K.state=2
	L1M.state=0
	L10M.state=0
	L50M.state=0
	L100M.state=0
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If
	'500mil     premio
	If p1>500000 and p1<1000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=2
	L10M.state=0
	L50M.state=0
	L100M.state=0
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If
	'1millon    premio
	If p1>1000000 and p1<10000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=2
	L50M.state=0
	L100M.state=0
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If

	'10millon   premio
	If p1>10000000 and p1<50000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=1
	L50M.state=2
	L100M.state=0
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If

	'50millon   premio
	If p1>50000000 and p1<100000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=1
	L50M.state=1
	L100M.state=2
	L250M.state=0
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If

	'100millon  premio
	If p1>100000000 and p1<250000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=1
	L50M.state=1
	L100M.state=1
	L250M.state=2
	L500M.state=0
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If

	'250millon  premio
	If p1>250000000 and p1<500000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=1
	L50M.state=1
	L100M.state=1
	L250M.state=1
	L500M.state=2
	Lfree.state=0
	LGame.state=0
	CheckFparla()
	End If

	'500millon  premio
	If p1>500000000 and p1<600000000 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=1
	L50M.state=1
	L100M.state=1
	L250M.state=1
	L500M.state=1
	Lfree.state=2
	LGame.state=2
	CheckFparla()
	End If

	'free game  premio
	If p1>600000001 Then
	L250K.state=1	
	L500K.state=1
	L1M.state=1
	L10M.state=1
	L50M.state=1
	L100M.state=1
	L250M.state=1
	L500M.state=1
	Lfree.state=1
	LGame.state=1
	credits +1
	CheckFparla()
	End If
End Sub

'****************** Comentario de FRANKY (habla) **********************************************************************
Sub CheckFparla()
    '1
	If L250K.state=1 and L500K.state=0 and L1M.state=0 and L10M.state=0 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa01"
	TimerBoca.enabled=true
	End If
	'2
	If L250K.state=1 and L500K.state=0 and L1M.state=0 and L10M.state=0 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu01"
	TimerBoca.enabled=true
	End If
	'3**********************
	If L250K.state=1 and L500K.state=1 and L1M.state=0 and L10M.state=0 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa02"
	TimerBoca.enabled=true
	End If
	'4
	If L250K.state=1 and L500K.state=1 and L1M.state=0 and L10M.state=0 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu02"
	TimerBoca.enabled=true
	End If
	'5**********************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=0 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa03"
	TimerBoca.enabled=true
	End If
	'6
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=0 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu03"
	TimerBoca.enabled=true
	End If
	'7**********************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa04"
	TimerBoca.enabled=true
	End If
	'8
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=0 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu01"
	TimerBoca.enabled=true
	End If
	'9**********************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa03"
	TimerBoca.enabled=true
	End If
	'10
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=0 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu04"
	TimerBoca.enabled=true
	End If
	'11*********************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa05"
	TimerBoca.enabled=true
	End If
	'12
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=0 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu5"
	TimerBoca.enabled=true
	End If
	'13********************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=1 and L500M.state=0 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa06"
	TimerBoca.enabled=true
	End If
	'14
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=1 and L500M.state=0 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu06"
	TimerBoca.enabled=true
	End If
	'15*******************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=1 and L500M.state=1 and Lfree.state=0 and ira>50 Then
	Playsound "ComMa07"
	TimerBoca.enabled=true
	End If
	'16
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=1 and L500M.state=1 and Lfree.state=0 and ira<51 Then
	Playsound "ComBu07"
	TimerBoca.enabled=true
	End If
	'17	*******************
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=1 and L500M.state=1 and Lfree.state=1 and ira>50 Then
	Playsound "ComMa08"
	TimerBoca.enabled=true
	End If
	'18
	If L250K.state=1 and L500K.state=1 and L1M.state=1 and L10M.state=1 and L50M.state=1 and L100M.state=1 and L250M.state=1 and L500M.state=1 and Lfree.state=1 and ira<51 Then
	Playsound "ComBu08"
	TimerBoca.enabled=true
	End If
	
End Sub

Sub TimerBoca_Timer()
	AddBocaA (+1)
	CheckLengua()
	CheckPera()
End Sub

Sub CheckPera()
	If BocaA=0 and TimerBoca.enabled=true Then
	FQuijada.TransZ=0
	End If

	If BocaA=1 and TimerBoca.enabled=true Then
	FQuijada.TransZ=1
	End If

	If BocaA=2 and TimerBoca.enabled=true Then
	FQuijada.TransZ=2
	End If

	If BocaA=3 and TimerBoca.enabled=true Then
	FQuijada.TransZ=3
	End If

	If BocaA=4 and TimerBoca.enabled=true Then
	FQuijada.TransZ=4
	End If

	If BocaA=5 and TimerBoca.enabled=true Then
	FQuijada.TransZ=5
	End If

	If BocaA=6 and TimerBoca.enabled=true Then
	FQuijada.TransZ=6
	End If

	If BocaA=7 and TimerBoca.enabled=true Then
	FQuijada.TransZ=7
	End If
End Sub

Sub CheckLengua()
	If BocaA=7 Then
	AddBocaA (-BocaA)
	TimerBoca.enabled=False
	End If
End Sub
'----------------------------------------------------------------
Sub AddBocaA(B0ca) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	BocaA = BocaA + B0ca ' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub
'****************************************************************
'****************************************************************



'****************************************************************
'**************  FLASHERS de eventos ****************************

'-----AMARILLO== 1 x     Cuando Se Activa el 3r Switch de Tapa(blink)
'-----AMARILLO== 2 x     Turba Iracunda
'---------AZUL== 3 x     Especial5
'---------ROJO== 4 x     Modo IRA
'---------AZUL== 5 x     Cuando se descarga elect a frank y vive
'-------BLANCO== 6      Relampagos (blink)
'--------VERDE== 7      Frankenball
'-------OSCURO== 8      Modo RITZ

'---------------------------------------------------------------
Sub AddFlashin(Flas1) 'we also need to Dim Score in the begining of script. all Variables should go in the begining of script.
	Flashin = Flashin + Flas1' This add your score + the points in the (Brackets) when something is hit & it contains AddScore(#)
End Sub

'---------------------------------------------------------------
Sub TimerFlasero_Timer()
	AddFlashin (+1)
	CheckSuich3Flash()
	CheckIRAFlasFin()
	Checkpalancaflash()
	CheckRelampagon()
End Sub

'**** (1) ****
Sub CheckSuich3Flash()
	If Flashin=0 and TimerFlasero.enabled=True Then
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If

	If Flashin=1 and TimerFlasero.enabled=True Then
	FlasherAZU.visible=False
	FlasherAMA.visible=True
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-----------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=True
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=False
	'-----------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=True
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=2
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=2
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=2
	LuzTopAMA1.state=2
	LuzTopAMA2.state=2
	LuzTopAMA3.state=2
	LuzTopAMA4.state=2
	LuzTopAMA5.state=2
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If

	If Flashin=2 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=False
	End If

	If Flashin=3 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=True
	End If

	If Flashin=4 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=False
	End If

	If Flashin=5 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=True
	End If

	If Flashin=6 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=False
	End If

	If Flashin=7 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=True
	End If

	If Flashin=8 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=False
	End If

	If Flashin=8 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=True
	End If
	
	If Flashin=10 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=False
	End If

	If Flashin=11 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=True
	End If

	If Flashin=12 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=False
	End If

	If Flashin=13 and TimerFlasero.enabled=True Then
	FlasherAMA.visible=True
	End If

	If Flashin=14 and TimerFlasero.enabled=True Then
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	TimerFlasero.enabled=False
	AddFlashin (-Flashin)
	End If
End Sub

'**** (2) ****

Sub CheckTurbaFlash()
	If Flashin=0 and LTQA01.State=1 and TimerFlasero.enabled=true Then
    FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If

	If Flashin>0 and LTQA01.State=1 and TimerFlasero.enabled=true Then
	FlasherAZU.visible=False
	FlasherAMA.visible=True
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=True
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=True
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=1
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=1
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=1
	LuzTopAMA1.state=1
	LuzTopAMA2.state=1
	LuzTopAMA3.state=1
	LuzTopAMA4.state=1
	LuzTopAMA5.state=1
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If

	If Flashin>0 and LTQA01.State=1 and TimerFlasero.enabled=False Then
    FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	AddFlashin (-Flashin)
	End If
End Sub

'**** (3) ****

Sub CheckEspecial5()
	If LEspecialA.State=0 Then
	FlasherAZU.visible=False
	DifusorDerAZU.Visible=False
	DifusorIzqAZU.Visible=False
	LDifDerAZU.state=0
	LDifIzqAZU.state=0
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	End If

	If LEspecialA.State=1 Then
	FlasherAZU.visible=True
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=True
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqAZU.Visible=True
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=1
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=1
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=1
	LuzTopAZU1.state=1
	LuzTopAZU2.state=1
	LuzTopAZU3.state=1
	LuzTopAZU4.state=1
	LuzTopAZU5.state=1
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If
End Sub

'**** (4) ****

Sub CheckIRAFlas()
	If LIRA.state=1 and flashin=1 Then
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=True
	FlasherROJ.opacity=0
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=true
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=True
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=1
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=1
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=1
	LuzTopROJ1.state=1
	LuzTopROJ2.state=1
	LuzTopROJ3.state=1
	LuzTopROJ4.state=1
	LuzTopROJ5.state=1
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=2 Then
	FlasherROJ.opacity=5
	End If

	If LIRA.state=1 and flashin=3 Then
	FlasherROJ.opacity=10
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=4 Then
	FlasherROJ.opacity=15
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=5 Then
	FlasherROJ.opacity=20
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=6 Then
	FlasherROJ.opacity=25
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=7 Then
	FlasherROJ.opacity=30
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=8 Then
	FlasherROJ.opacity=35
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=9 Then
	FlasherROJ.opacity=40
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=10 Then
	FlasherROJ.opacity=45
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=11 Then
	FlasherROJ.opacity=50
	CheckIRAFlasFin()
	End If
End Sub

	Sub CheckIRAFlas()
	If LIRA.state=1 and flashin=12 Then
	FlasherROJ.opacity=55
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=13 Then
	FlasherROJ.opacity=60
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=14 Then
	FlasherROJ.opacity=65
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=15 Then
	FlasherROJ.opacity=70
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=16 Then
	FlasherROJ.opacity=75
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=17 Then
	FlasherROJ.opacity=80
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=18 Then
	FlasherROJ.opacity=85
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=19 Then
	FlasherROJ.opacity=90
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=22 Then
	FlasherROJ.opacity=95
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=21 Then
	FlasherROJ.opacity=100
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=22 Then
	FlasherROJ.opacity=100
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=23 Then
	FlasherROJ.opacity=95
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=24 Then
	FlasherROJ.opacity=90
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=25 Then
	FlasherROJ.opacity=85
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=26 Then
	FlasherROJ.opacity=80
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=27 Then
	FlasherROJ.opacity=75
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=28 Then
	FlasherROJ.opacity=70
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=29 Then
	FlasherROJ.opacity=65
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=30 Then
	FlasherROJ.opacity=60
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=31 Then
	FlasherROJ.opacity=55
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=32 Then
	FlasherROJ.opacity=50
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=33 Then
	FlasherROJ.opacity=45
	CheckIRAFlasFin()
	End If
End Sub

	Sub CheckIRAFlas()
	If LIRA.state=1 and flashin=34 Then
	FlasherROJ.opacity=40
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=35 Then
	FlasherROJ.opacity=35
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=36 Then
	FlasherROJ.opacity=30
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=37 Then
	FlasherROJ.visible=True
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=38 Then
	FlasherROJ.opacity=20
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=39 Then
	FlasherROJ.opacity=15
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=40 Then
	FlasherROJ.opacity=10
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=41 Then
	
	FlasherROJ.opacity=5
	CheckIRAFlasFin()
	End If

	If LIRA.state=1 and flashin=42 Then
	FlasherROJ.opacity=0
	CheckIRAFlasFin()
	End If

End Sub

Sub CheckIRAFlasFin()
	If Flashin=42 and Lira.state=1 Then
	AddFlashin (-Flashin)
	End if
	
	If Flashin=42 and Lira.state=0 Then
	AddFlashin (-Flashin)
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherROJ.opacity=95
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=true
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	TimerFlasero.enabled=false
	CheckIRAFlasFin()
	End if
End Sub

'**** (5) ****

Sub Checkpalancaflash()

If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and flashin=1 Then
	FlasherAZU.visible=True
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherAZu.opacity=0
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=true
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqAZU.Visible=True
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=1
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=1
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=1
	LuzTopAZU1.state=1
	LuzTopAZU2.state=1
	LuzTopAZU3.state=1
	LuzTopAZU4.state=1
	LuzTopAZU5.state=1
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=2 Then
	FlasherAZu.opacity=5
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=3 Then
	FlasherAZu.opacity=10
	CheckelecoFlasFin()
	End If
	
	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=4 Then
	FlasherAZu.opacity=15
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=5 Then
	FlasherAZu.opacity=20
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=6 Then
	FlasherAZu.opacity=25
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=7 Then
	FlasherAZu.opacity=30
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=8 Then
	FlasherAZu.opacity=35
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=9 Then
	FlasherAZu.opacity=40
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=10 Then
	FlasherAZu.opacity=45
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=11 Then
	FlasherAZu.opacity=55
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=12 Then
	FlasherAZu.opacity=60
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=13 Then
	FlasherAZu.opacity=65
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=14 Then
	FlasherAZu.opacity=70
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=15 Then
	FlasherAZu.opacity=75
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=16 Then
	FlasherAZu.opacity=80
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=17 Then
	FlasherAZu.opacity=85
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=18 Then
	FlasherAZu.opacity=90
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=19 Then
	FlasherAZu.opacity=95
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=20 Then
	FlasherAZu.opacity=100
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=21 Then
	FlasherAZu.opacity=100
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=22 Then
	FlasherAZu.opacity=100
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=23 Then
	FlasherAZu.opacity=100
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=24 Then
	FlasherAZu.opacity=100
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=25 Then
	FlasherAZu.opacity=95
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=26 Then
	FlasherAZu.opacity=90
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=27 Then
	FlasherAZu.opacity=85
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=28 Then
	FlasherAZu.opacity=80
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=29 Then
	FlasherAZu.opacity=75
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=30 Then
	FlasherAZu.opacity=70
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=31 Then
	FlasherAZu.opacity=65
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=32 Then
	FlasherAZu.opacity=60
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=33 Then
	FlasherAZu.opacity=55
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=34 Then
	FlasherAZu.opacity=50
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=35 Then
	FlasherAZu.opacity=45
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=36 Then
	FlasherAZu.opacity=40
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=37 Then
	FlasherAZu.opacity=35
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=38 Then
	FlasherAZu.opacity=30
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=39 Then
	FlasherAZu.opacity=25
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=40 Then
	FlasherAZu.opacity=20
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=41 Then
	FlasherAZu.opacity=15
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=42 Then
	FlasherAZu.opacity=10
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=43 Then
	FlasherAZu.opacity=5
	CheckelecoFlasFin()
	End If

	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and  flashin=44 Then
	FlasherAZu.opacity=0
	CheckelecoFlasFin()
	End If

End Sub

Sub CheckelecoFlasFin()
	If Planka=10 and LPAL01.State=1 and TimerFlasero.enabled=true and flashin=44 Then
	AddFlashin (-Flashin)
	End if
	
	If Flashin=44 and Planka<10 and LPAL01.State=0 and TimerFlasero.enabled=false Then
	AddFlashin (-Flashin)
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherAZU.opacity=95
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=true
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End if
End Sub

'**** (6) ****

Sub CheckRelampagon()
	If LuIntdCast.state=1 and flashin=1 Then
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherAZU.opacity=95
	FlasherVER.visible=False
	FlasherBLA.visible=True
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=True
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=True
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=2
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=2
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=2
	LuzTopBLA1.state=2
	LuzTopBLA2.state=2
	LuzTopBLA3.state=2
	LuzTopBLA4.state=2
	LuzTopBLA5.state=2
	End If

	If LuIntdCast.state=1 and flashin=2 Then
	FlasherBLA.visible=False
	'-------------------
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	End If

	If LuIntdCast.state=1 and flashin=3 Then
	FlasherBLA.visible=True
	'-------------------
	DifusorDerBLA.Visible=True
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqBLA.Visible=True
	DifusorIzqGri.Visible=False
	End If
	
	If LuIntdCast.state=1 and flashin=4 Then
	FlasherBLA.visible=False
	'-------------------
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	End If

	If LuIntdCast.state=1 and flashin=5 Then
	FlasherBLA.visible=True
	'-------------------
	DifusorDerBLA.Visible=True
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqBLA.Visible=True
	DifusorIzqGri.Visible=False
	End If

	If LuIntdCast.state=1 and flashin=6 Then
	FlasherBLA.visible=False
	'-------------------
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=True
	End If

	If LuIntdCast.state=1 and flashin=7 Then
	FlasherBLA.visible=True
	'-------------------
	DifusorDerBLA.Visible=True
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqBLA.Visible=True
	DifusorIzqGri.Visible=False
	End If

	If LuIntdCast.state=1 and flashin=8 Then
	TimerFlasero.enabled=false
	AddFlashin (-Flashin)
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherAZU.opacity=95
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=true
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If
End Sub 

'**** (7) ****
Sub CheckFrankenballFlsh()
	If LFrBall.State=2 Then

	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherAZU.opacity=95
	FlasherVER.visible=True
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=True
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=False
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=True
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=False
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=1
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=1
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=1
	LuzTopVER1.state=1
	LuzTopVER2.state=1
	LuzTopVER3.state=1
	LuzTopVER4.state=1
	LuzTopVER5.state=1
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If

	IF LFrBall.State=0 or LFrBall.State=2 Then
	FlasherAZU.visible=False
	FlasherAMA.visible=False
	FlasherROJ.visible=False
	FlasherAZU.opacity=95
	FlasherVER.visible=False
	FlasherBLA.visible=False
	'-------------------
	DifusorDerAZU.Visible=False
	DifusorDerAMA.Visible=False
	DifusorDerROJ.Visible=False
	DifusorDerVER.Visible=False
	DifusorDerBLA.Visible=False
	DifusorDerGri.Visible=True
	'-------------------
	DifusorIzqAZU.Visible=False
	DifusorIzqAMA.Visible=False
	DifusorIzqROJ.Visible=False
	DifusorIzqVER.Visible=False
	DifusorIzqBLA.Visible=False
	DifusorIzqGri.Visible=true
	'-----------------
	LDifDerAZU.state=0
	LDifDerAMA.state=0
	LDifDerROJ.state=0
	LDifDerVER.state=0
	LDifDerBLA.state=0
	'-----------------
	LDifIzqAZU.state=0
	LDifIzqAMA.state=0
	LDifIzqROJ.state=0
	LDifIzqVER.state=0
	LDifIzqBLA.state=0
	'-----------------las pri son AZU
	LuzTopAZU.state=0
	LuzTopAZU1.state=0
	LuzTopAZU2.state=0
	LuzTopAZU3.state=0
	LuzTopAZU4.state=0
	LuzTopAZU5.state=0
	'----------------
	LuzTopAMA.state=0
	LuzTopAMA1.state=0
	LuzTopAMA2.state=0
	LuzTopAMA3.state=0
	LuzTopAMA4.state=0
	LuzTopAMA5.state=0
	'----------------
	LuzTopROJ.state=0
	LuzTopROJ1.state=0
	LuzTopROJ2.state=0
	LuzTopROJ3.state=0
	LuzTopROJ4.state=0
	LuzTopROJ5.state=0
	'----------------
	LuzTopVER.state=0
	LuzTopVER1.state=0
	LuzTopVER2.state=0
	LuzTopVER3.state=0
	LuzTopVER4.state=0
	LuzTopVER5.state=0
	'----------------
	LuzTopBLA.state=0
	LuzTopBLA1.state=0
	LuzTopBLA2.state=0
	LuzTopBLA3.state=0
	LuzTopBLA4.state=0
	LuzTopBLA5.state=0
	End If
End Sub


'*******************************************************************
'*********   Reset de dmd   ****************************************
Sub Trendor_Timer()
	If rendor>0 Then
	Addrendor (-1)
	checkcuenta()
	End If
End Sub

Sub Checkcuenta()
	If rendor=0 Then
	Trendor.enabled=false
	P1= (rendor)
	End If
End Sub

Sub AddRendor(rend0)
	Rendor = Rendor + rend0
End Sub
'*******************************************************************
'*******************************************************************



