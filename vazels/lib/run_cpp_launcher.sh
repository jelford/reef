ln -s libprotobuf.so.6.0.0 libprotobuf.so
ln -s libprotobuf.so.6.0.0 libprotobuf.so.6
g++ -pthread main.cc *.o libprotobuf.so -o cpp_launcher
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:`pwd`
./cpp_launcher ../Vazel/fifos/CPP_OUTBOUND_FIFO ../Vazel/fifos/CPP_INBOUND_FIFO $1
