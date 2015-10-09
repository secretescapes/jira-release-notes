package com.secretescapes.jirereleasenotesauto

import groovy.transform.ToString

/**
 * Container class for a 'Jira Ticket'. A 'Jira Ticket' can also be seen as a release feature, of which you can have multiple per release.
 */
@ToString
class JiraTicket {

	String id
	String key
	String apiLink
	String link
	String summary
	String note
	String team

}
