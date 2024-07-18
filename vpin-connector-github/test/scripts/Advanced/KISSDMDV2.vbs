' *************************************
'    KISS UltraDMDCode 
'
Const KissDMDV=1.08
' *************************************
'
' 20161031 - use ModifyScene to reduce flicker
' 20161101 - End of game audio
' 20161102 - New Match Gifs - be sure to download them and put in ultradmd directory.
' 20161113 - New UDMD Location code from Seraph74
' 20161120 - Updated UDMD Location code from Seraph74

Dim UltraDMD
Dim BonusLights1, BonusLights2, BonusLights3, BonusLights4

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
    ' Set UltraDMD = CreateObject("UltraDMD.DMDObject")
	Dim FlexDMD
    Set FlexDMD = CreateObject("FlexDMD.FlexDMD")
	Set UltraDMD = FlexDMD.NewUltraDMD()
    UltraDMD.Init

    Dim fso, curDir
    Set fso = CreateObject("Scripting.FileSystemObject")
    curDir = fso.GetAbsolutePathName(".")
    Set fso = nothing

    ' A Major version change indicates the version is no longer backward compatible
    If Not UltraDMD.GetMajorVersion = 1 Then
        MsgBox "Incompatible Version of UltraDMD found."
        Exit Sub
    End If

    'A Minor version change indicates new features that are all backward compatible
    If UltraDMD.GetMinorVersion < 3 Then
        MsgBox "Incompatible Version of UltraDMD found.  Please update to version 1.4 or newer."
        Exit Sub
    End If

    UltraDMD.SetProjectFolder curDir & "\KISS.UltraDMD"
    UltraDMD.SetVideoStretchMode UltraDMD_VideoMode_Middle
    UltraDMD.SetScoreboardBackgroundImage "1.png",15,13

    ImgList = "kiss.png,kiss.png,kiss.png,kiss.png,kiss1.png,kiss2.png,kiss3.png,kiss4.png,kiss.png,kiss.png,kiss.png,kiss.png"
    BonusLights1 = UltraDMD.CreateAnimationFromImages(4, false, imgList)
    ImgList = "Army.png,Army.png,Army.png,Army.png,Army1.png,Army2.png,Army3.png,Army4.png,Army.png,Army.png,Army.png,Army.png"
    BonusLights2 = UltraDMD.CreateAnimationFromImages(4, false, imgList)
    ImgList = "faces.png,faces.png,faces.png,faces.png,faces1.png,faces2.png,faces3.png,faces4.png,faces.png,faces.png,faces.png,faces.png"
    BonusLights3 = UltraDMD.CreateAnimationFromImages(4, false, imgList)
    ImgList = "inst.png,inst.png,inst.png,inst.png,inst1.png,inst2.png,inst3.png,inst4.png,inst.png,inst.png,inst.png,inst.png"
    BonusLights4 = UltraDMD.CreateAnimationFromImages(4, false, imgList)

    OnScoreboardChanged()
End Sub

'---------- UltraDMD Unique Table Color preference -------------
' http://www.vpforums.org/index.php?showtopic=26602&page=21#entry362581
'
Dim DMDColor, DMDColorSelect, UseFullColor
Dim DMDPosition, DMDPosX, DMDPosY

Sub GetDMDColor
    Dim WshShell,filecheck,directory
    Set WshShell = CreateObject("WScript.Shell")
    If DMDPosition then
        WshShell.RegWrite "HKCU\Software\UltraDMD\x",DMDPosX,"REG_DWORD"
        WshShell.RegWrite "HKCU\Software\UltraDMD\y",DMDPosY,"REG_DWORD"
    End if
    WshShell.RegWrite "HKCU\Software\UltraDMD\fullcolor",UseFullColor,"REG_SZ"
    WshShell.RegWrite "HKCU\Software\UltraDMD\color",DMDColorSelect,"REG_SZ"
End Sub
'---------------------------------------------------
'---------------------------------------------------

' *****************************************
'    DMD Interactions
' *****************************************
Sub DMDTextI(txt1,txt2,img)  ' Pass in Image
  debug.print "Text: " & txt1 & " " & txt2 
  DMDTextPauseI txt1,txt2,500,img
