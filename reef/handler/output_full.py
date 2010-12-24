import restlite
import config
import authentication

### Collects data from OutputFolder and returns JSON object with data in ###
#
# GET to fetch data
#

@restlite.resource
def output_full_handler():
    import pbparser
    import os
    import vazelsmanager
    ## requests will return the entire data available

    def GET(request):
        authentication.login(request)

        # We want the proper experiment path that vazelsmanager tells us
        exp_dir = vazelsmanager.getExperimentPath()
        path = os.path.join(exp_dir,"Output_Folder")
        parsed = pbparser.scan_output(path)
        # If the parsing broke in any way we get back None
        if parsed is None:
            raise restlite.Status, "500 Could Not Read Output Data"
        return request.response(parsed)

    return locals()

