' ============================================================
' WoVP Telemetry v1
' Bloc universel injecte automatiquement par WoVP_Patcher.
' Necessite DEUX Timer natifs :
'   - WoVP_Timer      : Interval=1000, Enabled=True
'   - WoVP_FastTimer  : Interval=50,   Enabled=True
' ============================================================

Const WoVP_LogDir = "C:\vpin_logs\"
Const WoVP_TickInterval = 1000
Const WoVP_LogEveryNTicks = 5
Const WoVP_IdleThresholdTicks = 6
Const WoVP_SwitchScanMax = 120

Dim WoVP_TickCount
Dim WoVP_InternalTimeMs
Dim WoVP_LastScore
Dim WoVP_LastBallRemaining
Dim WoVP_BallsConfigured
Dim WoVP_TableName
Dim WoVP_RomName
Dim WoVP_LogFileName
Dim WoVP_Initialized
Dim WoVP_LastSwitchStates()
Dim WoVP_SwitchTransitionsWindow
Dim WoVP_IdleTickCount
Dim WoVP_IdleStartTime
Dim WoVP_InIdle
Dim WoVP_EventCounters
Dim WoVP_PendingEvents
Dim WoVP_LastSystemTime
Dim WoVP_TimeAnomalyCount
Dim WoVP_LastStartButtonState
Dim WoVP_StartButtonIndex

WoVP_TickCount = 0
WoVP_InternalTimeMs = 0
WoVP_LastScore = "unknown"
WoVP_LastBallRemaining = "unknown"
WoVP_BallsConfigured = "unknown"
WoVP_TableName = "unknown"
WoVP_RomName = "unknown"
WoVP_LogFileName = ""
WoVP_Initialized = False
WoVP_SwitchTransitionsWindow = 0
WoVP_IdleTickCount = 0
WoVP_IdleStartTime = 0
WoVP_InIdle = False
WoVP_LastSystemTime = 0
WoVP_TimeAnomalyCount = 0
WoVP_LastStartButtonState = False
WoVP_StartButtonIndex = -1
Set WoVP_EventCounters = CreateObject("Scripting.Dictionary")
Set WoVP_PendingEvents = CreateObject("Scripting.Dictionary")
ReDim WoVP_LastSwitchStates(WoVP_SwitchScanMax)

Sub WoVP_Timer_Timer()
    On Error Resume Next

    WoVP_TickCount = WoVP_TickCount + 1
    WoVP_InternalTimeMs = WoVP_InternalTimeMs + WoVP_TickInterval

    If Not WoVP_Initialized Then
        WoVP_Initialize
    End If

    If Not WoVP_Initialized Then
        Exit Sub
    End If

    WoVP_CheckTimeAnomaly
    WoVP_ScanSwitches
    WoVP_ReadScore
    WoVP_ReadBallRemaining

    If WoVP_IdleTickCount >= WoVP_IdleThresholdTicks Then
        If Not WoVP_InIdle Then
            WoVP_InIdle = True
            WoVP_IdleStartTime = WoVP_InternalTimeMs
        End If
    Else
        If WoVP_InIdle Then
            Dim idleDuration
            idleDuration = (WoVP_InternalTimeMs - WoVP_IdleStartTime) \ 1000
            WoVP_WriteLog "IDLE|duration=" & idleDuration & "|score=" & WoVP_LastScore
            WoVP_InIdle = False
        End If
    End If

    If WoVP_TickCount Mod WoVP_LogEveryNTicks = 0 Then
        WoVP_WriteTickLog
    End If

    On Error Goto 0
End Sub

Sub WoVP_FastTimer_Timer()
    On Error Resume Next
    If WoVP_Initialized Then
        WoVP_CheckStartButton
    End If
    On Error Goto 0
End Sub

