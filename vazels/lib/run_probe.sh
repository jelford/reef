currentDir=`pwd`
java -cp vazels-control.jar -Djava.rmi.server.codebase=file://$currentDir/vazels-control.jar uk.ac.imperial.vazels.control.VazelSetup
tar zxf protobuf-2.3.0.tar.gz
cd protobuf-2.3.0
./configure --prefix=/usr
make
cp src/.libs/libprotobuf.so.6.0.0 ../Vazels/CPP_launcher/
cp -r python/google/ ../Vazels/PYTHON_launcher/
cd $currentDir/Vazels/Vazel
sh run.sh &