End Sub

Sub DMDText(txt1,txt2)
  debug.print "Text: " & txt1 & " " & txt2 
  DMDTextPause txt1,txt2,500
End Sub

Sub DMDTextPauseI(txt1,txt2,pause,img)
dim PriorState
  debug.print "Text: " & txt1 & " " & txt2 
  PriorState=UDMDTimer.Enabled
  UDMDTimer.Enabled=False
  if UseUDMD then
    UltraDMD.DisplayScene00Ex img, txt1, 15, 2, txt2, 15, 2, UltraDMD_Animation_None, pause, UltraDMD_Animation_None
  End if
  UDMDTimer.interval=100:UDMDTimer.Enabled=PriorState
End Sub

Sub DMDTextPause(txt1,txt2,pause)
  debug.print "Text: " & txt1 & " " & txt2 
  UDMDTimer.Enabled=False
  if UseUDMD then
    UltraDMD.DisplayScene00Ex "scene01.gif", txt1, 15, 2, txt2, 15, 2, UltraDMD_Animation_None, pause, UltraDMD_Animation_None
    UDMDTimer.interval=100:UDMDTimer.Enabled = True
  End if
End Sub

Sub DMDGif(img1,txt1,txt2,p)
  debug.print "Txt: >" & txt1 & "< Gif:" & img1 & " Pause =" & p
  if NOT UseUDMD then Exit Sub

  UDMDTimer.Enabled=False
  if txt2 = "" then
    UltraDMD.DisplayScene00Ex img1, txt1, 0, 15, "", -1, -1, UltraDMD_Animation_None, p, UltraDMD_Animation_None
  else
    UltraDMD.DisplayScene00Ex img1, txt1, 15, 2, txt2, 13, 2, UltraDMD_Animation_None, p, UltraDMD_Animation_None
  end if
  UDMDTimer.interval=p:UDMDTimer.Enabled = True
End Sub

Sub DisplayI(id)
  debug.print "Showing ID" & id
  if NOT useUDMD Then Exit Sub

  DMDFlush()
  Select Case id
    Case 2: DMDTextI "DANGER","", bgi
    Case 1: DMDTextI "TILT","", bgi
    Case 3: DMDTextI "BASS INSTRUMENT","LIT", bgi
    Case 4: DMDTextI "DRUM INSTRUMENT","LIT", bgi
    Case 5: DMDTextI "KISS COMBO","LIT", bgi                          ' right Lane
    Case 6: DMDTextI "SPELL DEMON TO","RAISE JACKPOT!", bgi
    Case 7: DMDTextI "LOCK","LIT", bgi
'when both green lights hit
    Case 8: DMDTextI "DEUCE","", bgi
' ARMY COMPLETED SPINNER VALUE 24,000 (spinning army target) 4 right targets
    Case 9: DMDTextI "SPINNER VALUE 5,000","ARMY COMPLETED", bgi
' FRONT ROW IS LIT .. when all left kiss targets hit
    Case 10: DMDTextI "FRONT ROW IS","LIT", bgi
    Case 11: DMDTextI "ARMY COMBO IS","LIT", bgi
