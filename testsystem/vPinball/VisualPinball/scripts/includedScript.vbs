
Const includedVar = "included"

Sub DMD_Init
	If turnonultradmd = 0 then exit sub
	ExecuteGlobal GetTextFile("UltraDMD_Options.vbs")
	InitUltraDMD cAssetsFolder, cGameName
End Sub

