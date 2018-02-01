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

echo GameElementsFramework
cd ..\GameElementsFramework
git pull "origin" master
echo.

echo RandomGenerators
cd ..\RandomGenerators
git pull "origin" master
echo.

echo RestClient
cd ..\RestClient
git pull "origin" master
echo.

echo Initium-Core
cd ..\initium-core
git pull "origin" master
echo.

pause