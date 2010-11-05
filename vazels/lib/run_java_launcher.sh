jars=`ls *.jar`
classpath=""
for jar in $jars; do
	classpath="$jar:$classpath"
done
java -cp $classpath uk.ac.imperial.vazels.actor.JavaEntryPoint ../Vazel/fifos/JAVA_OUTBOUND_FIFO ../Vazel/fifos/JAVA_INBOUND_FIFO $1 10000