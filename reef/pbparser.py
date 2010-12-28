"""
:synopsis: Parsing functions for protocol buffers.

This is created to be our interface to the google-generated parser scripts in
:mod:`vOutput_pb2`.

Any required libraries are stored with reef (in the :mod:`google` module).

"""

import os
import sys
import vOutput_pb2

def scan_output(path_to_output):
    """
    Parse output from a vazels experiment.

    This is the main entry point to the scanner.

    :param path_to_output: The path to an **existing** Output_Folder inside a
                           vazels experiment directory.
    :type path_to_output: ``str``
    :returns: A dictionary with the same hierarchy structure as Vazels
              outputs into the Output_Folder or ``None`` if there was an error.
    :rtype: ``dict``

    If you do not provide the path to an Output_Folder, bad things might
    happen.

    .. todo:: Check for specific types of failures.

    """

    #print (path_to_output)
    #if os.path.exists(path_to_output):
    #    result = scan_output_helper(path_to_output)
    #else:
    #    print "Folder provided did not exist"
    #    raise restlite.Status, "404 Output Folder Not Found"

    # Instead of how we did it before, let any exceptions spell failure.
    # This catches any problems with parsing too, although we should
    # probably look for specific failures
    try:
        return scan_output_helper(path_to_output)
    except:
        return None


def scan_output_helper(path):
    """
    Parse the Output_Folder from a Vazels experiment.

    This will recursively scan down through the directories looking for the
    files at the bottom, continually building dictionaries as it goes
    to store each layer in. Uses the current folder's name as the key for
    the dictionary. This ensures keys will not clash.

    :param path: Path to the Output_Folder from a Vazels experiment.
    :type path: ``str``
    :returns: A nested dictionary containing the experiment data.
    :rtype: ``dict``
    :raises: Absolutely anything - all exceptions are left to propagate.

    """

    file_paths = [os.path.join(path, name) for name in os.listdir(path)]

    # If any of the files in this directory are files, then they all are,
    # and they contain snapshots

    if not file_paths:
        return {}
    elif os.path.isfile(file_paths[0]) :
        snapshots_list = [process_file(name) for name in file_paths]
        snapshots_dict = {}
        # Join all the dictionaries together as one
        for snapshot in snapshots_list :
            snapshots_dict.update(snapshot)
        return snapshots_dict
    else:
        folder_dict = {}
        for name in file_paths:
            # Recurse through each folder building up a tree
            folder_dict[os.path.basename(name)] = scan_output_helper(name)
        return folder_dict


def process_file(filepath):
    """
    Generate a dictionary containing useable data from a buffered protocol file.

    :param filepath: Path to the protobuf file.
    :type filepath: ``str``
    :returns: A dictionary containing the data represented by the protobuf file.
    :rtype: ``dict``

    """

    # prepares an object for us to read the protocol buffer
    time_serie = vOutput_pb2.TimeSerie()
    with open(filepath, "rb") as f:
        time_serie.ParseFromString(f.read())

    # protocol buffer is now all in memory.

    time_serie_dict = {}

    for snapshot in time_serie.snapshot:

        snapshot_dict = {}
        snapshot_dict['timestamp'] = snapshot.timestamp
        snapshot_dict['actor'] = snapshot.actor

        # AFAIK the data value has many different types but there can only
        # be one data. i guess i just need one field for this in python

        if snapshot.value.HasField('doubleValue'):
            snapshot_dict['type'] = "Double"
            snapshot_dict['value'] = snapshot.value.doubleValue
        elif snapshot.value.HasField('stringValue'):
            snapshot_dict['type'] = "String"
            snapshot_dict['value'] = snapshot.value.stringValue
        elif snapshot.value.HasField('customValue'):
            snapshot_dict['type'] = "Byte"
            snapshot_dict['value'] = snapshot.value.customValue

        # Index them by timestamp instead of order in file
        time_serie_dict[snapshot_dict['timestamp']] = snapshot_dict

    return time_serie_dict
