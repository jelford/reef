"""
:synopsis: Interfaces with the vazels control centre.

Not much in here. Most of the dealing (including stopping the control centre)
is done by the command client in :mod:`commandclient`.

"""

import config
import os
import subprocess
import commandclient
import atexit

# When we start processes, we get a handle on them and can use that to
# terminate, check for termination, get return codes, ...

"""The control centre process."""
vazels_control_process = None


# DEFAULT SETTINGS FOR VAZELS - paths, certificates, ... 
config.getSettings("control_centre").setdefault("vazels_dir", "vazels")
config.getSettings("control_centre").setdefault("experiment_dir", "experiment")
config.getSettings("control_centre").setdefault("rmi_port", "1099")
config.getSettings("control_centre").setdefault("rmi_host", "localhost")
config.getSettings("control_centre").setdefault("rmi", "-start_rmi")
config.getSettings("control_centre").setdefault("siena", "-start_siena")
config.getSettings("control_centre").setdefault("out_file" ,"control_out")


def startVazels():
    """
    Start the vazels control centre.

    This should also set up the tear down when the control centre closes.

    :returns: ``True`` if the control centre is still running when we exit the function.
              (i.e. nothing has gone wrong yet.)
    :rtype: ``bool``

    """

    global vazels_control_process
  
    experiment_path = getExperimentPath()  
    group_data = config.getSettings("groups")
  
    # Work out the physical group numbers. Can't do this earlier as they can
    # change all the way up to the point when we start the server.
    group_list = []
    physical_group_number = 1
    for group in group_data:
        group_data[group]['group_number'] = physical_group_number
        physical_group_number += 1
        group_list.append(group + "," + str(group_data[group]["size"]))
  
    args = ['/bin/sh', 'vazels_control_centre.sh']
    args.append('--root_dir='+experiment_path+'/')
    args.append("--groups=" + ":".join(group_list))
    args.append('--ssh_server_identity='+config.getSettings('security')['certificate'])
    args.append(config.getSettings('control_centre')['rmi'])
    args.append(config.getSettings('control_centre')['siena'])
  
    # Wipes whatever was in the file before
    control_out = open(getControlCentreOutPath(), "w")
    vazels_control_process = subprocess.Popen(args, cwd=getVazelsPath(), stdout=control_out, stderr=subprocess.STDOUT)
  
    # Just to be safe when we leave the server (it should stop by itself...just this will shut down nicely hopefully)
    atexit.register(commandclient.stopVazels)

    # Return false if we know something's gone wrong already
    vazels_control_process.poll()
    return vazels_control_process.returncode == None
    

def runningState():
    """
    Check what state the vazels process is in.

    :returns: * ``True`` if the control centre is running.
              * ``False`` if the control centre is not running (or we don't know its running...).
              * ``None`` if we are in an intermediate state.
    :rtype: ``bool``
    :raises: :exc:`IOError` if the vazels output could not be read.

    """

    global vazels_control_process
  
    # If we haven't yet started the control centre
    if vazels_control_process is None:
        return False

    # If we have started but it died.
    if vazels_control_process.poll() is not None:
        return False
  
    # Read through what the control centre's told us & get the state from that
    # (if it is not any of the above cases it is either running or starting)
    with open(getControlCentreOutPath(), "r") as v_out:
        cur_line = None
        while cur_line != "": #Read through the whole thing
            cur_line = v_out.readline()
            if cur_line.find("[OK]") != -1:
                return True

    return None

  
def getVazelsPath():
    """
    Grab the path to the vazels installation directory.

    :returns: Absolute path to the vazels install directory.
    :rtype: ``str``

    """

    return os.path.join(
        config.getSettings("global")['basedir'],
        config.getSettings("control_centre")["vazels_dir"],
    )


def getExperimentPath():
    """
    Grab the experiment path.

    :returns: Absolute path to the vazels experiment for this project.
    :rtype: ``str``

    """

    return os.path.join(
        config.getSettings("global")['projdir'],
        config.getSettings("control_centre")["experiment_dir"]
    )
  

def getControlCentreOutPath():
    """
    Grab the control centre output file.

    :returns: Absolute path to the ouput file.
    :rtype: ``str``

    """

    return os.path.join(
        config.getSettings("global")["projdir"],
        config.getSettings("control_centre")["out_file"]
    )

