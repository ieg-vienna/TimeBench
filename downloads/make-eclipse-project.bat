rem "TimeBench setup helper by Alexander Rind, 2014-06-13"

echo "This script creates Eclipse Java project files from templates."
echo "Move the file into the project folder containing eclipse.project and double click."

cd /D "%~dp0"

copy eclipse.project .project
copy eclipse.classpath .classpath