' Left out lane - shows crows cheer the "FRONT ROW AWARDED | ROCK AGAIN!", dont wait to drain just pop ball and autoplunge
    Case 13: DMDTextI "FRONT ROW","ROCK AGAIN!", bgi
    Case 14: DMDTextI "GUITAR INSTRUMENT","LIT", bgi
    Case 15: DMDTextI "EXTRA BALL","", bgi
    Case 16: DMDGif "scene10.gif","REPLAY","",slen(10)
             ' PlaySound "audio"  'x175

    Case 17: DMDTextI "JACKPOT","", bgi:PlaySound "audio663"
    Case 18: DMDTextI "DEMON","JACKPOT", bgi:PlaySound "audio663"
    Case 19: DMDTextI "DOUBLE","JACKPOT", bgi:PlaySound "audio666"
    Case 20: DMDTextI "SUPER","JACKPOT", bgi
    Case 21: DMDTextI "DOUBLE SUPER","JACKPOT", bgi
    Case 22: DMDTextI "BONUS","2X", bgi:PlaySound "audio681"
    Case 23: DMDTextI "BONUS","3X", bgi:PlaySound "audio682"
    Case 24: DMDTextI "COLOSSAL","BONUS", bgi

    Case 25: DMDGif  "scene11.gif","EXTRA BALL","LIT",slen(11)
    Case 26: DMDGif  "scene11.gif","ROCK AGAIN!","",slen(11)
    Case 27: DMDGif  "scene45.gif","SUPER RAMPS","COMPLETED",slen(45)
    Case 28: DMDGif  "scene45.gif","SUPER BUMPERS","COMPLETED",slen(45)
    Case 29: DMDGif  "scene45.gif","SUPER SPINNER","COMPLETED",slen(45)
    Case 30: DMDGif  "scene45.gif","SUPER TARGETS","COMPLETED",slen(45)
    Case 31: DMDGif  "scene45.gif","SUPER RAMPS", (10-RampCnt(CurPlayer)) & " REMAINING",300
    Case 32: DMDGif  "scene45.gif","SUPER BUMPERS", (50-BumperCnt(CurPlayer)) & " REMAINING",300
    Case 33: DMDGif  "scene45.gif","SUPER SPINNER", (100-SpinCnt(CurPlayer)) & " REMAINING",300
    Case 34: DMDGif  "scene45.gif","SUPER TARGETS", (100-TargetCnt(CurPlayer)) & " REMAINING",300
  End Select
End Sub

Sub UDMDTimer_Timer
    If Not UltraDMD.IsRendering and NOT hsbModeActive Then
        'When the scene finishes rendering, then immediately display the scoreboard
        UDMDTimer.Enabled = False:UDMDTimer.interval=100 
        OnScoreboardChanged()
    End If
End Sub

Sub BV(val)
' only show dmd if timer is active so as not to overwhelm the display
DIM tstr,bstr
    if NOT useUDMD Then Exit Sub
    if bvtimer.enabled=False and svtimer.enabled=False and UltraDMD.IsRendering then Exit Sub  ' Some other animation is going on

    if RND*10 < 5 then
      tstr=string(INT(5*RND)," ") & ((bumpercolor(val)+1)*5) & "K"
    else
      tstr=((bumpercolor(val)+1)*5) & "K" & string(INT(5*RND)," ") 
    end if

    bstr=" "

    UDMDTimer.Enabled=False

    bvtimer.enabled=False
    bvtimer.interval=100
    bvtimer.enabled=True   
    debug.print "BV()"
    if CurScene <> "bv" then
      CurScene="bv"
      debug.print "BV Display scene TEST"
      UltraDMD.CancelRendering:UltraDMD.Clear
      if RND*10 < 5 then
      	  UltraDMD.DisplayScene00ExWithId "bvid", FALSE, "scene18.gif", tstr, 14, INT(RND*4)-1, bstr, -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
      else
    	  UltraDMD.DisplayScene00ExWithId "bvid", FALSE, "scene19.gif", bstr, -1, -1, tstr, 14, INT(RND*4)-1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
      end if
    Else
      debug.print "ModifyScene"
      if NOT UltraDMD.IsRendering then debug.print "bv is no longer rendering??"
      if RND*10 < 5 then
      	  UltraDMD.ModifyScene00 "bvid",  tstr, bstr
      else
    	  UltraDMD.ModifyScene00 "bvid",  bstr, tstr
      end if
    End if
    UDMDTimer.interval=500:UDMDTimer.Enabled = True
End Sub

Sub bvtimer_timer()
  if Not UltraDMD.IsRendering Then
    bvtimer.enabled=False
    debug.print "BV timer expired"
    If CurScene="bv" then 
      CurScene=""
    End If
    OnScoreboardChanged()
    UDMDTimer.interval=100:UDMDTimer.Enabled = True
  End if
End Sub

