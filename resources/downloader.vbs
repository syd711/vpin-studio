Dim objXMLHTTP
Dim objStream
Dim url
Dim filePath

' Check if two arguments are passed (URL and file path)
If WScript.Arguments.Count <> 2 Then
    WScript.Echo "Usage: cscript downloader.vbs <URL> <FilePath>"
    WScript.Quit
End If

' Get the command-line arguments
url = WScript.Arguments(0) ' First argument is the URL
filePath = WScript.Arguments(1) ' Second argument is the file path

' Create the XMLHTTP object for the HTTP request
Set objXMLHTTP = CreateObject("MSXML2.XMLHTTP")
objXMLHTTP.Open "GET", url, False
objXMLHTTP.Send

' Create a stream object to write the file locally
Set objStream = CreateObject("ADODB.Stream")
objStream.Type = 1 ' Binary
objStream.Open
objStream.Write objXMLHTTP.ResponseBody
objStream.SaveToFile filePath, 2 ' Overwrite if the file already exists
objStream.Close

' Cleanup
Set objStream = Nothing
Set objXMLHTTP = Nothing

WScript.Echo "File downloaded successfully!"
