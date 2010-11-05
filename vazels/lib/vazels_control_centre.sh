cd lib
currentDir=`pwd`
jars=`ls *.jar`
classpath=""
codebase=""
for jar in $jars; do
	classpath="$jar:$classpath"
	codebase="file://$currentDir/$jar $codebase"
done
java -Djava.rmi.server.codebase="$codebase" -Djava.security.policy=file://$currentDir/vazel_security.policy -cp $classpath uk.ac.imperial.vazels.cc.model.EntryPoint $@