Sub SpinV(txt)  ' spinner video
Dim bstr,tstr
    if NOT useUDMD Then Exit Sub
    if svtimer.enabled=False and bvtimer.enabled=False and UltraDMD.IsRendering then Exit Sub  ' Some other animation is going on

    if RND*10 < 7 then
      tstr=string(INT(5*RND)," ") & txt
    else
      tstr=txt & string(INT(5*RND)," ") 
    end if

    bstr=" "

    if left(txt,7)="Spinner" then
      tstr="Spinner Count"
      bstr=SpinCnt(CurPlayer)
    End if
    UDMDTimer.Enabled=False

    svtimer.enabled=False
    svtimer.interval=100
    svtimer.enabled=True   
    debug.print "SV() >" & tstr & "<" & "[" & bstr & "]"
    if CurScene <> "sv" then
      UltraDMD.CancelRendering:UltraDMD.Clear
      CurScene="sv"
      debug.print "New Scene"
      if RND*10 < 5 then
    	  UltraDMD.DisplayScene00ExWithId "sv", FALSE, "scene45.gif", tstr, 14, INT(RND*4)-1, bstr, -1, -1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
      else
    	  UltraDMD.DisplayScene00ExWithId "sv", FALSE, "scene45.gif", bstr, -1, -1, tstr, 14, INT(RND*4)-1, UltraDMD_Animation_None, 500, UltraDMD_Animation_None
      end if
    Else
      debug.print "ModifyScene"
      if NOT UltraDMD.IsRendering then debug.print "sv is no longer rendering??"
      if RND*10 < 5 then
      	  UltraDMD.ModifyScene00 "sv",  tstr, bstr
      else
    	  UltraDMD.ModifyScene00 "sv",  bstr, tstr
      end if
    End if
    UDMDTimer.interval=500:UDMDTimer.Enabled = True
End Sub

Sub svtimer_timer()
  if Not UltraDMD.IsRendering Then
    svtimer.enabled=False
    debug.print "SV timer expired"
    If CurScene="sv" then 
      CurScene=""
    End If
    OnScoreboardChanged()
    UDMDTimer.interval=100:UDMDTimer.Enabled = True
  End if
End Sub

Sub OnScoreboardChanged()
  if NOT useUDMD Then Exit Sub
  If UltraDMD.IsRendering Then Exit Sub
  if BVTimer.Enabled=True then Exit Sub ' Lets show the Bumper Animations
  if SVTimer.Enabled=True then Exit Sub ' Lets show the Spinner Animations

  debug.print "OnScoreboardChanged()"
  if UltraDMD.GetMinorVersion > 3 then
    if CurPlayer = 0 or BallsRemaining(CurPlayer)=0 then
      UltraDMD.DisplayScoreboard00 PlayersPlayingGame, 0, Score(1),  Score(2),  Score(3),  Score(4), "credits " & Credits, ""
    else
      UltraDMD.DisplayScoreboard00 PlayersPlayingGame, CurPlayer, Score(1),  Score(2),  Score(3),  Score(4), "credits " & Credits, "ball " & BallsPerGame-BallsRemaining(CurPlayer)+1
    end if
  else
    if CurPlayer = 0 or BallsRemaining(CurPlayer)=0 then
      UltraDMD.DisplayScoreboard PlayersPlayingGame, 0, Score(1),  Score(2),  Score(3),  Score(4), "credits " & Credits, ""
    else
      UltraDMD.DisplayScoreboard PlayersPlayingGame, CurPlayer, Score(1),  Score(2),  Score(3),  Score(4), "credits " & Credits, "ball " & BallsPerGame-BallsRemaining(CurPlayer)+1
    end if
  End if
End Sub

Sub RandomScene()
  RScene(INT(RND*20)+1)
End Sub

