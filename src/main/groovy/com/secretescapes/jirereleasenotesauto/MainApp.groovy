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
	 * 4. (optional) list of ticket ids - e.g. DEV-3444,DEV-5421. Tickets will be attached to provided fixVersion
	 * 5. (optional) Release notes that should be used as release notes for tickets attached to fixVersion prvided as 4th param

	 */
	public static void main(String[] args) {
		if ((args.size() != 3) && (args.size() != 5)) {
			println "Number of arguments passed: " + args.size()
			throw new IllegalArgumentException(
			"Must enter three or four arguments \n" +
			 "- jira URL e.g. https://myjira.atlassian.net, \n" +
			 "- Your encoded64 username and password, " +
			 "- the release version number - either existing one - if you release tickets already linked to fixVersion, or not existing one - it you want to create fixVersion and link it to tickets listed in next param \n" +
			 "- (optional) list of ticket ids - e.g. DEV-3444,DEV-5421. Tickets will be attached to fixVersion provided in 3rd param \n"+
			 "- (optional) the release note that should be used for tickets listed in 4th param\n" +
			 " Full command line example: e.g. jar https://myjira.atlassian.net ZGVWltMQ= 2.71 " + 
			 " or jar https://myjira.atlassian.net ZGVWltMQ= 2.12.1 DEV-45234,DEV-55444 'js improvements related to rendering speed'") 
		} else {
			JiraReleaseNotes jiraReleaseNotes = new JiraReleaseNotes(args[0], args[1])
			if(args.size() == 5) {
				System.out.println('five args')
				jiraReleaseNotes.createFixVersionAndLinkWithTickets(args[2], args[3])
				ReleaseNotes notYetFilledNotes = jiraReleaseNotes.getFixNotes(args[2])
				notYetFilledNotes.releaseIssues.each { releaseIssue ->
					System.out.println(releaseIssue.apiLink  + releaseIssue.note)
					if(!releaseIssue.note) {
						println "Release notes for ${releaseIssue.id} not found, will update them now"
						jiraReleaseNotes.updateReleaseNotes(releaseIssue.id, args[4].trim())
					} else {
						println "Release notes for ${releaseIssue.id} already existed, no need to update"
					}
				}
			}

			ReleaseNotes releaseNotes = jiraReleaseNotes.getFixNotes(args[2])
			String notesAsHTML = jiraReleaseNotes.formatAsHtml(releaseNotes)
			File file = new File('.', 'release-notes.html')
			file.write(notesAsHTML)
		}
	}
}