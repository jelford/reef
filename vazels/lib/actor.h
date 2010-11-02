#ifndef VAZELS_ACTOR_H
#define VAZELS_ACTOR_H

#include <string>
#include <map>

namespace vazels {

class Actor;
class ActorFactory;
class SnapshotWriter;
class ValueWrapper;
typedef std::map<std::string, ValueWrapper> InvocationSessionWrapper;
typedef std::map<std::string, ActorFactory*> ActorsFactoryRegistry;

// implement this function to create your actor factories
void register_actor_factories(ActorsFactoryRegistry* registry);

class Actor {
public:
	virtual bool invoke(const SnapshotWriter& writer,
			InvocationSessionWrapper* session) = 0;
	Actor();
	virtual ~Actor();
private:
	Actor(const Actor& actor);
	Actor& operator =(const Actor& actor);
};

class ActorFactory {
public:
	virtual Actor* create_actor() const = 0;
	ActorFactory();
	virtual ~ActorFactory();
private:
	ActorFactory(const ActorFactory& factory);
	ActorFactory& operator =(const ActorFactory& factory);
};

class SnapshotWriter {
public:
	virtual void write_snapshot(const std::string& title,
			const ValueWrapper& value) const = 0;
	SnapshotWriter();
	virtual ~SnapshotWriter();
private:
	SnapshotWriter(const SnapshotWriter& writer);
	SnapshotWriter& operator =(const SnapshotWriter& writer);
};

void run_cpp_launcher(const vazels::ActorsFactoryRegistry& registry,
		const std::string& inbound_fifo, const std::string& outbound_fifo,
		int num_threads);

class ValueWrapper {
public:
	bool has_double_value() const;
	bool has_string_value() const;
	bool has_custom_value() const;

	double get_double_value() const;
	std::string& get_string_value() const;
	std::string& get_custom_value() const;

	void set_double_value(const double* const value);
	void set_string_value(const std::string* const value);
	void set_custom_value(const std::string* const value);

	ValueWrapper();
	ValueWrapper(const ValueWrapper& value);
	ValueWrapper& operator =(const ValueWrapper& value);
	~ValueWrapper();
private:
	void copy(const ValueWrapper& value);
	void release();
	double* double_value;
	std::string* string_value;
	std::string* custom_value;
	static bool has_value(const void* ptr) {
		return ptr != NULL;
	}
};

}
#endif
