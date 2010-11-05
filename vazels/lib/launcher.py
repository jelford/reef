import actor_pb2
from google.protobuf.internal import decoder
from google.protobuf.internal import encoder
from threading import Lock
from ThreadPool import ThreadPool
import sys
        
class PipeReader:
    def __init__(self, file):
        self.__file = file
    def read_next(self):
        bytes = []
        result = actor_pb2.ActorMessage()
        while True:
            bytes += self.__file.read(1)
            try:
                result.ParseFromString(self.__file.read(decoder._DecodeVarint32(bytes, 0)[0]))
                return result
            except IndexError:
                continue
    
class OutputWriter:
    def __init__(self, file, lock):
        self.__file = file
        self.__lock = lock
    def write_output(self, message):
        serialized = message.SerializeToString()
        self.__lock.acquire()        
        encoder._EncodeVarint(self.__file.write, len(serialized))
        self.__file.write(serialized)
        self.__file.flush()
        self.__lock.release()
        
class SnapshotWriter:
    def __init__(self, outputWriter, actorName):
        self.__outputWriter = outputWriter;
        self.__actorName = actorName;
    def write_snapshot(self, title, value):
        message = actor_pb2.ActorMessage()
        message.code = actor_pb2.SNAPSHOT
        content = actor_pb2.SnapshotContent()
        content.title = title
        content.actorName = self.__actorName
        content.value.ParseFromString(value.SerializeToString())
        message.content = content.SerializeToString()
        self.__outputWriter.write_output(message)
        
def wrap_raw_session(rawSession):
    session = {}
    for entry in rawSession.entries:
        session[entry.key] = entry.value
    return session

def unwrap_session(rawSession, session):
    for (key, value) in session.items():
        entry = rawSession.entries.add()
        entry.key = key
        entry.value.ParseFromString(value.SerializeToString())
        
def invoke_actor(request, outputWriter, actors, actorsLock):
    requestContent = actor_pb2.InvokeContent()
    requestContent.ParseFromString(request.content)
    actorName = requestContent.actorName
    actorsLock.acquire()
    actor = actors[actorName]
    actorsLock.release()

    session = wrap_raw_session(requestContent.session)
    try:
        result = (True == actor.invoke(SnapshotWriter(outputWriter, actorName), session))
    except Exception as err:
        print(err)
        result = False
    
    response = actor_pb2.ActorMessage()
    response.code = actor_pb2.INVOKE_RESPONSE
    response.requestId = request.requestId
    responseContent = actor_pb2.InvokeResponseContent()
    responseContent.success = result
    responseContent.session.ParseFromString(actor_pb2.InvocationSession().SerializeToString())
    unwrap_session(responseContent.session, session)
    response.content = responseContent.SerializeToString()
    outputWriter.write_output(response)
    
def get_class(kls):
    parts = kls.split('.')
    module = ".".join(parts[:-1])
    m = __import__( module )
    for comp in parts[1:]:
        m = getattr(m, comp)
    return m()
    
def has_class(kls):
    try:
        get_class(kls)
        return True
    except Exception as err:
        return False            
    
def has_actor(request, outputWriter):
    requestContent = actor_pb2.HasActorContent()
    requestContent.ParseFromString(request.content)
    actorClass = requestContent.actorClass
    response = actor_pb2.ActorMessage()
    response.code = actor_pb2.HAS_ACTOR_RESPONSE
    response.requestId = request.requestId
    responseContent = actor_pb2.HasActorResponseContent()
    responseContent.result = has_class(actorClass)
    response.content = responseContent.SerializeToString()
    outputWriter.write_output(response)
    
def add_actor(request, actors, actorsLock):
    requestContent = actor_pb2.AddActorContent()
    requestContent.ParseFromString(request.content)
    actorsLock.acquire()
    actors[requestContent.actorName] = get_class(requestContent.actorClass)
    actorsLock.release()

if __name__=='__main__':
    pipeReader = PipeReader(open(sys.argv[1], 'r'))
    outputWriter = OutputWriter(open(sys.argv[2], 'w'), Lock())
    add_job = ThreadPool(int(sys.argv[3]), sys.maxint).add_job    
    actors = {}
    actorsLock = Lock()
    print('Launcher started')
    while True:
        message = pipeReader.read_next()
        code = message.code
        if code == actor_pb2.INVOKE:
            add_job(invoke_actor, [message, outputWriter, actors, actorsLock])
        elif code == actor_pb2.HAS_ACTOR:
            add_job(has_actor, [message, outputWriter])
        elif code == actor_pb2.ADD_ACTOR:
            add_job(add_actor, [message, actors, actorsLock])
        else:
            raise Exception("Invalid request code - " + str(code))

