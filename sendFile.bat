@echo off
setlocal

REM Set your variables
set HOST=localhost
set USERNAME=tester
set PASSWORD=password
set REMOTE_PATH=/delta/
set LOCAL_DIR=C:\envoi
set EXTENSIONS=.docx .pdf

REM Path to the psftp executable
set PSFTP_PATH=C:\Program Files\PuTTY\psftp.exe

REM Get current date in yyyy-mm-dd format
for /f %%x in ('powershell Get-Date -Format yyyy-MM-dd') do set CURRENT_DATE=%%x

REM Create the full destination directory path with date
set DESTINATION_DIR=%LOCAL_DIR%\archives\%CURRENT_DATE%

REM Ensure the destination directory exists
if not exist "%DESTINATION_DIR%" (
    mkdir "%DESTINATION_DIR%"
)

REM Loop through files with specified extensions in the local directory
for %%E in (%EXTENSIONS%) do (
    for %%F in (%LOCAL_DIR%\*%%E) do (
        set FILE=%%~nxF
        REM Construct the psftp command
        echo put "%%F" "%REMOTE_PATH%%%~nxF " > script.txt
        echo bye >> script.txt
        REM Execute the psftp command
        "%PSFTP_PATH%" "%USERNAME%@%HOST%" -pw "%PASSWORD%" -v -b script.txt
        set "PSFTP_EXIT_CODE=!ERRORLEVEL!"
		
        REM Check the exit code of the psftp command
        if !PSFTP_EXIT_CODE! equ 0 (
            REM Move the file to the destination directory with date
            move "%%F" "%DESTINATION_DIR%"
        ) else (
            echo Connection to SFTP server failed. File not moved.
        )
    )
)

endlocal


