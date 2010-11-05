currentDir=`pwd`
jars="vazels-impl.jar vazels-control.jar vazels-api.jar vazels-actors-api-spec.jar siena-1.5.5.jar protobuf-java-2.3.0.jar log4j-1.2.15.jar commons-net-2.0.jar commons-logging-1.1.1.jar"
classpath=""
codebase=""
for jar in $jars; do
	classpath="$jar:$classpath"
	codebase="file://$currentDir/$jar $codebase"
done
java -Djava.rmi.server.codebase="$codebase" -Djava.security.policy=file://$currentDir/vazel_security.policy -cp $classpath uk.ac.imperial.vazels.control.VazelStart