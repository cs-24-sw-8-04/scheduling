package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class DeletionFailedException(s: String, response: Response) : Throwable() {

}
