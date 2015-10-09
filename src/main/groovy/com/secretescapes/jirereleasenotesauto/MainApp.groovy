package com.secretescapes.jirereleasenotesauto


/**
 * Created by dean on 09/10/15.
 */
class MainApp {

	public static void main(String... args) {
		if (args.size() != 2) {
			throw new IllegalArgumentException("Must enter just two arguments - Your encoded64 username and password, the release version number. e.g. jar ZGVWltMQ= 2.71")
		} else {
			JiraReleaseNotes jiraReleaseNotes = new JiraReleaseNotes()
			ReleaseNotes releaseNotes = jiraReleaseNotes.getFixNotes(args[0], args[1])
			String notesAsHTML = jiraReleaseNotes.formatAsHtml(releaseNotes)
			println notesAsHTML
			notesAsHTML
		}
	}
}
