jars=`ls *.jar`
classpath=""
for jar in $jars; do
	classpath="$jar:$classpath"
done
java -cp $classpath uk.ac.imperial.vazels.actor.ShellEntryPoint ../Vazel/fifos/SHELL_OUTBOUND_FIFO ../Vazel/fifos/SHELL_INBOUND_FIFO $1 10000