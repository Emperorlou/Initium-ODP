@Echo Off

echo Initium-ODP
git pull "origin" master
echo.

echo GEFCommon
cd GEFCommon
git pull "origin" master
echo.

echo CachedDatastore
cd ..\CachedDatastore
git pull "origin" master
echo.

pause