Sub RScene(x)
  if NOT useUDMD Then Exit Sub
  If UltraDMD.IsRendering Then Exit Sub
  UDMDTimer.Enabled=False
  debug.print "RandomScene " & x
  Select Case x
    Case 1: DMDGif "scene14.gif","","",slen(14)
    Case 2: DMDGif "scene24.gif","","",slen(24)
    Case 3: DMDGif "scene25.gif","","",slen(25)
    Case 4: DMDGif "scene27.gif","","",slen(27)
    Case 5: DMDGif "scene28.gif","","",slen(28)
    Case 6: DMDGif "scene29.gif","","",slen(29)
    Case 7: DMDGif "scene30.gif","","",slen(30)
    Case 8: DMDGif "scene31.gif","","",slen(31)
    Case 9: DMDGif "scene32.gif","","",slen(32)
    Case 10: DMDGif "scene33.gif","","",slen(33)
    Case 11: DMDGif "scene35.gif","","",slen(35)
    Case 12: DMDGif "scene36.gif","","",slen(36)
    Case 13: DMDGif "scene45.gif","","",slen(45)
    Case 14: DMDGif "scene51.gif","","",slen(51)
    Case 15: DMDGif "scene69.gif","","",slen(69)
    Case 16: DMDGif "scene70.gif","","",slen(70)
    Case 17: DMDGif "scene71.gif","","",slen(71)
    Case 18: DMDGif "scene72.gif","","",slen(72)
    Case 19: DMDGif "scene73.gif","","",slen(73)
    Case 20: DMDGif "scene74.gif","","",slen(74)
  End Select
End Sub

Sub AttractMode_Timer
  Dim AttractSpacer,z

  if Not UseUDMD then Exit Sub
  if UltraDMD.IsRendering then Exit Sub

  AttractMode.enabled=False
  UltraDMD.DisplayScene00 "black.bmp", "  GAME OVER  ", 15, "", -1, UltraDMD_Animation_None, 2000, UltraDMD_Animation_None
  'If LastScoreP1 <> 0 Then
      UltraDMD.DisplayScene00 "black.bmp", "Player #1", 15, LastScoreP1, 12, UltraDMD_Animation_None, 2000, UltraDMD_Animation_None
  'End If
  If LastScoreP2 <> 0 Then
      UltraDMD.DisplayScene00 "black.bmp", "Player #2", 15, LastScoreP2, 12, UltraDMD_Animation_None, 2000, UltraDMD_Animation_None
  End If
  If LastScoreP3 <> 0 Then
      UltraDMD.DisplayScene00 "black.bmp", "Player #3", 15, LastScoreP3, 12, UltraDMD_Animation_None, 2000, UltraDMD_Animation_None
  End If
  If LastScoreP4 <> 0 Then
      UltraDMD.DisplayScene00 "black.bmp", "Player #4", 15, LastScoreP4, 12, UltraDMD_Animation_None, 2000, UltraDMD_Animation_None
  End If
	
  UltraDMD.DisplayScene00 "black.bmp", "REPLAY AT", 15, "14000000", -1, UltraDMD_Animation_None, 2000, UltraDMD_Animation_None

  z = 12 - Len(HighScore(0)):AttractSpacer = ""
  For Y = 0 to z
	AttractSpacer = AttractSpacer & " "
  Next
  UltraDMD.DisplayScene00 "black.bmp", "Grand Champion", 10, HighScoreName(0) & AttractSpacer & HighScore(0), 15, 14, 2000, 14

  z = 12 - Len(HighScore(1)):AttractSpacer = ""
  For Y = 0 to z
	AttractSpacer = AttractSpacer & " "
  Next
  UltraDMD.DisplayScene00 "black.bmp", "HIGH SCORE #1", 10, HighScoreName(1) & AttractSpacer & HighScore(1), 15, 14, 2000, 14

  z = 12 - Len(HighScore(2)):AttractSpacer = ""
  For Y = 0 to z
	AttractSpacer = AttractSpacer & " "
  Next
  UltraDMD.DisplayScene00 "black.bmp", "HIGH SCORE #2", 10, HighScoreName(2) & AttractSpacer & HighScore(2), 15, 14, 2000, 14

  z = 12 - Len(HighScore(3)):AttractSpacer = ""
  For Y = 0 to z
	AttractSpacer = AttractSpacer & " "
  Next
  UltraDMD.DisplayScene00 "black.bmp", "HIGH SCORE #3", 10, HighScoreName(3) & AttractSpacer & HighScore(3), 15, 14, 2000, 14

  z = 12 - Len(HighCombo):AttractSpacer = ""
  For Y = 0 to z
	AttractSpacer = AttractSpacer & " "
  Next
  UltraDMD.DisplayScene00 "black.bmp", "COMBO CHAMPION", 10, HighComboName & AttractSpacer & HighCombo, 15, 14, 2000, 14

  UltraDMD.DisplayScene00 "scene16CROP.gif", "", 10, "", -1, 14, slen(16), UltraDMD_Animation_FadeOut

  UltraDMD.DisplayScene00 "scene100.gif", "", 10, "", -1, 14, slen(100), UltraDMD_Animation_FadeOut

  UltraDMD.DisplayScene00 "black.bmp", "PARTICIPATE IN LOCAL", 10, "TOURNAMENTS", -1, 14, 3500, 14

  UltraDMD.DisplayScene00 "sternadj.gif", "", 10, "", -1, 10, 1500, UltraDMD_Animation_FadeOut

  If FreePlay = 1 OR Credits > 0 Then
       UltraDMD.DisplayScene00 "scene02.gif", "PRESS START", 10, "", 10, UltraDMD_Animation_None, slen(02), UltraDMD_Animation_FadeOut
  Else
       UltraDMD.DisplayScene00 "scene02.gif", "INSERT COIN", 10, "", 10, UltraDMD_Animation_None, slen(02), UltraDMD_Animation_FadeOut
  End If

  if credits > 0 then
       UltraDMD.DisplayScene00 "scene02.gif", "PRESS START", 15, "CREDITS " & credits, -1, UltraDMD_Animation_None, slen(02), UltraDMD_Animation_None
  end if
  AttractMode.interval=500:AttractMode.enabled=True
  debug.print "Queued the attract dmd"
