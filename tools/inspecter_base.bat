@echo off
setlocal
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
echo.
echo === contacts.db : table contacts ===
echo.
"%ADB%" shell "run-as com.example.contactsapp sqlite3 -header -column databases/contacts.db 'select _id,nom,prenom,telephone,latitude,longitude from contacts order by nom;'"
echo.
"%ADB%" shell "run-as com.example.contactsapp sqlite3 databases/contacts.db 'select count(*) || \" contacts au total\" from contacts;'"
echo.
endlocal
