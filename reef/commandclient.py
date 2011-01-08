"""
:syopsis: Does any talking with the command client.

"""

import config
import controlcentre
import os
import tempfile
import subprocess
import restlite
from handler.groups import getGroupFromRank
from threading import Timer

# When we start processes, we get a handle on them and can use that to
# terminate, check for termination, get return codes, ...

experiment_running_state = None
"""
Experiment state - to remember whether the experiment has started.

Valid values: ``None``, ``Statuses["STATUS_EXPERIMENT_STARTED"]``
"""


Statuses = {"STATUS_RUNNING": "running",
            "STATUS_STARTING": "starting",
            "STATUS_READY": "ready",
            "STATUS_EXPERIMENT_STARTED" : "started",
            "STATUS_FINISHED" : "finished"}
"""Some constants for reflecting state info."""


def vazelsPhase():
    """
    Check the running status of vazels.

    :returns: The status. One of the values in :data:`Statuses`.
    :rtype: ``str``

    """

    global experiment_running_state
    global Statuses
  
    controlstat = controlcentre.runningState()

    if controlstat is False:
        return Statuses["STATUS_READY"]
  
    if controlstat is None:
        return Statuses["STATUS_STARTING"]

    if experiment_running_state is not None:
        return experiment_running_state
    else:
        return Statuses["STATUS_RUNNING"]

  
def getCommandLineClientPath():
    """
    Grab the path to the command client installation directory.

    :returns: The absolute path to the directory.
    :rtype: ``str``

    """

    return os.path.join(controlcentre.getVazelsPath(), 'client')


def stopVazels():
    """
    Tell Vazels to go ahead and die.

    :returns: Whether the control centre was running.
    :rtype: ``bool``

    """

    global experiment_running_state

    experiment_running_state = None
    return issueControlCentreCommand('stop')


def startexperiment():
    """
    Try to start the experiment

    :returns: Whether the control centre was running.
    :rtype: ``bool``

    """

    global experiment_running_state
    global Statuses
    experiment_running_state = Statuses["STATUS_EXPERIMENT_STARTED"]
    return issueControlCentreCommand('start')

  
def getalloutput():
    """
    Start pulling output from the vazels.

    :returns: Whether the control centre was running.
    :rtype: ``bool``

    """

    return issueControlCentreCommand('getalloutput')

  
def updateStatuses():
    """
    Update the host statuses.

    :returns: Whether the control centre was running.
    :rtype: ``bool``

    """

    return issueControlCentreCommand('getallstatus', afterwards=__readStatusReturnValue)


def __readStatusReturnValue(iofile):
    """
    Read command client output to find and set host statuses.

    ..todo:: Make me more robust. Comments in the source.
             After a little refactor I have also noticed that this will throw
             status exceptions into nowhere. Hopefully they don't kill the server.

    :param iofile: The output file from the command client.
                   It must not be still being written to.
    :type iofile: ``file``
    :raises: :exc:`restlite.Status` 500 if we don't have a group
             for one of the group numbers.

             :exc:`restlite.Status` 500 if we encounter a status we don't understand.

    """

    FIRST_GROUP_RANK = 1
  
    iofile.seek(0)
    lastLine = "nothingiwanttofind"
  
    # Seek the first line of the output where we talk about group status
    while lastLine.find("group rank "+str(FIRST_GROUP_RANK)) == -1:
        lastLine = iofile.readline()
        if not lastLine:
            return # No statuses to deal with
  
    # TODO: Tidy this up and use string manipulation instead of brittle counting!
    # for now, easier just to count the group numbers rather than doing string manipulation
    current_group_rank = FIRST_GROUP_RANK
  
    while lastLine:
        current_group = getGroupFromRank(current_group_rank)
    
        for host in range(current_group['size']):
            status_text = lastLine.split('-->')[-1].strip()
      
            # Statuses are of the form 'STATUS @ hostname', so split this up.
            status, host_name = (lambda l: (l[0], l[-1]))(status_text.split(' '))
      
            if not __updateHostStatus(current_group, status, host_name):
                raise restlite.Status, "500 Something went seriously wrong trying to find status of hosts!"
      
            lastLine = vazels_command_stdout_READ.readline()
    
        current_group_rank += 1


def __updateHostStatus(group, status, host=None):
    """
    Update the given group with it's new status.

    :param group: An actual group object to change.
    :type group: :class:`handler.groups.Group`
    :param status: The status of the host.
    :type status: ``str``
    :param host: The name of a host if needed.
    :type host: ``str``
    :returns: Whether or not the status was valid.
    :rtype: ``bool``

    """

    def __host_free(group, host_name):
        pass # TODO: Maybe do something more intelligent with this in the future?
    def __host_evolving(group, host_name):
        group['evolving_hosts'].add(host_name)
    def __host_connected(group, host_name):
        try:
            group['evolving_hosts'].remove(host_name)
        except KeyError: pass
        group['online_hosts'].add(host_name)
    
    try:
        {
            'FREE' : __host_free,
            'CONNECTED' : __host_connected,
            'EVOLVING' : __host_evolving
        }[status](group, host)
    except KeyError:
        return False
    
    return True

  
def __getCommandLineClientArgs():
    """
    Get the arguments needed to start the command line client.

    :returns: List of the necessary arguments.
    :rtype: ``list(str)``

    """

    return [
        '/bin/sh',
        'commandline_client.sh',
        '--rmi_host='+config.getSettings('control_centre')['rmi_host'],
        '--rmi_port='+config.getSettings('control_centre')['rmi_port']
    ]


def issueControlCentreCommand(command, args=[], extraargs=[], afterwards=None):
    """
    Send a command to the command client and optionally run a function on finish.

    :param command: The command to run.
    :type command: ``str``
    :param args: List of arguments to the command. (optional)
    :type args: ``list(str)``
    :param extraargs: Arguments to the script, i.e. those beginning with "--". (optional)
    :type extraargs: ``dict``
    :param afterwards: The function to run afterwards (optional).
    :type afterwards: ``func``, takes the output file as a parameter. 
    :returns: ``True`` if the control centre is running, ``False`` otherwise.
    :rtype: ``bool``

    """

    if controlcentre.runningState() is not True:
        return False
  
    experiment_path = controlcentre.getExperimentPath()
    clientargs = __getCommandLineClientArgs()
    for arg in extraargs:
        clientargs.append(arg + "=" + extraargs[arg])
    clientargs.append(command)
    clientargs.extend(args)

    ioFile = open("/dev/null")
    try:
        ioFile = tempfile.SpooledTemporaryFile(256)
    except: pass # Ignore fuckups and use /dev/null instead

    command_process = subprocess.Popen(clientargs, cwd=getCommandLineClientPath(), stdout=ioFile, stderr=subprocess.STDOUT)

    if afterwards is not None:
        def _runWhenDone(process, func, io):
            if process.poll() is None:
                Timer(interval=1, function=_runWhenDone, args=[process, func, io]).start()
            else:
                func(io)
                io.close()

        _runWhenDone(command_process, afterwards, ioFile)

    return True