Sub WoVP_Initialize()
    On Error Resume Next

    WoVP_RomName = "norom"
    WoVP_RomName = Controller.GameName
    If WoVP_RomName = "" Then WoVP_RomName = "norom"

    WoVP_TableName = WoVP_RomName

    WoVP_BallsConfigured = WoVP_ReadBallsConfigured()

    Dim timestamp
    timestamp = WoVP_Timestamp()
    WoVP_LogFileName = WoVP_LogDir & "WoVP_" & WoVP_Sanitize(WoVP_TableName) & "_" & timestamp & ".log"

    CreateObject("Scripting.FileSystemObject").CreateFolder WoVP_LogDir

    WoVP_WriteLog "WOVP-LOG-V1"
    WoVP_WriteLog "GAME_START|table=" & WoVP_TableName & "|rom=" & WoVP_RomName & "|balls_configured=" & WoVP_BallsConfigured

    WoVP_Initialized = True

    On Error Goto 0
End Sub

Function WoVP_ReadBallsConfigured()
    Dim v
    v = "unknown"
    On Error Resume Next
    v = Controller.BallsPerGame
    If IsEmpty(v) Or v = "" Or v = 0 Then v = BallsPerGame
    If IsEmpty(v) Or v = "" Or v = 0 Then v = 3
    On Error Goto 0
    WoVP_ReadBallsConfigured = v
End Function

Sub WoVP_ReadScore()
    On Error Resume Next
    Dim v
    v = ""
    v = Controller.Score
    If IsEmpty(v) Or v = "" Then v = Score
    If IsEmpty(v) Or v = "" Then v = PlayerScore
    If IsEmpty(v) Or v = "" Then
        WoVP_LastScore = "unknown"
    Else
        WoVP_LastScore = v
    End If
    On Error Goto 0
End Sub

Sub WoVP_ReadBallRemaining()
    On Error Resume Next
    Dim v
    v = ""
    v = Controller.BallsRemaining
    If IsEmpty(v) Or v = "" Then v = BallsRemaining
    If IsEmpty(v) Or v = "" Then v = Balls
    If IsEmpty(v) Or v = "" Then
        WoVP_LastBallRemaining = "unknown"
    Else
        WoVP_LastBallRemaining = v
    End If
    On Error Goto 0
End Sub

Sub WoVP_ScanSwitches()
    On Error Resume Next
    Dim i, state, prev, transitions
    transitions = 0
    For i = 0 To WoVP_SwitchScanMax
        state = -1
        state = Controller.Switch(i)
        If Not IsEmpty(state) Then
            prev = WoVP_LastSwitchStates(i)
            If prev <> state Then
                transitions = transitions + 1
                WoVP_LastSwitchStates(i) = state
            End If
        End If
    Next
    WoVP_SwitchTransitionsWindow = WoVP_SwitchTransitionsWindow + transitions

    If transitions = 0 Then
        WoVP_IdleTickCount = WoVP_IdleTickCount + 1
    Else
        WoVP_IdleTickCount = 0
    End If
    On Error Goto 0
End Sub

Sub WoVP_CheckStartButton()
    On Error Resume Next
    Dim rawState, boolState

    If WoVP_StartButtonIndex = -1 Then
        WoVP_StartButtonIndex = WoVP_GetStartButtonIndex()
    End If

    If WoVP_StartButtonIndex = -1 Then
        Exit Sub
    End If

    rawState = Controller.Switch(WoVP_StartButtonIndex)
    boolState = Not (rawState = False Or rawState = 0 Or IsEmpty(rawState))

    If boolState And Not WoVP_LastStartButtonState Then
        WoVP_WriteLog "EVENT|START_BUTTON_PRESSED|time=" & (WoVP_InternalTimeMs \ 1000)
    End If

    WoVP_LastStartButtonState = boolState
    On Error Goto 0
End Sub

Function WoVP_GetStartButtonIndex()
    Dim idx
    idx = -1
    On Error Resume Next
    idx = swStartButton
    If Err.Number <> 0 Or idx = -1 Then
        Err.Clear
        idx = swStartButtonX
    End If
    If Err.Number <> 0 Or idx = -1 Then
        Err.Clear
        idx = -1
    End If
    On Error Goto 0
    WoVP_GetStartButtonIndex = idx
End Function

