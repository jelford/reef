import vazelsmanager
# from vazelsmanager import vazels_control_stdout_READ as vazels_control_stdout_READ
from threading import Timer
from time import sleep

__workloadScheduler = None

def applyWorkloads(pause=3) :
  __workloadScheduler = Timer(interval=pause, function=__applyWorkloadsToControlCentre)
  __workloadScheduler.start()
  
def __applyWorkloadsToControlCentre() :
  while(True): # Keep checking until we don't have to anymore
    runningState = vazelsmanager.vazelsRunning()
    if runningState is True:
      # TODO: Actually apply actors and workloads
      print("APPLYING WORKLOADS TO THE CONTROL CENTRE")
    elif runningState == "starting":
      # Go round again, after a quick pause
      print("Re-running workload applying fandango")
      sleep(2)
      continue
    elif runningState == "timout":
      vazelsmanager.vazels_control_process.terminate()
      print("TERMINATED CONTROL CENTRE BECAUSE OF A TIMEOUT")
    else:
      print("WORKLOAD APPLICATION FAILED: CONTROL CENTRE NOT RUNNING")
      
    break;
  