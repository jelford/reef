#include "actor.h"
#include <cstdlib>

void vazels::register_actor_factories(vazels::ActorsFactoryRegistry* registry) {
	// register your factories here
}

int main(int argc, const char** argv) {
	vazels::ActorsFactoryRegistry registry;
	vazels::register_actor_factories(&registry);
	const std::string inbound_fifo(argv[1]);
	const std::string outbound_fifo(argv[2]);
	const int num_threads = atoi(argv[3]);
	vazels::run_cpp_launcher(registry, inbound_fifo, outbound_fifo, num_threads);
	return 0;
}
