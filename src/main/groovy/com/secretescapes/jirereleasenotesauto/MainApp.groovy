package com.secretescapes.jirereleasenotesauto


/**
 * Entry point for application.
 */
class MainApp {

	/**
	 *
	 * @param args - Should take in an array of 3 characters.
	 *
	 * 1. Jira URL: e.g. http://myjira.atlassian.net
	 * 2. Base 64 encoded username and password in the format of <username password> - more info @ https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-basic-authentication
	 * 3. Release version number - e.g. 2.81
	 */
	public static void main(String[] args) {
		if (args.size() != 3) {
			throw new IllegalArgumentException("Must enter just three arguments - jira URL e.g. https://myjira.atlassian.net, Your encoded64 username and password, " +
					"and the release version number.\n Full command line example: e.g. jar https://myjira.atlassian.net ZGVWltMQ= 2.71")
		} else {
			JiraReleaseNotes jiraReleaseNotes = new JiraReleaseNotes()
			ReleaseNotes releaseNotes = jiraReleaseNotes.getFixNotes(args[0], args[1], args[2])
			String notesAsHTML = jiraReleaseNotes.formatAsHtml(releaseNotes)
			File file = new File('.', 'release-notes.html')
			file.write(notesAsHTML)
		}
	}
}