Sub WoVP_CheckTimeAnomaly()
    On Error Resume Next
    Dim nowSys
    nowSys = Timer()
    If WoVP_LastSystemTime > 0 Then
        Dim deltaSys, expected, ecart
        deltaSys = nowSys - WoVP_LastSystemTime
        If deltaSys < 0 Then deltaSys = deltaSys + 86400
        expected = WoVP_TickInterval / 1000
        ecart = Abs(deltaSys - expected)
        If ecart > (expected * 0.5) Then
            WoVP_TimeAnomalyCount = WoVP_TimeAnomalyCount + 1
        Else
            WoVP_TimeAnomalyCount = 0
        End If
        If WoVP_TimeAnomalyCount >= 5 Then
            WoVP_WriteLog "WARNING|TIME_ANOMALY|delta=" & FormatNumber(deltaSys, 3)
            WoVP_TimeAnomalyCount = 0
        End If
    End If
    WoVP_LastSystemTime = nowSys
    On Error Goto 0
End Sub

Sub WoVP_WriteTickLog()
    On Error Resume Next
    Dim line, k
    line = "TICK|time=" & (WoVP_InternalTimeMs \ 1000) & _
           "|score=" & WoVP_LastScore & _
           "|balls_remaining=" & WoVP_LastBallRemaining & _
           "|switches=" & WoVP_SwitchTransitionsWindow
    WoVP_WriteLog line

    For Each k In WoVP_PendingEvents.Keys
        WoVP_WriteLog "EVENT|" & k & "=" & WoVP_PendingEvents(k)
    Next
    WoVP_PendingEvents.RemoveAll

    WoVP_SwitchTransitionsWindow = 0
    On Error Goto 0
End Sub

Sub WoVP_CountEvent(eventName)
    On Error Resume Next
    If WoVP_EventCounters.Exists(eventName) Then
        WoVP_EventCounters(eventName) = WoVP_EventCounters(eventName) + 1
    Else
        WoVP_EventCounters.Add eventName, 1
    End If
    If WoVP_PendingEvents.Exists(eventName) Then
        WoVP_PendingEvents(eventName) = WoVP_PendingEvents(eventName) + 1
    Else
        WoVP_PendingEvents.Add eventName, 1
    End If
    On Error Goto 0
End Sub

Sub WoVP_WriteLog(content)
    On Error Resume Next
    If WoVP_LogFileName = "" Then Exit Sub
    Dim oFile, b64
    b64 = WoVP_Base64(content)

    Set oFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(WoVP_LogFileName, 8, True)
    If Err.Number <> 0 Then
        Err.Clear
        Exit Sub
    End If

    oFile.WriteLine b64
    oFile.Close
    Set oFile = Nothing
    On Error Goto 0
End Sub

Function WoVP_Base64(s)
    Dim i, c, b, out
    Dim chars
    chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    out = ""
    For i = 1 To Len(s) Step 3
        b = Asc(Mid(s, i, 1))
        If i + 1 <= Len(s) Then b = b * 256 + Asc(Mid(s, i + 1, 1)) Else b = b * 256
        If i + 2 <= Len(s) Then b = b * 256 + Asc(Mid(s, i + 2, 1)) Else b = b * 256
        out = out & Mid(chars, ((b \ 262144) Mod 64) + 1, 1)
        out = out & Mid(chars, ((b \ 4096) Mod 64) + 1, 1)
        If i + 1 <= Len(s) Then out = out & Mid(chars, ((b \ 64) Mod 64) + 1, 1) Else out = out & "="
        If i + 2 <= Len(s) Then out = out & Mid(chars, (b Mod 64) + 1, 1) Else out = out & "="
    Next
    WoVP_Base64 = out
End Function

Function WoVP_Timestamp()
    Dim d
    d = Now()
    WoVP_Timestamp = Year(d) & Right("0" & Month(d), 2) & Right("0" & Day(d), 2) & "_" & _
                     Right("0" & Hour(d), 2) & Right("0" & Minute(d), 2) & Right("0" & Second(d), 2)
End Function

Function WoVP_Sanitize(s)
    Dim i, c, out
    out = ""
    For i = 1 To Len(s)
        c = Mid(s, i, 1)
        If (c >= "A" And c <= "Z") Or (c >= "a" And c <= "z") Or (c >= "0" And c <= "9") Or c = "_" Then
            out = out & c
        Else
            out = out & "_"
        End If
    Next
    WoVP_Sanitize = out
End Function