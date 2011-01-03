import restlite
import config
import authentication

### Collects data from OutputFolder and returns Tarball with raw data in ###
#
import os
# GET to fetch data
#

@restlite.resource
def download_data_handler():
    import parser
    import os
    import vazelsmanager
    import tarfile
    import tempfile
    ## requests will return the entire data available

    def GET(request):
        authentication.login(request)

        # We want the proper experiment path that vazelsmanager tells us
        exp_dir = vazelsmanager.getExperimentPath()
        path = os.path.join(exp_dir,"Output_Folder")

	# Make a temporary file, fill it with the Output_Folder's contents.
	with tempfile.SpooledTemporaryFile(1024) as temp:
		tar = tarfile.open(fileobj=temp, mode="w:gz")
		tar.add(name=path, arcname="Output_Folder")
		tar.close()

		temp.seek(0)
		return_data = temp.read()

		return request.response(return_data, "application/x-gzip")

	raise restlite.Status, "500 Could Not Serve Data File"
    return locals()