End Sub

Dim MatchFN,MatchFlag
Sub CheckMatch()
   Dim X, XX, Y, Z, ZZ, abortLoop, tempSort, tmpScore
   Dim Match, divider	
   Dim fname

   Debug.print "CheckMatch()"
   UDMDTimer.Enabled=False
   match = INT(RND*10)
   matchFlag = 0
   for x=1 To (Players)	
        tmpScore=scores(x)							'Break player's scores down into 2 digit numbers for match
	divider = 1000000000							'Divider starts at 1 billion
  	for xx=0 To 7								'Seven places will get us the last 2 digits of a 10 digit score		
		if (tmpScore >= divider) Then
			tmpScore = tmpScore MOD divider
		End If
		divider = Divider / 10						
	Next
	if (tmpScore = (Match * 10)) Then	'Did we match?	
		matchFlag  = matchFlag  +  1						'Count it up!
	End If
   Next
   MatchFN = CStr(Match) & "0"
  ' fname="match" & MatchFN & ".gif"
   debug.print fname
   if UseUDMD then
     UltraDMD.CancelRendering:UltraDMD.Clear
     DMDGif "scene06.gif","","",12000
   end if
   UDMDTimer.Enabled=False
   MatchTimer.Interval=12000
   MatchTimer.Enabled=True
End Sub

Sub MatchTimer_Timer
    debug.print "MatchTimer()"
    MatchTimer.Enabled=False
    Debug.print  ">" & MatchFN & "<"
   if UseUDMD then
     UltraDMD.CancelRendering:UltraDMD.Clear
     DMDGif "black.bmp","MATCH",MatchFN,3000
   end if
    if (matchFlag) Then										'Does one of the player's scores match?
        PlaySound "audio746",0,4  ' Match      
        PlaySound SoundFXDOF("fx_kicker",141,DOFPulse,DOFContactors), 0, 1, 0.1, 0.1
	credits  = credits  +  matchFlag								'Award a credit for each match!
    End If
    ' set the machine into game over mode
   MatchTimer2.Interval=3000
   MatchTimer2.Enabled=True
End Sub

Sub MatchTimer2_Timer
    MatchTimer2.Enabled=False
    debug.print "MatchTimer2"
    UDMDTimer.interval=200:UDMDTimer.Enabled=True 
    If INT(RND*10) > 5 then
       PlaySound "audio415"
    Else  
      If Int(RND*10) > 5 Then  
        PlaySound "audio416"
      Else 
        if Int(RND*10) > 5 Then
          PlaySound "audio427"
        End If
      End If
    End If 
    EndOfGame()
End Sub